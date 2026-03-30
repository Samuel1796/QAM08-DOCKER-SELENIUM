package org.example.utils;

import com.github.javafaker.Faker;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Random;
import java.util.stream.Stream;

/**
 * Centralised test data generator using Java Faker with a fixed seed for reproducibility.
 *
 * <p>Static methods use a shared seeded {@link Faker} instance initialised from {@code faker.seed}
 * in {@code test.properties}. Use {@link #withSeed(long)} to obtain a fresh instance for
 * property-based testing reproducibility checks.
 */
public class TestDataFactory {

    /** Shared seeded Faker instance used by all static methods. */
    private static final Faker faker = new Faker(new Random(ConfigLoader.getInt("faker.seed")));

    /** Per-instance Faker used when created via {@link #withSeed(long)}. */
    private final Faker instanceFaker;

    private TestDataFactory(long seed) {
        this.instanceFaker = new Faker(new Random(seed));
    }

    // -------------------------------------------------------------------------
    // Inner record
    // -------------------------------------------------------------------------

    /** Holds checkout form data. */
    public record CheckoutInfo(String firstName, String lastName, String postalCode) {}

    // -------------------------------------------------------------------------
    // Factory method for property-based testing
    // -------------------------------------------------------------------------

    /**
     * Returns a new {@code TestDataFactory} instance seeded with the given value.
     * Use this in property-based tests to verify reproducibility across seeds.
     *
     * @param seed the seed for the underlying {@link Faker} instance
     * @return a fresh {@code TestDataFactory} bound to {@code seed}
     */
    public static TestDataFactory withSeed(long seed) {
        return new TestDataFactory(seed);
    }

    // -------------------------------------------------------------------------
    // Static methods (shared seeded faker)
    // -------------------------------------------------------------------------

    /**
     * Returns a {@link CheckoutInfo} populated with faker-generated first name, last name,
     * and postal code using the shared seeded faker.
     */
    public static CheckoutInfo validCheckoutInfo() {
        return new CheckoutInfo(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.address().zipCode()
        );
    }

    /**
     * Returns a {@link Stream} of {@link Arguments} containing invalid {@code (username, password)}
     * pairs for parameterised login tests.
     */
    public static Stream<Arguments> invalidCredentials() {
        return Stream.of(
                Arguments.of("wrong_user",                    "secret_sauce"),
                Arguments.of("standard_user",                 "wrong_pass"),
                Arguments.of("bad_user",                      "bad_pass"),
                Arguments.of("' OR '1'='1",                   "secret_sauce"),
                Arguments.of("<script>alert(1)</script>",      "secret_sauce"),
                Arguments.of(longString(256),                  "secret_sauce"),
                Arguments.of("   ",                            "secret_sauce"),
                Arguments.of("",                               "secret_sauce"),
                Arguments.of("standard_user",                  "")
        );
    }

    /**
     * Returns a {@link Stream} of {@link Arguments} containing invalid
     * {@code (firstName, lastName, postalCode)} triples for parameterised checkout form tests.
     *
     * <p>Only cases that Swag Labs actually rejects with a validation error are included:
     * empty first name and empty postal code. The SUT does not validate last name or
     * combinations of empty fields beyond these two.
     */
    public static Stream<Arguments> invalidCheckoutData() {
        return Stream.of(
                Arguments.of("",     "Doe",  "12345"),  // empty first name
                Arguments.of("John", "Doe",  "")        // empty postal code
        );
    }

    // -------------------------------------------------------------------------
    // Edge-case string helpers
    // -------------------------------------------------------------------------

    /** Returns an empty string. */
    public static String emptyString() {
        return "";
    }

    /** Returns a whitespace-only string. */
    public static String whitespaceOnly() {
        return "   ";
    }

    /**
     * Returns a string of exactly {@code length} {@code 'a'} characters.
     *
     * @param length the desired string length
     * @return a string of {@code length} 'a' characters
     */
    public static String longString(int length) {
        return "a".repeat(length);
    }

    /** Returns a string of common special characters. */
    public static String specialChars() {
        return "!@#$%^&*()";
    }

    /** Returns a string containing Unicode and emoji characters. */
    public static String unicodeEmoji() {
        return "José 😀";
    }

    // -------------------------------------------------------------------------
    // Instance methods (per-seed faker, used by property tests)
    // -------------------------------------------------------------------------

    /**
     * Returns a {@link CheckoutInfo} populated with faker-generated values using this
     * instance's seeded faker.
     */
    public CheckoutInfo validCheckoutInfoInstance() {
        return new CheckoutInfo(
                instanceFaker.name().firstName(),
                instanceFaker.name().lastName(),
                instanceFaker.address().zipCode()
        );
    }

    /**
     * Returns a {@link Stream} of invalid credential {@link Arguments} using this instance's
     * seeded faker (mirrors the static version but bound to the instance faker).
     */
    public Stream<Arguments> invalidCredentialsInstance() {
        return Stream.of(
                Arguments.of("wrong_user",                    "secret_sauce"),
                Arguments.of("standard_user",                 "wrong_pass"),
                Arguments.of("bad_user",                      "bad_pass"),
                Arguments.of("' OR '1'='1",                   "secret_sauce"),
                Arguments.of("<script>alert(1)</script>",      "secret_sauce"),
                Arguments.of(longString(256),                  "secret_sauce"),
                Arguments.of("   ",                            "secret_sauce"),
                Arguments.of("",                               "secret_sauce"),
                Arguments.of("standard_user",                  "")
        );
    }
}
