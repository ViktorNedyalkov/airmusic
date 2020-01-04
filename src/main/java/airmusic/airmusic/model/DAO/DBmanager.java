package airmusic.airmusic.model.DAO;



import  java.sql.*;


public class DBmanager {
    private static final String DATA_BASE_NAME = "mydb";
    private static final String PASSWORD = "iceman";
    private static final String USERNAME = "iceman";
    private static final String PORT = "3306";
    private static final String IP_ADDRESS = "127.0.0.1";

    private static DBmanager instance = new DBmanager();
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection.isClosed()) {
            new DBmanager();
        }
        return connection;
    }

    private DBmanager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("driver problem");
        }
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://"
                            + IP_ADDRESS + ":" + PORT + "/" +
                            DATA_BASE_NAME + "?serverTimezone = UTC",
                    USERNAME,
                    PASSWORD);
            System.out.println("connected");
        } catch (SQLException e) {
            System.out.println("connection problems " + e.getMessage());
        }
    }

    public static DBmanager getInstance() {
        return instance;
    }
}