package airmusic.airmusic.model.DAO;

import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.DTO.ResponseUserDTO;
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
    private static final String GET_FOLLOWING_SQL ="SELECT DISTINCT u.id,u.email,u.first_name,u.last_name,u.birth_date,u.avatar,u.activated, g.name AS gender FROM users AS u " +
            "JOIN users_follow_users as us " +
            "ON us.followed_id = u.id " +
            "JOIN genders AS g " +
            "ON g.id =u.gender_id " +
            "WHERE follower_id =?;";
    private static final String GET_FOLLOWERS_SQL = "SELECT DISTINCT u.id,u.email,u.first_name,u.last_name,u.birth_date,u.avatar,u.activated, g.name AS gender FROM users AS u " +
            "JOIN users_follow_users as us " +
            "ON us.follower_id = u.id " +
            "JOIN genders AS g " +
            "ON g.id =u.gender_id " +
            "WHERE followed_id =?;";

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
    public  List<ResponseUserDTO> getFollowers(User user) throws SQLException {
        return getFollowUsers(user, GET_FOLLOWERS_SQL);
    }
    public  List<ResponseUserDTO> getFollowing(User user) throws  SQLException {
        return getFollowUsers(user, GET_FOLLOWING_SQL);
    }

    private List<ResponseUserDTO> getFollowUsers(User user, String sql) throws SQLException {

        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1,user.getId());
            ResultSet rs = ps.executeQuery();
            List<ResponseUserDTO> followers = new ArrayList<>();
            while (rs.next()){
                followers.add(new ResponseUserDTO(
                        rs.getLong(1),
                        rs.getString("u.email"),
                        rs.getString("u.first_name"),
                        rs.getString("u.last_name"),
                        rs.getString("gender"),
                        rs.getString("u.birth_date"),
                        rs.getString("u.avatar"),
                        rs.getBoolean("u.activated")
                ));
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

    public List<ResponseUserDTO> searchUser(SearchDTO dto) throws SQLException {
        String sql = "SELECT " +
                "u.id," +
                "u.email," +
                "u.password," +
                "u.first_name," +
                "u.last_name," +
                "u.gender_id," +
                "u.birth_date," +
                "u.avatar," +
                "u.activated," +
                "g.name AS gender FROM users AS u " +
                "JOIN genders AS g " +
                "ON g.id=u.gender_id " +
                "WHERE u.first_name LIKE ?" +
                "OR u.last_name LIKE ?;";
        try(Connection connection = jdbcTemplate.getDataSource().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1,"%"+dto.getSearchingFor()+"%");
            ps.setString(2,"%"+dto.getSearchingFor()+"%");
            ResultSet rs = ps.executeQuery();
            List<ResponseUserDTO> searchResult = new ArrayList<>();
            while (rs.next()){
                searchResult.add(new ResponseUserDTO(
                        rs.getLong("u.id"),
                        rs.getString("u.email"),
                        rs.getString("u.first_name"),
                        rs.getString("u.last_name"),
                        rs.getString("gender"),
                        rs.getString("u.birth_date"),
                        rs.getString("u.avatar"),
                        rs.getBoolean("u.activated")
                ));
            }
            return searchResult;
        }
    }
}
