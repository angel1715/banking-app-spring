package com.ag.banking.app.Banking.App.restController;

import com.ag.banking.app.Banking.App.domain.User;
import com.ag.banking.app.Banking.App.userService.UserServiceI;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.*;

/**
 * REST controller that handles user management and banking operations.
 */
@RestController
@RequestMapping("/banking")
@CrossOrigin(value = "http://localhost:5173/")
public class UserRestController {

    @Autowired
    private UserServiceI userService;

    /**
     * Secret key used for JWT generation
     */
    private static final String SECRET_KEY = "lguguigighoihoigiugucfkjhgihoihbpoh";

    /**
     * Retrieves all users from the database.
     *
     * @return list of users
     */
    @GetMapping("/listAllUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.listAllUsers());
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id User identifier
     * @return User data or 404 if not found
     */
    @GetMapping("/findById/{id}")
    public ResponseEntity<?> findUserById(@PathVariable Long id) {
        User userFound = userService.findUserById(id);
        return (userFound != null) ?
                ResponseEntity.ok(userFound) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    /**
     * Creates a new user in the database.
     *
     * @param user User object to register
     * @return Created user
     */
    @PreAuthorize("permitAll()")
    @PostMapping("/saveNewUser")
    public ResponseEntity<?> saveNewUser(@RequestBody User user) {
        return userService.newUser(user);
    }

    /**
     * Login endpoint that validates user credentials and generates a JWT.
     *
     * @param email    User's email
     * @param password User's password
     * @return JWT token and authenticated user data, or 401 if invalid credentials
     */
    @PostMapping("/login/{email}/{password}")
    public ResponseEntity<?> login(@PathVariable String email,
                                   @PathVariable String password) {

        List<User> userDB = userService.listAllUsers();

        for (User u : userDB) {
            if (u.getEmail().equals(email) &&
                    new BCryptPasswordEncoder().matches(password, u.getPassword())) {

                String token = Jwts.builder()
                        .setSubject(u.getfName())
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                        .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                        .compact();

                u.setPassword(null); // Hide password before returning user
                return ResponseEntity.ok(Map.of("jwt", token, "user", u));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    //Logout endpoint
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {

        request.getSession().invalidate();
        return ResponseEntity.ok().build();
    }

    /**
     * Withdraws funds from a user account.
     *
     * @param cardNumber       Card number
     * @param withdrawalAmount Amount to withdraw
     * @param userId           User ID
     * @return Updated user data or error message
     */
    @PatchMapping("/withdrawFunds/{cardNumber}/{withdrawalAmount}/{userId}")
    public ResponseEntity<?> withdrawFunds(@PathVariable String cardNumber,
                                           @PathVariable Integer withdrawalAmount,
                                           @PathVariable Long userId) {

        User userDB = userService.findUserById(userId);

        if (userDB == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (!cardNumber.equals(userDB.getCardNumber())) {
            return ResponseEntity.badRequest().body("Card not registered for this user");
        }

        if (withdrawalAmount > userDB.getBalance()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body(Map.of("message", "Insufficient balance"));
        }

        userDB.setBalance(userDB.getBalance() - withdrawalAmount);
        userDB.setCardBalance(userDB.getCardBalance() + withdrawalAmount);
        userService.withdrawMoney(userDB.getId(), userDB.getBalance(), userDB.getCardBalance());

        return ResponseEntity.ok(userDB);
    }

    /**
     * Deposits funds into a user account from a registered card.
     *
     * @param cardNumber            Card number
     * @param month                 Expiration month
     * @param year                  Expiration year
     * @param cardVerificationValue CVV
     * @param amount                Amount to deposit
     * @param id                    User ID
     * @return Updated user data or error message
     */
    @PatchMapping("/depositFunds/{cardNumber}/{month}/{year}/{cardVerificationValue}/{amount}/{id}")
    public ResponseEntity<?> depositFunds(@PathVariable String cardNumber,
                                          @PathVariable String month,
                                          @PathVariable String year,
                                          @PathVariable String cardVerificationValue,
                                          @PathVariable Integer amount,
                                          @PathVariable Long id) {

        User userDb = userService.findUserById(id);

        if (userDb == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // here we get true or false if the card information is valid or not
        boolean validCard = userDb.getCardNumber().equals(cardNumber) &&
                userDb.getExpirationMonth().equals(month) &&
                userDb.getExpirationYear().equals(year) &&
                userDb.getCardVerificationValue().equals(cardVerificationValue);

        if (!validCard) {
            return ResponseEntity.status(HttpStatus.
                    BAD_REQUEST).body(Map.of("message", "Invalid card information"));
        }

        if (amount > userDb.getCardBalance()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body(Map.of("message", "Insufficient card balance"));
        }

        userDb.setBalance(userDb.getBalance() + amount);
        userDb.setCardBalance(userDb.getCardBalance() - amount);
        userService.depositMoney(userDb.getId(), userDb.getBalance(), userDb.getCardBalance());

        return ResponseEntity.ok(userDb);
    }

    /**
     * Finds a user by their account number.
     *
     * @param accountNumber Account number
     * @return User data or 404 if not found
     */
    @GetMapping("/findByAccountNumber/{accountNumber}")
    public ResponseEntity<?> findByAccountNumber(@PathVariable String accountNumber) {
        return userService.listAllUsers().stream()
                .filter(u -> accountNumber.equals(u.getAccountNumber()))
                .findFirst()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    /**
     * Finds a user by their card number.
     *
     * @param cardNumber Card number
     * @return User data or 404 if not found
     */
    @GetMapping("/findByCardNumber/{cardNumber}")
    public ResponseEntity<?> findByCardNumber(@PathVariable String cardNumber) {
        return userService.listAllUsers().stream()
                .filter(u -> cardNumber.equals(u.getCardNumber()))
                .findFirst()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    /**
     * Transfers money from one user to another.
     *
     * @param accountNumber Destination account number
     * @param amount        Amount to transfer
     * @param currentUserId ID of the sending user
     * @return Transaction status message
     */
    @PatchMapping("/sendMoney/{accountNumber}/{amount}/{currentUserId}")
    public ResponseEntity<?> sendMoney(@PathVariable String accountNumber,
                                       @PathVariable Integer amount,
                                       @PathVariable Long currentUserId) {

        User currentUser = userService.findUserById(currentUserId);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (accountNumber.equals(currentUser.getAccountNumber())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body(Map.of("message", "You cannot send money to your own account. " +
                            "Use deposit instead."));

        }

        Optional<User> recipientOpt = userService.listAllUsers().stream()
                .filter(u -> accountNumber.equals(u.getAccountNumber()))
                .findFirst();

        if (recipientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid account number");
        }

        User recipient = recipientOpt.get();

        if (amount > currentUser.getBalance()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body(Map.of("message", "Insufficient balance"));
        }

        recipient.setBalance(recipient.getBalance() + amount);
        currentUser.setBalance(currentUser.getBalance() - amount);

        userService.updateUserBalance(currentUser.getId(), currentUser.getBalance());
        userService.updateUserBalance(recipient.getId(), recipient.getBalance());

        return ResponseEntity.ok("Money sent successfully");
    }

}