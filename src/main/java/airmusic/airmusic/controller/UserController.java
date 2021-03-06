package airmusic.airmusic.controller;

import airmusic.airmusic.MailSender;
import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.DAO.UserDao;
import airmusic.airmusic.model.DTO.*;
import airmusic.airmusic.model.POJO.Gender;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.GenderRepository;
import airmusic.airmusic.model.repositories.UserRepository;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@RestController
public class UserController extends AbstractController {
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*?[#?!@$%^&*-])(?=.*[a-zA-Z]).{8,}$";
    ;
    /*
    At least one upper case English letter, (?=.*?[A-Z])
At least one lower case English letter, (?=.*?[a-z])
At least one digit, (?=.*?[0-9])
At least one special character, (?=.*?[#?!@$%^&*-])
Minimum eight in length .{8,} (with the anchors)
     */
    private static final String UPLOAD_PATH = "D:\\pics\\";

    private static final String EMAIL_REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static final String DATE_REGEX = "([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))";
    private static final String LOGGED = "logged";
    private static final LocalDate MIN_DATE_REQUIRED_FOR_REGISTRATION = LocalDate.of(1900, 01, 01);
    private static final String USER_ACTIVATION_PATH = "localhost:8080/users/activation/";
    private static final String USER_ACTIVATION_SUBJECT = "Profile Activation ";
    private static final String WRONG_PASSWORD_MAIL_SUBJECT = "ATTEMPT TO ENTER IN YOUR ACCOUNT";
    private static final String WRONG_PASSWORD_EMAIL_MSG = "Your account has been stopped for UNAUTHORIZED attempt to login " +
            "Follow the link to reactivate your account ";
    private static final String REPLACER_FOR_SLASH = "-";
    public static final int NUMBER_OF_GENDERS_IN_DB = 3;


    @Autowired
    private UserDao userDao;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GenderRepository genderRepository;

    @SneakyThrows
    @PostMapping("/register")
    public String registerUser(@RequestBody RegisterUserDTO dto) {
        if (dto.getEmail() == null
                || dto.getPassword() == null
                || dto.getConfirmPassword() == null
                || dto.getFirstName() == null
                || dto.getLastName() == null
        ) {
            throw new BadRequestException("All fills are required");
        }
        isPasswordCorrect(dto.getPassword(), dto.getConfirmPassword());
        if (!dto.getEmail().trim().matches(EMAIL_REGEX)) {
            throw new BadRequestException("Not a valid e-mail");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("User already exists with this e-mail");
        }

        if (dto.getEmail().isEmpty()
                || dto.getPassword().isEmpty()
                || dto.getConfirmPassword().isEmpty()
                || dto.getFirstName().isEmpty()
                || dto.getLastName().isEmpty()
        ) {
            throw new BadRequestException("All fills are required");
        }
        User user = dto.toUser();
        Optional<Gender> g = genderRepository.findById(dto.getGender());
        if (g.isEmpty()){
            throw new BadRequestException("Wrong gender type");
        }
        user.setGender(g.get());
        validateDate(dto.getBirthDate().toString());

        userRepository.save(user);
        new MailSender(user.getEmail(),
                USER_ACTIVATION_SUBJECT,
                USER_ACTIVATION_PATH +
                        user.getEmail() +
                        "/" + activationCode(user.getId())).start();
        return "Successfully registered! Check mail for activation link!";
    }

    private String activationCode(long id) {
        String code = BCrypt.hashpw(String.valueOf(id), BCrypt.gensalt());
        return code.replace("/", REPLACER_FOR_SLASH);
    }

    @SneakyThrows
    @PostMapping("/users/activation/{userEmail}/{activationCode}")
    public ResponseUserDTO activateUser(HttpSession session,
                                        @PathVariable("activationCode") String code,
                                        @PathVariable("userEmail") String email) {
        String activationCode = code.replace(REPLACER_FOR_SLASH, "/");
        Optional<User> user = userRepository.findByEmail(email);
        if (!user.isPresent() || !BCrypt.checkpw(String.valueOf(user.get().getId()), activationCode)) {
            throw new BadRequestException("No such user");
        }
        if (user.get().isActivated()) {
            throw new BadRequestException("user already activated");
        }
        user.get().setActivated(true);
        session.setAttribute(LOGGED, user.get());
        return new ResponseUserDTO(userRepository.save(user.get()));
    }

    @PostMapping("/login")//ok
    public ResponseUserDTO loginUser(@RequestBody LoginUserDTO dto, HttpSession session)
            throws BadRequestException {
        String email = dto.getEmail();
        String pass = dto.getPassword();
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new BadRequestException("Wrong e-mail or password");
        }
        if (!BCrypt.checkpw(pass, user.get().getPassword())) {
            throw new BadRequestException("Wrong e-mail or password");
        }
        if (!user.get().isActivated()) {

            throw new BadRequestException("Check your email and activate your profile");
        }
        session.setAttribute(LOGGED, user.get());
        return new ResponseUserDTO(user.get());
    }

    @PostMapping("/logout")//ok
    public String logout(HttpSession session) {
        session.invalidate();
        return "Logged out!";
    }

