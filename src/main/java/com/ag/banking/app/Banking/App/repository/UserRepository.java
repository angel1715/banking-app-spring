package com.ag.banking.app.Banking.App.repository;

import com.ag.banking.app.Banking.App.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByCardNumber(String cardNumber);

    /* Ese método revisa en la base de datos si existe al menos un
       registro cuya columna email coincida con el valor que pasas como parámetro.*/
    boolean existsByEmail(String email);

}
