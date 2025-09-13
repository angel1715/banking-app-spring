package com.ag.banking.app.Banking.App.repository;

import com.ag.banking.app.Banking.App.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByCardNumber(String cardNumber);

    /*
    This method checks the database to see if there is at least
    one record whose email column matches the value you pass as a parameter..*/
    boolean existsByEmail(String email);


}
