package com.naso.restapi.service;

import com.naso.restapi.dto.LibrariesDto;
import com.naso.restapi.dto.ShikimoriDto;
import com.naso.restapi.model.Profile;
import com.naso.restapi.model.Roles;
import com.naso.restapi.model.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class ShikimoriService {
    @Value(value = "${shikimori.redirect-uri}")
    private String redirectUri;
    @Value(value = "${shikimori.client-id}")
    private String clientId;
    @Value(value = "${shikimori.client-secret}")
    private String clientSecret;
    @Value(value = "${link.shikimori}")
    private String link;
    private final RestTemplate restTemplate;
    private final ProfileService profileService;
    private final RoleService roleService;

    @Autowired
    public ShikimoriService(RestTemplate restTemplate, ProfileService profileService, RoleService roleService) {
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

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        String result = restTemplate.postForObject(link + "/oauth/token", request , String.class);

        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        Roles role = roleService.findByName("SHIKIMORI");
        if (!profile.getRoles().contains(role)) {
            profile.addRole(role);
        }
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

        return restTemplate.postForObject(link + "/oauth/token", request , String.class);
    }

    @Transactional
    public Integer addShikimoriUserId(String token, String mail) throws Exception {
        String url = link + "/api/users/whoami?access_token=" + token;
        String data = restTemplate.getForObject(url, String.class);
        JsonObject object = JsonParser.parseString(Objects.requireNonNull(data)).getAsJsonObject();

        int idShiki = Integer.parseInt(String.valueOf(object.get("id")));

        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        profile.setIdShikimori(idShiki);
        profile = profileService.updateProfile(profile);
        return profile.getIdShikimori();
    }

    @Transactional
    public String increaseSeries(String mail, LibrariesDto librariesDto) throws Exception {
        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        int episodes = librariesDto.getEpisodesOrScore();
        int shikiId = profile.getIdShikimori();
        if (shikiId == 0) {
            throw new Exception("Shikimori's id can't be 0 after authorization");
        }

        String userInfoAboutAnime = link + "/api/v2/user_rates/?user_id=" + shikiId +
                "&target_id=" + librariesDto.getAnimeId() + "&target_type=Anime";

        String data = restTemplate.getForObject(userInfoAboutAnime, String.class);

        String animeInformation = restTemplate.getForObject(link + "/api/animes/" + librariesDto.getAnimeId(), String.class);
        JsonObject objectAnimeInformation = JsonParser.parseString(Objects.requireNonNull(animeInformation)).getAsJsonObject();
        int animeEpisodes = Integer.parseInt(String.valueOf(objectAnimeInformation.get("episodes")));

        if (episodes > animeEpisodes) {
            throw new Exception("Transferred episodes must be less");
        }
        String result;
        if (Objects.equals(data, "[]")) {
            ShikimoriDto shikimoriDto = new ShikimoriDto(shikiId, librariesDto.getAnimeId(), "Anime", episodes);
            if (episodes == animeEpisodes) {
                shikimoriDto.setStatus("completed");
            } else {
                shikimoriDto.setStatus("watching");
            }
            result = restTemplate.postForObject(link + "/api/v2/user_rates?access_token=" + librariesDto.getToken(),
                    shikimoriDto, String.class);
        } else {
            JsonArray objectUserInfoAboutAnime = JsonParser.parseString(data).getAsJsonArray();
            int usersEpisodes = Integer.parseInt(String.valueOf(objectUserInfoAboutAnime.get(0).getAsJsonObject().get("episodes")));
            int id = Integer.parseInt(String.valueOf(objectUserInfoAboutAnime.get(0).getAsJsonObject().get("id")));
            int rewatches = Integer.parseInt(String.valueOf(objectUserInfoAboutAnime.get(0).getAsJsonObject().get("rewatches")));
            String usersStatus = objectUserInfoAboutAnime.get(0).getAsJsonObject().get("status").toString().replaceAll("\"", "");

            if (episodes == usersEpisodes) {
                throw new Exception("Series must be not equaled");
            } else if (episodes < usersEpisodes &
                    (usersStatus.equals("watching") || usersStatus.equals("rewatching")
                            || usersStatus.equals("planned"))) {
                throw new Exception("Transferred episodes must be greatly");
            }

            ShikimoriDto shikimoriDto = new ShikimoriDto(shikiId, librariesDto.getAnimeId(), "Anime", episodes);

            if (usersStatus.equals("completed") || usersStatus.equals("rewatching")) {
                shikimoriDto.setStatus("rewatching");
            } else {
                shikimoriDto.setStatus("watching");
            }

            if ((episodes == animeEpisodes) && usersStatus.equals("rewatching")) {
                rewatches++;
                shikimoriDto.setStatus("completed");
            } else if (episodes == animeEpisodes) {
                shikimoriDto.setStatus("completed");
            }

            shikimoriDto.setRewatches(rewatches);
            result = restTemplate.patchForObject(link + "/api/v2/user_rates/" + id
                    + "?access_token=" + librariesDto.getToken(), shikimoriDto, String.class);
        }
        return result;
    }

    @Transactional
    public String decreaseSeries(String mail, LibrariesDto librariesDto) throws Exception {
        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        int episodes = librariesDto.getEpisodesOrScore();
        int shikiId = profile.getIdShikimori();
        if (shikiId == 0) {
            throw new Exception("Shikimori's id can't be 0 after authorization");
        }

        String animeInformation = restTemplate.getForObject(link + "/api/animes/" + librariesDto.getAnimeId(), String.class);
        JsonObject objectAnimeInformation = JsonParser.parseString(Objects.requireNonNull(animeInformation)).getAsJsonObject();
        int animeEpisodes = Integer.parseInt(String.valueOf(objectAnimeInformation.get("episodes")));

        String userInfoAboutAnime = link + "/api/v2/user_rates/?user_id=" + shikiId +
                "&target_id=" + librariesDto.getAnimeId() + "&target_type=Anime";
        String data = restTemplate.getForObject(userInfoAboutAnime, String.class);

        JsonArray objectUserInfoAboutAnime = JsonParser.parseString(Objects.requireNonNull(data)).getAsJsonArray();
        int usersEpisodes = Integer.parseInt(String.valueOf(objectUserInfoAboutAnime.get(0).getAsJsonObject().get("episodes")));
        int id = Integer.parseInt(String.valueOf(objectUserInfoAboutAnime.get(0).getAsJsonObject().get("id")));
        int rewatches = Integer.parseInt(String.valueOf(objectUserInfoAboutAnime.get(0).getAsJsonObject().get("rewatches")));
        String usersStatus = objectUserInfoAboutAnime.get(0).getAsJsonObject().get("status").toString().replaceAll("\"", "");

        if (episodes > usersEpisodes) {
            throw new Exception("Count of episodes must be less");
        }
        episodes--;
        ShikimoriDto shikimoriDto = new ShikimoriDto(shikiId, librariesDto.getAnimeId(), "Anime");

        if (rewatches != 0) {
            if ((episodes == 0) && (usersEpisodes == animeEpisodes)) {
                shikimoriDto.setStatus("completed");
                rewatches--;
            } else if ((episodes == 0)) {
                shikimoriDto.setStatus("completed");
                rewatches--;
            } else if (usersStatus.equals("completed")) {
                shikimoriDto.setStatus("rewatching");
                rewatches--;
            } /*else if (usersStatus.equals("rewatching")) {
                shikimoriListDto.setStatus("rewatching");
            }*/
        } else if (episodes == 0) {
            shikimoriDto.setStatus("planned");
        } else {
            shikimoriDto.setStatus("watching");
        }
        shikimoriDto.setRewatches(rewatches);

        if ((episodes == 0) && (shikimoriDto.getStatus().equals("completed"))) {
            episodes = animeEpisodes;
        }
        shikimoriDto.setEpisodes(episodes);

        return restTemplate.patchForObject(link + "/api/v2/user_rates/" + id
                + "?access_token=" + librariesDto.getToken(), shikimoriDto, String.class);
    }

    @Transactional
    public String rewatchAnime(String mail, LibrariesDto librariesDto) throws Exception {
        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        int shikiId = profile.getIdShikimori();
        if (shikiId == 0) {
            throw new Exception("Shikimori's id can't be 0 after authorization");
        }

        String userInfoAboutAnime = link + "/api/v2/user_rates/?user_id=" + shikiId +
                "&target_id=" + librariesDto.getAnimeId() + "&target_type=Anime";
        String data = restTemplate.getForObject(userInfoAboutAnime, String.class);

        JsonArray objectUserInfoAboutAnime = JsonParser.parseString(Objects.requireNonNull(data)).getAsJsonArray();
        int id = Integer.parseInt(String.valueOf(objectUserInfoAboutAnime.get(0).getAsJsonObject().get("id")));
        int rewatches = Integer.parseInt(String.valueOf(objectUserInfoAboutAnime.get(0).getAsJsonObject().get("rewatches")));
        String usersStatus = objectUserInfoAboutAnime.get(0).getAsJsonObject().get("status").toString().replaceAll("\"", "");

        if (!usersStatus.equals("completed")) {
            throw new Exception("Status must be completed");
        }
        ShikimoriDto shikimoriDto = new ShikimoriDto(shikiId, librariesDto.getAnimeId() , "Anime", "rewatching", rewatches);
        return restTemplate.patchForObject(link + "/api/v2/user_rates/" + id
                + "?access_token=" + librariesDto.getToken(), shikimoriDto, String.class);
    }

    @Transactional
    public String getUserStatusAboutAnime(String mail, long animeId) throws Exception {
        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        int shikiId = profile.getIdShikimori();
        if (shikiId == 0) {
            throw new Exception("Shikimori's id can't be 0 after authorization");
        }
        String userInfoAboutAnime = link + "/api/v2/user_rates/?user_id=" + shikiId +
                "&target_id=" + animeId + "&target_type=Anime";
        String data = restTemplate.getForObject(userInfoAboutAnime, String.class);
        JsonArray objectUserInfoAboutAnime = JsonParser.parseString(Objects.requireNonNull(data)).getAsJsonArray();
        return objectUserInfoAboutAnime.get(0).getAsJsonObject().get("status").toString().replaceAll("\"", "");
    }

    @Transactional
    public String setScore(String mail, LibrariesDto librariesDto) throws Exception {
        int score = librariesDto.getEpisodesOrScore();
        if (score < 1 || score > 10) {
            throw new Exception("Score must be less than 1 or greater than 10");
        }
        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        int shikiId = profile.getIdShikimori();

        if (shikiId == 0) {
            throw new Exception("Shikimori's id can't be 0 after authorization");
        }
        String animeInformation = restTemplate.getForObject(link + "/api/animes/" + librariesDto.getAnimeId(), String.class);
        JsonObject objectAnimeInformation = JsonParser.parseString(Objects.requireNonNull(animeInformation)).getAsJsonObject();
        int animeEpisodes = Integer.parseInt(String.valueOf(objectAnimeInformation.get("episodes")));

        String userInfoAboutAnime = link + "/api/v2/user_rates/?user_id=" + shikiId +
                "&target_id=" + librariesDto.getAnimeId() + "&target_type=Anime";
        String data = restTemplate.getForObject(userInfoAboutAnime, String.class);

        JsonArray objectUserInfoAboutAnime = JsonParser.parseString(Objects.requireNonNull(data)).getAsJsonArray();
        int id = Integer.parseInt(String.valueOf(objectUserInfoAboutAnime.get(0).getAsJsonObject().get("id")));
        String usersStatus = objectUserInfoAboutAnime.get(0).getAsJsonObject().get("status").toString().replaceAll("\"", "");
        int rewatches = Integer.parseInt(String.valueOf(objectUserInfoAboutAnime.get(0).getAsJsonObject().get("rewatches")));
        int usersEpisodes = Integer.parseInt(String.valueOf(objectUserInfoAboutAnime.get(0).getAsJsonObject().get("episodes")));
        if (!usersStatus.equals("completed")) {
            throw new Exception("Status must be completed");
        }
        if (usersEpisodes != animeEpisodes) {
            throw new Exception("Count of episodes must be equaled");
        }

        ShikimoriDto shikimoriDto = new ShikimoriDto(shikiId, librariesDto.getAnimeId(), "Anime", usersStatus, rewatches);
        shikimoriDto.setEpisodes(usersEpisodes);
        shikimoriDto.setScore(score);

        return restTemplate.patchForObject(link + "/api/v2/user_rates/" + id
                + "?access_token=" + librariesDto.getToken(), shikimoriDto, String.class);
    }

    @Transactional
    public void removeRole(String mail) throws Exception {
        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());

        List<Roles> roles = profile.getRoles();
        Roles role = roleService.findByName("SHIKIMORI");
        roles.remove(role);
        profile.setRoles(roles);

        profileService.updateProfile(profile);
    }
}