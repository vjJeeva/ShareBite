package com.foodshare.app.sharebite.controller;

import com.foodshare.app.sharebite.exception.ResourceNotFoundException;
import com.foodshare.app.sharebite.model.Profile;
import com.foodshare.app.sharebite.model.User;
import com.foodshare.app.sharebite.payload.request.LoginRequest;
import com.foodshare.app.sharebite.payload.request.RegisterRequest;
import com.foodshare.app.sharebite.payload.response.JwtResponse;
import com.foodshare.app.sharebite.repository.ProfileRepository;
import com.foodshare.app.sharebite.repository.UserRepository;
import com.foodshare.app.sharebite.security.jwt.JwtUtils;
import com.foodshare.app.sharebite.security.services.UserDetailsImpl;
import com.foodshare.app.sharebite.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Random;

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Long userId;
        String userRole;
        String userEmail;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl userPrincipal) {
            userId = userPrincipal.id();
            userRole = userPrincipal.getAuthorities().iterator().next().getAuthority();
            userEmail = userPrincipal.getUsername();

        } else if (principal instanceof UserDetails userPrincipal) {
            User user = userRepository.findByEmail(userPrincipal.getUsername())
                    .orElseThrow(() -> new RuntimeException("Error: User not found after authentication."));
            userId = user.getId();
            userRole = user.getRole();
            userEmail = user.getEmail();
        } else {
            throw new RuntimeException("Unrecognized principal type after authentication.");
        }

        String jwt = jwtUtils.generateJwtToken(userId);

        return ResponseEntity.ok(new JwtResponse(jwt,
                userId,
                userEmail,
                userRole));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        if (profileRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Phone number is already registered!");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(encoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole().toUpperCase());

        User savedUser = userRepository.save(user);

        Profile profile = new Profile();
        profile.setName(registerRequest.getName());
        profile.setPhoneNumber(registerRequest.getPhoneNumber());
        profile.setUser(savedUser);

        profileRepository.save(profile);

        // Generate and Send OTP

        savedUser = generateAndSaveOtp(savedUser);
        emailService.sendVerificationOtp(savedUser.getEmail(), savedUser.getOtpCode());

        return ResponseEntity.ok("User registered successfully. Please check your email for the verification code.");
    }

    private User generateAndSaveOtp(User user) {

        String otp = String.format("%06d", new Random().nextInt(1000000));

        Instant expiryTime = Instant.now().plusSeconds(5 * 60);

        user.setOtpCode(otp);
        user.setOtpExpiryTime(expiryTime);
        return userRepository.save(user);
    }

    /**
     * Endpoint for users to manually request a new OTP if the original expired.
     * Payload: {"email": "user@example.com"}
     */
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getIsEmailVerified()) {
            return ResponseEntity.ok("Email is already verified. No new OTP needed.");
        }

        user = generateAndSaveOtp(user);
        emailService.sendVerificationOtp(user.getEmail(), user.getOtpCode());

        return ResponseEntity.ok("Verification OTP sent to " + user.getEmail() + ". Check your inbox (and spam folder)!");
    }


    /**
     * Endpoint for users to submit the received OTP for verification.
     * Payload: {"email": "user@example.com", "otp": "123456"}
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otpSubmitted = request.get("otp");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getIsEmailVerified()) {
            return ResponseEntity.ok("Email is already verified.");
        }
        if (user.getOtpCode() == null || !otpSubmitted.equals(user.getOtpCode())) {
            return ResponseEntity.badRequest().body("Invalid or missing OTP code.");
        }

        if (user.getOtpExpiryTime().isBefore(Instant.now())) {
            return ResponseEntity.badRequest().body("OTP code has expired. Please request a new one using the /send-otp endpoint.");
        }

        user.setIsEmailVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);

        return ResponseEntity.ok("Email verified successfully! You can now log in.");
    }
}