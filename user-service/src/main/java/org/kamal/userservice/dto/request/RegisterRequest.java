package org.kamal.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "First name is required!")
    private String firstName;
    @NotBlank(message = "Last name is required!")
    private String lastName;
    @NotBlank(message = "Email is required!")
    @Email(message = "Email must be valid.")
    private String email;
    @NotBlank(message = "Password is required!")
    @Size(min=8,message = "Password must be at least 8 characters")
    private String password;
    @NotBlank(message = "Phone number is required!")
    private String phoneNumber;
}
