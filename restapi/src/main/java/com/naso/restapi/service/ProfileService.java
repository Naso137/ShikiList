package com.naso.restapi.service;

import com.naso.restapi.dto.ProfileDto;
import com.naso.restapi.dto.RegisterDto;
import com.naso.restapi.security.jwt.JwtResponse;
import com.naso.restapi.security.jwt.JwtUtils;
import com.naso.restapi.security.jwt.TokenRefreshResponse;
import com.naso.restapi.utils.Image;
import com.naso.restapi.repository.*;
import com.naso.restapi.model.*;
import com.google.gson.JsonObject;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfileService implements UserDetailsService {
    @Value(value = "${jedis.host}")
    private String jedisHost;
    @Value("${jedis.port}")
    private int jedisPort;
    @Value(value = "${spring.mail.username}")
    private String supervisorMail;
    @Value("${jwtActivationExpirationMs}")
    private int jwtActivationExpirationMs;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RoleService roleService;
    private final AnimePageRepository animePageRepository;
    private final VideoRepository videoRepository;
    private final NotificationsFromAnimeRepository notificationsFromAnimeRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;
    private final JwtUtils jwtUtils;
    private final ActivationCodeRepository activationCodeRepository;

    private final JedisPool jedisPool;

    @Autowired
    public ProfileService(UserRepository userRepository, ProfileRepository profileRepository, RoleService roleService,
                           AnimePageRepository animePageRepository, VideoRepository videoRepository,
                           NotificationsFromAnimeRepository notificationsFromAnimeRepository, PasswordEncoder passwordEncoder,
                           MailSenderService mailSenderService, JwtUtils jwtUtils, ActivationCodeRepository activationCodeRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.roleService = roleService;
        this.animePageRepository = animePageRepository;
        this.videoRepository = videoRepository;
        this.notificationsFromAnimeRepository = notificationsFromAnimeRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSenderService = mailSenderService;
        this.jwtUtils= jwtUtils;
        this.activationCodeRepository = activationCodeRepository;
        this.jedisPool = new JedisPool("localhost", 32768);


        Thread run = new Thread(() -> {
            while(true){
                try {
                    Thread.sleep(60000);
                    deleteUnactivatedAccounts();
                } catch (InterruptedException ex) {
                }
            }
        });
        run.start();
    }

    @Transactional
    public JwtResponse authenticateUser(String login, String password) throws IOException {
        User user = findUserByMail(login);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            long invalidLogIn = user.getInvalidLogIn();
            invalidLogIn++;
            user.setInvalidLogIn(invalidLogIn);
            userRepository.save(user);
            throw new IOException("Incorrect password. Invalid count of log-in: " + invalidLogIn);
        }

        ActivationCode activationCode = findActivationCode(user.getId());
        if (activationCode != null) {
            throw new IOException("Profile hasn't been activated yet");
        }

        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        updatePair(refreshToken, user.getMail());

        user.setInvalidLogIn(0);

        userRepository.save(user);

        return new JwtResponse(accessToken, refreshToken, user.getId());
    }

    @Transactional
    public TokenRefreshResponse refreshToken(String token) throws IOException {
        String mail = null;

        jwtUtils.validateRefreshJwtToken(token);

        try(Jedis jedis = jedisPool.getResource()) {
            mail = jedis.get(token);
        }

        if (mail == null) {
            throw new IOException("Invalid token!");
        }

        User user = findUserByMail(mail);

        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        updatePair(refreshToken, mail);

        return new TokenRefreshResponse(accessToken, refreshToken);
    }

    public void signOut(String refreshToken) throws IOException {
        String cursor = "0";
        ScanParams scanParams = new ScanParams().count(1);

        String mail = "";

        try (Jedis jedis = jedisPool.getResource()) {
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);

                if ((scanResult.getResult().size() != 0) && scanResult.getResult().get(0).equals(refreshToken)) {
                    mail = jedis.get(scanResult.getResult().get(0));
                    jedis.del(scanResult.getResult().get(0));
                }

                cursor = scanResult.getCursor();
            } while (!"0".equals(cursor));
        }

        if (Objects.equals(mail, "")) {
            throw new IOException("Mail hasn't found");
        }

        User user = findUserByMail(mail);
        Profile profile = findProfileById(user.getId());

        profile.setIdShikimori(0);

        List<Roles> roles = profile.getRoles();
        Roles role = roleService.findByName("SHIKIMORI");
        if (roles.contains(role)) {
            roles.remove(role);
        }
        role = roleService.findByName("MYANIMELIST");
        if (roles.contains(role)) {
            roles.remove(role);
        }
        profile.setRoles(roles);

        updateProfile(profile);
    }

    public void updatePair(String refreshToken, String username) {
        String cursor = "0";
        ScanParams scanParams = new ScanParams().count(1);


        try (Jedis jedis = jedisPool.getResource()) {
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);

                if ((scanResult.getResult().size() != 0) && jedis.get(scanResult.getResult().get(0)).equals(username)) {
                    jedis.del(scanResult.getResult().get(0));
                }

                cursor = scanResult.getCursor();
            } while (!"0".equals(cursor));

            jedis.set(refreshToken, username);
        }
    }

    @Transactional
    public void addProfile(RegisterDto registerUser) throws IOException, MessagingException {
        Profile profile = profileRepository.findByLogin(registerUser.getLogin());
        if (profile != null) {
            throw new IOException("Profile with the same name has already existed");
        }
        User user = userRepository.findByMail(registerUser.getMail());
        if (user != null) {
            throw new IOException("Profile with the same mail has already existed");
        }

        user = new User(registerUser.getMail(), passwordEncoder.encode(registerUser.getPassword()), 0);
        user = userRepository.save(user);

        ActivationCode activationCode = new ActivationCode(user, UUID.randomUUID().toString(),
                new Date((new Date()).getTime() + jwtActivationExpirationMs));

        activationCode = activationCodeRepository.save(activationCode);

        profile = new Profile(registerUser.getLogin(), user,"profilePictures/avatar.jpg");

        profileRepository.save(profile);

        if (!StringUtils.isEmpty(user.getMail())) {
            String message = String.format(
                    "Здравствуйте! " +
                            "Для подтверждения аккаунта сервиса ShikiList необходимо перейти по следующей ссылке: " +
                            "<a href=\"https://localhost:3000/activation/" + activationCode.getCode() +
                            "\"> Нажмите сюда </a>"
            );
            mailSenderService.send(user.getMail(), "Activation code", message);
        } else {
            throw new IOException("User should have an email");
        }
    }

    @Transactional
    public JsonObject changeProfile(String mail, ProfileDto profileDto) throws IOException {
        User user = findUserByMail(mail);
        Profile profile = findProfileById(user.getId());

        if (profileRepository.findByLogin(profileDto.getLogin()) != null) {
            throw new IOException("Profile with the same login has already exists");
        }
        user.setPassword(profileDto.getPassword());

        profile.setLogin(profileDto.getLogin());
        profile.setDescription(profileDto.getDescription());
        if (profileDto.getAvatar().contains("profilepicture")) {
            profile.setAvatar(profileDto.getAvatar());
        } else {
            String picPath = Image.loadImage(profileDto.getAvatar(), "profilePictures");

            profile.setAvatar(picPath);

            if (!picPath.contains("avatar")) {
                Path path = Paths.get("");

                String filepath = path.toAbsolutePath().toString();

                char delimiter;

                if (filepath.charAt(0) == '/') {
                    delimiter = '/';
                } else {
                    delimiter = '\\';
                }

                filepath = filepath.substring(0, filepath.indexOf(delimiter + "server"));

                Image.deleteFile(filepath + delimiter + "client" + delimiter + "public" + delimiter + picPath);
            }
        }
        profile.setShikimoriLink(profileDto.getShikimoriLink());
        profile.setMyAnimeListLink(profileDto.getMyAnimeListLink());

        User newUser = userRepository.save(user);
        Profile newProfile = profileRepository.save(profile);

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", newProfile.getId());
        jsonObject.addProperty("login", newProfile.getLogin());
        jsonObject.addProperty("mail", newUser.getMail());
        jsonObject.addProperty("password", newUser.getPassword());
        jsonObject.addProperty("description", newProfile.getDescription());
        jsonObject.addProperty("avatar", newProfile.getAvatar());
        jsonObject.addProperty("shikimoriLink", newProfile.getShikimoriLink());
        jsonObject.addProperty("myAnimeListLink", newProfile.getMyAnimeListLink());

        return jsonObject;
    }

    @Transactional
    public List<Profile> getUserProfiles(String word, String prevProfileLogin) {
        if (prevProfileLogin == null) {
            prevProfileLogin = "";
        }
        List<Profile> profileArrayList = profileRepository.findTop5ByLoginContainingAndLoginAfter(word, prevProfileLogin);
        Profile deleteProfile = null;
        for (Profile profile : profileArrayList) {
            User user = findUserById(profile.getId());
            if (user.getMail().equals(supervisorMail)) {
                deleteProfile = profile;
            }
        }
        if (deleteProfile != null) {
            profileArrayList.remove(deleteProfile);
        }
        return profileArrayList;
    }

    @Transactional
    public boolean isCorrectPassword(String mail, String password) throws IOException {
        User user = findUserByMail(mail);
        return password.equals(user.getPassword());
    }

    @Transactional
    public Profile updateProfile(Profile profile){
        return profileRepository.save(profile);
    }


    @Transactional
    public ActivationCode findActivationCode(long userId) {
        return activationCodeRepository.findByUserId(userId);
    }

    @Transactional
    public User findUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(()->
                new NoSuchElementException("Not found profile with id : " + userId));
    }

    @Transactional
    public Profile findProfileById(long profileId) throws IOException {
        return profileRepository.findById(profileId).orElseThrow(()->
                new IOException("Not found profile with id : " + profileId));
    }

    @Transactional
    public User findUserByMail(String mail) throws IOException {
        User user = userRepository.findByMail(mail);
        if (user == null) {
            throw new IOException("Not found profile with mail : " + mail);
        }
        return user;
    }

    @Transactional
    public JsonObject getProfile(String login) {
        Profile profile = profileRepository.findByLogin(login);
        return profile.toJson();
    }

    @Transactional
    public boolean activateUser(String code) throws IOException {
        ActivationCode activationCode = activationCodeRepository.findByCode(code);
        if (activationCode == null) {
            throw new IOException("Activation code is incorrect. Register again.");
        }

        Profile profile = findProfileById(activationCode.getUserId());
        User user = findUserById(profile.getId());

        if (activationCode.getExpiryDate().compareTo(new Date((new Date()).getTime())) < 0) {
            deleteUser(user);
            throw new IOException("Activation code was expired. Register again.");
        }

        Roles roles = roleService.findByName("USER");
        profile.addRole(roles);

        if (user.getMail().equals(supervisorMail)) {
            roles = roleService.findByName("SUPERVISOR");
            profile.addRole(roles);
        }

        profileRepository.save(profile);
        activationCodeRepository.delete(activationCode);
        return true;
    }

    @Transactional
    public Profile addAnimePage(String mail, int animeId) throws IOException {
        User user = findUserByMail(mail);
        Profile profile = findProfileById(user.getId());
        AnimePage animePage = animePageRepository.findById(animeId);
        if (animePage == null) {
            throw new IOException("Not found id : " + animeId);
        }
        List<AnimePage> animePages = profile.getAnimePageList();
        if (animePages.contains(animePage)) {
            throw new IOException("User has already been subscribed");
        }
        profile.addAnimePage(animePage);
        animePage.addProfile(profile);
        profileRepository.save(profile);
        animePageRepository.save(animePage);
        return profile;
    }

    @Transactional
    public void unsubscribeFromAnimePage(String mail, int animeId) throws IOException {
        User user = findUserByMail(mail);
        Profile profile = findProfileById(user.getId());
        AnimePage animePage = animePageRepository.findById(animeId);
        if (animePage == null) {
            throw new IOException("Not found id : " + animeId);
        }
        List<AnimePage> animePages = profile.getAnimePageList();
        List<Profile> profiles = animePage.getProfiles();
        if (!animePages.contains(animePage)) {
            throw new IOException("User hasn't been subscribed on this anime yet");
        }
        if (!profiles.contains(profile)) {
            throw new IOException("this anime page hasn't got this profile");
        }
        List<NotificationsFromAnime> notifications = notificationsFromAnimeRepository.findAllByAnimePageAndProfile(animePage, profile);

        if (notifications.size() > 0) {
            List<NotificationsFromAnime> notificationsProfile = profile.getNotificationsFromAnimeList();
            List<NotificationsFromAnime> notificationsAnime = animePage.getNotifications();

            notificationsProfile.removeAll(notifications);
            notificationsAnime.removeAll(notifications);

            profile.setNotificationsFromAnimeList(notificationsProfile);
            animePage.setNotifications(notificationsAnime);

            for (NotificationsFromAnime notification: notifications ) {
                Video video = notification.getVideo();
                List<NotificationsFromAnime> notificationsVideo = video.getNotifications();
                notificationsVideo.remove(notification);
                video.setNotifications(notificationsVideo);
                videoRepository.save(video);
            }

            notifications.forEach(notification -> notification.setAnimePage(null));
            notifications.forEach(notification -> notification.setProfile(null));
            notifications.forEach(notification -> notification.setVideo(null));

            notificationsFromAnimeRepository.deleteAll(notifications);
        }

        profiles.remove(profile);
        animePages.remove(animePage);
        profile.setAnimePageList(animePages);
        animePage.setProfiles(profiles);

        profileRepository.save(profile);
        animePageRepository.save(animePage);
    }

    @Transactional
    public Profile setAdminRole(String login, String mail) throws IOException {
        Profile profile = profileRepository.findByLogin(login);
        if (profile == null) {
            throw new IOException("Profile with the same name doesn't exist");
        }
        User user = findUserById(profile.getId());
        if (user.getMail().equals(mail)) {
            throw new IOException("You're a supervisor");
        }

        Roles role = roleService.findByName("ADMIN");
        profile.addRole(role);

        return profileRepository.save(profile);
    }

    @Transactional
    public Profile removeAdminRole(String login, String mail) throws IOException {
        Profile profile = profileRepository.findByLogin(login);
        if (profile == null) {
            throw new IOException("Profile with the same name doesn't exist");
        }
        User user = findUserById(profile.getId());
        if (user.getMail().equals(mail)) {
            throw new IOException("You're a supervisor");
        }
        List<Roles> roles = profile.getRoles();
        Roles role = roleService.findByName("ADMIN");
        roles.remove(role);
        profile.setRoles(roles);

        return profileRepository.save(profile);
    }

    @Transactional
    public void deleteUnactivatedAccounts() {
        List<ActivationCode> activationCodes = activationCodeRepository.findAllByExpiryDateBefore(new Date((new Date()).getTime()));
        for (ActivationCode activationCode : activationCodes) {
            deleteUser(activationCode.getUser());
        }
    }

    @Transactional
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        User user = null;
        try {
            user = findUserByMail(mail);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Profile profile = profileRepository.findById(user.getId()).orElseThrow(()->
                new UsernameNotFoundException("Profile with such id doesn't exists"));
        return new org.springframework.security.core.userdetails.User(user.getMail(), user.getPassword(),
                mapRolesToAuthorities(profile.getRoles()));
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Roles> roles) {
        return roles.stream().map(r -> new SimpleGrantedAuthority(r.getName())).collect(Collectors.toList());
    }
}
