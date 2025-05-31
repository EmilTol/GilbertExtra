package com.example.gilbertextra.application;


import com.example.gilbertextra.entity.User;
import com.example.gilbertextra.exception.LoginFailedException;
import com.example.gilbertextra.infrastructure.UserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) { // Tjekker at brugeren findes og bekræfter kodeord med bcrypt
            return user;
        }
        throw new LoginFailedException(email);
    }

    public boolean register(User user, String confirmPassword) {
        if (!user.getPassword().equals(confirmPassword)) { // Tejkker vores bekæft om de 2 kodeord stemmer
            return false;
        }
        if (userRepository.findByEmail(user.getEmail()) != null) { // Tjekker om bruger allerede findes
            return false;
        }
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()); // crypt / hash af passowrd
        user.setPassword(hashedPassword);

        //sætter username til være, det generet username, sender firstName med
        String username = generateUsername(user.getFirstName());
        //setter username som det oprettet
        user.setUsername(username);

        return userRepository.registerUser(user);
    }

    private String generateUsername(String firstName) {
        String username;
        do {
            String numbers = String.valueOf((int) (Math.random() * 9000) + 1000); //generer 4 random tal
            username = firstName.toLowerCase() + numbers;
        } while (userRepository.usernameExists(username)); //opretter username indtil et generet der ikke findes
        return username;
    }
}
