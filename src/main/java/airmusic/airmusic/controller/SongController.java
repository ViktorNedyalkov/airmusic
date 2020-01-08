package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.ForbiddenSongActionException;
import airmusic.airmusic.exceptions.NotLoggedUserException;
import airmusic.airmusic.exceptions.SongNotFoundException;
import airmusic.airmusic.model.DAO.SongDao;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
public class SongController {


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
    @PostMapping("/song")
    public Song addSong(@RequestBody Song song){
        //todo validate if user is logged in
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
    private User validateUser(HttpSession session) throws NotLoggedUserException {

        return (User) session.getAttribute("logged");
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED,reason = "please login")
    @ExceptionHandler({NotLoggedUserException.class})
    public void loggedExceptionHandler(){

    }
}
