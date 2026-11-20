package com.dance.dance_card_generator.web;

import com.dance.dance_card_generator.schedule.CardLine;
import com.dance.dance_card_generator.schedule.DanceCard;
import com.dance.dance_card_generator.schedule.Dancer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One dance card, arranged the way the web page shows it: the dancer's name and role,
 * then the dances grouped by set.
 *
 * This is a "view model": it only exists to make the HTML template simple. The scheduler
 * gives us one flat list of lines per card; here we split that list into sets so the
 * template can just loop over sets, then over the dances in each set.
 */
public record CardView(String name, String role, List<SetView> sets) {

    /** The dances of one set on one card, e.g. Set 2: Waltz with Ben, Foxtrot sit out. */
    public record SetView(int setNumber, List<CardLine> lines) {
    }

    public static CardView from(DanceCard card) {
        Dancer dancer = card.getDancer();

        // Group the lines by set number, keeping the sets in order.
        Map<Integer, List<CardLine>> linesBySet = new LinkedHashMap<>();
        for (CardLine line : card.getLines()) {
            int setNumber = line.slot().setNumber();
            // computeIfAbsent: get this set's list, or create an empty one if it's the first line of the set.
            linesBySet.computeIfAbsent(setNumber, key -> new ArrayList<>()).add(line);
        }

        List<SetView> sets = new ArrayList<>();
        for (Map.Entry<Integer, List<CardLine>> entry : linesBySet.entrySet()) {
            sets.add(new SetView(entry.getKey(), entry.getValue()));
        }

        return new CardView(dancer.name(), roleText(dancer), sets);
    }

    /** LEAD becomes "Lead", FOLLOW becomes "Follow". */
    private static String roleText(Dancer dancer) {
        String upper = dancer.role().name();
        return upper.charAt(0) + upper.substring(1).toLowerCase();
    }
}
