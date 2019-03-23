import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.mina.core.session.IoSession;

import java.sql.SQLException;
import java.util.LinkedList;

public final class MethodChecker {
    private static final String LOGIN_METHOD = "login";
    private static final String ADMIN = "admin";
    private static final String GET_MESSAGE_HISTORY_METHOD = "getMessageHistory";
    private static final String NEW_MESSAGE_METHOD = "message";
    private static final String REGISTRATION_METHOD = "registration";
    private static final String USER_STATUS_METHOD = "userStatus";
    private static final Integer OFFLINE_STATUS = 0;
    private static final Integer ONLINE_STATUS = 1;
    private static final String SESSION_ATTRIBUTE_ALLOWED = "Allowed";
    private static final String SESSION_ATTRIBUTE_LOGIN = "login";

    public static void checkMethod(String message, IoSession session) throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        SocketMessage socketMessage = gson.fromJson(message, SocketMessage.class);
        if (socketMessage.getMethod() == null || socketMessage.getBody() == null) {
            System.out.println("something wrong with coming packet");
        } else {
            switch (socketMessage.getMethod()) {
                case LOGIN_METHOD:
                    LoginCredentials loginCredentials = gson.fromJson(socketMessage.getBody(), LoginCredentials.class);
                    doLoginAction(loginCredentials, session, gson);
                    break;
                case GET_MESSAGE_HISTORY_METHOD:
                    sendMessageHistory(session, gson);
                    break;
                case NEW_MESSAGE_METHOD:
                    ReceivedMessage receivedMessage = gson.fromJson(socketMessage.getBody(), ReceivedMessage.class);
                    receiveMessage(receivedMessage, session, gson);
                    break;
                case REGISTRATION_METHOD:
                    LoginCredentials regCredentials = gson.fromJson(socketMessage.getBody(), LoginCredentials.class);
                    doRegistrationAction(regCredentials, session, gson);
                    break;
            }
        }
    }

    public static void closeSession(IoSession session) throws SQLException {
        String username = session.getAttribute(SESSION_ATTRIBUTE_LOGIN, "").toString();
        DatabaseHandler databaseHandler = DatabaseHandler.getInstance();
        SessionSaver sessionSaver = SessionSaver.getInstance();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        if (!username.equals(ADMIN)) {
            databaseHandler.changeStatus(username, OFFLINE_STATUS);
            sessionSaver.removeSession(session);
            User user = new User(username, OFFLINE_STATUS);
            SocketMessage sm = new SocketMessage(USER_STATUS_METHOD, gson.toJson(user));
            sessionSaver.sendStringToAllExcept(session, gson.toJson(sm));
        }
    }

    private synchronized static void doLoginAction(LoginCredentials loginCredentials, IoSession session, Gson gson) throws SQLException {
        if (loginCredentials.getUsername() != null && loginCredentials.getPassword() != null) {
            String username = loginCredentials.getUsername();
            String password = loginCredentials.getPassword();
            //hardcode for ADMIN
            SessionSaver sessionSaver = SessionSaver.getInstance();
            DatabaseHandler databaseHandler = DatabaseHandler.getInstance();
            if (username.equals(ADMIN) && password.equals(ADMIN)) {
                session.setAttribute(SESSION_ATTRIBUTE_ALLOWED, true);
                session.setAttribute(SESSION_ATTRIBUTE_LOGIN, ADMIN);
                sessionSaver.addSession(session);
                SocketMessage sm = new SocketMessage(LOGIN_METHOD, gson.toJson(new LoginStatusBody(username, DBCommunicationProtocol.DB_ACCESS_GRANTED.getValue())));
                session.write(gson.toJson(sm));
            } else {
                String str = databaseHandler.getUser(username, password);
                if (str.equals(DBCommunicationProtocol.DB_ACCESS_GRANTED.getValue())) {
                    session.setAttribute(SESSION_ATTRIBUTE_ALLOWED, true);
                    session.setAttribute(SESSION_ATTRIBUTE_LOGIN, username);
                    SocketMessage sm = new SocketMessage(LOGIN_METHOD, gson.toJson(new LoginStatusBody(username, DBCommunicationProtocol.DB_ACCESS_GRANTED.getValue())));
                    session.write(gson.toJson(sm));
                    sessionSaver.addSession(session);
                    databaseHandler.changeStatus(username, ONLINE_STATUS);
                    User user = new User(username, ONLINE_STATUS);
                    sm = new SocketMessage(USER_STATUS_METHOD, gson.toJson(user));
                    sessionSaver.sendStringToAllExcept(session, gson.toJson(sm));
                } else {
                    SocketMessage sm = new SocketMessage(LOGIN_METHOD, gson.toJson(new LoginStatusBody(username, str)));
                    session.write(gson.toJson(sm));
                }
            }
        }
    }

    private static void sendMessageHistory(IoSession session, Gson gson) throws SQLException {
        if (session.getAttribute(SESSION_ATTRIBUTE_ALLOWED) != null) {
            DatabaseHandler databaseHandler = DatabaseHandler.getInstance();
            LinkedList<Message> messages = databaseHandler.getMessages();
            for (Message msg : messages) {
                String stringToSend = gson.toJson(new SocketMessage(NEW_MESSAGE_METHOD, gson.toJson(msg)));
                session.write(stringToSend);
            }
            LinkedList<User> users = databaseHandler.getUsersList();
            for (User user : users) {
                String stringToSend = gson.toJson(new SocketMessage(USER_STATUS_METHOD, gson.toJson(user)));
                session.write(stringToSend);
            }
        }
    }

    private static void receiveMessage(ReceivedMessage receivedMessage, IoSession session, Gson gson) throws SQLException {
        String login = session.getAttribute("login").toString();
        String userMessage = receivedMessage.getMessage();
        String time = receivedMessage.getTime();
        DatabaseHandler.getInstance().setMessage(login, time, userMessage);
        //изменяем время для отправки в клиент
        time = time.substring(12);
        Message msg = new Message(login, time, userMessage);
        SocketMessage socketMessage = new SocketMessage(NEW_MESSAGE_METHOD, gson.toJson(msg));
        SessionSaver.getInstance().sendStringToAllExcept(session, gson.toJson(socketMessage));
    }

    private synchronized static void doRegistrationAction(LoginCredentials loginCredentials, IoSession session, Gson gson) throws SQLException {
        if (session.getAttribute(SESSION_ATTRIBUTE_LOGIN).equals(ADMIN)) {
            String login = loginCredentials.getUsername();
            String password = loginCredentials.getPassword();
            DatabaseHandler databaseHandler = DatabaseHandler.getInstance();
            String dbAnswer = databaseHandler.registerUser(login, password);
            SocketMessage socketMessage = new SocketMessage(REGISTRATION_METHOD, dbAnswer);
            session.write(gson.toJson(socketMessage));
            if (dbAnswer.equals(DBCommunicationProtocol.DB_REGISTRATION_SUCCESS.getValue())) {
                LoginStatusBody loginStatusBody = new LoginStatusBody(login, OFFLINE_STATUS.toString());
                socketMessage = new SocketMessage(USER_STATUS_METHOD, gson.toJson(loginStatusBody));
                SessionSaver.getInstance().sendToAll(gson.toJson(socketMessage));
            }
        }
    }
}
