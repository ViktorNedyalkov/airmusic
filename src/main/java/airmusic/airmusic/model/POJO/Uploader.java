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
    private static final String UPLOAD_PATH = "D:\\JAVA\\airmusic\\src\\main\\resources\\songs\\";

    private MultipartFile multipartFile;
    private AmazonClient amazonClient;
    private String description;
    private String genre_id;
    private String title;
    private User uploader;
    private SongRepository songRepository;

    public Uploader(MultipartFile multipartFile,
                    AmazonClient amazonClient,
                    String description,
                    String genre_id,
                    String title,
                    User uploader, SongRepository songRepository) {

        this.multipartFile = multipartFile;
        this.amazonClient = amazonClient;
        this.description = description;
        this.genre_id = genre_id;
        this.title = title;
        this.uploader = uploader;
        this.songRepository = songRepository;
    }

    @SneakyThrows
    @Override
    public void run() {
        byte[] fileBytes = multipartFile.getBytes();
        String songUrl = UPLOAD_PATH + multipartFile.getOriginalFilename() + System.currentTimeMillis();
        Path path = Paths.get(songUrl);
        Files.write(path, fileBytes);
        Song song = new Song();
        song.setTitle(title);
        Genre genre = new Genre();
        genre.setId(Long.valueOf(genre_id));

        song.setGenre(genre);
        song.setDescription(description);
        song.setTrackUrl(songUrl);
        song.setUploader(uploader);
        song.setUploadDate(new Date());

        String amazonUrl = amazonClient.uploadFile(multipartFile);
        song.setAmazonUrl(amazonUrl);
        songRepository.save(song);
    }
}
