package airmusic.airmusic.model.DTO;

import airmusic.airmusic.model.POJO.Comment;
import airmusic.airmusic.model.POJO.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
public class ResponseCommentDTO {

    private long id;

    private User user;

    private String text;
    private Date post_date;


    public ResponseCommentDTO(Comment c){
        this.id = c.getId();
        this.post_date = c.getPost_date();
        this.text = c.getText();
        this.user = c.getUser();
    }

}
