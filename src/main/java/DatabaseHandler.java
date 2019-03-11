

import java.sql.*;

public class DatabaseHandler implements DBConfig {

  Connection connection;
  static boolean initialized=false;
  DatabaseHandler() throws SQLException {
    this.connection=getDbConnectionFirstTime();
  }
  public Connection getDbConnectionFirstTime() throws SQLException {
    //Driver driver= (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
    //DriverManager.registerDriver(driver);
    //Class.forName("com.mysql.jdbc.Driver");
    //String connectionString="jdbc:mysql://"+dbHost+":"+dbPort+"/"+dbName;
    String connectionString = "jdbc:mysql://localhost/chat?useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
    Connection connection = DriverManager.getConnection(connectionString, dbUser, dbPass);
    if (!initialized) {
      makeAllUsersOffline(connection);
      initialized=true;
    }
    return connection;
  }

  public Connection getDbConnection() throws SQLException {
    if (connection == null) {
      return connection;
    } else {
      connection=getDbConnectionFirstTime();
      return connection;
    }
  }

  private void makeAllUsersOffline(Connection connection) throws SQLException {
    String update = "UPDATE users SET status=0";
    PreparedStatement preparedStatement = connection.prepareStatement(update);
    preparedStatement.executeUpdate();
    preparedStatement.close();
  }

  public String getUser(Connection connection, String user, String password) throws SQLException {
    String update = "SELECT * FROM users WHERE userName=?";
    PreparedStatement preparedStatement = connection.prepareStatement(update);
    preparedStatement.setString(1, user);
    ResultSet result = preparedStatement.executeQuery();
    String comparePass = "";
    int status = 0;
    while (result.next()) {
      comparePass = result.getString("password");
      status = result.getInt("status");
    }
    result.close();
    if (!comparePass.equals("") && comparePass.equals(password) && status == 0) {

      return "granted";
    } else {
      if (comparePass.equals("")) {
        return "UserNotFound";
      } else if (status == 1) {
        return "UserAlreadyLoggedIn";
      } else {
        return "PasswordNotCorrect";
      }
    }
  }

  public Message[] getMessages(Connection connection) throws SQLException {
    int count = getMessagesCount(connection);
    System.out.println(count);
    String update = "SELECT * FROM messages";
    PreparedStatement preparedStatement = connection.prepareStatement(update);
    ResultSet result = preparedStatement.executeQuery();
    Message[] message = new Message[count];
    int i = 0;
    while (result.next()) {
      message[i++] = new Message(result.getString("sender"), result.getTime("time").toString(), result.getString("message"));
    }
    preparedStatement.close();
    return message;
  }

  private int getMessagesCount(Connection connection) throws SQLException {
    String update = "SELECT count(*) FROM messages";
    PreparedStatement preparedStatement = connection.prepareStatement(update);
    ResultSet result = preparedStatement.executeQuery();
    result.next();
    int count = result.getInt(1);
    preparedStatement.close();
    return count;
  }

  public void setMessage(Connection connection, String login, String time, String userMessage) throws SQLException {
    String update = "INSERT INTO messages(sender,time,message) values(?,?,?)";
    PreparedStatement preparedStatement = connection.prepareStatement(update);
    preparedStatement.setString(1, login);
    preparedStatement.setString(2, "1000-01-00 " + time);
    preparedStatement.setString(3, userMessage);
    preparedStatement.execute();
    System.out.println(preparedStatement.getUpdateCount());
    preparedStatement.close();
  }

  public boolean userAwailable(Connection connection, String login) throws SQLException {
    String update = "SELECT * FROM users WHERE userName=?";
    PreparedStatement preparedStatement = connection.prepareStatement(update);
    preparedStatement.setString(1, login);
    ResultSet result = preparedStatement.executeQuery();
    boolean found = false;
    if (result.next()) {
      found = true;
    }
    preparedStatement.close();
    return !found;
  }

  public int addUser(Connection connection, String login, String password) throws SQLException {
    String update = "INSERT INTO users(userName,password,status) values(?,?,?)";
    PreparedStatement preparedStatement = connection.prepareStatement(update);
    preparedStatement.setString(1, login);
    preparedStatement.setString(2, password);
    preparedStatement.setBoolean(3, false);
    preparedStatement.execute();
    int affected = preparedStatement.getUpdateCount();
    preparedStatement.close();
    return affected;
  }

  public int getUsersCount(Connection connection) throws SQLException {
    String update = "SELECT count(*) FROM users";
    PreparedStatement preparedStatement = connection.prepareStatement(update);
    ResultSet result = preparedStatement.executeQuery();
    result.next();
    int count = result.getInt(1);
    preparedStatement.close();
    return count;
  }

  public User[] getUsersList(Connection connection) throws SQLException {
    int count = getUsersCount(connection);
    String update = "SELECT * FROM users";
    PreparedStatement preparedStatement = connection.prepareStatement(update);
    ResultSet result = preparedStatement.executeQuery();
    User[] users = new User[count];
    int i = 0;
    while (result.next()) {
      users[i++] = new User(result.getString("username"), result.getInt("status"));
    }
    preparedStatement.close();
    return users;
  }

  public void changeStatus(Connection connection, String username, int status) throws SQLException {
    String update = "UPDATE users SET status=? WHERE userName=?";
    PreparedStatement preparedStatement = connection.prepareStatement(update);
    preparedStatement.setInt(1, status);
    preparedStatement.setString(2, username);
    preparedStatement.executeUpdate();
    preparedStatement.close();
  }
}
