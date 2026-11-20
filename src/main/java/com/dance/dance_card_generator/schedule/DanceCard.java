package com.dance.dance_card_generator.schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * One dancer's card: a list of lines, one for every dance of the evening, in order.
 */
public class DanceCard {

    private final Dancer dancer;
    private final List<CardLine> lines = new ArrayList<>();

    public DanceCard(Dancer dancer) {
        this.dancer = dancer;
    }

    public Dancer getDancer() {
        return dancer;
    }

    public List<CardLine> getLines() {
        return lines;
    }

    public void addLine(DanceSlot slot, Dancer partner) {
        lines.add(new CardLine(slot, partner));
    }

    /** How many dances this dancer has with the given partner. */
    public int timesDancedWith(Dancer partner) {
        int count = 0;
        for (CardLine line : lines) {
            if (partner.equals(line.partner())) {
                count++;
            }
        }
        return count;
    }

    /** How many dances this dancer sits out. */
    public int timesSatOut() {
        int count = 0;
        for (CardLine line : lines) {
            if (line.isSitOut()) {
                count++;
            }
        }
        return count;
    }
}
