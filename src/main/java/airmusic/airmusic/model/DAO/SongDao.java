package airmusic.airmusic.model.DAO;

import airmusic.airmusic.exceptions.BadRequestException;

import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SongDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SongRepository songRepository;

    private static final String GET_MY_FAVOURITE_SONG_SQL = "SELECT track_id FROM users_likes_tracks WHERE user_id =?;";
    private static final String LIKE_SONG_SQL = "INSERT INTO users_likes_tracks VALUES(?,?);";
    private static final String DISLIKE_SONG_SQL = "DELETE FROM users_likes_tracks WHERE user_id = ? AND track_id =?;";
    private static final int IF_ANY_ROWS_EFFECTED = 1;
    private static final String GET_PLAYLIST_TRACKS_SQL = "SELECT track_id FROM playlists_have_tracks WHERE playlist_id =?;";
    //todo implement
    private static final String GET_BY_NUMBER_OF_LIKES = "SELECT COUNT(*), t.*  FROM users_likes_tracks AS u JOIN tracks AS t ON(u.track_id = t.id) WHERE user_id = 1";


    public List<Song> myFavouriteSongs(User user) throws SQLException {
        try (Connection connection =jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_MY_FAVOURITE_SONG_SQL)) {
            ps.setLong(1, user.getId());
            ResultSet rs = ps.executeQuery();
            List<Song> myFavouriteSongs = new ArrayList<>();
            while (rs.next()) {
                myFavouriteSongs.add(songRepository.findById(rs.getLong(1)));
            }
            return myFavouriteSongs;
        }

    }
    //todo implement
    public List<Song> getSongsByUploadDateAndNumberOfLikes(){
        return null;
    }
    public void likeSong(User user, Song song) throws BadRequestException {
        try  (Connection connection = jdbcTemplate.getDataSource().getConnection();
              PreparedStatement ps = connection.prepareStatement(LIKE_SONG_SQL)) {
            ps.setLong(1,user.getId());
            ps.setLong(2,song.getId());
            ps.execute();
        } catch (SQLException e) {
            throw new BadRequestException("Song is already liked");
        }
    }
    public void dislikeSong(User user, Song song) throws SQLException, BadRequestException {
        try  (Connection connection = jdbcTemplate.getDataSource().getConnection();
              PreparedStatement ps = connection.prepareStatement(DISLIKE_SONG_SQL)) {
            ps.setLong(1,user.getId());
            ps.setLong(2,song.getId());

            if (ps.executeUpdate()< IF_ANY_ROWS_EFFECTED){
                throw new BadRequestException("Song is not liked");
            }
        }
    }

    public List<Song> playlistSongs(long playlistId) throws SQLException {
        try (Connection connection =jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_PLAYLIST_TRACKS_SQL)) {
            List<Song> playlistSong = new ArrayList<>();
            ps.setLong(1, playlistId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                playlistSong.add(songRepository.findById(rs.getLong(1)));
            }
            return playlistSong;
        }

    }

}
