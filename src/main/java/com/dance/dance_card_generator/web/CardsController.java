package com.dance.dance_card_generator.web;

import com.dance.dance_card_generator.schedule.DanceCard;
import com.dance.dance_card_generator.schedule.DanceScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles the web page that shows every dancer's card.
 *
 * @Controller tells Spring "this class answers web requests". Spring creates one
 * object of it when the app starts; we never call "new CardsController()" ourselves.
 */
@Controller
public class CardsController {

    // ---- Change the evening's format here ----
    private static final int NUMBER_OF_SETS = 4;
    private static final List<String> DANCES_IN_EACH_SET = List.of("Waltz", "Foxtrot");

    private final DancerListStore store;

    public CardsController(DancerListStore store) {
        this.store = store;
    }

    /**
     * Runs when a browser asks for http://localhost:8080/cards.
     *
     * Makes the cards from the saved names. If nothing is saved yet, sends the browser
     * to the start page to enter some.
     *
     * The Model is a bag of named values we hand to the HTML template.
     * The returned String is the template's name: "cards" means
     * src/main/resources/templates/cards.html.
     */
    @GetMapping("/cards")
    public String showCards(Model model) {
        Optional<DancerLists> saved = store.load();
        if (saved.isEmpty() || saved.get().hasNoNames()) {
            return "redirect:/";
        }

        // The file can be edited by hand, so check the names the same way the form does.
        NamesForm form = NamesForm.from(saved.get());
        List<String> problems = form.problems();
        if (!problems.isEmpty()) {
            return NamesController.formPage(model, form, problems, store);
        }

        DanceScheduler scheduler = new DanceScheduler(NUMBER_OF_SETS, DANCES_IN_EACH_SET);
        List<DanceCard> cards = scheduler.createCards(form.toDancerLists().toDancers());

        List<CardView> cardViews = new ArrayList<>();
        for (DanceCard card : cards) {
            cardViews.add(CardView.from(card));
        }

        // In the template, ${cards} now refers to this list.
        model.addAttribute("cards", cardViews);
        return "cards";
    }
}
