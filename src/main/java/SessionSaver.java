import org.apache.mina.core.session.IoSession;

public class SessionSaver {
  static IoSession[] sessionArr;
  private static final int MAXIMUM=100;
  public SessionSaver(){
    sessionArr=new IoSession[MAXIMUM];
  }
  public static void addSession(IoSession session){
    for (int i = 0; i < MAXIMUM; i++) {
      if (sessionArr[i] == null) {
        sessionArr[i]=session;
        System.out.println("session added at:"+i);
        break;
      }
    }
  }
  public static void sendStringToAllExcept(IoSession session,String message){
    for (IoSession ses : sessionArr) {
      if (ses != null) {
        if (ses.getAttribute("login") != session.getAttribute("login")) {
          //System.out.println("SENT TO" + ses.getAttribute("login"));
          ses.write(message);
        }
      }
    }
  }

  public static void sendToAll(String message) {
    for (IoSession ses : sessionArr) {
      if (ses != null) {
        ses.write(message);
      }
    }
  }

  public static void removeSession(IoSession session) {
    for (int i = 0; i < MAXIMUM; i++) {
      if (sessionArr[i] != null) {
        if (sessionArr[i].equals(session)) {
          sessionArr[i]=null;
        }
      }
    }
  }
}
