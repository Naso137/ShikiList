package com.naso.restapi.service;

import com.naso.restapi.dto.LibrariesDto;
import com.naso.restapi.model.Profile;
import com.naso.restapi.model.Roles;
import com.naso.restapi.model.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class MyAnimeListService {
    @Value(value = "${myanimelist.redirect-uri}")
    private String redirectUri;
    @Value(value = "${myanimelist.client-id}")
    private String clientId;
    @Value(value = "${myanimelist.client-secret}")
    private String clientSecret;
    @Value(value = "${myanimelist.code_verifier}")
    private String codeVerifier;
    @Value(value = "${link.myanimelist}")
    private String link;
    @Value(value = "${link.apimal}")
    private String apiLink;
    private final RestTemplate restTemplate;
    private final ProfileService profileService;
    private final RoleService roleService;

    @Autowired
    public MyAnimeListService(RestTemplate restTemplate, ProfileService profileService, RoleService roleService) {
        this.restTemplate = restTemplate;
        this.profileService = profileService;
        this.roleService = roleService;
    }

    public String authorization(String code, String mail) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("grant_type", "authorization_code");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("code", code);
        map.add("redirect_uri", redirectUri);
        map.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        String result = restTemplate.postForObject(link + "/v1/oauth2/token", request , String.class);

        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        Roles role = roleService.findByName("MYANIMELIST");
        if (!profile.getRoles().contains(role)) {
            profile.addRole(role);
        }
        profile.addRole(role);
        profileService.updateProfile(profile);

        return result;
    }

    public String refreshToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        return restTemplate.postForObject(link + "/v1/oauth2/token", request , String.class);
    }

    public String increaseSeries(LibrariesDto librariesDto) throws Exception {
        String numTimesRewatched = "{num_times_rewatched}";
        String customerAPIUrl = apiLink + "/v2/anime/" + librariesDto.getAnimeId() + "?fields=my_list_status{numTimesRewatched},num_episodes";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(librariesDto.getToken());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
        String response = restTemplate.exchange(customerAPIUrl, HttpMethod.GET, entity, String.class, numTimesRewatched).getBody();

        JsonObject objectAnimeInformation = JsonParser.parseString(Objects.requireNonNull(response)).getAsJsonObject();
        int animeEpisodes = Integer.parseInt(String.valueOf(objectAnimeInformation.get("num_episodes")));

        JsonObject myListStatusObject = (JsonObject) objectAnimeInformation.get("my_list_status");

        if (librariesDto.getEpisodesOrScore() > animeEpisodes) {
            throw new Exception("Transferred episodes must be less");
        }
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        String result;
        if (myListStatusObject == null) {
            map.add("num_watched_episodes", String.valueOf(librariesDto.getEpisodesOrScore()));
            if (librariesDto.getEpisodesOrScore() == animeEpisodes) {
                map.add("status", "completed");
            } else {
                map.add("status", "watching");
            }
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            result = restTemplate.patchForObject(apiLink + "/v2/anime/" + librariesDto.getAnimeId() + "/my_list_status",
                    request, String.class);
        } else {
            int usersEpisodes = Integer.parseInt(String.valueOf(myListStatusObject.get("num_episodes_watched")));
            int rewatches = Integer.parseInt(String.valueOf(myListStatusObject.get("num_times_rewatched")));
            String usersStatus = myListStatusObject.get("status").toString().replaceAll("\"", "");
            boolean isRewatching = Boolean.parseBoolean(String.valueOf(myListStatusObject.get("is_rewatching")));

            if (librariesDto.getEpisodesOrScore() == usersEpisodes) {
                throw new Exception("Series must be not equaled");
            } else if (librariesDto.getEpisodesOrScore() < usersEpisodes &
                    (usersStatus.equals("watching") || isRewatching
                            || usersStatus.equals("planned"))) {
                throw new Exception("Transferred episodes must be greatly");
            }

            map.add("num_watched_episodes", String.valueOf(librariesDto.getEpisodesOrScore()));

            if (usersStatus.equals("completed")) {
                map.add("status", "watching");
                map.add("is_rewatching", "true");
            } else if ((librariesDto.getEpisodesOrScore() == animeEpisodes) && isRewatching) {
                rewatches++;
                map.add("status", "completed");
                map.add("is_rewatching", "false");
            } else if (librariesDto.getEpisodesOrScore() == animeEpisodes) {
                map.add("status", "completed");
            } else {
                map.add("status", "watching");
            }
            map.add("num_times_rewatched", String.valueOf(rewatches));

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            result = restTemplate.patchForObject(apiLink + "/v2/anime/" + librariesDto.getAnimeId() + "/my_list_status",
                    request, String.class);
        }
        return result;
    }

    public String decreaseSeries(LibrariesDto librariesDto) throws Exception {
        String numTimesRewatched = "{num_times_rewatched}";
        String customerAPIUrl = apiLink + "/v2/anime/" + librariesDto.getAnimeId() + "?fields=my_list_status{num},num_episodes";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(librariesDto.getToken());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
        String response = restTemplate.exchange(customerAPIUrl, HttpMethod.GET, entity, String.class, numTimesRewatched).getBody();

        JsonObject objectAnimeInformation = JsonParser.parseString(Objects.requireNonNull(response)).getAsJsonObject();
        int animeEpisodes = Integer.parseInt(String.valueOf(objectAnimeInformation.get("num_episodes")));

        JsonObject myListStatusObject = (JsonObject) objectAnimeInformation.get("my_list_status");

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (myListStatusObject == null) {
            throw new Exception("Status must be not null");
        }
        int usersEpisodes = Integer.parseInt(String.valueOf(myListStatusObject.get("num_episodes_watched")));
        int rewatches = Integer.parseInt(String.valueOf(myListStatusObject.get("num_times_rewatched")));
        String usersStatus = myListStatusObject.get("status").toString().replaceAll("\"", "");

        int episodes = librariesDto.getEpisodesOrScore();

        if (episodes > usersEpisodes) {
            throw new Exception("Transferred episodes must be less");
        }
        episodes--;

        if (rewatches != 0) {
            if ((episodes == 0) && (usersEpisodes == animeEpisodes)) {
                map.add("status", "completed");
                map.add("is_rewatching", "false");
                rewatches--;
            } else if (episodes == 0) {
                map.add("status", "completed");
                map.add("is_rewatching", "false");
            } else if (usersStatus.equals("completed")) {
                map.add("status", "watching");
                if (rewatches == 1) {
                    map.add("is_rewatching", "false");
                } else {
                    map.add("is_rewatching", "true");
                }
                rewatches--;
            }
        } else if (episodes == 0) {
            map.add("status", "plan_to_watch");
        } else {
            map.add("status", "watching");
        }
        map.add("num_times_rewatched", String.valueOf(rewatches));

        if ((episodes == 0) && (Objects.equals(map.getFirst("status"), "completed"))) {
            episodes = animeEpisodes;
        }
        map.add("num_watched_episodes", String.valueOf(episodes));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        return restTemplate.patchForObject(apiLink + "/v2/anime/" + librariesDto.getAnimeId() + "/my_list_status",
                request, String.class);
    }

    public String rewatchAnime(LibrariesDto librariesDto) throws Exception {
        String customerAPIUrl = apiLink + "/v2/anime/" + librariesDto.getAnimeId() + "?fields=my_list_status,num_episodes";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(librariesDto.getToken());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
        String response = restTemplate.exchange(customerAPIUrl, HttpMethod.GET, entity, String.class).getBody();

        JsonObject objectAnimeInformation = JsonParser.parseString(Objects.requireNonNull(response)).getAsJsonObject();
        JsonObject myListStatusObject = (JsonObject) objectAnimeInformation.get("my_list_status");
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        if (myListStatusObject == null) {
            throw new Exception("Status must be not null");
        }

        String usersStatus = myListStatusObject.get("status").toString().replaceAll("\"", "");

        if (!usersStatus.equals("completed")) {
            throw new Exception("Status must be completed");
        }

        map.add("status", "watching");
        map.add("is_rewatching", "true");
        map.add("num_watched_episodes", String.valueOf(0));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        return restTemplate.patchForObject(apiLink + "/v2/anime/" + librariesDto.getAnimeId() + "/my_list_status",
                request, String.class);
    }

    public String getUserStatusAboutAnime(long animeId, String token) throws Exception {
        String customerAPIUrl = apiLink + "/v2/anime/" + animeId + "?fields=my_list_status,num_episodes";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
        String response = restTemplate.exchange(customerAPIUrl, HttpMethod.GET, entity, String.class).getBody();

        JsonObject objectAnimeInformation = JsonParser.parseString(Objects.requireNonNull(response)).getAsJsonObject();
        JsonObject myListStatusObject = (JsonObject) objectAnimeInformation.get("my_list_status");

        if (myListStatusObject == null) {
            throw new Exception("Status must be not null");
        }
        return myListStatusObject.get("status").toString().replaceAll("\"", "");
    }

    public String setScore(LibrariesDto librariesDto) throws Exception {
        if (librariesDto.getEpisodesOrScore() < 1 || librariesDto.getEpisodesOrScore() > 10) {
            throw new Exception("Score must be less than 1 or greater than 10");
        }

        String customerAPIUrl = apiLink + "/v2/anime/" + librariesDto.getAnimeId() + "?fields=my_list_status,num_episodes";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(librariesDto.getToken());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
        String response = restTemplate.exchange(customerAPIUrl, HttpMethod.GET, entity, String.class).getBody();

        JsonObject objectAnimeInformation = JsonParser.parseString(Objects.requireNonNull(response)).getAsJsonObject();
        JsonObject myListStatusObject = (JsonObject) objectAnimeInformation.get("my_list_status");
        int animeEpisodes = Integer.parseInt(String.valueOf(objectAnimeInformation.get("num_episodes")));
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        if (myListStatusObject == null) {
            throw new Exception("Status must be not null");
        }

        String usersStatus = myListStatusObject.get("status").toString().replaceAll("\"", "");
        int usersEpisodes = Integer.parseInt(String.valueOf(myListStatusObject.get("num_episodes_watched")));

        if (!usersStatus.equals("completed")) {
            throw new Exception("Status must be completed");
        }
        if (usersEpisodes != animeEpisodes) {
            throw new Exception("Count of episodes must be equaled");
        }

        map.add("score", String.valueOf(librariesDto.getEpisodesOrScore()));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.patchForObject(apiLink + "/v2/anime/" + librariesDto.getAnimeId() + "/my_list_status",
                request, String.class);
    }
    @Transactional
    public void removeRole(String mail) throws Exception {
        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());

        List<Roles> roles = profile.getRoles();
        Roles role = roleService.findByName("MYANIMELIST");
        roles.remove(role);
        profile.setRoles(roles);

        profileService.updateProfile(profile);
    }
}