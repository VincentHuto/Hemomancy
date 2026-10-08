package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.mixin.core.VigilTemplateAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.*;

@GameTestHolder("crimson_troupe_validation")
@PrefixGameTestTemplate(false)
public final class CrimsonTroupeStructureGameTests {
    @GameTest(template="empty", timeoutTicks=40)
    public static void realTemplatesRetainTheArenaAndCoverEveryProfessor(GameTestHelper h) {
        var manager = h.getLevel().getStructureManager();
        var legacy = manager.get(Hemomancy.rloc("circus_pavilion")).orElseThrow();
        var expanded = manager.get(Hemomancy.rloc("crimson_troupe_pavilion")).orElseThrow();
        h.assertTrue(expanded.getSize().equals(new BlockPos(65,14,65)), "new pavilion bounds do not bind");
        var authored = new HashMap<BlockPos, StructureTemplate.StructureBlockInfo>();
        for (var block : ((VigilTemplateAccessor)expanded).hemomancy$palettes().getFirst().blocks()) authored.put(block.pos(), block);
        for (var block : ((VigilTemplateAccessor)legacy).hemomancy$palettes().getFirst().blocks()) {
            var actual = authored.get(block.pos().offset(16,0,16));
            h.assertTrue(actual != null && actual.state().equals(block.state()) && Objects.equals(actual.nbt(), block.nbt()),
                    "original arena state or block-entity data changed at " + block.pos());
        }
        h.assertTrue(faculty(h, expanded).size() == 9, "pavilion lost a faculty role or original performer");
        var covered = new HashSet<String>();
        for (String variant : List.of("air","blades","pageantry","embers","memory")) {
            var camp = manager.get(Hemomancy.rloc("troupe_camp_" + variant)).orElseThrow();
            h.assertTrue(camp.getSize().equals(new BlockPos(25,9,25)), "camp bounds do not bind");
            var teachers = faculty(h, camp);
            h.assertTrue(teachers.contains("hemomancy:circus_threadkeeper"), "camp lacks Threadkeeper: " + variant);
            checkTeachingCamp(h, camp, variant);
            covered.addAll(teachers);
        }
        for (String role : List.of("acrobat","knife_thrower","stilt_walker","strongman","beast_tamer","fire_eater","understudy"))
            h.assertTrue(covered.contains("hemomancy:circus_" + role), "no ordinary teaching camp for " + role);
        h.assertTrue(manager.get(Hemomancy.rloc("troupe_abandoned_stage")).isPresent(), "story stage template missing");
        h.succeed();
    }

    private static void checkTeachingCamp(GameTestHelper h, StructureTemplate camp, String variant) {
        var blocks = new HashMap<BlockPos, BlockState>();
        for (var block : ((VigilTemplateAccessor)camp).hemomancy$palettes().getFirst().blocks())
            blocks.put(block.pos(), block.state());
        for (int left : List.of(1, 15)) {
            h.assertTrue(blocks.get(new BlockPos(left + 4, 5, 5)).is(Blocks.RED_WOOL), "camp roof lost its peak: " + variant);
            h.assertTrue(blocks.get(new BlockPos(left, 1, 5)).is(Blocks.RED_WOOL), "camp roof lost its low eave: " + variant);
            h.assertTrue(blocks.get(new BlockPos(left + 4, 5, 3)).is(Blocks.WHITE_WOOL), "camp roof lost its ivory stripe: " + variant);
            for (int x = left + 3; x <= left + 5; x++)
                for (int y = 1; y <= 3; y++)
                    h.assertTrue(blocks.get(new BlockPos(x, y, 9)).isAir(), "camp entrance obstructed: " + variant);
        }
        h.assertTrue(BuiltInRegistries.BLOCK.getKey(blocks.get(new BlockPos(3, 1, 4)).getBlock())
                .equals(Hemomancy.rloc("puppeteers_spindle")), "camp lost its Spindle: " + variant);
        h.assertTrue(blocks.get(new BlockPos(4, 1, 4)).is(Blocks.CRAFTING_TABLE), "camp lost its crafting table: " + variant);
        for (Tag entry : camp.save(new CompoundTag()).getList("entities", Tag.TAG_COMPOUND)) {
            var pos = ((CompoundTag)entry).getList("blockPos", Tag.TAG_INT);
            var feet = new BlockPos(pos.getInt(0), pos.getInt(1), pos.getInt(2));
            h.assertTrue(blocks.get(feet).isAir() && blocks.get(feet.above()).isAir(), "camp faculty obstructed: " + variant);
        }
    }
    private static Set<String> faculty(GameTestHelper h, StructureTemplate template) {
        var roles = new HashSet<String>();
        for (Tag entry : template.save(new CompoundTag()).getList("entities", Tag.TAG_COMPOUND)) {
            String id = ((CompoundTag)entry).getCompound("nbt").getString("id");
            h.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(id)), "template references unknown actor: " + id);
            if (id.startsWith("hemomancy:circus_") && !id.equals("hemomancy:circus_carousel")) roles.add(id);
        }
        return roles;
    }
}
