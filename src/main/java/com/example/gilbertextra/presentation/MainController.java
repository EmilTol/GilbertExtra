package com.example.gilbertextra.presentation;

import com.example.gilbertextra.application.LoginService;
import com.example.gilbertextra.application.ListingService;
import com.example.gilbertextra.application.SortingService;
import com.example.gilbertextra.application.UserService;
import com.example.gilbertextra.entity.Category;
import com.example.gilbertextra.entity.Listing;
import com.example.gilbertextra.entity.Size;
import com.example.gilbertextra.entity.User;
import com.example.gilbertextra.exception.LoginFailedException;
import com.example.gilbertextra.infrastructure.ListingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MainController {

    private final LoginService loginService;
    private final ListingService listingService;
    private final UserService userService;
    private final ListingRepository listingRepository; //bliver ikke brugt
    private final SortingService sortingService;

    @Autowired
    public MainController(LoginService loginService, ListingService listingService, UserService userService, ListingRepository listingRepository, SortingService sortingService) {
        this.loginService  = loginService;
        this.listingService = listingService;
        this.userService = userService;
        this.listingRepository = listingRepository;
        this.sortingService = sortingService;
    }


    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String showHome(HttpSession session, Model model,
                           @RequestParam(defaultValue = "createdAt") String sortBy,
                           @RequestParam(defaultValue = "desc")      String direction,
                           @RequestParam(name = "search",   required = false) String search,
                           @RequestParam(name = "category", required = false) Long   categoryId) {
        model.addAttribute("currentUser", session.getAttribute("currentUser")); // Tilføjer currentUser til model
        User currentUser = (User) session.getAttribute("currentUser"); // Henter currentUser fra session

        //bruger samme sorting, men bruger dem gennem anden methode som sørge for kun Approved listings vises
        List<Listing> listings = listingService.getApprovedListings(search, categoryId);

        SortingService.Direction sortDirection; // Bestemmer sorterings retning
        if (direction.equalsIgnoreCase("asc")) {
            sortDirection = SortingService.Direction.ASC;
        } else {
            sortDirection = SortingService.Direction.DESC;
        }


        if (sortBy.equalsIgnoreCase("price")) { // Anvend sortering efter pris eller dato
            listings = sortingService.byPrice(listings, sortDirection); // Sortér efter pris
        } else {
            listings = sortingService.byDate(listings, sortDirection); // Sortér efter dato
        }

        model.addAttribute("listings", listings); // Tilføjer listings til model
        model.addAttribute("search", search);  // Tilføjer søgetekst til model
        model.addAttribute("sortBy", sortBy); // Tilføjer sortBy til model
        model.addAttribute("direction", direction); // Tilføjer direction til model
        model.addAttribute("selectedCategory", categoryId);
        //gør det samme som udkommenteret men samlet i en linje
        model.addAttribute("categories",listingService.getAllCategories());

        if (currentUser != null) {
            //hvis brugeren ikke er null, laver vi et map
            Map<Long, Boolean> favoriteMap = new HashMap<>();
            //vi gennemgår listings på homepage
            for (Listing listing : listings) {
                boolean isFavorite = userService.isFavorite(currentUser.getUserId(), listing.getListingId());
                favoriteMap.put(listing.getListingId(), isFavorite);
                //vi gemmer i vores map om listings er en favorite for den logget ind bruger
            }
            //vi sender vores map til html'en, bruges så thymeleaf ved hvilket icon det skal vise ved en listing
            model.addAttribute("favoriteMap", favoriteMap);
        } else //laver bare et emptyMap hvis ikke logget ind, hvis ikke her, giver 500 fejl
            model.addAttribute("favoriteMap", Collections.emptyMap());

        return "home";
    }

    @PostMapping("/favorites/toggle")
    public String toggleFavorite(@RequestHeader(value = "referer", required = false) String referer,
                                 @RequestParam Long listingId,
                                 HttpSession session,
                                 Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            model.addAttribute("needAuth", true);
            return "home";
        }

        userService.toggleFavorite(currentUser.getUserId(), listingId);
        //OKAY SÅ, den her finder ud af hvilken siden man var på før og digere dig tilbage til den
        //med det der request header, henter header og putter det i en string som vi bruger her
        return "redirect:" + (referer != null ? referer : "/home");
    }



    @GetMapping("/login")
    public String showLoginForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute User user, HttpSession session, Model model) {
        try {
            User loggedIn = loginService.login(user.getEmail(), user.getPassword());
            session.setAttribute("currentUser", loggedIn);
            return "redirect:/home";
        } catch (LoginFailedException e) {
            model.addAttribute("error", "Incorrect email or password");
            return "login";
        }
    }


    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        if (!model.containsAttribute("user")) { // Hvis vi ikke har en "user" så opretter vi en tom ny user og sender til signup
            model.addAttribute("user", new User());
        }
        return "signup";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                           @RequestParam("confirmPassword") String confirmPassword,
                           Model model) {
        boolean success = loginService.register(user, confirmPassword); // opret bruger, henter user inform og tjekker at confirm password er korrekt
        if (success) { return "redirect:/login"; } // Hvis oprettelse gik godt går vi til login
        model.addAttribute("registrationError", "Password confirmation mismatch or email already in use");
        return "signup";
    }

    @GetMapping("/privateUser")
    public String showPrivateUser(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            model.addAttribute("needAuth", true);
            return "home";
        }
        //giver informationer om logget ind bruger
        model.addAttribute("user", currentUser);
        //bruges til tjek om logget ind bruger er admin, admin panel tilgøes via denne side
        model.addAttribute("isAdmin", currentUser.isAdmin());

        //kan fjernes, havde nogen probelmer med role og admin
        System.out.println("Current user name: " + currentUser.getFirstName());
        System.out.println("Current user role: " + currentUser.getRole());
        System.out.println("Is admin? " + currentUser.isAdmin());

        return "privateUser";
    }

    //kan se godkendte listings
    @GetMapping("/privateUser/active")
    public String showApprovedListings(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            model.addAttribute("needAuth", true);
            return "home";
        }
        //noget th der gør man kan se hvilken side man er på, på sin profil
        model.addAttribute("activeTab", "active");
        //gør at den viser listings med APPROVED status
        return showListingsByStatus(session, model, "APPROVED");
    }

    //kan se solgte listings
    @GetMapping("/privateUser/sold")
    public String showSoldListings(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            model.addAttribute("needAuth", true);
            return "home";
        }
        model.addAttribute("activeTab", "sold");
        //gør at den viser listings med SOLD status
        return showListingsByStatus(session, model, "SOLD");
    }

    //kan se sine listings som afventer godkendelse
    @GetMapping("/privateUser/waiting")
    public String showPendingListings(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            model.addAttribute("needAuth", true);
            return "home";
        }
        model.addAttribute("activeTab", "waiting");
        //gør at den viser listings med PENDING status
        return showListingsByStatus(session, model, "PENDING");
    }

    //methode bliver kaldt af en getMapping og sender en status med sig
    private String showListingsByStatus(HttpSession session, Model model, String status) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            model.addAttribute("needAuth", true);
            return "home";
        }

        //opretter tom List af Listing
        List<Listing> listings;
        //ud fra hvad status er, bliver den relevant metode kaldt
        switch (status) {
            case "APPROVED" -> listings = listingService.getApprovedListingsForUser(currentUser.getUserId());
            case "SOLD" -> listings = listingService.getSoldListingsForUser(currentUser.getUserId());
            case "PENDING" -> listings = listingService.getPendingListingsForUser(currentUser.getUserId());
            //hvis status ikke matcher forbliver den tom
            default -> listings = List.of();
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("userListings", listings);
        model.addAttribute("isAdmin", currentUser.isAdmin());

        return "privateUser";
    }

    //html siden hvor man kan redigere sin bruger
    @GetMapping("/editUser")
    public String showEditUser(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            model.addAttribute("needAuth", true);
            return "home";
        }
        model.addAttribute("user", currentUser);

        return "editUser";
    }
    @PostMapping("/editUser")
    //vi requester f.eks. email men gør at den ikke siger fejl hvis ikke indtastet
    //ikke alle felter behøver blive ændret i
    public String editUser(HttpSession session, Model model, @RequestParam (required = false) String email,
                           @RequestParam (required = false) String password,
                           @RequestParam (required = false) String firstName,
                           @RequestParam (required = false) String lastName,
                           @RequestParam (required = false) String phoneNumber) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/home";
        }
        userService.updateUser(currentUser.getUserId(), email, password, firstName, lastName, phoneNumber );

        //sørger for at "updatere" informationerne på siden
        User updatedUser = userService.getUserById(currentUser.getUserId());
        session.setAttribute("currentUser", updatedUser);

        return "redirect:/privateUser";

    }
    //side til opret listing
    @GetMapping("/createSale")
    public String showCreateListingForm(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("saleAd", new Listing());

        // Hent alle kategorier fra databasen
        List<Category> allCategories = listingService.getAllCategories();
        List<Size> allSizes = listingService.getAllSizes();

        //bruges til dropdown box
        model.addAttribute("allCategories", allCategories);
        model.addAttribute("allSizes", allSizes);
        model.addAttribute("conditionsList", List.of("NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"));

        return "createSale";
    }

    @PostMapping("/createSale")
    public String createListing(@ModelAttribute("saleAd") Listing ad,
                                @RequestParam("image") MultipartFile file,
                                HttpSession session,
                                Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        ad.setSellerId(currentUser.getUserId());
        ad.setStatus(Listing.Status.PENDING);
        ad.setFairTrade(false);
        ad.setValidated(false);

        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

                String uniqueFilename = UUID.randomUUID().toString() + extension;

                String uploadDir = new File("src/main/resources/static/uploads").getAbsolutePath();

                File destination = new File(uploadDir + "/" + uniqueFilename);
                file.transferTo(destination);

                ad.setImageFileName(uniqueFilename);

            } catch (IOException e) { // TILFØJ EN RIGTIG EXCEPTION!!!!=!!=!=!=!=!=!=!=!=!=!=!=!
                e.printStackTrace();
                model.addAttribute("error", "There was an error uploading the image.");

                List<Category> allCategories = listingService.getAllCategories();
                List<Size> allSizes = listingService.getAllSizes();
                model.addAttribute("allCategories", allCategories);
                model.addAttribute("allSizes", allSizes);
                model.addAttribute("conditionsList", List.of("NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"));

                return "createSale";
            }
        }

        listingService.createListing(ad);
        return "redirect:/privateUser";
    }

    @GetMapping("/listings/{id}")
    public String listingPage(@PathVariable Long id, Model model) {
        Listing listing = listingService.getListingById(id);
        model.addAttribute("listing", listing);
        return "listingPage";
    }

    //side til admins
    @GetMapping("/adminPanel")
    public String showAdminPanel(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        //sørger for logget ind bruger er admin
        if (currentUser == null || !currentUser.isAdmin()) {
            //hvis ikke sendt til home
            return "redirect:/home";
        }
        model.addAttribute("isAdmin", currentUser.isAdmin());

        return "/adminPanel";
    }

    //side hvor admin kan godkende eller afvise nye Listings
    @GetMapping("/newArrivals")
    public String showPendingListings(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/home";
        }

        //henter listings med pending status
        List<Listing> pendingListings = listingService.getPendingListings();
        model.addAttribute("listings", pendingListings);
        model.addAttribute("isAdmin", currentUser.isAdmin());
        return "/newArrivals";
    }

    //til at godkende valgt listing
    @PostMapping("/approveListing")
    public String approveListing(@RequestParam("listingId") Long listingId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/home";
        }

        listingService.approveListingById(listingId, currentUser);
        return "redirect:newArrivals";
    }

    //til at afvise valgt listing
    @PostMapping("/denyListing")
    public String denyListing(@RequestParam("listingId") Long listingId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/home";
        }

        listingService.denyListingById(listingId, currentUser);
        return "redirect:newArrivals";
    }

    @GetMapping("/favorites")
    public String showFavorites(HttpSession session, Model model,
                                @RequestParam(defaultValue = "createdAt") String sortBy,
                                @RequestParam(defaultValue = "desc") String direction) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            model.addAttribute("needAuth", true);
            return "home";
        }
        List<Listing> favoriteListings = userService.getFavoriteListingsForUser(currentUser.getUserId());

        // Sorter listings ligesom på /home
        SortingService.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? SortingService.Direction.ASC
                : SortingService.Direction.DESC;

        if (sortBy.equalsIgnoreCase("price")) {
            favoriteListings = sortingService.byPrice(favoriteListings, sortDirection);
        } else {
            favoriteListings = sortingService.byDate(favoriteListings, sortDirection);
        }

        model.addAttribute("listings", favoriteListings);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        // Favoritmap bruges stadig for at vise hjerterne
        Map<Long, Boolean> favoriteMap = new HashMap<>();
        for (Listing listing : favoriteListings) {
            favoriteMap.put(listing.getListingId(), true); // vi ved alle er favoritter
        }
        model.addAttribute("favoriteMap", favoriteMap);
        return "/favorites";
    }
    @GetMapping("/publicUser/{userId}/{tab}")
    public String publicUser(@PathVariable Long userId,
                             @PathVariable String tab,
                             Model model,
                             HttpSession session) {

        User viewedUser = userService.getPublicUserById(userId);
        if (viewedUser == null) {
            return "redirect:/home";
        }

        List<Listing> listings = switch (tab) {
            case "active" -> listingService.getActiveListingsByUserId(userId);
            case "sold" -> listingService.getSoldListingsByUserId(userId);
            default -> listingService.getActiveListingsByUserId(userId);
        };

        model.addAttribute("user", viewedUser);
        model.addAttribute("userListings", listings);
        model.addAttribute("activeTab", tab);

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser != null) {
            Map<Long, Boolean> favoriteMap = new HashMap<>();
            for (Listing listing : listings) {
                boolean isFavorite = userService.isFavorite(currentUser.getUserId(), listing.getListingId());
                favoriteMap.put(listing.getListingId(), isFavorite);
            }
            model.addAttribute("favoriteMap", favoriteMap);
            model.addAttribute("isLoggedIn", true);
        } else {
            model.addAttribute("favoriteMap", Collections.emptyMap());
            model.addAttribute("isLoggedIn", false);
        }

        return "publicUser";
    }

}
