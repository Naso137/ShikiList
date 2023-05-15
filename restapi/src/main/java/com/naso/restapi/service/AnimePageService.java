package com.naso.restapi.service;

import com.naso.restapi.utils.Image;
import com.naso.restapi.repository.AnimePageRepository;
import com.naso.restapi.repository.CommentsRepository;
import com.naso.restapi.repository.NotificationsFromAnimeRepository;
import com.naso.restapi.repository.VideoRepository;
import com.naso.restapi.model.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class AnimePageService {
    @Value(value = "${link.shikimori}")
    private String shikimoriLink;
    private final RestTemplate restTemplate;
    private final AnimePageRepository animePageRepository;
    private final CommentsRepository commentsRepository;
    private final VideoRepository videoRepository;
    private final NotificationsFromAnimeRepository notificationsFromAnimeRepository;
    private final ProfileService profileService;

    @Autowired
    public AnimePageService(RestTemplate restTemplate, AnimePageRepository animePageRepository,
                             CommentsRepository commentsRepository, VideoRepository videoRepository,
                             NotificationsFromAnimeRepository notificationsFromAnimeRepository, ProfileService profileService) {
        this.restTemplate = restTemplate;
        this.animePageRepository = animePageRepository;
        this.commentsRepository = commentsRepository;
        this.videoRepository = videoRepository;
        this.notificationsFromAnimeRepository = notificationsFromAnimeRepository;
        this.profileService = profileService;
    }

    @Transactional
    public JsonObject addAnimePage(long animeId) throws IOException {
        AnimePage page = animePageRepository.findById(animeId);
        if (page != null) {
            throw new IOException("Anime page with the same id has already existed");
        }
        String animeInformation = restTemplate.getForObject(shikimoriLink + "/api/animes/" + animeId, String.class);
        JsonObject objectAnimeInformation = JsonParser.parseString(Objects.requireNonNull(animeInformation)).getAsJsonObject();
        String name = String.valueOf(objectAnimeInformation.get("name")).replaceAll("\"", "");
        String russian = String.valueOf(objectAnimeInformation.get("russian")).replaceAll("\"", "");
        String image = shikimoriLink +
                String.valueOf(objectAnimeInformation.get("image").getAsJsonObject().get("original")).replaceAll("\"", "");
        String description = String.valueOf(objectAnimeInformation.get("description_html")).replaceAll("\"", "");
        int episodes = Integer.parseInt(String.valueOf(objectAnimeInformation.get("episodes")));

        if (episodes == 0) {
            episodes = Integer.parseInt(String.valueOf(objectAnimeInformation.get("episodes_aired")));
        }

        AnimePage animePage = new AnimePage(animeId, name, russian, image, description, episodes);
        animePageRepository.save(animePage);

        objectAnimeInformation.addProperty("name", animePage.getOriginalName());
        objectAnimeInformation.addProperty("russian", animePage.getRussianName());
        objectAnimeInformation.getAsJsonObject("image").addProperty("original", animePage.getAnimeImage());
        objectAnimeInformation.addProperty("description_html", animePage.getDescription());

        return objectAnimeInformation;
    }

    @Transactional
    public List<AnimePage> findAll() {
        return animePageRepository.findAll();
    }

    @Transactional
    public List<AnimePage> findByNameOrRussianName(String word, String prevRussianAnimeName) throws IOException {
        if (prevRussianAnimeName == null) {
            prevRussianAnimeName = "";
        }
        List<AnimePage> list = animePageRepository
                .findTop5ByOriginalNameContainingOrRussianNameContainingAndRussianNameAfterOrderByRussianNameAsc
                        (word, word, prevRussianAnimeName);
        if (list == null) {
            throw new IOException("Nothing was found");
        }
        return list;
    }

    @Transactional
    public AnimePage findById(long animeId) {
        AnimePage animePage = animePageRepository.findById(animeId);
        if (animePage == null) {
            throw new NoSuchElementException("Not found id : " + animeId);
        }
        return animePage;
    }

    @Transactional
    public JsonObject getFullAnimePage(long animeId) {
        AnimePage page = findById(animeId);
        String animeInformation = restTemplate.getForObject(shikimoriLink + "/api/animes/" + animeId, String.class);
        JsonObject objectAnimeInformation = JsonParser.parseString(Objects.requireNonNull(animeInformation)).getAsJsonObject();

        objectAnimeInformation.addProperty("name", page.getOriginalName());
        objectAnimeInformation.addProperty("russian", page.getRussianName());
        objectAnimeInformation.getAsJsonObject("image").addProperty("original", page.getAnimeImage());
        objectAnimeInformation.addProperty("description_html", page.getDescription());
        objectAnimeInformation.addProperty("episodes", page.getNumberOfEpisodes());

        return objectAnimeInformation;
    }

    @Transactional
    public void updateAnimePage(AnimePage animePage) {
        animePageRepository.save(animePage);
    }

    @Transactional
    public AnimePage changeAnimePage(long id, AnimePage animePage) throws IOException {
        AnimePage newAnimePage = findById(id);

        newAnimePage.setOriginalName(animePage.getOriginalName());
        newAnimePage.setRussianName(animePage.getRussianName());

        newAnimePage.setAnimeImage(animePage.getAnimeImage());

        if (animePage.getAnimeImage().contains("shikimori.one")) {
            newAnimePage.setAnimeImage(animePage.getAnimeImage());
        } else {
            String picPath = Image.loadImage(animePage.getAnimeImage(), "animePagePictures");

            newAnimePage.setAnimeImage(picPath);

            if (!picPath.contains("shikimori.one")) {
                Path path = Paths.get("");

                String filepath = path.toAbsolutePath().toString();

                char delimitter;

                if (filepath.charAt(0) == '/') {
                    delimitter = '/';
                } else {
                    delimitter = '\\';
                }

                filepath = filepath.substring(0, filepath.indexOf(delimitter + "server"));

                Image.deleteFile(filepath + delimitter + "client" + delimitter + "public" + delimitter + picPath);
            }
        }

        newAnimePage.setDescription(animePage.getDescription());
        newAnimePage.setNumberOfEpisodes(animePage.getNumberOfEpisodes());

        animePageRepository.save(newAnimePage);
        return newAnimePage;
    }

    @Transactional
    public void deleteAnimePage(long animeId) throws IOException {
        AnimePage animePage = findById(animeId);

        List<Profile> profileList = animePage.getProfiles();

        for (Profile profile: profileList) {
            List<AnimePage> animePageList = profile.getAnimePageList();

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

            animePageList.remove(animePage);
            profile.setAnimePageList(animePageList);
            profileService.updateProfile(profile);
        }

        List<Comments> comments = animePage.getComments();
        List<Video> video = animePage.getVideoList();

        commentsRepository.deleteAll(comments);
        videoRepository.deleteAll(video);

        animePage.setComments(null);
        animePage.setVideoList(null);

        animePageRepository.delete(animePage);
        if (animePageRepository.findById(animeId) != null) {
            throw new IOException("Anime page hasn't ben deleted");
        }
    }
}
