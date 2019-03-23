public enum DBCommunicationProtocol {
    DB_ACCESS_GRANTED("granted"),
    DB_ACCESS_DENIED_WRONG_PASSWORD("PasswordNotCorrect"),
    DB_ACCESS_DENIED_USER_NOT_FOUND("UserNotFound"),
    DB_ACCESS_DENIED_USER_ALREADY_LOGGED_IN("UserAlreadyLoggedIn"),
    DB_REGISTRATION_FAILED_USER_ALREADY_REGISTERED("User with this name already registered"),
    DB_REGISTRATION_SUCCESS("completed"),
    DB_REGISTRATION_FAILED("registrationFailed");

    private String value;

    DBCommunicationProtocol(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
