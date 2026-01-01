package com.foodshare.app.sharebite.service;

import com.foodshare.app.sharebite.model.Claim;
import com.foodshare.app.sharebite.model.Listing;
import com.foodshare.app.sharebite.repository.ClaimRepository;
import com.foodshare.app.sharebite.repository.ListingRepository;
import com.foodshare.app.sharebite.repository.ProfileRepository;
import com.foodshare.app.sharebite.exception.ResourceNotFoundException;
import com.foodshare.app.sharebite.exception.ClaimProcessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Transactional
    public Claim claimListing(Long listingId, Long recipientId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + listingId));

        if (!"AVAILABLE".equals(listing.getStatus())) {
            throw new ClaimProcessException("Listing is already " + listing.getStatus().toLowerCase() + " or unavailable.");
        }

        listing.setStatus("CLAIMED");
        listing.setClaimerId(recipientId);
        listingRepository.save(listing);

        Claim newClaim = new Claim();
        newClaim.setListingId(listingId);
        newClaim.setRecipientId(recipientId);
        newClaim.setClaimTime(Instant.now());
        newClaim.setStatus("PENDING_PICKUP");

        return claimRepository.save(newClaim);
    }

    @Transactional
    public Claim fulfillClaim(Long claimId, Long userId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        Listing listing = listingRepository.findById(claim.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated listing not found."));

        if (!listing.getDonorId().equals(userId)) {
            throw new ClaimProcessException("Only the listing donor can confirm fulfillment.");
        }

        if (!"PENDING_PICKUP".equals(claim.getStatus())) {
            throw new ClaimProcessException("Claim must be in PENDING_PICKUP status to fulfill.");
        }

        claim.setStatus("FULFILLED");
        claim.setFulfillmentTime(Instant.now());
        Claim fulfilledClaim = claimRepository.save(claim);

        listing.setStatus("COMPLETED");
        listingRepository.save(listing);

        return fulfilledClaim;
    }

    @Transactional
    public Claim cancelClaim(Long claimId, Long userId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        Listing listing = listingRepository.findById(claim.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated listing not found."));

        if (!claim.getRecipientId().equals(userId)) {
            throw new ClaimProcessException("You are not authorized to cancel this claim.");
        }

        if (!"PENDING_PICKUP".equals(claim.getStatus())) {
            throw new ClaimProcessException("Claim cannot be cancelled. Status is: " + claim.getStatus());
        }

        listing.setStatus("AVAILABLE");
        listingRepository.save(listing);

        claim.setStatus("CANCELLED");
        return claimRepository.save(claim);
    }

    public List<Claim> getClaimsByRecipient(Long recipientId) {
        return claimRepository.findByRecipientId(recipientId);
    }

    public List<Claim> getClaimsForDonorView(Long donorId) {
        List<Listing> donorListings = listingRepository.findByDonorId(donorId);
        List<Long> listingIds = donorListings.stream().map(Listing::getId).toList();

        List<Claim> claims = claimRepository.findByListingIdIn(listingIds);

        for (Claim claim : claims) {
            profileRepository.findByUserId(claim.getRecipientId()).ifPresent(p -> {
                claim.setRecipientName(p.getName());
                claim.setRecipientPhone(p.getPhoneNumber());
            });
        }

        return claims;
    }
}