package com.vincenthuto.hemomancy.client.render.world;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class AnimusMortemShaderTest {
    @Test void programsHaveMatchingResourcesAndUniformDefaults() throws Exception {
        Path root = Path.of("src/main/resources/assets/hemomancy/shaders/core/world");
        for (String name : new String[]{"manipulation_animus", "manipulation_mortem", "manipulation_infection", "manipulation_animus_model"}) {
            Path program = root.resolve(name + ".json");
            assertTrue(Files.isRegularFile(program), "Missing blood material: " + name);
            var json = JsonParser.parseString(Files.readString(program)).getAsJsonObject();
            String source=name.equals("manipulation_animus_model")?"manipulation_animus":name;
            assertEquals("hemomancy:world/" + source, json.get("fragment").getAsString());
            assertTrue(Files.isRegularFile(root.resolve(source + ".vsh")));
            String fragment = Files.readString(root.resolve(source + ".fsh"));
            assertTrue(fragment.contains("linear_fog"));
            for (var value : json.getAsJsonArray("uniforms")) {
                var uniform = value.getAsJsonObject();
                assertEquals(uniform.get("count").getAsInt(), uniform.getAsJsonArray("values").size());
            }
        }
    }
}
