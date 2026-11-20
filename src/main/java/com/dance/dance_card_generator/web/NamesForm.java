package com.dance.dance_card_generator.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * What's in the two text boxes on the start page, exactly as typed.
 *
 * Spring fills this in from the submitted form: the textarea named "leads" goes into
 * setLeads(...), and "follows" into setFollows(...). We keep the raw text (rather than
 * only the cleaned names) so an error can show the boxes exactly as they were.
 */
public class NamesForm {

    private String leads = "";
    private String follows = "";

    /** Fills the boxes from saved names, one per line. */
    public static NamesForm from(DancerLists lists) {
        NamesForm form = new NamesForm();
        form.setLeads(String.join("\n", lists.leads()));
        form.setFollows(String.join("\n", lists.follows()));
        return form;
    }

    public String getLeads() {
        return leads;
    }

    public void setLeads(String leads) {
        this.leads = leads == null ? "" : leads;
    }

    public String getFollows() {
        return follows;
    }

    public void setFollows(String follows) {
        this.follows = follows == null ? "" : follows;
    }

    /** The lead names, cleaned up (see cleanLines). */
    public List<String> leadNames() {
        return cleanLines(leads);
    }

    /** The follow names, cleaned up (see cleanLines). */
    public List<String> followNames() {
        return cleanLines(follows);
    }

    public DancerLists toDancerLists() {
        return new DancerLists(leadNames(), followNames());
    }

    /**
     * Everything that has to be fixed before the cards can be made, written for the person
     * at the keyboard. An empty list means the names are fine.
     */
    public List<String> problems() {
        List<String> leadNames = leadNames();
        List<String> followNames = followNames();
        List<String> problems = new ArrayList<>();

        if (leadNames.isEmpty()) {
            problems.add("Please enter at least one lead.");
        }
        if (followNames.isEmpty()) {
            problems.add("Please enter at least one follow.");
        }

        for (String name : namesListedMoreThanOnce(leadNames, followNames)) {
            problems.add("“" + name + "” is listed more than once. Each dancer needs their own name, "
                    + "so to tell two people apart, add a last initial (for example “"
                    + name + " R.” and “" + name + " S.”).");
        }
        return problems;
    }

    /**
     * Splits the text into one name per line, trims spaces from each end, squeezes runs of
     * spaces inside a name down to one, and drops blank lines.
     *
     * "\\R" matches any kind of line break, including the \r\n that browsers send.
     */
    static List<String> cleanLines(String text) {
        List<String> names = new ArrayList<>();
        for (String line : text.split("\\R")) {
            String name = line.strip().replaceAll("\\s+", " ");
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * Names that appear twice anywhere, in either list or one in each. Capitals don't matter:
     * "dana" and "Dana" count as the same person. Each repeated name is reported once,
     * spelled the way it was first typed.
     */
    private static Set<String> namesListedMoreThanOnce(List<String> leadNames, List<String> followNames) {
        List<String> everyone = new ArrayList<>(leadNames);
        everyone.addAll(followNames);

        // Lowercase name -> the name as first typed.
        Map<String, String> firstSpelling = new LinkedHashMap<>();
        Set<String> repeated = new LinkedHashSet<>();
        for (String name : everyone) {
            String key = name.toLowerCase(Locale.ROOT);
            // putIfAbsent returns the existing value if this name was already seen, or null if it's new.
            String earlier = firstSpelling.putIfAbsent(key, name);
            if (earlier != null) {
                repeated.add(earlier);
            }
        }
        return repeated;
    }
}
