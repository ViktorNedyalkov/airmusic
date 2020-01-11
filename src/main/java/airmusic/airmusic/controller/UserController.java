package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.DAO.UserDao;
import airmusic.airmusic.model.DTO.*;
import airmusic.airmusic.model.POJO.User;
import airmusic.airmusic.model.repositories.UserRepository;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@RestController
public class UserController extends AbstractController{
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
    private static final LocalDate MIN_DATE_REQUIRED_FOR_REGISTRATION = LocalDate.of(1900,01,01);


    @Autowired
    private UserDao userDao;
    @Autowired
    private UserRepository userRepository;

    @SneakyThrows
    @PostMapping("/register")
    public String registerUser(@RequestBody RegisterUserDTO dto) {
        isPasswordCorrect(dto.getPassword(),dto.getConfirmPassword());
        if (!dto.getEmail().trim().matches(EMAIL_REGEX)) {
           throw new BadRequestException("Not a valid e-mail");
        }
        if (userRepository.existsByEmail(dto.getEmail())){
            throw new BadRequestException("User already exists with this e-mail");
        }
        if (dto.getEmail().isEmpty()
                || dto.getPassword().isEmpty()
                || dto.getConfirmPassword().isEmpty()
                || dto.getFirstName().isEmpty()
                || dto.getLastName().isEmpty()
                || dto.getGender().isEmpty()
                || dto.getBirthDate().isEmpty()) {
            throw new BadRequestException("All fills are required");
        }
        validateDate(dto.getBirthDate());
        User user = dto.toUser();
        userRepository.save(user);
        new airmusc.airmusic.MailSender(user.getEmail(),"Profile Activation ", "localhost:3306/users/activation/" +user.getId()).start();
        return "Successfully registered! Check mail for activation link!";
    }
    @SneakyThrows
    @PostMapping("/users/activation/{id}")
    public User activateUser(@PathVariable("id") long userId){
       Optional<User> user = userRepository.findById(userId);
       if (!user.isPresent()){
           throw new BadRequestException("No such user");
       }
       if(user.get().isActivated()){
           throw new BadRequestException("user already activated");
       }
       user.get().setActivated(true);
       return userRepository.save( user.get());
    }
    private boolean isPasswordCorrect(String password, String confirmPassword) throws BadRequestException {
        if (!confirmPassword.equals(password)) {
            throw new BadRequestException("Passwords must match");
        }
        if (!password.matches(PASSWORD_REGEX)) {
            throw  new BadRequestException("Password must contains one upper letter, one lower letter, one digit, one special symbol form {#?!@$%^&*-_} and be at least 8 characters");
        }
        if (!password.equals(confirmPassword.trim())){
            throw  new BadRequestException("Password can`t have blank characters");
        }
        return true;
    }
    private void validateDate(@RequestBody String date) throws BadRequestException, SQLException {
        if (date.matches(DATE_REGEX)){
            if (LocalDate.parse(date).isAfter(LocalDate.now())||
                LocalDate.parse(date).isBefore(MIN_DATE_REQUIRED_FOR_REGISTRATION)){
                 throw new BadRequestException("Invalid date");
            }
        }
        else{
            throw new BadRequestException("Invalid date");
        }
    }

    @PostMapping("/login")//ok
    public String loginUser(@RequestBody LoginUserDTO dto, HttpSession session) throws BadRequestException {
        String email = dto.getEmail();
        String pass = dto.getPassword();
        User user = userRepository.findByEmail(email);
        if (user == null||!BCrypt.checkpw(pass,user.getPassword())) {
            throw new BadRequestException("Wrong e-mail or password");
        }
        if (!user.isActivated()){
            airmusc.airmusic.MailSender.sendMail(user.getEmail().trim(),"Activation mail","localhost:3306/users/activation/"+user.getId());
            throw new BadRequestException("Check your email and activate your profile");
        }
        session.setAttribute(LOGGED, user);

        return "Successfully login";
    }

    @PutMapping("/users/pi")
    public User updateUser(HttpSession session, @RequestBody UpdateInformationUserDTO updatedUser) throws SQLException, BadRequestException {
        User user = validateUser(session);
        if (userDao.doesExist(updatedUser.getEmail())&&
            !user.getEmail().equals(updatedUser.getEmail())) {
            throw new BadRequestException("User already exists with this e-mail");
        }
        user.setEmail(updatedUser.getEmail());
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setGender(updatedUser.getGender());
        validateDate(updatedUser.getBirthDate());
        user.setBirthDate(updatedUser.getBirthDate());
        userRepository.save(user);
        return user;
    }
    @PutMapping("users/pw")
    public User changePassword(HttpSession session, @RequestBody ChangePasswordUserDTO dto) throws BadRequestException {
        User user = validateUser(session);
        if (isPasswordCorrect(dto.getNewPassword(),dto.getConfirmNewPassword())&&
            BCrypt.checkpw(dto.getOldPassword(),user.getPassword())){
            user.setPassword(dto.getNewPassword());
            return user;
        }
        else {
            throw new BadRequestException("Wrong Credentials");
        }
    }

    @PostMapping("/users/follow/{id}")
    public User followUser(HttpSession session, @PathVariable("id") long id) throws  BadRequestException {
        User user = validateUser(session);
        Optional<User> followedUser = userRepository.findById(id);
        if (!followedUser.isPresent()){
            throw new BadRequestException("No such user");
        }
        userDao.followUser(user, followedUser.get());
        return user;
    }

    //DELETE MAPPINGS
    @DeleteMapping("/users/unfollow/{id}")
    public User unfollowUser(HttpSession session, @PathVariable("id") long id) throws SQLException, BadRequestException {
        User user = validateUser(session);
        Optional<User> targetUser = userRepository.findById(id);
        if (!targetUser.isPresent()){
            throw new BadRequestException("No such user");
        }
        userDao.unFollowUser(user, targetUser.get());
        return user;
    }



    //GET MAPPINGS
    @SneakyThrows
    @GetMapping("/users/followers")
    public List<User> getFollowers(HttpSession session)  {
        User user = validateUser(session);
        return userDao.getFollowers(user);
    }

    @GetMapping("/users/following")
    public List<User> getFollowing(HttpSession session) throws SQLException {
        User user = validateUser(session);
        return userDao.getFollowing(user);
    }

    @GetMapping("users/find")
    public List<User> findUser(@RequestBody SearchDTO dto){//finds users By firstName or LastName
        List <User> searchResult = userRepository.findAllByFirstNameContaining(dto.getSearchingFor().trim());
        List<User> lastNameResult = userRepository.findAllByLastNameContaining(dto.getSearchingFor().trim());
        searchResult.removeAll(lastNameResult);
        searchResult.addAll(lastNameResult);
        return searchResult;
    }
    @SneakyThrows
    @PostMapping("/users/picture")
    public User addPicture(@RequestParam(value = "avatar") MultipartFile file,
                           HttpSession session) {
        User user = validateUser(session);
        if(file == null){
            throw new IllegalValuePassedException("Please upload a file");
        }
        if(!file.getContentType().equalsIgnoreCase("image/png")){
            throw new IllegalValuePassedException("Please upload a png file");
        }
        byte[] fileBytes = file.getBytes();

        String avatarUrl = UPLOAD_PATH + user.getId() + System.currentTimeMillis();
        Path path = Paths.get(avatarUrl);
        Files.write(path, fileBytes);
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        return user;
    }
}
