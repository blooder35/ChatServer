public final class LoginStatusBody {
    private String username;
    private String status;

    public LoginStatusBody(String username, String status) {
        this.username = username;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }
}
