package com.vincenthuto.hemomancy.common.worldgen.structure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CrimsonTroupeCampTemplateTest {
    private static final List<String> VARIANTS = List.of("air", "blades", "pageantry", "embers", "memory");

    @Test
    void canvasRoofsRiseToPeaksWithWalkableCourtyardEntrances() throws Exception {
        for (String variant : VARIANTS) {
            var blocks = blocks(camp(variant));
            for (int left : new int[]{1, 15}) {
                int middle = left + 4;
                assertTrue(roofHeight(blocks, middle, 5) >= roofHeight(blocks, left, 5) + 4,
                        variant + " needs a pitched tent instead of a flat wool box");
                assertTrue(roofHeight(blocks, middle, 5) >= roofHeight(blocks, left + 8, 5) + 4);
                for (int x = middle - 1; x <= middle + 1; x++) {
                    for (int y = 1; y <= 3; y++) assertEquals("minecraft:air", blocks.get(key(x, y, 9)));
                }
            }
        }
    }

    @Test
    void professorsHaveHeadroomAndOriginalInstructionBlocksRemainAccessible() throws Exception {
        for (String variant : VARIANTS) {
            var camp = camp(variant);
            var blocks = blocks(camp);
            assertEquals("hemomancy:puppeteers_spindle", blocks.get(key(3, 1, 4)));
            assertEquals("minecraft:crafting_table", blocks.get(key(4, 1, 4)));
            for (var tag : camp.getList("entities", Tag.TAG_COMPOUND)) {
                var actor = (CompoundTag) tag;
                var pos = actor.getList("blockPos", Tag.TAG_INT);
                for (int y = pos.getInt(1); y <= pos.getInt(1) + 1; y++) {
                    assertEquals("minecraft:air", blocks.get(key(pos.getInt(0), y, pos.getInt(2))),
                            variant + " buries a professor or Threadkeeper");
                }
            }
        }
    }

    private static CompoundTag camp(String variant) throws Exception {
        return NbtIo.readCompressed(Path.of("src/main/resources/data/hemomancy/structure/troupe_camp_" + variant + ".nbt"),
                NbtAccounter.unlimitedHeap());
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

    private static int roofHeight(Map<String, String> blocks, int x, int z) {
        int roof = 0;
        for (int y = 1; y < 9; y++) {
            String block = blocks.get(key(x, y, z));
            if (block != null && block.endsWith("_wool")) roof = y;
        }
        return roof;
    }

    private static String key(int x, int y, int z) {
        return x + ":" + y + ":" + z;
    }
}
