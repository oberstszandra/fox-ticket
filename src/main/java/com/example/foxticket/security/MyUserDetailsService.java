package com.example.foxticket.security;

import com.example.foxticket.models.User;
import com.example.foxticket.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public MyUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(email);
        user.orElseThrow(() -> new UsernameNotFoundException("Not found: " + email));
        return user.map(MyUserDetails::new).get();
    }

    public MyUserDetails loadById(Long id) throws Exception {
        Optional<User> user = userRepository.findById(id);
        user.orElseThrow(() -> new Exception("Not found: " + id));
        return user.map(MyUserDetails::new).get();
    }
}