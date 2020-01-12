package airmusic.airmusic.controller;


import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.DAO.CommentDao;
import airmusic.airmusic.model.DTO.CommentDTO;
import airmusic.airmusic.model.POJO.Comment;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.CommentRepository;
import airmusic.airmusic.model.repositories.SongRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Date;

@RestController
public class CommentController extends AbstractController {

    @Autowired
    private SongRepository songRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentDao commentDao;

    @SneakyThrows
    @PostMapping("comments/{song_id}")
    public Comment commentOnSong(@PathVariable(name = "song_id") long song_id,
                                 HttpSession session,
                                 @RequestBody CommentDTO commentDTO) {
        //comment with current logged user
        User user = validateUser(session);

        if (songRepository.findById(song_id) == null) {
            throw new NotFoundException("Song not found");
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
    @PutMapping(value = "/{song_id}/comments/{comment_id}")
    public void editComment(@PathVariable("song_id") long songId,
                            @PathVariable("comment_id") long commentId,
                            HttpSession session,
                            @RequestBody CommentDTO commentDTO) {
        //TODO MAJOR REFACTOR
        User user = validateUser(session);
        checkIfSongExists(songId);

        if (user.getId() != commentRepository.findById(songId).getUser().getId()) {
            throw new NotLoggedUserException();
        }
        Comment oldComment = getCommentIfItExists(songId);
        if (oldComment == null) {
            throw new NotFoundException("Comment not found");
        }
        if (commentDTO.getText() == null) {
            throw new IllegalValuePassedException();
        }
        oldComment.setText(commentDTO.getText());
        commentRepository.save(oldComment);

    }

    @SneakyThrows
    @DeleteMapping("/{song_id}/comments/{comment_id}")
    public void deleteComment(@PathVariable("song_id") long songId,
                              @PathVariable("comment_id") long commentId,
                              HttpSession session) {
        //TODO MAJOR REFACTOR
        User user = validateUser(session);
        checkIfSongExists(songId);
        if (songRepository.findById(songId) == null) {
            throw new NotFoundException("Song not found");
        }

        if (user.getId() != commentRepository.findById(songId).getUser().getId()) {

            throw new NotLoggedUserException();
        }
        Comment commentToDelete = commentRepository.findById(commentId);
        if (commentToDelete == null) {
            throw new NotFoundException("Comment not found");
        }
        Comment comment = getCommentIfItExists(commentId);
        commentRepository.delete(comment);
    }
    @SneakyThrows
    @PostMapping("/comments/like/{comment_id}")
    public Comment likeComment(@PathVariable("comment_id") long id,
                               HttpSession session){
        User user = validateUser(session);
        Comment comment = commentRepository.findById(id);
        if(comment ==null){
            throw new BadRequestException("Comment not found!");
        }
        commentDao.likeComment(user,comment);
        return comment;
    }
    @DeleteMapping("/comments/dislike/{song_id}")
    public Comment dislikeSong(HttpSession session, @PathVariable("song_id") long id) throws BadRequestException, SQLException {
        User user = validateUser(session);
        Comment comment = commentRepository.findById(id);
        if (comment==null){
            throw  new BadRequestException("Comment not found!");
        }
        commentDao.dislike(user,comment);
        return comment;
    }
    private void checkIfSongExists(long songId) throws NotFoundException {
        if (songRepository.findById(songId) == null) {
            throw new NotFoundException("Song not found");
        }
    }

    @SneakyThrows
    private Comment getCommentIfItExists(long commentId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) {
            throw new NotFoundException("Comment not found");
        }
        return comment;
    }

}
