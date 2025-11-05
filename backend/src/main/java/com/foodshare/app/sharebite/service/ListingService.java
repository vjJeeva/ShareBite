package com.foodshare.app.sharebite.service;

import com.foodshare.app.sharebite.model.Listing;
import com.foodshare.app.sharebite.payload.request.ListingRequest; // Import the new DTO
import com.foodshare.app.sharebite.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;

    public Listing createListing(ListingRequest request, Long donorId) {

        Listing listing = new Listing();
        listing.setName(request.getName());
        listing.setDescription(request.getDescription());
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
        return listingRepository.findById(id);
    }
}