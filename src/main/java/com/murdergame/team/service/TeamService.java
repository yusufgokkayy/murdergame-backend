package com.murdergame.team.service;

import com.murdergame.team.dto.CreateTeamRequest;
import com.murdergame.team.dto.TeamResponse;
import com.murdergame.team.dto.AddUserResponse;
import com.murdergame.team.dto.JoinTeamRequest;
import java.util.List;

public interface TeamService {
    TeamResponse createTeam(CreateTeamRequest request);
    List<TeamResponse> getAllTeams();

    // User kendisi takıma giriyor (token ile)
    AddUserResponse userJoinTeam(Long userId, JoinTeamRequest request);

    // Admin user ekliyor (token gereksiz)
    AddUserResponse addUserToTeam(Long teamId, Long userId);

    // Admin user çıkarıyor
    AddUserResponse removeUserFromTeam(Long userId);

    void deleteTeam(Long teamId);
}