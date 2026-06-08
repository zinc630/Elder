package com.example.elder.domain;

public enum Role {
    ELDER,
    CHILD,
    AGENCY,
    ADMIN;

    public static boolean agencyOrAdmin(Role r) {
        return r == AGENCY || r == ADMIN;
    }
}

