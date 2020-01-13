package airmusic.airmusic.controller;

import airmusic.airmusic.AmazonClient;
import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.DAO.SongDao;
import airmusic.airmusic.model.DTO.ResponseSongDTO;
import airmusic.airmusic.model.DTO.SongEditDTO;
import airmusic.airmusic.model.DTO.SongSearchDTO;
import airmusic.airmusic.model.DTO.SongWithLikesDTO;
import airmusic.airmusic.model.POJO.Comment;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.Uploader;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.SongRepository;
import airmusic.airmusic.model.repositories.UserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
public class SongController extends  AbstractController{



    @Autowired
    private AmazonClient amazonClient;
    @Autowired
    private SongDao songDao;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private UserRepository userRepository;

    //get mappings

    @GetMapping("/songs")
    public List<ResponseSongDTO> returnAllSongs(){
        return ResponseSongDTO.respondSongs(songRepository.findAll());
    }
    @SneakyThrows
    @GetMapping("/songs/search")
    public List<ResponseSongDTO> searchForSongByUsername(@RequestBody @Valid SongSearchDTO songSearchDTO){
        if(songSearchDTO==null){
            throw new BadRequestException();
        }
        if(songSearchDTO.getTitle() == null){
            throw new BadRequestException();
        }
        return ResponseSongDTO.respondSongs(songRepository.findAllByTitleContaining(songSearchDTO.getTitle()));
    }


    @SneakyThrows
    @GetMapping("/songs/search/byNumberOfLikes")
    public List<SongWithLikesDTO> searchByNumberOfLikes(){
        return songDao.getSongsByNumberOfLikes();
    }

    @SneakyThrows
    @GetMapping("/songs/search/byUploadDate")
    public List<ResponseSongDTO> searchByUploadDate(){
        return ResponseSongDTO.respondSongs(songRepository.findAllByOrderByUploadDate());
    }

    @SneakyThrows
    @GetMapping("/songs/{id}")
    public ResponseSongDTO getSongById(@PathVariable("id") long id){
        Song song =songRepository.findById(id);
        if(song != null){
            return new ResponseSongDTO(song);
        }else{
            throw new NotFoundException("Song not found");
        }
    }
    @SneakyThrows
    @GetMapping("/songs/{id}/withLikes")
    public SongWithLikesDTO getSongWithLikesById(@PathVariable("id") long id){
        Song song = songRepository.findById(id);
        if(song != null){
            return SongWithLikesDTO.getFromSong(song,
                    songDao.getNumberOfLikesForTrackId(song.getId()));
        }else{
            throw new NotFoundException("Song not found");
        }
    }

