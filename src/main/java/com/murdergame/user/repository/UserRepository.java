package com.murdergame.user.repository;

import com.murdergame.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByTeamIdAndUsername(Long teamId, String username);
    Optional<User> findByTeamIdAndUsername(Long teamId, String username);
    Optional<User> findByTeamIdAndUsernameAndActiveTrue(Long teamId, String username);
    boolean existsByUsername(String username);  // ← BUNU EKLE
    Optional<User> findByUsername(String username);
}