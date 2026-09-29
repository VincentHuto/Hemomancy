package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.block.harbinger.EscharianScyphusBlock;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.ClinicalBloodDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEventHandler;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.MnemonistStarterMemoryChoice;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.PhlegethonticCommissionDialogue;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSicklePruning;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules;
import com.vincenthuto.hemomancy.common.menu.tile.functional.PhlebotomistsCabinetMenu;
import com.vincenthuto.hemomancy.common.mission.alchemist.*;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncVesperFightScene;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomEvents;
import com.vincenthuto.hemomancy.common.worldgen.VesperOrdealManager;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyFixtures;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyStage;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneySnapshot;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyChecks;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyController;
import com.vincenthuto.hemomancy.gametest.journey.JourneyRoute;
import com.vincenthuto.hemomancy.gametest.journey.HarbingerJourneyAutomation;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.PhlebotomistsCabinetBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.*;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress.Lesson.*;

@GameTestHolder("clinical_validation")
@PrefixGameTestTemplate(false)
public final class ClinicalBloodProgressionGameTests {
    private static ServerPlayer player(GameTestHelper h, int degree) {
        return player(h, degree, h.getLevel());
    }
    private static ServerPlayer player(GameTestHelper h, int degree, net.minecraft.server.level.ServerLevel level) {
        return player(h, degree, level, packet -> {});
    }
    private static ServerPlayer player(GameTestHelper h, int degree, net.minecraft.server.level.ServerLevel level,
            java.util.function.Consumer<net.minecraft.network.protocol.Packet<?>> received) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "clinical-test"), false);
        var p = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(p.server, connection, p, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { received.accept(packet); }
        };
        p.setGameMode(GameType.SURVIVAL);
        p.setPos(level == h.getLevel() ? h.absolutePos(new BlockPos(0, 3, 0)).getCenter()
                : new Vec3(0.5, 80, 0.5));
        p.setNoGravity(true);
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(degree);
        return p;
    }

    private static void captureVesperScene(net.minecraft.network.protocol.Packet<?> packet,
            List<PacketSyncVesperFightScene> scenes) {
        if (packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom
                && custom.payload() instanceof PacketSyncVesperFightScene scene) scenes.add(scene);
        if (packet instanceof net.minecraft.network.protocol.game.ClientboundBundlePacket bundle)
            bundle.subPackets().forEach(child -> captureVesperScene(child, scenes));
    }
    private static ItemStack vial(String source) {
        var stack = new ItemStack(ItemInit.bloody_vial.get());
        var tag = new CompoundTag();
        tag.putString(BloodVialItem.TAG_ENTITY_TYPE, source);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
    private static void examine(GameTestHelper h, ServerPlayer p, ItemStack vial) {
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
        p.setItemInHand(InteractionHand.OFF_HAND, vial);
        p.getMainHandItem().use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        for (int i = 0; i < 40; i++) p.doTick();
        h.assertTrue(BloodSampleData.identified(vial), "Examination did not identify the sample");
        p.releaseUsingItem();
    }

    @GameTest(template = "empty") public static void degreeTwoBriefingAndTeachingValidateOwnerAndEvidence(GameTestHelper h) {
        var p = player(h, 2);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        var artificer = EntityInit.harbinger_artificer.get().create(h.getLevel());
        alchemist.setPos(p.position()); artificer.setPos(p.position());
        h.assertTrue(FirstSeparationAssignment.canBrief(p), "First Separation is still locked behind Degree 2");
        var dialogue = alchemist.progressionDialogue(p);
        h.assertTrue(dialogue.getStartNode().options().stream()
                .anyMatch(o -> "first_separation_offer".equals(o.nextNodeId())),
                "D2 briefing offer is unreachable");
        var offer = dialogue.getNode("first_separation_offer");
        h.assertTrue(offer != null && offer.lines().size() == 2,
                "The First Separation offer must explain the assignment before acceptance");
        h.assertTrue(offer.options().stream().anyMatch(o ->
                        HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF.equals(o.eventId())
                                && "first_separation_briefing".equals(o.nextNodeId())),
                "The explained offer does not retain the briefing action");
        var decorated = DialogueHubFactory.decorate(dialogue, "alchemist", p);
        h.assertTrue(decorated.presentation().topics(DialogueCategory.QUESTS).stream()
                        .anyMatch(topic -> "first_separation_offer".equals(topic.targetNodeId())),
                "The dialogue hub does not open the authored First Separation offer");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, alchemist, MICROSCOPE), "Empty-handed microscope lesson accepted");
        var firstVial = new ItemStack(ItemInit.bloody_vial.get());
        p.setItemInHand(InteractionHand.OFF_HAND, firstVial);
        ItemInit.bloody_vial.get().onLeftClickEntity(firstVial, p, net.minecraft.world.entity.EntityType.PIG.create(h.getLevel()));
        h.assertTrue(HemoCapabilityAccess.clinicalBlood(p).collected, "Successful sampling did not immediately record collection");
        ClinicalBloodKnowledge.met(p, "alchemist");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, artificer, MICROSCOPE), "Wrong teacher unlocked microscopy");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, alchemist, MICROSCOPE), "Alchemist did not teach held sample");
        h.assertTrue(p.getRecipeBook().contains(Hemomancy.rloc("hematic_microscope")), "Microscope recipe not awarded");
        h.assertTrue(LiberKnowledgeHelper.hasEntry(p, ClinicalBloodKnowledge.entry("microscope")), "Liber lesson missing");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, alchemist, MICROSCOPE), "Repeat lesson rewarded twice");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, alchemist, INJECTION), "Collection alone unlocked injection");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void journeyCompletesMissingFirstBloodcraftProofsBeforeVicar(GameTestHelper h) {
        var actor = player(h, 1);
        setServerPlayerLookup(actor, true);
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                h.getLevel().dimension().location().toString());
        var origin = HemoJourneyFixtures.findClearOrigin(actor);
        var proofStage = java.util.Arrays.stream(HemoJourneyStage.values())
                .filter(stage -> "first_bloodcraft_proofs".equals(stage.id())).findFirst();
        try {
            h.assertTrue(proofStage.isPresent()
                            && proofStage.get().ordinal() < HemoJourneyStage.VICAR_REWARD.ordinal(),
                    "The journey skips Blood Absorption and Venous Stone before the Vicar return");
            FirstBloodcraftAssignment.recordFormation(actor);
            FirstBloodcraftAssignment.recordStructure(actor, new ItemStack(ItemInit.liber_sanguinum.get()));
            var blood = HemoCapabilityAccess.requireBloodVolume(actor);
            blood.setActive(true);
            blood.setBloodVolume(4600.0D);
            HemoJourneyFixtures.prepare(actor, proofStage.orElseThrow(), origin);
            HarbingerJourneyAutomation.perform(actor, proofStage.orElseThrow().id(), origin);
            var progress = FirstBloodcraftAssignment.progress(actor);
            h.assertTrue(progress.absorbedMl() >= 500.0D && progress.venousStoneProjected()
                            && progress.readyForVicar(),
                    "The proof checkpoint did not earn absorption and Stone infusion through gameplay paths: " + progress);
            HemoJourneyFixtures.cleanup(actor, origin);
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.VICAR_REWARD, origin);
            actor.setPos(origin.getX() + 1.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
            HarbingerJourneyAutomation.perform(actor, HemoJourneyStage.VICAR_REWARD.id(), origin);
            h.assertTrue(HemoCapabilityAccess.requireInitiatoryDegree(actor).getDegreeNumber() == 2
                            && FirstBloodcraftAssignment.isClaimed(actor),
                    "The Vicar still refused a player with all four independent proofs: degree="
                            + HemoCapabilityAccess.requireInitiatoryDegree(actor).getDegreeNumber()
                            + ", claimed=" + FirstBloodcraftAssignment.isClaimed(actor)
                            + ", check=" + HemoJourneyChecks.verify(actor, HemoJourneyStage.VICAR_REWARD, origin));
            h.assertTrue(HemoJourneyFixtures.captureExpectedOutputs(actor, HemoJourneyStage.VICAR_REWARD, origin),
                    "The Vicar reward kit was not attributable after the scrap pickup");
            HemoJourneyFixtures.cleanupOwnedOutputs(actor, origin);
            h.assertTrue(actor.getInventory().items.stream().anyMatch(stack ->
                            stack.is(ItemInit.hematic_iron_scrap.get()) && stack.getCount() >= 4),
                    "The Vicar's Hematic Iron Scrap did not survive fixture output cleanup");
        } finally {
            HemoJourneyFixtures.cleanup(actor, origin);
            setServerPlayerLookup(actor, false);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void unboundPomesAreFoodButNotCommunionProof(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        try {
            for (int i = 0; i < 9; i++) {
                ItemStack pome = new ItemStack(ItemInit.qliphoth_pome.get());
                pome.getItem().finishUsingItem(pome, h.getLevel(), actor);
            }
            h.assertTrue(actor.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION),
                    "Unbound pomes should still provide their ordinary effects");
            h.assertTrue(degree.getTotalPomesConsumed() == 0,
                    "Unbound pomes must not count as nine fruits from one owned Bloom");
            h.assertTrue(!degree.isQliphothCommunionDone() && !degree.hasFungalSpineGranted(),
                    "Unbound pomes must not award Communion or the Fungal Spine");
        } finally {
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void ownedBoundPomesStillCompleteCommunion(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        var bloom = new QliphothBloomSavedData.BloomEntry(actor.getUUID(),
                h.absolutePos(new BlockPos(2, 2, 2)), h.getLevel().dimension().location().toString(), 3,
                h.getLevel().getGameTime());
        blooms.addBloom(bloom);
        try {
            for (int husk = 0; husk < 9; husk++) {
                ItemStack pome = QliphothPomeItem.createPickedPomeStack(bloom, husk);
                pome.getItem().finishUsingItem(pome, h.getLevel(), actor);
            }
            h.assertTrue(degree.getTotalPomesConsumed() == 9 && degree.isQliphothCommunionDone()
                            && degree.hasFungalSpineGranted(),
                    "Nine sequential bound pomes from one Bloom must complete Communion");
        } finally {
            blooms.removeBloomInChunk(bloom.center(), bloom.dimension());
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void fullInventoryKeepsFungalSpineClaimPending(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        var bloom = new QliphothBloomSavedData.BloomEntry(actor.getUUID(),
                h.absolutePos(new BlockPos(2, 2, 2)), h.getLevel().dimension().location().toString(), 3,
                h.getLevel().getGameTime());
        blooms.addBloom(bloom);
        var nearby = new AABB(actor.blockPosition()).inflate(4);
        try {
            for (int husk = 0; husk < 8; husk++) {
                ItemStack pome = QliphothPomeItem.createPickedPomeStack(bloom, husk);
                pome.getItem().finishUsingItem(pome, h.getLevel(), actor);
            }
            for (int slot = 0; slot < actor.getInventory().getContainerSize(); slot++) {
                actor.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE));
            }
            ItemStack last = QliphothPomeItem.createPickedPomeStack(bloom, 8);
            last.getItem().finishUsingItem(last, h.getLevel(), actor);
            h.assertTrue(degree.isQliphothCommunionDone() && !degree.hasFungalSpineGranted()
                            && actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 0
                            && h.getLevel().getEntitiesOfClass(ItemEntity.class, nearby,
                                    item -> item.getItem().is(ItemInit.fungal_spine.get())).isEmpty(),
                    "A full inventory must keep the Fungal Spine claim pending instead of dropping its only copy");
            var persisted = new com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegree();
            persisted.deserializeNBT(actor.registryAccess(), actor.getData(
                    com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes.INITIATORY_DEGREE)
                    .serializeNBT(actor.registryAccess()));
            h.assertTrue(persisted.isQliphothCommunionDone() && !persisted.hasFungalSpineGranted(),
                    "The pending Spine claim did not survive degree serialization");
            InitiatoryDegreeEvents.playerLoggedIn(new PlayerEvent.PlayerLoggedInEvent(actor));
            h.assertTrue(!degree.hasFungalSpineGranted()
                            && actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 0,
                    "Login must not mark a full-inventory Spine claim as delivered");
            actor.getInventory().setItem(0, ItemStack.EMPTY);
            QliphothBloomEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
            h.assertTrue(degree.hasFungalSpineGranted()
                            && actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "Making room did not deliver the pending Fungal Spine");
            QliphothBloomEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
            h.assertTrue(actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "Spine delivery retried after its one-time claim was recorded");
        } finally {
            h.getLevel().getEntitiesOfClass(ItemEntity.class, nearby,
                    item -> item.getItem().is(ItemInit.fungal_spine.get())).forEach(ItemEntity::discard);
            blooms.removeBloomInChunk(bloom.center(), bloom.dimension());
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void existingFungalSpineFulfillsLegacyClaimWithoutDuplicate(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        try {
            degree.setQliphothCommunionDone(true);
            actor.getInventory().setItem(0, new ItemStack(ItemInit.fungal_spine.get()));
            InitiatoryDegreeEvents.playerLoggedIn(new PlayerEvent.PlayerLoggedInEvent(actor));
            QliphothBloomEvents.deliverPendingFungalSpine(actor);
            h.assertTrue(degree.hasFungalSpineGranted()
                            && actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "A legacy Spine in inventory must fulfill the saved claim without another copy");
        } finally {
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void journeyCommunionUsesAndCleansUpOneBloomLifecycle(GameTestHelper h) {
        var actor = player(h, 7);
        var center = HemoJourneyFixtures.findClearOrigin(actor);
        var dimension = h.getLevel().dimension().location().toString();
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        try {
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.QLIPHOTH_COMMUNION, center);
            var bloom = blooms.getBloomAt(center.above(), dimension);
            h.assertTrue(bloom != null && bloom.center().equals(center.above())
                            && bloom.ownerUUID().equals(actor.getUUID()),
                    "Journey Communion must prepare a real owned Bloom");
            h.assertTrue(h.getLevel().getBlockState(center.above()).is(BlockInit.qliphoth_bloom.get()),
                    "Journey Bloom must exist in the test world");
            h.assertTrue(!QliphothPomeItem.isBoundPomeFromBloom(
                            QliphothPomeItem.createPickedPomeStack(bloom.center().asLong(), 0, actor.getUUID()), bloom),
                    "Coordinate-only legacy fruit must not prove the new journey Bloom identity");
            h.assertTrue(actor.getInventory().countItem(ItemInit.qliphoth_pome.get()) == 0,
                    "Journey Communion must not supply fruit before the Bloom ripens");
            blooms.incrementPomesDropped(bloom);
            blooms.setPendingPome(bloom, 0);
            var hit = new BlockHitResult(Vec3.atCenterOf(bloom.center()), Direction.UP, bloom.center(), false);
            h.getLevel().getBlockState(bloom.center()).useWithoutItem(h.getLevel(), actor, hit);
            h.assertTrue(blooms.isPendingPomeClaimed(bloom)
                            && QliphothPomeItem.isBoundPomeFromBloom(actor.getInventory().getItem(0), bloom),
                    "The interrupted journey must begin with its first pome already picked");
            HarbingerJourneyAutomation.perform(actor, HemoJourneyStage.QLIPHOTH_COMMUNION.id(), center);
            var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
            h.assertTrue(degree.isQliphothCommunionDone()
                            && degree.getPomesConsumedFromBloom(bloom.bloomId(), bloom.center().asLong()) == 9
                            && blooms.getPomesDropped(bloom) == 9 && !blooms.hasPendingPome(bloom),
                    "Journey automation must pick and consume nine sequential fruit from its Bloom");
            HemoJourneyFixtures.cleanup(actor, center);
            h.assertTrue(blooms.getBloomById(bloom.bloomId()) == null
                            && h.getLevel().getBlockState(center.above()).isAir()
                            && h.getLevel().getBlockState(center.above(8)).isAir(),
                    "Journey fixture cleanup must remove its Bloom and saved lifecycle");
        } finally {
            HemoJourneyFixtures.cleanup(actor, center);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void ownerPicksAndConsumesNineRipePomesThroughBloom(GameTestHelper h) {
        var owner = player(h, 7);
        var stranger = player(h, 7);
        var origin = HemoJourneyFixtures.findClearOrigin(owner);
        var dimension = h.getLevel().dimension().location().toString();
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        owner.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        try {
            HemoJourneyFixtures.prepare(owner, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var root = origin.above();
            var bloom = blooms.getBloomAt(root, dimension);
            h.assertTrue(bloom != null && bloom.center().equals(root), "Ripe-pome test needs a real Bloom");
            var hit = new BlockHitResult(Vec3.atCenterOf(root), Direction.UP, root, false);
            for (int husk = 0; husk < 9; husk++) {
                blooms.incrementPomesDropped(bloom);
                blooms.setPendingPome(bloom, husk);
                h.getLevel().getBlockState(root).useWithoutItem(h.getLevel(), stranger, hit);
                h.assertTrue(stranger.getInventory().countItem(ItemInit.qliphoth_pome.get()) == 0
                                && !blooms.isPendingPomeClaimed(bloom),
                        "A stranger claimed the owner's ripe pome");
                h.getLevel().getBlockState(root).useWithoutItem(h.getLevel(), owner, hit);
                ItemStack pome = owner.getInventory().getItem(0);
                h.assertTrue(QliphothPomeItem.isBoundPomeFromBloom(pome, bloom)
                                && blooms.isPendingPomeClaimed(bloom),
                        "The Bloom did not give its owner the current bound pome");
                h.getLevel().getBlockState(root).useWithoutItem(h.getLevel(), owner, hit);
                h.assertTrue(owner.getInventory().countItem(ItemInit.qliphoth_pome.get()) == 1,
                        "A claimed husk could be picked twice");
                pome.getItem().finishUsingItem(pome, h.getLevel(), owner);
                h.assertTrue(!blooms.hasPendingPome(bloom)
                                && HemoCapabilityAccess.requireInitiatoryDegree(owner)
                                        .getPomesConsumedFromBloom(bloom.bloomId(), root.asLong()) == husk + 1,
                        "Eating the picked husk did not release the tree for the next fruit");
            }
            var degree = HemoCapabilityAccess.requireInitiatoryDegree(owner);
            h.assertTrue(degree.isQliphothCommunionDone() && degree.hasFungalSpineGranted()
                            && owner.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "Nine picked and eaten pomes did not complete Communion");
        } finally {
            HemoJourneyFixtures.cleanup(owner, origin);
            owner.discard();
            stranger.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void ownerReclaimsLostFungalSpineAtCompletedBloom(GameTestHelper h) {
        var owner = player(h, 7);
        var stranger = player(h, 7);
        var origin = HemoJourneyFixtures.findClearOrigin(owner);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        owner.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                h.getLevel().dimension().location().toString());
        try {
            HemoJourneyFixtures.prepare(owner, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var root = origin.above();
            var bloom = blooms.getBloomAt(root, h.getLevel().dimension().location().toString());
            HarbingerJourneyAutomation.perform(owner, HemoJourneyStage.QLIPHOTH_COMMUNION.id(), origin);
            var degree = HemoCapabilityAccess.requireInitiatoryDegree(owner);
            h.assertTrue(bloom != null && degree.isQliphothCommunionDone()
                            && degree.hasFungalSpineGranted()
                            && owner.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "Recovery test needs a completed owner-bound Bloom and earned Spine");
            owner.getInventory().clearContent();
            var hit = new BlockHitResult(Vec3.atCenterOf(root), Direction.UP, root, false);
            owner.setPos(root.getCenter());
            stranger.setPos(root.getCenter());
            stranger.setShiftKeyDown(true);
            stranger.gameMode.useItemOn(stranger, h.getLevel(), stranger.getMainHandItem(),
                    InteractionHand.MAIN_HAND, hit);
            h.assertTrue(stranger.getInventory().countItem(ItemInit.fungal_spine.get()) == 0,
                    "A stranger reclaimed the owner's Fungal Spine");
            owner.setShiftKeyDown(true);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            owner.gameMode.useItemOn(owner, h.getLevel(), owner.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            h.assertTrue(owner.getInventory().countItem(ItemInit.fungal_spine.get()) == 0,
                    "Holding an unrelated item should not trigger empty-hand Spine recovery");
            owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            owner.gameMode.useItemOn(owner, h.getLevel(), owner.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            h.assertTrue(owner.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "The Communion owner could not reclaim a lost Spine at the living Bloom");
            owner.gameMode.useItemOn(owner, h.getLevel(), owner.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            h.assertTrue(owner.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "The Bloom issued a duplicate Spine while the owner carried one");
            owner.getInventory().clearContent();
            blooms.removeBloomInChunk(root, bloom.dimension());
            var replacement = new QliphothBloomSavedData.BloomEntry(owner.getUUID(), root,
                    bloom.dimension(), 3, h.getLevel().getGameTime());
            blooms.addBloom(replacement);
            for (int i = 0; i < 9; i++) blooms.incrementPomesDropped(replacement);
            owner.gameMode.useItemOn(owner, h.getLevel(), owner.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            h.assertTrue(owner.getInventory().countItem(ItemInit.fungal_spine.get()) == 0,
                    "A new Bloom at the old coordinates inherited the spent tree's Spine claim");
        } finally {
            HemoJourneyFixtures.cleanup(owner, origin);
            owner.discard();
            stranger.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void apotheosJourneyRunsInFocusedServer(GameTestHelper h) {
        HarbingerJourneyFixtureGameTests.apotheosJourneyUsesRealCommunionChoiceAndRite(h);
    }

    @GameTest(template = "empty")
    public static void journeyRestoresNumericFungalProjectionState(GameTestHelper h) {
        var actor = player(h, 7);
        var persistent = actor.getPersistentData();
        persistent.putBoolean(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.PROJECTION_ACTIVE, true);
        persistent.putInt(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.PROJECTION_REMAINING, 77);
        persistent.putDouble(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.RETURN_X, 123.5D);
        persistent.putFloat(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.RETURN_YROT, 91.0F);
        try {
            h.assertTrue(HemoJourneySnapshot.capture(actor).passed(), "Fungal projection snapshot capture failed");
            h.assertTrue(HemoJourneySnapshot.resetForJourney(actor).passed(),
                    "Journey reset rejected numeric Fungal projection fields");
            h.assertTrue(!persistent.contains(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.PROJECTION_ACTIVE)
                            && !persistent.contains(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.RETURN_X),
                    "Journey reset left the prior Fungal projection active");
            var restored = HemoJourneySnapshot.restore(actor);
            h.assertTrue(restored.passed(), "Fungal projection snapshot restore failed: " + restored.message());
            h.assertTrue(persistent.getBoolean(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.PROJECTION_ACTIVE)
                            && persistent.getInt(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.PROJECTION_REMAINING) == 77
                            && persistent.getDouble(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.RETURN_X) == 123.5D
                            && persistent.getFloat(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.RETURN_YROT) == 91.0F,
                    "Journey restore changed the preexisting Fungal projection");
        } finally {
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void journeyAutomationUsesSpineBeforeApotheosChoice(GameTestHelper h) {
        var actor = player(h, 7);
        var origin = HemoJourneyFixtures.findClearOrigin(actor);
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                h.getLevel().dimension().location().toString());
        try {
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            HarbingerJourneyAutomation.perform(actor, HemoJourneyStage.QLIPHOTH_COMMUNION.id(), origin);
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.APOTHEOS_CHOICE, origin);
            h.assertTrue(!actor.getPersistentData().getBoolean(
                            com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.REVELATION_CHOICE_PENDING),
                    "Automation fixture offered the ending before projection");
            HarbingerJourneyAutomation.perform(actor, HemoJourneyStage.APOTHEOS_CHOICE.id(), origin);
            h.assertTrue(HemoJourneyChecks.verify(actor, HemoJourneyStage.APOTHEOS_CHOICE, origin).passed(),
                    "Journey automation did not use the Spine, return, and choose Apotheos");
        } finally {
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void journeyAcceptsSilentChoiceAfterRealProjection(GameTestHelper h) {
        var actor = player(h, 7);
        var origin = HemoJourneyFixtures.findClearOrigin(actor);
        var dimension = h.getLevel().dimension().location().toString();
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        try {
            h.assertTrue(HemoJourneySnapshot.capture(actor).passed(), "Silent journey snapshot capture failed");
            actor.getPersistentData().putLong(HemoJourneyFixtures.ORIGIN_KEY, origin.asLong());
            actor.getPersistentData().putString(JourneyRoute.KEY, JourneyRoute.HARBINGER);
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var bloom = blooms.getBloomAt(origin.above(), dimension);
            HarbingerJourneyAutomation.perform(actor, HemoJourneyStage.QLIPHOTH_COMMUNION.id(), origin);
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.APOTHEOS_CHOICE, origin);
            actor.getPersistentData().putString(HemoJourneySnapshot.STAGE_KEY, HemoJourneyStage.APOTHEOS_CHOICE.id());
            actor.getMainHandItem().use(h.getLevel(), actor, InteractionHand.MAIN_HAND);
            com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.performForcedProjectionReturn(actor);
            DialogueEventHandler.onDialogueOption(new DialogueEvent(actor, "archon_choice_silence", 0));
            h.assertTrue(HemoJourneyChecks.verify(actor, HemoJourneyStage.APOTHEOS_CHOICE, origin).passed()
                            && blooms.getBloomById(bloom.bloomId()) != null,
                    "A real Spine return and Silent choice must pass without losing the Communion Bloom");
            for (int slot = 1; slot < actor.getInventory().getContainerSize(); slot++) {
                actor.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE));
            }
            var failedTransition = HemoJourneyController.next(actor);
            h.assertTrue(!failedTransition.passed() && blooms.getBloomById(bloom.bloomId()) != null
                            && h.getLevel().getBlockState(bloom.center()).is(BlockInit.qliphoth_bloom.get()),
                    "A full-inventory transition must leave the verified choice and Bloom retryable");
            actor.getInventory().setItem(1, ItemStack.EMPTY);
            var next = HemoJourneyController.next(actor);
            h.assertTrue(next.passed() && next.stage() == HemoJourneyStage.SILENT_REFUSAL
                            && blooms.getBloomById(bloom.bloomId()) != null
                            && h.getLevel().getBlockState(bloom.center()).is(BlockInit.qliphoth_bloom.get()),
                    "The Silent branch must retain its owned Bloom for the refusal ordeal: " + next.message());
            var known = HemoCapabilityAccess.requireKnownManipulations(actor);
            h.assertTrue(known.isManipulationAvailable(ManipulationInit.conjure_blade.get())
                            && known.getEquippedManipNames().contains(ManipulationInit.conjure_blade.get().getName()),
                    "The refusal fixture must leave a selectable Living Arsenal weapon form");
            h.assertTrue(!HemoJourneyChecks.verify(actor, HemoJourneyStage.SILENT_REFUSAL, origin).passed(),
                    "The open refusal stage must not pass before Vesper victory");
            blooms.severBloom(bloom);
            blooms.sealBloom(bloom);
            HemoCapabilityAccess.requireInitiatoryDegree(actor).setArchonPath(
                    com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_ARCHON);
            HarbingerAdvancementGranter.grantIfNotDone(actor, HarbingerAdvancementGranter.ADV_VESPER_DEFEATED);
            actor.getPersistentData().putBoolean("hemomancy:vesper_memory_pending", true);
            h.assertTrue(HemoJourneyChecks.verify(actor, HemoJourneyStage.SILENT_REFUSAL, origin).passed(),
                    "The sealed owned Bloom and verified reward state must finish the Silent checkpoint");
            h.assertTrue(HemoJourneyController.next(actor).stage() == HemoJourneyStage.COMPLETE,
                    "The Silent checkpoint did not reach journey completion");
            h.assertTrue(HemoJourneyController.next(actor).passed()
                            && !HarbingerAdvancementGranter.hasAdvancement(actor,
                                    HarbingerAdvancementGranter.ADV_VESPER_DEFEATED)
                            && !actor.getPersistentData().contains("hemomancy:vesper_memory_pending")
                            && HemoCapabilityAccess.requireInitiatoryDegree(actor).getArchonPath()
                                    == com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.NONE
                            && blooms.getBloomById(bloom.bloomId()) == null,
                    "Journey restore kept a temporary Silent victory or its fixture Bloom");
        } finally {
            HemoJourneyController.clear(actor);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void oldSickleCannotSeverReplacementBloom(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING);
        degree.setQliphothCommunionDone(true);
        degree.syncTotalPomesConsumed(9);
        var origin = HemoJourneyFixtures.findClearOrigin(actor);
        var dimension = h.getLevel().dimension().location().toString();
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        try {
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var original = blooms.getBloomAt(origin.above(), dimension);
            h.assertTrue(original != null, "First Bloom was not registered");
            actor.setItemInHand(InteractionHand.MAIN_HAND, livingBlade());
            h.assertTrue(LivingSicklePruning.interact(h.getLevel(), origin.above(), actor, InteractionHand.MAIN_HAND)
                            && LivingSicklePruning.isTemporarySickle(actor.getMainHandItem()),
                    "The owned Bloom did not shape a temporary Sickle");
            ItemStack oldSickle = actor.getMainHandItem().copy();

            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var replacement = blooms.getBloomAt(origin.above(), dimension);
            h.assertTrue(replacement != null && !replacement.bloomId().equals(original.bloomId()),
                    "The replacement Bloom did not start a new lifecycle");
            actor.setItemInHand(InteractionHand.MAIN_HAND, oldSickle);
            LivingSicklePruning.interact(h.getLevel(), origin.above(), actor, InteractionHand.MAIN_HAND);
            h.assertTrue(blooms.getState(replacement) == com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState.LIVING
                            && LivingSicklePruning.isTemporarySickle(actor.getMainHandItem()),
                    "A Sickle from the removed Bloom severed its replacement");

            actor.setItemInHand(InteractionHand.MAIN_HAND, livingBlade());
            LivingSicklePruning.interact(h.getLevel(), origin.above(), actor, InteractionHand.MAIN_HAND);
            LivingSicklePruning.interact(h.getLevel(), origin.above(), actor, InteractionHand.MAIN_HAND);
            h.assertTrue(blooms.getState(replacement) == com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState.OPEN,
                    "A new Sickle could not open its own Bloom for the refusal ordeal");
        } finally {
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void legacySickleCanSeverMigratedBloom(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING);
        degree.setQliphothCommunionDone(true);
        degree.syncTotalPomesConsumed(9);
        var origin = HemoJourneyFixtures.findClearOrigin(actor);
        var dimension = h.getLevel().dimension().location().toString();
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        try {
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            blooms.removeBloomInChunk(origin.above(), dimension);
            var migrated = new QliphothBloomSavedData.BloomEntry(actor.getUUID(), origin.above(),
                    dimension, 3, h.getLevel().getGameTime(), UUID.randomUUID(), true);
            blooms.addBloom(migrated);
            actor.setItemInHand(InteractionHand.MAIN_HAND, livingBlade());
            LivingSicklePruning.interact(h.getLevel(), origin.above(), actor, InteractionHand.MAIN_HAND);
            ItemStack oldSickle = actor.getMainHandItem();
            h.assertTrue(LivingSicklePruning.isTemporarySickle(oldSickle), "Legacy test could not shape a Sickle");
            CompoundTag tag = oldSickle.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.remove(LivingSicklePruning.BLOOM_ID_KEY);
            oldSickle.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            LivingSicklePruning.interact(h.getLevel(), origin.above(), actor, InteractionHand.MAIN_HAND);
            h.assertTrue(blooms.getState(migrated) == com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState.OPEN,
                    "A pre-ID Sickle could not finish cutting its migrated Bloom");
        } finally {
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void refusalPortalRemainsRetryableWithoutChamber(GameTestHelper h) {
        var owner = player(h, 7);
        var stranger = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(owner);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING);
        degree.setQliphothCommunionDone(true);
        degree.syncTotalPomesConsumed(9);
        var origin = HemoJourneyFixtures.findClearOrigin(owner);
        var dimension = h.getLevel().dimension().location().toString();
        owner.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        try {
            HemoJourneyFixtures.prepare(owner, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var root = origin.above();
            var bloom = blooms.getBloomAt(root, dimension);
            h.assertTrue(bloom != null, "Refusal test needs a registered Bloom");
            var hit = new BlockHitResult(Vec3.atCenterOf(root), Direction.UP, root, false);
            stranger.setItemInHand(InteractionHand.MAIN_HAND, livingBlade());
            h.getLevel().getBlockState(root).useItemOn(stranger.getMainHandItem(), h.getLevel(),
                    stranger, InteractionHand.MAIN_HAND, hit);
            h.assertTrue(!LivingSicklePruning.isTemporarySickle(stranger.getMainHandItem())
                            && blooms.getState(bloom) == com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState.LIVING,
                    "A stranger shaped a Sickle or opened the owner's Bloom");

            owner.setItemInHand(InteractionHand.MAIN_HAND, livingBlade());
            h.getLevel().getBlockState(root).useItemOn(owner.getMainHandItem(), h.getLevel(),
                    owner, InteractionHand.MAIN_HAND, hit);
            h.getLevel().getBlockState(root).useItemOn(owner.getMainHandItem(), h.getLevel(),
                    owner, InteractionHand.MAIN_HAND, hit);
            h.assertTrue(blooms.getState(bloom) == com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState.OPEN
                            && degree.getArchonPath() == com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING,
                    "Opening the refusal portal prematurely awarded Silent Archon");
            h.assertTrue(h.getLevel().getServer().getLevel(
                            com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.CHAMBER_OF_WILL) == null,
                    "This focused test expects the GameTest server's absent Chamber dimension");
            owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            stranger.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            h.getLevel().getBlockState(root).useWithoutItem(h.getLevel(), stranger, hit);
            h.getLevel().getBlockState(root).useWithoutItem(h.getLevel(), owner, hit);
            h.assertTrue(blooms.getState(bloom) == com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState.OPEN
                            && degree.getArchonPath() == com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING
                            && !VesperOrdealManager.isActive(owner),
                    "A denied arena transfer consumed the refusal portal or granted its victory");
        } finally {
            HemoJourneyFixtures.cleanup(owner, origin);
            owner.discard();
            stranger.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void bloomTickRipensOneHuskAndWaitsForConsumption(GameTestHelper h) {
        var owner = player(h, 7);
        var level = h.getLevel();
        var origin = HemoJourneyFixtures.findClearOrigin(owner);
        var dimension = level.dimension().location().toString();
        owner.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        int delay = 40 - (int) (level.getGameTime() % 40);
        h.runAfterDelay(delay, () -> {
            try {
                HemoJourneyFixtures.prepare(owner, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
                var blooms = QliphothBloomSavedData.get(level.getServer().overworld());
                var bloom = blooms.getBloomAt(origin.above(), dimension);
                h.assertTrue(bloom != null && level.getGameTime() % 40 == 0,
                        "Ripening test did not reach a Bloom effect tick");
                long seed = 0L;
                while (seed < 10000L) {
                    level.getRandom().setSeed(seed);
                    if (level.getRandom().nextInt(80) == 0) break;
                    seed++;
                }
                h.assertTrue(seed < 10000L, "No deterministic ripe-pome roll was found");
                level.getRandom().setSeed(seed);
                var tick = new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(() -> true, level);
                QliphothBloomEvents.onLevelTick(tick);
                h.assertTrue(blooms.getPomesDropped(bloom) == 1
                                && blooms.getPendingPomeHuskIndex(bloom) == 0,
                        "Bloom world tick did not ripen its first husk");
                level.getRandom().setSeed(seed);
                QliphothBloomEvents.onLevelTick(tick);
                h.assertTrue(blooms.getPomesDropped(bloom) == 1,
                        "Bloom ripened another husk while the first was pending");
                blooms.clearPendingPome(bloom);
                level.getRandom().setSeed(seed);
                QliphothBloomEvents.onLevelTick(tick);
                h.assertTrue(blooms.getPomesDropped(bloom) == 2
                                && blooms.getPendingPomeHuskIndex(bloom) == 1,
                        "Bloom did not resume with the second husk after the pending fruit cleared");
                h.succeed();
            } finally {
                HemoJourneyFixtures.cleanup(owner, origin);
                owner.discard();
            }
        });
    }

    private static ItemStack livingBlade() {
        ItemStack blade = new ItemStack(ItemInit.living_blade.get());
        CompoundTag tag = new CompoundTag();
        tag.putString(LivingStaffWeaponFormHelper.FORM_KEY, LivingStaffWeaponFormRules.CONJURE_BLADE);
        blade.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return blade;
    }

    @GameTest(template = "empty")
    public static void replacementBloomDoesNotInheritPickedPomeProgress(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        var original = new QliphothBloomSavedData.BloomEntry(actor.getUUID(),
                h.absolutePos(new BlockPos(2, 2, 2)), h.getLevel().dimension().location().toString(), 3,
                h.getLevel().getGameTime());
        blooms.addBloom(original);
        ItemStack oldPome = QliphothPomeItem.createPickedPomeStack(original, 0);
        blooms.removeBloomInChunk(original.center(), original.dimension());
        var replacement = new QliphothBloomSavedData.BloomEntry(actor.getUUID(),
                original.center(), original.dimension(), 3, h.getLevel().getGameTime());
        blooms.addBloom(replacement);
        blooms.setPendingPome(replacement, 0);
        try {
            oldPome.getItem().finishUsingItem(oldPome, h.getLevel(), actor);
            h.assertTrue(blooms.hasPendingPome(replacement),
                    "Eating an old picked pome cleared the replacement Bloom's pending fruit");
            ItemStack newPome = QliphothPomeItem.createPickedPomeStack(replacement, 0);
            newPome.getItem().finishUsingItem(newPome, h.getLevel(), actor);
            h.assertTrue(degree.getPomesConsumedFromBloom(original.bloomId(), original.center().asLong()) == 1
                            && degree.getPomesConsumedFromBloom(replacement.bloomId(), replacement.center().asLong()) == 1,
                    "Pomes from separate lifecycles shared Communion sequence progress");
            h.assertTrue(!blooms.hasPendingPome(replacement),
                    "Eating the new pome did not clear its own pending fruit");
        } finally {
            blooms.removeBloomInChunk(replacement.center(), replacement.dimension());
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void vesperOrdealRetainsBloomIdentityAcrossPhasesAndReload(GameTestHelper h) {
        UUID owner = UUID.randomUUID();
        UUID bloomId = UUID.randomUUID();
        long origin = h.absolutePos(new BlockPos(2, 2, 2)).asLong();
        VesperTheCrownedRefusalEntity first = EntityInit.vesper_crowned_refusal.get().create(h.getLevel());
        first.setOrdeal(owner, origin, bloomId);
        VesperTheCrownedRefusalEntity restoredFirst = EntityInit.vesper_crowned_refusal.get().create(h.getLevel());
        restoredFirst.load(first.saveWithoutId(new CompoundTag()));
        h.assertTrue(bloomId.equals(restoredFirst.getBloomId()), "The first phase lost its Bloom ID on reload");

        VesperTheEveningStarEntity second = EntityInit.vesper_evening_star.get().create(h.getLevel());
        VesperOrdealManager.copyOrdeal(restoredFirst, second);
        VesperTheEveningStarEntity restoredSecond = EntityInit.vesper_evening_star.get().create(h.getLevel());
        restoredSecond.load(second.saveWithoutId(new CompoundTag()));
        h.assertTrue(owner.equals(restoredSecond.getOrdealOwner()) && origin == restoredSecond.getBloomOrigin()
                        && bloomId.equals(restoredSecond.getBloomId()),
                "Vesper's second phase did not retain the exact Bloom lifecycle");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void vesperMemoryWaitsForInventorySpace(GameTestHelper h) {
        var owner = player(h, 7);
        try {
            for (int slot = 0; slot < owner.getInventory().getContainerSize(); slot++) {
                owner.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE));
            }
            owner.getPersistentData().putBoolean("hemomancy:vesper_memory_pending", true);
            VesperOrdealManager.onLogin(new PlayerEvent.PlayerLoggedInEvent(owner));
            h.assertTrue(owner.getPersistentData().getBoolean("hemomancy:vesper_memory_pending")
                            && h.getLevel().getEntitiesOfClass(ItemEntity.class,
                                    new AABB(owner.blockPosition()).inflate(4),
                                    item -> item.getItem().is(ItemInit.memory_of_vesper.get())).isEmpty(),
                    "A full inventory must retain the memory claim instead of dropping its only copy");

            owner.getInventory().setItem(0, ItemStack.EMPTY);
            VesperOrdealManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
            h.assertTrue(!owner.getPersistentData().contains("hemomancy:vesper_memory_pending")
                            && owner.getInventory().countItem(ItemInit.memory_of_vesper.get()) == 1,
                    "The pending memory was not delivered after inventory space opened");
            VesperOrdealManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
            h.assertTrue(owner.getInventory().countItem(ItemInit.memory_of_vesper.get()) == 1,
                    "Retrying delivery duplicated the unique Vesper memory");
        } finally {
            owner.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void staleVesperAttemptReleasesReplacedBloom(GameTestHelper h) {
        var owner = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(owner);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING);
        var origin = HemoJourneyFixtures.findClearOrigin(owner);
        var dimension = h.getLevel().dimension().location().toString();
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        owner.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        try {
            HemoJourneyFixtures.prepare(owner, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var original = blooms.getBloomAt(origin.above(), dimension);
            blooms.severBloom(original);
            owner.getPersistentData().putLong("hemomancy:vesper_ordeal_bloom", original.center().asLong());
            owner.getPersistentData().putUUID("hemomancy:vesper_ordeal_bloom_id", original.bloomId());
            h.assertTrue(VesperOrdealManager.hasValidActiveBloom(owner),
                    "An open owned Bloom should support its active refusal attempt");
            degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.APOTHEOS_PENDING);
            h.assertTrue(!VesperOrdealManager.hasValidActiveBloom(owner),
                    "A changed ending choice must not resume a refusal attempt");
            degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING);
            blooms.sealBloom(original);
            h.assertTrue(!VesperOrdealManager.hasValidActiveBloom(owner),
                    "A sealed source Bloom must not resume Vesper");

            blooms.removeBloomInChunk(original.center(), dimension);
            var replacement = new QliphothBloomSavedData.BloomEntry(owner.getUUID(), original.center(),
                    dimension, 3, h.getLevel().getGameTime());
            blooms.addBloom(replacement);
            blooms.severBloom(replacement);
            h.assertTrue(!VesperOrdealManager.hasValidActiveBloom(owner),
                    "A new Bloom at the same coordinates cannot validate the old attempt");
            VesperOrdealManager.tickArenaPlayer(owner, h.getLevel());
            h.assertTrue(!VesperOrdealManager.isActive(owner)
                            && degree.getArchonPath() == com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING
                            && blooms.getState(replacement).isPortalOpen(),
                    "Stale Vesper recovery must preserve the replacement portal and pending path");
        } finally {
            HemoJourneyFixtures.cleanup(owner, origin);
            owner.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void chamberSceneSyncKeepsActiveVesperFloorAtArena(GameTestHelper h) {
        List<PacketSyncVesperFightScene> scenes = new ArrayList<>();
        var owner = player(h, 7, h.getLevel(), packet -> captureVesperScene(packet, scenes));
        var chamber = ChamberOfWillManager.get(h.getLevel().getServer());
        try {
            chamber.setSkyThemeOverride(owner, ChamberOfWillManager.THEME_VESPER_FIGHT);
            h.assertTrue(!scenes.isEmpty() && scenes.getLast().active(),
                    "The selected Vesper preview theme did not send its floor scene");
            BlockPos previewCenter = scenes.getLast().center();

            owner.getPersistentData().putLong("hemomancy:vesper_ordeal_bloom", h.absolutePos(new BlockPos(2, 2, 2)).asLong());
            scenes.clear();
            chamber.syncTo(owner);
            h.assertTrue(!scenes.isEmpty() && scenes.getLast().active()
                            && scenes.getLast().center().getX() == 4096
                            && !scenes.getLast().center().equals(previewCenter),
                    "An active Vesper sync used the preview cell instead of the real fight arena");
            VesperOrdealManager.abandonAttempt(owner);
            scenes.clear();
            chamber.syncTo(owner);
            h.assertTrue(!scenes.isEmpty() && scenes.getLast().center().equals(previewCenter),
                    "Ending the ordeal did not restore the selected Chamber floor preview");
        } finally {
            VesperOrdealManager.abandonAttempt(owner);
            chamber.clearSkyThemeOverride(owner);
            owner.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void blockedBloomRiteKeepsItsSeedForRetry(GameTestHelper h) {
        var actor = player(h, 7);
        var level = h.getLevel();
        var center = h.absolutePos(new BlockPos(6, 3, 6));
        var obstruction = center.above(5);
        var recipeId = Hemomancy.rloc("cardinal_rite/bloom_of_qliphoth");
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe
                .getRiteByLocation(level, recipeId);
        h.assertTrue(recipe != null, "Bloom rite recipe is missing");
        var bloodlines = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData
                .get(level.getServer().overworld());
        var line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline(
                "Bloom helper", actor.getUUID(), UUID.randomUUID(), new java.util.ArrayList<>());
        bloodlines.registerBloodline(line);
        HemoCapabilityAccess.requireBloodVolume(actor).setBloodLine(line);
        var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(
                actor.getUUID(), center, recipeId, 1200, 7, 7, false, 0);
        var role = com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.ANCHOR;
        var station = center.offset(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .markers(recipe).get(role));
        level.setBlock(station.below(), Blocks.STONE.defaultBlockState(), 3);
        var helper = EntityInit.harbinger_vicar.get().create(level);
        h.assertTrue(helper != null, "Bloom rite helper could not be created");
        helper.setPos(station.getX() + 0.5, station.getY(), station.getZ() + 0.5);
        helper.setNoAi(true);
        h.assertTrue(level.addFreshEntity(helper), "Bloom rite helper could not be spawned");
        bloodlines.addNpcMember(line.getBloodlineUUID(), helper.getUUID(),
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(helper.getType()));
        rite.assignAlly(helper.getUUID(), role);
        level.setBlock(center, BlockInit.cardinal_focus.get().defaultBlockState(), 3);
        var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity)
                level.getBlockEntity(center);
        h.assertTrue(focus.insertMedium(actor, new ItemStack(ItemInit.qliphoth_seed.get())),
                "The Bloom Seed could not be seated");
        level.setBlock(obstruction, Blocks.OBSIDIAN.defaultBlockState(), 3);
        try {
            h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                    .hasRequiredHelpers(level, rite), "Bloom helper setup did not satisfy the recipe");
            var complete = com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents.class
                    .getDeclaredMethod("completeRite", net.minecraft.server.level.ServerLevel.class,
                            ServerPlayer.class, com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.class);
            complete.setAccessible(true);
            h.assertTrue(!(boolean) complete.invoke(null, level, actor, rite),
                    "An obstructed Bloom column must reject completion");
            h.assertTrue(focus.getMediumForMatching().is(ItemInit.qliphoth_seed.get()),
                    "A rejected Bloom must leave its Seed in the Focus");
            var blooms = com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData
                    .get(level.getServer().overworld());
            var dimension = level.dimension().location().toString();
            h.assertTrue(blooms.getBloomAt(center.above(2), dimension) == null,
                    "A rejected Bloom registered a tree");
            level.removeBlock(obstruction, false);
            var nearby = new QliphothBloomSavedData.BloomEntry(UUID.randomUUID(),
                    center.above(2).east(16), dimension, 3, level.getGameTime());
            blooms.addBloom(nearby);
            try {
                h.assertTrue(!(boolean) complete.invoke(null, level, actor, rite),
                        "An overlapping Bloom must reject completion");
                h.assertTrue(focus.getMediumForMatching().is(ItemInit.qliphoth_seed.get()),
                        "An overlap rejection consumed the Seed");
            } finally {
                blooms.removeBloomInChunk(nearby.center(), dimension);
            }
            h.assertTrue((boolean) complete.invoke(null, level, actor, rite),
                    "A clear-site retry did not complete the Bloom rite");
            var bloom = blooms.getBloomAt(center.above(2), dimension);
            h.assertTrue(bloom != null && bloom.ownerUUID().equals(actor.getUUID())
                            && level.getBlockState(center.above(2)).is(BlockInit.qliphoth_bloom.get()),
                    "The retry did not place and register the owner's Bloom");
            h.assertTrue(!focus.hasMedium(), "The successful retry did not consume the Seed");
        } catch (ReflectiveOperationException exception) {
            h.fail("Bloom completion check could not run: " + exception);
        } finally {
            level.removeBlock(obstruction, false);
            level.removeBlock(center.above(2), false);
            level.removeBlock(center, false);
            helper.discard();
            bloodlines.disbandBloodline(line.getBloodlineUUID());
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void blockedBloomSiteCannotStartItsCeremony(GameTestHelper h) {
        var actor = player(h, 7);
        var level = h.getLevel();
        var origin = h.absolutePos(new BlockPos(14, 3, 14));
        var focusPos = origin.above();
        var obstruction = focusPos.above(5);
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                level.dimension().location().toString());
        try {
            HemoJourneyFixtures.prepareCardinalRite(actor, origin, "bloom_of_qliphoth");
            var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity)
                    level.getBlockEntity(focusPos);
            h.assertTrue(focus != null && focus.getMediumForMatching().is(ItemInit.qliphoth_seed.get()),
                    "Bloom fixture did not seat its Seed");
            for (int height = 3; height <= 9; height++) {
                level.setBlock(focusPos.above(height), Blocks.AIR.defaultBlockState(), 3);
            }
            h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents
                            .canPlaceQliphothBloom(level, actor, focusPos),
                    "Bloom fixture did not provide a clear column");
            level.setBlock(obstruction, Blocks.OBSIDIAN.defaultBlockState(), 3);
            var result = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, focusPos,
                            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
            h.assertTrue(result == com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.HANDLED
                            && com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level)
                                    .getRite(actor.getUUID()) == null,
                    "A blocked Bloom site started its ceremony");
            h.assertTrue(focus.getMediumForMatching().is(ItemInit.qliphoth_seed.get()),
                    "A rejected Bloom start consumed its Seed");
            level.removeBlock(obstruction, false);
            var root = focusPos.above(2);
            level.setBlock(root, Blocks.OBSIDIAN.defaultBlockState(), 3);
            var blockedRoot = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, focusPos,
                            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
            h.assertTrue(blockedRoot == com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.HANDLED
                            && com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level)
                                    .getRite(actor.getUUID()) == null,
                    "An occupied Bloom root started the ceremony");
            level.removeBlock(root, false);
            var retry = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, focusPos,
                            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
            var active = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level)
                    .getRite(actor.getUUID());
            h.assertTrue(retry == com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED
                            && active != null,
                    "A clear-site retry did not start the Bloom ceremony: " + retry + ", active=" + (active != null)
                            + ", placement=" + com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents
                                    .canPlaceQliphothBloom(level, actor, focusPos));
        } finally {
            level.removeBlock(obstruction, false);
            com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level).removeRite(actor.getUUID());
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void mnemonistChoiceAndBlankRecipeFollowDegree(GameTestHelper h) {
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(h.getLevel());
        var d1 = player(h, 1);
        mnemonist.setPos(d1.position());
        h.getLevel().addFreshEntity(mnemonist);
        h.assertTrue(mnemonist.progressionDialogue(d1).getStartNode().options().stream()
                .noneMatch(option -> "starter_choice".equals(option.nextNodeId())),
                "D1 Mnemonist offered the directed starter choice");
        var d2 = player(h, 2);
        h.assertTrue(mnemonist.progressionDialogue(d2).getStartNode().options().stream()
                .anyMatch(option -> "starter_choice".equals(option.nextNodeId())),
                "D2 Mnemonist did not offer the starter choice");
        DialogueEventHandler.onDialogueOption(new DialogueEvent(d2,
                "mnemonist_blank_memory_recipe", mnemonist.getId()));
        h.assertTrue(!d2.getRecipeBook().contains(Hemomancy.rloc("hematic_memory")),
                "Forged D2 lesson awarded the D3 blank recipe");
        d2.getPersistentData().putBoolean(MnemonistStarterMemoryChoice.CLAIM_KEY, true);
        h.assertTrue(mnemonist.progressionDialogue(d2).getStartNode().options().stream()
                .noneMatch(option -> "starter_choice".equals(option.nextNodeId())),
                "An earlier starter claim was offered again");

        var d3 = player(h, 3);
        var dialogue = mnemonist.progressionDialogue(d3);
        h.assertTrue(dialogue.getStartNode().options().stream().anyMatch(option ->
                        "mnemonist_blank_memory_recipe".equals(option.eventId())
                                && "woven_vessel".equals(option.nextNodeId())),
                "Woven Vessel did not teach the blank-memory recipe");
        h.assertTrue(!d3.getRecipeBook().contains(Hemomancy.rloc("hematic_memory")),
                "Blank recipe was awarded before the D3 lesson");
        DialogueEventHandler.onDialogueOption(new DialogueEvent(d3,
                "mnemonist_blank_memory_recipe", mnemonist.getId()));
        h.assertTrue(d3.getRecipeBook().contains(Hemomancy.rloc("hematic_memory")),
                "Mnemonist did not award the blank-memory recipe");
        HarbingerAdvancementGranter.grantIfNotDone(d3,
                HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE);
        h.assertTrue(mnemonist.progressionDialogue(d3).getStartNode().options().stream()
                .anyMatch(option -> "mnemonist_blank_memory_recipe".equals(option.eventId())
                        && "loom".equals(option.nextNodeId())),
                "Returning D3 learner lost the blank-memory lesson");
        h.succeed();
    }

    @GameTest(template = "empty") public static void chickenVialsProvideD3BlankMemoryEnzymeRoute(GameTestHelper h) {
        var p = player(h, 2);
        HemoCapabilityAccess.requireBloodVolume(p).setActive(true);
        var chicken = net.minecraft.world.entity.EntityType.CHICKEN.create(h.getLevel());
        var vial = new ItemStack(ItemInit.bloody_vial.get());
        ItemInit.bloody_vial.get().onLeftClickEntity(vial, p, chicken);
        h.assertTrue("minecraft:chicken".equals(BloodSampleData.rawSource(vial)),
                "D2 player could not collect chicken blood with an ordinary vial");
        var pos = h.absolutePos(new BlockPos(4, 2, 4));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.vial_centrifuge.get().defaultBlockState());
        var centrifuge = (VialCentrifugeBlockEntity) h.getLevel().getBlockEntity(pos);
        boolean neurotic = false;
        for (int i = 0; i < 64; i++) {
            var result = centrifuge.getResultFromVial(net.minecraft.world.entity.EntityType.CHICKEN);
            h.assertTrue(result.is(ItemInit.vivacious_enzyme.get()) || result.is(ItemInit.neurotic_enzyme.get()),
                    "Chicken centrifuging produced an inaccessible or unrelated enzyme");
            neurotic |= result.is(ItemInit.neurotic_enzyme.get());
        }
        h.assertTrue(neurotic, "Chicken blood never produced Neurotic Enzyme");
        h.assertTrue(h.getLevel().getRecipeManager().byKey(Hemomancy.rloc("hematic_memory")).isPresent(),
                "The blank Hematic Memory recipe did not load");
        h.succeed();
    }

    @GameTest(template = "empty") public static void craftedBlankCarriesWovenVesselIntoFirstLoomWeave(GameTestHelper h) {
        var p = player(h, 3);
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(h.getLevel());
        mnemonist.setPos(p.position());
        h.getLevel().addFreshEntity(mnemonist);
        DialogueEventHandler.onDialogueOption(new DialogueEvent(p,
                "mnemonist_blank_memory_recipe", mnemonist.getId()));
        h.assertTrue(p.getRecipeBook().contains(Hemomancy.rloc("hematic_memory")),
                "D3 player did not learn the blank recipe before crafting it");

        var recipe = h.getLevel().getRecipeManager().byKey(Hemomancy.rloc("hematic_memory"))
                .orElseThrow().value();
        var stoneRecipe = h.getLevel().getRecipeManager()
                .byKey(Hemomancy.rloc("blood_stained_stone_from_venous_stone"))
                .orElseThrow().value();
        var stoneInput = CraftingInput.of(2, 2, java.util.List.of(
                new ItemStack(BlockInit.venous_stone.get()),
                new ItemStack(ItemInit.sanguine_formation.get()),
                new ItemStack(Items.REDSTONE), ItemStack.EMPTY));
        h.assertTrue(stoneRecipe instanceof CraftingRecipe stoneCraft && stoneCraft.matches(stoneInput, h.getLevel()),
                "Main-route bloodcraft materials must make Blood Stained Stone without a Remnant expedition");
        var stone = ((CraftingRecipe) stoneRecipe).assemble(stoneInput, h.getLevel().registryAccess());
        h.assertTrue(stone.is(ItemInit.blood_stained_stone.get()), "The bloodcraft recipe did not yield a stained stone");
        var formation = new ItemStack(ItemInit.sanguine_formation.get());
        var input = CraftingInput.of(3, 3, java.util.List.of(
                formation, stone, formation,
                stone, new ItemStack(ItemInit.neurotic_enzyme.get()), stone,
                formation, stone, formation));
        h.assertTrue(recipe instanceof CraftingRecipe crafting && crafting.matches(input, h.getLevel()),
                "Accessible D3 ingredients do not match the taught blank recipe");
        var blank = ((CraftingRecipe) recipe).assemble(input, h.getLevel().registryAccess());
        h.assertTrue(blank.is(ItemInit.hematic_memory.get()), "The crafted blank is not a Hematic Memory");
        p.getInventory().add(blank);
        p.getInventory().add(new ItemStack(Items.BOOK));
        p.getInventory().add(new ItemStack(Items.INK_SAC));
        p.getInventory().add(new ItemStack(Items.PAPER, 3));
        DialogueEventHandler.onDialogueOption(new DialogueEvent(p,
                "mnemonist_woven_vessel_turn_in", mnemonist.getId()));
        h.assertTrue(HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(p)
                        && !HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(p),
                "Material turn-in either failed or incorrectly counted as the first weave");
        var rewards = h.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(mnemonist.blockPosition()).inflate(2));
        h.assertTrue(rewards.stream().anyMatch(item -> item.getItem().is(ItemInit.bleeding_bulb.get()))
                        && rewards.stream().anyMatch(item -> item.getItem().is(ItemInit.vivacious_enzyme.get())
                                && item.getItem().getCount() == 3),
                "Woven Vessel did not drop its authored Bulb and three enzymes");
        rewards.forEach(item -> item.playerTouch(p));
        h.assertTrue(p.getInventory().contains(new ItemStack(ItemInit.bleeding_bulb.get()))
                        && p.getInventory().contains(new ItemStack(ItemInit.vivacious_enzyme.get())),
                "Woven Vessel did not supply the first weave's Bulb and enzyme");

        var pos = h.absolutePos(new BlockPos(4, 2, 4));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.somatic_loom.get().defaultBlockState());
        var loom = (SomaticLoomBlockEntity) h.getLevel().getBlockEntity(pos);
        loom.addItem(p, p.getInventory().items.stream().filter(stack -> stack.is(ItemInit.hematic_memory.get()))
                .findFirst().orElseThrow(), null);
        loom.addItem(p, p.getInventory().items.stream().filter(stack -> stack.is(ItemInit.bleeding_bulb.get()))
                .findFirst().orElseThrow(), null);
        loom.addItem(p, p.getInventory().items.stream().filter(stack -> stack.is(ItemInit.vivacious_enzyme.get()))
                .findFirst().orElseThrow(), null);
        h.assertTrue(loom.selectRecipe(p, Hemomancy.rloc("memory_weaving/memory_blood_shot"))
                        && loom.getResultItem().is(ItemInit.memory_blood_shot.get()),
                "The supplied D3 materials cannot select the authored Blood Shot weave");
        var blood = HemoCapabilityAccess.requireBloodVolume(p);
        blood.setActive(true);
        blood.setBloodVolume(100);
        h.assertTrue(loom.startRitual(p) && loom.tryChargeRitualBlood(p, 50, true)
                        && loom.isWeavingOrbs() && blood.getBloodVolume() == 50,
                "The first weave did not accept its 50 blood charge");
        for (var orb : loom.getRitualOrbs()) {
            var orbPos = Vec3.atCenterOf(pos).add(orb.offset());
            p.setPos(orbPos.x + 2, orbPos.y - p.getEyeHeight(), orbPos.z);
            p.lookAt(EntityAnchorArgument.Anchor.EYES, orbPos);
            h.assertTrue(loom.dragSelectedOrb(p, 1), "The player could not catch the first strand");
            var target = Vec3.atCenterOf(pos).add(0, 0.4, 0);
            double eyeY = pos.getY() + 0.2 + p.getEyeHeight();
            double horizontal = Math.sqrt(4 - Math.pow(eyeY - target.y, 2));
            p.setPos(target.x + horizontal, pos.getY() + 0.2, target.z);
            p.lookAt(EntityAnchorArgument.Anchor.EYES, target);
            for (int pull = 0; pull < 200 && !orb.completed(); pull++) loom.dragSelectedOrb(p, 1);
            h.assertTrue(orb.completed(), "The first strand could not be drawn into the Loom");
        }
        h.assertTrue(HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(p)
                        && HarbingerAdvancementGranter.isMnemonistWovenVesselFinished(p),
                "Completing Blood Shot did not finish the Woven Vessel chapter proof");
        h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2))
                        .stream().anyMatch(item -> item.getItem().is(ItemInit.memory_blood_shot.get())),
                "The first completed weave did not produce Blood Shot memory");
        p.discard();
        h.succeed();
    }

    @GameTest(template = "empty") public static void mnemonistStationLessonsFollowDegreeWithoutNewPromotionGates(GameTestHelper h) {
        var p = player(h, 3);
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(h.getLevel());
        mnemonist.setPos(p.position());
        var d3 = mnemonist.progressionDialogue(p);
        h.assertTrue(d3.getNode("scriptorium") != null && d3.getNode("memory_practice") == null
                        && d3.getNode("distributor") == null,
                "D3 station lessons are missing or later practice arrived early");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(4);
        var d4 = mnemonist.progressionDialogue(p);
        h.assertTrue(d4.getNode("memory_practice") != null && d4.getNode("distributor") == null,
                "D4 did not deepen memory selection or exposed the D5 Distributor");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        var d5 = mnemonist.progressionDialogue(p);
        h.assertTrue(d5.getNode("distributor") != null
                        && d5.getNode("scriptorium").lines().contains("hemomancy.mnemonist.scriptorium.eightfold"),
                "D5 did not teach named loadouts and the Eightfold Script");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(7);
        h.assertTrue(mnemonist.progressionDialogue(p).getNode("scriptorium").lines()
                        .contains("hemomancy.mnemonist.scriptorium.monolithic"),
                "D7 did not name the Monolithic Script");
        h.succeed();
    }

    @GameTest(template = "empty") public static void alchemistRecognizesEarlierTinctureWorkAtD3(GameTestHelper h) {
        var p = player(h, 3);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setPos(p.position());
        var unbriefed = alchemist.progressionDialogue(p);
        h.assertTrue(unbriefed.getNode("thelemic_preparations") != null
                        && unbriefed.getNode("thelemic_preparations").lines()
                                .contains("hemomancy.alchemist.preparations.unbriefed")
                        && unbriefed.getNode("advanced_condenser") != null
                        && unbriefed.getNode("advanced_athanor") == null,
                "D3 preparations or existing Condenser access were not explained");
        HarbingerAdvancementGranter.grantIfNotDone(p, BodyAnswersAssignment.ADV_BRIEFED);
        h.assertTrue(alchemist.progressionDialogue(p).getNode("thelemic_preparations").lines()
                        .contains("hemomancy.alchemist.preparations.briefed"),
                "An already briefed player was treated as new to Body Answers");
        HarbingerAdvancementGranter.grantIfNotDone(p, BodyAnswersAssignment.ADV_COMPLETE);
        h.assertTrue(alchemist.progressionDialogue(p).getNode("thelemic_preparations").lines()
                        .contains("hemomancy.alchemist.preparations.complete"),
                "An earlier tincture drink was not recognized at D3");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        h.assertTrue(alchemist.progressionDialogue(p).getNode("advanced_athanor") != null,
                "D5 Athanor eligibility disappeared from the existing station progression");
        h.succeed();
    }

    @GameTest(template = "empty") public static void specimenJarLessonGrantsOnlyNewStudentsOnce(GameTestHelper h) {
        var p = player(h, 2);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setPos(p.position());
        h.assertTrue(SpecimenJarLesson.canTeach(p), "New D2 player cannot request the jar lesson");
        var dialogue = alchemist.progressionDialogue(p);
        h.assertTrue(dialogue.getNode("living_bestiary_intro").options().stream()
                .anyMatch(option -> com.vincenthuto.hemomancy.common.entity.npc.dialogue.SpecimenJarLessonDialogue.TEACH.equals(option.eventId())),
                "D2 Living Bestiary dialogue has no jar lesson action");
        h.assertTrue(dialogue.getNode("red_taxonomy_intro").options().stream()
                .anyMatch(option -> "bloodwood_lesson".equals(option.nextNodeId())),
                "D2 Red Taxonomy dialogue has no bloodwood instruction");
        h.assertTrue(SpecimenJarLesson.teach(p, alchemist), "Alchemist did not teach the jar lesson");
        h.assertTrue(!SpecimenJarLesson.teach(p, alchemist), "Jar lesson could be claimed twice");
        h.assertTrue(jarCount(p) == 2, "Jar lesson did not give exactly two jars");
        h.assertTrue(alchemist.progressionDialogue(p).getNode("living_bestiary_intro").options().stream()
                .noneMatch(option -> com.vincenthuto.hemomancy.common.entity.npc.dialogue.SpecimenJarLessonDialogue.TEACH.equals(option.eventId())),
                "Claimed jar lesson remained available in dialogue");
        var restored = player(h, 2);
        restored.getPersistentData().put(Player.PERSISTED_NBT_TAG,
                p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy());
        h.assertTrue(!SpecimenJarLesson.canTeach(restored), "Jar lesson claim was lost after player data restore");

        var experienced = player(h, 2);
        var otherAlchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        otherAlchemist.setPos(experienced.position());
        HemoCapabilityAccess.requireSpecimenBestiary(experienced).recordSpecimen(Hemomancy.rloc("chitinite"));
        h.assertTrue(SpecimenJarLesson.teach(experienced, otherAlchemist), "Experienced player could not hear the lesson");
        h.assertTrue(jarCount(experienced) == 0, "Existing specimen researcher received duplicate starter jars");
        h.succeed();
    }

    private static int jarCount(ServerPlayer player) {
        return player.getInventory().items.stream()
                .filter(stack -> stack.is(BlockInit.specimen_jar.get().asItem()))
                .mapToInt(ItemStack::getCount).sum();
    }

	@GameTest(template = "empty") public static void firstDrawsCountsCollectedAnimalsAndCarriesSampleToD2(GameTestHelper h) {
		var p = player(h, 1);
		HemoCapabilityAccess.requireBloodVolume(p).setActive(true);
		HemoCapabilityAccess.requireEquipment(p).setStackInSlot(5,
				new ItemStack(ItemInit.charm_of_vascularium.get()));
		var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
		alchemist.setPos(p.position());
		h.assertTrue(alchemist.progressionDialogue(p).getStartNode().options().stream()
				.anyMatch(option -> "first_draws_offer".equals(option.nextNodeId())),
				"D1 dialogue did not offer First Draws");
		h.assertTrue(DialogueHubFactory.decorate(alchemist.progressionDialogue(p), "alchemist", p)
				.presentation().topics(DialogueCategory.QUESTS).stream()
				.anyMatch(topic -> "first_draws_offer".equals(topic.targetNodeId())),
				"First Draws did not appear among Alchemist assignments");
		h.assertTrue(FirstDrawsAssignment.brief(p, alchemist), "D1 Alchemist did not offer First Draws");
		h.assertTrue(!FirstDrawsAssignment.brief(p, alchemist), "Repeat briefing duplicated the one-time supply claim");
		h.assertTrue(p.getRecipeBook().contains(Hemomancy.rloc("bloody_vial")),
				"First Draws did not teach the replacement Blood Vial recipe");
		var firstEmpty = new ItemStack(ItemInit.bloody_vial.get());
		p.setItemInHand(InteractionHand.MAIN_HAND, firstEmpty);
		ItemInit.bloody_vial.get().onLeftClickEntity(firstEmpty, p,
				net.minecraft.world.entity.EntityType.COW.create(h.getLevel()));
		ItemInit.bloody_vial.get().onLeftClickEntity(p.getMainHandItem(), p,
				net.minecraft.world.entity.EntityType.COW.create(h.getLevel()));
		h.assertTrue(FirstDrawsAssignment.progress(p).samples() == 1,
				"Inspecting a filled vial counted as a second collection");
		for (var type : java.util.List.of(net.minecraft.world.entity.EntityType.COW,
				net.minecraft.world.entity.EntityType.COW,
				net.minecraft.world.entity.EntityType.PIG, net.minecraft.world.entity.EntityType.SHEEP)) {
			var empty = new ItemStack(ItemInit.bloody_vial.get());
			p.setItemInHand(InteractionHand.MAIN_HAND, empty);
			ItemInit.bloody_vial.get().onLeftClickEntity(empty, p, type.create(h.getLevel()));
		}
		h.assertTrue(FirstDrawsAssignment.progress(p).complete(), "Five real samples from three animals were not recorded");
		h.assertTrue(!ClinicalBloodKnowledge.canInject(p), "D1 collection unlocked ordinary injection");
		var restored = player(h, 1);
		restored.getPersistentData().put(Player.PERSISTED_NBT_TAG,
				p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy());
		h.assertTrue(FirstDrawsAssignment.progress(restored).complete(), "First Draws was lost after player data restore");
		HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(2);
		FirstSeparationAssignment.markBriefed(p);
		h.assertTrue(FirstSeparationAssignment.hasSampleAcquired(p),
				"The D1 sample in inventory was not recognized at D2 briefing");
		h.succeed();
	}

	@GameTest(template = "empty") public static void firstBloodcraftProofsPromoteOnlyAfterVicarReturn(GameTestHelper h) {
		var p = player(h, 1);
		FirstBloodcraftAssignment.recordStructure(p, new ItemStack(BlockInit.hematic_iron_block.get()));
		FirstBloodcraftAssignment.recordVenousStone(p);
		FirstBloodcraftAssignment.recordAbsorption(p, 300);
		h.assertTrue(!FirstBloodcraftAssignment.canClaim(p), "Incomplete D1 lessons allowed promotion");
		FirstBloodcraftAssignment.recordFormation(p);
		FirstBloodcraftAssignment.recordAbsorption(p, 200);
		h.assertTrue(FirstBloodcraftAssignment.canClaim(p), "All four D1 proofs did not unlock the Vicar return");
		h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(p) == 1,
				"Completing proofs promoted the player before speaking to the Vicar");
		h.assertTrue(FirstBloodcraftAssignment.promote(p), "The Vicar could not grant Degree 2");
		h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(p) == 2,
				"The Vicar return did not grant Degree 2");
		h.succeed();
	}

	@GameTest(template = "empty") public static void firstSeparationSamplingProofSurvivesUsingTheVial(GameTestHelper h) {
		var p = player(h, 2);
		FirstSeparationAssignment.markBriefed(p);
		var vial = new ItemStack(ItemInit.bloody_vial.get());
		p.setItemInHand(InteractionHand.MAIN_HAND, vial);
		ItemInit.bloody_vial.get().onLeftClickEntity(vial, p,
				net.minecraft.world.entity.EntityType.COW.create(h.getLevel()));
		h.assertTrue(FirstSeparationAssignment.hasSampleAcquired(p),
				"The First Separation did not remember a successful living sample");
		p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		h.assertTrue(FirstSeparationAssignment.hasSampleAcquired(p),
				"Using the sampled vial erased completed ledger progress");
		var restored = player(h, 2);
		restored.getPersistentData().put(Player.PERSISTED_NBT_TAG,
				p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy());
		h.assertTrue(FirstSeparationAssignment.hasSampleAcquired(restored),
				"The completed sample step did not survive persisted player restoration");
		h.succeed();
	}

	@GameTest(template = "empty") public static void assignmentLedgerPacketRoundTripsEveryField(GameTestHelper h) {
		try {
			RecordComponent[] components = OpenHarbingerAssignmentLedgerPacket.class.getRecordComponents();
			Class<?>[] parameterTypes = new Class<?>[components.length];
			Object[] values = new Object[components.length];
			for (int i = 0; i < components.length; i++) {
				parameterTypes[i] = components[i].getType();
				values[i] = "circusDiscovered".equals(components[i].getName())
						? true : ledgerPacketValue(components[i].getType(), i);
			}
			var constructor = OpenHarbingerAssignmentLedgerPacket.class.getDeclaredConstructor(parameterTypes);
			var original = (OpenHarbingerAssignmentLedgerPacket) constructor.newInstance(values);
			FriendlyByteBuf buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
			try {
				OpenHarbingerAssignmentLedgerPacket.STREAM_CODEC.encode(buffer, original);
				h.assertTrue(original.equals(OpenHarbingerAssignmentLedgerPacket.STREAM_CODEC.decode(buffer)),
						"The assignment ledger packet changed fields during its network round trip");
				h.assertTrue(buffer.readableBytes() == 0,
						"The assignment ledger packet left unread network data");
			} finally {
				buffer.release();
			}
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not construct the assignment ledger packet", exception);
		}
		h.succeed();
	}

	private static Object ledgerPacketValue(Class<?> type, int index) {
		if (type == int.class) return index * 17 + 3;
		if (type == boolean.class) return index % 3 == 0;
		if (type == FirstSeparationLedgerProgress.class) {
			return new FirstSeparationLedgerProgress(true, false, true, false, true, false, true, true, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurveyProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurveyProgress(
					true, 2, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroductionProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroductionProgress(true, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingProgress(true, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.alchemist.DeepDarkCommissionProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.alchemist.DeepDarkCommissionProgress(true, false);
		}
		if (type == PhlegethonticCommissionProgress.class) {
			return new PhlegethonticCommissionProgress(true, 4, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiryProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiryProgress(
					true, true, true, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftLedgerProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftLedgerProgress(
					275, true, false, true, false);
		}
		throw new IllegalArgumentException("No packet test value for " + type.getName());
	}

    @GameTest(template = "empty") public static void phlegethonticCommissionChecksBothSourcesAndFiveItems(GameTestHelper h) {
        var p = player(h, 5);
        HemoCapabilityAccess.requireBloodVolume(p).setActive(true);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setNoAi(true); alchemist.setPos(p.position()); h.getLevel().addFreshEntity(alchemist);
        var vicar = EntityInit.harbinger_vicar.get().create(h.getLevel());
        vicar.setNoAi(true); vicar.setPos(p.position()); h.getLevel().addFreshEntity(vicar);
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(4);
        h.assertTrue(alchemist.progressionDialogue(p).getNode("phlegethontic_commission") == null,
                "Degree 4 saw the formal Nether commission");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        h.assertTrue(alchemist.progressionDialogue(p).getNode("phlegethontic_commission") != null,
                "Degree 5 could not reach the Nether commission");
        var scyphus = BlockInit.escharian_scyphus.get().asItem();
        p.getInventory().setItem(9, new ItemStack(scyphus, 5));
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.bloody_vial.get()));
        h.assertTrue(!PhlegethonticCommission.report(p, alchemist), "Empty vial satisfied the sample step");
        p.setItemInHand(InteractionHand.MAIN_HAND, vial("hemomancy:missing_source"));
        h.assertTrue(!PhlegethonticCommission.report(p, alchemist), "Unreadable vial satisfied the sample step");
        var unrelated = vial("minecraft:creeper");
        BloodSampleData.identify(unrelated);
        p.setItemInHand(InteractionHand.MAIN_HAND, unrelated);
        h.assertTrue(!PhlegethonticCommission.report(p, alchemist), "Unrelated blood satisfied the sample step");
        var excoriated = vial("hemomancy:excoriated");
        p.setItemInHand(InteractionHand.MAIN_HAND, excoriated);
        h.assertTrue(PhlegethonticCommission.validSample(excoriated),
                "A valid source-bearing Excoriated vial required unrelated microscope identification");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(4);
        h.assertTrue(!PhlegethonticCommission.report(p, alchemist),
                "Degree 4 completed a Degree 5 commission with otherwise valid proof");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        p.getInventory().setItem(9, new ItemStack(scyphus, 4));
        h.setBlock(new BlockPos(3, 1, 3), Blocks.BLACKSTONE);
        h.setBlock(new BlockPos(3, 2, 3), BlockInit.escharian_scyphus.get().defaultBlockState()
                .setValue(EscharianScyphusBlock.COUNT, 5));
        h.assertTrue(!PhlegethonticCommission.report(p, alchemist), "Four Scyphus items satisfied a five-item commission");
        p.getInventory().add(new ItemStack(scyphus));
        h.assertTrue(!PhlegethonticCommission.report(p, vicar), "The Vicar accepted the Alchemist's commission");
        h.assertTrue(DialogueOptionPacket.dispatch(p, PhlegethonticCommissionDialogue.BEARING, vicar.getId()) == null,
                "The Vicar dispatched the Alchemist's Basin bearing");
        var bearing = DialogueOptionPacket.dispatch(p, PhlegethonticCommissionDialogue.BEARING, alchemist.getId());
        h.assertTrue(bearing != null && bearing.wasRewardDelivered(), "Alchemist could not give a Basin bearing");
        var before = excoriated.copy();
        var state = PhlegethonticCommission.progress(p);
        h.assertTrue(state.ready(), "Ready state: sample=" + state.sampleProof()
                + ", Scyphus=" + state.scyphusCount() + ", eligible=" + PhlegethonticCommission.eligible(p)
                + ", held=" + p.getMainHandItem() + ", identified=" + BloodSampleData.identified(p.getMainHandItem()));
        var report = DialogueOptionPacket.dispatch(p, PhlegethonticCommissionDialogue.REPORT, alchemist.getId());
        h.assertTrue(report != null && report.wasRewardDelivered(), "Valid Excoriated sample was refused");
        h.assertTrue(ItemStack.isSameItemSameComponents(before, p.getMainHandItem())
                        && p.getInventory().countItem(scyphus) == 5,
                "Commission consumed the specimen or Scyphus items");
        h.assertTrue(!PhlegethonticCommission.report(p, alchemist), "Commission was accepted twice");
        p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        p.getInventory().clearContent();
        h.assertTrue(PhlegethonticCommission.progress(p).complete(), "Completed proof vanished with the items");
        var restored = player(h, 5);
        restored.getPersistentData().put(Player.PERSISTED_NBT_TAG,
                p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy());
        h.assertTrue(PhlegethonticCommission.progress(restored).complete()
                        && PhlegethonticCommission.progress(restored).sampleProof()
                        && PhlegethonticCommission.progress(restored).scyphusCount() == 5,
                "Saved commission did not retain its completed ledger steps");

        var bombardier = player(h, 5);
        HemoCapabilityAccess.requireBloodVolume(bombardier).setActive(true);
        var bombardierSample = vial("hemomancy:phlegethontic_bombardier");
        bombardier.setItemInHand(InteractionHand.MAIN_HAND, bombardierSample);
        bombardier.getInventory().add(new ItemStack(scyphus, 5));
        h.assertTrue(PhlegethonticCommission.report(bombardier, alchemist),
                "Valid Bombardier sample was refused");
        alchemist.discard(); vicar.discard(); h.succeed();
    }

    @GameTest(template = "empty") public static void vagrantMindDiscoverySupportsTwoIndependentReports(GameTestHelper h) {
        var end = h.getLevel().getServer().getLevel(net.minecraft.world.level.Level.END);
        h.assertTrue(end != null, "End is unavailable for the Vagrant Mind inquiry test");
        var p = player(h, 6, end);
        end.getChunkAt(p.blockPosition());
        HemoCapabilityAccess.requireBloodVolume(p).setActive(true);
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(end);
        var alchemist = EntityInit.harbinger_alchemist.get().create(end);
        mnemonist.setPos(p.position()); alchemist.setPos(p.position());
        end.addFreshEntity(mnemonist); end.addFreshEntity(alchemist);
        com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.observeMind(p);
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.progress(p)
                .mindVisited(), "Being anywhere in the End counted as entering the Mind");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        h.assertTrue(mnemonist.progressionDialogue(p).getNode("vagrant_memory_inquiry") == null
                        && alchemist.progressionDialogue(p).getNode("vagrant_biology_inquiry") == null,
                "Degree 5 saw the Degree 6 Mind inquiries");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(6);
        h.assertTrue(mnemonist.progressionDialogue(p).getNode("vagrant_memory_inquiry") != null,
                "Mnemonist does not offer the D6 Mind inquiry");
        h.assertTrue(alchemist.progressionDialogue(p).getNode("vagrant_biology_inquiry") != null,
                "Alchemist does not offer the D6 biology inquiry");
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry
                .reportMemory(p, mnemonist), "Mind report accepted without a visit");
        var data = p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        var inquiry = new CompoundTag();
        inquiry.putBoolean("MindVisited", true);
        data.put("hemomancy:vagrant_mind_inquiry", inquiry);
        p.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry
                .reportBiology(p, alchemist), "Biology report accepted without an observation");
        var chorus = p.blockPosition().offset(2, 0, 0);
        end.setBlockAndUpdate(chorus.below(), Blocks.END_STONE.defaultBlockState());
        end.setBlockAndUpdate(chorus, Blocks.CHORUS_FLOWER.defaultBlockState());
        com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.observeBiology(p);
        h.assertTrue(com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.progress(p)
                .biologyObserved(), "Nearby End chorus was not recorded as biology evidence");
        h.assertTrue(DialogueOptionPacket.dispatch(p, "mnemonist_vagrant_memory_report", alchemist.getId()) == null,
                "Alchemist dispatched the Mnemonist's report");
        h.assertTrue(DialogueOptionPacket.dispatch(p, "alchemist_vagrant_biology_report", mnemonist.getId()) == null,
                "Mnemonist dispatched the Alchemist's report");
        p.getInventory().setItem(9, new ItemStack(ItemInit.naeglerophaeon_ganglion.get()));
        var memoryReport = DialogueOptionPacket.dispatch(p, "mnemonist_vagrant_memory_report", mnemonist.getId());
        h.assertTrue(memoryReport != null && memoryReport.wasRewardDelivered(),
                "Mnemonist refused the shared Mind discovery");
        var biologyReport = DialogueOptionPacket.dispatch(p, "alchemist_vagrant_biology_report", alchemist.getId());
        h.assertTrue(biologyReport != null && biologyReport.wasRewardDelivered(),
                "Alchemist refused the same discovery plus Choir observation");
        var progress = com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.progress(p);
        h.assertTrue(progress.memoryReported() && progress.biologyReported(),
                "The two reports did not persist independently");
        h.assertTrue(p.getInventory().countItem(ItemInit.naeglerophaeon_ganglion.get()) == 1,
                "The paired reports consumed the unique ganglion");
        var restored = player(h, 6, end);
        restored.getPersistentData().put(Player.PERSISTED_NBT_TAG,
                p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy());
        var saved = com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.progress(restored);
        h.assertTrue(saved.mindVisited() && saved.biologyObserved()
                        && saved.memoryReported() && saved.biologyReported(),
                "The shared discovery or paired reports did not survive saved-data restoration");
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry
                .reportMemory(p, mnemonist) && !com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry
                .reportBiology(p, alchemist), "A completed report was accepted twice");
        mnemonist.discard(); alchemist.discard(); h.succeed();
    }

    @GameTest(template = "empty") public static void silentArchonBonusWaitsForProvenVictory(GameTestHelper h) {
        var p = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(p);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING);
        h.assertTrue(com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior.claimedWillBonusCap(p) == 0,
                "Pending refusal received the Silent Archon summon cap");
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_ARCHON);
        h.assertTrue(com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior.claimedWillBonusCap(p) == 1,
                "Proven Silent Archon did not receive the configured summon cap");
        degree.setDegreeNumber(8);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.APOTHEOS);
        h.assertTrue(com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior.claimedWillBonusCap(p) == 0,
                "Apotheos retained the Silent Archon summon cap");
        h.succeed();
    }

    @GameTest(template = "empty") public static void recruitedOutpostNpcCanHoldVigilStationWithoutSuccession(GameTestHelper h) {
        var p = player(h, 6);
        var data = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData
                .get(h.getLevel().getServer().overworld());
        var line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline(
                "Vigil helper", p.getUUID(), UUID.randomUUID(), new java.util.ArrayList<>());
        data.registerBloodline(line);
        HemoCapabilityAccess.requireBloodVolume(p).setBloodLine(line);
        var recipeId = Hemomancy.rloc("cardinal_rite/covenant_vigil");
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe
                .getRiteByLocation(h.getLevel(), recipeId);
        h.assertTrue(recipe != null, "Covenant Vigil recipe is missing");
        BlockPos center = h.absolutePos(new BlockPos(6, 3, 6));
        var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(
                p.getUUID(), center, recipeId, 1200, 5, 6, false, 0);
        for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
        h.assertTrue(rite.enterInscription(), "Vigil did not enter helper assignment phase");
        BlockPos station = center.offset(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .markers(recipe).get(com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.ANCHOR));
        h.getLevel().setBlock(station.below(), Blocks.STONE.defaultBlockState(), 3);
        h.getLevel().setBlock(station, Blocks.AIR.defaultBlockState(), 3);
        h.getLevel().setBlock(station.above(), Blocks.AIR.defaultBlockState(), 3);
        var npc = EntityInit.harbinger_vicar.get().create(h.getLevel());
        h.assertTrue(npc != null, "Vicar helper could not be created");
        npc.setPos(station.getX() + 0.5, station.getY(), station.getZ() + 0.5);
        npc.setNoAi(true);
        h.assertTrue(h.getLevel().addFreshEntity(npc), "Vicar helper could not be spawned");
        h.assertTrue(!npc.isSuccessor(), "Test NPC unexpectedly entered Succession");
        h.assertTrue(!com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .tryAssignNpc(h.getLevel(), p, rite, npc), "Unrecruited outpost Vicar was accepted");
        var updated = data.addNpcMember(line.getBloodlineUUID(), npc.getUUID(),
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(npc.getType()));
        h.assertTrue(updated != null, "Recruitment fixture failed");
        HemoCapabilityAccess.requireBloodVolume(p).setBloodLine(updated);
        h.assertTrue(h.getLevel().hasChunkAt(station), "Vigil station chunk was not loaded");
        h.assertTrue(h.getLevel().getBlockState(station.below()).isFaceSturdy(
                h.getLevel(), station.below(), net.minecraft.core.Direction.UP),
                "Vigil station lacked sturdy support");
        h.assertTrue(h.getLevel().noCollision(npc, npc.getBoundingBox()),
                "Vigil station collided with the recruited Vicar");
        h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .tryAssignNpc(h.getLevel(), p, rite, npc), "Recruited outpost Vicar could not take the station");
        h.assertTrue(rite.getAllyRoles().containsKey(npc.getUUID()),
                "Recruited Vicar passed interaction but was not assigned to a role");
        h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .isAvailable(h.getLevel(), rite, npc.getUUID()),
                "Recruited Vicar did not count as a live helper at " + npc.blockPosition()
                        + " for marker " + station);
        npc.discard();
        h.assertTrue(!com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .hasRequiredHelpers(h.getLevel(), rite), "Lost Vicar still satisfied the Vigil helper requirement");
        var guest = player(h, 5);
        data.addMember(line.getBloodlineUUID(), guest.getUUID());
        guest.setPos(station.getCenter());
        h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .tryClaimPlayerRole(h.getLevel(), guest, rite, station), "Guest could not claim the lost helper's station");
        h.assertTrue(rite.getAllyRoles().get(guest.getUUID())
                        == com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.ANCHOR
                        && !rite.getAllyRoles().containsKey(npc.getUUID()),
                "Lost helper kept its station when a bloodline player claimed it");
        rite.removeAlly(guest.getUUID());
        data.removeNpcMember(line.getBloodlineUUID(), npc.getUUID());
        for (var role : com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.values()) {
            var missing = UUID.randomUUID();
            data.addNpcMember(line.getBloodlineUUID(), missing);
            rite.assignAlly(missing, role);
        }
        var replacement = EntityInit.harbinger_vicar.get().create(h.getLevel());
        h.assertTrue(replacement != null, "Replacement Vicar could not be created");
        replacement.setPos(station.getX() + 0.5, station.getY(), station.getZ() + 0.5);
        replacement.setNoAi(true);
        h.assertTrue(h.getLevel().addFreshEntity(replacement), "Replacement Vicar could not be spawned");
        h.assertTrue(data.addNpcMember(line.getBloodlineUUID(), replacement.getUUID(),
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(replacement.getType())) != null,
                "Replacement Vicar could not join the bloodline");
        h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .tryAssignNpc(h.getLevel(), p, rite, replacement), "Replacement Vicar interaction was rejected");
        h.assertTrue(rite.getAllyRoles().containsKey(replacement.getUUID()),
                "Missing NPC reservations blocked a legitimate replacement at full quota");
        com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .returnNpcAlliesToFane(h.getLevel(), rite);
        h.assertTrue(rite.getAllyRoles().isEmpty(), "Lost original helper retained an assignment after cleanup");
        replacement.discard();
        guest.discard();
        h.succeed();
    }

    @GameTest(template = "empty") public static void ledgerSummonUsesOriginalAndNeverInventsMissingRecruit(GameTestHelper h) {
        var outpost = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules
                .createOutpostKey(Hemomancy.rloc("test_dimension"),
                        new net.minecraft.world.level.levelgen.structure.BoundingBox(-17, 60, 32, 30, 90, 79));
        var origin = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules
                .outpostOrigin(outpost).orElseThrow();
        h.assertTrue(origin.minChunkX() == -2 && origin.maxChunkX() == 1
                        && origin.minChunkZ() == 2 && origin.maxChunkZ() == 4,
                "Saved outpost bounds did not resolve to their covered chunks");
        h.assertTrue(com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules
                .outpostOrigin("minecraft:overworld|harbinger_outpost|0,0,0|9999,0,9999").isEmpty(),
                "Unbounded outpost origin could force-load arbitrary chunks");
        var p = player(h, 6);
        setServerPlayerLookup(p, true);
        h.assertTrue(p.server.getPlayerList().getPlayer(p.getUUID()) == p,
                "Ledger fixture was not visible in the server player lookup");
        var overworld = h.getLevel().getServer().overworld();
        var data = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData
                .get(overworld);
        var line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline(
                "Ledger summon", p.getUUID(), UUID.randomUUID(), new java.util.ArrayList<>());
        data.registerBloodline(line);
        var volume = HemoCapabilityAccess.requireBloodVolume(p);
        volume.setBloodLine(line);
        com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData.get(h.getLevel())
                .consecrateHeart(p.getUUID(), p.blockPosition());
        var vicar = EntityInit.harbinger_vicar.get().create(h.getLevel());
        h.assertTrue(vicar != null, "Recruited Vicar could not be created");
        vicar.setPos(p.getX() + 2, p.getY() + 25, p.getZ() + 2);
        vicar.setNoAi(true);
        vicar.addTag("original-ledger-recruit");
        h.assertTrue(h.getLevel().addFreshEntity(vicar), "Recruited Vicar could not be spawned");
        UUID vicarId = vicar.getUUID();
        UUID missingId = UUID.randomUUID();
        line.addNpcMember(vicarId);
        BlockPos remote = p.blockPosition().offset(1024, 0, 1024);
        String remoteOutpost = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules
                .createOutpostKey(h.getLevel().dimension().location(),
                        new net.minecraft.world.level.levelgen.structure.BoundingBox(
                                remote.getX(), remote.getY(), remote.getZ(),
                                remote.getX(), remote.getY(), remote.getZ()));
        line.addNpcMember(missingId, null, remoteOutpost);
        h.assertTrue(!h.getLevel().hasChunkAt(remote), "Remote outpost chunk was already loaded");
        h.runAfterDelay(2, () -> {
            h.assertTrue(h.getLevel().getEntity(vicarId) == vicar, "Original Vicar was not loaded");
            invokeLedgerSummon(p, volume);
            var arrived = h.getLevel().getEntity(vicarId);
            h.assertTrue(arrived == vicar && arrived.getType() == vicar.getType()
                            && arrived.getTags().contains("original-ledger-recruit")
                            && arrived.distanceTo(p) < 8.0,
                    "Ledger did not bring the original recruited Vicar to the Fane");
            h.assertTrue(h.getLevel().getEntity(missingId) == null,
                    "Ledger manufactured a replacement for a missing recruited NPC");
            h.assertTrue(h.getLevel().hasChunkAt(remote),
                    "Ledger did not inspect the recorded outpost chunk for an unloaded recruit");
            var late = EntityInit.harbinger_vicar.get().create(h.getLevel());
            h.assertTrue(late != null, "Delayed outpost Vicar could not be created");
            late.setUUID(missingId);
            late.setPos(p.getX() + 2, p.getY() + 25, p.getZ() + 2);
            late.setNoAi(true);
            late.setNoGravity(true);
            late.addTag("late-ledger-recruit");
            h.assertTrue(h.getLevel().addFreshEntity(late), "Delayed outpost Vicar could not be spawned");
            h.runAfterDelay(5, () -> {
                var delayedArrival = h.getLevel().getEntity(missingId);
                h.assertTrue(delayedArrival == late && delayedArrival.getTags().contains("late-ledger-recruit")
                                && delayedArrival.distanceTo(p) < 8.0,
                        "Ledger did not finish the pending summon when the original NPC loaded");
                UUID cancelledId = UUID.randomUUID();
                h.assertTrue(line.removeNpcMember(missingId), "Completed recruit could not leave the test bloodline");
                h.assertTrue(line.addNpcMember(cancelledId, null, remoteOutpost),
                        "Cancellation recruit was not added to the bloodline");
                invokeLedgerSummon(p, volume);
                p.setPos(p.getX() + 100, p.getY(), p.getZ());
                var cancelled = EntityInit.harbinger_vicar.get().create(h.getLevel());
                h.assertTrue(cancelled != null, "Cancellation Vicar could not be created");
                cancelled.setUUID(cancelledId);
                cancelled.setPos(arrived.getX(), arrived.getY() + 25, arrived.getZ());
                cancelled.setNoAi(true);
                cancelled.setNoGravity(true);
                h.assertTrue(h.getLevel().addFreshEntity(cancelled), "Cancellation Vicar could not be spawned");
                Vec3 originalPosition = cancelled.position();
                h.runAfterDelay(3, () -> {
                    h.assertTrue(cancelled.position().distanceToSqr(originalPosition) < 1.0,
                            "Pending ledger summon moved an NPC after the player left the Fane");
                    arrived.discard();
                    delayedArrival.discard();
                    cancelled.discard();
                    data.disbandBloodline(line.getBloodlineUUID());
                    com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData.get(h.getLevel())
                            .remove(p.getUUID());
                    setServerPlayerLookup(p, false);
                    h.succeed();
                });
            });
        });
    }

    private static void invokeLedgerSummon(ServerPlayer player,
            com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume volume) {
        try {
            var summon = com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction.class
                    .getDeclaredMethod("handleNpcSummon", ServerPlayer.class,
                            com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume.class);
            summon.setAccessible(true);
            summon.invoke(null, player, volume);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not invoke ledger summon", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void setServerPlayerLookup(ServerPlayer player, boolean present) {
        try {
            var field = net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");
            field.setAccessible(true);
            var players = (java.util.Map<UUID, ServerPlayer>) field.get(player.server.getPlayerList());
            if (present) players.put(player.getUUID(), player);
            else players.remove(player.getUUID(), player);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not register ledger GameTest player", e);
        }
    }

    @GameTest(template = "empty") public static void personalExaminationCountsUniqueSourcesAndUnlocksOrderedCollection(GameTestHelper h) {
        var p = player(h, 2);
        var state = HemoCapabilityAccess.clinicalBlood(p);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel()); alchemist.setPos(p.position());
        state.learn(MICROSCOPE);
        var pig = vial("minecraft:pig");
        examine(h, p, pig);
        h.assertTrue(state.sourceCount() == 1, "First successful examination was not recorded");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, alchemist, INJECTION), "Examined sample did not qualify for injection lesson");
        examine(h, p, pig.copy());
        h.assertTrue(state.sourceCount() == 1, "Duplicate source advanced count");
        h.assertTrue(!ClinicalBloodKnowledge.canLearn(p, CABINET), "Cabinet unlocked at first source");
        var cow = vial("minecraft:cow"); BloodSampleData.identify(cow);
        p.getInventory().add(cow.copy());
        h.assertTrue(state.sourceCount() == 1, "Traded identification granted personal proof");
        examine(h, p, cow);
        examine(h, p, vial("minecraft:sheep"));
        h.assertTrue(state.sourceCount() == 3 && ClinicalBloodKnowledge.teach(p, alchemist, CABINET), "Third source did not unlock Cabinet lesson");
        h.assertTrue(ClinicalBloodKnowledge.recipeVisible(p, Hemomancy.rloc("phlebotomists_cabinet")), "Known Cabinet recipe hidden");
        h.assertTrue(!ClinicalBloodKnowledge.recipeVisible(p, Hemomancy.rloc("phlebotomists_field_case")), "Untaught Field Case recipe exposed");
        h.succeed();
    }

    @GameTest(template = "empty") public static void cabinetProofComesFromSuccessfulPlayerTransfers(GameTestHelper h) {
        var p = player(h, 2);
        var state = HemoCapabilityAccess.clinicalBlood(p);
        state.learn(CABINET);
        h.setBlock(new BlockPos(0, 2, 0), BlockInit.phlebotomists_cabinet.get());
        var be = (PhlebotomistsCabinetBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(new BlockPos(0, 2, 0)));
        var menu = new PhlebotomistsCabinetMenu(1, p.getInventory(), be); p.containerMenu = menu;
        menu.clicked(0, 0, ClickType.PICKUP, p);
        h.assertTrue(!state.cabinetWithdrawn, "Empty withdrawal counted");
        menu.setCarried(new ItemStack(Items.STONE)); menu.clicked(0, 0, ClickType.PICKUP, p);
        h.assertTrue(!state.cabinetInserted, "Rejected item counted as specimen storage");
        menu.setCarried(vial("minecraft:pig")); menu.clicked(0, 0, ClickType.PICKUP, p);
        h.assertTrue(state.cabinetInserted && !state.cabinetWithdrawn, "Successful insertion was not recorded");
        menu.clicked(0, 0, ClickType.PICKUP, p);
        h.assertTrue(state.cabinetWithdrawn, "Successful withdrawal was not recorded");
        h.assertTrue(!state.canLearn(FIELD_REFERRAL, 2), "Missing craft and iron proof bypassed referral");
        ClinicalBloodKnowledge.crafted(new PlayerEvent.ItemCraftedEvent(p, new ItemStack(BlockInit.phlebotomists_cabinet.get()), new net.minecraft.world.SimpleContainer(9)));
        state.hematicIronObtained = true;
        ClinicalBloodKnowledge.met(p, "artificer");
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel()); alchemist.setPos(p.position());
        var artificer = EntityInit.harbinger_artificer.get().create(h.getLevel()); artificer.setPos(p.position());
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, artificer, FIELD_CASE), "Field Case bypassed Alchemist handoff");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, alchemist, FIELD_REFERRAL), "Qualified referral denied");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, artificer, FIELD_CASE), "Artificer did not teach referred field conversion");
        menu.removed(p);
        h.succeed();
    }

    @GameTest(template = "empty") public static void mnemonicInquiryHintsUntilReferredFormalStudy(GameTestHelper h) {
        var p = player(h, 2);
        var state = HemoCapabilityAccess.clinicalBlood(p);
        var machine = new ItemStack(BlockInit.clairaudiograph.get());
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel()); alchemist.setPos(p.position());
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(h.getLevel()); mnemonist.setPos(p.position());
        ClinicalBloodKnowledge.met(p, "mnemonist");
        h.assertTrue(ClinicalBloodDialogue.inquiry(p, "mnemonist", machine).orElseThrow()
                .contains("hemomancy.clinical.inquiry.echo_hint"), "Untaught mnemonic inquiry leaked full workflow");
        for (String source : new String[]{"minecraft:pig", "minecraft:cow", "minecraft:sheep"}) state.recordExamination(source);
        h.assertTrue(ClinicalBloodKnowledge.teach(p, alchemist, ECHO_REFERRAL), "D2 echo referral denied");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, mnemonist, CLAIRAUDIOGRAPH), "Formal study unlocked before D3");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(3);
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, alchemist, CLAIRAUDIOGRAPH), "Alchemist usurped mnemonic teaching");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, mnemonist, CLAIRAUDIOGRAPH), "Qualified mnemonic lesson denied");
        h.assertTrue(p.getRecipeBook().contains(Hemomancy.rloc("wax_cylinder")), "Ordinary cylinder recipe missing");
        h.assertTrue(ClinicalBloodDialogue.inquiry(p, "mnemonist", machine).orElseThrow()
                .contains("hemomancy.clinical.clairaudiograph.lesson"), "Taught inquiry did not show workflow");
        h.succeed();
    }

    @GameTest(template = "empty") public static void injectionRequiresKnowledgeAndInterruptedStudyGrantsNothing(GameTestHelper h) {
        var p = player(h, 1);
        var sample = vial("minecraft:pig");
        p.setItemInHand(InteractionHand.MAIN_HAND, sample);
        sample.use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        h.assertTrue(!p.isUsingItem() && BloodSampleData.isFilled(sample), "Untaught injection consumed or started");
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
        p.setItemInHand(InteractionHand.OFF_HAND, sample);
        p.getMainHandItem().use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        for (int i = 0; i < 20; i++) p.doTick();
        p.releaseUsingItem();
        for (int i = 0; i < 30; i++) p.doTick();
        h.assertTrue(HemoCapabilityAccess.clinicalBlood(p).sourceCount() == 0, "Interrupted examination granted proof");
        h.assertTrue(!BloodSampleData.identified(sample), "Interrupted examination identified blood");
        h.succeed();
    }
}
