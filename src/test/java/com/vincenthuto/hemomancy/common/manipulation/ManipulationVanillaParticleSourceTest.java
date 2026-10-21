package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ManipulationVanillaParticleSourceTest {
    private static final Path ROOT = Path.of("src/main/java/com/vincenthuto/hemomancy/common/manipulation");
    // Vanilla's ParticleTypes, not a mod class whose name merely ends in it.
    private static final Pattern VANILLA = Pattern.compile("(?<![A-Za-z0-9_])ParticleTypes\\.");

    @Test
    void manipulationsUseTheHemomancyVisualSuiteInsteadOfVanillaParticles() throws IOException {
        List<String> offenders;
        try (Stream<Path> files = Files.walk(ROOT)) {
            offenders = files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> {
                        try {
                            return VANILLA.matcher(Files.readString(path)).find();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(path -> ROOT.relativize(path).toString())
                    .sorted()
                    .toList();
        }
        assertTrue(offenders.isEmpty(),
                "Manipulation visuals use ManipulationVisuals forms or HemoParticleData, not vanilla ParticleTypes"
                        + " (see docs/MANIPULATION_VISUALS.md): " + offenders);
    }
}
