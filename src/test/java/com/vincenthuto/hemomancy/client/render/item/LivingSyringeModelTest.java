package com.vincenthuto.hemomancy.client.render.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingSyringeModelTest {
    private static final Path MODELS = Path.of("src/main/resources/assets/hemomancy/models");

    @Test
    void wristAndChamberCanMoveIndependently() throws IOException {
        var syringe = model("hemomancy:item/living_syringe");
        assertEquals("builtin/entity", syringe.get("parent").getAsString(),
                "The hinge and drum need independently rendered parts");
        var wrist = model("hemomancy:item/living_syringe/wrist");
        var chamber = model("hemomancy:item/living_syringe/chamber");
        assertTrue(wrist.getAsJsonArray("elements").size() > 0);
        assertTrue(chamber.getAsJsonArray("elements").size() > 0);
    }

    @Test
    void thirdPersonLeftHandUsesTheRightHandTransformSoTheRendererCanMirrorIt() throws IOException {
        var display = model("hemomancy:item/living_syringe").getAsJsonObject("display");
        assertEquals(display.get("thirdperson_righthand"), display.get("thirdperson_lefthand"));
    }

    @Test
    void theIndexedVialSitsOnTheFangAxisRatherThanTheDrumAxle() throws IOException {
        var project = MODELS.resolve("item/bbmodel/living_syringe_rack.bbmodel");
        var elements = JsonParser.parseString(Files.readString(project)).getAsJsonObject().getAsJsonArray("elements");
        var fang = part(elements, "hollow fang");
        var vial = part(elements, "vial body 1");
        var axle = part(elements, "spindle");
        assertEquals(center(fang, 0), center(vial, 0));
        assertEquals(center(fang, 2), center(vial, 2));
        assertTrue(center(fang, 2) > center(axle, 2));
        assertTrue(fang.getAsJsonArray("from").get(1).getAsDouble() > vial.getAsJsonArray("to").get(1).getAsDouble());
    }

    private static JsonObject part(JsonArray elements, String name) {
        for (var element : elements) {
            var cube = element.getAsJsonObject();
            if (name.equals(cube.get("name").getAsString())) return cube;
        }
        throw new AssertionError("Missing model part " + name);
    }

    private static double center(JsonObject part, int axis) {
        return (part.getAsJsonArray("from").get(axis).getAsDouble()
                + part.getAsJsonArray("to").get(axis).getAsDouble()) / 2;
    }

    @Test
    void detachedSpeedloaderShowsTheSameEightFillSteps() throws IOException {
        var rack = model("hemomancy:item/vial_rack");
        var overrides = rack.getAsJsonArray("overrides");
        assertEquals(8, overrides.size(), "The empty rack and all eight fill steps must be distinguishable");
        assertTrue(model(rack.get("parent").getAsString()).has("elements"),
                "The detached speedloader needs three-dimensional geometry");
        for (int count = 1; count <= 8; count++) {
            var override = overrides.get(count - 1).getAsJsonObject();
            assertEquals(count, override.getAsJsonObject("predicate").get("hemomancy:filled_vials").getAsInt());
            var textures = model(override.get("model").getAsString()).getAsJsonObject("textures");
            for (int chamber = 1; chamber <= 8; chamber++) {
                assertEquals(chamber <= count ? "#sample_full" : "#sample_empty",
                        textures.get("sample_" + chamber).getAsString());
            }
        }
    }

    private static JsonObject model(String id) throws IOException {
        var path = MODELS.resolve(id.substring("hemomancy:".length()) + ".json");
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
