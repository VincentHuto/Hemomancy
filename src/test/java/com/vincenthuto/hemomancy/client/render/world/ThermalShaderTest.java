package com.vincenthuto.hemomancy.client.render.world;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThermalShaderTest {
    @Test void materialsHaveLoadableProgramsAndCompleteUniformDefaults() throws Exception {
        Path root = Path.of("src/main/resources/assets/hemomancy/shaders/core/world");
        for (String name : new String[]{"manipulation_flame", "manipulation_molten", "manipulation_glass", "manipulation_cruor", "manipulation_skin"}) {
            Path definition = root.resolve(name + ".json");
            assertTrue(Files.exists(definition), "Missing material program: " + name);
            var json = JsonParser.parseString(Files.readString(definition)).getAsJsonObject();
            for (String stage : new String[]{"vertex", "fragment"}) {
                String id = json.get(stage).getAsString().replace("hemomancy:world/", "");
                assertTrue(Files.exists(root.resolve(id + (stage.equals("vertex") ? ".vsh" : ".fsh"))));
            }
            for (var item : json.getAsJsonArray("uniforms")) {
                var uniform = item.getAsJsonObject();
                assertEquals(uniform.get("count").getAsInt(), uniform.getAsJsonArray("values").size(),
                        name + ": " + uniform.get("name").getAsString());
            }
        }
    }
}
