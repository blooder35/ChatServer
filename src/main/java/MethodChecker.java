import org.apache.mina.core.session.IoSession;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.sql.Connection;
import java.sql.SQLException;

public class MethodChecker {
  private static final String LOGIN_METHOD = "login";
  private static final String ADMIN = "admin";
  private static final String GET_MESSAGE_HISTORY_METHOD="getMessageHistory";
  private static final String NEW_MESSAGE_METHOD="message";
  private static final String REGISTRATION_METHOD="registration";
  static final String USER_STATUS_METHOD = "userStatus";
  private static final int MESSAGE_LENGTH=1000;
  private static final String SESSION_METHOD = "session";
  private static final Integer OFFLINE_STATUS=0;
  private static final Integer ONLINE_STATUS=1;

  public static void CheckMethod(String message, IoSession session) throws SQLException {
    //SessionSaver sessionSaver=new SessionSaver();
    String[] strArr=message.toString().split(" ");
    DatabaseHandler databaseHandler=new DatabaseHandler();
    Connection connection=null;
    connection=databaseHandler.getDbConnection();
    if (strArr.length == 3 && strArr[0].equals(LOGIN_METHOD)) {
      String login = strArr[1];
      String password = strArr[2];
      //check with database and proceed;
      //HARDCODE FOR ADMIN
      if (login.equals(ADMIN) && password.equals(ADMIN)) {
        session.setAttribute("Allowed", true);
        session.setAttribute("login",ADMIN);
        SessionSaver.addSession(session);
        session.write("login granted");
      } else {
        String str=databaseHandler.getUser(connection,login,password);
        if (str.equals("granted")) {
          session.setAttribute("login",login);
          session.setAttribute("Allowed",true);
          session.write("login granted");
          SessionSaver.addSession(session);
          databaseHandler.changeStatus(connection, login, ONLINE_STATUS);
          SessionSaver.sendStringToAllExcept(session, USER_STATUS_METHOD+" " + session.getAttribute("login")+" "+ONLINE_STATUS);
          //add to runtime
        } else {
          session.write("login "+str);
        }
      }
    } else if ((Boolean)session.getAttribute("Allowed")!=null) {
      if (strArr[0].equals(GET_MESSAGE_HISTORY_METHOD)) {
        System.out.println((Boolean) session.getAttribute("Allowed"));
        Message[] messages = databaseHandler.getMessages(connection);
        //String str=databaseHandler.getMessages(connection);
        //session.write("message "+str);
        for (Message msg : messages) {
          session.write(msg.getMessageAsStringToSend());
        }
//        sendLongMessage(session, strArr);
        User[] users = databaseHandler.getUsersList(connection);
        for (User user : users) {
          session.write(user.getUserAsStringToSend());
        }
      } else if (strArr[0].equals(NEW_MESSAGE_METHOD)) {
        String login = session.getAttribute("login").toString();
        String userMessage = message.substring(17);
        String time = message.substring(8, 16);
        databaseHandler.setMessage(connection, login, time, userMessage);
        StringBuilder sb = new StringBuilder("");
        sb.append(login + " [");
        sb.append(time + "]:");
        sb.append("//&&//");
        sb.append(userMessage);
        sb.append("//&&//");
        sb.append("//&&//");
        System.out.println(sb.toString());
        SessionSaver.sendStringToAllExcept(session, NEW_MESSAGE_METHOD + " " + sb.toString());
      } else if (strArr[0].equals(REGISTRATION_METHOD) && strArr.length == 3) {
        //check if it is admin
        if (session.getAttribute("login").equals(ADMIN)) {
          String login = strArr[1];
          String password = strArr[2];
          if (databaseHandler.userAwailable(connection, login)) {
            int affected = databaseHandler.addUser(connection, login, password);
            if (affected > 0) {
              session.write(REGISTRATION_METHOD + " completed");
              SessionSaver.sendToAll(USER_STATUS_METHOD + " " + login + " " + OFFLINE_STATUS);
            } else {
              session.write(REGISTRATION_METHOD + " " + affected);
            }
          } else {
            session.write(REGISTRATION_METHOD + " User with this name already registered");
          }
        }
      }
      if (strArr[0].equals(SESSION_METHOD) && strArr.length == 2) {
        String username = session.getAttribute("login", "").toString();
        System.out.println(username);
        if (!username.equals(ADMIN)) {
          databaseHandler.changeStatus(connection, username, OFFLINE_STATUS);
          SessionSaver.removeSession(session);
          SessionSaver.sendStringToAllExcept(session, USER_STATUS_METHOD + " " + session.getAttribute("login") + " " + OFFLINE_STATUS);
        }
      }
    }
  }

  //OLD METHOD
//  private static void sendLongMessage(IoSession session, String message) {
//    StringBuilder sb = new StringBuilder(message);
//    while (sb.length() > MESSAGE_LENGTH) {
//      session.write(NEW_MESSAGE_METHOD+" "+sb.subSequence(0,MESSAGE_LENGTH));
//      sb.delete(0,MESSAGE_LENGTH);
//    }
//    session.write(NEW_MESSAGE_METHOD+" "+sb);
//  }
}
