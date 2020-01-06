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

    public static void addUserToDB(User user) throws SQLException {
        String sql = "INSERT INTO users(email,password,first_name,last_name,gender_id,birth_date) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,user.getEmail());
            ps.setString(2,user.getPassword());//todo discus if its in the DB or in different FILE
            ps.setString(3,user.getFirstName());
            ps.setString(4,user.getLastName());
            ps.setInt(5,user.setGenderID(user.getGender()));
            ps.setDate(6, Date.valueOf(LocalDate.parse(user.getBirthDate())));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()){
                long id = rs.getLong(1);
                user.setId(id);
            }
        } catch (SQLException e) {
            throw e;
        }

    }

}
