package com.karim.controller;

import com.karim.config.AuditorConfig;
import com.karim.dto.AddressRequestDto;
import com.karim.dto.AddressResponseDto;
import com.karim.entity.User;
import com.karim.repository.UserRepository;
import com.karim.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final UserRepository userRepository;

    // JWT stores email as subject → AuditorConfig.getCurrentUserEmail() reads it
    // from SecurityContext → we look up the User entity to get the Long id
    private Long resolveUserId() {
        String email = AuditorConfig.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        return user.getId();
    }

    @GetMapping
    public ResponseEntity<List<AddressResponseDto>> getAll() {
        return ResponseEntity.ok(addressService.getAddressesForUser(resolveUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressById(resolveUserId(), id));
    }

    @PostMapping
    public ResponseEntity<AddressResponseDto> create(@Valid @RequestBody AddressRequestDto dto) {
        AddressResponseDto created = addressService.createAddress(resolveUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDto dto) {
        return ResponseEntity.ok(addressService.updateAddress(resolveUserId(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.deleteAddress(resolveUserId(), id);
        return ResponseEntity.noContent().build();
    }
}