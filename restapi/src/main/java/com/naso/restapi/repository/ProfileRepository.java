package com.naso.restapi.repository;

import com.naso.restapi.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findByLogin(String login);
    List<Profile> findTop5ByLoginContainingAndLoginAfter(String login, String prevProfileLogin);

}
