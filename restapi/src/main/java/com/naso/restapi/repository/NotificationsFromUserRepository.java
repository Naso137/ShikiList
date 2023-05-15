package com.naso.restapi.repository;

import com.naso.restapi.model.Comments;
import com.naso.restapi.model.NotificationsFromUser;
import com.naso.restapi.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface NotificationsFromUserRepository extends JpaRepository<NotificationsFromUser, Long> {
    List<NotificationsFromUser> findAllByComment(Comments comments);
    void deleteAllByComment(Comments comments);
    List<NotificationsFromUser> findTop5ByProfileAndDateAfterOrderByDateDesc(Profile profile, Timestamp date);
    List<NotificationsFromUser> findTop5ByProfileAndDateBeforeOrderByDateDesc(Profile profile, Timestamp date);
    int countAllByProfile(Profile profile);
}
