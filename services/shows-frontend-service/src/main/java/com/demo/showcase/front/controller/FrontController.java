package com.demo.showcase.front.controller;

import com.demo.showcase.common.dto.AddShowRequest;
import com.demo.showcase.common.dto.GetUserShowsResponse;
import com.demo.showcase.common.dto.ShowFrontDto;
import com.demo.showcase.common.dto.ShowShortInfo;
import com.demo.showcase.common.dto.ShowView;
import com.demo.showcase.common.dto.mapper.ShowsDtoMapper;
import com.demo.showcase.common.feign.ShowsDataFeignClient;
import com.demo.showcase.common.feign.UsersShowsFeignClient;
import com.demo.showcase.common.sso.KeycloakUtils;
import com.demo.showcase.common.sso.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class FrontController {

    private final static String BASE_URL = "/front";

    private final ShowsDataFeignClient showsDataFeignClient;

    private final UsersShowsFeignClient usersShowsFeignClient;

    private final ShowsDtoMapper showsDtoMapper;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/")
    public String index(Model model) {
        if (KeycloakUtils.getRole() == Role.ADMIN) {
            List<ShowShortInfo> shows = showsDataFeignClient.getShows(KeycloakUtils.getBearerToken());
            model.addAttribute("shows", shows);
            return "index";
        } else {
            List<ShowShortInfo> shows = showsDataFeignClient.getShows(KeycloakUtils.getBearerToken());
            model.addAttribute("shows", shows);
            return "indexUser";
        }
    }

    @GetMapping(BASE_URL + "/search")
    public String search(Model model, String searchTitle) {
        List<ShowShortInfo> shows = showsDataFeignClient.findShows(KeycloakUtils.getBearerToken(), searchTitle);
        model.addAttribute("shows", shows);
        return "index";
    }

    @GetMapping(BASE_URL + "/{id}")
    public String show(@PathVariable("id") UUID id, Model model) {
        ShowView show = showsDataFeignClient.findShowInfoById(KeycloakUtils.getBearerToken(), id);
        model.addAttribute("show", show);
        return "show";
    }

    @GetMapping(BASE_URL + "/users/add/{id}")
    public String addShowUserPage(@PathVariable("id") UUID id, Model model) {
        ShowView show = showsDataFeignClient.findShowInfoById(KeycloakUtils.getBearerToken(), id);
        model.addAttribute("show", show);
        return "addShowUser";
    }

    @PostMapping(BASE_URL + "/users/add/{id}")
    public String addShowUser(@PathVariable("id") UUID id,
                              @ModelAttribute("request") AddShowRequest addShowRequest,
                              Model model) {
        UUID recordId = usersShowsFeignClient.addShow(KeycloakUtils.getBearerToken(), addShowRequest);
        return "redirect:/";
    }

    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping(BASE_URL + "/myShows")
    public String myShows(Model model) {
        List<GetUserShowsResponse> shows = usersShowsFeignClient.getUserShows(KeycloakUtils.getBearerToken());
        model.addAttribute("shows", shows);
        return "myShows";
    }

    @DeleteMapping(BASE_URL + "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String delete(@PathVariable("id") UUID id, Model model) {
        showsDataFeignClient.deleteShowInfoById(KeycloakUtils.getBearerToken(), id);
        return "redirect:/";
    }

    @GetMapping(BASE_URL + "/pictures/{id}")
    public ResponseEntity<Resource> getPoster(@PathVariable("id") UUID id) {
        ResponseEntity<Resource> poster = showsDataFeignClient.findPosterByShowId(KeycloakUtils.getBearerToken(), id);
        return poster;
    }

    @GetMapping(BASE_URL + "/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String getEditPage(@PathVariable("id") UUID id, Model model) {
        ShowView show = showsDataFeignClient.findShowInfoById(KeycloakUtils.getBearerToken(), id);
        model.addAttribute("show", show);
        return "showEdit";
    }

    @PutMapping(BASE_URL + "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String editShow(@PathVariable("id") UUID id, @ModelAttribute("show") ShowFrontDto show, Model model) {
        showsDataFeignClient.editShowInfoById(KeycloakUtils.getBearerToken(), id, showsDtoMapper.map(show));
        return "redirect:/front/" + id.toString();
    }

    @PostMapping(BASE_URL)
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String addShow(@ModelAttribute("show") ShowFrontDto show, Model model) {
        UUID id = showsDataFeignClient.addShow(KeycloakUtils.getBearerToken(), showsDtoMapper.map(show));
        return "redirect:/front/" + id.toString();
    }

    @GetMapping(BASE_URL + "/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String getCreatePage(Model model) {
        return "addShow";
    }

}
