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

    @NotNull(message = "Food ID is required for a claim")
    @Column(name = "food_id", nullable = false)
    private Long foodId;


    @NotNull(message = "Claimer ID is required")
    @Column(name = "claimer_id", nullable = false)
    private Long claimerId;

    @Column(nullable = false)
    private String claimStatus = "PENDING";

    @Column(nullable = false)
    private Instant claimedAt = Instant.now();
}
