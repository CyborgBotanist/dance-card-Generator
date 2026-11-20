package com.dance.dance_card_generator.web;

import com.dance.dance_card_generator.schedule.Dancer;
import com.dance.dance_card_generator.schedule.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * The names of the evening's leads and follows, in the order they were typed.
 *
 * This is exactly what gets saved to the dancers.json file, e.g.
 * { "leads": ["Adam", "Ben"], "follows": ["Dana", "Emma", "Fiona"] }
 */
public record DancerLists(List<String> leads, List<String> follows) {

    /**
     * A "compact constructor": runs whenever a DancerLists is created, including when it's
     * read from the file. A file with "leads" missing gives null, so treat that as no names.
     * List.copyOf also makes the lists unchangeable, so nobody can edit them by accident.
     */
    public DancerLists {
        leads = leads == null ? List.of() : List.copyOf(leads);
        follows = follows == null ? List.of() : List.copyOf(follows);
    }

    /** True when neither list has any names. */
    public boolean hasNoNames() {
        return leads.isEmpty() && follows.isEmpty();
    }

    /** Turns the names into Dancer objects for the scheduler: the leads first, then the follows. */
    public List<Dancer> toDancers() {
        List<Dancer> dancers = new ArrayList<>();
        for (String name : leads) {
            dancers.add(new Dancer(name, Role.LEAD));
        }
        for (String name : follows) {
            dancers.add(new Dancer(name, Role.FOLLOW));
        }
        return dancers;
    }
}
