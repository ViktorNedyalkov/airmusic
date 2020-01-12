package airmusic.airmusic.model.DAO;

import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.POJO.Gender;
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
    private static final String GET_FOLLOWING_SQL = "SELECT followed_id FROM users_follow_users WHERE follower_id =?";
    private static final String GET_FOLLOWERS_SQL = "SELECT follower_id FROM users_follow_users WHERE followed_id =?";

    public  boolean doesExist(String email) throws SQLException {
        try  (Connection connection = jdbcTemplate.getDataSource().getConnection();
              PreparedStatement ps = connection.prepareStatement(USER_BY_EMAIL)) {
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }
    @SneakyThrows
    public  void followUser(User follower, User following) {
        try  (Connection connection = jdbcTemplate.getDataSource().getConnection();
              PreparedStatement ps = connection.prepareStatement(FOLLOW_USERS_SQL)) {
            ps.setLong(1, follower.getId());
            ps.setLong(2, following.getId());
            boolean userIsFollowed =!ps.execute();
            System.out.println(userIsFollowed);
            if (userIsFollowed) {
                throw new BadRequestException("User is already followed");
            }
        }
        catch (java.sql.SQLIntegrityConstraintViolationException e){
            throw new BadRequestException("User already followed");
        }
    }
    public void unFollowUser(User user, User targetUser) throws SQLException, BadRequestException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(UNFOLLOW_USER_SQL)) {
            ps.setLong(1,targetUser.getId());
            ps.setLong(2,user.getId());
            if (ps.executeUpdate()< IF_ANY_ROWS_EFFECTED){
                throw new BadRequestException("User is not followed");
            }
        }
    }
    public  List<User> getFollowers(User user) throws SQLException {
        return getFollowUsers(user, GET_FOLLOWERS_SQL);
    }
    public  List<User> getFollowing(User user) throws  SQLException {
        return getFollowUsers(user, GET_FOLLOWING_SQL);
    }

    private List<User> getFollowUsers(User user, String sql) throws SQLException {

        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1,user.getId());
            ResultSet rs = ps.executeQuery();
            List<User> followers = new ArrayList<>();
            while (rs.next()){
                followers.add(userRepository.findById(rs.getLong(1)).get());
            }
            return followers;
        }
    }


    public Gender setGender(String gender) throws SQLException, BadRequestException {
        try(Connection connection = jdbcTemplate.getDataSource().getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT id,name  FROM genders WHERE name =?"))   {
            ps.setString(1,gender);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return new Gender(rs.getLong(1), rs.getString(2));
            }
            throw new BadRequestException("Ivalid gender type");
        }
    }
}
