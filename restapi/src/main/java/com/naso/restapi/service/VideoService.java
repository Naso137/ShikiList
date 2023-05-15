package com.naso.restapi.service;

import com.naso.restapi.dto.VideoDto;
import com.naso.restapi.repository.*;
import com.naso.restapi.model.AnimePage;
import com.naso.restapi.model.NotificationsFromAnime;
import com.naso.restapi.model.Profile;
import com.naso.restapi.model.Video;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final AnimePageRepository animePageRepository;
    private final ProfileRepository profileRepository;
    private final NotificationsFromAnimeRepository notificationsFromAnimeRepository;

    @Autowired
    public VideoService(VideoRepository videoRepository, AnimePageRepository animePageRepository,
                         ProfileRepository profileRepository, NotificationsFromAnimeRepository notificationsFromAnimeRepository) {
        this.videoRepository = videoRepository;
        this.animePageRepository = animePageRepository;
        this.profileRepository = profileRepository;
        this.notificationsFromAnimeRepository = notificationsFromAnimeRepository;
    }

    @Transactional
    public Video addVideo(VideoDto videoDto) throws IOException {
        AnimePage page = animePageRepository.findById(videoDto.getAnimeId());
        if (page == null) {
            throw new IOException("Anime page with the same id hasn't existed yet");
        }
        if (videoDto.getNumber() < 1 || videoDto.getNumber() > page.getNumberOfEpisodes()) {
            throw new IOException("Number of episodes is incorrect");
        }

        Video video = new Video(page, videoDto.getType(), videoDto.getName(), videoDto.getLink(), videoDto.getNumber());
        Video newVideo = videoRepository.save(video);

        List<Profile> profiles = page.getProfiles();
        for (Profile profile: profiles) {
            NotificationsFromAnime notificationsFromAnime = new NotificationsFromAnime(profile, new Timestamp(System.currentTimeMillis()));
            notificationsFromAnime.setAnimePage(page);
            notificationsFromAnime.setVideo(newVideo);

            profile.addNotificationFromAnime(notificationsFromAnime);
            page.addNotification(notificationsFromAnime);
            newVideo.addNotification(notificationsFromAnime);

            notificationsFromAnimeRepository.save(notificationsFromAnime);
            profileRepository.save(profile);
        }
        page.addVideo(video);
        animePageRepository.save(page);
        newVideo = videoRepository.save(newVideo);
        return newVideo;
    }

    @Transactional
    public void updateVideo(Video video) {
        videoRepository.save(video);
    }

    @Transactional
    public Video changeVideo(long videoId, VideoDto videoDto) throws IOException {
        Video video = findById(videoId);
        video.setType(videoDto.getType());
        video.setName(videoDto.getName());
        return videoRepository.save(video);
    }

    @Transactional
    public Video findById(long videoId) throws IOException {
        return videoRepository.findById(videoId).orElseThrow(()->
                new IOException("Video with the same id doesn't exist"));
    }

    @Transactional
    public void deleteVideo(long videoId) throws IOException {
        Video video = findById(videoId);
        AnimePage animePage = animePageRepository.findById(video.getAnimePage().getId());
        List<Video> animePageVideos = animePage.getVideoList();
        animePageVideos.remove(video);

        List<NotificationsFromAnime> notificationsFromAnimeVideoList = notificationsFromAnimeRepository.findAllByVideo(video);
        List<Profile> profiles = animePage.getProfiles();
        List<NotificationsFromAnime> animeList = animePage.getNotifications();

        if (notificationsFromAnimeVideoList.size() > 0) {
            video.setNotifications(null);
            animeList.removeAll(notificationsFromAnimeVideoList);
            for (Profile profile: profiles) {
                List<NotificationsFromAnime> profileList = profile.getNotificationsFromAnimeList();
                profileList.removeAll(notificationsFromAnimeVideoList);
                profile.setNotificationsFromAnimeList(profileList);
                profileRepository.save(profile);
            }
            animePage.setNotifications(animeList);
            notificationsFromAnimeRepository.deleteAll(notificationsFromAnimeVideoList);
            if (notificationsFromAnimeRepository.findAllByVideo(video).size() > 0) {
                throw new IOException("Notifications hasn't been deleted");
            }
        }
        animePageRepository.save(animePage);
        videoRepository.save(video);
        videoRepository.deleteById(videoId);
        if (videoRepository.findById(videoId).isPresent()) {
            throw new IOException("Video hasn't been deleted");
        }
    }
}
