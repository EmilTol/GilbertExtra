package com.example.gilbertextra.application;

import com.example.gilbertextra.entity.Listing;
import com.example.gilbertextra.entity.User;
import com.example.gilbertextra.infrastructure.UserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, SortingService sortingService) {
        this.userRepository = userRepository;
    }

    public User getUserById (long id) {
        return userRepository.findById(id);
    }

    //updatere en bruger
    public void updateUser(long userId, String email, String password, String firstName, String lastName, String phoneNumber) {
        User user = userRepository.findById(userId);

        //updatere kun hvis der er input
        // sørger for hvis intet indtastet for firstName at det ikke bliver sat som null i db
        if (firstName != null && !firstName.isEmpty()) {
            user.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            user.setLastName(lastName);
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            user.setPhoneNumber(phoneNumber);
        }
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        if (password != null && !password.isEmpty()) {
            //gør at nyt password bliver hashed
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            user.setPassword(hashedPassword);
        }
        //updatere
        userRepository.updateUser(user);
    }

    //boolean tjek om den er en favorite
    public boolean isFavorite(Long userId, Long listingId) {
        return userRepository.IsitUserFavorite(userId, listingId);
    }

    public void toggleFavorite(Long userId, Long listingId) {
        //hvis user har listing som favorite bliver den fjernet
        if (userRepository.IsitUserFavorite(userId, listingId)) {
            userRepository.removeFavorite(userId, listingId);
        }
        else {
            //ellers bliver den tilføjet til favoritter
            userRepository.addFavorite(userId, listingId);
        }
    }

    public List<Listing> getFavoriteListingsForUser(Long userId) {
        return userRepository.findFavoritesByUserId(userId);
    }
    public User getPublicUserById(long userId) {
        return userRepository.findUserById(userId);
    }

    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }
}
