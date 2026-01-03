package org.example.ootoutfitoftoday.common.constant;

public final class ValidationRegex {

    private ValidationRegex() {
        throw new AssertionError("Utility class는 인스턴스화할 수 없습니다.");
    }

    /**
     * ID 형식 검증용 정규표현식
     * 영문 대소문자, 숫자, 언더스코어(_)만 허용
     */
    public static final String ID_REGEX = "^[a-zA-Z0-9_]+$";

    /**
     * 이메일 로컬 파트(@ 앞부분)
     * - 시작과 끝에 점(.) 불가
     * - 영문, 숫자, 특수문자(._%+-) 허용
     */
    public static final String EMAIL_LOCAL_PART = "(?!\\.)[A-Za-z0-9._%+-]+(?<!\\.)";

    /**
     * 이메일 도메인 파트(@ 뒷부분, TLD 제외)
     * - 영문, 숫자, 하이픈(-) 허용
     * - 하이픈으로 시작하거나 끝날 수 없음
     */
    public static final String EMAIL_DOMAIN = "@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*";

    /**
     * 이메일 최상위 도메인(TLD)
     * - 최소 2자 이상의 영문자
     */
    public static final String EMAIL_TLD = "\\.[A-Za-z]{2,}";

    /**
     * 완성된 이메일 검증용 정규표현식
     */
    public static final String EMAIL_REGEX = "^" + EMAIL_LOCAL_PART + EMAIL_DOMAIN + EMAIL_TLD + "$";

    /**
     * 닉네임 형식 검증용 정규표현식
     * 앞뒤 공백 불가, 중간 공백은 허용
     */
    public static final String NICKNAME_REGEX = "^(?!\\s).*(?<!\\s)$";

    /**
     * 사용자명 형식 검증용 정규표현식
     * 공백 문자 불가
     */
    public static final String USERNAME_REGEX = "^\\S+$";

    /**
     * 비밀번호 형식 검증용 정규표현식
     * - 최소 1개 이상의 영문자, 숫자, 특수문자 필수
     * - 공백 문자 불가
     */
    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[^\\s]+$";

    /**
     * 한국 휴대전화번호 형식 검증용 정규표현식
     * - 010, 011, 016, 017, 018, 019로 시작
     * - 총 11자리(010XXXXXXXX 형태)
     */
    public static final String PHONE_NUMBER_REGEX = "^01[016789]\\d{8}$";
}