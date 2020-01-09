package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.CommentNotFoundException;
import airmusic.airmusic.exceptions.IlligalValuePassedException;
import airmusic.airmusic.exceptions.NotLoggedUserException;
import airmusic.airmusic.exceptions.SongNotFoundException;
import airmusic.airmusic.model.DTO.CommentDTO;
import airmusic.airmusic.model.POJO.Comment;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.CommentRepository;
import airmusic.airmusic.model.repositories.SongRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;

@RestController
public class CommentController extends AbstractController{

    @Autowired
    private SongRepository songRepository;
    @Autowired
    private CommentRepository commentRepository;

    @SneakyThrows
    @PostMapping("{song_id}/comments/add")
    public Comment commentOnSong(@PathVariable(name = "song_id") long song_id,
                                 HttpSession session,
                                 @RequestBody CommentDTO commentDTO){
        //comment with current logged user
        User user = validateUser(session);

        if(songRepository.findById(song_id) == null){
            throw new SongNotFoundException();
        }
        Comment comment = new Comment();
        comment.setText(commentDTO.getText());
        comment.setPost_date(new Date());
        comment.setUser(user);
        comment.setTrack(songRepository.findById(song_id));
        commentRepository.save(comment);
        return comment;
    }

    @SneakyThrows
    @PostMapping(value = "/{song_id}/comments/{comment_id}/edit")
    public void editComment(@PathVariable("song_id") long songId,
                            @PathVariable("comment_id") long commentId,
                            HttpSession session,
                            @RequestBody CommentDTO commentDTO){
        //TODO MAJOR REFACTOR
        User user = validateUser(session);

        if(songRepository.findById(songId) == null){
            throw new SongNotFoundException();
        }
        if(user.getId() != commentRepository.findById(songId).getUser().getId()){
            //todo maybe change to another exception
            throw new NotLoggedUserException();
        }
        Comment oldComment = commentRepository.findById(commentId);

        if(oldComment == null){
            throw new CommentNotFoundException();
        }
        if(commentDTO.getText() == null){
            throw new IlligalValuePassedException();
        }
        oldComment.setText(commentDTO.getText());
        commentRepository.save(oldComment);

    }

    @SneakyThrows
    @DeleteMapping("/{song_id}/comments/{comment_id}/delete")
    public void deleteComment(@PathVariable("song_id") long songId,
                              @PathVariable("comment_id") long commentId,
                              HttpSession session){
        //TODO MAJOR REFACTOR
        User user = validateUser(session);

        if(songRepository.findById(songId) == null){
            throw new SongNotFoundException();
        }

        if(user.getId() != commentRepository.findById(songId).getUser().getId()){

            throw new NotLoggedUserException();
        }
        Comment commentToDelete = commentRepository.findById(commentId);
        if(commentToDelete == null){
            throw new CommentNotFoundException();
        }
        commentRepository.delete(commentToDelete);

    }

}
