public final class SocketMessage {
    private String method;
    private String body;

    public SocketMessage(String method, String body) {
        this.method = method;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }
}
