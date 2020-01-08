package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.DAO.UserDao;
import airmusic.airmusic.model.DTO.LoginUserDTO;
import airmusic.airmusic.model.DTO.RegisterUserDTO;
import airmusic.airmusic.model.POJO.Playlist;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.PlaylistRepository;
import airmusic.airmusic.model.repositories.SongRepository;
import airmusic.airmusic.model.repositories.UserRepository;
import com.google.gson.JsonObject;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;


@RestController
public class UserController {
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*?[#?!@$%^&*-])(?=.*[a-zA-Z]).{8,}$";
    ;
    /*
    At least one upper case English letter, (?=.*?[A-Z])
At least one lower case English letter, (?=.*?[a-z])
At least one digit, (?=.*?[0-9])
At least one special character, (?=.*?[#?!@$%^&*-])
Minimum eight in length .{8,} (with the anchors)
     */
    private static final String EMAIL_REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    @Autowired
    private JdbcTemplate jdbcTemplate;//TODO check this. Not sure we need it at all
    @Autowired
    private UserDao dao;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SongRepository songRepository;
    @Autowired
    private PlaylistRepository playlistRepository;

    @PostMapping("/register")
    public String registerUser(@RequestBody RegisterUserDTO dto) throws IOException, SQLException {
        JsonObject resp = new JsonObject();
        System.out.println(dto.toString());
        if (!dto.getConfirmPassword().equals(dto.getPassword())) {
            resp.addProperty("Status", "400");
            resp.addProperty("msg", "Passwords do not match");
            return resp.toString();
        }
        if (!dto.getPassword().matches(PASSWORD_REGEX)) {
            resp.addProperty("Status", "400");
            resp.addProperty("msg", "Password must contains one upper letter, one lower letter, one digit, one special symbol form {#?!@$%^&*-_} and be at least 8 characters");
            return resp.toString();
        }
        if (!dto.getEmail().matches(EMAIL_REGEX)) {
            resp.addProperty("Status", "400");
            resp.addProperty("msg", "Not a valid e-mail");
            return resp.toString();
        }
        if (dto.getEmail().isEmpty()
                || dto.getPassword().isEmpty()
                || dto.getConfirmPassword().isEmpty()
                || dto.getFirstName().isEmpty()
                || dto.getLastName().isEmpty()
                || dto.getGender().isEmpty()
                || dto.getBirthDate().isEmpty()) {
            resp.addProperty("Status", "400");
            resp.addProperty("msg", "Fill all fields");
            return resp.toString();
        }
        if (dao.doesExist(dto.getEmail())) {
            resp.addProperty("Status", "400");
            resp.addProperty("msg", "User already exist with this email");
            return resp.toString();
        }
        User user = dto.toUser();
        //users gender to be converted to gender_id

        userRepository.save(user);
        // dao.addUserToDB(dto.toUser());
        resp.addProperty("Status", "200");
        resp.addProperty("msg", "User is registered");
        return resp.toString();
    }//// TODO: 6.1.2020 г. send mail

    @PostMapping("/login")//ok
    public String loginUser(@RequestBody LoginUserDTO dto, HttpSession session) throws IOException {
        JsonObject resp = new JsonObject();
        String email = dto.getEmail();
        String pass = dto.getPassword();
        User user = userRepository.findByEmail(email);
        if (user == null || !user.getPassword().equals(pass)) {
            resp.addProperty("status", "401");
            resp.addProperty("msg", "wrong email or password");
            return resp.toString();
        }
        session.setAttribute("logged", user);

        return "Successfully login";
    }

    @PostMapping("/users/update")
    public User updateUser(HttpSession session, @RequestBody RegisterUserDTO updatedUser) throws NotLoggedUserException, SQLException, UserAlreadyExistsException {
        User user = validateUser(session);
        if (dao.doesExist(updatedUser.getEmail())) {
            throw new UserAlreadyExistsException();
        }
        user.setEmail(updatedUser.getEmail());
        user.setPassword(updatedUser.getPassword());
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setGender(updatedUser.getGender());
        user.setBirthDate(updatedUser.getBirthDate());
        userRepository.save(user);
        return user;
    }

    @PostMapping("/users/follow/{id}")
    public User followUser(HttpSession session, @PathVariable("id") long id) throws NotLoggedUserException, FollowUserException {

        User user = (User) session.getAttribute("logged");
        if (user == null) {
            throw new NotLoggedUserException();
        }
        User followedUser = userRepository.findById(id);
        dao.followUser(user, followedUser);
        return user;
    }

    @PostMapping("/users/songs/like/{id}")
    public Song likeSong(HttpSession session, @PathVariable("id") long id) throws NotLoggedUserException, SongAlreadyLikedException {
        User user = (User) session.getAttribute("logged");
        if (user == null) {
            throw new NotLoggedUserException();
        }
        Song song = songRepository.findById(id);
        dao.likeSong(user, song);
        return song;
    }

    //DELETE MAPPINGS
    @DeleteMapping("/users/unfollow/{id}")
    public User unfollowUser(HttpSession session, @PathVariable("id") long id) throws NotLoggedUserException, UnFollowUserException, SQLException {
        User user = (User) session.getAttribute("logged");
        if (user == null) {
            throw new NotLoggedUserException();
        }
        User targetUser = userRepository.findById(id);
        dao.unFollowUser(user, targetUser);
        return user;
    }

    @DeleteMapping("/users/songs/dislike/{id}")
    public Song dislikeSong(HttpSession session, @PathVariable("id") long id) throws NotLoggedUserException, UnFollowUserException, SQLException, NotLikedSongException {
        User user = (User) session.getAttribute("logged");
        if (user == null) {
            throw new NotLoggedUserException();
        }
        Song song = songRepository.findById(id);
        dao.dislikeSong(user, song);
        return song;
    }

    //GET MAPPINGS
    @GetMapping("/getItAll")
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @GetMapping("/users/followers")
    public List<User> getFollowers(HttpSession session) throws NotLoggedUserException, NoAccessException {
        User user = validateUser(session);
        return dao.getFollowers(user);
    }

    @GetMapping("/users/following")
    public List<User> getFollowing(HttpSession session) throws NotLoggedUserException, NoAccessException {
        User user = validateUser(session);
        return dao.getFollowing(user);
    }

    @GetMapping("/users/mySongs")
    public List<Song> mySongs(HttpSession session) throws NotLoggedUserException {
        User user = validateUser(session);
        return songRepository.findAllByUploaderId(user.getId());
    }

    @GetMapping("/users/myFavouriteSongs")
    public List<Song> myFavouriteSongs(HttpSession session) throws NotLoggedUserException {
        User user = validateUser(session);
        return dao.myFavouriteSongs(user);
    }

    @GetMapping("/users/myLists")
    public List<Playlist> myLists(HttpSession session) throws NotLoggedUserException {
        User user = validateUser(session);
        System.out.println(user.getId());
        return playlistRepository.findAllByCreator_Id(user.getId());
    }

    @GetMapping("/{user_id}/songs")
    public List<Song> getAllSongByUser(@PathVariable("user_id") long user_id){
        return songRepository.findAllByUploaderId(user_id);
    }

    //not mappings
    private User validateUser(HttpSession session) throws NotLoggedUserException {
        User user = (User) session.getAttribute("logged");
        if (user == null) {
            throw new NotLoggedUserException();
        }
        return user;
    }

    //Exception handler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "user already followed")
    @ExceptionHandler({FollowUserException.class})
    public void handleException() {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "user is not followed")
    @ExceptionHandler({UnFollowUserException.class})
    public void handleUnFollowUserException() {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "user already exist with this email")
    @ExceptionHandler({UserAlreadyExistsException.class})
    public void userAlreadyExistHandler() {
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "please login")
    @ExceptionHandler({NotLoggedUserException.class})
    public void loggedExceptionHandler() {

    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "access denied")
    @ExceptionHandler({NoAccessException.class})
    public void accessHandler() {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "song already liked")
    @ExceptionHandler({SongAlreadyLikedException.class})
    public void handleSongAlreadyLikedException() {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "song is not liked")
    @ExceptionHandler({NotLikedSongException.class})
    public void handleNotLikedSongException() {
    }
    //TESTING ZONE




}
