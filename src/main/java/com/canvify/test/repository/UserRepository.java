package com.canvify.test.repository;

import com.canvify.test.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByUsernameOrEmailOrMobileNumber(String username, String email, String mobile);
    boolean existsByMobileNumber(String mobileNumber);
    @Query("SELECT u FROM User u ORDER BY u.id DESC LIMIT 1")
    Optional<User> findTopByOrderByIdDesc();
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByMobileNumberAndIdNot(String mobileNumber, Long id);

}
