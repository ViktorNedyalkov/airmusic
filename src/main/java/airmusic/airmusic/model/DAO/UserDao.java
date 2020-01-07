package airmusic.airmusic.model.DAO;

import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.Song;
import airmusic.airmusic.model.User;
import airmusic.airmusic.model.repositories.SongRepository;
import airmusic.airmusic.model.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@Component
public class UserDao {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final String FOLLOW_USERS_SQL = "INSERT INTO users_follow_users VALUES(?,?);";
    private static final String LIKE_SONG_SQL = "INSERT INTO users_likes_tracks VALUES(?,?);";
    private static final String USER_BY_EMAIL = "SELECT email FROM users WHERE email = ?";
    private static final int CHECK_IF_ANY_DELETED_ROWS = 1;
    private static final String UNFOLLOW_USER_SQL = "DELETE FROM users_follow_users WHERE followed_id = ? AND follower_id =?;";
    private static final String DISLIKE_SONG_SQL = "DELETE FROM users_likes_tracks WHERE user_id = ? AND track_id =?;";

    public  boolean doesExist(String email) throws SQLException {
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(USER_BY_EMAIL)) {
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }
    public  void followUser(User follower,User folllowing) throws FollowUserException {
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(FOLLOW_USERS_SQL)) {
            ps.setLong(1,follower.getId());
            ps.setLong(2,folllowing.getId());
            ps.execute();
        } catch (SQLException e) {
            throw new FollowUserException();
        }

    }
    public void unFollowUser(User user, User targetUser) throws SQLException, UnFollowUserException {
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(UNFOLLOW_USER_SQL)) {
            ps.setLong(1,user.getId());
            ps.setLong(2,targetUser.getId());
            if (ps.executeUpdate()< CHECK_IF_ANY_DELETED_ROWS){
                throw new UnFollowUserException();
            }
        }
    }
    public  List<User> getFollowers(User user) throws NoAccessException {
        String sql = "SELECT follower_id FROM users_follow_users WHERE followed_id =?";
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(sql)) {
            ps.setLong(1,user.getId());
            ResultSet rs = ps.executeQuery();
            List<User> followers = new ArrayList<>();
            while (rs.next()){
                followers.add(userRepository.findById(rs.getLong(1)));
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
                followers.add(userRepository.findById(rs.getLong(1)));
            }
            return followers;
        } catch (SQLException e) {
            throw new NoAccessException();
        }
    }
    public void likeSong(User user, Song song) throws SongAlreadyLikedException {
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(LIKE_SONG_SQL)) {
            ps.setLong(1,user.getId());
            ps.setLong(2,song.getId());
            ps.execute();
        } catch (SQLException e) {
            throw new SongAlreadyLikedException();
        }
    }
    public void dislikeSong(User user, Song song) throws SQLException, NotLikedSongException {
        try (PreparedStatement ps = DBmanager.getConnection().prepareStatement(DISLIKE_SONG_SQL)) {
            ps.setLong(1,user.getId());
            ps.setLong(2,song.getId());
            if (ps.executeUpdate()< CHECK_IF_ANY_DELETED_ROWS){
                throw new NotLikedSongException();
            }
        }
    }
    public List<Song> myFavouriteSongs(User user) {
        List<Song> myFavouriteSongs = new ArrayList<>();
        String sql = "SELECT track_id FROM users_likes_tracks WHERE user_id =?;";
        try (PreparedStatement ps = jdbcTemplate.getDataSource().getConnection().prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                myFavouriteSongs.add(songRepository.findById(rs.getLong(1)));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return myFavouriteSongs;
    }


}
