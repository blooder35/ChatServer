public class Message {
  private String sender;
  private String time;
  private String message;

  public Message(String sender, String time, String message) {
    this.sender = sender;
    this.time = time;
    this.message = message;
  }

  public String getMessageAsStringToSend() {
    StringBuilder sb = new StringBuilder("");
    sb.append("message ");
    sb.append(sender + " [");
    sb.append(time + "]:");
    sb.append("//&&//");
    sb.append(message);
    sb.append("//&&//");
    sb.append("//&&//");
    return sb.toString();
  }
}
