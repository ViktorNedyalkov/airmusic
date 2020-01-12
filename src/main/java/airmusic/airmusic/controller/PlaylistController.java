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
    public ResponsePlaylistDTO createPlaylist(HttpSession session, @RequestBody PlaylistCreateDTO dto) throws NotLoggedUserException, SQLException {
        User user = validateUser(session);
        Playlist playlist = new Playlist();
        playlist.setCreator(user);
        validatePlaylistCreateDTO(dto);
        playlist.setTitle(dto.getTitle());
        playlist.setDescription(dto.getDescription());
        return new ResponsePlaylistDTO(playlistRepository.save(playlist));
    }

    private void validatePlaylistCreateDTO(PlaylistCreateDTO dto) {
        if (dto.getDescription()==null||dto.getDescription().trim().isEmpty()){
            throw new BadRequestException("Description needed");
        }
        if (dto.getTitle()==null||dto.getTitle().trim().isEmpty()){
            throw new BadRequestException("Title needed");
        }
    }

    @PostMapping("/playlists/track")
    public ResponsePlaylistDTO addTrackToList(HttpSession session, @RequestBody TrackToListDTO dto) throws NotLoggedUserException, BadRequestException, SQLException {
        User user = validateUser(session);
        Playlist playlist = getPlaylist(dto.getPlaylist_id(), user);
        Song song = getSong(dto);
        if (playlistsDao.containsSong(dto.getPlaylist_id(),dto.getSong_id())){
            throw new BadRequestException("Song already added");
        }
        return  new ResponsePlaylistDTO(playlistsDao.addSongToPlaylist(playlist,song),
                                        songDao.playlistSongs(dto.getPlaylist_id()));
    }


    //get
    @GetMapping("/user/playlists")
    public List<ResponsePlaylistDTO> myLists(HttpSession session) {
        User user = validateUser(session);
        return ResponsePlaylistDTO.responsePlaylists(playlistRepository.findAllByCreator_Id(user.getId()));
    }

    @GetMapping("playlists/search/{playlist_title}")//TODO pathvariable or SearchDTO
    public List<ResponsePlaylistDTO> searchPlaylist(@PathVariable("playlist_title") String title){
        return ResponsePlaylistDTO.responsePlaylists(playlistRepository.findAllByTitleContaining(title));
    }

    @GetMapping("playlists/{playlist_id}")
    public ResponsePlaylistDTO getPlaylistSong(HttpSession session,@PathVariable("playlist_id") long id) throws SQLException, BadRequestException {
        validateUser(session);
        Optional<Playlist> playlist = playlistRepository.findById(id);
        if(!playlist.isPresent()){
            throw new BadRequestException("Playlist doesn't exist");
        }
        return new ResponsePlaylistDTO(playlist.get(),songDao.playlistSongs(id));
    }
    @GetMapping("/playlists/{creator_id}/")
    public List<ResponsePlaylistDTO> getPlaylistFromCreator(@PathVariable("creator_id") long id){
        return ResponsePlaylistDTO.responsePlaylists(playlistRepository.findAllByCreator_Id(id));
    }
    //delete
    @SneakyThrows
    @DeleteMapping("playlists/{playlist_id}")
    public String deletePlaylist(HttpSession session,@PathVariable("playlist_id") long id){
        User user = validateUser(session);
        Optional<Playlist> playlist = playlistRepository.findById(id);
        if (!playlist.isPresent()){
            throw new BadRequestException("Playlist does`n exist");
        }
        if (playlist.get().getCreator().getId()!= user.getId()){
            throw new NotLoggedUserException("User not allowed to remove playlist");
        }
        playlistRepository.delete(playlist.get());
        return "Successfully deleted Playlist: " + playlist.get().getTitle();
    }
    @DeleteMapping("/playlists/track")
    public ResponsePlaylistDTO removeSongFromPlaylist(HttpSession session, @RequestBody TrackToListDTO dto) throws BadRequestException, SQLException {
        User user = validateUser(session);
        Playlist playlist = getPlaylist(dto.getPlaylist_id(),user);
        Song song = getSong(dto);
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

    private Song getSong(@RequestBody TrackToListDTO dto) throws BadRequestException, SQLException {
        Song song = songRepository.findById(dto.getSong_id());
        if (song==null){
            throw new BadRequestException("Not such a song  found");
        }
        return song;
    }

    private Playlist getPlaylist(long playlist_id, User user) throws BadRequestException {
        Optional<Playlist> playlist = playlistRepository.findById(playlist_id);
        if (!playlist.isPresent()){
            throw new BadRequestException("Not such a playlist found");
        }
        if (playlist.get().getCreator().getId()!=user.getId()){
            throw new BadRequestException("Not Allowed Correction, you are not the owner of the Playlist");
        }
        return playlist.get();
    }
}
