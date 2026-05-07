package com.murdergame.user.repository;

import com.murdergame.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByTeamIdAndUsername(Long teamId, String username);
    Optional<User> findByTeamIdAndUsername(Long teamId, String username);
    Optional<User> findByTeamIdAndUsernameAndActiveTrue(Long teamId, String username);
    // YENİ EKLENEN METOT
    Optional<User> findByUsernameAndActiveTrue(String username);
    boolean existsByUsername(String username);  // ← BUNU EKLE
    Optional<User> findByUsername(String username);

    int countByTeamId(Long teamId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.team = null WHERE u.team.id = :teamId")
    void clearTeam(@Param("teamId") Long teamId);
}