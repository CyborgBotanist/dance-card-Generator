package com.dance.dance_card_generator.schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * A temporary console demo: run this class's main method to print sample cards.
 * In IntelliJ, click the green arrow next to "public static void main".
 *
 * This does not start Spring or the web server. It just tests the scheduling logic.
 */
public class DanceCardDemo {

    // ---- Change the evening's format here ----
    private static final int NUMBER_OF_SETS = 4;
    private static final List<String> DANCES_IN_EACH_SET = List.of("Waltz", "Foxtrot");

    public static void main(String[] args) {
        DanceScheduler scheduler = new DanceScheduler(NUMBER_OF_SETS, DANCES_IN_EACH_SET);

        // Example 1: equal numbers (3 leads, 3 follows)
        List<Dancer> evenGroup = List.of(
                new Dancer("Adam", Role.LEAD),
                new Dancer("Ben", Role.LEAD),
                new Dancer("Carlos", Role.LEAD),
                new Dancer("Dana", Role.FOLLOW),
                new Dancer("Emma", Role.FOLLOW),
                new Dancer("Fiona", Role.FOLLOW)
        );
        printEverything("EXAMPLE 1: 3 leads, 3 follows", scheduler, evenGroup);

        // Example 2: unequal numbers (4 leads, 3 follows), so someone sits out each dance
        List<Dancer> unevenGroup = new ArrayList<>(evenGroup);
        unevenGroup.add(new Dancer("George", Role.LEAD));
        printEverything("EXAMPLE 2: 4 leads, 3 follows", scheduler, unevenGroup);
    }

    private static void printEverything(String title, DanceScheduler scheduler, List<Dancer> dancers) {
        System.out.println();
        System.out.println("##########  " + title + "  ##########");

        List<DanceCard> cards = scheduler.createCards(dancers);
        for (DanceCard card : cards) {
            printCard(card);
        }
        printPairingSummary(cards);
        printSitOutSummary(cards);
    }

    private static void printCard(DanceCard card) {
        Dancer dancer = card.getDancer();
        System.out.println();
        System.out.println("=== " + dancer.name() + " (" + dancer.role() + ") ===");
        for (CardLine line : card.getLines()) {
            // printf fills in the %s placeholders in order. "%-10s" means
            // "a string, padded with spaces to 10 characters", which lines up the columns.
            System.out.printf("  Set %d   Dance %d   %-10s %s%n",
                    line.slot().setNumber(), line.slot().danceNumber(),
                    line.slot().danceType(), line.partnerText());
        }
    }

    /** A table: one row per lead, one column per follow, showing how many times they danced together. */
    private static void printPairingSummary(List<DanceCard> cards) {
        List<DanceCard> leadCards = new ArrayList<>();
        List<Dancer> follows = new ArrayList<>();
        for (DanceCard card : cards) {
            if (card.getDancer().role() == Role.LEAD) {
                leadCards.add(card);
            } else {
                follows.add(card.getDancer());
            }
        }

        System.out.println();
        System.out.println("--- Times each lead danced with each follow ---");
        System.out.printf("%-10s", "");
        for (Dancer follow : follows) {
            System.out.printf("%8s", follow.name());
        }
        System.out.println();

        for (DanceCard leadCard : leadCards) {
            System.out.printf("%-10s", leadCard.getDancer().name());
            for (Dancer follow : follows) {
                System.out.printf("%8d", leadCard.timesDancedWith(follow));
            }
            System.out.println();
        }
    }

    private static void printSitOutSummary(List<DanceCard> cards) {
        System.out.println();
        System.out.println("--- Sit-outs per dancer ---");
        for (DanceCard card : cards) {
            System.out.printf("  %-10s %d%n", card.getDancer().name(), card.timesSatOut());
        }
    }
}
