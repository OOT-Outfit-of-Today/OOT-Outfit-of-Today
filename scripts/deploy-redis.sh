#!/usr/bin/env bash
set -euo pipefail

# ===== 필수 환경변수 점검 =====
: "${AWS_REGION:?AWS_REGION required}"
: "${REDIS_EC2_INSTANCE_ID:?REDIS_EC2_INSTANCE_ID required}"
: "${FULL_URI:?FULL_URI required}"
: "${CONTAINER_NAME:?CONTAINER_NAME required}"
: "${REDIS_PORT:?REDIS_PORT required}"
: "${SPRING_PROFILE:?SPRING_PROFILE required}"

# ===== ECR 경로 파싱 =====
REG_URI="$(echo "${FULL_URI}" | cut -d/ -f1)"
REPO_AND_TAG="$(echo "${FULL_URI}" | cut -d/ -f2- )"
REPO="$(echo "${REPO_AND_TAG}" | rev | cut -d: -f2- | rev)"
TAG="$(echo "${REPO_AND_TAG}"  | awk -F: '{print $NF}')"

# SSM 코멘트(100자 제한 방어)
COMMENT="Deploy Redis ${REPO}:${TAG}"
if [ ${#COMMENT} -gt 100 ]; then
  COMMENT="${COMMENT:0:100}"
fi

echo "[INFO] FULL_URI=${FULL_URI}"
echo "[INFO] REG_URI=${REG_URI}"
echo "[INFO] REDIS_EC2_INSTANCE_ID=${REDIS_EC2_INSTANCE_ID}"
echo "[INFO] COMMENT=${COMMENT}"

# ===== EC2에서 실행할 커맨드(배열로 안전하게 정의) =====
CMDS=(
  "echo '=============================================='"
  "echo '  Redis Deployment Started'"
  "echo '=============================================='"

  "echo '[Step 1/6] Logging in to ECR...'"
  "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${REG_URI}"
  "echo '✓ ECR login successful'"

  "echo ''"
  "echo '[Step 2/6] Pulling new Redis image...'"
  "docker pull ${FULL_URI}"
  "echo '✓ Redis image pulled successfully'"

  "echo ''"
  "echo '[Step 3/6] Stopping and removing old Redis container...'"
  "docker stop ${CONTAINER_NAME} || true"
  "docker rm   ${CONTAINER_NAME} || true"
  "echo '✓ Old Redis container removed'"

  # Parameter Store에서 Redis 비밀번호 가져오기
  "echo ''"
  "echo '[Step 4/6] Fetching Redis password from Parameter Store...'"
  "REDIS_PASSWORD=\$(aws ssm get-parameter \\
    --name /config/${SPRING_PROFILE}/REDIS_PASSWORD \\
    --with-decryption \\
    --query Parameter.Value \\
    --output text \\
    --region ${AWS_REGION}) || { echo 'Error: Failed to retrieve REDIS_PASSWORD' >&2; exit 1; }"

  # Redis 비밀번호 검증
  "[ -n \"\$REDIS_PASSWORD\" ] || { echo 'Error: REDIS_PASSWORD is empty' >&2; exit 1; }"
  "echo '✓ Redis password loaded'"

  # Redis 컨테이너 실행
  # restart policy 변경: always -> on-failure:5
  # - Redis 메모리 릭 등으로 인한 무한 재시작 루프 방지
  # - 최대 5번 재시작 시도 후 중단하여 명확한 장애 상태 유지
  # - 일시적 장애는 자동 복구하되, 지속적 문제는 수동 개입 필요하도록 설정
  # 메모리 제한 추가: 600m
  # - Redis 프로세스 maxmemory(512mb) + 오버헤드 고려하여 600mb로 설정
  # - Swap 방지를 위해 memory-swap도 동일하게 설정
  "echo ''"
  "echo '[Step 5/6] Starting new Redis container...'"
  "docker run -d \\
    --name ${CONTAINER_NAME} \\
    --restart=on-failure:5 \\
    --memory=600m \\
    --memory-swap=600m \\
    -p ${REDIS_PORT}:${REDIS_PORT} \\
    -v redis-data:/data \\
    ${FULL_URI} \\
    redis-server \\
      --requirepass \"\$REDIS_PASSWORD\" \\
      --maxmemory 512mb \\
      --maxmemory-policy allkeys-lru \\
      --appendonly yes"

  # 동적 컨테이너 시작 대기 및 실패 감지(docker inspect로 정확한 상태 확인)
  "echo ''"
  "echo 'Waiting for Redis container to start...'"
  "for i in {1..30}; do
    CONTAINER_STATUS=\$(docker inspect -f '{{.State.Status}}' ${CONTAINER_NAME} 2>/dev/null || echo 'not_found')
    if [ \"\$CONTAINER_STATUS\" = \"running\" ]; then
      echo \"✓ Redis container is running (attempt \$i/30)\"
      break
    fi
    if [ \$i -eq 30 ]; then
      echo '==============================================' >&2
      echo '✗ ERROR: Redis container failed to start' >&2
      echo '==============================================' >&2
      echo '' >&2
      echo \"Container Status: \$CONTAINER_STATUS\" >&2
      echo '' >&2
      docker ps -a --filter name=${CONTAINER_NAME} >&2 || true
      echo '' >&2
      echo 'Container Logs (last 50 lines):' >&2
      docker logs ${CONTAINER_NAME} --tail 50 >&2 || true
      echo '' >&2
      echo '==============================================' >&2
      exit 1
    fi
    echo \"Waiting for Redis container... (attempt \$i/30, status: \$CONTAINER_STATUS)\"
    sleep 1
  done"

  # Redis 헬스체크
  "echo ''"
  "echo '[Step 6/6] Checking Redis health...'"
  "for i in {1..30}; do
    REDIS_RESPONSE=\$(docker exec ${CONTAINER_NAME} redis-cli -a \"\$REDIS_PASSWORD\" ping 2>/dev/null || echo 'FAILED')
    if [ \"\$REDIS_RESPONSE\" = \"PONG\" ]; then
      echo \"✓ Redis is healthy (attempt \$i/30)\"
      break
    fi
    if [ \$i -eq 30 ]; then
      echo '==============================================' >&2
      echo '✗ ERROR: Redis health check failed' >&2
      echo '==============================================' >&2
      echo '' >&2
      echo 'Container Status:' >&2
      docker ps --filter name=${CONTAINER_NAME} >&2
      echo '' >&2
      echo 'Container Logs (last 50 lines):' >&2
      docker logs ${CONTAINER_NAME} --tail 50 >&2
      echo '' >&2
      echo '==============================================' >&2
      exit 1
    fi
    echo \"Waiting for Redis health... (attempt \$i/30)\"
    sleep 1
  done"

  "echo ''"
  "echo '=============================================='"
  "echo '  Redis Deployment Status'"
  "echo '=============================================='"
  "echo 'Container Status:'"
  "docker ps --filter name=${CONTAINER_NAME} --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'"
  "echo ''"
  "echo 'Redis Info:'"
  "docker exec ${CONTAINER_NAME} redis-cli -a \"\$REDIS_PASSWORD\" info server | grep -E '(redis_version|uptime_in_seconds|used_memory_human)' || echo 'Unable to fetch Redis info'"
  "echo ''"
  "echo 'Memory Status:'"
  "free -h"
  "echo ''"
  "echo '=============================================='"
  "echo '  ✓ Redis Deployment Completed Successfully'"
  "echo '=============================================='"
)

# Bash 배열 → JSON 배열 변환(jq 필수)
COMMANDS_JSON=$(jq -Rn --argjson arr "$(printf '%s\n' "${CMDS[@]}" | jq -R . | jq -s .)" '$arr')
echo "[DEBUG] COMMANDS_JSON=${COMMANDS_JSON}"

# ===== SSM 명령 전송 =====
RESP=$(aws ssm send-command \
  --document-name "AWS-RunShellScript" \
  --comment "${COMMENT}" \
  --targets "Key=instanceIds,Values=${REDIS_EC2_INSTANCE_ID}" \
  --parameters "{\"commands\": ${COMMANDS_JSON}}" \
  --region "${AWS_REGION}" \
  --output json)

CMD_ID=$(echo "${RESP}" | jq -r '.Command.CommandId')
echo "[INFO] SSM CommandId: ${CMD_ID}"

# ===== 완료 대기/성공 판정 =====
for i in {1..30}; do
  STATUS=$(aws ssm get-command-invocation \
    --command-id "${CMD_ID}" \
    --instance-id "${REDIS_EC2_INSTANCE_ID}" \
    --query 'Status' \
    --output text \
    --region "${AWS_REGION}") || true

  echo "[INFO] SSM Status: ${STATUS}"

  case "${STATUS}" in
    Success) exit 0 ;;
    Failed|Cancelled|TimedOut)
      echo "=============================================="
      echo "  SSM Command Failed: ${STATUS}"
      echo "=============================================="
      echo ""
      echo "Attempting to fetch error logs from EC2..."
      echo ""

      # 실패 원인 파악을 위한 로그 출력(에러도 캡처)
      SSM_RESULT=$(aws ssm get-command-invocation \
        --command-id "${CMD_ID}" \
        --instance-id "${REDIS_EC2_INSTANCE_ID}" \
        --query '{stdOut: StandardOutputContent, stdErr: StandardErrorContent}' \
        --output json \
        --region "${AWS_REGION}" 2>&1)

      # AWS CLI 명령 자체의 성공/실패 확인
      if [ $? -ne 0 ]; then
        echo "Failed to fetch detailed logs from EC2 instance"
        echo "This can occur when the command has expired or been deleted"
        echo ""
        echo "AWS CLI Error:"
        echo "${SSM_RESULT}"
        exit 1
      fi

      # JSON 파싱 시도
      if ! echo "${SSM_RESULT}" | jq -e . >/dev/null 2>&1; then
        echo "Unable to parse SSM response"
        echo "Raw response:"
        echo "${SSM_RESULT}"
        exit 1
      fi

      echo "--- Standard Output ---"
      echo "${SSM_RESULT}" | jq -r '.stdOut // "No output available"'

      echo ""
      echo "--- Standard Error ---"
      echo "${SSM_RESULT}" | jq -r '.stdErr // "No errors available"'

      echo ""
      echo "=============================================="
      exit 1
      ;;
  esac

  sleep 5
done

echo "[ERROR] SSM command did not complete in time"
exit 1