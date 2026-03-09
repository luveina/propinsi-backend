package com.propinsi.backend.service;
import com.propinsi.backend.model.User;
import com.propinsi.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userDb;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDb.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Akun tidak ditemukan"));
    
        // Proteksi tambahan jika filter Hibernate meleset
        if (user.getStatus() == "Inactive") {
            throw new UsernameNotFoundException("Akun tidak ditemukan.");
        }
    
        return user;
    }
}