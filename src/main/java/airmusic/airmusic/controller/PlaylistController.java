package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.BadRequestException;
import airmusic.airmusic.exceptions.NotLoggedUserException;
import airmusic.airmusic.model.DAO.PlaylistsDao;
import airmusic.airmusic.model.DAO.SongDao;
import airmusic.airmusic.model.DTO.EditPlaylistDTO;
import airmusic.airmusic.model.DTO.TrackToListDTO;
import airmusic.airmusic.model.DTO.PlaylistCreateDTO;
import airmusic.airmusic.model.DTO.ResponsePlaylistDTO;
import airmusic.airmusic.model.POJO.Playlist;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.PlaylistRepository;
import airmusic.airmusic.model.repositories.SongRepository;

import lombok.SneakyThrows;
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
    //post
    @PostMapping("/playlists")
    public Playlist createPlaylist(HttpSession session, @RequestBody PlaylistCreateDTO dto) throws NotLoggedUserException, SQLException {
        User user = validateUser(session);
        Playlist playlist = new Playlist();
        playlist.setCreator(user);
        playlist.setTitle(dto.getTitle());
        playlist.setDescription(dto.getName());

        return playlistRepository.save(playlist);
    }
    @PostMapping("/playlists/track/")
    public ResponsePlaylistDTO addTrackToList(HttpSession session, @RequestBody TrackToListDTO dto) throws NotLoggedUserException, BadRequestException, SQLException {
        User user = validateUser(session);
        Playlist playlist = getPlaylist(dto.getPlaylist_id(), user);
        Song song = preparedSongForPlaylist(dto);
        return  new ResponsePlaylistDTO(playlistsDao.addSongToPlaylist(playlist,song),
                                        songDao.playlistSongs(dto.getPlaylist_id()));
    }


    //get
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
    @GetMapping("/playlists/{creator_id}/")
    public List<Playlist> getPlaylistFromCreator(@PathVariable("creator_id") long id){
        return playlistRepository.findAllByCreator_Id(id);
    }
    //delete
    @SneakyThrows
    @DeleteMapping("playlists/{playlist_id}")
    public void deletePlaylist(HttpSession session,@PathVariable("playlist_id") long id){
        User user = validateUser(session);
        Optional<Playlist> playlist = playlistRepository.findById(id);
        if (!playlist.isPresent()){
            throw new BadRequestException("Playlist does`n exist");
        }
        if (playlist.get().getCreator()!= user){
            throw new NotLoggedUserException("User not allowed to remove playlist");
        }
        playlistRepository.delete(playlist.get());
    }
    @DeleteMapping("/playlists/track")
    public ResponsePlaylistDTO removeSongFromPlaylist(HttpSession session, @RequestBody TrackToListDTO dto) throws BadRequestException, SQLException {
        User user = validateUser(session);
        Playlist playlist = getPlaylist(dto.getPlaylist_id(),user);
        Song song = preparedSongForPlaylist(dto);
        playlistsDao.removeFromPlaylist(playlist,song);
        return  new ResponsePlaylistDTO(playlist,
                songDao.playlistSongs(dto.getPlaylist_id()));
    }

    //put
    @SneakyThrows
    @PutMapping("/playlist/")
    public void editPlaylist(HttpSession session, @RequestBody EditPlaylistDTO dto){
        User user = validateUser(session);
        Playlist playlist = getPlaylist(dto.getPlaylistId(),user);
        playlist.setDescription(dto.getDescription());
        playlist.setTitle(dto.getTitle());
        playlistRepository.save(playlist);
    }


    //private

    private Song preparedSongForPlaylist(@RequestBody TrackToListDTO dto) throws BadRequestException, SQLException {
        Song song = songRepository.findById(dto.getSong_id());
        if (song==null){
            throw new BadRequestException("Not such a song  found");
        }
        if (playlistsDao.containsSong(dto.getPlaylist_id(),dto.getSong_id())){
            throw new BadRequestException("Song already added");
        }
        return song;
    }

    private Playlist getPlaylist(long playlist_id, User user) throws BadRequestException {
        Optional<Playlist> playlist = playlistRepository.findById(playlist_id);
        if (!playlist.isPresent()){
            throw new BadRequestException("Not such a playlist found");
        }
        if (playlist.get().getCreator()!=user){
            throw new BadRequestException("Not Allowed Correction, you are not the owner of the Playlist");
        }
        return playlist.get();
    }
}
