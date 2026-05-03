package com.restaurant.pos.auth;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password hashing utility (cost factor 12).
 */
@ApplicationScoped
public class PasswordHasher {

    private static final int COST = 12;

    public String hash(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(COST));
    }

    public boolean verify(String plaintext, String hash) {
        return BCrypt.checkpw(plaintext, hash);
    }
}
