import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class Server {
  private static final int PORT=9123;

  public static void main(String[] args) throws IOException {
    SessionSaver ss=new SessionSaver();
    IoAcceptor acceptor = new NioSocketAcceptor();
    acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
    acceptor.setHandler(new ServerHandler());
    acceptor.getSessionConfig().setReadBufferSize(2048);
    acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
    acceptor.bind(new InetSocketAddress(PORT));
    System.out.println("SERVER STARTED");

  }
}
