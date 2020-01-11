package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.BadRequestException;
import airmusic.airmusic.exceptions.NotLoggedUserException;
import airmusic.airmusic.model.DAO.PlaylistsDao;
import airmusic.airmusic.model.DAO.SongDao;
import airmusic.airmusic.model.DTO.TrackToListDTO;
import airmusic.airmusic.model.DTO.PlaylistCreateDTO;
import airmusic.airmusic.model.DTO.ResponsePlaylistDTO;
import airmusic.airmusic.model.POJO.Playlist;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.PlaylistRepository;
import airmusic.airmusic.model.repositories.SongRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
public class PlaylistController extends AbstractController{
    @Autowired
    private PlaylistRepository playlistRepository;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private PlaylistsDao playlistsDao;
    @Autowired
    private SongDao songDao;
    @PostMapping("/playlists")
    public Playlist createPlaylist(HttpSession session, @RequestBody PlaylistCreateDTO dto) throws NotLoggedUserException, SQLException {
        User user = validateUser(session);
        Playlist playlist = new Playlist();
        playlist.setCreator(user);
        playlist.setTitle(dto.getTitle());
        playlist.setName(dto.getName());

        return playlistRepository.save(playlist);
    }
    @PostMapping("/users/playlists/track/add")
    public ResponsePlaylistDTO addTrackToList(HttpSession session, @RequestBody TrackToListDTO dto) throws NotLoggedUserException, BadRequestException, SQLException {
        User user = validateUser(session);
        Optional<Playlist> playlist = playlistRepository.findById(dto.getPlaylist_id());
        Song song = songRepository.findById(dto.getSong_id());
        if (!playlist.isPresent()){
            throw new BadRequestException("Not such a playlist found");
        }
        if (!playlistRepository.findAllByCreator_Id(user.getId()).contains(playlist.get())){
            throw new BadRequestException("Not Allowed Correction, you are not the owner of the Playlist");
        }
        if (song==null){
            throw new BadRequestException("Not such a song  found");
        }
        if (playlistsDao.containsSong(dto.getPlaylist_id(),dto.getSong_id())){
            throw new BadRequestException("Song already added");
        }
        return  new ResponsePlaylistDTO(playlistsDao.addSongToPlaylist(playlist.get(),song),
                                        songDao.playlistSongs(dto.getPlaylist_id()));
    }

    @GetMapping("/users/playlists/myLists")
    public List<Playlist> myLists(HttpSession session) {
        User user = validateUser(session);
        return playlistRepository.findAllByCreator_Id(user.getId());
    }

    @GetMapping("playlists/search/{playlist_title}")//TODO pathvariable or SearchDTO
    public List<Playlist> searchPlaylist(@PathVariable("playlist_title") String title){
        return playlistRepository.findAllByTitleContaining(title);
    }

    @GetMapping("playlists/{playlist_id}")
    public List<Song> getPlaylistSong(HttpSession session,@PathVariable("playlist_id") long id) throws SQLException, BadRequestException {
        validateUser(session);
        if(!playlistRepository.existsById(id)){
            throw new BadRequestException("Playlist doesn't exist");
        }
        return songDao.playlistSongs(id);
    }
    @DeleteMapping("/playlists")
    public ResponsePlaylistDTO removeSongFromPlaylist(HttpSession session, @RequestBody TrackToListDTO dto) throws BadRequestException, SQLException {
        User user = validateUser(session);
        Optional<Playlist> playlist = playlistRepository.findById(dto.getPlaylist_id());
        Song song = songRepository.findById(dto.getSong_id());
        if (!playlist.isPresent()){
            throw new BadRequestException("Not such a playlist found");
        }
        if (!playlistRepository.findAllByCreator_Id(user.getId()).contains(playlist.get())){
            throw new BadRequestException("Not Allowed Correction, you are not the owner of the Playlist");
        }
        if (song==null){
            throw new BadRequestException("Not such a song  found");
        }
        playlistsDao.removeFromPlaylist(playlist.get(),song);
        return  new ResponsePlaylistDTO(playlist.get(),
                songDao.playlistSongs(dto.getPlaylist_id()));
    }
}
