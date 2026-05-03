package com.murdergame.team.repository;

import com.murdergame.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByTeamNo(String teamNo);
    boolean existsByTeamNo(String teamNo);

}