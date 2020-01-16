package airmusic.airmusic.model.POJO;

import airmusic.airmusic.AmazonClient;
import airmusic.airmusic.model.repositories.SongRepository;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class Uploader extends Thread {


    private MultipartFile multipartFile;
    private AmazonClient amazonClient;
    private SongRepository songRepository;
    private Song song;
    public Uploader(MultipartFile multipartFile,
                    AmazonClient amazonClient,
                    SongRepository songRepository, Song song) {

        this.multipartFile = multipartFile;
        this.amazonClient = amazonClient;
        this.songRepository = songRepository;
        this.song = song;
    }

    @SneakyThrows
    @Override
    public void run() {

        String amazonUrl = amazonClient.uploadFile(multipartFile);
        song.setAmazonUrl(amazonUrl);
        //rewrites
        songRepository.save(song);
    }
}
