package com.dance.dance_card_generator.schedule;

/**
 * A "record" is a short way to write a simple class that just holds data.
 * Java automatically creates the constructor, the getters name() and role(),
 * and equals()/hashCode()/toString() for us.
 *
 * Usage: new Dancer("Adam", Role.LEAD)
 */
public record Dancer(String name, Role role) {
}
