package com.dance.dance_card_generator.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WebMvcTest starts only the web part of the app (controllers and templates), without a real
 * server. MockMvc sends a pretend browser request and lets us check the response.
 *
 * The save folder is pointed at a temporary folder, so these tests never touch ~/DanceCards.
 */
@WebMvcTest({NamesController.class, CardsController.class})
@Import(DancerListStore.class)
class NamesAndCardsPagesTest {

    private static final Path SAVE_FOLDER = createTempFolder();

    @DynamicPropertySource
    static void useTempSaveFolder(DynamicPropertyRegistry registry) {
        registry.add("dancecards.data-folder", SAVE_FOLDER::toString);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DancerListStore store;

    @BeforeEach
    void startWithNoSavedNames() throws IOException {
        Files.deleteIfExists(store.getFile());
    }

    @Test
    void startPageHasEmptyBoxesWhenNothingIsSaved() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Dance Cards")))
                .andExpect(content().string(containsString("name=\"leads\"")))
                .andExpect(content().string(containsString("name=\"follows\"")))
                .andExpect(content().string(containsString(store.getFile().toString())))
                .andExpect(content().string(not(containsString("Almost there"))));
    }

    @Test
    void startPageFillsInTheSavedNames() throws Exception {
        store.save(new DancerLists(List.of("Adam", "Ben"), List.of("Dana")));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Adam\nBen</textarea>")))
                .andExpect(content().string(containsString("Dana</textarea>")));
    }

    @Test
    void creatingCardsSavesTheCleanedNamesAndGoesToTheCardsPage() throws Exception {
        mockMvc.perform(post("/")
                        .param("leads", "  Adam \r\n\r\nBen\r\nCarlos")
                        .param("follows", "Dana\r\nEmma\r\n  \r\nFiona\r\nGrace\r\n"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards"));

        assertThat(store.load()).contains(new DancerLists(
                List.of("Adam", "Ben", "Carlos"),
                List.of("Dana", "Emma", "Fiona", "Grace")));
    }

    @Test
    void cardsPageShowsEveryDancerTheSitOutsAndTheBackLink() throws Exception {
        store.save(new DancerLists(
                List.of("Adam", "Ben", "Carlos"),
                List.of("Dana", "Emma", "Fiona", "Grace")));

        mockMvc.perform(get("/cards"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Adam")))
                .andExpect(content().string(containsString("Grace")))
                .andExpect(content().string(containsString("Follow")))
                .andExpect(content().string(containsString("Set 4")))
                .andExpect(content().string(containsString("Foxtrot")))
                .andExpect(content().string(containsString("Sit out")))
                .andExpect(content().string(containsString("class=\"sit-out\"")))
                .andExpect(content().string(containsString("Back / Edit Names")))
                .andExpect(content().string(containsString("href=\"/\"")));
    }

    @Test
    void cardsPageSendsYouToTheStartPageWhenNothingIsSaved() throws Exception {
        mockMvc.perform(get("/cards"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void missingLeadsShowsAnErrorAndKeepsWhatWasTyped() throws Exception {
        mockMvc.perform(post("/")
                        .param("leads", "   ")
                        .param("follows", "Dana\nEmma"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Please enter at least one lead.")))
                .andExpect(content().string(containsString("Dana\nEmma</textarea>")));

        assertThat(store.getFile()).doesNotExist();
    }

    @Test
    void missingFollowsShowsAnError() throws Exception {
        mockMvc.perform(post("/")
                        .param("leads", "Adam")
                        .param("follows", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Please enter at least one follow.")));
    }

    @Test
    void repeatedNameShowsAnErrorKeepsWhatWasTypedAndDoesNotOverwriteTheSave() throws Exception {
        DancerLists lastGoodList = new DancerLists(List.of("Adam"), List.of("Dana"));
        store.save(lastGoodList);

        mockMvc.perform(post("/")
                        .param("leads", "Adam\nBen")
                        .param("follows", "Dana\nben"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("is listed more than once")))
                .andExpect(content().string(containsString("Dana\nben</textarea>")));

        assertThat(store.load()).contains(lastGoodList);
    }

    @Test
    void handEditedFileWithARepeatedNameShowsTheFormInsteadOfCrashing() throws Exception {
        Files.writeString(store.getFile(), "{ \"leads\": [\"Adam\"], \"follows\": [\"Adam\"] }");

        mockMvc.perform(get("/cards"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("is listed more than once")));
    }

    private static Path createTempFolder() {
        try {
            Path folder = Files.createTempDirectory("dance-cards-test");
            folder.toFile().deleteOnExit();
            return folder;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
