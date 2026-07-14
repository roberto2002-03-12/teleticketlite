package com.app.teleticket.users.repository;

import com.app.teleticket.users.entity.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserEntity> {

    public Optional<UserEntity> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<UserEntity> findByDni(String dni) {
        return find("dni", dni).firstResultOptional();
    }

    public Optional<UserEntity> findByPhoneNumber(String phoneNumber) {
        return find("phoneNumber", phoneNumber).firstResultOptional();
    }

    public Optional<Integer> findByEmailAndReturnOnlyUsersId(String email) {
        return getEntityManager()
                .createQuery("SELECT u.id FROM UserEntity u WHERE u.email = :email", Integer.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public boolean existsByDni(String dni) {
        return count("dni", dni) > 0;
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return count("phoneNumber", phoneNumber) > 0;
    }
}
