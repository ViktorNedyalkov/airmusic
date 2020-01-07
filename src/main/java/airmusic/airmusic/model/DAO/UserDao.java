package airmusic.airmusic.model.DAO;

import airmusic.airmusic.exceptions.FollowUserException;
import airmusic.airmusic.exceptions.NoAccessException;
import airmusic.airmusic.exceptions.UnFollowUserException;
import airmusic.airmusic.model.User;
import airmusic.airmusic.model.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@Component
public class UserDao {
    private static final String INSERT_INTO_USERS_FOLLOW_USERS_VALUES = "INSERT INTO users_follow_users VALUES(?,?);";
    private static final String USER_BY_EMAIL = "SELECT email FROM users WHERE email = ?";
    @Autowired
    private  UserRepository repo;
    public  boolean doesExist(String email) throws SQLException {

        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(USER_BY_EMAIL)) {
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }

    }

    public  void followUser(User follower,User folllowing) throws FollowUserException {
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(INSERT_INTO_USERS_FOLLOW_USERS_VALUES)) {
            ps.setLong(1,follower.getId());
            ps.setLong(2,folllowing.getId());
            ps.execute();

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
    public  List<User> getFollowing(User user) throws NoAccessException {
        String sql = "SELECT followed_id FROM users_follow_users WHERE follower_id =?";
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

    public void unfollowUser(User user, User targetUser) throws SQLException, UnFollowUserException {
        String sql = "DELETE FROM users_follow_users WHERE followed_id = ? AND follower_id =?;";
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(sql)) {
            ps.setLong(1,user.getId());
            ps.setLong(2,targetUser.getId());

            if (ps.executeUpdate()<1){
                throw new UnFollowUserException();
            }
        }

    }
}
