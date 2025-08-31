package com.ag.banking.app.Banking.App.userService;

import com.ag.banking.app.Banking.App.domain.User;
import com.ag.banking.app.Banking.App.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * Implementación del servicio de usuarios.
 * Se encarga de la gestión de usuarios y la generación de datos bancarios
 * asegurando unicidad en número de cuenta y tarjeta.
 */
@Service
public class UserServiceIm implements UserServiceI {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    private final Random random = new Random();

    /**
     * Lista todos los usuarios de la base de datos.
     */
    @Override
    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Crea un nuevo usuario con:
     * - Contraseña encriptada
     * - Número de cuenta único
     * - Número de tarjeta único
     * - CVV de 3 dígitos
     * - Fecha de expiración (+5 años)
     *
     * @param user usuario con la información básica
     * @return usuario guardado en la BD
     */
    @Override
    public ResponseEntity<?> newUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body("Email is already in use");
        }
        // Encrypt the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Genera número de cuenta único
        String accountNumber = generateUniqueAccountNumber();
        user.setAccountNumber(accountNumber);

        // Genera número de tarjeta único
        String cardNumber = generateUniqueCardNumber();
        user.setCardNumber(cardNumber);

        // Genera CVV de 3 dígitos
        String cvv = String.format("%03d", random.nextInt(1000)); // 000 - 999
        user.setCardVerificationValue(cvv);

        // Fecha de expiración (mes/año)
        LocalDate expirationDate = LocalDate.now().plusYears(5);
        String expirationMonth = expirationDate.format(DateTimeFormatter.ofPattern("MM"));
        String expirationYear = expirationDate.format(DateTimeFormatter.ofPattern("yyyy"));
        user.setExpirationMonth(expirationMonth);
        user.setExpirationYear(expirationYear);

        // Saldos iniciales
        user.setBalance(500);
        user.setCardBalance(0);
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    /**
     * Genera un número de cuenta único (9 dígitos).
     * Valida que no exista en la base de datos.
     */
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.valueOf(100000000 + random.nextInt(900000000)); // 9 dígitos
        } while (userRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    /**
     * Genera un número de tarjeta único (16 dígitos).
     * Valida que no exista en la base de datos.
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

    @Override
    public User findUserById(Long Id) {
        return userRepository.findById(Id).orElse(null);
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    public void updateUserBalance(Long userId, Integer newBalance) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setBalance(newBalance);
        userRepository.save(existingUser);
    }

    @Override
    public void withdrawMoney(Long userId, Integer newBalance, Integer newCardBalance) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setBalance(newBalance);
        existingUser.setCardBalance(newCardBalance);
        userRepository.save(existingUser);
    }

    @Override
    public void depositMoney(Long userId, Integer newBalance, Integer newCardBalance) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setBalance(newBalance);
        existingUser.setCardBalance(newCardBalance);
        userRepository.save(existingUser);
    }
}