package com.ag.banking.app.Banking.App.userService;

import com.ag.banking.app.Banking.App.domain.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserServiceI {

    //This method returns all users
    public List<User> listAllUsers();

    //This method creates a new user
    public ResponseEntity<?> newUser(User user);


    //This method finds a specific user by his Id and returns it
    public User findUserById(Long Id);

    //This method deletes an existing user from the data-base
    public void deleteUser(User user);

    //This is the mothod used to send money to other users
    public void updateUserBalance(Long userId, Integer newBalance);

    //This method updates the user balance when the user makes a withdrawal
      public void withdrawMoney(Long userId, Integer newBalance, Integer newCardBalance);

    //This method updates the user balance when the user makes a withdrawal
    public void depositMoney(Long userId, Integer newBalance, Integer newCardBalance);

}
