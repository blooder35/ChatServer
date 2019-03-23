

import java.sql.*;
import java.util.LinkedList;

public final class DatabaseHandler {

    private static DatabaseHandler instance;
    private Connection connection;

    private DatabaseHandler() {
    }

    public static DatabaseHandler getInstance() {
        //double check locking pattern
        if (instance == null) {
            synchronized (DatabaseHandler.class) {
                if (instance == null) {
                    instance = new DatabaseHandler();
                }
            }
        }
        return instance;
    }

    public void getDbConnectionFirstTime() {
        String connectionString = "jdbc:mysql://localhost/chat?useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
        try {
            this.connection = DriverManager.getConnection(connectionString, DBConfig.DBUSER.getValue(), DBConfig.DBPASS.getValue());
            makeAllUsersOffline();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Your SQL server isn't running. Application won't work properly without it");
        }
    }

    public String getUser(String user, String password) throws SQLException {
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
            return DBCommunicationProtocol.DB_ACCESS_GRANTED.getValue();
        } else {
            if (comparePass.equals("")) {
                return DBCommunicationProtocol.DB_ACCESS_DENIED_USER_NOT_FOUND.getValue();
            } else if (status == 1) {
                return DBCommunicationProtocol.DB_ACCESS_DENIED_USER_ALREADY_LOGGED_IN.getValue();
            } else {
                return DBCommunicationProtocol.DB_ACCESS_DENIED_WRONG_PASSWORD.getValue();
            }
        }
    }

    public LinkedList<Message> getMessages() throws SQLException {
        String update = "SELECT * FROM messages";
        PreparedStatement preparedStatement = connection.prepareStatement(update);
        ResultSet result = preparedStatement.executeQuery();
        LinkedList<Message> message = new LinkedList<>();
        while (result.next()) {
            message.add(new Message(result.getString("sender"), result.getTime("time").toString(), result.getString("message")));
        }
        preparedStatement.close();
        return message;
    }

    public void setMessage(String login, String time, String userMessage) throws SQLException {
        String update = "INSERT INTO messages(sender,time,message) values(?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(update);
        preparedStatement.setString(1, login);
        preparedStatement.setString(2, time);
        preparedStatement.setString(3, userMessage);
        preparedStatement.execute();
        preparedStatement.close();
    }

    public String registerUser(String login, String password) throws SQLException {
        if (userAwailable(login)) {
            if (addUser(login, password) > 0) {
                return DBCommunicationProtocol.DB_REGISTRATION_SUCCESS.getValue();
            } else {
                return DBCommunicationProtocol.DB_REGISTRATION_FAILED.getValue();
            }
        } else {
            return DBCommunicationProtocol.DB_REGISTRATION_FAILED_USER_ALREADY_REGISTERED.getValue();
        }
    }

    public LinkedList<User> getUsersList() throws SQLException {
        String update = "SELECT * FROM users";
        PreparedStatement preparedStatement = connection.prepareStatement(update);
        ResultSet result = preparedStatement.executeQuery();
        LinkedList<User> users = new LinkedList<>();
        while (result.next()) {
            users.add(new User(result.getString("username"), result.getInt("status")));
        }
        preparedStatement.close();
        return users;
    }

    public void changeStatus(String username, int status) throws SQLException {
        String update = "UPDATE users SET status=? WHERE userName=?";
        PreparedStatement preparedStatement = connection.prepareStatement(update);
        preparedStatement.setInt(1, status);
        preparedStatement.setString(2, username);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private void makeAllUsersOffline() throws SQLException {
        String update = "UPDATE users SET status=0";
        PreparedStatement preparedStatement = connection.prepareStatement(update);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private boolean userAwailable(String login) throws SQLException {
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

    private int addUser(String login, String password) throws SQLException {
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
}
