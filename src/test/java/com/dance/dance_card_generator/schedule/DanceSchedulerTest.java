package com.dance.dance_card_generator.schedule;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Automatic checks of the fairness rules for every group size from
 * 1 lead + 1 follow up to 25 leads + 25 follows (50 dancers).
 */
class DanceSchedulerTest {

    private final DanceScheduler scheduler = new DanceScheduler(4, List.of("Waltz", "Foxtrot"));

    @Test
    void schedulesAreFairForAllGroupSizesUpTo50() {
        for (int leadCount = 1; leadCount <= 25; leadCount++) {
            for (int followCount = 1; followCount <= 25; followCount++) {
                checkSchedule(leadCount, followCount);
            }
        }
    }

    private void checkSchedule(int leadCount, int followCount) {
        List<Dancer> dancers = new ArrayList<>();
        for (int i = 1; i <= leadCount; i++) {
            dancers.add(new Dancer("Lead" + i, Role.LEAD));
        }
        for (int i = 1; i <= followCount; i++) {
            dancers.add(new Dancer("Follow" + i, Role.FOLLOW));
        }
        String label = leadCount + " leads, " + followCount + " follows: ";

        List<DanceCard> cards = scheduler.createCards(dancers);
        int danceCount = scheduler.getSlots().size();

        // Sit-outs are compared within each role, because when the groups are unequal
        // only the larger group can sit out. Index 0 = leads, 1 = follows.
        int[] fewestSitOuts = {Integer.MAX_VALUE, Integer.MAX_VALUE};
        int[] mostSitOuts = {0, 0};

        for (DanceCard card : cards) {
            // Every card has one line per dance.
            assertEquals(danceCount, card.getLines().size(), label + "wrong number of lines");

            // Partners are always the opposite role, and the partner's card points back to us.
            for (int d = 0; d < danceCount; d++) {
                Dancer partner = card.getLines().get(d).partner();
                if (partner != null) {
                    assertTrue(partner.role() != card.getDancer().role(), label + "same-role pair");
                    DanceCard partnerCard = findCard(cards, partner);
                    assertEquals(card.getDancer(), partnerCard.getLines().get(d).partner(),
                            label + "pairing is not mutual");
                }
            }

            // Partner counts: nobody dances with one partner more than 1 extra time vs another.
            int fewest = Integer.MAX_VALUE;
            int most = 0;
            for (DanceCard other : cards) {
                if (other.getDancer().role() != card.getDancer().role()) {
                    int times = card.timesDancedWith(other.getDancer());
                    fewest = Math.min(fewest, times);
                    most = Math.max(most, times);
                }
            }
            assertTrue(most - fewest <= 1, label + card.getDancer().name() + " has uneven partners");

            int role = card.getDancer().role().ordinal(); // LEAD = 0, FOLLOW = 1
            fewestSitOuts[role] = Math.min(fewestSitOuts[role], card.timesSatOut());
            mostSitOuts[role] = Math.max(mostSitOuts[role], card.timesSatOut());
        }

        // Sit-outs: nobody sits out more than 1 extra time vs anyone else in their role.
        assertTrue(mostSitOuts[0] - fewestSitOuts[0] <= 1, label + "uneven lead sit-outs");
        assertTrue(mostSitOuts[1] - fewestSitOuts[1] <= 1, label + "uneven follow sit-outs");
    }

    private DanceCard findCard(List<DanceCard> cards, Dancer dancer) {
        for (DanceCard card : cards) {
            if (card.getDancer().equals(dancer)) {
                return card;
            }
        }
        throw new AssertionError("No card for " + dancer);
    }
}
