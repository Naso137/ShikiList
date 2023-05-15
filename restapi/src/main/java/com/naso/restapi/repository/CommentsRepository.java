package com.naso.restapi.repository;

import com.naso.restapi.model.AnimePage;
import com.naso.restapi.model.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {
    List<Comments> findTop5ByAnimePageAndDateAfterOrderByDateAsc(AnimePage animePage, Timestamp date);
}
