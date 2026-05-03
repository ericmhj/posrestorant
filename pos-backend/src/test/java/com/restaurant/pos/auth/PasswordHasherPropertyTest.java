package com.restaurant.pos.auth;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.strings;

/**
 * Property-based tests for {@link PasswordHasher}.
 *
 * <p><b>Validates: Requirements 10.2</b> — passwords are stored with a secure hash (bcrypt).
 *
 * @tag Feature: devcontainer-setup, Property 2: Hash de contraseña es irreversible
 */
@QuarkusTest
@Tag("Feature: devcontainer-setup, Property 2: Hash de contraseña es irreversible")
class PasswordHasherPropertyTest {

    @Inject
    PasswordHasher passwordHasher;

    /**
     * Property: for any non-empty plaintext password {@code p}:
     * <ol>
     *   <li>{@code hash(p) != p} — the hash is never equal to the plaintext</li>
     *   <li>{@code verify(p, hash(p)) == true} — the hash can be verified</li>
     * </ol>
     *
     * <p><b>Validates: Requirements 10.2</b>
     */
    @Test
    void property_hashIsIrreversibleAndVerifiable() {
        qt()
                .withExamples(100)
                .forAll(strings().ascii().ofLengthBetween(1, 50))
                .checkAssert(plaintext -> {
                    String hash = passwordHasher.hash(plaintext);

                    // Property 1: hash is never equal to plaintext
                    assert !hash.equals(plaintext)
                            : "Hash should not equal plaintext for: " + plaintext;

                    // Property 2: verify(plaintext, hash(plaintext)) is always true
                    assert passwordHasher.verify(plaintext, hash)
                            : "verify should return true for correct plaintext: " + plaintext;
                });
    }

    /**
     * Property: two hashes of the same password are different (BCrypt uses random salt).
     *
     * <p><b>Validates: Requirements 10.2</b>
     */
    @Test
    void property_samePasswordProducesDifferentHashes() {
        qt()
                .withExamples(50)
                .forAll(strings().ascii().ofLengthBetween(1, 50))
                .checkAssert(plaintext -> {
                    String hash1 = passwordHasher.hash(plaintext);
                    String hash2 = passwordHasher.hash(plaintext);

                    // BCrypt uses random salt — same password should produce different hashes
                    assert !hash1.equals(hash2)
                            : "Two hashes of the same password should differ (random salt)";

                    // But both should verify correctly
                    assert passwordHasher.verify(plaintext, hash1)
                            : "First hash should verify";
                    assert passwordHasher.verify(plaintext, hash2)
                            : "Second hash should verify";
                });
    }

    /**
     * Property: a wrong password never verifies against a hash.
     *
     * <p><b>Validates: Requirements 10.2</b>
     */
    @Test
    void property_wrongPasswordDoesNotVerify() {
        qt()
                .withExamples(50)
                .forAll(
                        strings().ascii().ofLengthBetween(1, 50),
                        strings().ascii().ofLengthBetween(1, 50)
                )
                .assuming((p1, p2) -> !p1.equals(p2))
                .checkAssert((password, wrongPassword) -> {
                    String hash = passwordHasher.hash(password);

                    assert !passwordHasher.verify(wrongPassword, hash)
                            : "Wrong password should not verify against hash";
                });
    }
}