    @SneakyThrows
    @PostMapping("/users/picture")
    public ResponseUserDTO addPicture(@RequestParam(value = "avatar") MultipartFile file,
                                      HttpSession session) {
        User user = validateUser(session);
        if (file == null) {
            throw new IllegalValuePassedException("Please upload a file");
        }
        if (!file.getContentType().equalsIgnoreCase("image/png")) {
            throw new IllegalValuePassedException("Please upload a png file");
        }
        byte[] fileBytes = file.getBytes();
        String oldAvatarUrl = user.getAvatar();
        String avatarUrl = "Avatar" + user.getId() + System.currentTimeMillis();
        Path path = Paths.get(UPLOAD_PATH + avatarUrl);
        Files.write(path, fileBytes);
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        System.out.println(UPLOAD_PATH + oldAvatarUrl);
        Files.deleteIfExists(Paths.get(UPLOAD_PATH + oldAvatarUrl));
        return new ResponseUserDTO(user);
    }

    @PostMapping("/users/follow/{id}")
    public ResponseUserDTO followUser(HttpSession session, @PathVariable("id") long id) throws BadRequestException {
        User user = validateUser(session);
        Optional<User> followedUser = userRepository.findById(id);
        if (!followedUser.isPresent()) {
            throw new BadRequestException("No such user");
        }
        userDao.followUser(user, followedUser.get());
        return new ResponseUserDTO(user);
    }

    //put
    @PutMapping("/users/pi")

    public ResponseUserDTO updateUser(HttpSession session,
                                      @RequestBody UpdateInformationUserDTO updatedUser) throws SQLException, BadRequestException {

        User user = validateUser(session);
        if (userDao.doesExist(updatedUser.getEmail()) &&
                !user.getEmail().equals(updatedUser.getEmail())) {
            throw new BadRequestException("User already exists with this e-mail");
        }
        if(updatedUser.getEmail() == null){
            updatedUser.setEmail(user.getEmail());
        }
        if(updatedUser.getBirthDate() == null){
            updatedUser.setBirthDate(user.getBirthDate());
        }
        if(updatedUser.getFirstName() == null){
            updatedUser.setFirstName(user.getFirstName());
        }
        if(updatedUser.getLastName() == null){
            updatedUser.setLastName(user.getLastName());
        }
        if(updatedUser.getGender() > NUMBER_OF_GENDERS_IN_DB){
            throw new BadRequestException("Please enter a valid gender");
        }
        updatedUser.toUser(user);
        validateGender(updatedUser.getGender(), user);
        validateDate(updatedUser.getBirthDate().toString());
        userRepository.save(user);
        return new ResponseUserDTO(user);
    }

    private void validateGender(long genderId, User user) {
        Optional<Gender> g = genderRepository.findById(genderId);
        if (g.isEmpty()){
            return;
        }
        user.setGender(g.get());
    }

    @PutMapping("users/pw")
    public ResponseUserDTO changePassword(HttpSession session, @RequestBody ChangePasswordUserDTO dto) throws BadRequestException {
        User user = validateUser(session);
        if (isPasswordCorrect(dto.getNewPassword(), dto.getConfirmNewPassword()) &&
                BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            user.setPassword(dto.getNewPassword());
            return new ResponseUserDTO(user);
        } else {
            throw new BadRequestException("Wrong Credentials");
        }
    }


    //DELETE MAPPINGS
    @DeleteMapping("/users/unfollow/{id}")
    public ResponseUserDTO unfollowUser(HttpSession session, @PathVariable("id") long id) throws SQLException, BadRequestException {
        User user = validateUser(session);
        Optional<User> targetUser = userRepository.findById(id);
        if (!targetUser.isPresent()) {
            throw new BadRequestException("No such user");
        }
        userDao.unFollowUser(user, targetUser.get());
        return new ResponseUserDTO(user);
    }

    //GET MAPPINGS
    @SneakyThrows
    @GetMapping("/users/followers")
    public List<ResponseUserDTO> getFollowers(HttpSession session) {
        User user = validateUser(session);
        return (userDao.getFollowers(user));
    }

    @GetMapping("/users/following")
    public List<ResponseUserDTO> getFollowing(HttpSession session) throws SQLException {
        User user = validateUser(session);
        return (userDao.getFollowing(user));
    }

    @SneakyThrows
    @GetMapping("users/find")
    public List<ResponseUserDTO> findUser(@RequestBody SearchDTO dto) {//finds users By firstName or LastName
        List<ResponseUserDTO> result = userDao.searchUser(dto);
        System.out.println(result.size());
        return result;
    }


    private boolean isPasswordCorrect(String password, String confirmPassword) throws BadRequestException {
        if (!confirmPassword.equals(password)) {
            throw new BadRequestException("Passwords must match");
        }
        if (!password.matches(PASSWORD_REGEX)) {
            throw new BadRequestException("Password must contains one upper letter, one lower letter, one digit, one special symbol form {#?!@$%^&*-_} and be at least 8 characters");
        }
        if (!password.equals(confirmPassword.trim())) {
            throw new BadRequestException("Password can`t have blank characters");
        }
        return true;
    }

    private void validateDate(@RequestBody String date) throws BadRequestException, SQLException {
        if (date.matches(DATE_REGEX)) {
            if (LocalDate.parse(date).isAfter(LocalDate.now()) ||
                    LocalDate.parse(date).isBefore(MIN_DATE_REQUIRED_FOR_REGISTRATION)) {
                throw new BadRequestException("Invalid date");
            }
        } else {
            throw new BadRequestException("Invalid date");
        }
    }



}
