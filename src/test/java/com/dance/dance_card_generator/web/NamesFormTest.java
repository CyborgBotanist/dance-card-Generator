package com.dance.dance_card_generator.web;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NamesFormTest {

    private static NamesForm form(String leads, String follows) {
        NamesForm form = new NamesForm();
        form.setLeads(leads);
        form.setFollows(follows);
        return form;
    }

    @Test
    void trimsSpacesAndSkipsBlankLines() {
        // \r\n is what browsers send for line breaks in a text box.
        NamesForm form = form("  Adam  \r\n\r\n   \r\nBen\r\n", "Dana\n\n  Emma   Rose \n");

        assertThat(form.leadNames()).containsExactly("Adam", "Ben");
        assertThat(form.followNames()).containsExactly("Dana", "Emma Rose");
        assertThat(form.problems()).isEmpty();
    }

    @Test
    void complainsWhenThereAreNoLeads() {
        assertThat(form("  \n\n", "Dana")).extracting(NamesForm::problems)
                .isEqualTo(List.of("Please enter at least one lead."));
    }

    @Test
    void complainsWhenThereAreNoFollows() {
        assertThat(form("Adam", "")).extracting(NamesForm::problems)
                .isEqualTo(List.of("Please enter at least one follow."));
    }

    @Test
    void complainsAboutBothEmptyBoxesAtOnce() {
        assertThat(form("", "").problems()).containsExactly(
                "Please enter at least one lead.",
                "Please enter at least one follow.");
    }

    @Test
    void spotsTheSameNameTwiceInOneListIgnoringCapitals() {
        List<String> problems = form("Adam\nBen", "Dana\ndana").problems();

        assertThat(problems).hasSize(1);
        assertThat(problems.get(0)).startsWith("“Dana” is listed more than once.");
    }

    @Test
    void spotsTheSameNameInBothLists() {
        List<String> problems = form("Adam\nSam", "Dana\nSam").problems();

        assertThat(problems).hasSize(1);
        assertThat(problems.get(0)).startsWith("“Sam” is listed more than once.");
    }

    @Test
    void reportsEachRepeatedNameOnlyOnce() {
        assertThat(form("Adam\nAdam\nAdam", "Dana").problems()).hasSize(1);
    }

    @Test
    void roundTripsThroughSavedLists() {
        DancerLists lists = new DancerLists(List.of("Adam", "Ben"), List.of("Dana"));

        NamesForm form = NamesForm.from(lists);

        assertThat(form.getLeads()).isEqualTo("Adam\nBen");
        assertThat(form.toDancerLists()).isEqualTo(lists);
    }
}
