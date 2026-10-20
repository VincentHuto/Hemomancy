package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArtEnding;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The casting wiki and the preview command keep pace with the presentation enums. */
class CastingDocsSourceTest {
    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path)).replace("\r\n","\n");
    }
    /** CRIMSON_CORONATION -> "Crimson Coronation". */
    static String title(Enum<?> value) {
        var words=new ArrayList<String>();
        for(var word:value.name().toLowerCase(Locale.ROOT).split("_"))
            words.add(Character.toUpperCase(word.charAt(0))+word.substring(1));
        return String.join(" ",words);
    }

    @Test void wikiNamesEveryStyleAndEnding() throws IOException {
        // Compared without case so prose capitalisation ("Bloom of Rot") still counts.
        var wiki=read("wiki/Casting-Animations.md").toLowerCase(Locale.ROOT);
        var missing=new ArrayList<String>();
        for(var style:CastStyle.values())
            if(style!=CastStyle.SCHOOL && !wiki.contains(title(style).toLowerCase(Locale.ROOT)))missing.add(title(style));
        for(var ending:StillArtEnding.values())
            if(!wiki.contains(title(ending).toLowerCase(Locale.ROOT)))missing.add(title(ending));
        assertTrue(missing.isEmpty(),"wiki/Casting-Animations.md does not name "+missing);
    }

    @Test void previewRegistersStillFormAndOptions() throws IOException {
        var source=read("src/main/java/com/vincenthuto/hemomancy/client/event/CastingPreviewCommands.java");
        for(var needle:new String[]{"literal(\"still\")","\"charge\"","\"style\"","\"type\""})
            assertTrue(source.contains(needle),"CastingPreviewCommands is missing "+needle);
    }

    @Test void referenceDescribesStillArtPresentation() throws IOException {
        var reference=read("docs/HEMOMANCY_REFERENCE.md");
        assertTrue(reference.contains("StillArtEnding") || reference.contains("Still Art presentation"),
                "docs/HEMOMANCY_REFERENCE.md must describe Still Art presentation");
    }
}
