package com.ZahidHasanJamil.SBproject.service;

import com.ZahidHasanJamil.SBproject.exception.UserAlreadyExistsException;
import com.ZahidHasanJamil.SBproject.exception.UserNotFoundException;
import com.ZahidHasanJamil.SBproject.model.*;
import com.ZahidHasanJamil.SBproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthResponseDto login(LoginRequestDto request) throws UserNotFoundException {

    authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );
    var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
    if(user == null) {
      throw new UserNotFoundException("Please give correct credentials");
    }
    var jwt = jwtService.generateToken(user);
    return AuthResponseDto.builder()
      .token(jwt)
      .build();
  }

  public AuthResponseDto register(RegisterRequestDto request ) throws UserAlreadyExistsException {
    var tempUser = userRepository.findByEmail(request.getEmail());
    if(!tempUser.isEmpty())  {
      throw new UserAlreadyExistsException("User with " + request.getEmail() + " already exists.");
    }
    var user = User.builder()
      .firstName(request.getFirstName())
      .lastName(request.getLastName())
      .email(request.getEmail())
      .password(passwordEncoder.encode(request.getPassword()))
            .mobile(request.getMobile())
      .role(Role.USER)
      .build();
    userRepository.save(user);
    var jwt = jwtService.generateToken(user);
    return AuthResponseDto.builder()
      .token(jwt)
      .build();
  }

}
