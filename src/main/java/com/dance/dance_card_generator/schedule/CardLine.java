package com.dance.dance_card_generator.schedule;

/**
 * One line on a dance card: which dance it is, and who the partner is.
 * If partner is null, the dancer sits out this dance.
 */
public record CardLine(DanceSlot slot, Dancer partner) {

    public static final String SIT_OUT_TEXT = "Sit out";

    public boolean isSitOut() {
        return partner == null;
    }

    /** The text to show on the card: the partner's name, or "Sit out". */
    public String partnerText() {
        if (isSitOut()) {
            return SIT_OUT_TEXT;
        }
        return partner.name();
    }
}
