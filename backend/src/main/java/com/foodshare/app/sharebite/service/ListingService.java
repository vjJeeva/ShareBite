package com.foodshare.app.sharebite.service;

import com.foodshare.app.sharebite.model.Listing;
import com.foodshare.app.sharebite.model.Profile;
import com.foodshare.app.sharebite.model.User;
import com.foodshare.app.sharebite.payload.request.ListingRequest;
import com.foodshare.app.sharebite.repository.ListingRepository;
import com.foodshare.app.sharebite.repository.ProfileRepository;
import com.foodshare.app.sharebite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Listing createListing(ListingRequest request, Long donorId) {
        // ... (existing code remains same)
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
        return listingRepository.save(listing);
    }

    public List<Listing> getAvailableListings() {
        return listingRepository.findByStatus("AVAILABLE");
    }

    public List<Listing> getListingsByDonor(Long donorId) {
        return listingRepository.findByDonorId(donorId);
    }

    public Optional<Listing> getListingById(Long id) {
        Optional<Listing> listingOpt = listingRepository.findById(id);
        if (listingOpt.isPresent()) {
            Listing listing = listingOpt.get();
            // Populate donor info
            profileRepository.findByUserId(listing.getDonorId()).ifPresent(p -> listing.setDonorName(p.getName()));
            userRepository.findById(listing.getDonorId()).ifPresent(u -> listing.setDonorEmail(u.getEmail()));
        }
        return listingOpt;
    }
}