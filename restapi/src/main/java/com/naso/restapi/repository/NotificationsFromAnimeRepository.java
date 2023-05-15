package com.naso.restapi.repository;

import com.naso.restapi.model.AnimePage;
import com.naso.restapi.model.NotificationsFromAnime;
import com.naso.restapi.model.Profile;
import com.naso.restapi.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface NotificationsFromAnimeRepository extends JpaRepository<NotificationsFromAnime, Long> {
    List<NotificationsFromAnime> findAllByVideo(Video video);
    List<NotificationsFromAnime> findAllByAnimePageAndProfile(AnimePage animePage, Profile profile);
    List<NotificationsFromAnime> findTop5ByProfileAndDateAfterOrderByDateDesc(Profile profile, Timestamp date);
    List<NotificationsFromAnime> findTop5ByProfileAndDateBeforeOrderByDateDesc(Profile profile, Timestamp date);
    int countAllByProfile(Profile profile);
}
