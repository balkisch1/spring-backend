package com.example.hotel.service;

import com.example.hotel.dto.AuthDTOs;
import com.example.hotel.entity.Client;
import com.example.hotel.repository.ClientRepository;
import com.example.hotel.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthDTOs.AuthResponse register(AuthDTOs.RegisterRequest request) {

        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Client client = Client.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .telephone(request.getTelephone())
                .role(Client.Role.CLIENT)
                .build();

        clientRepository.save(client);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                client.getEmail(),
                client.getPassword(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + client.getRole().name()))
        );

        String token = jwtUtils.generateToken(userDetails);

        return AuthDTOs.AuthResponse.builder()
                .token(token)
                .email(client.getEmail())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .role(client.getRole().name())
                .id(client.getId())
                .build();
    }

    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request) {

        Client client = clientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        if (!passwordEncoder.matches(request.getPassword(), client.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                client.getEmail(),
                client.getPassword(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + client.getRole().name()))
        );

        String token = jwtUtils.generateToken(userDetails);

        return AuthDTOs.AuthResponse.builder()
                .token(token)
                .email(client.getEmail())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .role(client.getRole().name())
                .id(client.getId())
                .build();
    }
}