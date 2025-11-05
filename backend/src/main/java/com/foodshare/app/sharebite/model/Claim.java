package com.foodshare.app.sharebite.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "claims")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Renamed from foodId to listingId to match the standard entity name
    @NotNull(message = "Listing ID is required for a claim")
    @Column(name = "listing_id", nullable = false)
    private Long listingId;


    // Renamed from claimerId to recipientId for clearer domain language
    @NotNull(message = "Recipient ID is required")
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    // Changed name from 'claimStatus' to 'status' for simplicity and consistency with Listing entity
    @Column(nullable = false)
    private String status = "PENDING_PICKUP";

    // Changed name from 'claimedAt' to 'claimTime' to match ClaimService usage
    @Column(nullable = false)
    private Instant claimTime = Instant.now();

    // Added for tracking when the item was actually picked up
    @Column(name = "fulfillment_time")
    private Instant fulfillmentTime;
}