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
public class PlaylistController {
    @Autowired
    private PlaylistRepository playlistRepository;
    @Autowired
    private SongRepository songRepository;
    @PostMapping("/users/playlists/create")
    public Playlist createPlaylist(HttpSession session, @RequestBody PlaylistCreateDTO pl) throws NotLoggedUserException {
        User user = UserController.validateUser(session);
        Playlist playlist = new Playlist();
        playlist.setCreator(user);
        playlist.setTitle(pl.getTitle());
        playlist.setName(pl.getName());
        playlist = playlistRepository.save(playlist);
        //TODO return created playlist
        return playlist;
    }
}
