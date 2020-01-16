package airmusic.airmusic.model.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {


    private String msg;
    private int status;
    @JsonFormat(pattern = "yyyy-mm-dd")
    private LocalDateTime time;
    private String exceptionType;
}
