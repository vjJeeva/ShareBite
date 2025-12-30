package com.foodshare.app.sharebite.service;

import com.foodshare.app.sharebite.model.Listing;
import com.foodshare.app.sharebite.payload.request.ListingRequest;
import com.foodshare.app.sharebite.repository.ListingRepository;
import com.foodshare.app.sharebite.repository.ProfileRepository;
import com.foodshare.app.sharebite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Listing createListing(ListingRequest request, Long donorId) {
        Listing listing = new Listing();
        listing.setName(request.getName());
        listing.setDescription(request.getDescription());
        listing.setServings(request.getServings());
        listing.setType(request.getType());
        listing.setPhotoUrl(request.getPhotoUrl());
        listing.setLatitude(request.getLatitude());
        listing.setLongitude(request.getLongitude());
        listing.setAddress(request.getAddress());
        listing.setPhoneNumber(request.getPhoneNumber());

        listing.setDonorId(donorId);
        listing.setStatus("AVAILABLE");

        if (request.getClaimByTime() != null) {
            listing.setClaimByTime(request.getClaimByTime());
        } else {
            listing.setClaimByTime(Instant.now().plusSeconds(24 * 3600));
        }

        Listing saved = listingRepository.save(listing);

        populateDonorInfo(saved);

        messagingTemplate.convertAndSend("/topic/new-food", saved);

        return saved;
    }


    public List<Listing> getAvailableListings() {
        List<Listing> listings = listingRepository.findByStatus("AVAILABLE");
        // Hydrate every listing with donor details
        listings.forEach(this::populateDonorInfo);
        return listings;
    }


    public List<Listing> getListingsByDonor(Long donorId) {
        List<Listing> listings = listingRepository.findByDonorId(donorId);
        listings.forEach(this::populateDonorInfo);
        return listings;
    }


    public Optional<Listing> getListingById(Long id) {
        Optional<Listing> listingOpt = listingRepository.findById(id);
        listingOpt.ifPresent(this::populateDonorInfo);
        return listingOpt;
    }

    private void populateDonorInfo(Listing listing) {
        if (listing.getDonorId() != null) {
            profileRepository.findByUserId(listing.getDonorId())
                    .ifPresent(p -> listing.setDonorName(p.getName()));

            userRepository.findById(listing.getDonorId())
                    .ifPresent(u -> listing.setDonorEmail(u.getEmail()));
        }
    }
}