package com.example.campusrunbackend.service;

import com.example.campusrunbackend.dto.ProviderAdminDetailDto;
import com.example.campusrunbackend.dto.ProviderDiscoveryDto;
import com.example.campusrunbackend.dto.ProviderRegistrationDto;
import com.example.campusrunbackend.dto.ProviderResponseDto;
import com.example.campusrunbackend.model.Attachment;
import com.example.campusrunbackend.model.ProviderProfile;
import com.example.campusrunbackend.model.Role;
import com.example.campusrunbackend.model.ServiceCategory;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.model.SubscriptionPlan;
import com.example.campusrunbackend.model.ProviderRank;
import com.example.campusrunbackend.model.PromotionType;
import com.example.campusrunbackend.model.Booking;
import com.example.campusrunbackend.model.ProviderSubscription;
import com.example.campusrunbackend.repository.ProviderProfileRepository;
import com.example.campusrunbackend.repository.ServiceCategoryRepository;
import com.example.campusrunbackend.repository.UserRepository;
import com.example.campusrunbackend.repository.ProviderSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.campusrunbackend.dto.ProviderDashboardDto;
import com.example.campusrunbackend.model.BookingStatus;
import java.time.LocalDateTime;
import java.time.LocalDate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProviderService {

    private final ProviderProfileRepository providerProfileRepository;
    private final UserRepository userRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final com.example.campusrunbackend.repository.CampusServiceRepository campusServiceRepository;
    private final com.example.campusrunbackend.repository.BookingRepository bookingRepository;
    private final ProviderSubscriptionRepository providerSubscriptionRepository;
    private final com.example.campusrunbackend.repository.ProviderPromotionRepository providerPromotionRepository;
    private final com.example.campusrunbackend.repository.PromotionRepository promotionRepository;
    private final com.example.campusrunbackend.repository.ReviewRepository reviewRepository;

    @Autowired
    public ProviderService(ProviderProfileRepository providerProfileRepository,
                           UserRepository userRepository,
                           ServiceCategoryRepository categoryRepository,
                           FileStorageService fileStorageService,
                           com.example.campusrunbackend.repository.CampusServiceRepository campusServiceRepository,
                           com.example.campusrunbackend.repository.BookingRepository bookingRepository,
                           ProviderSubscriptionRepository providerSubscriptionRepository,
                           com.example.campusrunbackend.repository.ProviderPromotionRepository providerPromotionRepository,
                           com.example.campusrunbackend.repository.PromotionRepository promotionRepository,
                           com.example.campusrunbackend.repository.ReviewRepository reviewRepository) {
        this.providerProfileRepository = providerProfileRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
        this.campusServiceRepository = campusServiceRepository;
        this.bookingRepository = bookingRepository;
        this.providerSubscriptionRepository = providerSubscriptionRepository;
        this.providerPromotionRepository = providerPromotionRepository;
        this.promotionRepository = promotionRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public ProviderProfile registerProvider(User user, ProviderRegistrationDto dto, List<MultipartFile> cvs, List<MultipartFile> proofs) {
        // Validation: Max 5 categories
        if (dto.getCategoryIds() == null || dto.getCategoryIds().isEmpty()) {
            throw new RuntimeException("At least one service category must be selected");
        }
        if (dto.getCategoryIds().size() > 5) {
            throw new RuntimeException("Maximum 5 service categories allowed");
        }

        // Check if profile already exists
        providerProfileRepository.findByUserId(user.getId()).ifPresent(p -> {
            throw new RuntimeException("Provider profile already exists for this user");
        });

        // Save CVs (Limit 5)
        java.util.Set<Attachment> cvAttachments = new java.util.HashSet<>();
        if (cvs != null) {
            for (MultipartFile file : cvs) {
                if (cvAttachments.size() >= 5) break;
                String storedPath = fileStorageService.storeFile(file, "cvs");
                cvAttachments.add(new Attachment(file.getOriginalFilename(), storedPath));
            }
        }

        // Save Proofs (Limit 5)
        java.util.Set<Attachment> proofAttachments = new java.util.HashSet<>();
        if (proofs != null) {
            for (MultipartFile file : proofs) {
                if (proofAttachments.size() >= 5) break;
                String storedPath = fileStorageService.storeFile(file, "proofs");
                proofAttachments.add(new Attachment(file.getOriginalFilename(), storedPath));
            }
        }

        // Fetch categories
        Set<ServiceCategory> categories = new HashSet<>(categoryRepository.findAllById(dto.getCategoryIds()));
        if (categories.size() != dto.getCategoryIds().size()) {
            throw new RuntimeException("One or more category IDs are invalid");
        }

        // Create profile
        ProviderProfile profile = new ProviderProfile();
        profile.setUser(user);
        profile.setBio(dto.getBio());
        profile.setLocation(dto.getLocation());
        profile.setCvFiles(cvAttachments);
        profile.setProofFiles(proofAttachments);
        profile.setCategories(categories);

        // Ensure user is marked as unapproved provider
        user.setRole(Role.PROVIDER);
        user.setProviderStatus(com.example.campusrunbackend.model.ProviderStatus.PENDING);
        userRepository.save(user);

        // Save profile
        ProviderProfile savedProfile = providerProfileRepository.save(profile);
        System.out.println("REGISTRATION: Profile saved for user: " + user.getName());

        // Save selected services or create default ones for selected categories
        Set<Long> processedCategoryIds = new HashSet<>();
        if (dto.getServiceSelections() != null) {
            System.out.println("REGISTRATION: Received " + dto.getServiceSelections().size() + " service selections.");
            for (com.example.campusrunbackend.dto.ServiceSelectionDto selection : dto.getServiceSelections()) {
                ServiceCategory cat = categoryRepository.findById(selection.getCategoryId()).orElse(null);
                if (cat != null) {
                    com.example.campusrunbackend.model.CampusService campusService = new com.example.campusrunbackend.model.CampusService();
                    campusService.setProvider(user);
                    campusService.setCategory(cat);
                    campusService.setTitle(selection.getServiceTitle());
                    campusService.setDescription("Professional " + cat.getName() + " services provided by " + user.getName());
                    campusService.setPrice(new java.math.BigDecimal("0.00")); // Default price, provider can change later

                    campusServiceRepository.save(campusService);
                    System.out.println("REGISTRATION: Saved Service [" + campusService.getTitle() + "] in Category [" + cat.getName() + "]");
                    processedCategoryIds.add(cat.getId());
                }
            }
        }

        // Auto-create basic services for any selected categories that were missed
        for (ServiceCategory cat : categories) {
            if (!processedCategoryIds.contains(cat.getId())) {
                com.example.campusrunbackend.model.CampusService defaultService = new com.example.campusrunbackend.model.CampusService();
                defaultService.setProvider(user);
                defaultService.setCategory(cat);
                defaultService.setTitle("General " + cat.getName() + " Services");
                defaultService.setDescription("Reliable " + cat.getName() + " services provided by " + user.getName());
                defaultService.setPrice(new java.math.BigDecimal("0.00"));

                campusServiceRepository.save(defaultService);
                System.out.println("REGISTRATION: Created default Service for category: " + cat.getName());
            }
        }

        return savedProfile;
    }

    @Transactional(readOnly = true)
    public ProviderResponseDto getProviderById(Long id) {
        ProviderProfile profile = providerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));
        return mapToResponseDto(profile);
    }


    @Transactional
    public List<ProviderDiscoveryDto> discoverProviders(Long categoryId) {
        ServiceCategory category = categoryRepository.findById(categoryId).orElse(null);
        String categoryName = (category != null) ? category.getName() : "Unknown";

        System.out.println("DEBUG: Discovery Request -> Category ID: " + categoryId + " (" + categoryName + ")");

        // 1. Fetch matching providers (Online + Approved + Category match)
        List<User> matchingUsers = providerProfileRepository.findOnlineProvidersByCategory(
                categoryId, com.example.campusrunbackend.model.ProviderStatus.APPROVED);
        System.out.println("DEBUG: Found " + matchingUsers.size() + " online/approved providers for this category.");

        // 2. Fetch active subscriptions
        List<ProviderSubscription> activeSubscriptions = providerSubscriptionRepository.findAllByProviderInAndActive(matchingUsers, true);
        java.util.Map<Long, SubscriptionPlan> userPlanMap = activeSubscriptions.stream()
                .collect(Collectors.toMap(sub -> sub.getProvider().getId(), ProviderSubscription::getPlanType));

        // 2.5 Fetch active promotions
        List<com.example.campusrunbackend.model.ProviderPromotion> activePromotions = providerPromotionRepository.findAllByProviderInAndActive(matchingUsers, true);

        // 2.6 Fetch reviews for rating calculation
        List<com.example.campusrunbackend.model.Review> reviews = reviewRepository.findByBooking_Service_ProviderIn(matchingUsers);
        java.util.Map<Long, List<com.example.campusrunbackend.model.Review>> userReviewMap = reviews.stream()
                .collect(Collectors.groupingBy(r -> r.getBooking().getService().getProvider().getId()));

        // 3. Map to DTO and Sort
        return matchingUsers.stream()
                .map(user -> {
                    // Auto-assign promotions based on performance
                    autoAssignPromotions(user);

                    // Fetch the specific service the provider offers for this category
                    List<com.example.campusrunbackend.model.CampusService> categoryServices = campusServiceRepository.findByProvider(user).stream()
                            .filter(s -> s.getCategory() != null &&
                                    (s.getCategory().getId().equals(categoryId) ||
                                            (categoryName != null && s.getCategory().getName().equalsIgnoreCase(categoryName))))
                            .collect(Collectors.toList());

                    String serviceTitle = "General " + categoryName + " Services";
                    Double servicePrice = 0.0;
                    String serviceDesc = user.getName() + " provides professional services in " + categoryName;

                    if (categoryServices.isEmpty()) return null; // Skip providers with no actual service in this category

                    com.example.campusrunbackend.model.CampusService primaryService = categoryServices.get(0);
                    serviceTitle = primaryService.getTitle();
                    if (primaryService.getPrice() != null) {
                        servicePrice = primaryService.getPrice().doubleValue();
                    }
                    if (primaryService.getDescription() != null && !primaryService.getDescription().isEmpty()) {
                        serviceDesc = primaryService.getDescription();
                    }

                    List<String> allServiceTitles = categoryServices.stream()
                            .map(com.example.campusrunbackend.model.CampusService::getTitle)
                            .collect(Collectors.toList());

                    SubscriptionPlan plan = userPlanMap.getOrDefault(user.getId(), SubscriptionPlan.FREE);
                    ProviderRank rank = ProviderRank.FRESH_HUSTLER;
                    if (user.getProviderProfile() != null) {
                        rank = user.getProviderProfile().getRank();
                    }

                    com.example.campusrunbackend.model.Promotion highestPromo = activePromotions.stream()
                            .filter(p -> p.getProvider().getId().equals(user.getId()))
                            .map(com.example.campusrunbackend.model.ProviderPromotion::getPromotion)
                            .max(java.util.Comparator.comparingInt(p -> switch (p.getType()) { case FEATURED -> 4; case TRENDING -> 3; case TOP_RATED -> 2; case NEW_BOOST -> 1; }))
                            .orElse(null);

                    ProviderDiscoveryDto dto = new ProviderDiscoveryDto();
                    dto.setPromotionBadgeLabel(highestPromo != null ? highestPromo.getBadgeLabel() : null);
                    dto.setPromotionScore(highestPromo != null ? (switch (highestPromo.getType()) { case FEATURED -> 4; case TRENDING -> 3; case TOP_RATED -> 2; case NEW_BOOST -> 1; }) : 0);
                    dto.setId(primaryService.getId()); // Use Service ID for booking!
                    dto.setName(user.getName());
                    dto.setProviderName(user.getName());
                    dto.setProfileImage(user.getProfilePicture());
                    dto.setServices(allServiceTitles);
                    dto.setOnline(user.isOnline());
                    dto.setProviderPhone(user.getPhoneNumber());
                    List<com.example.campusrunbackend.model.Review> userReviews = userReviewMap.getOrDefault(user.getId(), new java.util.ArrayList<>());
                    double avgRating = userReviews.stream().mapToInt(com.example.campusrunbackend.model.Review::getRating).average().orElse(0.0);
                    int reviewCount = userReviews.size();

                    dto.setAverageRating(avgRating);
                    dto.setReviewCount(reviewCount);
                    dto.setTitle(serviceTitle);
                    dto.setPrice(servicePrice);
                    dto.setDescription(serviceDesc);
                    dto.setProviderId(user.getId()); // Actual user ID for chat
                    dto.setRecentReviews(new java.util.ArrayList<>()); // Empty list instead of String placeholder

                    dto.setSubscriptionPlan(plan);
                    dto.setRank(rank);

                    return dto;
                })
                .filter(java.util.Objects::nonNull)
                .sorted((a, b) -> {
                    // 1. Sort by promotions (Featured > Trending > Top Rated > Normal)
                    int promoCompare = b.getPromotionScore().compareTo(a.getPromotionScore());
                    if (promoCompare != 0) return promoCompare;

                    // 2. Sort by plan (LEGEND > CAMPUS_PLUG > FREE)
                    int pA = switch (a.getSubscriptionPlan()) { case LEGEND -> 3; case CAMPUS_PLUG -> 2; case FREE -> 1; };
                    int pB = switch (b.getSubscriptionPlan()) { case LEGEND -> 3; case CAMPUS_PLUG -> 2; case FREE -> 1; };
                    int planCompare = pB - pA;
                    if (planCompare != 0) return planCompare;

                    // Sort by rank (LEGEND > CAMPUS_PLUG > FRESH_HUSTLER)
                    int rA = switch (a.getRank()) { case LEGEND -> 3; case CAMPUS_PLUG -> 2; case FRESH_HUSTLER -> 1; };
                    int rB = switch (b.getRank()) { case LEGEND -> 3; case CAMPUS_PLUG -> 2; case FRESH_HUSTLER -> 1; };
                    int rankCompare = rB - rA;
                    if (rankCompare != 0) return rankCompare;

                    // Sort by rating
                    return b.getAverageRating().compareTo(a.getAverageRating());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void autoAssignPromotions(User provider) {
        if (provider.getCreatedAt() != null && provider.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusDays(7))) {
            assignPromotionIfNotExists(provider, PromotionType.NEW_BOOST, "New Provider 🚀", 7);
        }

        List<com.example.campusrunbackend.model.Review> reviews = reviewRepository.findByBooking_Service_Provider(provider);
        double avgRating = reviews.stream().mapToInt(com.example.campusrunbackend.model.Review::getRating).average().orElse(0.0);
        int reviewCount = reviews.size();

        if (avgRating >= 4.5 && reviewCount >= 5) {
            assignPromotionIfNotExists(provider, PromotionType.TOP_RATED, "Top Rated ⭐", 30);
        }

        if (provider.getJobsCompletedToday() >= 3 || provider.getTotalJobsCompleted() >= 20) {
            assignPromotionIfNotExists(provider, PromotionType.TRENDING, "Trending 🔥", 3);
        }
    }

    private void assignPromotionIfNotExists(User provider, PromotionType type, String badgeLabel, int durationDays) {
        com.example.campusrunbackend.model.Promotion promotion = promotionRepository.findAll().stream()
                .filter(p -> p.getType() == type)
                .findFirst()
                .orElseGet(() -> {
                    com.example.campusrunbackend.model.Promotion p = new com.example.campusrunbackend.model.Promotion();
                    p.setTitle(type.name());
                    p.setDescription(type.name() + " promotion");
                    p.setType(type);
                    p.setActive(true);
                    p.setBadgeLabel(badgeLabel);
                    return promotionRepository.save(p);
                });

        List<com.example.campusrunbackend.model.ProviderPromotion> existing = providerPromotionRepository.findAllByProviderAndActive(provider, true);
        boolean alreadyHas = existing.stream().anyMatch(pp -> pp.getPromotion().getType() == type);

        if (!alreadyHas) {
            com.example.campusrunbackend.model.ProviderPromotion pp = new com.example.campusrunbackend.model.ProviderPromotion();
            pp.setProvider(provider);
            pp.setPromotion(promotion);
            pp.setActivatedAt(java.time.LocalDateTime.now());
            pp.setExpiryDate(java.time.LocalDateTime.now().plusDays(durationDays));
            pp.setActive(true);
            providerPromotionRepository.save(pp);
        }
    }


    @Transactional
    public ProviderDashboardDto getDashboardData(User user) {
        // Daily reset logic
        LocalDate today = LocalDate.now();
        if (user.getLastProgressReset() == null || !user.getLastProgressReset().toLocalDate().isEqual(today)) {
            user.setJobsCompletedToday(0);
            user.setLastProgressReset(LocalDateTime.now());
            userRepository.save(user);
        }

        long newRequests = bookingRepository.countByService_ProviderAndStatus(user, BookingStatus.NEW);
        long activeJobs = bookingRepository.countByService_ProviderAndStatus(user, BookingStatus.PROCESSING);

        ProviderDashboardDto dto = new ProviderDashboardDto();
        dto.setGreeting("Hi " + user.getName().split(" ")[0] + " 👋");
        dto.setOnline(user.isOnline());
        dto.setCurrentProgress(user.getJobsCompletedToday());
        dto.setNewRequestsCount((int) newRequests);
        dto.setActiveJobsCount((int) activeJobs);

        // Calculate ratings
        List<com.example.campusrunbackend.model.Review> reviews = reviewRepository.findByReviewee(user);
        double avgRating = reviews.stream().mapToInt(com.example.campusrunbackend.model.Review::getRating).average().orElse(0.0);
        int totalReviews = reviews.size();

        dto.setAverageRating(avgRating);
        dto.setTotalReviews(totalReviews);

        // Status Message Logic
        if (!user.isOnline()) {
            dto.setStatusMessage("You’re offline — go online to start earning 💰");
        } else if (newRequests > 0) {
            dto.setStatusMessage("🔥 You have " + newRequests + " new job request(s) waiting!");
        } else if (activeJobs > 0) {
            dto.setStatusMessage("You’re on a roll 🚀 Keep it up!");
        } else {
            dto.setStatusMessage("You’re all set! Waiting for your next job…");
        }

        // Goal Logic
        int targetGoal = 5; // default
        if (user.getTotalJobsCompleted() < 10) targetGoal = 2;
        else if (user.getTotalJobsCompleted() < 50) targetGoal = 5;
        else targetGoal = 10;
        dto.setTargetGoal(targetGoal);

        // Challenge Status
        if (user.getJobsCompletedToday() == 0) dto.setChallengeStatus("ACTIVE");
        else if (user.getJobsCompletedToday() >= targetGoal) dto.setChallengeStatus("COMPLETED");
        else dto.setChallengeStatus("ACTIVE");

        if (user.getJobsCompletedToday() > targetGoal) dto.setChallengeStatus("BONUS");

        return dto;
    }

    @Transactional
    public void toggleOnlineStatus(User user) {
        if (user.getProviderStatus() != com.example.campusrunbackend.model.ProviderStatus.APPROVED) {
            throw new RuntimeException("Your account is pending approval. You cannot go online yet.");
        }
        user.setOnline(!user.isOnline());
        userRepository.saveAndFlush(user);
    }

    public ProviderResponseDto mapToResponseDto(ProviderProfile profile) {
        List<String> categoryNames = profile.getCategories().stream()
                .map(ServiceCategory::getName)
                .collect(Collectors.toList());

        return new ProviderResponseDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getUsername(),
                profile.getBio(),
                profile.getLocation(),
                categoryNames,
                profile.getCvFiles().isEmpty() ? null : profile.getCvFiles().iterator().next().getStoredPath(),
                profile.getProofFiles().isEmpty() ? null : profile.getProofFiles().iterator().next().getStoredPath(),
                profile.getUser().getProviderStatus() != null ? profile.getUser().getProviderStatus().name() : null,
                profile.getUser().isOnline()
        );
    }

    public ProviderAdminDetailDto mapToAdminDetailDto(ProviderProfile profile) {
        List<String> categoryNames = profile.getCategories().stream()
                .map(ServiceCategory::getName)
                .collect(Collectors.toList());

        return new ProviderAdminDetailDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getUsername(),
                profile.getUser().getEmail(),
                profile.getUser().getPhoneNumber(),
                profile.getUser().getIndexNumber(),
                profile.getBio(),
                profile.getLocation(),
                categoryNames,
                profile.getCvFiles(),
                profile.getProofFiles(),
                profile.getUser().getProviderStatus() != null ? profile.getUser().getProviderStatus().name() : null
        );
    }

    public ProviderAdminDetailDto mapUserToAdminDetailDto(User user) {
        ProviderProfile profile = user.getProviderProfile();
        if (profile == null) {
            return new ProviderAdminDetailDto(
                    null,
                    user.getId(),
                    user.getName(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getIndexNumber(),
                    "Profile setup not completed",
                    "N/A",
                    new java.util.ArrayList<>(),
                    new java.util.ArrayList<>(),
                    new java.util.ArrayList<>(),
                    user.getProviderStatus() != null ? user.getProviderStatus().name() : null
            );
        }
        return mapToAdminDetailDto(profile);
    }

    @Transactional
    public void activateSubscription(User provider, SubscriptionPlan plan) {
        // Placeholder for payment gateway integration
        // In a real app, you would verify payment before this step.

        // Deactivate old subscriptions
        List<ProviderSubscription> existing = providerSubscriptionRepository.findAllByProviderInAndActive(List.of(provider), true);
        existing.forEach(sub -> sub.setActive(false));
        providerSubscriptionRepository.saveAll(existing);

        ProviderSubscription subscription = new ProviderSubscription();
        subscription.setProvider(provider);
        subscription.setPlanType(plan);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1)); // 1 month duration
        subscription.setActive(true);

        providerSubscriptionRepository.save(subscription);

        // Update provider rank or other status if needed
        if (provider.getProviderProfile() != null) {
            if (plan == SubscriptionPlan.LEGEND) {
                provider.getProviderProfile().setRank(ProviderRank.LEGEND);
            } else if (plan == SubscriptionPlan.CAMPUS_PLUG) {
                provider.getProviderProfile().setRank(ProviderRank.CAMPUS_PLUG);
            }
            providerProfileRepository.save(provider.getProviderProfile());
        }
    }
}

