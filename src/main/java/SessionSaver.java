import org.apache.mina.core.session.IoSession;

import java.util.HashSet;
import java.util.Set;

public final class SessionSaver {
    private static SessionSaver instance;
    private Set<IoSession> sessionSet;

    private SessionSaver() {
        sessionSet = new HashSet<>();
    }

    public static SessionSaver getInstance() {
        //double check locking pattern
        if (instance == null) {
            synchronized (SessionSaver.class) {
                if (instance == null) {
                    instance = new SessionSaver();
                }
            }
        }
        return instance;
    }

    public synchronized void addSession(IoSession session) {
        sessionSet.add(session);
    }

    public synchronized void sendStringToAllExcept(IoSession session, String message) {
        for (IoSession ses : sessionSet) {
            if (!ses.equals(session)) {
                ses.write(message);
            }
        }
    }

    public synchronized void sendToAll(String message) {
        for (IoSession ses : sessionSet) {
            ses.write(message);
        }
    }

    public synchronized void removeSession(IoSession session) {
        sessionSet.remove(session);
    }
}
