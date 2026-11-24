package com.vincenthuto.hemomancy.client.model.entity.boss.phlegethosoma;

import com.google.gson.JsonParser;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PhlegethosomaModelTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");

    @Test
    void runtimeAtlasMatchesTheBlockbenchProject() throws Exception {
        var source = ASSETS.resolve("models/entity/bbmodel/bosses/PhlegethosomaModel.bbmodel");
        try (var reader = Files.newBufferedReader(source)) {
            var model = JsonParser.parseReader(reader).getAsJsonObject();
            assertEquals("phlegethosoma_arterial_fluke", model.get("model_identifier").getAsString());
            String data = model.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
            byte[] atlas = Base64.getDecoder().decode(data.substring(data.indexOf(',') + 1));
            assertArrayEquals(atlas, Files.readAllBytes(ASSETS.resolve("textures/entity/phlegethosoma/phlegethosoma.png")));
        }
    }

    @Test
    void bakedRigCanMoveTheSuckerCropAndRibbonIndependently() {
        ModelPart root = PhlegethosomaModel.createBodyLayer().bakeRoot().getChild("arterial_fluke");
        root.getChild("head").getChild("oral_sucker").getChild("upper_lip");
        ModelPart joint = root.getChild("forebody").getChild("crop");
        joint.getChild("blood_sac");
        for (int i = 1; i <= 8; i++) {
            joint = joint.getChild("swim_segment_0" + i);
            joint.getChild("left_frill");
            joint.getChild("right_frill");
        }
        joint.getChild("ribbon_tail").getChild("ribbon_tip");
    }
}

