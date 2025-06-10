package com.example.gilbertextra.application;

import com.example.gilbertextra.entity.Category;
import com.example.gilbertextra.entity.Listing;
import com.example.gilbertextra.entity.Size;
import com.example.gilbertextra.entity.User;
import com.example.gilbertextra.infrastructure.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final SortingService sortingService;

    @Autowired
    public ListingService(ListingRepository listingRepository, SortingService sortingService) {
        this.listingRepository = listingRepository;
        this.sortingService = sortingService;
    }

    //opretter ny listing
    public void createListing(Listing ad) {
        //tjekker at input for model ikke er null/empty
        if (ad.getModel() == null || ad.getModel().isEmpty()) {
            throw new IllegalArgumentException("Model is required");
        }
        if (ad.getBrand() == null || ad.getBrand().isEmpty()) {
            throw new IllegalArgumentException("Brand is required");
        }

        //gemmer input og opretter ny listing
        listingRepository.save(ad);
    }

    //henter alle listings fra en bruger med status approved
    public List<Listing> getApprovedListingsForUser(Long userId) {
        return listingRepository.findListingsBySellerIdAndStatus(userId, "APPROVED");
    }

    //henter alle listings fra en bruger med status sold
    public List<Listing> getSoldListingsForUser(Long userId) {
        return listingRepository.findListingsBySellerIdAndStatus(userId, "SOLD");
    }

    //henter alle listings fra en bruger med status pending
    public List<Listing> getPendingListingsForUser(Long userId) {
        return listingRepository.findListingsBySellerIdAndStatus(userId, "PENDING");
    }

    public Listing getListingById(Long id) {
        return listingRepository.findListingById(id);
    }

    //henter alle categorier til brug i oprettelse af ny listing
    public List<Category> getAllCategories() {
        return listingRepository.findAllCategoriesFlat();
    }

    //henter alle størrelser til brug i oprettelse af ny listing
    public List<Size> getAllSizes() {
        return listingRepository.findAllSizes();
    }

    //henter alle Listings med status pending
    public List<Listing> getPendingListings() {
        return listingRepository.findByStatus(Listing.Status.PENDING);
    }

    //til godkendelse af listings med status pending
    public void approveListingById(Long listingId, User currentUser) {
        //tjekker om den logget ind bruger er admin
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new SecurityException("Only admins can approve listings.");
        }
        Listing listing = listingRepository.findListingById(listingId);

        //tjekker at en listing er valgt og har statuspending
        if (listing != null && listing.getStatus() == Listing.Status.PENDING) {

            //listing status bliver updateret til approved
            listing.setStatus(Listing.Status.APPROVED);
            listingRepository.update(listing);
        }
    }

    //til afvisning af listings med status pending
    public void denyListingById(Long listingId, User currentUser) {
        //tjekker om den logget ind bruger er admin
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new SecurityException("Only admins can deny listings.");
        }

        Listing listing = listingRepository.findListingById(listingId);

        //tjekker at en listing er valgt og har statuspending
        if (listing != null && listing.getStatus() == Listing.Status.PENDING) {

            //listing status bliver updateret til removed
            listing.setStatus(Listing.Status.REMOVED);
            listingRepository.update(listing);
        }
    }

    public List<Listing> searchListings(String query) { // Søg efter lister baseret på en query

        List<Listing> list; // Midlertidig liste til data som ikke er sorteret ( rå data )

        if (query == null || query.trim().isEmpty()) { // Tjek om query er null eller tom
            list = listingRepository.findAllListings(); // Hent alle lister, hvis ingen query
        } else {
            String like = "%" + query.trim().toLowerCase() + "%"; // Forbered LIKE-mønster f.eks &Gucci%
            list = listingRepository.findByLikePattern(like); // Søg med LIKE-mønster i model, brand eller description
        }

        return list;
    }

    public List<Listing> getListingsByCategory(Long categoryId) {
        List<Listing> list = listingRepository.findByCategoryId(categoryId);
        return list;
    }

    // Ny metode til forsiden, viser kun godkendte listings baseret på søgning eller kategori
    public List<Listing> getApprovedListings(String query, Long categoryId) {
        List<Listing> raw;

        if (categoryId != null) {
            raw = getListingsByCategory(categoryId);
        } else {
            raw = searchListings(query);
        }

        // Filtrer så kun APPROVED listings vises
        return raw.stream()
                .filter(listing -> listing.getStatus() == Listing.Status.APPROVED)
                .collect(Collectors.toList());
    }

    public List<Listing> getActiveListingsByUserId(Long userId) {
        return listingRepository.findByUserIdAndStatus(userId, Listing.Status.APPROVED);
    }

    public List<Listing> getSoldListingsByUserId(Long userId) {
        return listingRepository.findByUserIdAndStatus(userId, Listing.Status.SOLD);
    }

    public void deleteListing(Long listingId, Long userId) {
        Listing listing = listingRepository.findListingById(listingId);
        if (listing == null || !listing.getSellerId().equals(userId)) {
            throw new SecurityException("User not allowed to delete this listing");
        }
        listingRepository.deleteById(listingId);
    }

}