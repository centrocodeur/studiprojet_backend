package com.marien.jwt.backend.config;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.marien.jwt.backend.dto.UserDto;
import com.marien.jwt.backend.entities.User;
import com.marien.jwt.backend.exceptions.AppException;
import com.marien.jwt.backend.mappers.UserMapper;
import com.marien.jwt.backend.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
//import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class UserAuthProvider {

    private  final UserRepository userRepository;
    private final UserMapper userMapper;


    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    @PostConstruct
    protected void init(){
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }
    public String createToken(UserDto dto){
        Date now = new Date();
        Date validity = new Date(now.getTime()+ 3_600_000);
        return JWT.create()
                .withIssuer(dto.getEmail())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim("firstName", dto.getFirstName())
                .withClaim("lastName", dto.getLastName())
                .sign(Algorithm.HMAC256(secretKey));


    }


    public Authentication validateToken(String token){
        System.out.println("Validation token");
        Algorithm algorithm= Algorithm.HMAC256(secretKey);

        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT decoded = verifier.verify(token);

        UserDto user = UserDto.builder()
                .email(decoded.getIssuer()) // .login(decoded.getIssuer())
                .firstName(decoded.getClaim("firstName").asString())
                .lastName(decoded.getClaim("lastName").asString())
                .build();
        return  new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    }

    public Authentication validateTokenStrongly(String token){
        System.out.println("Strongly Validation token");
        Algorithm algorithm= Algorithm.HMAC256(secretKey);

        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT decoded = verifier.verify(token);
       User user= userRepository.findByEmail(decoded.getIssuer())
                .orElseThrow(()-> new AppException("Unknown user", HttpStatus.NOT_FOUND));

       return new UsernamePasswordAuthenticationToken(userMapper.toUserDto(user), null, Collections.emptyList());
    }


}
