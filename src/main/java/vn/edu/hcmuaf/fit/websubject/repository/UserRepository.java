package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.websubject.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Page<User> findAll(Specification<User> specification, Pageable pageable);

    Optional<User> findById(Integer id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.description = :role AND u.id = :idUser")
    List<User> findAllByRoles(int idUser, String role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
