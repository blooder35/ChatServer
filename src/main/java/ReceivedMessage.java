public final class ReceivedMessage {
    private String time;
    private String message;

    public ReceivedMessage(String time, String message) {
        this.time = time;
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }
}
