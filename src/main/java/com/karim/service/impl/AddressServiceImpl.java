package com.karim.service.impl;

import com.karim.dto.AddressRequestDto;
import com.karim.dto.AddressResponseDto;
import com.karim.entity.Address;
import com.karim.entity.User;
import com.karim.exception.UserNotFoundException;
import com.karim.repository.AddressRepository;
import com.karim.repository.UserRepository;
import com.karim.service.AddressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository    userRepository;

    @Override
    public List<AddressResponseDto> getAddressesForUser(Long userId) {
        return addressRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponseDto createAddress(Long userId, AddressRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            addressRepository.clearDefaultForUser(userId);
        }

        Address address = Address.builder()
                .user(user)
                .label(dto.getLabel())
                .name(dto.getName())
                .phone(dto.getPhone())
                .line1(dto.getLine1())
                .line2(dto.getLine2())
                .city(dto.getCity())
                .state(dto.getState())
                .pin(dto.getPin())
                .landmark(dto.getLandmark())
                .isDefault(Boolean.TRUE.equals(dto.getIsDefault()))
                .build();

        return toDto(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponseDto updateAddress(Long userId, Long addressId, AddressRequestDto dto) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new UserNotFoundException("Address not found: " + addressId));

        if (Boolean.TRUE.equals(dto.getIsDefault()) && !address.getIsDefault()) {
            addressRepository.clearDefaultForUser(userId);
        }

        address.setLabel(dto.getLabel());
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setLine1(dto.getLine1());
        address.setLine2(dto.getLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPin(dto.getPin());
        address.setLandmark(dto.getLandmark());
        address.setIsDefault(Boolean.TRUE.equals(dto.getIsDefault()));

        return toDto(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new UserNotFoundException("Address not found: " + addressId));
        addressRepository.delete(address);
    }

    @Override
    public AddressResponseDto getAddressById(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .map(this::toDto)
                .orElseThrow(() -> new UserNotFoundException("Address not found: " + addressId));
    }

    // ── Mapper ──
    private AddressResponseDto toDto(Address a) {
        return AddressResponseDto.builder()
                .id(a.getId())
                .label(a.getLabel())
                .name(a.getName())
                .phone(a.getPhone())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .city(a.getCity())
                .state(a.getState())
                .pin(a.getPin())
                .landmark(a.getLandmark())
                .isDefault(a.getIsDefault())
                .formattedAddress(a.toFormattedString())
                .createdAt(a.getCreatedAt())
                .build();
    }
}