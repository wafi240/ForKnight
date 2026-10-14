package com.forknight.common.enums;

/**
 * Enumeration of secret categories detected by the secret scanner.
 */
public enum SecretType {
    API_KEY,
    PASSWORD,
    TOKEN,
    AWS_SECRET,
    PRIVATE_KEY,
    DATABASE_URI,
    HIGH_ENTROPY_STRING
}
