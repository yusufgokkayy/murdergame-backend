package com.murdergame.team.controller;

import com.murdergame.team.dto.*;
import com.murdergame.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    // ============ ADMIN ENDPOINTS ============

    // Admin: Takım oluştur
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(@RequestBody CreateTeamRequest request) {
        return teamService.createTeam(request);
    }

    // Admin: Tüm takımları getir
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TeamResponse> getAllTeams() {
        return teamService.getAllTeams();
    }

    // Admin: User'ı takıma ekle (userId ile)
    @PostMapping("/admin/add-user/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public AddUserResponse addUserToTeam(
            @PathVariable Long teamId,
            @RequestBody AddUserToTeamRequest request) {
        return teamService.addUserToTeam(teamId, request.userId());
    }

    // Admin: User'ı takımdan çıkar
    @PostMapping("/admin/remove-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public AddUserResponse removeUserFromTeam(@PathVariable Long userId) {
        return teamService.removeUserFromTeam(userId);
    }

    // ============ USER ENDPOINTS ============

    // User: Kendisi takıma katılır (token ile)
    @PostMapping("/join")
    @PreAuthorize("hasRole('USER')")
    public AddUserResponse joinTeam(
            @RequestBody JoinTeamRequest request,
            Authentication authentication) {

        Long userId = extractUserIdFromToken(authentication);
        return teamService.userJoinTeam(userId, request);
    }

    // Token'dan userId çıkart (Subject: "USER:5" → 5)
    private Long extractUserIdFromToken(Authentication authentication) {
        String subject = authentication.getName();
        return Long.parseLong(subject.split(":")[1]);
    }
}