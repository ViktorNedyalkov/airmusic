package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.SongNotFoundException;
import airmusic.airmusic.model.DAO.SongDao;
import airmusic.airmusic.model.Song;
import airmusic.airmusic.model.repositories.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
public class SongController {


    @Autowired
    private SongDao dao;
    @Autowired
    private SongRepository repo;

    @GetMapping("/songs")
    public List<Song> returnAllSongs(){
        //todo validations
        return dao.getAllSongs();
    }

    @GetMapping("/songs/{id}")
    public Song getSongById(@PathVariable("id") long id){
        if(repo.findById(id) != null){
            return repo.findById(id);
        }else{
            throw new SongNotFoundException();
        }


    }

    @PostMapping("/song")
    public Song addSong(@RequestBody Song song){
        //todo validate if user is logged in
        repo.save(song);
        return song;
    }

    @GetMapping("/{id}/songs")
    public List<Song> getAllSongByUser(@PathVariable("id") long id){
        return repo.findAllByUploaderId(id);
    }

    //todo: taka li se pravi???!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    @PutMapping("/users/{song_id}/edit")
    public Song editSongDescription(@PathVariable("song_id") long song_id, @RequestBody Map<String, String> bodyData){
        //fixme
        //validate if user is logged in
            //update description

            if(repo.findById(song_id) != null){
                Song song = repo.findById(song_id);
                String newDescription = bodyData.get("description");
                song.setDescription(newDescription);
                repo.save(song);

            }else{
                throw new SongNotFoundException();
            }

        return null;
    }
}
