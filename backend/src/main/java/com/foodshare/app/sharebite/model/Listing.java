package com.foodshare.app.sharebite.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Food name is required")
    @Column(nullable = false)
    private String name;

    private String description;

    @NotNull(message = "Servings quantity is required")
    @Positive(message = "Servings must be a positive number")
    @Column(nullable = false)
    private Double servings;

    @NotBlank(message = "Food type is required")
    private String type;

    @NotBlank(message = "Photo URL is required")
    private String photoUrl; // photo URL (result of cloud upload)

    @NotNull(message = "Latitude is required")
    private Double latitude; // map location (for geolocation)

    @NotNull(message = "Longitude is required")
    private Double longitude; // map location (for geolocation)

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Donor phone number is required")
    @Pattern(regexp = "^[0-9]{10}$|^\\+[0-9]{1,3}[0-9]{10}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Column(nullable = false)
    private String status = "AVAILABLE"; // AVAILABLE, CLAIMED, EXPIRED, COMPLETED

    private Instant claimByTime;

    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    private Long claimerId;

    private Boolean recipientSigned = false;
    private Boolean donorVerified = false;
}