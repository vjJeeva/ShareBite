package com.foodshare.app.sharebite.controller;

import com.foodshare.app.sharebite.model.User;
import com.foodshare.app.sharebite.payload.request.LoginRequest;
import com.foodshare.app.sharebite.payload.request.RegisterRequest;
import com.foodshare.app.sharebite.payload.response.JwtResponse;
import com.foodshare.app.sharebite.repository.UserRepository;
import com.foodshare.app.sharebite.security.jwt.JwtUtils;
import com.foodshare.app.sharebite.security.services.UserDetailsImpl;
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

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;


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

        if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Phone number is already registered!");
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setPasswordHash(encoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole().toUpperCase());
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully! You can now log in.");
    }
}