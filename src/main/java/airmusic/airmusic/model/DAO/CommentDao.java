package airmusic.airmusic.model.DAO;

import airmusic.airmusic.exceptions.BadRequestException;
import airmusic.airmusic.model.POJO.Comment;
import airmusic.airmusic.model.POJO.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class CommentDao {
    private static final String LIKE_COMMENT_SQL = "INSERT INTO users_like_comments VALUES (?,?)";
    private static final String DISLIKE_COMMENT_SQL = "DELETE FROM users_like_comments WHERE user_id =? AND comment_id=?;";
    private static final int CHECK_FOR_DELETED_ROWS = 1;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void likeComment(User user, Comment comment) throws SQLException, BadRequestException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(LIKE_COMMENT_SQL)){
            ps.setLong(1,user.getId());
            ps.setLong(2,comment.getId());
            if (!ps.execute()){
                throw  new BadRequestException("Comment is already liked");
            }
        }
    }
    public void dislike(User user, Comment comment) throws SQLException, BadRequestException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(DISLIKE_COMMENT_SQL)){
            ps.setLong(1,user.getId());
            ps.setLong(2,comment.getId());
            if (ps.executeUpdate()< CHECK_FOR_DELETED_ROWS){
                throw  new BadRequestException("Comment is already disliked");
            }
        }
    }
}
