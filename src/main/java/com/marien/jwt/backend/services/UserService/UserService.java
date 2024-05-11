package com.marien.jwt.backend.services.UserService;


import com.marien.jwt.backend.dto.CredentialsDto;
import com.marien.jwt.backend.dto.SignUpDto;
import com.marien.jwt.backend.dto.UserDto;
import com.marien.jwt.backend.entities.User;
import com.marien.jwt.backend.exceptions.AppException;
import com.marien.jwt.backend.mappers.UserMapper;
import com.marien.jwt.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private  final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserDto login(CredentialsDto credentialsDto){
        User user = userRepository.findByLogin(credentialsDto.login())
                .orElseThrow(()-> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if(passwordEncoder.matches(CharBuffer.wrap(credentialsDto.password()),
                 user.getPassword())){
            return userMapper.toUserDto(user);

        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);

    }

    public UserDto register(SignUpDto signUpDto){
        Optional<User> oUser = userRepository.findByLogin(signUpDto.login());

        if(oUser.isPresent()){
            throw new AppException("Login already existe", HttpStatus.BAD_REQUEST);
        }

        User user = userMapper.signUpToUser(signUpDto);

        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(signUpDto.password())));

        User savedUser = userRepository.save(user);

        return userMapper.toUserDto(savedUser);


    }
}
