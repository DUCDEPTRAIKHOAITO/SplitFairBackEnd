package com.anygroup.splitfair.repository;

import com.anygroup.splitfair.enums.UserStatus;
import com.anygroup.splitfair.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Tìm user theo email (đăng nhập)
    Optional<User> findByEmail(String email);

    // Lọc user theo trạng thái
    List<User> findByStatus(UserStatus status);

    // Lọc user theo role
    List<User> findByRole_Name(String roleName);
    @Query("""
        SELECT u FROM User u
        WHERE u.status = com.anygroup.splitfair.enums.UserStatus.ACTIVE
          AND (
                LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    List<User> searchActiveUsers(@Param("keyword") String keyword);

}