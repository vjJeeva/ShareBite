package com.foodshare.app.sharebite.controller;

import com.foodshare.app.sharebite.exception.ClaimProcessException;
import com.foodshare.app.sharebite.exception.ResourceNotFoundException;
import com.foodshare.app.sharebite.model.Claim;
import com.foodshare.app.sharebite.security.services.UserDetailsImpl;
import com.foodshare.app.sharebite.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @PostMapping("/initiate/{listingId}")
    @PreAuthorize("hasAuthority('ROLE_RECIPIENT')")
    public ResponseEntity<?> initiateClaim(
            @PathVariable Long listingId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long recipientId = userDetails.id();

        try {
            Claim newClaim = claimService.claimListing(listingId, recipientId);
            return new ResponseEntity<>(newClaim, HttpStatus.CREATED);
        } catch (ClaimProcessException | ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-claims")
    @PreAuthorize("hasAuthority('ROLE_RECIPIENT')")
    public ResponseEntity<List<Claim>> getClaimsByRecipient(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long recipientId = userDetails.id();
        List<Claim> claims = claimService.getClaimsByRecipient(recipientId);
        return ResponseEntity.ok(claims);
    }

    @PutMapping("/cancel/{claimId}")
    @PreAuthorize("hasAuthority('ROLE_RECIPIENT')")
    public ResponseEntity<?> cancelClaim(
            @PathVariable Long claimId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long recipientId = userDetails.id();

        try {
            Claim cancelledClaim = claimService.cancelClaim(claimId, recipientId);
            return ResponseEntity.ok(cancelledClaim);
        } catch (ClaimProcessException | ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/donated-claims")
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<List<Claim>> getClaimsForDonorView(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long donorId = userDetails.id();
        List<Claim> claims = claimService.getClaimsForDonorView(donorId);
        return ResponseEntity.ok(claims);
    }

    @PutMapping("/fulfill/{claimId}")
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<?> fulfillClaim(
            @PathVariable Long claimId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long donorId = userDetails.id();

        try {
            Claim fulfilledClaim = claimService.fulfillClaim(claimId, donorId);
            return ResponseEntity.ok(fulfilledClaim);
        } catch (ClaimProcessException | ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}