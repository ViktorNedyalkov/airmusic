package airmusic.airmusic.model.DAO;

import airmusic.airmusic.exceptions.FollowUserException;
import airmusic.airmusic.exceptions.NoAccessException;
import airmusic.airmusic.model.User;
import airmusic.airmusic.model.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@Component
public class UserDao {
    @Autowired
    private  UserRepository repo;
    public  boolean doesExist(String email) throws SQLException {
        String sql = "SELECT email FROM users WHERE email = ?";
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(sql)) {
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }

    }

    public  void followUser(User follower,User folllowing) throws FollowUserException {
        String sql = "INSERT INTO users_follow_users VALUES(?,?);";
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1,follower.getId());
            ps.setLong(2,folllowing.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new FollowUserException();
        }

    }
    public  List<User> getFollowers(User user) throws NoAccessException {
        String sql = "SELECT follower_id FROM users_follow_users WHERE followed_id =?";
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(sql)) {
            ps.setLong(1,user.getId());
            ResultSet rs = ps.executeQuery();
            List<User> followers = new ArrayList<>();
            while (rs.next()){
                followers.add(repo.findById(rs.getLong(1)));
            }
            return followers;
        } catch (SQLException e) {
            throw new NoAccessException();
        }
    }
}
