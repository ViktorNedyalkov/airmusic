package airmusic.airmusic.model.DAO;

import airmusic.airmusic.model.User;

import java.sql.*;
import java.time.LocalDate;

public class UserDao {
    public static boolean doesExist(String email) throws SQLException {
        String sql = "SELECT email FROM users WHERE email = ?";
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(sql)) {
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }

    }

    public static void followUser(User follower,User folllowing) throws SQLException {
        String sql = "INSERT INTO users_follow_users VALUES(?,?);";
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1,follower.getId());
            ps.setLong(2,folllowing.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }

    }

}
