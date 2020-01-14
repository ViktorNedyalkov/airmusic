package airmusic.airmusic.model.DAO;

import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.DTO.SearchDTO;
import airmusic.airmusic.model.POJO.Gender;
import airmusic.airmusic.model.POJO.User;
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
            ps.execute();
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

    public List<User> searchUser(SearchDTO dto) throws SQLException {
        String sql = "SELECT DISTINCT " +
                "u.id," +
                "u.email," +
                "u.password," +
                "u.first_name," +
                "u.last_name," +
                "u.gender_id," +
                "u.birth_date," +
                "u.avatar," +
                "u.activated," +
                "g.name FROM users AS u " +
                "JOIN genders AS g " +
                "ON g.id=u.gender_id " +
                "WHERE u.first_name LIKE ? " +
                "OR u.last_name LIKE ?;";
        try(Connection connection = jdbcTemplate.getDataSource().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1,dto.getSearchingFor());
            ps.setString(2,dto.getSearchingFor());
            ResultSet rs = ps.executeQuery();
            List<User> searchResult = new ArrayList<>();
            while (rs.next()){
                User user = new User();
                user.setId(rs.getLong(1));
                user.setEmail(rs.getString(2));
                user.setPassword(rs.getString(3));
                user.setFirstName(rs.getString(4));
                user.setLastName(rs.getString(5));
                user.setActivated(rs.getBoolean(9));
                user.setBirthDate(rs.getDate(7).toLocalDate());
                user.setAvatar(rs.getString(8));
                Gender gender = new Gender();
                gender.setName(rs.getString(10));
                gender.setId(rs.getShort(6));
                user.setGender(gender);
                searchResult.add(user);
            }
            return searchResult;
        }
    }
}
