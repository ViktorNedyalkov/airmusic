package airmusic.airmusic.controller;


import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.DAO.CommentDao;
import airmusic.airmusic.model.DTO.CommentDTO;
import airmusic.airmusic.model.DTO.ResponseCommentDTO;
import airmusic.airmusic.model.POJO.Comment;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.CommentRepository;
import airmusic.airmusic.model.repositories.SongRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@RestController
public class CommentController extends AbstractController {

    @Autowired
    private SongRepository songRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentDao commentDao;

    @SneakyThrows
    @PostMapping("songs/{song_id}/comment")
    public Comment commentOnSong(@PathVariable(name = "song_id") long song_id,
                                 HttpSession session,
                                 @RequestBody CommentDTO commentDTO) {
        //comment with current logged user
        User user = validateUser(session);
        if(commentDTO == null){
            throw new BadRequestException("Please enter valid json");
        }
        if(commentDTO.getText() == null){
            throw new BadRequestException("Please enter a valid json");
        }
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
    @PutMapping(value = "songs/{song_id}/comments/{comment_id}")
    public Comment editComment(@PathVariable("song_id") long songId,
                            @PathVariable("comment_id") long commentId,
                            HttpSession session,
                            @RequestBody CommentDTO commentDTO) {

        User user = validateUser(session);
        checkIfSongExists(songId);
        Comment oldComment = getCommentIfItExists(commentId);

        if (user.getId() != commentRepository.findById(commentId).getUser().getId()) {
            throw new NotLoggedUserException("You are not the owner of this comment");
        }


        if (oldComment == null) {
            throw new NotFoundException("Comment not found");
        }

        if(commentDTO == null){
            throw new BadRequestException("Please fill out the json");
        }
        if (commentDTO.getText() == null) {
            throw new BadRequestException("Illegal data type passed");
        }

        oldComment.setText(commentDTO.getText());

        commentRepository.save(oldComment);
        return oldComment;
    }

    @SneakyThrows
    @DeleteMapping("songs/{song_id}/comments/{comment_id}")
    public void deleteComment(@PathVariable("song_id") long songId,
                              @PathVariable("comment_id") long commentId,
                              HttpSession session) {

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
    @PostMapping("/comments/{comment_id}/like/")
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

    @DeleteMapping("/songs/{comment_id}/dislike/")
    public Comment dislikeComment(HttpSession session, @PathVariable("comment_id") long id) throws BadRequestException, SQLException {
        User user = validateUser(session);
        Comment comment = commentRepository.findById(id);
        if (comment==null){
            throw  new BadRequestException("Comment not found!");
        }
        commentDao.dislike(user,comment);
        return comment;
    }
    private void checkIfSongExists(long songId) throws NotFoundException {
        Song song = songRepository.findById(songId);
        if (song == null) {
            throw new NotFoundException("Song not found");
        }
    }

    @GetMapping("/songs/{song_id}/comments")
    public List<ResponseCommentDTO> getCommentsOfSong(@PathVariable(value = "song_id") long songId){
        if(songRepository.findById(songId) == null){
            throw new NotFoundException("Song not found");
        }

        return ResponseCommentDTO.responseComments(commentRepository.findAllByTrack_Id(songId));
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
