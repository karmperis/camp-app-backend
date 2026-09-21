package com.karmperis.campapp.repository;

import com.karmperis.campapp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Role entities.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a non soft-deleted role by its UUID.
     *
     * @param uuid The UUID of the role to find
     * @return An optional containing the role if found and not soft-deleted
     */
    Optional<Role> findByUuidAndDeletedAtIsNull(UUID uuid);

    /**
     * Finds a non soft-deleted role by its name.
     *
     * @param name The name of the role to find
     * @return An optional containing the role if found and not soft-deleted
     */
    Optional<Role> findByNameAndDeletedAtIsNull(String name);

    /**
     * Finds all non soft-deleted roles, ordered by name in ascending order.
     *
     * @return A list of all non soft-deleted roles, ordered by name in ascending order
     */
    List<Role> findAllByDeletedAtIsNullOrderByNameAsc();

    /**
     * Finds all soft-deleted roles, ordered by name in ascending order.
     *
     * @return A list of all soft-deleted roles, ordered by name in ascending order
     */
    List<Role> findAllByDeletedAtIsNotNullOrderByNameAsc();

    /**
     * Checks whether a role name is already used, including soft-deleted roles.
     *
     * @param name The name of the role to check
     * @return true if the name is already used, false otherwise
     */
    boolean existsByName(String name);
}