package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.DAO.SongDao;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.SongRepository;
import airmusic.airmusic.model.repositories.UserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class SongController extends  AbstractController{

    private static final String UPLOAD_PATH = "D:\\JAVA\\airmusic\\src\\main\\resources\\songs\\";

    @Autowired
    private SongDao songDao;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private UserRepository userRepository;

    //get mappings

    @GetMapping("/songs")
    public List<Song> returnAllSongs(){
        //todo validations
        return songDao.getAllSongs();
    }
    @SneakyThrows
    @GetMapping("/songs/{id}")
    public Song getSongById(@PathVariable("id") long id){
        if(songRepository.findById(id) != null){
            return songRepository.findById(id);
        }else{
            throw new NotFoundException("Song not found");

        }
    }

    @GetMapping("user/{user_id}/songs")
    public List<Song> songsByUser(@PathVariable(value = "user_id") long userId) throws NotFoundException {
        if(userRepository.findById(userId) == null){
            
            throw new NotFoundException("User not found");
        }
        return songRepository.findAllByUploader_Id(userId);
    }

    //post mappings
    @SneakyThrows
    @PostMapping("/songs")
    public Song addSong(@RequestParam String description, @RequestParam String title, @RequestParam String genre_id, @RequestParam(value = "song")MultipartFile file, HttpSession session){
        //is user logged in
        User uploader = validateUser(session);
        if(file == null){
            throw new IllegalValuePassedException("Please upload a file");
        }
        if(!file.getContentType().equalsIgnoreCase("audio/mpeg")){
            throw new IllegalValuePassedException("Please upload an audio file");
        }
        byte[] fileBytes = file.getBytes();


        String songUrl = UPLOAD_PATH + file.getOriginalFilename() + new Time(System.currentTimeMillis());
        Path path = Paths.get(songUrl);
        Files.write(path, fileBytes);

        Song song = new Song();
        song.setTitle(title);
        song.setGenre_id(Long.valueOf(genre_id));
        song.setDescription(description);
        song.setTrack_url(songUrl);
        song.setUploader(uploader);
        song.setUpload_date(new Date());

        songRepository.save(song);
        return song;
    }

    //put mappings
    //todo: taka li se pravi???!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    @PutMapping("song/{song_id}")
    public Song editSongDescription(@PathVariable("song_id") long song_id,
                                    @RequestBody Map<String, String> bodyData,
                                    HttpSession session) throws NotLoggedUserException, NotFoundException {
        //fixme
        //validate if user is logged in
        User user = validateUser(session);
        if(songRepository.findById(song_id) == null){
            throw new NotFoundException("Song not found");
        }
        if(bodyData.containsValue(null)){
            throw new ForbiddenSongActionException();
        }
        Song song = songRepository.findById(song_id);
        if(user.getId() != song.getUploader().getId()){
            //todo maybe change to another exception
            throw new NotLoggedUserException();
        }
        //todo refactor
        String newDescription = bodyData.get("description");
        song.setDescription(newDescription);
        songRepository.save(song);
        return song;
    }


    //delete mappings

    @SneakyThrows
    @DeleteMapping("/songs/{/{song_id}")
    public Song deleteSong(@PathVariable("song_id") long song_id, HttpSession session){
        //is user logged in
        User user = validateUser(session);
        if(user == null){
            throw new NotLoggedUserException();
        }

        //does song exist
        if(songRepository.findById(song_id) == null){
            throw new NotFoundException("Song not found");
        }
        //is user the same as uploader
        Song song = songRepository.findById(song_id);
        if(user.getId() != song.getUploader().getId()){
            //todo maybe change to another exception
            throw new NotLoggedUserException();
        }
        //delete song
        //from file system
        File file = new File(song.getTrack_url());
        System.out.println(file.exists());

        //from database
        songRepository.delete(song);
        //don't know if i should return anything
        return song;
    }

    @PostMapping("/songs/like/{song_id}")
    public Song likeSong(HttpSession session, @PathVariable("song_id") long id) throws NotLoggedUserException, BadRequestException {
        User user = validateUser(session);
        Song song = songRepository.findById(id);
        if (song==null){
            throw  new BadRequestException("Not such song found");
        }
        songDao.likeSong(user, song);
        return song;
    }
    @DeleteMapping("/songs/dislike/{song_id}")
    public Song dislikeSong(HttpSession session, @PathVariable("song_id") long id) throws NotLoggedUserException, SQLException, BadRequestException {
        User user = validateUser(session);
        Song song = songRepository.findById(id);
        if (song==null){
            throw  new BadRequestException("Not such song found");
        }
        songDao.dislikeSong(user,song);
        return song;
    }
    @GetMapping("/songs/myFavouriteSongs")
    public List<Song> myFavouriteSongs(HttpSession session) throws NotLoggedUserException, SQLException {
        User user = validateUser(session);
        return songDao.myFavouriteSongs(user);
    }
    @GetMapping("/songs/mySongs")
    public List<Song> mySongs(HttpSession session) throws NotLoggedUserException {
        User user = validateUser(session);
        return songRepository.findAllByUploader_Id(user.getId());
    }

    @GetMapping("/songs/find/{song_title}")
    public List<Song> findSong(@PathVariable ("song_title") String title){
        return songRepository.findAllByTitleContaining(title);
    }
    @GetMapping("/songs/{uploader_id}/")
    public List<Song> getUploaderSong(@PathVariable("uploader_id") long id){
        return songRepository.findAllByUploader_Id(id);
    }
}
