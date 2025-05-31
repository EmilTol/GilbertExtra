package com.example.gilbertextra.application;


import com.example.gilbertextra.entity.Listing;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SortingService {

    public enum Direction { ASC, DESC }

    public List<Listing> byPrice(List<Listing> listings, Direction direction) {
        List<Listing> sortedListings = new ArrayList<>(listings); // Ny liste til at sortere
        sortedListings.sort(Comparator.comparing(Listing::getPrice)); // Sortere efter pris
        if (direction == Direction.DESC) {
            sortedListings.sort(Comparator.comparing(Listing::getPrice).reversed()); // reverser sorteringen
        }
        return sortedListings;
    }

    public List<Listing> byDate(List<Listing> listings, Direction direction) {
        List<Listing> sortedListings = new ArrayList<>(listings); // Laver en ny liste til at sortere
        sortedListings.sort(Comparator.comparing(Listing::getCreatedAt)); //Sortere efter oprettelses dato
        if (direction == Direction.DESC) {
            sortedListings.sort(Comparator.comparing(Listing::getCreatedAt).reversed()); //bruger reversed til at gøre så det er ældste posts
        }
        return sortedListings;
    }
}

