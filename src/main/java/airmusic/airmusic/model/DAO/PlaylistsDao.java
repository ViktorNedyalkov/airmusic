package airmusic.airmusic.model.DAO;

import airmusic.airmusic.exceptions.BadRequestException;
import airmusic.airmusic.model.POJO.Playlist;
import airmusic.airmusic.model.POJO.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


@Component
public class PlaylistsDao {
    private static final int IS_ANY_ROWS_EFFECTED = 1;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String ADD_SONG_TO_PLAYLIST_SQL = "INSERT INTO playlists_have_tracks VALUES(?,?)";
    private static final String CONTAINS_SONG_SQL = "SELECT * FROM playlists_have_tracks WHERE playlist_id =? AND track_id =?;";
    private static final String DELETE_FROM_PLAYLIST_SQL = "DELETE FROM playlists_have_tracks where playlist_id =? AND track_id = ?;";

    public boolean containsSong(long playlist_id,long song_id) throws SQLException {
        try(Connection connection = jdbcTemplate.getDataSource().getConnection();
                PreparedStatement ps =connection.prepareStatement(CONTAINS_SONG_SQL)){
            ps.setLong(1,playlist_id);
            ps.setLong(2,song_id);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public Playlist addSongToPlaylist(Playlist playlist, Song song) throws SQLException {
        try(Connection connection = jdbcTemplate.getDataSource().getConnection();
            PreparedStatement ps = connection.prepareStatement(ADD_SONG_TO_PLAYLIST_SQL)) {
            ps.setLong(1, playlist.getId());
            ps.setLong(2, song.getId());
            ps.execute();
            return playlist;

        }
    }

    public Playlist removeFromPlaylist(Playlist playlist, Song song) throws SQLException {
        try(Connection connection = jdbcTemplate.getDataSource().getConnection();
            PreparedStatement ps = connection.prepareStatement(DELETE_FROM_PLAYLIST_SQL)) {
            ps.setLong(1,playlist.getId());
            ps.setLong(2,song.getId());
            if (ps.executeUpdate()< IS_ANY_ROWS_EFFECTED){
                throw new BadRequestException("No such song in this list");
            }
            return playlist;
        }
    }
}
