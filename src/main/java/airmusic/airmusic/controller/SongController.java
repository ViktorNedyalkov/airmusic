package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.ForbiddenSongActionException;
import airmusic.airmusic.exceptions.NotLoggedUserException;
import airmusic.airmusic.exceptions.SongNotFoundException;
import airmusic.airmusic.model.DAO.SongDao;
import airmusic.airmusic.model.DTO.SongUploadDTO;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.SongRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class SongController extends  AbstractController{

    private static final String UPLOAD_PATH = "D:\\JAVA\\airmusic\\src\\main\\resources\\songs\\";

    @Autowired
    private SongDao dao;
    @Autowired
    private SongRepository songRepository;

    //get mappings

    @GetMapping("/songs")
    public List<Song> returnAllSongs(){
        //todo validations
        return dao.getAllSongs();
    }

    @GetMapping("/songs/{id}")
    public Song getSongById(@PathVariable("id") long id){
        if(songRepository.findById(id) != null){
            return songRepository.findById(id);
        }else{
            throw new SongNotFoundException();
        }


    }

    //post mappings
    @SneakyThrows
    @PostMapping("/songs/upload")
    public Song addSong(@RequestParam String description, @RequestParam String title, @RequestParam String genre_id, @RequestParam("song")MultipartFile file, HttpSession session){
        //is user logged in
        User uploader = validateUser(session);
        byte[] fileBytes = file.getBytes();
        String songUrl = UPLOAD_PATH + file.getOriginalFilename();
        Path path = Paths.get(songUrl);
        Files.write(path, fileBytes);

        Song song = new Song();
        song.setTitle(title);
        song.setGenre_id(Long.valueOf(genre_id));
        song.setDescription(description);
        song.setTrack_url(songUrl + file.getOriginalFilename());
        song.setUploader(uploader);
        song.setUpload_date(new Date());

        songRepository.save(song);
        return song;
    }

    //put mappings
    //todo: taka li se pravi???!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    @PutMapping("/user/song/{song_id}/edit")
    public Song editSongDescription(@PathVariable("song_id") long song_id, @RequestBody Map<String, String> bodyData, HttpSession session) throws NotLoggedUserException {
        //fixme
        //validate if user is logged in
        User user = validateUser(session);
        if(user == null){
            throw new NotLoggedUserException();
        }

        if(songRepository.findById(song_id) == null){
            throw new SongNotFoundException();
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
    @DeleteMapping("/users/{user_id}/{song_id}/delete")
    public Song deleteSong(@PathVariable("user_id") long user_id, @PathVariable("song_id") long song_id, HttpSession session) throws NotLoggedUserException{
        //is user logged in
        User user = validateUser(session);
        if(user == null){
            throw new NotLoggedUserException();
        }

        //does song exist
        if(songRepository.findById(song_id) == null){
            throw new SongNotFoundException();
        }
        //is user the same as uploader
        Song song = songRepository.findById(song_id);
        if(user.getId() != song.getUploader().getId()){
            //todo maybe change to another exception
            throw new NotLoggedUserException();
        }
        //delete song
        songRepository.delete(song);
        //don't know if i should return anything
        return song;
    }



}
