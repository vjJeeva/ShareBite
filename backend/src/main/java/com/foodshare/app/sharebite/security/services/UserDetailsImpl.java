// UserDetailsImpl.java
package com.foodshare.app.sharebite.security.services;

import com.foodshare.app.sharebite.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record UserDetailsImpl(
        Long id,
        String email,
        String name,

        @JsonIgnore
        String password,

        Collection<? extends GrantedAuthority> authorities // ⬅️ The field is named 'authorities'
) implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole()));

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPasswordHash(),
                authorities);
    }

    // --- UserDetails Interface Implementations ---

    // ⬅️ CRITICAL FIX: Explicitly implement getAuthorities() and return the component accessor.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities; // Accesses the record component
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password; // Accesses the record component
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

    // Use the component accessor for equality check
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(this.id, user.id()); // ⬅️ Using the accessor method id()
    }
}