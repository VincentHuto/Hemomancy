package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Chunk 15 T9: the five teaching camps and the abandoned stage, as tools/worldgen/circus writes them. */
class CrimsonTroupeCampTemplateTest {
    private static final Map<String, List<String>> APPARATUS = Map.of(
            "air", List.of("circus_trapeze", "circus_net"),
            "blades", List.of("circus_knife_wheel", "circus_target_board"),
            "pageantry", List.of("circus_stilt_rack", "circus_weight_frame"),
            "embers", List.of("circus_fire_pit", "circus_bellows_stand", "circus_hound_hoop"),
            "memory", List.of("circus_prompt_table", "circus_quarters_record"));
    private static final Map<String, List<String>> PROFESSORS = Map.of(
            "air", List.of("circus_acrobat"), "blades", List.of("circus_knife_thrower"),
            "pageantry", List.of("circus_stilt_walker", "circus_strongman"),
            "embers", List.of("circus_fire_eater", "circus_beast_tamer"), "memory", List.of("circus_understudy"));
    /** What #minecraft:dirt holds; no yard is floored with it. */
    private static final Set<String> DIRT = Set.of("minecraft:dirt", "minecraft:grass_block", "minecraft:coarse_dirt",
            "minecraft:podzol", "minecraft:rooted_dirt", "minecraft:mycelium", "minecraft:moss_block", "minecraft:mud",
            "minecraft:muddy_mangrove_roots");
    private static final int FLOOR = 2;

    @Test
    void eachCampStandsOnItsOwnGroundAroundItsApparatus() throws Exception {
        for (String variant : APPARATUS.keySet()) {
            var camp = read("troupe_camp_" + variant);
            assertEquals(List.of(27, 12, 27), size(camp), variant);
            var blocks = blocks(camp);
            for (var entry : blocks.entrySet()) {
                int[] p = pos(entry.getKey());
                if (p[1] == FLOOR - 1 && "minecraft:air".equals(blocks.get(key(p[0], FLOOR, p[2]))))
                    assertFalse(DIRT.contains(entry.getValue()), variant + " yard is floored with dirt at " + entry.getKey());
                if (p[1] == 0) assertNotEquals("minecraft:air", entry.getValue(), variant + " skirt has a hole");
            }
            for (String piece : APPARATUS.get(variant))
                assertTrue(blocks.containsValue("hemomancy:" + piece), variant + " lacks its " + piece);
            for (String piece : List.of("puppeteers_spindle", "circus_ring_curb", "circus_quarter_pole",
                    "circus_wagon_panel", "circus_laced_canvas", "hematic_lantern", "packed_mud"))
                assertTrue(blocks.containsValue("hemomancy:" + piece) || blocks.containsValue("minecraft:" + piece),
                        variant + " lacks its " + piece);
            assertTrue(blocks.containsValue("minecraft:crafting_table"), variant);
            assertEquals(4, wheelHubs(camp), variant + " has one wagon on four wheels");
        }
    }

    @Test
    void professorsHaveHeadroomAndRoomForTheirRehearsalThreats() throws Exception {
        for (String variant : APPARATUS.keySet()) {
            var camp = read("troupe_camp_" + variant);
            var blocks = blocks(camp);
            var roles = new java.util.ArrayList<String>();
            for (var tag : camp.getList("entities", Tag.TAG_COMPOUND)) {
                var actor = (CompoundTag) tag;
                var p = actor.getList("blockPos", Tag.TAG_INT);
                String id = actor.getCompound("nbt").getString("id").replace("hemomancy:", "");
                roles.add(id);
                assertEquals(FLOOR, p.getInt(1), variant + " " + id + " stands on the yard");
                clear(blocks, variant + " " + id, p.getInt(0), p.getInt(2));
                if (id.equals("circus_threadkeeper")) continue;
                // CircusPracticalController.begin: two threats four blocks east, one a block either side.
                for (int dz : new int[]{-1, 1}) clear(blocks, variant + " " + id + " practice", p.getInt(0) + 4, p.getInt(2) + dz);
            }
            var expected = new java.util.ArrayList<>(PROFESSORS.get(variant));
            expected.add("circus_threadkeeper");
            assertEquals(Set.copyOf(expected), Set.copyOf(roles), variant);
        }
    }

    @Test
    void theAbandonedStageIsASmallRoadsideWreckWithItsBill() throws Exception {
        var stage = read("troupe_abandoned_stage");
        assertEquals(List.of(17, 10, 15), size(stage));
        var blocks = blocks(stage);
        assertEquals("hemomancy:circus_stage_record", blocks.get(key(5, 5, 5)), "the stage bill stands on the boards");
        assertEquals("hemomancy:blood_wood_slab", blocks.get(key(4, 4, 5)), "a board to read it from");
        assertEquals("minecraft:air", blocks.get(key(4, 5, 5)));
        assertEquals("minecraft:air", blocks.get(key(4, 6, 5)));
        assertTrue(blocks.containsValue("hemomancy:circus_wrecked_panel"));
        assertTrue(blocks.containsValue("minecraft:gravel"), "the road");
        assertEquals(0, stage.getList("entities", Tag.TAG_COMPOUND).size(), "nobody is at the wreck");
        assertTrue(wheelHubs(stage) >= 2, "two wheels on the wagon and one dropped by the road");
    }

    private static void clear(Map<String, String> blocks, String who, int x, int z) {
        assertNotEquals(null, blocks.get(key(x, FLOOR - 1, z)), who + " has ground");
        assertNotEquals("minecraft:air", blocks.get(key(x, FLOOR - 1, z)), who + " has ground");
        for (int y = FLOOR; y <= FLOOR + 1; y++)
            assertEquals("minecraft:air", blocks.get(key(x, y, z)), who + " is obstructed at " + key(x, y, z));
    }

    private static int wheelHubs(CompoundTag root) {
        var palette = root.getList("palette", Tag.TAG_COMPOUND);
        int hubs = 0;
        for (var tag : root.getList("blocks", Tag.TAG_COMPOUND)) {
            var state = palette.getCompound(((CompoundTag) tag).getInt("state"));
            if (state.getString("Name").equals("hemomancy:circus_wagon_wheel")
                    && state.getCompound("Properties").getString("quarter").equals("0")) hubs++;
        }
        return hubs;
    }

    private static CompoundTag read(String name) throws Exception {
        return NbtIo.readCompressed(Path.of("src/main/resources/data/hemomancy/structure/" + name + ".nbt"),
                NbtAccounter.unlimitedHeap());
    }

    private static List<Integer> size(CompoundTag root) {
        var size = root.getList("size", Tag.TAG_INT);
        return List.of(size.getInt(0), size.getInt(1), size.getInt(2));
    }

    private static Map<String, String> blocks(CompoundTag root) {
        var palette = root.getList("palette", Tag.TAG_COMPOUND);
        var blocks = new HashMap<String, String>();
        for (var tag : root.getList("blocks", Tag.TAG_COMPOUND)) {
            var block = (CompoundTag) tag;
            var pos = block.getList("pos", Tag.TAG_INT);
            blocks.put(key(pos.getInt(0), pos.getInt(1), pos.getInt(2)),
                    palette.getCompound(block.getInt("state")).getString("Name"));
        }
        return blocks;
    }

    private static int[] pos(String key) {
        String[] parts = key.split(":");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])};
    }

    private static String key(int x, int y, int z) {
        return x + ":" + y + ":" + z;
    }
}
