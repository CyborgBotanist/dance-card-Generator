package com.dance.dance_card_generator.schedule;

/**
 * One dance of the evening, e.g. "Set 2, Dance 2, Foxtrot".
 *
 * danceNumber is the dance's position within its set, so it starts
 * over at 1 in every set (Set 1: Dance 1, Dance 2; Set 2: Dance 1, Dance 2; ...).
 */
public record DanceSlot(int setNumber, int danceNumber, String danceType) {
}
