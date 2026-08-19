package org.kamal.userservice.service;

import lombok.RequiredArgsConstructor;
import org.kamal.userservice.dto.request.LoginRequest;
import org.kamal.userservice.dto.request.RegisterRequest;
import org.kamal.userservice.dto.response.UserResponse;
import org.kamal.userservice.entity.Role;
import org.kamal.userservice.entity.User;
import org.kamal.userservice.exception.InvalidCredentialsException;
import org.kamal.userservice.exception.UserAlreadyExistsException;
import org.kamal.userservice.repository.UserRepository;
import org.kamal.userservice.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponse registerUser(RegisterRequest registerRequest) {
        if(userRepository.existsByEmail(registerRequest.getEmail()) || userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new UserAlreadyExistsException("User already exists");
        }
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        user.setPassword(encodedPassword);
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRole(Role.USER);

        userRepository.save(user);

        return UserResponse.fromEntity(user);
    }

    public String loginUser(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String token = jwtService.generateToken(
                    user.getEmail(),
                    user.getRole(),
                    user.getId()
            );
            return token;

        } else {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }
}
