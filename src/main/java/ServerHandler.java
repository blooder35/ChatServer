import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class ServerHandler extends IoHandlerAdapter {

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
    cause.printStackTrace();
  }

  @Override
  public void messageReceived(IoSession session, Object message) throws Exception {
    System.out.println(message);
    MethodChecker.CheckMethod(message.toString(),session);

  }

  @Override
  public void messageSent(IoSession session, Object message) throws Exception {
    super.messageSent(session, message);
  }

  @Override
  public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
    System.out.println("IDLE: " + session.getIdleCount(status));
  }

  @Override
  public void sessionClosed(IoSession session) throws Exception {
    MethodChecker.CheckMethod("session closed",session);
    super.sessionClosed(session);
    System.out.println("session closed");
    //make this man offline
  }
}
