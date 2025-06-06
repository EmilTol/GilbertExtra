package com.example.gilbertextra.infrastructure;

import com.example.gilbertextra.entity.Category;
import com.example.gilbertextra.entity.Listing;
import com.example.gilbertextra.entity.Size;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ListingRepository {

    private final JdbcTemplate jdbcTemplate;

    public ListingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //opretter ny listing ud fra input, giv nyt navn
    public void save(Listing ad) {
        String sql = "INSERT INTO listings (seller_id, category_id, size_id, item_type, model, brand, description, " +
                "conditions, materials, price, max_discount_percent, color, status, is_fair_trade, is_validated, image_file_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                ad.getSellerId(),
                ad.getCategoryId(),
                ad.getSizeId(),
                ad.getItemType(),
                ad.getModel(),
                ad.getBrand(),
                ad.getDescription(),
                ad.getConditions(),
                ad.getMaterials(),
                ad.getPrice(),
                ad.getMaxDiscountPercent(),
                ad.getColor(),
                ad.getStatus().name(),
                false,
                false,
                ad.getImageFileName()
        );
    }

    //henter alle kategorier til dropdown
    public List<Category> findAllCategoriesFlat() {
        String sql = "SELECT * FROM categories";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Category.class));
    }

    //henter alle sizes til dropdown
    public List<Size> findAllSizes() {
        String sql = "SELECT * FROM sizes";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Size.class));
    }


    //henter listings fra specifik bruger ud fra status, brugt i profil
    public List<Listing> findListingsBySellerIdAndStatus(Long sellerId, String status) {
        String sql = """
        SELECT l.*, s.size_label 
        FROM listings l
        LEFT JOIN sizes s ON l.size_id = s.size_id
        WHERE l.seller_id = ? AND l.status = ?
    """;

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Listing.class), sellerId, status);
    }

    public List<Listing> findAllListings() {
        String sql = "SELECT l.listing_id, l.seller_id, l.category_id, l.size_id, l.item_type, l.model, l.brand,\n" +
                "       l.description, l.conditions, l.materials, l.price, l.max_discount_percent, l.created_at,\n" +
                "       l.status, l.is_fair_trade, l.is_validated, l.color, s.size_label, l.image_file_name,\n" +
                "       u.username AS seller_username\n" +
                "FROM listings l\n" +
                "LEFT JOIN sizes s ON l.size_id = s.size_id\n" +
                "JOIN users u ON l.seller_id = u.user_id";

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
            //henter username fra user og sætter det som seller_username i sql'en
            listing.setSellerUsername(rs.getString("seller_username"));
            return listing;
        });
    }

    public Listing findListingById(Long listingId) {
        try {
            String sql = "SELECT l.listing_id, l.seller_id, l.category_id, l.size_id, l.item_type, l.model, l.brand, " +
                    "l.description, l.conditions, l.materials, l.price, l.max_discount_percent, l.created_at, " +
                    "l.status, l.is_fair_trade, l.is_validated, l.color, s.size_label, l.image_file_name " +
                    "FROM listings l LEFT JOIN sizes s ON l.size_id = s.size_id WHERE l.listing_id = ?";

            return jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{listingId},
                    (rs, rowNum) -> {
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
                    }
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    //updatere status på en valgt listing, brugt til godkendelse
    public void update(Listing listing) {
        String sql = "UPDATE listings SET status = ? WHERE listing_id = ?";
        jdbcTemplate.update(sql, listing.getStatus().name(), listing.getListingId());
    }

    //finder ALLE listings baseret på en status, kunne bruges på homepage til approved
    public List<Listing> findByStatus(Listing.Status status) {
        String sql = """
    SELECT l.*, s.size_label 
    FROM listings l
    LEFT JOIN sizes s ON l.size_id = s.size_id
    WHERE l.status = ?
    """;
        return jdbcTemplate.query(sql, new Object[]{status.name()}, (rs, rowNum) -> {
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
        });
    }

    public List<Listing> findByLikePattern(String likePattern) {

        String sql = """
        SELECT l.*, s.size_label
        FROM listings l
        LEFT JOIN sizes s ON l.size_id = s.size_id
        WHERE  LOWER(l.model)       LIKE ?
           OR  LOWER(l.brand)       LIKE ?
           OR  LOWER(l.description) LIKE ?
        """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Listing.class),
                likePattern, likePattern, likePattern
        );
    }

    public List<Listing> findByCategoryId(Long categoryId) {
        String sql = """
            SELECT l.*, s.size_label
              FROM listings l
              LEFT JOIN sizes s ON l.size_id = s.size_id
             WHERE l.category_id = ?
            """;
        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Listing.class),
                categoryId
        );
    }
    //bruges i public profile til finde approved og sold for valgt bruger
    public List<Listing> findByUserIdAndStatus(long userId, Listing.Status status) {
        String sql = """
        SELECT l.*, s.size_label, u.username
        FROM listings l
        LEFT JOIN sizes s ON l.size_id = s.size_id
        JOIN users u ON l.seller_id = u.user_id
        WHERE l.seller_id = ? AND l.status = ?
    """;

        return jdbcTemplate.query(sql, new Object[]{userId, status.name()}, (rs, rowNum) -> {
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
            listing.setSellerUsername(rs.getString("username"));
            return listing;
        });
    }

    public void deleteById(Long listingId) {
        String sql = "DELETE FROM listings WHERE listing_id = ?";
        jdbcTemplate.update(sql, listingId);
    }
}