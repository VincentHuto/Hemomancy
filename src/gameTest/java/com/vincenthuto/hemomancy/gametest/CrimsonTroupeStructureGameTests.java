package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.TroupePoleBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.TroupeWagonWheelBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
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
    public static void realTemplatesCoverEveryProfessor(GameTestHelper h) {
        var manager = h.getLevel().getStructureManager();
        var expanded = manager.get(Hemomancy.rloc("crimson_troupe_pavilion")).orElseThrow();
        h.assertTrue(expanded.getSize().equals(new BlockPos(81,23,65)), "new pavilion bounds do not bind");
        h.assertTrue(faculty(h, expanded).size() == 9, "pavilion lost a faculty role or original performer");
        // Chunk 15 T8: the big top's set pieces by role, from the site kit.
        var pieces = new HashSet<String>();
        for (var block : ((VigilTemplateAccessor)expanded).hemomancy$palettes().getFirst().blocks())
            pieces.add(BuiltInRegistries.BLOCK.getKey(block.state().getBlock()).getPath());
        for (String piece : List.of("circus_king_pole", "circus_quarter_pole", "circus_ring_curb", "circus_seating",
                "circus_gallery_rail", "circus_podium", "circus_trapeze", "circus_net", "circus_footlight",
                "circus_plinth", "circus_wagon_wheel", "circus_ticket_window", "hematic_lantern", "abocipher_emitter"))
            h.assertTrue(pieces.contains(piece), "the pavilion lacks its " + piece);
        var covered = new HashSet<String>();
        for (String variant : List.of("air","blades","pageantry","embers","memory")) {
            var camp = manager.get(Hemomancy.rloc("troupe_camp_" + variant)).orElseThrow();
            h.assertTrue(camp.getSize().equals(new BlockPos(27,12,27)), "camp bounds do not bind");
            var teachers = faculty(h, camp);
            h.assertTrue(teachers.contains("hemomancy:circus_threadkeeper"), "camp lacks Threadkeeper: " + variant);
            checkTeachingCamp(h, camp, variant);
            covered.addAll(teachers);
        }
        for (String role : List.of("acrobat","knife_thrower","stilt_walker","strongman","beast_tamer","fire_eater","understudy"))
            h.assertTrue(covered.contains("hemomancy:circus_" + role), "no ordinary teaching camp for " + role);
        var stage = manager.get(Hemomancy.rloc("troupe_abandoned_stage")).orElseThrow();
        h.assertTrue(stage.getSize().equals(new BlockPos(17,10,15)), "the stage is a small roadside wreck");
        h.assertTrue(pieces(stage).containsAll(List.of("circus_stage_record", "circus_wrecked_panel", "circus_wagon_wheel")),
                "the wreck lost its stage bill, burnt panels or wheels");
        h.succeed();
    }

    /** Chunk 15 T7: the kit sampler places whole, so every wheel, two-tall piece and pole survives its neighbours. */
    @GameTest(template = "circus_kit_sampler", timeoutTicks = 40)
    public static void kitSamplerStandsWhole(GameTestHelper h) {
        var sampler = h.getLevel().getStructureManager().get(ResourceLocation.fromNamespaceAndPath("crimson_troupe_validation", "circus_kit_sampler")).orElseThrow();
        h.runAfterDelay(2, () -> {
            var blocks = ((VigilTemplateAccessor) sampler).hemomancy$palettes().getFirst().blocks();
            // Where the harness set the template down, from its one king pole's foot.
            var foot = blocks.stream().filter(b -> b.state().is(BlockInit.circus_king_pole.get())
                    && b.state().getValue(TroupePoleBlock.SECTION) == TroupePoleBlock.Section.FOOT).findFirst().orElseThrow().pos();
            var lift = h.getBlockState(foot).is(BlockInit.circus_king_pole.get()) ? BlockPos.ZERO : new BlockPos(0, 1, 0);
            BlockPos wheel = null;
            for (var block : blocks) {
                if (!BuiltInRegistries.BLOCK.getKey(block.state().getBlock()).getPath().startsWith("circus_")) continue;
                var placed = h.getBlockState(block.pos().offset(lift));
                h.assertTrue(placed.getBlock() == block.state().getBlock(), "kit piece did not survive at "
                        + block.pos() + ": " + block.state() + " became " + placed);
                if (block.state().is(BlockInit.circus_wagon_wheel.get()) && block.state().getValue(TroupeWagonWheelBlock.QUARTER) == 3)
                    wheel = block.pos().offset(lift);
            }
            h.assertTrue(wheel != null, "the sampler has no wagon wheel");
            var top = wheel;
            var state = h.getBlockState(top);
            h.setBlock(top, Blocks.AIR);
            var facing = state.getValue(TroupeWagonWheelBlock.FACING);
            for (int q = 0; q < 3; q++) {
                var at = top.offset(TroupeWagonWheelBlock.offset(facing, 3, q));
                h.assertTrue(h.getBlockState(at).isAir(), "a broken wheel left quarter " + q + " standing");
            }
            h.succeed();
        });
    }

    /** Chunk 15 T9: each camp is built from the site kit round its discipline's apparatus. */
    private static void checkTeachingCamp(GameTestHelper h, StructureTemplate camp, String variant) {
        var blocks = new HashMap<BlockPos, BlockState>();
        for (var block : ((VigilTemplateAccessor)camp).hemomancy$palettes().getFirst().blocks())
            blocks.put(block.pos(), block.state());
        var pieces = pieces(camp);
        var apparatus = switch (variant) {
            case "air" -> List.of("circus_trapeze", "circus_net");
            case "blades" -> List.of("circus_knife_wheel", "circus_target_board");
            case "pageantry" -> List.of("circus_stilt_rack", "circus_weight_frame");
            case "embers" -> List.of("circus_fire_pit", "circus_bellows_stand", "circus_hound_hoop");
            default -> List.of("circus_prompt_table", "circus_quarters_record");
        };
        for (String piece : apparatus) h.assertTrue(pieces.contains(piece), variant + " camp lacks its " + piece);
        for (String piece : List.of("puppeteers_spindle", "circus_ring_curb", "circus_wagon_wheel", "circus_laced_canvas",
                "hematic_lantern", "crafting_table"))
            h.assertTrue(pieces.contains(piece), variant + " camp lacks its " + piece);
        for (Tag entry : camp.save(new CompoundTag()).getList("entities", Tag.TAG_COMPOUND)) {
            var pos = ((CompoundTag)entry).getList("blockPos", Tag.TAG_INT);
            var feet = new BlockPos(pos.getInt(0), pos.getInt(1), pos.getInt(2));
            h.assertTrue(blocks.get(feet).isAir() && blocks.get(feet.above()).isAir(), "camp faculty obstructed: " + variant);
        }
    }

    private static Set<String> pieces(StructureTemplate template) {
        var pieces = new HashSet<String>();
        for (var block : ((VigilTemplateAccessor)template).hemomancy$palettes().getFirst().blocks())
            pieces.add(BuiltInRegistries.BLOCK.getKey(block.state().getBlock()).getPath());
        return pieces;
    }
    private static Set<String> faculty(GameTestHelper h, StructureTemplate template) {
        var roles = new HashSet<String>();
        for (Tag entry : template.save(new CompoundTag()).getList("entities", Tag.TAG_COMPOUND)) {
            String id = ((CompoundTag)entry).getCompound("nbt").getString("id");
            h.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(id)), "template references unknown actor: " + id);
            if (id.startsWith("hemomancy:circus_") && !id.equals("hemomancy:impaled_procession")) roles.add(id);
        }
        return roles;
    }
}
