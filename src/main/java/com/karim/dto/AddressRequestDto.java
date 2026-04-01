package com.karim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequestDto {

    @NotBlank(message = "Label is required")
    private String label;           // Home / Work / Other

    @NotBlank(message = "Recipient name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+\\d\\s\\-]{7,20}$", message = "Invalid phone number")
    private String phone;
    
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 200)
    private String line1;

    @NotBlank(message = "Address line 2 is required")
    @Size(max = 200)
    private String line2;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @NotBlank(message = "PIN code is required")
    @Pattern(regexp = "^\\d{4,10}$", message = "Invalid PIN code")
    private String pin;

    @Size(max = 200)
    private String landmark;
    
    private Double latitude;
    private Double longitude;

    private Boolean isDefault = false;
}