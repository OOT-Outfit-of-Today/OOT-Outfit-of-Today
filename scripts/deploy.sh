#!/usr/bin/env bash
set -euo pipefail

# ===== 필수 환경변수 점검 =====
: "${AWS_REGION:?AWS_REGION required}"
: "${EC2_INSTANCE_ID:?EC2_INSTANCE_ID required}"
: "${FULL_URI:?FULL_URI required}"
: "${CONTAINER_NAME:?CONTAINER_NAME required}"
: "${APP_PORT:?APP_PORT required}"
: "${SPRING_PROFILE:?SPRING_PROFILE required}"

# ===== ECR 경로 파싱 =====
REG_URI="$(echo "${FULL_URI}" | cut -d/ -f1)"
REPO_AND_TAG="$(echo "${FULL_URI}" | cut -d/ -f2- )"
REPO="$(echo "${REPO_AND_TAG}" | rev | cut -d: -f2- | rev)"
TAG="$(echo "${REPO_AND_TAG}"  | awk -F: '{print $NF}')"

# SSM 코멘트(100자 제한 방어)
COMMENT="Deploy ${REPO}:${TAG}"
if [ ${#COMMENT} -gt 100 ]; then
  COMMENT="${COMMENT:0:100}"
fi

echo "[INFO] FULL_URI=${FULL_URI}"
echo "[INFO] REG_URI=${REG_URI}"
echo "[INFO] EC2_INSTANCE_ID=${EC2_INSTANCE_ID}"
echo "[INFO] COMMENT=${COMMENT}"

# ===== EC2에서 실행할 커맨드(배열로 안전하게 정의) =====
CMDS=(
  "echo '========================================'"
  "echo '  Deployment Started'"
  "echo '========================================'"

  "echo '[Step 1/7] Logging in to ECR...'"
  "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${REG_URI}"
  "echo '✓ ECR login successful'"

  # 이미지 자동 정리: 7일(168시간) 이상 된 오래된 이미지 삭제
  "echo ''"
  "echo '[Step 2/7] Cleaning up old Docker images...'"
  "docker image prune -a --filter 'until=168h' --force || true"
  "echo '✓ Cleanup completed'"

  "echo ''"
  "echo '[Step 3/7] Pulling new image...'"
  "docker pull ${FULL_URI}"
  "echo '✓ Image pulled successfully'"

  "echo ''"
  "echo '[Step 4/7] Stopping and removing old container...'"
  "docker stop ${CONTAINER_NAME} || true"
  "docker rm   ${CONTAINER_NAME} || true"
  "echo '✓ Old container removed'"

  # 로그 디렉토리 생성 및 spring 유저(999:999)에게 권한
  "mkdir -p /app-logs && chown 999:999 /app-logs"

  # Parameter Store에서 Redis 설정 가져오기
  "echo ''"
  "echo '[Step 5/7] Fetching Redis configuration from Parameter Store...'"
  "REDIS_HOST=\$(aws ssm get-parameter \\
    --name /config/${SPRING_PROFILE}/REDIS_HOST \\
    --query Parameter.Value \\
    --output text \\
    --region ${AWS_REGION}) || { echo 'Error: Failed to retrieve REDIS_HOST' >&2; exit 1; }"

  "REDIS_PORT=\$(aws ssm get-parameter \\
    --name /config/${SPRING_PROFILE}/REDIS_PORT \\
    --query Parameter.Value \\
    --output text \\
    --region ${AWS_REGION}) || { echo 'Error: Failed to retrieve REDIS_PORT' >&2; exit 1; }"

  "REDIS_PASSWORD=\$(aws ssm get-parameter \\
    --name /config/${SPRING_PROFILE}/REDIS_PASSWORD \\
    --with-decryption \\
    --query Parameter.Value \\
    --output text \\
    --region ${AWS_REGION}) || { echo 'Error: Failed to retrieve REDIS_PASSWORD' >&2; exit 1; }"

  # 값이 비어있는지 검증
  "[ -n \"\$REDIS_HOST\" ] || { echo 'Error: REDIS_HOST is empty' >&2; exit 1; }"
  "[ -n \"\$REDIS_PORT\" ] || { echo 'Error: REDIS_PORT is empty' >&2; exit 1; }"
  "[ -n \"\$REDIS_PASSWORD\" ] || { echo 'Error: REDIS_PASSWORD is empty' >&2; exit 1; }"
  "echo '✓ Redis configuration loaded'"

  # Spring Boot 실행(메모리 제한 추가)
  "echo ''"
  "echo '[Step 6/7] Starting new container with memory limits...'"
  "docker run -d \\
    --name ${CONTAINER_NAME} \\
    --restart=always \\
    --memory=650m \\
    --memory-swap=650m \\
    -p ${APP_PORT}:${APP_PORT} \\
    -v /app-logs:/app-logs \\
    -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILE} \\
    -e AWS_REGION=${AWS_REGION} \\
    -e REDIS_HOST=\${REDIS_HOST} \\
    -e REDIS_PORT=\${REDIS_PORT} \\
    -e REDIS_PASSWORD=\${REDIS_PASSWORD} \\
    ${FULL_URI}"

  # 동적 컨테이너 시작 대기 및 실패 감지(docker inspect로 정확한 상태 확인)
  "echo ''"
  "echo 'Waiting for container to start...'"
  "for i in {1..30}; do
    CONTAINER_STATUS=\$(docker inspect -f '{{.State.Status}}' ${CONTAINER_NAME} 2>/dev/null || echo 'not_found')
    if [ \"\$CONTAINER_STATUS\" = \"running\" ]; then
      echo \"✓ Container is running(attempt \$i/30)\"
      break
    fi
    if [ \$i -eq 30 ]; then
      echo '========================================' >&2
      echo '✗ ERROR: Container failed to start' >&2
      echo '========================================' >&2
      echo '' >&2
      echo \"Container Status: \$CONTAINER_STATUS\" >&2
      echo '' >&2
      docker ps -a --filter name=${CONTAINER_NAME} >&2 || true
      echo '' >&2
      echo 'Container Logs(last 50 lines):' >&2
      docker logs ${CONTAINER_NAME} --tail 50 >&2 || true
      echo '' >&2
      echo '========================================' >&2
      exit 1
    fi
    echo \"Waiting for container...(attempt \$i/30, status: \$CONTAINER_STATUS)\"
    sleep 1
  done"

  # 애플리케이션 헬스체크(Spring Boot Actuator health endpoint with /api context path)
  "echo ''"
  "echo '[Step 7/7] Checking application health...'"
  "for i in {1..60}; do
    HEALTH_STATUS=\$(curl -f -s http://localhost:${APP_PORT}/api/actuator/health 2>/dev/null | grep -o '\"status\":\"UP\"' || echo '')
    if [ -n \"\$HEALTH_STATUS\" ]; then
      echo \"✓ Application is healthy(attempt \$i/60)\"
      echo \"Health response: \$(curl -s http://localhost:${APP_PORT}/api/actuator/health 2>/dev/null)\"
      break
    fi
    if [ \$i -eq 60 ]; then
      echo '========================================' >&2
      echo '✗ ERROR: Application failed to become healthy' >&2
      echo '========================================' >&2
      echo '' >&2
      echo 'Health Check Endpoint:' >&2
      curl -v http://localhost:${APP_PORT}/api/actuator/health >&2 || echo 'Health endpoint not responding' >&2
      echo '' >&2
      echo 'Container Status:' >&2
      docker ps --filter name=${CONTAINER_NAME} >&2
      echo '' >&2
      echo 'Container Logs(last 50 lines):' >&2
      docker logs ${CONTAINER_NAME} --tail 50 >&2
      echo '' >&2
      echo '========================================' >&2
      exit 1
    fi
    echo \"Waiting for application health...(attempt \$i/60)\"
    sleep 1
  done"

  "echo ''"
  "echo '========================================'"
  "echo '  Deployment Status'"
  "echo '========================================'"
  "echo 'Container Status:'"
  "docker ps --filter name=${CONTAINER_NAME} --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'"
  "echo ''"
  "echo 'Application Health:'"
  "curl -s http://localhost:${APP_PORT}/api/actuator/health | head -20 || echo 'Unable to fetch health status'"
  "echo ''"
  "echo 'Memory Status:'"
  "free -h"
  "echo ''"
  "echo 'Recent Logs(last 10 lines):'"
  "docker logs ${CONTAINER_NAME} --tail 10"
  "echo ''"
  "echo '========================================'"
  "echo '  ✓ Deployment Completed Successfully'"
  "echo '========================================'"
)

# Bash 배열 → JSON 배열 변환(jq 필수)
COMMANDS_JSON=$(jq -Rn --argjson arr "$(printf '%s\n' "${CMDS[@]}" | jq -R . | jq -s .)" '$arr')
echo "[DEBUG] COMMANDS_JSON=${COMMANDS_JSON}"

# ===== SSM 명령 전송 =====
RESP=$(aws ssm send-command \
  --document-name "AWS-RunShellScript" \
  --comment "${COMMENT}" \
  --targets "Key=instanceIds,Values=${EC2_INSTANCE_ID}" \
  --parameters "{\"commands\": ${COMMANDS_JSON}}" \
  --region "${AWS_REGION}" \
  --output json)

CMD_ID=$(echo "${RESP}" | jq -r '.Command.CommandId')
echo "[INFO] SSM CommandId: ${CMD_ID}"

# ===== 완료 대기/성공 판정 =====
for i in {1..30}; do
  STATUS=$(aws ssm get-command-invocation \
    --command-id "${CMD_ID}" \
    --instance-id "${EC2_INSTANCE_ID}" \
    --query 'Status' \
    --output text \
    --region "${AWS_REGION}") || true

  echo "[INFO] SSM Status: ${STATUS}"

  case "${STATUS}" in
    Success) exit 0 ;;
    Failed|Cancelled|TimedOut)
      echo "========================================"
      echo "  SSM Command Failed: ${STATUS}"
      echo "========================================"
      echo ""
      echo "Attempting to fetch error logs from EC2..."
      echo ""

      # 실패 원인 파악을 위한 로그 출력(에러도 캡처)
      SSM_RESULT=$(aws ssm get-command-invocation \
        --command-id "${CMD_ID}" \
        --instance-id "${EC2_INSTANCE_ID}" \
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
      if [ -z "$SSM_RESULT" ] || ! echo "${SSM_RESULT}" | jq -e . >/dev/null 2>&1; then
        echo "Unable to parse logs from SSM"
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
      echo "========================================"
      exit 1
      ;;
  esac

  sleep 5
done

echo "[ERROR] SSM command did not complete in time"
exit 1