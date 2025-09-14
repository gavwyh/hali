package com.cs301.crm.repositories;

import com.cs301.crm.dtos.responses.ReadResponseDTO;
import com.cs301.crm.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT new com.cs301.crm.dtos.responses.ReadResponseDTO(u.id, u.firstName, u.lastName, u.email, u.enabled, u.userRole) from UserEntity u")
    List<ReadResponseDTO> findAllUsers();

    @Query("SELECT new com.cs301.crm.dtos.responses.ReadResponseDTO(u.id, u.firstName, u.lastName, u.email, u.enabled, u.userRole) from UserEntity u where u.enabled = true and u.userRole = com.cs301.crm.models.UserRole.AGENT")
    List<ReadResponseDTO> findAllActiveAgents();
}