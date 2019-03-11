public class User {
  private String username;
  private int status;
  public User(String username, int status) {
    this.username=username;
    this.status=status;
  }

  public Object getUserAsStringToSend() {
    StringBuilder sb = new StringBuilder("");
    sb.append(MethodChecker.USER_STATUS_METHOD + " ");
    sb.append(username + " ");
    sb.append(status);
    return sb.toString();
  }
}
