package com.example.parameterapproval.security;

public record HeaderUser(String id, String displayName) {
    @Override
    public String toString() {
        return id;
    }
}

