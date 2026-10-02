package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MorphlingRenameScanScopeTest {
    @Test void includesCurrentSourceResourcesAndDocumentation() {
        for (String path : new String[] {
                "src/main/java/example/Item.java",
                "src/main/resources/assets/hemomancy/lang/en_us.json",
                "docs/harbinger_progression_redesign/00-player-journey.md",
                "docs/doneish/README.md",
                "src/main/resources/done/README.md"
        }) {
            assertTrue(MorphlingLumenlaceRenameResourceTest.isActiveText(Path.of(path)), path);
        }
    }

    @Test void excludesOnlyTheDocumentationArchiveAndNonTextAssets() {
        assertFalse(MorphlingLumenlaceRenameResourceTest.isActiveText(
                Path.of("docs/done/phlegethontic-nether-worldgen/orcadian-horseman/README.md")));
        assertFalse(MorphlingLumenlaceRenameResourceTest.isActiveText(
                Path.of("src/main/resources/assets/hemomancy/textures/item/morphling_lumenlace.png")));
    }
}
