package com.murdergame.team.service.impl;

import com.murdergame.common.exception.BusinessException;
import com.murdergame.common.exception.InvalidCredentialsException;
import com.murdergame.common.exception.ResourceNotFoundException;
import com.murdergame.common.exception.ValidationException;
import com.murdergame.quiz.repository.QuizAnswerRepository;
import com.murdergame.team.dto.CreateTeamRequest;
import com.murdergame.team.dto.TeamResponse;
import com.murdergame.team.dto.AddUserResponse;
import com.murdergame.team.dto.JoinTeamRequest;
import com.murdergame.team.entity.Team;
import com.murdergame.team.repository.TeamRepository;
import com.murdergame.team.service.TeamService;
import com.murdergame.user.entity.User;
import com.murdergame.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuizAnswerRepository quizAnswerRepository;

    @Override
    public TeamResponse createTeam(CreateTeamRequest request) {
        if (teamRepository.existsByTeamNo(request.teamNo())) {
            throw new RuntimeException("Team already exists with teamNo: " + request.teamNo());
        }

        Team team = new Team();
        team.setTeamNo(request.teamNo());
        team.setTeamPasswordHash(passwordEncoder.encode(request.teamPassword()));
        team.setActive(true);

        Team saved = teamRepository.save(team);
        return toResponse(saved);
    }

    @Override
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AddUserResponse userJoinTeam(Long userId, JoinTeamRequest request) {
        // Token sahibi user'ı al
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // ✅ TOKEN SAHİBİNİN USERNAME'İ KONTROL ET
        if (!user.getUsername().equals(request.username())) {
            throw new ValidationException("Başkasının hesabıyla giriş yapamazsın!");
        }

        if (user.getTeam() != null) {
            throw new BusinessException("Kullanıcı zaten bir takımda");
        }

        Team team = teamRepository.findByTeamNo(request.teamNo())
                .orElseThrow(() -> new ResourceNotFoundException("Takım bulunamadı"));

        if (!passwordEncoder.matches(request.teamPassword(), team.getTeamPasswordHash())) {
            throw new InvalidCredentialsException("Takım şifresi yanlış");
        }

        user.setTeam(team);
        User saved = userRepository.save(user);

        return new AddUserResponse(
                saved.getId(),
                saved.getUsername(),
                team.getId(),
                team.getTeamNo(),
                "Kullanıcı takıma başarıyla katıldı"
        );
    }

    @Override
    public AddUserResponse addUserToTeam(Long teamId, Long userId) {
        // User var mı?
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // User zaten bir takımda mı?
        if (user.getTeam() != null) {
            throw new RuntimeException("Kullanıcı zaten bir takımda");
        }

        // Takım var mı?
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Takım bulunamadı"));

        // Admin user'ı takıma ekle
        user.setTeam(team);
        User saved = userRepository.save(user);

        return new AddUserResponse(
                saved.getId(),
                saved.getUsername(),
                team.getId(),
                team.getTeamNo(),
                "Kullanıcı takıma eklenmiştir (Admin tarafından)"
        );
    }

    @Override
    public AddUserResponse removeUserFromTeam(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (user.getTeam() == null) {
            throw new RuntimeException("Kullanıcı bir takımda değil");
        }

        Long teamId = user.getTeam().getId();
        String teamNo = user.getTeam().getTeamNo();
        user.setTeam(null);
        User saved = userRepository.save(user);

        return new AddUserResponse(
                saved.getId(),
                saved.getUsername(),
                teamId,
                teamNo,
                "Kullanıcı takımdan çıkarıldı"
        );
    }

    // YENİ EKLENEN METOT
    @Override
    public AddUserResponse setSpokesperson(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Takım bulunamadı"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // Kullanıcı bu takımda mı diye kontrol et
        if (user.getTeam() == null || !user.getTeam().getId().equals(teamId)) {
            throw new BusinessException("Kullanıcı bu takımda değil, önce takıma eklemelisiniz.");
        }

        team.setSpokespersonId(userId);
        Team saved = teamRepository.save(team);

        return new AddUserResponse(
                user.getId(),
                user.getUsername(),
                team.getId(),
                team.getTeamNo(),
                "Kullanıcı başarıyla takım sözcüsü olarak atandı."
        );
    }

    @Override
    @Transactional
    public void deleteTeam(Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Takım bulunamadı: " + teamId));

        // 1. users detach et
        userRepository.clearTeam(teamId); // team_id = null yap

        // 2. quiz answers sil (veya detach)
        quizAnswerRepository.deleteByTeamId(teamId);

        // 3. team sil
        teamRepository.delete(team);
    }

    private TeamResponse toResponse(Team team) {
        return new TeamResponse(team.getId(), team.getTeamNo(), team.getActive());
    }
}