package ru.gb;

import java.sql.*;

public class DBConnector {

    private static Connection connection;
    private static Statement statement;

    public static void connect() throws ClassNotFoundException, SQLException {

        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:storageusers.db");
        statement = connection.createStatement();

        createTableUsers();
    }

    private static void createTableUsers() throws SQLException {
        statement.execute("CREATE TABLE if not exists 'storageusers' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'login' text, " +
                "'password' INT," +
                "'root_directory' text);");
    }

    public static String checkAuth(String login, String password) {
        String query = String.format("select root_directory, password from users where login = '%s'", login);
        try {
            ResultSet rs = statement.executeQuery(query);

            int myHash = password.hashCode();
            if(rs.next()){
                String root_directory = rs.getString(1);
                int dbHash = rs.getInt(2);
                if(myHash == dbHash){
                    return root_directory;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static int addUser(String login, String password) {
        try {
            String query = "INSERT INTO users (login, password, root_directory) VALUES (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, login);
            ps.setInt(2, password.hashCode());
            ps.setString(3, login);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}