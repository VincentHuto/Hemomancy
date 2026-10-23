package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnettaDialogueSourceTest {
    private static final Pattern KEY = Pattern.compile("\"(hemomancy\\.(?:annetta|dialogue\\.annetta)\\.[a-z0-9_.]+?)\\.?\"");

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    void everyEncounterLineHasEnglishText() throws IOException {
        JsonObject lang = JsonParser.parseString(read("src/main/resources/assets/hemomancy/lang/en_us.json")).getAsJsonObject();
        for (String file : new String[]{
                "src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/AnnettaDialogueTrees.java",
                "src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/AnnettaKnowlesEntity.java"}) {
            Matcher matcher = KEY.matcher(read(file));
            while (matcher.find()) {
                String key = matcher.group(1);
                if (key.endsWith(".frantic") || key.endsWith(".ally")) continue;
                assertTrue(lang.has(key), file + " uses missing lang key " + key);
            }
        }
        for (int i = 0; i < 8; i++) assertTrue(lang.has("hemomancy.annetta.frantic." + i), "frantic line " + i);
        for (int i = 0; i < 4; i++) assertTrue(lang.has("hemomancy.annetta.ally." + i), "ally line " + i);
    }

    @Test
    void bothRoutesBeginFromADialogueChoice() throws IOException {
        String handler = com.vincenthuto.hemomancy.support.SourceAssertions.readDialogueRoutes();
        assertTrue(handler.contains("startsWith(\"annetta_\")") && handler.contains("annetta.handleDialogueChoice(player, event.getEventId())"));
        String trees = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/AnnettaDialogueTrees.java");
        assertTrue(trees.contains("EVENT_UNSEAL_JAR") && trees.contains("EVENT_OFFER_DRAUGHT"));
        String annetta = read("src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/AnnettaKnowlesEntity.java");
        int choice = annetta.indexOf("public boolean handleDialogueChoice");
        assertTrue(choice >= 0);
        String body = annetta.substring(choice, annetta.indexOf("private DialogueTree chooseCoweringDialogue"));
        // The choice re-checks what the dialogue promised before either route starts.
        assertTrue(body.contains("isToothPecksJar(held) && isHarbinger(player)") && body.contains("startHarbingerRoute(player, held)"));
        assertTrue(body.contains("canPurify(player)") && body.contains("startCureRoute(player, held)"));
    }
}
