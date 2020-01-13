package airmusic.airmusic.model.repositories;

import airmusic.airmusic.model.POJO.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Comment findById(long id);
    List<Comment> findAllByTrack_Id(long trackId);
}
