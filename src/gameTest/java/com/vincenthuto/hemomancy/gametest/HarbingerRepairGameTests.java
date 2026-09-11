package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
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
    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void oldConsumedPomeReconcilesOnlyItsOwnersMatchingPendingHusk(GameTestHelper helper) {
        var actor = player(helper); var stranger = player(helper);
        var level = helper.getLevel();
        var origin = helper.absolutePos(new net.minecraft.core.BlockPos(0, 48, 0));
        var blooms = com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData.get(level.getServer().overworld());
        var dimension = level.dimension().location().toString();
        level.setBlock(origin, com.vincenthuto.hemomancy.common.init.BlockInit.qliphoth_bloom.get().defaultBlockState(), 3);
        blooms.addBloom(new com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData.BloomEntry(actor.getUUID(), origin, dimension, 0, level.getGameTime()));
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(origin), net.minecraft.core.Direction.UP, origin, false);
        try {
            blooms.setPendingPome(origin, 0); blooms.markPendingPomeClaimed(origin);
            HemoCapabilityAccess.requireInitiatoryDegree(actor).recordPomeConsumed(origin.asLong());
            level.getBlockState(origin).useWithoutItem(level, stranger, hit);
            helper.assertTrue(blooms.getPendingPomeHuskIndex(origin) == 0, "A stranger must not reconcile another tree");
            level.getBlockState(origin).useWithoutItem(level, actor, hit);
            helper.assertTrue(blooms.getPendingPomeHuskIndex(origin) == -1, "Authoritative owner consumption must reconcile the old stuck pending husk");
            blooms.setPendingPome(origin, 1); blooms.markPendingPomeClaimed(origin);
            level.getBlockState(origin).useWithoutItem(level, actor, hit);
            helper.assertTrue(blooms.getPendingPomeHuskIndex(origin) == 1, "The next uneaten husk must remain pending");
        } finally { blooms.removeBloomInChunk(origin, dimension); actor.discard(); stranger.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void duplicatePomeCannotAdvanceOrClearTheNextHusk(GameTestHelper helper) {
        var actor = player(helper);
        var origin = helper.absolutePos(new net.minecraft.core.BlockPos(0, 45, 0));
        var blooms = com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData.get(helper.getLevel().getServer().overworld());
        var fruit = com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem.createPickedPomeStack(origin.asLong(), 0, actor.getUUID());
        var duplicate = fruit.copy();
        try {
            blooms.setPendingPome(origin, 0); blooms.markPendingPomeClaimed(origin);
            fruit.getItem().finishUsingItem(fruit, helper.getLevel(), actor);
            helper.assertTrue(HemoCapabilityAccess.requireInitiatoryDegree(actor).getTotalPomesConsumed() == 1, "First husk must count");
            blooms.setPendingPome(origin, 1); blooms.markPendingPomeClaimed(origin);
            duplicate.getItem().finishUsingItem(duplicate, helper.getLevel(), actor);
            helper.assertTrue(HemoCapabilityAccess.requireInitiatoryDegree(actor).getTotalPomesConsumed() == 1,
                    "Duplicate husk must not advance communion");
            helper.assertTrue(blooms.getPendingPomeHuskIndex(origin) == 1 && duplicate.getCount() == 1,
                    "Rejected duplicate must preserve itself and the next pending husk");
            var next = com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem.createPickedPomeStack(origin.asLong(), 1, actor.getUUID());
            next.getItem().finishUsingItem(next, helper.getLevel(), actor);
            helper.assertTrue(HemoCapabilityAccess.requireInitiatoryDegree(actor).getTotalPomesConsumed() == 2
                    && blooms.getPendingPomeHuskIndex(origin) == -1, "The actual next husk must still advance and clear itself");
            var later = com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem.createPickedPomeStack(origin.asLong(), 3, actor.getUUID());
            later.getItem().finishUsingItem(later, helper.getLevel(), actor);
            helper.assertTrue(later.getCount() == 1 && HemoCapabilityAccess.requireInitiatoryDegree(actor).getTotalPomesConsumed() == 2,
                    "An out-of-order husk must wait for its preceding fruit");
        } finally { blooms.clearPendingPome(origin.asLong()); actor.discard(); }
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
    public static void secondPlayerCanEarnInitiationAtAnAlreadySpentTemple(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var first = player(helper); var second = player(helper);
        var center = helper.absolutePos(new net.minecraft.core.BlockPos(0, 40, 0));
        var displayPos = center.east(2);
        var hermit = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_hermit.get().create(level);
        hermit.moveTo(center.getX(), center.getY(), center.getZ() + 3);
        level.addFreshEntity(hermit);
        first.setPos(net.minecraft.world.phys.Vec3.atCenterOf(center.above()));
        second.setPos(net.minecraft.world.phys.Vec3.atCenterOf(center.above()));
        level.setBlock(center, com.vincenthuto.hemomancy.common.init.BlockInit.cardinal_focus.get().defaultBlockState(), 3);
        level.setBlock(displayPos, com.vincenthuto.hemomancy.common.init.BlockInit.mortal_display.get().defaultBlockState(), 3);
        var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity) level.getBlockEntity(center);
        var display = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.MortalDisplayBlockEntity) level.getBlockEntity(displayPos);
        focus.linkTempleDisplay(displayPos); display.linkHermit(hermit.getUUID());
        var completion = com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents.class.getDeclaredMethod(
                "completeRite", net.minecraft.server.level.ServerLevel.class, ServerPlayer.class,
                com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.class);
        completion.setAccessible(true);
        try {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent(first, "hermit_heart_offered", hermit.getId()));
            level.getBlockState(displayPos).useWithoutItem(level, first, new net.minecraft.world.phys.BlockHitResult(
                    net.minecraft.world.phys.Vec3.atCenterOf(displayPos), net.minecraft.core.Direction.UP, displayPos, false));
            focus.insertMedium(first, new ItemStack(net.minecraft.world.item.Items.IRON_NUGGET));
            var rite = new com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite(first.getUUID(), center,
                    Hemomancy.rloc("cardinal_rite/sanguine_initiation"), 1, 3);
            helper.assertTrue((boolean) completion.invoke(null, level, first, rite), "First supplied initiation must complete");
            helper.assertTrue(level.getBlockState(displayPos).is(com.vincenthuto.hemomancy.common.init.BlockInit.placed_blood_stained_stone.get()),
                    "First initiation must leave the actual spent display marker");
            for (int attempt = 0; attempt < 2; attempt++) {
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent(second, "hermit_heart_offered", hermit.getId()));
                helper.assertTrue(com.vincenthuto.hemomancy.common.rite.TempleOathRules.hasClaimedHeartFrom(second, hermit.getUUID()),
                        "A second eligible player's real Hermit blessing must provide a personal route at the spent temple");
                helper.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(second) == 0
                        && !HemoCapabilityAccess.getBloodVolume(second).orElseThrow().isActive(), "The entitlement must not grant initiation itself");
            }
            int charms = second.getInventory().countItem(ItemInit.charm_of_vascularium.get());
            var equipment = HemoCapabilityAccess.getEquipment(second).orElseThrow();
            for (int i = 0; i < equipment.getSlots(); i++) if (equipment.getStackInSlot(i).is(ItemInit.charm_of_vascularium.get())) charms += equipment.getStackInSlot(i).getCount();
            helper.assertTrue(charms == 1, "Repeated blessing must not farm dormant charms");
            var nugget = new ItemStack(net.minecraft.world.item.Items.IRON_NUGGET);
            level.getBlockState(center).useItemOn(nugget, level, second, net.minecraft.world.InteractionHand.MAIN_HAND,
                    new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(center), net.minecraft.core.Direction.UP, center, false));
            helper.assertTrue(com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level).hasActiveRite(second.getUUID()),
                    "Second player must enter the real paid initiation through the same focus");
        } finally {
            com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level).removeRite(second.getUUID());
            first.discard(); second.discard(); hermit.discard();
        }
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

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void actualInitiationCompletionAwardsStarterBloodOnlyOnce(GameTestHelper helper) throws Exception {
        initiationSupply(helper, false);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void fullInventoryInitiationDropsStarterBloodOnlyOnce(GameTestHelper helper) throws Exception {
        initiationSupply(helper, true);
    }

    private static void initiationSupply(GameTestHelper helper, boolean fullInventory) throws Exception {
        var player = player(helper);
        if (fullInventory) for (int slot = 0; slot < 36; slot++) player.getInventory().setItem(slot, new ItemStack(net.minecraft.world.item.Items.BARRIER, 64));
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(0, 30, 0));
        player.setPos(net.minecraft.world.phys.Vec3.atCenterOf(pos.above()));
        helper.getLevel().setBlock(pos, com.vincenthuto.hemomancy.common.init.BlockInit.cardinal_focus.get().defaultBlockState(), 3);
        var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity) helper.getLevel().getBlockEntity(pos);
        var completion = com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents.class.getDeclaredMethod(
                "completeRite", net.minecraft.server.level.ServerLevel.class, ServerPlayer.class,
                com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.class);
        completion.setAccessible(true);
        try {
            for (int attempt = 0; attempt < 2; attempt++) {
                focus.extractMedium();
                focus.insertMedium(player, new ItemStack(net.minecraft.world.item.Items.IRON_NUGGET));
                var rite = new com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite(player.getUUID(), pos,
                        Hemomancy.rloc("cardinal_rite/sanguine_initiation"), 1, 3);
                helper.assertTrue((boolean) completion.invoke(null, helper.getLevel(), player, rite), "Supplied initiation completion must succeed");
                int drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(pos).inflate(2), e -> e.getItem().is(ItemInit.bloody_flask.get()))
                        .stream().mapToInt(e -> e.getItem().getCount()).sum();
                helper.assertTrue(player.getInventory().countItem(ItemInit.bloody_flask.get()) + drops == 4,
                        "Actual initiation completion must deliver four starter flasks exactly once");
            }
        } finally { player.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void attendantRejectsInsufficientPaymentWithoutSpending(GameTestHelper helper) {
        try (var fixture = new AllyPaymentFixture(helper, com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.ATTENDANT)) {
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
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void wardenPaysOnlyForALivingRiteThreat(GameTestHelper helper) {
        try (var fixture = new AllyPaymentFixture(helper, com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.WARDEN)) {
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
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "harbinger_repair")
    public static void helperPaymentDoesNotRoundFractionalPoolIntoFreeBlood(GameTestHelper helper) {
        try (var fixture = new AllyPaymentFixture(helper, com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.ANCHOR)) {
            fixture.line.setBloodVolume(0.5F);
            int drawn = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService.spend(
                    helper.getLevel(), fixture.rite, fixture.ally.getUUID(), 1);
            helper.assertTrue(drawn == 1 && fixture.line.getBloodVolume() == 0.5F && fixture.reserve() == 999,
                    "A whole-ml transfer must retain a fractional shared remainder and debit the private ml");
        }
        helper.succeed();
    }

    private static final class AllyPaymentFixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer actor;
        final net.minecraft.world.entity.Mob ally;
        final com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline line;
        final com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData data;
        final com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe recipe;
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
            level.setBlock(station.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
            ally = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_vicar.get().create(level);
            ally.setNoAi(true); // Effect/payment fixture only, not navigation acceptance.
            ally.moveTo(station.getX() + 0.5, station.getY(), station.getZ() + 0.5);
            level.addFreshEntity(ally);
            data.addNpcMember(line.getBloodlineUUID(), ally.getUUID());
            rite.assignAlly(ally.getUUID(), role);
            helper.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService.isAvailable(level, rite, ally.getUUID()), "Supplied helper must be available");
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
        level.setBlock(station.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
        var allies = new java.util.ArrayList<net.minecraft.world.entity.Mob>();
        for (int i = 0; i < 3; i++) {
            var ally = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_vicar.get().create(level);
            ally.setNoAi(true); // Supplied effect fixture; navigation is validated in the live campaign.
            ally.moveTo(station.getX() + 1.5, station.getY(), station.getZ() - 1.5 + i * 1.5);
            level.addFreshEntity(ally);
            data.addNpcMember(line.getBloodlineUUID(), ally.getUUID());
            rite.assignAlly(ally.getUUID(), role);
            allies.add(ally);
        }
        try {
            for (var ally : allies) helper.assertTrue(
                    com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService.isAvailable(level, rite, ally.getUUID()),
                    "Each supplied helper must be available before measuring its effect");
            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine.tick(level, actor, rite, recipe);
            helper.assertTrue(java.util.Arrays.stream(rite.getAnchorBloodMl()).sum() == 30,
                    "Three available Anchor helpers must each supply 10 ml across the two real rings");
            helper.assertTrue(allies.stream().mapToInt(a -> line.getNpcRiteReserve(a.getUUID(), level.getGameTime())).sum() == 2970,
                    "Only the 30 ml actually delivered may leave private reserves");
        } finally {
            allies.forEach(net.minecraft.world.entity.Entity::discard);
            data.disbandBloodline(line.getBloodlineUUID());
            actor.discard();
        }
        helper.succeed();
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
        original.getPersistentData().putBoolean(prefix + "active", true);
        original.getPersistentData().putString(prefix + "mode", "DREAM");
        original.getPersistentData().putInt(prefix + "remaining", 1200);
        original.getPersistentData().put(prefix + "dream_inventory", new net.minecraft.nbt.ListTag());
        com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.copyEarnedProgress(original, replacement);
        helper.assertTrue(com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isAttuned(replacement)
                && com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isChairBound(replacement), "Earned access survives");
        helper.assertTrue(!replacement.getPersistentData().contains(prefix + "active")
                && !replacement.getPersistentData().contains(prefix + "remaining")
                && !replacement.getPersistentData().contains(prefix + "dream_inventory"), "Session and inventory snapshots are not copied");
        original.discard(); replacement.discard(); helper.succeed();
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
        instructor.setNoAi(true); level.addFreshEntity(instructor);
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
        instructor.discard(); student.discard(); helper.succeed();
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
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "repair-fixture"), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        return player;
    }
}
