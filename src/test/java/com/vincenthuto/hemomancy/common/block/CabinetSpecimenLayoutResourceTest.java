package com.vincenthuto.hemomancy.common.block;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CabinetSpecimenLayoutResourceTest {
    private static final Path MODELS = Path.of("src/main/resources/assets/hemomancy/models/block");

    @Test
    void cabinetSpecimenColumnsMatchGuiOrderFromTheFront() throws Exception {
        assertEquals(12.0, centerX(0), 0.001, "GUI cell 0 must render in the front-view left column");
        assertEquals(8.0, centerX(1), 0.001, "GUI cell 1 must render in the front-view middle column");
        assertEquals(4.0, centerX(2), 0.001, "GUI cell 2 must render in the front-view right column");
    }

    private static double centerX(int cell) throws Exception {
        var path = MODELS.resolve("phlebotomists_cabinet_specimen_" + cell + ".json");
        var model = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        var blood = model.getAsJsonArray("elements").get(0).getAsJsonObject();
        double from = blood.getAsJsonArray("from").get(0).getAsDouble();
        double to = blood.getAsJsonArray("to").get(0).getAsDouble();
        return (from + to) / 2.0;
    }
}
