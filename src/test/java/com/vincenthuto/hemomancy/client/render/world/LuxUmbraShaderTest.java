package com.vincenthuto.hemomancy.client.render.world;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class LuxUmbraShaderTest {
    @Test void bothFlowMaterialsShipCompleteProgramsWithMatchingUniformDefaults() throws Exception {
        Path root = Path.of("src/main/resources/assets/hemomancy/shaders/core/world");
        for (String name : new String[]{"manipulation_lux", "manipulation_umbra"}) {
            Path program = root.resolve(name + ".json");
            assertTrue(Files.isRegularFile(program), "Missing flow shader: " + name);
            var json = JsonParser.parseString(Files.readString(program)).getAsJsonObject();
            assertEquals("hemomancy:world/" + name, json.get("fragment").getAsString());
            assertTrue(Files.isRegularFile(root.resolve(name + ".vsh")));
            assertTrue(Files.isRegularFile(root.resolve(name + ".fsh")));
            var uniforms = json.getAsJsonArray("uniforms");
            var names = StreamSupport.stream(uniforms.spliterator(), false)
                    .map(u -> u.getAsJsonObject().get("name").getAsString()).collect(Collectors.toSet());
            assertTrue(names.containsAll(Set.of("HemoTime", "ColorModulator", "ModelViewMat", "ProjMat")));
            for (var value : uniforms) {
                var uniform = value.getAsJsonObject();
                assertEquals(uniform.get("count").getAsInt(), uniform.getAsJsonArray("values").size());
            }
        }
    }
}
