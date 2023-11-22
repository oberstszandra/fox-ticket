package com.example.foxticket.security;

import com.example.foxticket.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyUserDetails implements UserDetails {
    private final Long id;
    private final String email;
    private final String password;
    private final List<GrantedAuthority> grantedAuthorityList;
    private final Boolean isAdmin;

    public MyUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.grantedAuthorityList = new ArrayList<>();
        this.isAdmin = user.isAdmin();
        addRole();
    }

    public Boolean isAdmin() {
        return isAdmin;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorityList;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private void addRole() {
        if (isAdmin) {
            grantedAuthorityList.add(new SimpleGrantedAuthority("ADMIN"));
        } else {
            grantedAuthorityList.add(new SimpleGrantedAuthority("USER"));
        }
    }
}