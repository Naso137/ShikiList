package com.naso.restapi.repository;

import com.naso.restapi.model.AnimePage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimePageRepository extends JpaRepository<AnimePage, Long> {
    AnimePage findById(long id);
    List<AnimePage> findTop5ByOriginalNameContainingOrRussianNameContainingAndRussianNameAfterOrderByRussianNameAsc
            (String word1, String word2, String prevRussianName);
}
