package com.example.parameterapproval.security;

import com.example.parameterapproval.common.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public HeaderUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof HeaderUser user)) {
            throw new BusinessException("Kimliği doğrulanmış kullanıcı bulunamadı");
        }
        return user;
    }
}

