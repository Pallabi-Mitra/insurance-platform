package com.pallabi.insurance.policy.model;

/**
 * The 3 insurance products we sell.
 * An "enum" is a fixed list of allowed values, so a typo like "MOTR"
 * becomes a compile error instead of bad data in the database.
 * Matches the CHECK constraint on the product_type column.
 */
public enum ProductType {
    MOTOR,
    HEALTH,
    LIFE
}
