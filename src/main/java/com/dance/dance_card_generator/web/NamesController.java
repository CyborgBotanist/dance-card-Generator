package com.dance.dance_card_generator.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.List;

/**
 * The start page at http://localhost:8080/, where the leads and follows are typed in.
 */
@Controller
public class NamesController {

    private static final Logger log = LoggerFactory.getLogger(NamesController.class);

    private final DancerListStore store;

    /** Spring sees this constructor needs a DancerListStore and passes in the one it created. */
    public NamesController(DancerListStore store) {
        this.store = store;
    }

    /** Shows the form, filled in with the last saved names (or empty boxes if there are none). */
    @GetMapping("/")
    public String showForm(Model model) {
        NamesForm form = store.load().map(NamesForm::from).orElseGet(NamesForm::new);
        return formPage(model, form, List.of(), store);
    }

    /**
     * Runs when "Create Dance Cards" is clicked.
     *
     * If the names have a problem, shows the form again with what was typed and the problems.
     * Otherwise saves them and sends the browser on to /cards. ("redirect:" makes the browser
     * load /cards itself, so refreshing the cards page doesn't ask to resubmit the form.)
     */
    @PostMapping("/")
    public String createCards(@ModelAttribute NamesForm namesForm, Model model) {
        List<String> problems = namesForm.problems();
        if (!problems.isEmpty()) {
            return formPage(model, namesForm, problems, store);
        }

        try {
            store.save(namesForm.toDancerLists());
        } catch (IOException e) {
            log.error("Couldn't save the names to {}", store.getFile(), e);
            return formPage(model, namesForm,
                    List.of("Your names couldn't be saved to " + store.getFile()
                            + ", so the cards weren't created. (" + e.getMessage() + ")"),
                    store);
        }
        return "redirect:/cards";
    }

    /**
     * Puts everything the start page needs into the model and returns the template name.
     * CardsController uses this too, if the saved file has names that can't be scheduled.
     */
    static String formPage(Model model, NamesForm form, List<String> problems, DancerListStore store) {
        model.addAttribute("namesForm", form);
        model.addAttribute("problems", problems);
        model.addAttribute("savedFile", store.getFile().toString());
        return "start";
    }
}
