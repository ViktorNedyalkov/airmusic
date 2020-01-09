package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.SongNotFoundException;
import airmusic.airmusic.model.DAO.SongDao;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.repositories.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class SongController extends AbstractController{


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
        return repo.findAllByUploaderId(id);//TODO  tova ne trqbva li da e v user-a
    }

    //todo: taka li se pravi???!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    @PutMapping("/user/song/{song_id}/edit")
    public Song editSongDescription(@PathVariable("song_id") long song_id, @RequestBody Map<String, String> bodyData){
        //fixme
        //validate if user is logged in
            //update descriptio
        Song song = repo.findById(song_id);

            if(song !=null){
                String newDescription = bodyData.get("description");
                song.setDescription(newDescription);
                repo.save(song);

            }else{
                throw new SongNotFoundException();
            }

        return null;
    }
}
