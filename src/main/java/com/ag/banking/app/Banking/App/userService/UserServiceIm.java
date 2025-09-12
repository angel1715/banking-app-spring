package com.ag.banking.app.Banking.App.userService;

import com.ag.banking.app.Banking.App.domain.User;
import com.ag.banking.app.Banking.App.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * User service implementation.
 * Responsible for handling user-related operations such as:
 * - Creating new users with encrypted credentials
 * - Generating unique account and card numbers
 * - Managing balances (deposit, withdraw, transfer)
 */
@Service
public class UserServiceIm implements UserServiceI {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    private final Random random = new Random();

    /**
     * Retrieves all users from the database.
     *
     * @return List of users
     */
    @Override
    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Creates a new user with:
     * - Encrypted password
     * - Unique account number
     * - Unique card number
     * - Random CVV
     * - Expiration date (valid for 5 years)
     * - Initial balance
     *
     * @param user User object with basic info (email, password, etc.)
     * @return ResponseEntity containing the saved user or an error message
     */
    @Override
    public ResponseEntity<?> newUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body("Email is already in use");
        }
        if (userRepository.existsByPhoneNumber(user.getPhone())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body("Phone number is already in use");
        }
        // Encrypt the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Generate unique account number
        String accountNumber = generateUniqueAccountNumber();
        user.setAccountNumber(accountNumber);

        // Generate unique card number
        String cardNumber = generateUniqueCardNumber();
        user.setCardNumber(cardNumber);

        // Generate CVV (3 digits)
        String cvv = String.format("%03d", random.nextInt(1000)); // 000 - 999
        user.setCardVerificationValue(cvv);

        // Set expiration date (MM/YYYY format, +5 years)
        LocalDate expirationDate = LocalDate.now().plusYears(5);
        String expirationMonth = expirationDate.format(DateTimeFormatter.
                ofPattern("MM"));
        String expirationYear = expirationDate.format(DateTimeFormatter.
                ofPattern("yyyy"));
        user.setExpirationMonth(expirationMonth);
        user.setExpirationYear(expirationYear);

        // Initial balances
        user.setBalance(500);
        user.setCardBalance(0);

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    /**
     * Generates a unique 9-digit account number.
     *
     * @return account number
     */
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.valueOf(100000000 +
                    random.nextInt(900000000)); // 9 dígitos
        } while (userRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    /**
     * Generates a unique 16-digit card number.
     *
     * @return card number
     */
    private String generateUniqueCardNumber() {
        String cardNumber;
        do {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                builder.append(random.nextInt(10));
            }
            cardNumber = builder.toString();
        } while (userRepository.existsByCardNumber(cardNumber));
        return cardNumber;
    }

    /**
     * Finds a user by ID.
     *
     * @param Id User ID
     * @return User object or null if not found
     */
    @Override
    public User findUserById(Long Id) {
        return userRepository.
                findById(Id).orElse(null);
    }

    /**
     * Deletes a user from the database.
     *
     * @param user User to delete
     */
    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    /**
     * Updates the balance of a user.
     *
     * @param userId     User ID
     * @param newBalance New balance value
     */
    @Override
    public void updateUserBalance(Long userId, Integer newBalance) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setBalance(newBalance);
        userRepository.save(existingUser);
    }

    /**
     * Withdraws money by updating user balance and card balance.
     *
     * @param userId         User ID
     * @param newBalance     Updated account balance
     * @param newCardBalance Updated card balance
     */
    @Override
    public void withdrawMoney(Long userId, Integer newBalance, Integer newCardBalance) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setBalance(newBalance);
        existingUser.setCardBalance(newCardBalance);
        userRepository.save(existingUser);
    }

    /**
     * Deposits money by updating user balance and card balance.
     *
     * @param userId         User ID
     * @param newBalance     Updated account balance
     * @param newCardBalance Updated card balance
     */
    @Override
    public void depositMoney(Long userId, Integer newBalance, Integer newCardBalance) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setBalance(newBalance);
        existingUser.setCardBalance(newCardBalance);
        userRepository.save(existingUser);
    }
}