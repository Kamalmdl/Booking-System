package org.kamal.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kamal.userservice.dto.request.LoginRequest;
import org.kamal.userservice.dto.request.RegisterRequest;
import org.kamal.userservice.dto.response.UserResponse;
import org.kamal.userservice.entity.Role;
import org.kamal.userservice.entity.User;
import org.kamal.userservice.exception.InvalidCredentialsException;
import org.kamal.userservice.exception.UserAlreadyExistsException;
import org.kamal.userservice.repository.UserRepository;
import org.kamal.userservice.security.JwtService;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldSetRoleUserOnRegistration() {
        RegisterRequest registerRequest = new RegisterRequest("Kamal", "Mammadli", "kamal@example.com", "kamal1234", "kamal-phone");
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");

        UserResponse result = userService.registerUser(registerRequest);

        assertEquals(Role.USER, result.getRole());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterRequest registerRequest = new RegisterRequest("Kamal", "Mammadli", "kamal@example.com", "kamal1234", "kamal-phone");

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(registerRequest));
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExists() {
        RegisterRequest registerRequest = new RegisterRequest("Kamal", "Mammadli", "kamal@example.com", "kamal1234", "kamal-phone");

        when(userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(registerRequest));
    }

    @Test
    void shouldHashPasswordBeforeSaving() {
        RegisterRequest registerRequest = new RegisterRequest("Kamal", "Mammadli", "kamal@example.com", "kamal1234", "kamal-phone");
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");

        userService.registerUser(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User user =  userCaptor.getValue();

        assertEquals("hashedPassword", user.getPassword());
    }

    @Test
    void shouldThrowInvalidCredentialsWhenUserNotFound() {
        LoginRequest loginRequest = new LoginRequest("kamal@example.com", "kamal1234");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(loginRequest));
    }

    @Test
    void shouldThrowInvalidCredentialsWhenPasswordDoesNotMatch() {
        LoginRequest loginRequest = new LoginRequest("kamal@example.com", "kamal1234");
        User user  = new User();
        user.setPassword("hashedPassword");
        user.setEmail("kamal@example.com");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(loginRequest));

        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());

        verify(jwtService, never()).generateToken(any(), any(), any());
    }
}
