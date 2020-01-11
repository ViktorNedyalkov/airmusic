package airmusic.airmusic.model.DAO;

import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.SongRepository;
import airmusic.airmusic.model.repositories.UserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@Component
public class UserDao {
    private static final int IF_ANY_ROWS_EFFECTED = 1;
    @Autowired
    private  UserRepository userRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String FOLLOW_USERS_SQL = "INSERT INTO users_follow_users VALUES(?,?);";
    private static final String USER_BY_EMAIL = "SELECT email FROM users WHERE email = ?";
    private static final String UNFOLLOW_USER_SQL = "DELETE FROM users_follow_users WHERE followed_id = ? AND follower_id =?;";

    public  boolean doesExist(String email) throws SQLException {
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(USER_BY_EMAIL)) {
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }
    @SneakyThrows
    public  void followUser(User follower, User following) {
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(FOLLOW_USERS_SQL)) {
            ps.setLong(1, follower.getId());
            ps.setLong(2, following.getId());
            if (ps.execute()) {
                throw new BadRequestException("User is already followed");
            }
        }
    }
    public void unFollowUser(User user, User targetUser) throws SQLException, BadRequestException {
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(UNFOLLOW_USER_SQL)) {
            ps.setLong(1,targetUser.getId());
            ps.setLong(2,user.getId());
            if (ps.executeUpdate()< IF_ANY_ROWS_EFFECTED){
                throw new BadRequestException("User is not followed");
            }
        }
    }
    public  List<User> getFollowers(User user) throws SQLException {
        String sql = "SELECT follower_id FROM users_follow_users WHERE followed_id =?";
        return getFollowUsers(user, sql);
    }
    public  List<User> getFollowing(User user) throws  SQLException {
        String sql = "SELECT followed_id FROM users_follow_users WHERE follower_id =?";
        return getFollowUsers(user, sql);
    }

    private List<User> getFollowUsers(User user, String sql) throws SQLException {
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(sql)) {
            ps.setLong(1,user.getId());
            ResultSet rs = ps.executeQuery();
            List<User> followers = new ArrayList<>();
            while (rs.next()){
                followers.add(userRepository.findById(rs.getLong(1)).get());
            }
            return followers;
        }
    }





}
