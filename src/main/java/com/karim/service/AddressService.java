package com.karim.service;

import com.karim.dto.AddressRequestDto;
import com.karim.dto.AddressResponseDto;

import java.util.List;

public interface AddressService {
    List<AddressResponseDto> getAddressesForUser(Long userId);
    AddressResponseDto createAddress(Long userId, AddressRequestDto dto);
    AddressResponseDto updateAddress(Long userId, Long addressId, AddressRequestDto dto);
    void deleteAddress(Long userId, Long addressId);
    AddressResponseDto getAddressById(Long userId, Long addressId);
}