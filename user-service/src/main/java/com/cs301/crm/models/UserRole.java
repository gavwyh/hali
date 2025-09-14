package com.cs301.crm.models;

import lombok.Getter;

@Getter
public enum UserRole {
    AGENT("ROLE_AGENT"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }
}
