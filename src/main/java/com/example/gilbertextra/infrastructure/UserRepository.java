package com.example.gilbertextra.infrastructure;

import com.example.gilbertextra.entity.Listing;
import com.example.gilbertextra.entity.User;
import com.example.gilbertextra.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User findByEmail(String email) {
        try {
            String sql = "SELECT user_id, email, password, first_name, last_name, phone_number, role, username FROM users WHERE email = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{email}, (rs, rowNum) -> {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setRole(User.Role.valueOf(rs.getString("role")));
                user.setUsername(rs.getString("username"));
                return user;
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (email, password, first_name, last_name, phone_number, username) VALUES (?, ?, ?, ?, ?, ?)";
        int result = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getUsername());
        return result == 1;
    }

    //ligesom i favs, bruges til at tjek om noget eksister
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    //updatere en bruger
    public void updateUser(User user) {
        try {
            String sql = "UPDATE users SET email = ?, password = ?, first_name = ?, last_name = ?, phone_number = ? WHERE user_id = ?";
            int updatedRows = jdbcTemplate.update(sql,
                    user.getEmail(),
                    user.getPassword(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPhoneNumber(),
                    user.getUserId());
        } catch (DataAccessException dataAccessException) {
            throw new RuntimeException("Error updating user", dataAccessException);
        }
    }

    public User findById(long id) {
        try {
            String sql = "SELECT user_id, email, password, first_name, last_name, phone_number FROM users WHERE user_id = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setPhoneNumber(rs.getString("phone_number"));
                return user;
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException dataAccessException) {
            throw new UserNotFoundException(id); // TEST TEST TES
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, id);
    }

    //finder alle brugere, brug i admin panel, husk tilføj role
    public List<User> findAllUsers() {
        String sql = "SELECT user_id, email, password, first_name, last_name, phone_number FROM users";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setUserId(rs.getLong("user_id"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            user.setPhoneNumber(rs.getString("phone_number"));
            return user;
        });
    }

    //tæller hvor der er user_id og listing_id, for at se om en listing er en favorite for brugeren
    public boolean IsitUserFavorite (Long userId, Long ListindId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND listing_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, ListindId);
        return count != null && count > 0;
    }

    //tilføjer en favorite
    public void addFavorite(Long userId, Long ListindId) {
        String sql = "INSERT INTO favorites (user_id, listing_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, ListindId);
    }

    //sletter en favorite
    public void removeFavorite(Long userId, Long ListindId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND listing_id = ?";
        jdbcTemplate.update(sql, userId, ListindId);
    }

    public List<Listing> findFavoritesByUserId(Long userId) {
        String sql = "SELECT l.listing_id, l.seller_id, l.category_id, l.size_id, l.item_type, l.model, l.brand, " +
                "l.description, l.conditions, l.materials, l.price, l.max_discount_percent, l.created_at, " +
                "l.status, l.is_fair_trade, l.is_validated, l.color, s.size_label, l.image_file_name " +
                "FROM listings l " +
                "JOIN favorites f ON l.listing_id = f.listing_id " +
                "LEFT JOIN sizes s ON l.size_id = s.size_id " +
                "WHERE f.user_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Listing listing = new Listing();
            listing.setListingId(rs.getLong("listing_id"));
            listing.setSellerId(rs.getLong("seller_id"));
            listing.setCategoryId(rs.getLong("category_id"));
            listing.setSizeId(rs.getLong("size_id"));
            listing.setItemType(rs.getString("item_type"));
            listing.setModel(rs.getString("model"));
            listing.setBrand(rs.getString("brand"));
            listing.setDescription(rs.getString("description"));
            listing.setConditions(rs.getString("conditions"));
            listing.setMaterials(rs.getString("materials"));
            listing.setPrice(rs.getBigDecimal("price"));
            listing.setMaxDiscountPercent(rs.getBigDecimal("max_discount_percent"));
            listing.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            listing.setStatus(Listing.Status.valueOf(rs.getString("status")));
            listing.setFairTrade(rs.getBoolean("is_fair_trade"));
            listing.setValidated(rs.getBoolean("is_validated"));
            listing.setColor(rs.getString("color"));
            listing.setSizeLabel(rs.getString("size_label"));
            listing.setImageFileName(rs.getString("image_file_name"));
            return listing;
        }, userId);
    }
    public User findUserById(long userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{userId}, (rs, rowNum) -> {
            User user = new User();
            user.setUserId(rs.getLong("user_id"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            user.setUsername(rs.getString("username"));
            user.setPhoneNumber(rs.getString("phone_number"));
            user.setRole(User.Role.valueOf(rs.getString("role")));
            return user;
        });
    }

}