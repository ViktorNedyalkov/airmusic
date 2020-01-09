package airmusic.airmusic.model.DAO;

import airmusic.airmusic.model.POJO.Playlist;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.repositories.SongRepository;
import airmusic.airmusic.model.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class PlaylistsDao {
    private static final String ADD_SONG_TO_PLAYLIST_SQL = "INSERT INTO playlists_have_tracks VALUES(?,?)";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private  JdbcTemplate jdbcTemplate;

    private static final String CONTAINS_SONG_SQL = "SELECT * FROM playlists_have_tracks WHERE playlist_id =? AND track_id =?;";

    public boolean containsSong(long playlist_id,long song_id) throws SQLException {
        try(PreparedStatement ps =jdbcTemplate.getDataSource().getConnection().prepareStatement(CONTAINS_SONG_SQL)){
            ps.setLong(1,playlist_id);
            ps.setLong(2,song_id);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public Playlist addSongToPlaylist(Playlist playlist, Song song) throws SQLException {
        try(PreparedStatement ps = jdbcTemplate.getDataSource().getConnection().prepareStatement(ADD_SONG_TO_PLAYLIST_SQL)) {
            jdbcTemplate.getDataSource().getConnection().setAutoCommit(false);
            ps.setLong(1, playlist.getId());
            ps.setLong(2, song.getId());
            ps.execute();
            jdbcTemplate.getDataSource().getConnection().commit();
            return playlist;

        }
        finally {
            jdbcTemplate.getDataSource().getConnection().setAutoCommit(true);
        }
    }


}
