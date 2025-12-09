package com.eparapheur.core.models;

public class HttpContextStatus {
    public static final int PAYMENT_ERROR =9008;
    // Success Codes
    public static int SUCCESS_OPERATION = 7000;
    public static int SUCCESS_GET_TOKEN = 7001;
    public static int SUCCESS_OTP_SEND = 7002;
    public static int SUCCESS_USER_REGISTERED = 7003;
    public static int SUCCESS_JOB_POST_CREATED = 7004;
    public static int SUCCESS_APPLICATION_SUBMITTED = 7005;

    // Client Error Codes
    public static int ERROR_ACTION_NOT_AUTHORIZE = 8000;
    public static int ERROR_AUTHENTICATION_BEARER_NOT_FOUND = 8002;
    public static int ERROR_AUTHENTICATION_HEADER_NOT_FOUND = 8003;
    public static int ERROR_RESOURCE_NOT_FOUND = 8004;
    public static int ERROR_DATA_NOT_VALID = 8006;
    public static int ERROR_AUTHENTICATION_FAIL = 8007;
    public static int ERROR_USER_NOT_FOUND = 8008;
    public static int ERROR_ACCOUNT_NOT_EXIST = 8088;
    public static int ERROR_USER_ALREADY_EXISTS = 8009;
    public static int ERROR_APPLICATION_LIMIT_REACHED = 8010;
    public static int ERROR_INVALID_SESSION = 8012;
    public static int ERROR_BAD_FILE_FORMAT = 8013;
    public static int ERROR_BAD_FILE_EXTENSION = 8014;
    public static int ERROR_FAIL_DOWNLOAD_FILE_BY_URL = 8015;
    public static int ERROR_FAIL_DOWNLOAD_FILE = 8016;
    public static int ERROR_OTP_EXPIRED = 8017;
    public static int ERROR_FAIL_OTP_GENERATION = 8018;
    public static int ERROR_JOB_POST_LIMIT_REACHED = 8019;
    public static int ERROR_FAIL_RENEWAL_SESSION = 8020;
    public static int ERROR_FAIL_UPDATE_PROFILE = 8030;
    public static int ERROR_FAIL_CREATION = 8031;
    public static int ERROR_FAIL_EDITION = 8034;
    public static int ERROR_FAIL_DELETION = 8032;
    public static int ERROR_FAIL_WRITE = 8033;

    // Server Error Codes
    public static int SERVER_ERROR = 9000;
    public static int SERVER_ERROR_OTP_CANNOT_GENERATED = 9001;
    public static int SERVER_ERROR_OTP_SEND_FAIL = 9004;
    public static int SERVER_ERROR_CANNOT_GET_JOB_ID = 9005;
    public static int SERVER_ERROR_INCORRECT_HASH = 9006;
    public static int SERVER_ERROR_DATABASE_CONNECTION_FAIL = 9007;

    // Platform-Specific Codes
    public static int ERROR_JOB_NOT_FOUND = 8100;
    public static int ERROR_APPLICATION_NOT_FOUND = 8101;
    public static int ERROR_RECRUITER_NOT_FOUND = 8102;
    public static int ERROR_RESUME_NOT_FOUND = 8103;
    public static int ERROR_CANDIDATE_ALREADY_APPLIED = 8104;
    public static int ERROR_INCOMPLETE_PROFILE = 8105;
}
