package com.dance.dance_card_generator.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @TempDir gives each test a fresh empty folder that's deleted afterwards,
 * so these tests never touch the real ~/DanceCards folder.
 */
class DancerListStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsTheNames() throws Exception {
        DancerListStore store = new DancerListStore(tempDir.toString());
        DancerLists lists = new DancerLists(List.of("Adam", "Ben"), List.of("Dana", "Émilie"));

        store.save(lists);

        assertThat(store.load()).contains(lists);
    }

    @Test
    void createsTheFolderIfItIsMissing() throws Exception {
        Path folder = tempDir.resolve("not").resolve("there").resolve("yet");
        DancerListStore store = new DancerListStore(folder.toString());

        store.save(new DancerLists(List.of("Adam"), List.of("Dana")));

        assertThat(folder.resolve("dancers.json")).exists();
    }

    @Test
    void writesOneNamePerLineSoItIsEasyToEditByHand() throws Exception {
        DancerListStore store = new DancerListStore(tempDir.toString());

        store.save(new DancerLists(List.of("Adam", "Ben"), List.of("Dana")));

        String json = Files.readString(store.getFile());
        assertThat(json).contains("\"leads\": [").containsPattern("\\R\\s+\"Adam\",\\R\\s+\"Ben\"\\R");
    }

    @Test
    void replacesTheOldNamesWhenSavingAgain() throws Exception {
        DancerListStore store = new DancerListStore(tempDir.toString());
        store.save(new DancerLists(List.of("Adam"), List.of("Dana")));

        DancerLists newer = new DancerLists(List.of("Ben"), List.of("Emma"));
        store.save(newer);

        assertThat(store.load()).contains(newer);
        assertThat(tempDir.resolve("dancers.json.tmp")).doesNotExist();
    }

    @Test
    void startsEmptyWhenThereIsNoFile() {
        DancerListStore store = new DancerListStore(tempDir.toString());

        assertThat(store.load()).isEmpty();
    }

    @Test
    void startsEmptyInsteadOfCrashingWhenTheFileIsBroken() throws Exception {
        Files.writeString(tempDir.resolve("dancers.json"), "{ this is not JSON");
        DancerListStore store = new DancerListStore(tempDir.toString());

        assertThat(store.load()).isEmpty();
    }

    @Test
    void treatsAMissingListAsNoNames() throws Exception {
        Files.writeString(tempDir.resolve("dancers.json"), "{ \"leads\": [\"Adam\"] }");
        DancerListStore store = new DancerListStore(tempDir.toString());

        assertThat(store.load()).contains(new DancerLists(List.of("Adam"), List.of()));
    }
}
