package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingIdentity;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.itemhandler.MorphlingJarItemHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;

/** Supplied state fixtures for repair contracts, separate from campaign acceptance. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HarbingerRepairGameTests {
    private static final net.minecraft.server.level.TicketType<UUID> HELPER_FIXTURE_TICKET =
            net.minecraft.server.level.TicketType.create("hemomancy_test_helper", UUID::compareTo, 200);

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void awakenedSigilRetainsItsProjectionBlocker(GameTestHelper helper) {
        var level = helper.getLevel();
        var actor = player(helper);
        var center = helper.absolutePos(new net.minecraft.core.BlockPos(12, 70, 12));
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
                level, Hemomancy.rloc("cardinal_rite/apotheos_rite"));
        var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(
                actor.getUUID(), center, recipe.getId(), 3600, 9, 7, false, 6, 32);
        for (int i = 0; i < 32; i++) rite.fillAnchor(i, 50);
        helper.assertTrue(rite.enterInscription(), "Paid fixture must enter inscription");
        var id = Hemomancy.rloc("bastion");
        var sigil = com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry.get(id);
        var socket = recipe.getCeremony().supportSockets().getFirst();
        helper.assertTrue(socket.suggestedSigil().equals("bastion"), "Fixture must target the first Bastion socket");
        var occupied = new java.util.HashSet<net.minecraft.core.BlockPos>();
        for (var anchor : recipe.getCeremony().anchors()) {
            occupied.add(new net.minecraft.core.BlockPos(anchor.x(), 0, anchor.z()));
        }
        var placement = com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules.resolveSupportPlacement(
                new net.minecraft.core.BlockPos(socket.x(), 0, socket.z()), sigil.nodes(), occupied);
        var node = sigil.nodes().getFirst();
        rite.setSigilProgress(id.toString(), sigil.nodes().size());
        rite.awakenSigil(id.toString());
        var saved = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level);
        saved.startRite(rite);
        var blood = HemoCapabilityAccess.requireBloodVolume(actor);
        blood.setActive(true);
        blood.setBloodVolume(2000);
        HemoCapabilityAccess.requireInitiatoryDegree(actor).setDegreeNumber(7);
        actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
        var floor = center.offset(placement.getX() + (int) Math.round(node.x()), socket.y(),
                placement.getZ() + (int) Math.round(node.z()));
        var backdrop = floor.offset(0, 1, 3);
        var oldFloor = level.getBlockState(floor);
        var oldBackdrop = level.getBlockState(backdrop);
        var lowerFloor = floor.below(2);
        var oldLowerFloor = level.getBlockState(lowerFloor);
        level.setBlock(floor, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(backdrop, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
        var aim = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTargetGeometry.sigilAimPoint(
                center, floor.above(), placement.getX(), placement.getZ(), node.x(), node.z());
        actor.setPos(aim.x, aim.y - actor.getEyeHeight(), aim.z - 2);
        actor.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, aim);
        try {
            var physical = com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting.pick(
                    level, actor, 5.5, true);
            helper.assertTrue(physical instanceof net.minecraft.world.phys.BlockHitResult hit
                            && hit.getBlockPos().equals(backdrop), "Ray must reach ordinary stone behind the completed marker");
            var handled = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler
                    .tryProject(level, actor, 5);
            helper.assertTrue(handled.handled() && handled.bloodSpent() == 0,
                    "A fully awakened sigil must still absorb held Projection without ordinary fallback: " + handled);
            for (int tick = 0; tick < 65; tick++) {
                com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem
                        .projectFromEntity(level, actor, 5, 100);
            }
            helper.assertTrue(blood.getBloodVolume() == 2000
                            && level.getBlockState(floor).is(net.minecraft.world.level.block.Blocks.STONE)
                            && level.getBlockState(backdrop).is(net.minecraft.world.level.block.Blocks.STONE)
                            && rite.getSigilProgress().get(id.toString()) == sigil.nodes().size(),
                    "Overholding the completed node must retain blood, terrain and completed progress");

            var tracing = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(
                    actor.getUUID(), center, recipe.getId(), 3600, 9, 7, false, 6, 32);
            for (int i = 0; i < 32; i++) tracing.fillAnchor(i, 50);
            helper.assertTrue(tracing.enterInscription(), "Broken-floor fixture must enter inscription");
            saved.startRite(tracing);
            level.setBlock(floor, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(lowerFloor, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
            var loweredSurface = com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules.surfaceAirPosition(
                    level, center.offset(0, socket.y(), 0),
                    placement.getX() + (int) Math.round(node.x()), placement.getZ() + (int) Math.round(node.z()));
            helper.assertTrue(loweredSurface.equals(lowerFloor.above()),
                    "A broken floor must place its visible marker above the lower stone");
            var loweredAim = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTargetGeometry.sigilAimPoint(
                    center, loweredSurface, placement.getX(), placement.getZ(), node.x(), node.z());
            actor.setPos(loweredAim.x, loweredAim.y - actor.getEyeHeight(), loweredAim.z - 2);
            actor.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, loweredAim);
            var partial = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler
                    .tryProject(level, actor, 5);
            helper.assertTrue(partial.handled() && partial.bloodSpent() == 5
                            && tracing.getSigilProgress().getOrDefault(id.toString(), 0) == 0
                            && tracing.getSigilProgress().getOrDefault("blood:" + id, 0) == 5,
                    "A lowered marker must accept partial payment without a false stroke: " + partial);
            level.setBlock(floor, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
            actor.setPos(aim.x, aim.y - actor.getEyeHeight(), aim.z - 2);
            actor.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, aim);
            var resumed = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler
                    .tryProject(level, actor, 45);
            helper.assertTrue(resumed.handled() && resumed.bloodSpent() == 45
                            && blood.getBloodVolume() == 1950
                            && tracing.getSigilProgress().getOrDefault(id.toString(), 0) == 1
                            && tracing.getSigilProgress().getOrDefault("blood:" + id, 0) == 0,
                    "Repairing the platform must retain partial payment and finish the same node: " + resumed);
            var paid = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler
                    .tryProject(level, actor, 5);
            helper.assertTrue(paid.handled() && paid.bloodSpent() == 0 && blood.getBloodVolume() == 1950
                            && tracing.getSigilProgress().getOrDefault(id.toString(), 0) == 1
                            && tracing.getInstability() == 0,
                    "The repaired paid marker must block held input without cost or false-stroke instability");
            saved.startRite(rite);
            actor.setPos(aim.x, aim.y + 2 - actor.getEyeHeight(), aim.z);
            actor.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, aim);
            helper.assertTrue(rite.sealAltar(), "Support lifetime fixture must seal");
            var ordealBlocker = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler
                    .tryProject(level, actor, 5);
            helper.assertTrue(ordealBlocker.handled() && ordealBlocker.bloodSpent() == 0,
                    "Awakened support must remain a zero-cost blocker during ordeal");
            for (int wave = 0; wave < 6; wave++) rite.completeWave();
            helper.assertTrue(rite.getPhase() == com.vincenthuto.hemomancy.common.rite.CardinalRitePhase.STILL_INTERVAL,
                    "Support lifetime fixture must reach its final still interval");
            var stillBlocker = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler
                    .tryProject(level, actor, 5);
            helper.assertTrue(stillBlocker.handled() && stillBlocker.bloodSpent() == 0,
                    "Awakened support must remain a zero-cost blocker during the still interval");
            rite.finishStillInterval();
            helper.assertTrue(rite.getPhase() == com.vincenthuto.hemomancy.common.rite.CardinalRitePhase.CULMINATION
                            && com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler
                                    .tryProject(level, actor, 5).allowsOrdinaryProjection()
                            && blood.getBloodVolume() == 1950,
                    "Culmination must release the old support target without taking blood");
            actor.setPos(center.getX() + 20.5, center.getY() + 1, center.getZ() + 20.5);
            actor.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, actor.getEyePosition().add(0, 0, -3));
            helper.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler
                            .tryProject(level, actor, 5).allowsOrdinaryProjection(),
                    "An unrelated miss must still permit ordinary Projection");
        } finally {
            saved.removeRite(actor.getUUID());
            level.setBlock(floor, oldFloor, 3);
            level.setBlock(backdrop, oldBackdrop, 3);
            level.setBlock(lowerFloor, oldLowerFloor, 3);
            actor.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void awakenedResponseBlocksOnlyItsCurrentWave(GameTestHelper helper) {
        var level = helper.getLevel();
        var actor = player(helper);
        var center = helper.absolutePos(new net.minecraft.core.BlockPos(12, 70, 12));
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
                level, Hemomancy.rloc("cardinal_rite/apotheos_rite"));
        var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(
                actor.getUUID(), center, recipe.getId(), 3600, 9, 7, false, 2, 32);
        for (int i = 0; i < 32; i++) rite.fillAnchor(i, 50);
        helper.assertTrue(rite.enterInscription(), "Response fixture must enter inscription");
        rite.getWaveDeck().addAll(java.util.List.of("response_sigil", "rogue_will"));
        helper.assertTrue(rite.sealAltar(true), "Response fixture must seal with between-wave intervals");
        var occupied = new java.util.HashSet<net.minecraft.core.BlockPos>();
        for (var anchor : recipe.getCeremony().anchors()) {
            occupied.add(new net.minecraft.core.BlockPos(anchor.x(), 0, anchor.z()));
        }
        for (var socket : recipe.getCeremony().supportSockets()) {
            var support = com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry.get(
                    Hemomancy.rloc(socket.suggestedSigil()));
            var placement = com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules.resolveSupportPlacement(
                    new net.minecraft.core.BlockPos(socket.x(), 0, socket.z()), support.nodes(), occupied);
            occupied.addAll(com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules
                    .footprint(placement, support.nodes()));
        }
        var id = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler.sigilForWave(rite);
        var sigil = com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry.get(id);
        var placement = com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules.resolveNearestPlacement(
                net.minecraft.core.BlockPos.ZERO, sigil.nodes(), occupied);
        var node = sigil.nodes().getFirst();
        var floor = center.offset(placement.getX() + (int) Math.round(node.x()), 0,
                placement.getZ() + (int) Math.round(node.z()));
        var oldFloor = level.getBlockState(floor);
        var saved = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level);
        var key = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler.responseProgressKey(0, id);
        rite.setSigilProgress(key, sigil.nodes().size());
        rite.awakenSigil(key);
        saved.startRite(rite);
        var blood = HemoCapabilityAccess.requireBloodVolume(actor);
        blood.setActive(true);
        blood.setBloodVolume(2000);
        HemoCapabilityAccess.requireInitiatoryDegree(actor).setDegreeNumber(7);
        actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
        try {
            level.setBlock(floor, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
            var aim = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTargetGeometry.sigilAimPoint(
                    center, floor.above(), placement.getX(), placement.getZ(), node.x(), node.z());
            actor.setPos(aim.x, aim.y + 2 - actor.getEyeHeight(), aim.z);
            actor.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, aim);
            var blocked = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler
                    .tryProject(level, actor, 5);
            helper.assertTrue(blocked.handled() && blocked.bloodSpent() == 0,
                    "An awakened current response must block without payment: " + blocked);
            for (int tick = 0; tick < 65; tick++) {
                com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem
                        .projectFromEntity(level, actor, 5, 100);
            }
            helper.assertTrue(blood.getBloodVolume() == 2000 && rite.getSigilProgress().get(key) == sigil.nodes().size()
                            && level.getBlockState(floor).is(net.minecraft.world.level.block.Blocks.STONE),
                    "Held Projection must preserve the awakened response, blood and underlying stone");
            rite.completeWave();
            helper.assertTrue(rite.getPhase() == com.vincenthuto.hemomancy.common.rite.CardinalRitePhase.STILL_INTERVAL
                            && com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler
                                    .tryProject(level, actor, 5).allowsOrdinaryProjection(),
                    "The ended response must release its old target during the interval");
            rite.finishStillInterval();
            helper.assertTrue(rite.getCurrentWave() == 1
                            && com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler
                                    .tryProject(level, actor, 5).allowsOrdinaryProjection()
                            && blood.getBloodVolume() == 2000 && rite.getSigilProgress().get(key) == sigil.nodes().size(),
                    "A later non-response wave must not reclaim the old completed response or alter its record");
        } finally {
            saved.removeRite(actor.getUUID());
            level.setBlock(floor, oldFloor, 3);
            actor.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void pomeEmpoweredVicarRetainsOrdinaryEndingServices(GameTestHelper helper) {
        var actor = player(helper);
        var vicar = EntityInit.harbinger_vicar.get().create(helper.getLevel());
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        HemoCapabilityAccess.requireBloodVolume(actor).setActive(true);
        try {
            for (var path : com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.values()) {
                degree.setDegreeNumber(path == com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.APOTHEOS ? 8 : 7);
                degree.setArchonPath(path);
                degree.setPomeEmpowermentExpiry(0);
                var baseline = vicar.progressionDialogue(actor);
                degree.setPomeEmpowermentExpiry(helper.getLevel().getGameTime() + 3600);
                var empowered = vicar.progressionDialogue(actor);
                helper.assertTrue(empowered.getStartNode().lines().contains("hemomancy.vicar.archon.pome_empowered.line2"),
                        "Pome empowerment lost its authored Vicar reaction for " + path);
                helper.assertTrue(empowered.getStartNode().options().containsAll(baseline.getStartNode().options()),
                        "Pome empowerment hid an ordinary Vicar service for " + path);
                for (var node : baseline.nodes().values()) {
                    if (!node.id().equals(baseline.startNodeId())) helper.assertTrue(
                            node.equals(empowered.getNode(node.id())),
                            "Pome empowerment replaced service node " + node.id() + " for " + path);
                }
                assertDialogueTargetsExist(helper, empowered);
                helper.assertTrue(degree.getArchonPath() == path
                                && degree.getDegreeNumber() == (path == com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.APOTHEOS ? 8 : 7),
                        "The Vicar reaction changed the player's ending or degree");
            }
        } finally {
            vicar.discard();
            actor.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void alchemistHeldJarKeepsCurrentDegreeLessons(GameTestHelper helper) {
        var jar = new com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees.HeldSpecimenJar(
                Hemomancy.rloc("morphling_polyp"), java.util.List.of(
                        com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer.BAT));
        for (int degree = 2; degree <= 8; degree++) {
            var baseline = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees
                    .forDegree(degree, 42, true, false);
            var held = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees
                    .forDegree(degree, 42, true, false, null, jar);
            helper.assertTrue(held.getStartNode().lines().equals(baseline.getStartNode().lines()),
                    "A held jar must retain the current greeting at D" + degree);
            helper.assertTrue(held.getStartNode().options().containsAll(baseline.getStartNode().options()),
                    "A held jar must retain all current lessons at D" + degree);
            helper.assertTrue(held.getStartNode().options().stream().anyMatch(option ->
                            "alchemist_bestiary_record".equals(option.eventId()))
                    && held.getStartNode().options().stream().anyMatch(option ->
                            "alchemist_bestiary_surrender_morphling_bat".equals(option.eventId())),
                    "Recording and layer-specific surrender must remain available at D" + degree);
            assertDialogueTargetsExist(helper, held);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void alchemistHeldFloraKeepsCurrentAndUnfinishedLessons(GameTestHelper helper) {
        var sample = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees
                .RedTaxonomySample.INFECTED_FUNGUS;
        for (int degree = 2; degree <= 8; degree++) {
            var baseline = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees
                    .forDegree(degree, 42, true, false, null, null, true, true, true, true);
            var held = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees
                    .forDegree(degree, 42, true, false, sample, null, true, true, true, true);
            helper.assertTrue(held.getStartNode().lines().equals(baseline.getStartNode().lines()),
                    "Held flora must retain the current greeting at D" + degree);
            helper.assertTrue(held.getStartNode().options().containsAll(baseline.getStartNode().options()),
                    "Held flora must retain current and unfinished lessons at D" + degree);
            helper.assertTrue(held.getStartNode().options().stream().anyMatch(option ->
                            sample.eventId().equals(option.eventId())),
                    "The actual sample submission must remain available at D" + degree);
            assertDialogueTargetsExist(helper, held);
        }
        helper.succeed();
    }

    private static void assertDialogueTargetsExist(GameTestHelper helper,
            com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree tree) {
        for (var node : tree.nodes().values()) {
            for (var option : node.options()) {
                helper.assertTrue(option.nextNodeId() == null || tree.getNode(option.nextNodeId()) != null,
                        "Merged dialogue must retain target " + option.nextNodeId());
            }
        }
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void firstCultureCreditsLanternOutputWithoutPriorCatalogue(GameTestHelper helper) {
        var actor = player(helper);
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        var level = helper.getLevel();
        level.setBlock(pos, com.vincenthuto.hemomancy.common.init.BlockInit.mycelial_lantern.get().defaultBlockState(), 3);
        try {
            var lantern = (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MycelialLanternBlockEntity)
                    level.getBlockEntity(pos);
            var enzyme = new ItemStack(ItemInit.vivacious_enzyme.get());
            helper.assertTrue(!com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter
                    .hasRecordedEnzyme(actor, enzyme), "Fixture must not already have an enzyme catalogue record");
            lantern.setItem(lantern.SLOT_OUTPUT, enzyme);
            var menu = new com.vincenthuto.hemomancy.common.menu.tile.crafting.MycelialLanternMenu(
                    0, actor.getInventory(), lantern, lantern.dataAccess);
            var taken = menu.getSlot(lantern.SLOT_OUTPUT).remove(1);
            menu.getSlot(lantern.SLOT_OUTPUT).onTake(actor, taken);
            helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter
                    .hasAdvancement(actor, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter
                            .ADV_FIRST_CULTURE_COMPLETE),
                    "Taking a valid Lantern output must credit First Culture without a catalogue prerequisite");
        } finally {
            actor.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void firstCultureCreditsShiftClickedLanternOutput(GameTestHelper helper) {
        var actor = player(helper);
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        var level = helper.getLevel();
        level.setBlock(pos, com.vincenthuto.hemomancy.common.init.BlockInit.mycelial_lantern.get().defaultBlockState(), 3);
        try {
            var lantern = (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MycelialLanternBlockEntity)
                    level.getBlockEntity(pos);
            lantern.setItem(lantern.SLOT_OUTPUT, new ItemStack(ItemInit.vivacious_enzyme.get()));
            var menu = new com.vincenthuto.hemomancy.common.menu.tile.crafting.MycelialLanternMenu(
                    0, actor.getInventory(), lantern, lantern.dataAccess);
            menu.quickMoveStack(actor, lantern.SLOT_OUTPUT);
            helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter
                    .hasAdvancement(actor, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter
                            .ADV_FIRST_CULTURE_COMPLETE),
                    "Shift-clicking a valid Lantern output must credit First Culture");
        } finally {
            actor.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void morphlingHandlingInspectsCapturedPolypWithoutIncubator(GameTestHelper helper) {
        var learner = player(helper);
        var priorResearcher = player(helper);
        var level = helper.getLevel();
        var alchemist = EntityInit.harbinger_alchemist.get().create(level);
        var vicar = EntityInit.harbinger_vicar.get().create(level);
        var polyp = EntityInit.morphling_polyp.get().create(level);
        alchemist.setPos(learner.position());
        vicar.setPos(learner.position());
        polyp.setPos(learner.position());
        polyp.setLayerMask(1);
        level.addFreshEntity(alchemist);
        level.addFreshEntity(vicar);
        level.addFreshEntity(polyp);
        HemoCapabilityAccess.requireInitiatoryDegree(learner).setDegreeNumber(4);
        HemoCapabilityAccess.requireBloodVolume(learner).setActive(true);
        HemoCapabilityAccess.requireInitiatoryDegree(priorResearcher).setDegreeNumber(4);
        HemoCapabilityAccess.requireBloodVolume(priorResearcher).setActive(true);
        try {
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingAssignment
                    .inspect(learner, alchemist), "No specimen proof must not complete handling");
            var otherJar = new ItemStack(com.vincenthuto.hemomancy.common.init.BlockInit.specimen_jar.get());
            var otherSpecimen = new net.minecraft.nbt.CompoundTag();
            otherSpecimen.putString("id", "hemomancy:chitinite");
            com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData
                    .setSpecimen(otherJar, otherSpecimen);
            learner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, otherJar);
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingAssignment
                    .inspect(learner, alchemist), "An ordinary ecology specimen is not Morphling handling proof");
            var emptyJar = new ItemStack(com.vincenthuto.hemomancy.common.init.BlockInit.specimen_jar.get());
            learner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, emptyJar);
            emptyJar.getItem().interactLivingEntity(emptyJar, learner, polyp,
                    net.minecraft.world.InteractionHand.MAIN_HAND);
            var captured = learner.getMainHandItem();
            helper.assertTrue(com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData
                    .MORPHLING_POLYP_ID.equals(com.vincenthuto.hemomancy.common.item.harbinger.tile.functional
                            .SpecimenJarData.getSpecimenEntityId(captured).orElseThrow().toString()),
                    "The supported jar interaction must actually capture the Polyp");
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingAssignment
                    .inspect(learner, vicar), "The Vicar cannot inspect Morphling handling");
            HemoCapabilityAccess.requireInitiatoryDegree(learner).setDegreeNumber(3);
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingAssignment
                    .inspect(learner, alchemist), "D3 capture must wait for the D4 Alchemist lesson");
            HemoCapabilityAccess.requireInitiatoryDegree(learner).setDegreeNumber(4);
            var before = captured.copy();
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingAssignment
                    .inspect(learner, alchemist), "The Alchemist should recognize the captured Polyp");
            helper.assertTrue(ItemStack.isSameItemSameComponents(before, learner.getMainHandItem()),
                    "Inspection must retain the captured Polyp and its specimen components");
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingAssignment
                    .inspect(learner, alchemist), "Handling inspection is one-time");
            learner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingAssignment
                    .progress(learner).inspected(), "Completed handling must survive jar removal");
            HemoCapabilityAccess.requireSpecimenBestiary(priorResearcher)
                    .recordSpecimen(Hemomancy.rloc("morphling_polyp"));
            alchemist.setPos(priorResearcher.position());
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingAssignment
                    .inspect(priorResearcher, alchemist), "Earlier Polyp Bestiary study should count without a second jar");
            var restored = player(helper);
            try {
                restored.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG,
                        learner.getPersistentData().getCompound(
                                net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG).copy());
                helper.assertTrue(com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingAssignment
                        .progress(restored).inspected(), "Handling completion must survive persisted restoration");
            } finally {
                restored.discard();
            }
        } finally {
            learner.discard(); priorResearcher.discard(); alchemist.discard(); vicar.discard(); polyp.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void oldConsumedPomeReconcilesOnlyItsOwnersMatchingPendingHusk(GameTestHelper helper) {
        var actor = player(helper); var stranger = player(helper);
        var level = helper.getLevel();
        var origin = helper.absolutePos(new net.minecraft.core.BlockPos(0, 48, 0));
        var blooms = com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData.get(level.getServer().overworld());
        var dimension = level.dimension().location().toString();
        level.setBlock(origin, com.vincenthuto.hemomancy.common.init.BlockInit.qliphoth_bloom.get().defaultBlockState(), 3);
        var bloom = new com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData.BloomEntry(actor.getUUID(), origin, dimension, 0, level.getGameTime());
        blooms.addBloom(bloom);
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(origin), net.minecraft.core.Direction.UP, origin, false);
        try {
            blooms.setPendingPome(bloom, 0); blooms.markPendingPomeClaimed(bloom);
            HemoCapabilityAccess.requireInitiatoryDegree(actor).recordPomeConsumed(bloom.bloomId(), origin.asLong());
            level.getBlockState(origin).useWithoutItem(level, stranger, hit);
            helper.assertTrue(blooms.getPendingPomeHuskIndex(bloom) == 0, "A stranger must not reconcile another tree");
            level.getBlockState(origin).useWithoutItem(level, actor, hit);
            helper.assertTrue(blooms.getPendingPomeHuskIndex(bloom) == -1, "Authoritative owner consumption must reconcile the old stuck pending husk");
            blooms.setPendingPome(bloom, 1); blooms.markPendingPomeClaimed(bloom);
            level.getBlockState(origin).useWithoutItem(level, actor, hit);
            helper.assertTrue(blooms.getPendingPomeHuskIndex(bloom) == 1, "The next uneaten husk must remain pending");
        } finally { blooms.removeBloomInChunk(origin, dimension); actor.discard(); stranger.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void duplicatePomeCannotAdvanceOrClearTheNextHusk(GameTestHelper helper) {
        var actor = player(helper);
        HemoCapabilityAccess.requireInitiatoryDegree(actor).setDegreeNumber(7);
        var origin = helper.absolutePos(new net.minecraft.core.BlockPos(0, 45, 0));
        var blooms = com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData.get(helper.getLevel().getServer().overworld());
        var bloom = new com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData.BloomEntry(
                actor.getUUID(), origin, helper.getLevel().dimension().location().toString(), 0,
                helper.getLevel().getGameTime());
        blooms.addBloom(bloom);
        var fruit = com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem.createPickedPomeStack(bloom, 0);
        var duplicate = fruit.copy();
        try {
            blooms.setPendingPome(bloom, 0); blooms.markPendingPomeClaimed(bloom);
            fruit.getItem().finishUsingItem(fruit, helper.getLevel(), actor);
            helper.assertTrue(HemoCapabilityAccess.requireInitiatoryDegree(actor).getTotalPomesConsumed() == 1, "First husk must count");
            blooms.setPendingPome(bloom, 1); blooms.markPendingPomeClaimed(bloom);
            duplicate.getItem().finishUsingItem(duplicate, helper.getLevel(), actor);
            helper.assertTrue(HemoCapabilityAccess.requireInitiatoryDegree(actor).getTotalPomesConsumed() == 1,
                    "Duplicate husk must not advance communion");
            helper.assertTrue(blooms.getPendingPomeHuskIndex(bloom) == 1 && duplicate.getCount() == 1,
                    "Rejected duplicate must preserve itself and the next pending husk");
            var next = com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem.createPickedPomeStack(bloom, 1);
            next.getItem().finishUsingItem(next, helper.getLevel(), actor);
            helper.assertTrue(HemoCapabilityAccess.requireInitiatoryDegree(actor).getTotalPomesConsumed() == 2
                    && blooms.getPendingPomeHuskIndex(bloom) == -1, "The actual next husk must still advance and clear itself");
            var later = com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem.createPickedPomeStack(bloom, 3);
            later.getItem().finishUsingItem(later, helper.getLevel(), actor);
            helper.assertTrue(later.getCount() == 1 && HemoCapabilityAccess.requireInitiatoryDegree(actor).getTotalPomesConsumed() == 2,
                    "An out-of-order husk must wait for its preceding fruit");
        } finally { blooms.removeBloomInChunk(origin, bloom.dimension()); actor.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void noeticLessonRejectsReturningToItsOriginalBuild(GameTestHelper helper) {
        var actor = player(helper);
        var first = java.util.List.of(Hemomancy.rloc("scar_heart"));
        var second = java.util.List.of(Hemomancy.rloc("scar_marrow"));
        var scars = HemoCapabilityAccess.getScarState(actor).orElseThrow();
        try {
            var mission = com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.D6_COUNSEL;
            com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.grant(actor, mission);
            scars.activateCerebralScar(first.getFirst());
            com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.onMatchingNoeticCast(actor);
            scars.deactivateCerebralScar(first.getFirst()); scars.activateCerebralScar(second.getFirst());
            com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.onChangedLoadout(actor, first, second);
            scars.deactivateCerebralScar(second.getFirst()); scars.activateCerebralScar(first.getFirst());
            com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.onChangedLoadout(actor, second, first);
            com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.onMatchingNoeticCast(actor);
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.has(actor,
                    com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.D6_SECOND_ROUTE),
                    "Returning to the original scar build must not count as the second route");
            scars.deactivateCerebralScar(first.getFirst()); scars.activateCerebralScar(second.getFirst());
            com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.onChangedLoadout(actor, first, second);
            com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.onMatchingNoeticCast(actor);
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.has(actor,
                    com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.D6_SECOND_ROUTE),
                    "A different active scar build must still count as the second route");
        } finally { actor.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void secondPlayerCannotClaimAnAlreadySpentTemple(GameTestHelper helper) {
        var first = player(helper); var second = player(helper);
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(0, 5, 0));
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, com.vincenthuto.hemomancy.common.init.BlockInit.mortal_display.get().defaultBlockState());
        var display = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.MortalDisplayBlockEntity) level.getBlockEntity(pos);
        var hermit = java.util.UUID.randomUUID(); display.linkHermit(hermit);
        HemoCapabilityAccess.requireInitiatoryDegree(first).setDegreeNumber(0);
        HemoCapabilityAccess.requireInitiatoryDegree(second).setDegreeNumber(0);
        HemoCapabilityAccess.getBloodVolume(first).orElseThrow().setActive(false);
        HemoCapabilityAccess.getEquipment(first).orElseThrow().setStackInSlot(5, ItemStack.EMPTY);
        com.vincenthuto.hemomancy.common.rite.TempleOathRules.bless(first, hermit);
        com.vincenthuto.hemomancy.common.rite.TempleOathRules.bless(second, hermit);
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
        try {
            level.getBlockState(pos).useWithoutItem(level, first, hit);
            level.getBlockState(pos).useWithoutItem(level, second, hit);
            helper.assertTrue(display.isClaimedBy(first.getUUID()), "Temple did not retain its first heir");
            helper.assertTrue(!com.vincenthuto.hemomancy.common.rite.TempleOathRules.hasClaimedHeartFrom(second, hermit), "Second player claimed spent heart");
        } finally { first.discard(); second.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void unansweredLensCannotTimeOutIntoSuccess(GameTestHelper helper) {
        assertWaveDeadline(helper, "discover_lens", true);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void livingFalseOmenCannotTimeOutIntoSuccess(GameTestHelper helper) {
        assertWaveDeadline(helper, "false_omens", true);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void ordinaryDefensiveWaveStillAllowsSurvival(GameTestHelper helper) {
        assertWaveDeadline(helper, "fargone_dive", false);
    }

    private static void assertWaveDeadline(GameTestHelper helper, String wave, boolean requiredObjective) {
        assertWaveDeadline(helper, wave, requiredObjective, false);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void answeredLensStillCompletesAtTheDeadline(GameTestHelper helper) {
        assertWaveDeadline(helper, "discover_lens", false, true);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void defeatedFalseOmensStillCompleteAtTheDeadline(GameTestHelper helper) {
        assertWaveDeadline(helper, "false_omens", false, true);
    }

    private static void assertWaveDeadline(GameTestHelper helper, String wave, boolean requiredObjective, boolean fulfilled) {
        var actor = player(helper);
        var level = helper.getLevel();
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(level, Hemomancy.rloc("cardinal_rite/chamber_of_will"));
        var center = helper.absolutePos(new net.minecraft.core.BlockPos(0, 35, 0));
        var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(actor.getUUID(), center, recipe.getId(), 1000, 7, 6, false, 1, 8);
        rite.getWaveDeck().add(wave);
        for (int i = 0; i < 8; i++) rite.fillAnchor(i, 50);
        var threat = net.minecraft.world.entity.EntityType.COW.create(level);
        threat.setNoAi(true);
        threat.moveTo(center.getX() + 12, center.getY() + 1, center.getZ());
        level.addFreshEntity(threat);
        rite.addRiteThreat(threat.getUUID());
        if (fulfilled) {
            if (wave.equals("discover_lens")) {
                rite.setSigilProgress("hemomancy:lens", com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry.get(Hemomancy.rloc("lens")).nodes().size());
            } else threat.setHealth(0);
        }
        var tag = rite.serialize(); tag.putString("Phase", "ORDEAL"); tag.putInt("PhaseTicks", 359);
        rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.deserialize(tag);
        try {
            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine.tick(level, actor, rite, recipe);
            // The Chamber's storm can debit an outer anchor on the deadline tick.
            for (int i = 0; i < 8; i++) rite.fillAnchor(i, 50);
            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine.tick(level, actor, rite, recipe);
            helper.assertTrue(requiredObjective ? rite.getPhase() == com.vincenthuto.hemomancy.common.rite.CardinalRitePhase.COLLAPSED
                    && rite.getCurrentWave() == 0 : rite.getCurrentWave() == 1,
                    "Deadline must enforce the authored objective for " + wave + ", rather than award an unanswered required wave");
        } finally { threat.discard(); actor.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void armatureCenterDoesNotSuffocateItsRestrainedPlayer(GameTestHelper helper) {
        var player = player(helper);
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(0, 40, 0));
        var block = (com.vincenthuto.hemomancy.common.block.harbinger.crafting.HematicArmatureBlock)
                com.vincenthuto.hemomancy.common.init.BlockInit.hematic_armature.get();
        helper.getLevel().setBlock(pos, block.defaultBlockState(), 3);
        block.placeFillers(helper.getLevel(), pos, block.defaultBlockState());
        player.setPos(pos.getX() + .5, pos.getY() + .05, pos.getZ() + .54);
        block.stepOn(helper.getLevel(), pos, block.defaultBlockState(), player);
        try {
            helper.assertTrue(player.isPassenger(), "Actual Armature entry must restrain the supplied player");
            helper.assertTrue(!player.isInWall(), "Idle restraint must not put the player's eyes inside a suffocating filler");
            helper.assertTrue(helper.getLevel().getBlockState(pos.above()).getCollisionShape(helper.getLevel(), pos.above()).isEmpty(),
                    "Center filler collision must use its linked Armature state");
            helper.assertTrue(!helper.getLevel().getBlockState(pos.above(3)).getCollisionShape(helper.getLevel(), pos.above(3)).isEmpty(),
                    "The solid top arch must retain collision");
        } finally {
            var restraint = player.getVehicle();
            player.stopRiding();
            if (restraint != null) restraint.discard();
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair", timeoutTicks = 240)
    public static void actualInitiationCompletionAwardsStarterBloodOnlyOnce(GameTestHelper helper) throws Exception {
        initiationSupply(helper, false);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair", timeoutTicks = 240)
    public static void fullInventoryInitiationDropsStarterBloodOnlyOnce(GameTestHelper helper) throws Exception {
        initiationSupply(helper, true);
    }

    private static void initiationSupply(GameTestHelper helper, boolean fullInventory) {
        var player = player(helper);
        HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(0);
        HemoCapabilityAccess.getEquipment(player).orElseThrow().setStackInSlot(5, new ItemStack(ItemInit.charm_of_vascularium.get()));
        com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.activate(player);
        if (fullInventory) for (int slot = 0; slot < 36; slot++) player.getInventory().setItem(slot, new ItemStack(net.minecraft.world.item.Items.BARRIER, 64));
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(0, 5, 0));
        player.setPos(net.minecraft.world.phys.Vec3.atCenterOf(pos));
        var vicar = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_vicar.get().create(helper.getLevel());
        vicar.setPos(player.position().add(1, 0, 0)); vicar.setNoGravity(true); helper.getLevel().addFreshEntity(vicar);
        helper.assertTrue(com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.begin(player, vicar), "Vicar ceremony rejected");
        helper.runAfterDelay(202, () -> {
            com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
            int drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(4), e -> e.getItem().is(ItemInit.bloody_flask.get()))
                    .stream().mapToInt(e -> e.getItem().getCount()).sum();
            helper.assertTrue(player.getInventory().countItem(ItemInit.bloody_flask.get()) + drops == 4, "Starter blood missing or duplicated");
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.begin(player, vicar), "Ceremony repeated rewards");
            player.discard(); vicar.discard(); helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void attendantRejectsInsufficientPaymentWithoutSpending(GameTestHelper helper) {
        var fixture = new AllyPaymentFixture(helper, com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.ATTENDANT);
        fixture.verifyAfterChunkActivation(() -> {
            fixture.line.setBloodVolume(20);
            fixture.data.drawNpcRiteReserve(fixture.line.getBloodlineUUID(), fixture.ally.getUUID(), 975, helper.getLevel().getGameTime());
            helper.assertTrue(!com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService.tryCorrectMiss(helper.getLevel(), fixture.rite),
                    "An Attendant with only 45 ml cannot buy a 50 ml correction");
            helper.assertTrue(fixture.line.getBloodVolume() == 20 && fixture.reserve() == 25,
                    "Rejected correction must retain both shared blood and private reserve");
            fixture.line.setBloodVolume(25);
            helper.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService.tryCorrectMiss(helper.getLevel(), fixture.rite),
                    "The exact combined 50 ml must buy one correction");
            helper.assertTrue(fixture.line.getBloodVolume() == 0 && fixture.reserve() == 0,
                    "Successful correction must debit exactly its 50 ml cost");
            helper.assertTrue(!com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService.tryCorrectMiss(helper.getLevel(), fixture.rite),
                    "One Attendant cannot correct a second miss in the same rite");
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void wardenPaysOnlyForALivingRiteThreat(GameTestHelper helper) {
        var fixture = new AllyPaymentFixture(helper, com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.WARDEN);
        fixture.verifyAfterChunkActivation(() -> {
            fixture.rite.addRiteThreat(UUID.randomUUID()); // Unloaded/expired threat reference.
            fixture.tickAt(100);
            helper.assertTrue(fixture.reserve() == 1000,
                    "Warden must not spend blood without an eligible threat");
            var threat = net.minecraft.world.entity.EntityType.COW.create(helper.getLevel());
            threat.moveTo(fixture.rite.getCenterPos().getX(), fixture.rite.getCenterPos().getY() + 1, fixture.rite.getCenterPos().getZ());
            helper.getLevel().addFreshEntity(threat);
            try {
                fixture.rite.addRiteThreat(threat.getUUID());
                fixture.tickAt(200);
                var effect = threat.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
                helper.assertTrue(effect != null && effect.getAmplifier() == 3 && fixture.reserve() == 975,
                        "A living rite threat receives the authored slow for exactly 25 ml");
            } finally { threat.discard(); }
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void helperPaymentDoesNotRoundFractionalPoolIntoFreeBlood(GameTestHelper helper) {
        var fixture = new AllyPaymentFixture(helper, com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.ANCHOR);
        fixture.verifyAfterChunkActivation(() -> {
            fixture.line.setBloodVolume(0.5F);
            int drawn = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService.spend(
                    helper.getLevel(), fixture.rite, fixture.ally.getUUID(), 1);
            helper.assertTrue(drawn == 1 && fixture.line.getBloodVolume() == 0.5F && fixture.reserve() == 999,
                    "A whole-ml transfer must retain a fractional shared remainder and debit the private ml");
        });
    }

    private static final class AllyPaymentFixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer actor;
        final net.minecraft.world.entity.Mob ally;
        final com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline line;
        final com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData data;
        final com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe recipe;
        final net.minecraft.world.level.ChunkPos stationChunk;
        com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite rite;

        AllyPaymentFixture(GameTestHelper helper, com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole role) {
            this.helper = helper;
            actor = player(helper);
            var level = helper.getLevel();
            recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(level, Hemomancy.rloc("cardinal_rite/chamber_of_will"));
            var center = helper.absolutePos(new net.minecraft.core.BlockPos(12, 30, 12));
            rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(actor.getUUID(), center, recipe.getId(), 1000, 7, 6, false, 1, 8);
            line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline("payment-fixture", actor.getUUID(), UUID.randomUUID(), new java.util.ArrayList<>());
            data = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData.get(level.getServer().overworld());
            data.registerBloodline(line);
            var station = center.offset(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService.markers(recipe).get(role));
            stationChunk = new net.minecraft.world.level.ChunkPos(station);
            level.getChunkSource().addRegionTicket(HELPER_FIXTURE_TICKET, stationChunk, 2, actor.getUUID(), true);
            level.setBlock(station.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
            ally = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_vicar.get().create(level);
            ally.setNoAi(true); // Effect/payment fixture only, not navigation acceptance.
            ally.moveTo(station.getX() + 0.5, station.getY(), station.getZ() + 0.5);
            level.addFreshEntity(ally);
            data.addNpcMember(line.getBloodlineUUID(), ally.getUUID());
            rite.assignAlly(ally.getUUID(), role);
        }

        void verifyAfterChunkActivation(Runnable checks) {
            cleanupOnFailure(helper, this::close);
            // The helper station can lie outside the vanilla template's ticking chunks.
            helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                    helper.getLevel().areEntitiesLoaded(stationChunk.toLong()),
                    "The supplied helper station chunk must become entity-loaded"))
                    .thenExecute(() -> {
                        try (this) {
                            assertAvailableHelper(helper, rite, ally);
                            checks.run();
                        }
                    }).thenSucceed();
        }

        int reserve() { return line.getNpcRiteReserve(ally.getUUID(), helper.getLevel().getGameTime()); }

        void tickAt(int tick) {
            var saved = rite.serialize();
            saved.putString("Phase", "ORDEAL");
            saved.putInt("PhaseTicks", tick - 1);
            rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.deserialize(saved);
            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine.tick(helper.getLevel(), actor, rite, recipe);
        }

        public void close() {
            ally.discard(); actor.discard(); data.disbandBloodline(line.getBloodlineUUID());
            helper.getLevel().getChunkSource().removeRegionTicket(HELPER_FIXTURE_TICKET, stationChunk, 2, actor.getUUID(), true);
        }
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void daemonFinaleWaitsForSavedEntitiesBeforeAdvancing(GameTestHelper helper) {
        var level = helper.getLevel();
        var actor = player(helper);
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
                level, Hemomancy.rloc("cardinal_rite/chamber_of_will"));
        var center = new net.minecraft.core.BlockPos(25000000, 100, 25000000);
        helper.assertTrue(!level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(center)),
                "The supplied restart fixture must have unavailable saved entities");
        for (String phase : new String[] {"OFFERING_PROCESSION", "CULMINATION"}) {
            var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(
                    actor.getUUID(), center, recipe.getId(), 1000, 7, 6, false, 1, 8);
            var saved = rite.serialize();
            saved.putString("Phase", phase);
            saved.putInt("PhaseTicks", 19);
            rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.deserialize(saved);
            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine.tick(level, actor, rite, recipe);
            helper.assertTrue(rite.getPhase().name().equals(phase) && rite.getPhaseTicks() == 19,
                    phase + " must wait for saved entities without collapsing or spending finale time");
        }
        actor.discard();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void threeAnchorHelpersRepairTwoActualRings(GameTestHelper helper) {
        var level = helper.getLevel();
        var actor = player(helper);
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
                level, Hemomancy.rloc("cardinal_rite/chamber_of_will"));
        var center = helper.absolutePos(new net.minecraft.core.BlockPos(12, 30, 12));
        var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(
                actor.getUUID(), center, recipe.getId(), 1000, 7, 6, false, 1, 8);
        var saved = rite.serialize();
        saved.putString("Phase", "ORDEAL");
        saved.putInt("PhaseTicks", 19);
        rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.deserialize(saved);
        var line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline(
                "helper-ring-fixture", actor.getUUID(), UUID.randomUUID(), new java.util.ArrayList<>());
        var data = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData.get(level.getServer().overworld());
        data.registerBloodline(line);
        var role = com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.ANCHOR;
        var station = center.offset(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService.markers(recipe).get(role));
        var stationChunk = new net.minecraft.world.level.ChunkPos(station);
        level.getChunkSource().addRegionTicket(HELPER_FIXTURE_TICKET, stationChunk, 2, actor.getUUID(), true);
        level.setBlock(station.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
        var allies = new java.util.ArrayList<net.minecraft.world.entity.Mob>();
        Runnable cleanup = () -> {
            allies.forEach(net.minecraft.world.entity.Entity::discard);
            data.disbandBloodline(line.getBloodlineUUID());
            actor.discard();
            level.getChunkSource().removeRegionTicket(HELPER_FIXTURE_TICKET, stationChunk, 2, actor.getUUID(), true);
        };
        cleanupOnFailure(helper, cleanup);
        for (int i = 0; i < 3; i++) {
            var ally = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_vicar.get().create(level);
            ally.setNoAi(true); // Supplied effect fixture; navigation is validated in the live campaign.
            ally.moveTo(station.getX() + 1.5, station.getY(), station.getZ() - 1.5 + i * 1.5);
            level.addFreshEntity(ally);
            data.addNpcMember(line.getBloodlineUUID(), ally.getUUID());
            rite.assignAlly(ally.getUUID(), role);
            allies.add(ally);
        }
        var activeRite = rite;
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                level.areEntitiesLoaded(stationChunk.toLong()),
                "The supplied Anchor station chunk must become entity-loaded"))
                .thenExecute(() -> {
                    try {
                        for (var ally : allies) assertAvailableHelper(helper, activeRite, ally);
                        com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine.tick(level, actor, activeRite, recipe);
                        helper.assertTrue(java.util.Arrays.stream(activeRite.getAnchorBloodMl()).sum() == 30,
                                "Three available Anchor helpers must each supply 10 ml across the two real rings");
                        helper.assertTrue(allies.stream().mapToInt(a -> line.getNpcRiteReserve(a.getUUID(), level.getGameTime())).sum() == 2970,
                                "Only the 30 ml actually delivered may leave private reserves");
                    } finally {
                        cleanup.run();
                    }
                }).thenSucceed();
    }

    private static void cleanupOnFailure(GameTestHelper helper, Runnable cleanup) {
        helper.testInfo.addListener(new net.minecraft.gametest.framework.GameTestListener() {
            public void testStructureLoaded(net.minecraft.gametest.framework.GameTestInfo test) { }
            public void testPassed(net.minecraft.gametest.framework.GameTestInfo test,
                    net.minecraft.gametest.framework.GameTestRunner runner) { }
            public void testFailed(net.minecraft.gametest.framework.GameTestInfo test,
                    net.minecraft.gametest.framework.GameTestRunner runner) {
                cleanup.run();
            }
            public void testAddedForRerun(net.minecraft.gametest.framework.GameTestInfo oldTest,
                    net.minecraft.gametest.framework.GameTestInfo newTest,
                    net.minecraft.gametest.framework.GameTestRunner runner) { }
        });
    }

    private static void assertAvailableHelper(GameTestHelper helper,
            com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite rite, net.minecraft.world.entity.Mob ally) {
        var level = helper.getLevel();
        if (com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .isAvailable(level, rite, ally.getUUID())) return;
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
        var role = rite.getAllyRoles().get(ally.getUUID());
        var station = rite.getCenterPos().offset(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .markers(recipe).get(role));
        var bounds = ally.getBoundingBox().move(station.getX() + 0.5 - ally.getX(),
                station.getY() - ally.getY(), station.getZ() + 0.5 - ally.getZ());
        var line = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData
                .get(level.getServer().overworld()).getBloodlineForPlayer(rite.getPlayerUUID());
        helper.fail("Supplied helper unavailable: role=" + role + ", station=" + station
                + ", indexed=" + (level.getEntity(ally.getUUID()) == ally)
                + ", entitiesLoaded=" + level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(station))
                + ", npcMember=" + (line != null && line.hasNpcMember(ally.getUUID()))
                + ", alive=" + ally.isAlive() + ", bloodspent=" + (line != null && line.isNpcBloodspent(ally.getUUID(), level.getGameTime()))
                + ", floor=" + level.getBlockState(station.below())
                + ", blockCollision=" + level.getBlockCollisions(ally, bounds).iterator().hasNext()
                + ", entityCollisions=" + level.getEntityCollisions(ally, bounds).size()
                + ", position=" + ally.position());
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void helperStationsClearActualChamberAnchors(GameTestHelper helper) {
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
                helper.getLevel(), Hemomancy.rloc("cardinal_rite/chamber_of_will"));
        var markers = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService.markers(recipe);
        for (var marker : markers.entrySet()) {
            for (var anchor : recipe.getCeremony().anchors()) {
                double dx = marker.getValue().getX() - anchor.x();
                double dz = marker.getValue().getZ() - anchor.z();
                helper.assertTrue(dx * dx + dz * dz >= 4,
                        marker.getKey() + " station overlaps an actual Chamber repair target " + anchor.offset());
            }
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void equippedBondWritesBackToOnlyItsOwnSpecimen(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack original = new ItemStack(ItemInit.morphling_bootlace.get());
        MorphlingIdentity.ensureIdentity(original);
        ItemStack sameStrain = new ItemStack(ItemInit.morphling_bootlace.get());
        MorphlingIdentity.ensureIdentity(sameStrain);
        ItemStack jar = jar(original, sameStrain);
        player.getInventory().setItem(0, jar);
        ItemStack equipped = original.copy();
        MorphlingItem.recordBondingBlood(equipped, 50);
        HemoCapabilityAccess.getEquippedMorphling(player).orElseThrow().setEquippedMorphling(equipped);
        EquippedMorphlingEvents.persistEquippedMorphling(player);
        MorphlingJarItemHandler stored = handler(jar);
        helper.assertTrue(ItemStack.isSameItemSameComponents(stored.getStackInSlot(0), equipped),
                "The owning slot must retain the authoritative earned components");
        helper.assertTrue(ItemStack.isSameItemSameComponents(stored.getStackInSlot(1), sameStrain),
                "Another specimen of the same strain must remain unchanged");
        player.discard();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void duplicatedIdentityDoesNotSpreadEarnedBond(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack original = new ItemStack(ItemInit.morphling_bootlace.get());
        MorphlingIdentity.ensureIdentity(original);
        ItemStack jar = jar(original, original.copy());
        player.getInventory().setItem(0, jar);
        ItemStack equipped = original.copy();
        MorphlingItem.recordBondingBlood(equipped, 50);
        HemoCapabilityAccess.getEquippedMorphling(player).orElseThrow().setEquippedMorphling(equipped);
        EquippedMorphlingEvents.persistEquippedMorphling(player);
        for (int slot = 0; slot < 2; slot++) {
            helper.assertTrue(ItemStack.isSameItemSameComponents(handler(jar).getStackInSlot(slot), original),
                    "Ambiguous duplicate identity must not receive guessed progress");
        }
        player.discard();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void staffPrimalChargesOnceAndRejectsCooldownWithoutWear(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack creature = new ItemStack(ItemInit.morphling_bootlace.get());
        MorphlingIdentity.ensureIdentity(creature);
        MorphlingItem.setPrimalized(creature); // Stage fixture; this is not earned growth acceptance.
        MorphlingItem.setLastAbilityTick(creature, "Primal:WebOfRedThread", -10000);
        player.getInventory().setItem(1, jar(creature));
        HemoCapabilityAccess.getEquippedMorphling(player).orElseThrow().setEquippedMorphling(creature.copy());
        var blood = HemoCapabilityAccess.getBloodVolume(player).orElseThrow();
        blood.setActive(true);
        blood.setMaxBloodVolume(5000);
        blood.setBloodVolume(1000);
        ItemStack staff = new ItemStack(ItemInit.living_staff.get());
        // The normal Staff has no durability component. Supply one to exercise conditional wear.
        staff.set(DataComponents.MAX_DAMAGE, 100);
        staff.set(DataComponents.DAMAGE, 0);
        player.getInventory().setItem(0, staff);
        staff.getItem().releaseUsing(staff, helper.getLevel(), player, 35980);
        helper.assertTrue(blood.getBloodVolume() == 750, "Bootlace must charge its authored 250 blood exactly once");
        helper.assertTrue(staff.getDamageValue() == 1, "Successful activation must wear the triggering Staff once");
        staff.getItem().releaseUsing(staff, helper.getLevel(), player, 35980);
        helper.assertTrue(blood.getBloodVolume() == 750 && staff.getDamageValue() == 1,
                "Cooldown rejection must consume neither blood nor Staff durability");
        player.discard();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void selectedLoomRecipeSurvivesStockAndChargedReload(GameTestHelper helper) {
        var pos = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(pos, com.vincenthuto.hemomancy.common.init.BlockInit.somatic_loom.get());
        var loom = (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity) helper.getBlockEntity(pos);
        loom.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        loom.addItem(null, new ItemStack(ItemInit.bleeding_bulb.get()), null);
        loom.addItem(null, new ItemStack(ItemInit.vivacious_enzyme.get()), null);
        var player = player(helper);
        helper.assertTrue(loom.selectRecipe(player, net.minecraft.resources.ResourceLocation.parse(
                "hemomancy:memory_weaving/memory_blood_shot")), "Explicit Blood Shot selection must succeed");
        for (int i = 0; i < 19; i++) loom.addItem(null, new ItemStack(ItemInit.vivacious_enzyme.get()), null);
        helper.assertTrue(loom.getResultItem().is(ItemInit.memory_blood_shot.get()), "Stocking 20 enzymes must not change the selected output");
        var blood = HemoCapabilityAccess.getBloodVolume(player).orElseThrow();
        blood.setActive(true); blood.setBloodVolume(1000);
        helper.assertTrue(loom.tryChargeRitualBlood(player, 50, true), "Selected weave must accept its authored blood charge");
        var saved = loom.saveWithoutMetadata(helper.getLevel().registryAccess());
        loom.loadWithComponents(saved, helper.getLevel().registryAccess());
        loom.refreshRecipe();
        helper.assertTrue(loom.isWeavingOrbs() && loom.getCurRecipe() != null && loom.getRitualBloodCharged() == 50,
                "Charged reload must restore the recipe without resetting or charging again");
        helper.assertTrue(loom.getStoredEnzyme(com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.ANIMUS) == 20,
                "Reload must preserve unspent enzymes");
        var missing = saved.copy();
        missing.putString("selectedRecipeId", "hemomancy:memory_weaving/removed_fixture");
        loom.loadWithComponents(missing, helper.getLevel().registryAccess());
        loom.refreshRecipe();
        helper.assertTrue(loom.getCurRecipe() == null && loom.isWeavingOrbs() && loom.getRitualBloodCharged() == 50,
                "Missing recipe must pause paid state, without a guessed output or refund");
        var changed = saved.copy();
        changed.getCompound("committedRecipe").putDouble("blood", 51);
        loom.loadWithComponents(changed, helper.getLevel().registryAccess());
        loom.refreshRecipe();
        helper.assertTrue(loom.getCurRecipe() == null && loom.isWeavingOrbs(), "Changed paid commitment must pause");
        loom.loadWithComponents(saved, helper.getLevel().registryAccess());
        loom.refreshRecipe();
        helper.assertTrue(loom.getCurRecipe() != null && blood.getBloodVolume() == 950, "Restoring the recipe must not charge a second time");
        player.discard(); helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void emptyHandCanCyclePastUnavailableStaffForm(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        var known = HemoCapabilityAccess.getKnownManipulations(player).orElseThrow();
        var shot = com.vincenthuto.hemomancy.common.init.ManipulationInit.getByName("blood_shot");
        var blade = com.vincenthuto.hemomancy.common.init.ManipulationInit.getByName("conjure_blade");
        com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.learnAndEquipIfPossible(known, shot, 10);
        com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.learnAndEquipIfPossible(known, blade, 10);
        known.setEquippedManipNames(java.util.List.of("blood_shot", "conjure_blade"));
        known.setSelectedManip(shot);
        var context = (net.neoforged.neoforge.network.handling.IPayloadContext) java.lang.reflect.Proxy.newProxyInstance(
                HarbingerRepairGameTests.class.getClassLoader(),
                new Class<?>[]{net.neoforged.neoforge.network.handling.IPayloadContext.class}, (proxy, method, args) -> {
                    if (method.getName().equals("player")) return player;
                    if (method.getName().equals("enqueueWork")) {
                        ((Runnable) args[0]).run();
                        return java.util.concurrent.CompletableFuture.completedFuture(null);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        var packet = new com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ChangeSelectedManipPacket(0);
        com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ChangeSelectedManipPacket.handle(packet, context);
        helper.assertTrue(known.getSelectedManip() == blade, "Unavailable weapon form must still become the selection");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "Selection must not grant a missing Staff");
        com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ChangeSelectedManipPacket.handle(packet, context);
        helper.assertTrue(known.getSelectedManip() == shot, "The next cycle must escape the unavailable form");
        player.discard(); helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void pendingWhisperDeduplicatesAndRetainsUnansweredDecision(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.tickCount = 200;
        String key = "hemomancy:pending_whispers";
        String pending = com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.REVELATION_CHOICE_PENDING;
        player.getPersistentData().putBoolean(pending, true);
        var tree = com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees.coreWitnessDialogue();
        com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers.enqueue(player, tree);
        com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers.enqueue(player, tree);
        var entries = player.getPersistentData().getList(key, net.minecraft.nbt.Tag.TAG_COMPOUND);
        helper.assertTrue(entries.size() == 1, "Repeated offers must retain one decision");
        UUID id = entries.getCompound(0).getUUID("id");
        helper.assertTrue(com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers.open(player), "Quiet fixture can listen");
        com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers.close(player, id);
        helper.assertTrue(player.getPersistentData().getList(key, 10).size() == 1, "Closing does not answer the choice");
        var restored = player(helper);
        restored.tickCount = 200;
        restored.getPersistentData().merge(player.getPersistentData().copy());
        helper.assertTrue(com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers.open(restored), "Persisted inbox reopens on a new actor");
        restored.getPersistentData().remove(pending); // Supplied acceptance state; not a path-choice gameplay test.
        com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers.close(restored, id);
        helper.assertTrue(restored.getPersistentData().getList(key, 10).isEmpty(), "Answered decision is consumed once");
        player.discard(); restored.discard(); helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void lateAlchemistRetainsEligibleSeparationClaim(GameTestHelper helper) {
        var tree = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees
                .forDegree(7, 42, false, false, null, null, false, true, false, false);
        String claim = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM;
        helper.assertTrue(tree.getStartNode().options().stream().anyMatch(o -> claim.equals(o.eventId())),
                "An eligible late claim must remain in the ordinary root choices");
        helper.assertTrue(tree.getNode("first_separation_complete") != null, "The authored response remains reachable");
        var claimed = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees
                .forDegree(7, 42, false, false, null, null, false, false, false, false);
        helper.assertTrue(claimed.getStartNode().options().stream().noneMatch(o -> claim.equals(o.eventId())),
                "An ineligible or claimed lesson must not be offered");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void chamberCloneRetainsOnlyEarnedVisitProgress(GameTestHelper helper) {
        ServerPlayer original = player(helper), replacement = player(helper);
        String prefix = "hemomancy:chamber_visit_";
        original.getPersistentData().putBoolean(prefix + "attuned", true);
        original.getPersistentData().putBoolean(prefix + "chair_bound", true);
        original.getPersistentData().putBoolean(prefix + "guided_complete", true);
        original.getPersistentData().putBoolean(prefix + "active", true);
        original.getPersistentData().putString(prefix + "mode", "DREAM");
        original.getPersistentData().putInt(prefix + "remaining", 1200);
        original.getPersistentData().put(prefix + "dream_inventory", new net.minecraft.nbt.ListTag());
        com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.copyEarnedProgress(original, replacement);
        helper.assertTrue(com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isAttuned(replacement)
                && com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isChairBound(replacement)
                && com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.hasCompletedGuidedVisit(replacement),
                "Earned access and guided lesson survive");
        helper.assertTrue(!replacement.getPersistentData().contains(prefix + "active")
                && !replacement.getPersistentData().contains(prefix + "remaining")
                && !replacement.getPersistentData().contains(prefix + "dream_inventory"), "Session and inventory snapshots are not copied");
        original.discard(); replacement.discard(); helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void guidedChamberStartAndReturnDoNotGrantAttunement(GameTestHelper helper) {
        ServerPlayer learner = player(helper);
        var chamber = com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.CHAMBER_OF_WILL;
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireInitiatoryDegree(learner)
                .setDegreeNumber(3);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireBloodVolume(learner)
                .setActive(true);
        try {
            if (helper.getLevel().getServer().getLevel(chamber) == null) {
                helper.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.beginGuidedVisit(learner),
                        "A missing destination must leave the lesson retryable");
                helper.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isActive(learner)
                        && !com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.hasCompletedGuidedVisit(learner),
                        "A failed start grants neither a visit nor the lesson");
            } else {
                helper.assertTrue(com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.beginGuidedVisit(learner),
                        "Degree 3 should enter a guided Chamber visit");
                helper.assertTrue(com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isActive(learner)
                        && !com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.hasCompletedGuidedVisit(learner),
                        "Entry alone must not complete the lesson");
                com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.returnFromVisit(learner);
                helper.assertTrue(com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.hasCompletedGuidedVisit(learner)
                        && !com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isAttuned(learner),
                        "Only a valid return completes the lesson; it does not grant rite attunement");
            }
        } finally {
            if (com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isActive(learner))
                com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.returnFromVisit(learner);
            learner.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void interruptedGuidedVisitRestoresInventoryAndEquipment(GameTestHelper helper) {
        ServerPlayer learner = player(helper);
        var equipment = (com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentContainer)
                HemoCapabilityAccess.requireEquipment(learner);
        String prefix = "hemomancy:chamber_visit_";
        learner.getInventory().setItem(0, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 7));
        equipment.setStackInSlot(6, new ItemStack(ItemInit.blood_gourd_white.get()));
        var data = learner.getPersistentData();
        data.put(prefix + "dream_inventory", learner.getInventory().save(new net.minecraft.nbt.ListTag()));
        data.put(prefix + "guided_equipment", equipment.serializeNBT(learner.registryAccess()));
        data.putBoolean(prefix + "active", true);
        data.putString(prefix + "mode", "GUIDED");
        try {
            var interruptedAt = learner.position();
            learner.getInventory().setItem(0, new ItemStack(net.minecraft.world.item.Items.DIRT));
            equipment.setStackInSlot(6, ItemStack.EMPTY);
            com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.returnFromVisit(learner);
            helper.assertTrue(learner.getInventory().getItem(0).is(net.minecraft.world.item.Items.IRON_INGOT)
                    && learner.getInventory().getItem(0).getCount() == 7,
                    "A recovery outside the Chamber restores the exact inventory stack");
            helper.assertTrue(equipment.getStackInSlot(6).is(ItemInit.blood_gourd_white.get()),
                    "The equipped vessel must be restored as well");
            helper.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isActive(learner)
                    && !com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.hasCompletedGuidedVisit(learner),
                    "Recovery clears the session but cannot award a completed lesson");
            helper.assertTrue(learner.position().equals(interruptedAt),
                    "Recovery outside the Chamber must not teleport the player a second time");
        } finally {
            learner.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void overworldFungalSurveyRequiresVisitAndKeepsBothSpecimens(GameTestHelper helper) {
        ServerPlayer learner = player(helper);
        var alchemist = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_alchemist.get()
                .create(helper.getLevel());
        alchemist.setPos(learner.position());
        helper.getLevel().addFreshEntity(alchemist);
        var first = new ItemStack(com.vincenthuto.hemomancy.common.init.BlockInit.infected_fungus.get());
        var second = new ItemStack(com.vincenthuto.hemomancy.common.init.BlockInit.puffball_fungus.get());
        learner.getInventory().setItem(0, first);
        learner.getInventory().setItem(1, second);
        HemoCapabilityAccess.requireInitiatoryDegree(learner).setDegreeNumber(2);
        HemoCapabilityAccess.requireBloodVolume(learner).setActive(true);
        try {
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey
                    .progress(learner).ready(), "Holding specimens alone is not a visit");
            com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.recordVisit(learner,
                    net.minecraft.world.level.Level.NETHER,
                    com.vincenthuto.hemomancy.common.init.BiomeInit.FUNGAL_GARDENS);
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.visited(learner),
                    "The fungal projection must not count as an Overworld survey");
            com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.recordVisit(learner,
                    net.minecraft.world.level.Level.OVERWORLD,
                    com.vincenthuto.hemomancy.common.init.BiomeInit.FUNGAL_GARDENS);
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey
                    .progress(learner).ready(), "Prior Overworld discovery must count before a referral");
            learner.getInventory().setItem(1, new ItemStack(
                    com.vincenthuto.hemomancy.common.init.BlockInit.ghost_pipe.get()));
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey
                    .report(learner, alchemist), "Forest Ghost Pipe is not a local Gardens specimen");
            learner.getInventory().setItem(1, first.copy());
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey
                    .report(learner, alchemist), "Two copies of one plant are not two specimens");
            learner.getInventory().setItem(1, second);
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey
                    .report(learner, alchemist), "The Alchemist should accept two distinct local plants");
            helper.assertTrue(first.getCount() == 1 && second.getCount() == 1
                    && learner.getInventory().getItem(0).is(first.getItem())
                    && learner.getInventory().getItem(1).is(second.getItem()),
                    "Inspection must preserve both carried specimens");
            learner.getInventory().setItem(0, ItemStack.EMPTY);
            learner.getInventory().setItem(1, ItemStack.EMPTY);
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey
                    .progress(learner).specimens() == 2,
                    "A completed report must not regress when the plants are stored elsewhere");
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey
                    .report(learner, alchemist), "The report is one-time");
            ServerPlayer restored = player(helper);
            try {
                restored.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG,
                        learner.getPersistentData().getCompound(
                                net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG).copy());
                var persisted = com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.progress(restored);
                helper.assertTrue(persisted.visited() && persisted.reported() && persisted.specimens() == 2,
                        "Visit and report evidence must survive persisted player restoration");
            } finally {
                restored.discard();
            }
        } finally {
            alchemist.discard(); learner.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void fungalSurveyRejectsSpectatorVisitAndResumesWithAnotherAlchemist(GameTestHelper helper) {
        var actor = player(helper);
        var restored = player(helper);
        actor.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 2, 2.5)));
        restored.setPos(actor.position());
        HemoCapabilityAccess.requireInitiatoryDegree(actor).setDegreeNumber(0);
        HemoCapabilityAccess.requireBloodVolume(actor).setActive(false);
        var firstTeacher = EntityInit.harbinger_alchemist.get().create(helper.getLevel());
        var secondTeacher = EntityInit.harbinger_alchemist.get().create(helper.getLevel());
        try {
            actor.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.recordVisit(actor,
                    net.minecraft.world.level.Level.OVERWORLD,
                    com.vincenthuto.hemomancy.common.init.BiomeInit.FUNGAL_GARDENS);
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.visited(actor),
                    "Spectator inspection must not earn the Overworld survey visit");
            actor.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.recordVisit(actor,
                    net.minecraft.world.level.Level.OVERWORLD,
                    com.vincenthuto.hemomancy.common.init.BiomeInit.FUNGAL_GARDENS);
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.visited(actor)
                            && !com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.reported(actor),
                    "Ordinary early discovery must remain useful before the report");
            restored.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG,
                    actor.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG).copy());
            HemoCapabilityAccess.requireInitiatoryDegree(restored).setDegreeNumber(2);
            HemoCapabilityAccess.requireBloodVolume(restored).setActive(true);
            var first = new ItemStack(com.vincenthuto.hemomancy.common.init.BlockInit.infected_fungus.get());
            var second = new ItemStack(com.vincenthuto.hemomancy.common.init.BlockInit.puffball_fungus.get());
            first.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Retained fungus"));
            restored.getInventory().setItem(0, first);
            restored.getInventory().setItem(1, second);
            firstTeacher.setPos(restored.position());
            secondTeacher.setPos(restored.position());
            helper.assertTrue(helper.getLevel().addFreshEntity(firstTeacher)
                            && helper.getLevel().addFreshEntity(secondTeacher), "Both survey teachers must spawn");
            firstTeacher.discard();
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.report(restored, firstTeacher),
                    "A removed teacher must not accept the restored report");
            secondTeacher.setPos(restored.position().add(9, 0, 0));
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.report(restored, secondTeacher),
                    "The replacement teacher must still be within interaction range");
            secondTeacher.setPos(restored.position());
            var firstSnapshot = first.copy();
            var secondSnapshot = second.copy();
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.report(restored, secondTeacher),
                    "A different living Alchemist must accept restored early discovery without a repeat visit");
            helper.assertTrue(ItemStack.matches(firstSnapshot, restored.getInventory().getItem(0))
                            && ItemStack.matches(secondSnapshot, restored.getInventory().getItem(1))
                            && HemoCapabilityAccess.getPlayerDegreeNumber(restored) == 2,
                    "Replacement-teacher inspection must retain exact specimen components/counts and degree");
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.report(restored, secondTeacher),
                    "Changing teacher must not allow repeat completion");
            helper.succeed();
        } finally { firstTeacher.discard(); secondTeacher.discard(); actor.discard(); restored.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void voyagerIntroductionPreservesEarlyObservationAndRejectsWrongTeacher(GameTestHelper helper) {
        ServerPlayer learner = player(helper);
        var level = helper.getLevel();
        var voyager = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_voyager.get().create(level);
        var vicar = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_vicar.get().create(level);
        voyager.setPos(learner.position());
        vicar.setPos(learner.position());
        level.addFreshEntity(voyager);
        level.addFreshEntity(vicar);
        HemoCapabilityAccess.requireInitiatoryDegree(learner).setDegreeNumber(2);
        HemoCapabilityAccess.requireBloodVolume(learner).setActive(true);
        try {
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.canRequestBearing(
                    learner, vicar), "The vessel bearing begins at Degree 3, not Degree 2");
            helper.assertTrue(com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket.dispatch(
                    learner, com.vincenthuto.hemomancy.common.entity.npc.dialogue.VoyagerIntroductionDialogue.BEARING,
                    voyager.getId()) == null, "A Voyager cannot impersonate the Vicar's bearing service");
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.isFieldSite(
                    net.minecraft.world.level.Level.NETHER,
                    com.vincenthuto.hemomancy.common.init.BiomeInit.ERYTHROCORAL_REEF, true),
                    "A reef-like site outside the Overworld is not the Voyager field assignment");
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.isFieldSite(
                    net.minecraft.world.level.Level.OVERWORLD,
                    com.vincenthuto.hemomancy.common.init.BiomeInit.ERYTHROCORAL_REEF, false),
                    "An Overworld reef is valid without a vessel");
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.isFieldSite(
                    net.minecraft.world.level.Level.OVERWORLD,
                    net.minecraft.world.level.biome.Biomes.DEEP_OCEAN, true),
                    "An active vessel is valid even at a reef edge");
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.observe(
                    learner, voyager), "A spawned Voyager outside a reef or vessel cannot grant proof");
            HemoCapabilityAccess.requireInitiatoryDegree(learner).setDegreeNumber(3);
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.canRequestBearing(
                    learner, vicar), "An eligible unobserved Overworld learner can request the lead");
            vicar.setPos(learner.getX() + 20, learner.getY(), learner.getZ());
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.canRequestBearing(
                    learner, vicar), "The bearing cannot be requested from a distant teacher");
            vicar.setPos(learner.position());
            HemoCapabilityAccess.requireBloodVolume(learner).setActive(false);
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.canRequestBearing(
                    learner, vicar), "The bearing respects active-blood eligibility");
            HemoCapabilityAccess.requireBloodVolume(learner).setActive(true);
            HemoCapabilityAccess.requireInitiatoryDegree(learner).setDegreeNumber(2);
            var persisted = learner.getPersistentData().getCompound(
                    net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);
            var evidence = new net.minecraft.nbt.CompoundTag();
            evidence.putBoolean("Observed", true);
            persisted.put("hemomancy:voyager_introduction", evidence);
            learner.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG, persisted);
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.report(
                    learner, vicar), "A prior observation is saved but the formal report begins at Degree 3");
            HemoCapabilityAccess.requireInitiatoryDegree(learner).setDegreeNumber(3);
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.canRequestBearing(
                    learner, vicar), "An earlier observation needs a report, not another journey");
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.report(
                    learner, voyager), "Only a Vicar can accept the report");
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.report(
                    learner, vicar), "The Vicar must recognize the earlier observation");
            helper.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.report(
                    learner, vicar), "The report cannot be claimed twice");
            ServerPlayer restored = player(helper);
            try {
                restored.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG,
                        learner.getPersistentData().getCompound(
                                net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG).copy());
                var result = com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.progress(restored);
                helper.assertTrue(result.observed() && result.reported(),
                        "Voyager observation and report must survive persisted player restoration");
            } finally {
                restored.discard();
            }
        } finally {
            voyager.discard(); vicar.discard(); learner.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void symmetricFloorResolvesOccupiedSocketsInEveryRotation(GameTestHelper helper) {
        var level = helper.getLevel();
        var focus = helper.absolutePos(new net.minecraft.core.BlockPos(12, 30, 12));
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(level,
                Hemomancy.rloc("cardinal_rite/illuminatus_rite"));
        var floor = com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry.get(recipe.getFloorId()).orElseThrow();
        var actor = player(helper);
        com.vincenthuto.hemomancy.common.network.PlaceStructurePacket.placeLayeredCardinalRite(level, focus, recipe, actor);
        // The authored floor and four pillars are symmetric; only the supplied offerings rotate.
        for (var rotation : net.minecraft.world.level.block.Rotation.values()) {
            for (var socket : floor.brazierSockets()) {
                for (var clearRotation : net.minecraft.world.level.block.Rotation.values()) {
                    var offset = new net.minecraft.core.BlockPos(socket.getX(), socket.getY(), -socket.getZ()).rotate(clearRotation);
                    level.setBlockAndUpdate(focus.offset(offset), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                }
            }
            int index = 0;
            for (var requirement : recipe.getBrazierSignature()) {
                for (int copy = 0; copy < requirement.count(); copy++) {
                    var socket = floor.brazierSockets().get(index++);
                    var pos = focus.offset(new net.minecraft.core.BlockPos(socket.getX(), socket.getY(), -socket.getZ()).rotate(rotation));
                    level.setBlockAndUpdate(pos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                    level.setBlockAndUpdate(pos, com.vincenthuto.hemomancy.common.init.BlockInit.iron_brazier.get().defaultBlockState()
                            .setValue(com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock.RITUAL_PHASE, 1));
                    var brazier = (com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity) level.getBlockEntity(pos);
                    brazier.insertOffering(null, requirement.ingredient().getItems()[0].copyWithCount(1));
                }
            }
            var result = com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher.resolve(level, focus, java.util.List.of(recipe));
            helper.assertTrue(result.status() == com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher.Status.MATCHED,
                    "Occupied authored sockets must resolve once at " + rotation + "; got " + result.status());
            helper.assertTrue(com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher.find(level, focus, recipe).isPresent(),
                    "Direct station lookup must agree at " + rotation);
        }
        actor.discard(); helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void rejectedScarPreservesInputsAndTraceSurvivesReload(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(2, 3, 2));
        var block = com.vincenthuto.hemomancy.common.init.BlockInit.scar_station.get();
        level.setBlockAndUpdate(pos, block.defaultBlockState());
        var station = (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ScarStationBlockEntity) level.getBlockEntity(pos);
        station.setItem(0, new ItemStack(ItemInit.scar_blank.get()));
        station.setItem(1, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT));
        station.setItem(3, new ItemStack(ItemInit.hematic_iron_knapper.get()));
        var partial = com.vincenthuto.hemomancy.common.recipe.ScarRecipe.blank();
        partial[2][3] = 1;
        station.setScarList(partial);
        station.craftEvent();
        helper.assertTrue(station.getItem(0).is(ItemInit.scar_blank.get()) && station.getItem(1).is(net.minecraft.world.item.Items.IRON_INGOT)
                && station.getItem(2).isEmpty() && station.getItem(3).getDamageValue() == 0 && station.scarsList[2][3] == 1,
                "Invalid carving must preserve inputs, tool and trace");
        var saved = station.saveWithFullMetadata(level.registryAccess());
        var restored = new com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ScarStationBlockEntity(pos, block.defaultBlockState());
        restored.setLevel(level);
        restored.loadWithComponents(saved, level.registryAccess());
        restored.onLoad();
        helper.assertTrue(java.util.Arrays.deepEquals(partial, restored.scarsList), "World load must retain a partial trace");
        var recipe = station.getCurrentRecipe();
        helper.assertTrue(recipe != null, "Supplied Thorn inputs must resolve an authored recipe");
        station.setScarList(recipe.getPattern()); // Explicit trace fixture; real tracing is replayed in the client.
        station.setItem(3, ItemStack.EMPTY);
        station.craftEvent();
        helper.assertTrue(station.getItem(2).isEmpty() && !station.getItem(0).isEmpty(), "A complete trace without a knapper must not craft");
        station.setItem(3, new ItemStack(ItemInit.hematic_iron_knapper.get()));
        station.craftEvent();
        helper.assertTrue(station.getItem(2).is(ItemInit.scar_thorn.get()) && station.getItem(0).isEmpty() && station.getItem(1).isEmpty(),
                "One valid carving consumes its ingredients and produces exactly the authored scar");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void scarSupervisionIsLocalAndDoesNotGrantMastery(GameTestHelper helper) {
        var level = helper.getLevel();
        var student = player(helper);
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(4, 5, 4));
        var block = com.vincenthuto.hemomancy.common.init.BlockInit.scar_station.get();
        level.setBlockAndUpdate(pos, block.defaultBlockState());
        student.setPos(pos.getX() + 1, pos.getY(), pos.getZ());
        HemoCapabilityAccess.requireInitiatoryDegree(student).setDegreeNumber(4);
        HemoCapabilityAccess.requireBloodVolume(student).setActive(true);
        com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.grantIfNotDone(student,
                com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON);
        helper.assertTrue(!com.vincenthuto.hemomancy.common.event.MachineAccessEvents.canUseStation(student, level, pos),
                "A supplied lesson milestone alone must not unlock a station");
        var instructor = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_cicatrix_anchorite.get().create(level);
        instructor.setPos(pos.getX() + 2, pos.getY(), pos.getZ());
        var instructorChunk = new net.minecraft.world.level.ChunkPos(instructor.blockPosition());
        level.getChunkSource().addRegionTicket(HELPER_FIXTURE_TICKET, instructorChunk, 2, student.getUUID(), true);
        instructor.setNoAi(true); level.addFreshEntity(instructor);
        Runnable cleanup = () -> {
            instructor.discard(); student.discard();
            level.getChunkSource().removeRegionTicket(HELPER_FIXTURE_TICKET, instructorChunk, 2, student.getUUID(), true);
        };
        cleanupOnFailure(helper, cleanup);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.areEntitiesLoaded(instructorChunk.toLong()),
                "The supplied scar instructor chunk must become entity-loaded")).thenExecute(() -> {
            try {
                helper.assertTrue(com.vincenthuto.hemomancy.common.event.MachineAccessEvents.canUseStation(student, level, pos),
                        "The unfinished lesson may use the nearby supervised station");
                helper.assertTrue(!com.vincenthuto.hemomancy.common.event.MachineAccessEvents.hasPersonalAccess(student, block),
                        "Supervision must not grant personal craft credit");
                instructor.setPos(pos.getX() + 20, pos.getY(), pos.getZ());
                helper.assertTrue(!com.vincenthuto.hemomancy.common.event.MachineAccessEvents.canUseStation(student, level, pos),
                        "Moving the instructor away revokes access");
                instructor.setPos(pos.getX() + 2, pos.getY(), pos.getZ());
                com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.grantIfNotDone(student,
                        com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_LEARNED);
                helper.assertTrue(!com.vincenthuto.hemomancy.common.event.MachineAccessEvents.canUseStation(student, level, pos),
                        "Learning the first scar ends the exception");
                com.vincenthuto.hemomancy.common.event.MachineAccessEvents.awardMachineCrafted(student, block);
                helper.assertTrue(com.vincenthuto.hemomancy.common.event.MachineAccessEvents.canUseStation(student, level, pos),
                        "Canonical personal credit still permits access after the lesson");
            } finally { cleanup.run(); }
        }).thenSucceed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void projectionPassesOnlyRecruitedAlliesAndKeepsDeliberateConversation(GameTestHelper helper) {
        var player = player(helper);
        var npc = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_vicar.get().create(helper.getLevel());
        var line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline(
                "supplied-line", player.getUUID(), UUID.randomUUID(), new java.util.ArrayList<>(java.util.List.of(player.getUUID())));
        HemoCapabilityAccess.requireBloodVolume(player).setBloodLine(line);
        player.getInventory().setItem(0, new ItemStack(ItemInit.blood_projection.get()));
        helper.assertTrue(!com.vincenthuto.hemomancy.common.event.BloodProjectionInteractionEvents.prefersProjectionOverConversation(player, npc),
                "An unrelated NPC retains ordinary interaction");
        line.addNpcMember(npc.getUUID());
        helper.assertTrue(com.vincenthuto.hemomancy.common.event.BloodProjectionInteractionEvents.prefersProjectionOverConversation(player, npc),
                "Projection may start through a recruited NPC");
        player.setShiftKeyDown(true);
        helper.assertTrue(!com.vincenthuto.hemomancy.common.event.BloodProjectionInteractionEvents.prefersProjectionOverConversation(player, npc),
                "Crouching retains deliberate conversation");
        player.setShiftKeyDown(false); player.getInventory().setItem(0, ItemStack.EMPTY);
        helper.assertTrue(!com.vincenthuto.hemomancy.common.event.BloodProjectionInteractionEvents.prefersProjectionOverConversation(player, npc),
                "Empty-hand role assignment and conversation remain available");
        npc.discard(); player.discard(); helper.succeed();
    }

    private static ItemStack jar(ItemStack... creatures) {
        ItemStack jar = new ItemStack(ItemInit.morphling_jar.get());
        MorphlingJarItemHandler handler = handler(jar);
        for (int slot = 0; slot < creatures.length; slot++) handler.setStackInSlot(slot, creatures[slot].copy());
        handler.save();
        return jar;
    }

    private static MorphlingJarItemHandler handler(ItemStack jar) {
        MorphlingJarItemHandler handler = (MorphlingJarItemHandler) jar.getCapability(Capabilities.ItemHandler.ITEM);
        handler.load();
        return handler;
    }

    private static ServerPlayer player(GameTestHelper helper) {
        return player(helper, message -> {});
    }

    private static ServerPlayer player(GameTestHelper helper,
            java.util.function.Consumer<net.minecraft.network.chat.Component> messages) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "repair-fixture"), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation()) {
            @Override public void displayClientMessage(net.minecraft.network.chat.Component message, boolean overlay) {
                messages.accept(message);
            }
        };
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        return player;
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void voyagerBearingAcknowledgesQueuedSearchWithoutGrantingProof(GameTestHelper helper) {
        var messages = new java.util.ArrayList<String>();
        var learner = player(helper, message -> {
            if (message.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text)
                messages.add(text.getKey());
        });
        var vicar = EntityInit.harbinger_vicar.get().create(helper.getLevel());
        learner.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 2, 2.5)));
        vicar.setPos(learner.position());
        helper.getLevel().addFreshEntity(vicar);
        HemoCapabilityAccess.requireInitiatoryDegree(learner).setDegreeNumber(3);
        HemoCapabilityAccess.requireBloodVolume(learner).setActive(true);
        try {
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.tellBearing(
                    learner, vicar), "An eligible player must be able to request chart work");
            helper.assertTrue(messages.contains("hemomancy.vicar.voyager_introduction.bearing_searching"),
                    "The request must acknowledge queued chart work instead of finishing a blocking lookup");
            var progress = com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.progress(learner);
            helper.assertTrue(!progress.observed() && !progress.reported(), "Chart work grants no quest proof");
        } finally {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
                    new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(learner));
            vicar.discard(); learner.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void voyagerBearingBoundsRequestsAndReleasesLoggedOutPlayer(GameTestHelper helper) {
        var learners = new java.util.ArrayList<ServerPlayer>();
        var messages = new java.util.ArrayList<String>();
        var vicar = EntityInit.harbinger_vicar.get().create(helper.getLevel());
        vicar.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 2, 2.5)));
        helper.getLevel().addFreshEntity(vicar);
        try {
            for (int index = 0; index < 9; index++) {
                var learner = player(helper, message -> {
                    if (message.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text)
                        messages.add(text.getKey());
                });
                learners.add(learner);
                learner.setPos(vicar.position());
                HemoCapabilityAccess.requireInitiatoryDegree(learner).setDegreeNumber(3);
                HemoCapabilityAccess.requireBloodVolume(learner).setActive(true);
                boolean accepted = com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.tellBearing(learner, vicar);
                helper.assertTrue(accepted == (index < 8), "The chart queue admits eight distinct requests, not nine");
                if (index == 0) helper.assertTrue(
                        com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.tellBearing(learner, vicar),
                        "A repeated request acknowledges the existing job without spending another queue slot");
            }
            helper.assertTrue(messages.contains("hemomancy.vicar.voyager_introduction.bearing_busy"),
                    "A full queue must explain why the request was refused");
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
                    new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(learners.getFirst()));
            helper.assertTrue(com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.tellBearing(
                    learners.getLast(), vicar), "Logout must release the request so another eligible learner can retry");
        } finally {
            for (var learner : learners) {
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
                        new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(learner));
                learner.discard();
            }
            vicar.discard();
        }
        helper.succeed();
    }
}