    @SneakyThrows
    @GetMapping("user/liked")
    public List<ResponseSongDTO> getLikedByUser(HttpSession session){
        User user = validateUser(session);
        return ResponseSongDTO.respondSongs(songDao.getLikedByUser(user.getId()));

    }
    @GetMapping("user/{user_id}/songs")
    public List<ResponseSongDTO> songsByUser(@PathVariable(value = "user_id") long userId) throws NotFoundException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(!optionalUser.isPresent()){
            throw new NotFoundException("User not found");
        }
        return ResponseSongDTO.respondSongs(songRepository.findAllByUploader_Id(userId));
    }


    //post mappings
    @SneakyThrows
    @PostMapping("/songs")
    public String addSong(@RequestParam String description,
                        @RequestParam String title,
                        @RequestParam String genre_id,
                        @RequestParam(value = "song")MultipartFile file,
                        HttpSession session) {
        System.out.println(genre_id);
        User uploader = validateUser(session);
        if (file == null) {
            throw new IllegalValuePassedException("Please upload a file");
        }
        if (file.getContentType() == null || !file.getContentType().equalsIgnoreCase("audio/mpeg")) {
            throw new IllegalValuePassedException("Please upload an audio file");
        }
        Thread uploadToAmazon = new Uploader(file, amazonClient, description, genre_id, title, uploader, songRepository);
        uploadToAmazon.start();
        return "Your song has started uploading";
    }

    //put mappings

    @PutMapping("song/{song_id}")
    public ResponseSongDTO editSongDescription(@PathVariable("song_id") long song_id,
                                               @RequestBody SongEditDTO songEditDTO,
                                               HttpSession session) throws NotLoggedUserException, NotFoundException {

        //validate if user is logged in
        User user = validateUser(session);
        Song song = songRepository.findById(song_id);
        if(song == null){
            throw new NotFoundException("Song not found");
        }
        if(songEditDTO == null){
            throw new BadRequestException("Please enter a correct json");
        }
        if(songEditDTO.getDescription() == null){
            throw new BadRequestException("Please pass a valid value");
        }
        if(user.getId() != song.getUploader().getId()){
            throw new NotLoggedUserException("You are not the owner of this song");
        }

        //todo refactor
        song.setDescription(songEditDTO.getDescription());
        songRepository.save(song);
        return new ResponseSongDTO(song);
    }


    //delete mappings

    @SneakyThrows
    @DeleteMapping("/songs/{song_id}")
    public Song deleteSong(@PathVariable("song_id") long song_id, HttpSession session){
        //is user logged in
        User user = validateUser(session);
        if(user == null){
            throw new NotLoggedUserException("Please log in");
        }

        //does song exist
        if(songRepository.findById(song_id) == null){
            throw new NotFoundException("Song not found");
        }
        //is user the same as uploader
        Song song = songRepository.findById(song_id);
        if(user.getId() != song.getUploader().getId()){
            //todo maybe change to another exception
            throw new NotLoggedUserException("You are not the owner of this song");
        }
        //delete song
        //from file system
        File file = new File(song.getTrackUrl());
        System.out.println(file.exists());
        //delete from amazon
        amazonClient.deleteFileFromS3Bucket(song.getAmazonUrl());
        //from database
        songRepository.delete(song);
        //don't know if i should return anything
        return song;
    }

    @PostMapping("/songs/like/{song_id}")
    public ResponseSongDTO likeSong(HttpSession session, @PathVariable("song_id") long id) throws NotLoggedUserException,
            BadRequestException {
        User user = validateUser(session);
        Song song = songRepository.findById(id);
        if (song==null){
            throw  new BadRequestException("No such song found");
        }
        songDao.likeSong(user, song);
        return new ResponseSongDTO(song);
    }
    @DeleteMapping("/songs/dislike/{song_id}")
    public ResponseSongDTO dislikeSong(HttpSession session, @PathVariable("song_id") long id) throws NotLoggedUserException,
            SQLException,
            BadRequestException {
        User user = validateUser(session);
        Song song = songRepository.findById(id);
        if (song==null){
            throw  new BadRequestException("No such song found");
        }
        songDao.dislikeSong(user,song);
        return new ResponseSongDTO(song);
    }
    @GetMapping("/songs/myFavouriteSongs")
    public List<ResponseSongDTO> myFavouriteSongs(HttpSession session) throws NotLoggedUserException, SQLException {
        User user = validateUser(session);
        return ResponseSongDTO.respondSongs(songDao.myFavouriteSongs(user));
    }
    @GetMapping("/songs/mySongs")
    public List<ResponseSongDTO> mySongs(HttpSession session) throws NotLoggedUserException {
        User user = validateUser(session);
        return ResponseSongDTO.respondSongs(songRepository.findAllByUploader_Id(user.getId()));
    }

    @GetMapping("/songs/find/{song_title}")
    public List<ResponseSongDTO> findSong(@PathVariable ("song_title") String title){
        return ResponseSongDTO.respondSongs(songRepository.findAllByTitleContaining(title));
    }
    @GetMapping("/songs/{uploader_id}/")
    public List<ResponseSongDTO> getUploaderSong(@PathVariable("uploader_id") long id){
        return ResponseSongDTO.respondSongs(songRepository.findAllByUploader_Id(id));
    }
}

