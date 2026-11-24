package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Bakes Blockbench face rotations and cube pivots into root-relative bind vertices. */
final class NaeglerophaeonRig {
    private static final String[] SIDES = {"north", "south", "west", "east", "up", "down"};
    // Top-left, bottom-left, bottom-right, top-right when viewed from outside.
    private static final int[][] CORNERS = {
            {5, 4, 0, 1}, {3, 2, 6, 7}, {1, 0, 2, 3}, {7, 6, 4, 5}, {1, 3, 7, 5}, {2, 0, 4, 6}
    };
    private static final float UNIT = 1F / 16F;

    record Face(Vector3f[] vertices, float[] u, float[] v, int pass, int webPair,
                int axonAxis, Vector3f axonDetail) {}
    record Part(String name, Vector3f pivot, List<Face> faces, List<Part> children,
                Vector3f terminal, Vector3f bundleCenter) {
        Part child(String name) {
            return children.stream().filter(part -> part.name.equals(name)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Missing Naeglerophaeon bone: " + name));
        }

        List<Part> chain(String prefix) {
            List<Part> bones = new ArrayList<>();
            Part current = this;
            while (current.name.startsWith(prefix)) {
                bones.add(current);
                current = current.children.isEmpty() ? null : current.children.getFirst();
                if (current == null) break;
            }
            return List.copyOf(bones);
        }
    }

    private NaeglerophaeonRig() {}

    static Part load(String resource) {
        try (var stream = NaeglerophaeonRig.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("Missing " + resource);
            var model = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            var root = model.getAsJsonArray("outliner").get(0).getAsJsonObject();
            Vector3f origin = vector(root.getAsJsonArray("origin"));
            var resolution = model.getAsJsonObject("resolution");
            float width = resolution.get("width").getAsFloat(), height = resolution.get("height").getAsFloat();
            var elements = new HashMap<String, JsonObject>();
            for (JsonElement element : model.getAsJsonArray("elements")) {
                var cube = element.getAsJsonObject();
                elements.put(cube.get("uuid").getAsString(), cube);
            }
            return part(root, elements, origin, width, height);
        } catch (Exception failure) {
            throw new IllegalStateException("Cannot load Naeglerophaeon's Blockbench rig", failure);
        }
    }

    private static Part part(JsonObject parent, HashMap<String, JsonObject> elements, Vector3f origin,
                             float width, float height) {
        var name = parent.get("name").getAsString();
        var pivot = position(parent, "origin", origin);
        List<Face> faces = new ArrayList<>();
        List<Part> children = new ArrayList<>();
        Vector3f terminal = null;
        Vector3f bundleCenter = null;
        for (JsonElement child : parent.getAsJsonArray("children")) {
            if (child.isJsonObject()) {
                children.add(part(child.getAsJsonObject(), elements, origin, width, height));
                continue;
            }
            var cube = elements.get(child.getAsString());
            var cubeName = cube.get("name").getAsString();
            if (cubeName.endsWith("_terminal")) terminal = position(cube, "origin", origin);
            if (cubeName.endsWith("_bundle")) bundleCenter = position(cube, "origin", origin);
            addCube(cube, cubeName, origin, width, height, faces);
        }
        return new Part(name, pivot, List.copyOf(faces), List.copyOf(children), terminal, bundleCenter);
    }

    private static void addCube(JsonObject cube, String name, Vector3f origin, float width, float height,
                                List<Face> destination) {
        var min = position(cube, "from", origin);
        var max = position(cube, "to", origin);
        var pivot = position(cube, "origin", origin);
        var angles = vector(cube.getAsJsonArray("rotation")).mul((float) Math.PI / 180);
        var rotation = new Quaternionf().rotationZYX(angles.z, angles.y, angles.x);
        var vertices = new Vector3f[8];
        for (int i = 0; i < vertices.length; i++) {
            var point = new Vector3f((i & 4) != 0 ? max.x : min.x, (i & 1) != 0 ? max.y : min.y,
                    (i & 2) != 0 ? max.z : min.z);
            vertices[i] = point.sub(pivot).rotate(rotation).add(pivot);
        }
        var faces = cube.getAsJsonObject("faces");
        boolean glow = name.contains("_nodule_") || name.contains("_node_")
                || name.contains("_dorsal_node") || name.contains("_ending_") || name.endsWith("_terminal");
        boolean axon = name.startsWith("axon_joint_");
        boolean fiber = axon && (name.endsWith("_bundle") || name.endsWith("_fascicle"));
        int webPair = name.startsWith("web_") ? Integer.parseInt(name.substring(4, 6)) - 1 : -1;
        int pass = webPair >= 0 ? 2 : glow ? 1 : name.contains("_surface_nerve_") ? 4 : 0;
        for (int side = 0; side < SIDES.length; side++) {
            var face = faces.getAsJsonObject(SIDES[side]);
            if (face == null) continue;
            var uv = face.getAsJsonArray("uv");
            float u0 = uv.get(0).getAsFloat() / width, v0 = uv.get(1).getAsFloat() / height;
            float u1 = uv.get(2).getAsFloat() / width, v1 = uv.get(3).getAsFloat() / height;
            float[] sourceU = {u0, u0, u1, u1}, sourceV = {v0, v1, v1, v0};
            int turns = face.has("rotation") ? face.get("rotation").getAsInt() / 90 : 0;
            var corners = new Vector3f[4];
            var u = new float[4];
            var v = new float[4];
            for (int i = 0; i < corners.length; i++) {
                corners[i] = vertices[CORNERS[side][i]];
                int texel = Math.floorMod(i + turns, 4);
                u[i] = sourceU[texel];
                v[i] = sourceV[texel];
            }
            int axonAxis = !fiber || side < 2 ? 0 : side < 4 ? 1 : 2;
            destination.add(new Face(corners, u, v, pass, webPair, axonAxis, axon && !fiber ? pivot : null));
        }
    }

    private static Vector3f position(JsonObject json, String key, Vector3f origin) {
        return vector(json.getAsJsonArray(key)).sub(origin).mul(UNIT);
    }

    private static Vector3f vector(JsonArray values) {
        return new Vector3f(values.get(0).getAsFloat(), values.get(1).getAsFloat(), values.get(2).getAsFloat());
    }
}
