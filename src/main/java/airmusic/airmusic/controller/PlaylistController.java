package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.BadRequestException;
import airmusic.airmusic.exceptions.NotLoggedUserException;
import airmusic.airmusic.model.DAO.PlaylistsDao;
import airmusic.airmusic.model.DTO.AddTrackToLIstDTO;
import airmusic.airmusic.model.DTO.PlaylistCreateDTO;
import airmusic.airmusic.model.POJO.Playlist;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.PlaylistRepository;
import airmusic.airmusic.model.repositories.SongRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Optional;

@RestController
public class PlaylistController extends AbstractController{
    @Autowired
    private PlaylistRepository playlistRepository;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private PlaylistsDao playlistsDao;
    @PostMapping("/users/playlists/create")
    public Playlist createPlaylist(HttpSession session, @RequestBody PlaylistCreateDTO dto) throws NotLoggedUserException {
        User user = validateUser(session);
        Playlist playlist = new Playlist();
        playlist.setCreator(user);
        playlist.setTitle(dto.getTitle());
        playlist.setName(dto.getName());
        playlistRepository.save(playlist);

        //TODO return created
        //Not working correctly
        return playlistRepository.findById(playlist.getId());
    }
    @PostMapping("/users/playlists/track/add")
    public Playlist addTrackToList(HttpSession session, @RequestBody AddTrackToLIstDTO dto) throws NotLoggedUserException, BadRequestException, SQLException {
        User user = validateUser(session);
        //check if exists, such a playlist        //TODO check if its users playlist
        Playlist playlist = playlistRepository.findById(dto.getPlaylist_id());
        Song song = songRepository.findById(dto.getSong_id());

        if (playlist==null){
            throw new BadRequestException("Not such a playlist found");
        }
        if (song==null){
            throw new BadRequestException("Not such a song  found");
        }
      //  check if it contains this track
        if (playlistsDao.containsSong(dto.getPlaylist_id(),dto.getSong_id())){
            throw new BadRequestException("Song already added");
        }

        //add track_id and playlist_id to playlists_have_tracks
        //"INSERT INTO playlists_have_tracks VALUES(?,?)

        return   playlistsDao.addSongToPlaylist(playlist,song);
    }
}
