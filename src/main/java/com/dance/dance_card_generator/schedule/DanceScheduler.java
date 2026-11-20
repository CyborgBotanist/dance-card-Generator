package com.dance.dance_card_generator.schedule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds a dance card for every dancer.
 *
 * HOW THE PAIRING WORKS (a "rotation" mixer):
 *
 * Picture the larger group (say, the leads) standing in a circle, each in a fixed spot.
 * The smaller group (the follows) stands in an inner circle facing them.
 * After every dance, the inner circle moves one spot to the right, so everyone
 * gets a new partner, and you only see the same partner again after you've met
 * everyone else.
 *
 * If the groups are unequal, the inner circle has some EMPTY spots, so both
 * circles are the same size. Whoever faces an empty spot sits out that dance.
 * The empty spots are spread out evenly around the circle (not bunched together),
 * so the sit-outs move around the room evenly instead of hitting the same
 * few people several times.
 */
public class DanceScheduler {

    /** Every dance of the evening, in order (e.g. Set 1 Waltz, Set 1 Foxtrot, Set 2 Waltz, ...). */
    private final List<DanceSlot> slots = new ArrayList<>();

    /**
     * @param numberOfSets    how many sets the evening has, e.g. 4
     * @param dancesInEachSet the dances in each set, in order, e.g. List.of("Waltz", "Foxtrot")
     */
    public DanceScheduler(int numberOfSets, List<String> dancesInEachSet) {
        if (numberOfSets < 1 || dancesInEachSet.isEmpty()) {
            throw new IllegalArgumentException("Need at least one set with at least one dance.");
        }
        for (int set = 1; set <= numberOfSets; set++) {
            int danceNumber = 1; // starts over at 1 in every set
            for (String danceType : dancesInEachSet) {
                slots.add(new DanceSlot(set, danceNumber, danceType));
                danceNumber++;
            }
        }
    }

    public List<DanceSlot> getSlots() {
        return slots;
    }

    /**
     * Creates one card per dancer. The cards come back in the same order as the input list.
     */
    public List<DanceCard> createCards(List<Dancer> dancers) {
        checkNamesAreUnique(dancers);

        // Split the dancers into leads and follows.
        List<Dancer> leads = new ArrayList<>();
        List<Dancer> follows = new ArrayList<>();
        for (Dancer dancer : dancers) {
            if (dancer.role() == Role.LEAD) {
                leads.add(dancer);
            } else {
                follows.add(dancer);
            }
        }
        if (leads.isEmpty() || follows.isEmpty()) {
            throw new IllegalArgumentException("Need at least one lead and at least one follow.");
        }

        // Figure out which group is larger. (The "? :" is a short if/else:
        // condition ? valueIfTrue : valueIfFalse.)
        List<Dancer> largerGroup = leads.size() >= follows.size() ? leads : follows;
        List<Dancer> smallerGroup = leads.size() >= follows.size() ? follows : leads;

        // The inner circle: the smaller group, with empty spots (null) mixed in evenly.
        List<Dancer> innerCircle = buildInnerCircle(smallerGroup, largerGroup.size());
        int circleSize = largerGroup.size();

        // A Map links each dancer (the "key") to their card (the "value").
        // LinkedHashMap remembers the order we put things in, so the cards
        // come out in the same order as the input list.
        Map<Dancer, DanceCard> cards = new LinkedHashMap<>();
        for (Dancer dancer : dancers) {
            cards.put(dancer, new DanceCard(dancer));
        }

        // Go through the evening one dance at a time.
        for (int danceIndex = 0; danceIndex < slots.size(); danceIndex++) { // 0, 1, 2, ... through the whole evening
            DanceSlot slot = slots.get(danceIndex);

            for (int spot = 0; spot < circleSize; spot++) {
                // The inner circle has moved danceIndex spots to the right.
                // The % (remainder) makes the count wrap back around to 0 at the end of the circle.
                Dancer outer = largerGroup.get((spot + danceIndex) % circleSize);
                Dancer inner = innerCircle.get(spot);

                if (inner == null) {
                    // Facing an empty spot: sit out this dance.
                    cards.get(outer).addLine(slot, null);
                } else {
                    cards.get(outer).addLine(slot, inner);
                    cards.get(inner).addLine(slot, outer);
                }
            }
        }

        return new ArrayList<>(cards.values());
    }



    /*
     * I need to remember that it is almost 100% gauranteed that the smaller group will be the leads.
     * basically, the men must not have nay empty dances.
     */

    /**
     * Makes a list of size circleSize that holds every dancer from smallerGroup,
     * with null (an empty spot) filling the remaining places, spread out evenly.
     *
     * Example: 3 follows in a circle of 6 gives [empty, A, empty, B, empty, C].
     */
    private List<Dancer> buildInnerCircle(List<Dancer> smallerGroup, int circleSize) {
        List<Dancer> circle = new ArrayList<>();
        int placed = 0;
        for (int spot = 0; spot < circleSize; spot++) {
            // How many real dancers "should" have been placed by the end of this spot,
            // if they were spread perfectly evenly. (Integer division rounds down.)
            int shouldHavePlaced = (spot + 1) * smallerGroup.size() / circleSize;
            if (shouldHavePlaced > placed) {
                circle.add(smallerGroup.get(placed));
                placed++;
            } else {
                circle.add(null); // empty spot
            }
        }
        return circle;
    }

    private void checkNamesAreUnique(List<Dancer> dancers) {
        Set<String> seen = new HashSet<>();
        for (Dancer dancer : dancers) {
            // Set.add returns false if the name was already in the set.
            if (!seen.add(dancer.name())) {
                throw new IllegalArgumentException("Two dancers are both named " + dancer.name());
            }
        }
    }
}
