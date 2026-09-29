package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("blood_injection_validation")
@PrefixGameTestTemplate(false)
public final class BloodInjectionGameTests {
    @GameTest(template = "empty")
    public static void founderInitiationChecksRankOwnershipAndInterruption(GameTestHelper h) {
        var recruit = animationPlayer(h).player();
        var founder = animationPlayer(h).player();
        founder.setPos(recruit.position().add(1, 0, 0));
        HemoCapabilityAccess.requireInitiatoryDegree(recruit).setDegreeNumber(0);
        HemoCapabilityAccess.getEquipment(recruit).orElseThrow().setStackInSlot(5,
                new ItemStack(ItemInit.charm_of_vascularium.get()));
        EarlyInitiation.activate(recruit);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(founder);
        degree.setDegreeNumber(4);
        degree.setHasFoundedBloodline(true);
        var data = BloodlineSavedData.get(h.getLevel().getServer().overworld());
        var line = new Bloodline("Interrupted initiation", founder.getUUID(), java.util.UUID.randomUUID(), new java.util.ArrayList<>());
        data.registerBloodline(line);
        var volume = HemoCapabilityAccess.getBloodVolume(founder).orElseThrow();
        volume.setBloodLine(line); volume.setActive(true);
        h.assertTrue(!EarlyInitiation.begin(recruit, founder), "Degree 4 initiated");
        degree.setDegreeNumber(5); degree.setHasFoundedBloodline(false);
        h.assertTrue(!EarlyInitiation.begin(recruit, founder), "Non-founder initiated");
        degree.setHasFoundedBloodline(true);
        data.disbandBloodline(line.getBloodlineUUID());
        h.assertTrue(!EarlyInitiation.begin(recruit, founder), "Stale bloodline initiated");
        data.registerBloodline(line);
        var foreign = new Bloodline("Other line", java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), new java.util.ArrayList<>());
        foreign.addMember(founder.getUUID()); data.registerBloodline(foreign); volume.setBloodLine(foreign);
        h.assertTrue(!EarlyInitiation.begin(recruit, founder), "Ordinary member initiated");
        volume.setBloodLine(line);
        h.assertTrue(EarlyInitiation.begin(recruit, founder), "Valid founder rejected");
        EarlyInitiation.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(founder));
        h.assertTrue(EarlyInitiation.begin(recruit, founder), "Founder logout did not cancel");
        degree.setDegreeNumber(4);
        EarlyInitiation.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(recruit));
        degree.setDegreeNumber(5);
        h.assertTrue(EarlyInitiation.begin(recruit, founder), "Lost eligibility did not cancel");
        data.disbandBloodline(line.getBloodlineUUID());
        EarlyInitiation.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(recruit));
        data.registerBloodline(line);
        h.assertTrue(EarlyInitiation.begin(recruit, founder), "Disbanding during ceremony did not cancel");
        EarlyInitiation.cancel(recruit);
        foreign.addMember(recruit.getUUID());
        h.assertTrue(!EarlyInitiation.begin(recruit, founder), "Existing different membership overwritten");
        foreign.removeMember(recruit.getUUID());
        h.assertTrue(EarlyInitiation.begin(recruit, founder), "Unbound recruit rejected");
        EarlyInitiation.cancel(recruit);
        h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(recruit) == 0 && !line.hasMember(recruit.getUUID()),
                "Interrupted ceremony granted degree or membership");
        h.assertTrue(recruit.getInventory().countItem(ItemInit.sanguine_conduit.get()) == 0, "Interrupted ceremony gave conduit");
        data.disbandBloodline(line.getBloodlineUUID()); data.disbandBloodline(foreign.getBloodlineUUID());
        recruit.discard(); founder.discard(); h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 240)
    public static void founderInitiationBindsOnlyAfterTenSeconds(GameTestHelper h) {
        var recruit = animationPlayer(h).player();
        var founder = animationPlayer(h).player();
        founder.setPos(recruit.position().add(3, 0, 0));
        HemoCapabilityAccess.requireInitiatoryDegree(recruit).setDegreeNumber(0);
        HemoCapabilityAccess.getEquipment(recruit).orElseThrow().setStackInSlot(5,
                new ItemStack(ItemInit.charm_of_vascularium.get()));
        EarlyInitiation.activate(recruit);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(founder);
        degree.setDegreeNumber(5);
        degree.setHasFoundedBloodline(true);
        var data = BloodlineSavedData.get(h.getLevel().getServer().overworld());
        var line = new Bloodline("Initiation test", founder.getUUID(), java.util.UUID.randomUUID(), new java.util.ArrayList<>());
        data.registerBloodline(line);
        HemoCapabilityAccess.getBloodVolume(founder).orElseThrow().setBloodLine(line);
        HemoCapabilityAccess.getBloodVolume(founder).orElseThrow().setActive(true);
        // Clear the fixture along the eye-height ray before testing the explicit obstruction.
        for (int x = 0; x <= 3; x++)
            h.getLevel().setBlockAndUpdate(net.minecraft.core.BlockPos.containing(recruit.getEyePosition()).offset(x, 0, 0),
                    net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        founder.setYRot(90); founder.setXRot(0);
        var wall = net.minecraft.core.BlockPos.containing(recruit.getEyePosition()).offset(1, 0, 0);
        var previous = h.getLevel().getBlockState(wall);
        h.getLevel().setBlockAndUpdate(wall, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem.projectFromEntity(
                h.getLevel(), founder, 1, 1);
        h.assertTrue(EarlyInitiation.begin(recruit, founder), "Projection initiated through a wall");
        EarlyInitiation.cancel(recruit);
        h.getLevel().setBlockAndUpdate(wall, previous);
        com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem.projectFromEntity(
                h.getLevel(), founder, 1, 1);
        h.assertTrue(!EarlyInitiation.begin(recruit, founder), "Projection did not begin ceremony");
        h.assertTrue(!EarlyInitiation.begin(recruit, founder), "Duplicate ceremony accepted");
        h.runAfterDelay(199, () -> {
            h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(recruit) == 0, "Degree granted early");
            h.assertTrue(!line.hasMember(recruit.getUUID()), "Membership granted early");
        });
        h.runAfterDelay(202, () -> {
            EarlyInitiation.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(recruit));
            h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(recruit) == 1, "Degree missing");
            h.assertTrue(line.hasMember(recruit.getUUID()), "Canonical membership missing");
            h.assertTrue(HemoCapabilityAccess.getBloodVolume(recruit).orElseThrow().getBloodLine().getBloodlineUUID()
                    .equals(line.getBloodlineUUID()), "Player bloodline missing");
            h.assertTrue(recruit.getInventory().countItem(ItemInit.sanguine_conduit.get()) == 1, "Conduit missing");
            h.assertTrue(!EarlyInitiation.begin(recruit, founder), "Completed initiation repeated");
            data.disbandBloodline(line.getBloodlineUUID());
            recruit.discard(); founder.discard(); h.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void concentratedBloodRequiresOwnRewardAndCompletedSleep(GameTestHelper h) {
        var capture = animationPlayer(h);
        var player = capture.player();
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getEquipment(player).orElseThrow()
                .setStackInSlot(5, new ItemStack(ItemInit.charm_of_vascularium.get()));
        com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.activate(player);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(2);
        var sample = com.vincenthuto.hemomancy.common.mission.alchemist.ConcentratedBlood.create();
        player.setItemInHand(InteractionHand.MAIN_HAND, sample);
        h.assertTrue(!sample.use(h.getLevel(), player, InteractionHand.MAIN_HAND).getResult().consumesAction(), "Unclaimed mission permitted injection");
        com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment.markClaimed(player);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.advancedBrewing(player).record("distill");
        sample.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        for (int i = 0; i < 7; i++) player.doTick();
        player.releaseUsingItem();
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.ConcentratedBlood.pending(player), "Interrupted injection advanced state");
        sample.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        for (int i = 0; i < 16; i++) player.doTick();
        h.assertTrue(com.vincenthuto.hemomancy.common.mission.alchemist.ConcentratedBlood.pending(player), "Injection did not persist pending rest");
        h.assertTrue(!BloodSampleData.isFilled(player.getMainHandItem()), "Special injection did not return empty vial");
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.ConcentratedBlood.completeSleep(player, false), "Interrupted sleep advanced degree");
        h.assertTrue(com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getPlayerDegreeNumber(player) == 2, "Injection granted degree early");
        h.assertTrue(com.vincenthuto.hemomancy.common.mission.alchemist.ConcentratedBlood.completeSleep(player, true), "Completed sleep did not advance");
        h.assertTrue(com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getPlayerDegreeNumber(player) == 3, "Wrong wake degree");
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.alchemist.ConcentratedBlood.completeSleep(player, true), "Sleep repeated reward");
        player.discard(); h.succeed();
    }

    @GameTest(template = "empty")
    public static void vicarRemovalClearsOathAndCeremonyCancellationRestoresNpc(GameTestHelper h) {
        var player = animationPlayer(h).player();
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(0);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getEquipment(player).orElseThrow()
                .setStackInSlot(5, new ItemStack(ItemInit.charm_of_vascularium.get()));
        com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.activate(player);
        var hermit = java.util.UUID.randomUUID();
        com.vincenthuto.hemomancy.common.rite.TempleOathRules.bless(player, hermit);
        com.vincenthuto.hemomancy.common.rite.TempleOathRules.recordHeartClaim(player, hermit);
        var vicar = new com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity(EntityInit.harbinger_vicar.get(), h.getLevel());
        vicar.setPos(player.position().add(1, 0, 0)); h.getLevel().addFreshEntity(vicar);
        h.assertTrue(com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.begin(player, vicar), "Eligible ceremony rejected");
        h.assertTrue(vicar.isNoAi() && vicar.isInitiating(), "Vicar not held in performance");
        var saved = new CompoundTag(); vicar.addAdditionalSaveData(saved);
        h.assertTrue(!saved.getBoolean("NoAI"), "Saving ceremony permanently disables Vicar AI");
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.begin(player, vicar), "Duplicate ceremony accepted");
        com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.cancel(player);
        h.assertTrue(!vicar.isNoAi() && !vicar.isInitiating(), "Cancellation left Vicar frozen");
        h.assertTrue(player.getInventory().countItem(ItemInit.sanguine_conduit.get()) == 0, "Interrupted ceremony gave conduit");
        h.assertTrue(com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.release(player, vicar), "Removal rejected");
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.attached(player), "Removal kept charm");
        h.assertTrue(com.vincenthuto.hemomancy.common.rite.TempleOathRules.claimedHeartHermit(player) == null, "Removal kept temple claim");
        h.assertTrue(!com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getBloodVolume(player).orElseThrow().isActive(), "Removal kept active blood");
        vicar.discard(); player.discard(); h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 240)
    public static void vicarCompletesAfterTenSecondsAndGrantsConduitOnce(GameTestHelper h) {
        var player = animationPlayer(h).player();
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(0);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getEquipment(player).orElseThrow()
                .setStackInSlot(5, new ItemStack(ItemInit.charm_of_vascularium.get()));
        com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.activate(player);
        var vicar = new com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity(EntityInit.harbinger_vicar.get(), h.getLevel());
        vicar.setPos(player.position().add(1, 0, 0)); h.getLevel().addFreshEntity(vicar);
        h.assertTrue(com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.begin(player, vicar), "Ceremony did not begin");
        h.runAfterDelay(199, () -> {
            h.assertTrue(com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getPlayerDegreeNumber(player) == 0, "Early degree grant");
            h.assertTrue(player.getInventory().countItem(ItemInit.sanguine_conduit.get()) == 0, "Early conduit grant");
        });
        h.runAfterDelay(202, () -> {
            com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
            h.assertTrue(com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getPlayerDegreeNumber(player) == 1, "Ceremony did not grant Degree 1");
            h.assertTrue(player.getInventory().countItem(ItemInit.sanguine_conduit.get()) == 1, "Conduit missing or duplicated");
            h.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.begin(player, vicar), "Initiated player can repeat ceremony");
            h.assertTrue(!com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.release(player, vicar), "Settled charm can be removed");
            vicar.discard(); player.discard(); h.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void templePermissionAttachmentAndRewardsAreAtomic(GameTestHelper h) {
        var player = animationPlayer(h).player();
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(0);
        var pos = h.absolutePos(new net.minecraft.core.BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.mortal_display.get().defaultBlockState());
        var display = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.MortalDisplayBlockEntity) h.getLevel().getBlockEntity(pos);
        var hermit = java.util.UUID.randomUUID(); display.linkHermit(hermit);
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
        var state = h.getLevel().getBlockState(pos);
        state.useWithoutItem(h.getLevel(), player, hit);
        h.assertTrue(!display.isClaimed(), "Unblessed player claimed heart");
        com.vincenthuto.hemomancy.common.rite.TempleOathRules.bless(player, java.util.UUID.randomUUID());
        state.useWithoutItem(h.getLevel(), player, hit);
        h.assertTrue(!display.isClaimed(), "Wrong temple blessing claimed heart");
        com.vincenthuto.hemomancy.common.rite.TempleOathRules.bless(player, hermit);
        var equipment = com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getEquipment(player).orElseThrow();
        equipment.setStackInSlot(5, new ItemStack(ItemInit.curved_horn.get()));
        state.useWithoutItem(h.getLevel(), player, hit);
        h.assertTrue(!display.isClaimed(), "Occupied scar consumed heart");
        equipment.setStackInSlot(5, ItemStack.EMPTY);
        state.useWithoutItem(h.getLevel(), player, hit);
        h.assertTrue(display.isClaimedBy(player.getUUID()), "Valid attachment failed");
        h.assertTrue(com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.attached(player), "Charm not attached");
        h.assertTrue(com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getPlayerDegreeNumber(player) == 0, "Display granted Degree 1");
        h.assertTrue(player.getInventory().countItem(ItemInit.covenant_waybill.get()) == 1, "Waybill missing");
        h.assertTrue(player.getInventory().countItem(ItemInit.sanguine_conduit.get()) == 0, "Display granted conduit");
        state.useWithoutItem(h.getLevel(), player, hit);
        h.assertTrue(player.getInventory().countItem(ItemInit.covenant_waybill.get()) == 1, "Repeated click duplicated waybill");
        player.discard(); h.succeed();
    }

    private static net.minecraft.world.entity.player.Player educatedPlayer(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(2);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.clinicalBlood(player)
                .learn(com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress.Lesson.INJECTION);
        return player;
    }
    @GameTest(template = "empty")
    public static void injectionAnimationPacketsTrackOutcomeAndPhysicalHand(GameTestHelper h) {
        for (var dominant : net.minecraft.world.entity.HumanoidArm.values()) {
            for (var hand : InteractionHand.values()) {
                var capture = animationPlayer(h);
                var player = capture.player();
                player.setMainArm(dominant);
                var sample = vial("minecraft:pig");
                player.setItemInHand(hand, sample);
                sample.use(h.getLevel(), player, hand);
                for (int i = 0; i < 16; i++) player.doTick();
                h.assertTrue(capture.packets().size() == 2, "Expected start and exactly one impact: " + capture.packets());
                var start = capture.packets().getFirst();
                var impact = capture.packets().getLast();
                h.assertTrue(start.phase() == com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection.Phase.START
                        && impact.phase() == com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection.Phase.IMPACT,
                        "Successful injection cancelled or never impacted");
                h.assertTrue(start.hand() == hand && start.right() == ((hand == InteractionHand.MAIN_HAND)
                        == (dominant == net.minecraft.world.entity.HumanoidArm.RIGHT)), "Wrong physical injection arm");
                h.assertTrue(BloodSampleData.isFilled(start.vial()) && !BloodSampleData.isFilled(impact.vial()),
                        "Animated vial did not change from full to empty");
                for (var packet : capture.packets()) {
                    var buf = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), h.getLevel().registryAccess());
                    try {
                        var codec = com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection.STREAM_CODEC;
                        codec.encode(buf, packet);
                        var decoded = codec.decode(buf);
                        h.assertTrue(decoded.entityId() == player.getId() && decoded.startedAt() == start.startedAt()
                                && decoded.phase() == packet.phase() && decoded.hand() == hand
                                && decoded.right() == start.right() && decoded.tendency() == packet.tendency()
                                && ItemStack.matches(decoded.vial(), packet.vial()), "Animation packet lost presentation data");
                    } finally { buf.release(); }
                }
                player.discard();
            }
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void interruptedAndRejectedInjectionsSendCancelWithoutImpact(GameTestHelper h) {
        for (boolean release : new boolean[]{true, false}) {
            var capture = animationPlayer(h);
            var player = capture.player();
            var sample = vial("minecraft:pig");
            player.setItemInHand(InteractionHand.OFF_HAND, sample);
            sample.use(h.getLevel(), player, InteractionHand.OFF_HAND);
            for (int i = 0; i < 7; i++) player.doTick();
            if (release) player.releaseUsingItem();
            else {
                player.addEffect(new MobEffectInstance(EffectInit.transfusion_saturation, 400));
                for (int i = 0; i < 9; i++) player.doTick();
            }
            h.assertTrue(capture.packets().size() == 2 && capture.packets().getLast().phase()
                    == com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection.Phase.CANCEL,
                    "Interrupted/rejected use produced an impact or left animation active");
            h.assertTrue(BloodSampleData.isFilled(player.getOffhandItem()), "Cancellation consumed sample");
            player.discard();
        }
        h.succeed();
    }

    private static AnimationCapture animationPlayer(GameTestHelper h) {
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "vial-animation"), false);
        var player = new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(), h.getLevel(),
                cookie.gameProfile(), cookie.clientInformation());
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(2);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.clinicalBlood(player)
                .learn(com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress.Lesson.INJECTION);
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        var packets = new java.util.ArrayList<com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection>();
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {
                if (packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket payload
                        && payload.payload() instanceof com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection animation)
                    packets.add(animation);
            }
        };
        player.setPos(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1))));
        h.getLevel().addNewPlayer(player);
        return new AnimationCapture(player, packets);
    }

    @GameTest(template = "empty")
    public static void switchingToAnIdenticalVialCancelsCosmeticRecovery(GameTestHelper h) {
        for (var hand : InteractionHand.values()) {
            var capture = animationPlayer(h);
            var player = capture.player();
            var sample = vial("minecraft:pig");
            player.setItemInHand(hand, sample);
            sample.use(h.getLevel(), player, hand);
            for (int i = 0; i < 16; i++) player.doTick();
            var identicalEmpty = player.getItemInHand(hand).copy();
            if (hand == InteractionHand.MAIN_HAND) {
                player.getInventory().items.set(1, identicalEmpty);
                player.getInventory().selected = 1;
            } else player.setItemInHand(hand, identicalEmpty);
            player.doTick();
            h.assertTrue(capture.packets().size() == 3 && capture.packets().getLast().phase()
                    == com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection.Phase.CANCEL,
                    "Switching to another identical vial did not cancel recovery");
            h.assertTrue(player.getItemInHand(hand) == identicalEmpty, "Cosmetic cancellation changed inventory");
            player.discard();
        }
        h.succeed();
    }

    private record AnimationCapture(net.minecraft.server.level.ServerPlayer player,
            java.util.List<com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection> packets) { }

    private static ItemStack vial(String source) {
        var stack = new ItemStack(ItemInit.bloody_vial.get());
        var tag = new CompoundTag();
        tag.putString("entity_type", source);
        tag.putString("addon_marker", "preserve");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
    @GameTest(template = "empty")
    public static void unreadableSamplesRemainFilled(GameTestHelper h) {
        for (var source : new String[]{"absent:creature", "Malformed source", ""}) {
            var stack = vial(source);
            h.assertTrue(BloodVialItem.getEntityType(stack) == null, "Unresolved source resolved");
            h.assertTrue(!VialRackItem.isEmptyVial(stack), "Missing source became empty glass");
            var before = stack.copy();
            var player = educatedPlayer(h);
            ItemInit.bloody_vial.get().onLeftClickEntity(stack, player, h.spawn(net.minecraft.world.entity.EntityType.PIG, 0, 2, 0));
            h.assertTrue(ItemStack.isSameItemSameComponents(before, stack), "Sampling overwrote unreadable blood");
            h.assertTrue(!BloodSampleData.identify(stack), "Unreadable sample was identified");
        }
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void identificationAndLegacyTags(GameTestHelper h) {
        var a = vial("minecraft:pig"); var b = a.copy();
        h.assertTrue(BloodSampleData.identify(a), "Registered source could not be examined");
        h.assertTrue(!ItemStack.isSameItemSameComponents(a,b), "Knowledge not part of identity");
        BloodSampleData.identify(b);
        h.assertTrue(ItemStack.isSameItemSameComponents(a,b), "Identical examinations differ");
        var snapshot = BloodInjectionData.snapshot(false);
        var pig = BloodSampleData.profile(a,false);
        var bear = BloodSampleData.profile(vial("minecraft:polar_bear"),false);
        h.assertTrue(pig.tendencies().contains(com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.ANIMUS), "Pig lost Animus");
        h.assertTrue(!bear.tendencies().contains(com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.ANIMUS), "Polar bear gained Animus");
        h.assertTrue(bear.tendencies().contains(com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.CONGEATIO), "Bear lost Congeatio");
        h.assertTrue(snapshot.resolve(bear).drawbacks().size() == 1, "Cold property lost Congeatio drawback");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void bothHandsReturnExactlyOneEmptyVessel(GameTestHelper h) {
        for (var hand : InteractionHand.values()) {
            var player = educatedPlayer(h);
            for(int slot=0;slot<player.getInventory().items.size();slot++) player.getInventory().items.set(slot,new ItemStack(Items.STONE,64));
            var stack = vial("minecraft:pig"); player.setItemInHand(hand,stack);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,600,2));
            stack.use(h.getLevel(),player,hand);
            h.assertTrue(player.isUsingItem(), "Filled vial did not start use");
            for (int i=0;i<16;i++) player.tick();
            var result = player.getItemInHand(hand);
            h.assertTrue(result.is(ItemInit.bloody_vial.get()) && result.getCount()==1 && !BloodSampleData.isFilled(result), "Vessel not returned once");
            h.assertTrue(player.hasEffect(EffectInit.transfusion_saturation), "No lockout");
            h.assertTrue(player.getEffect(MobEffects.REGENERATION).getAmplifier()==2, "Stronger buff replaced");
            h.assertTrue(result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("addon_marker").equals("preserve"), "Opaque data discarded");
        }
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void interruptedAndSaturatedUsesPreserveSample(GameTestHelper h) {
        var player = educatedPlayer(h);
        var stack=vial("minecraft:pig"); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        stack.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        for(int i=0;i<7;i++) player.tick();
        player.releaseUsingItem();
        h.assertTrue(BloodSampleData.isFilled(stack) && !player.hasEffect(EffectInit.transfusion_saturation), "Interrupted injection applied");
        stack.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        player.addEffect(new MobEffectInstance(EffectInit.transfusion_saturation,400));
        for(int i=0;i<16;i++) player.tick();
        h.assertTrue(BloodSampleData.isFilled(player.getMainHandItem()) && !player.hasEffect(MobEffects.REGENERATION), "Completion bypassed saturation");
        stack.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(!player.isUsingItem(), "Saturated start allowed");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void creativeRetainsSampleAndRackNeverInjects(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.CREATIVE); player.getAbilities().instabuild=true;
        var stack=vial("minecraft:pig"); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        stack.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        for(int i=0;i<16;i++) player.tick();
        h.assertTrue(player.getMainHandItem()==stack && BloodSampleData.isFilled(stack), "Creative sample consumed");
        h.assertTrue(player.hasEffect(EffectInit.transfusion_saturation), "Creative validation skipped");
        player.removeAllEffects();
        var rack=new ItemStack(ItemInit.vial_rack.get()); var vials=VialRackItem.getVials(rack);
        vials.set(0,stack.copy()); VialRackItem.setVials(rack,vials);
        player.setItemInHand(InteractionHand.MAIN_HAND,rack);
        rack.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(!player.isUsingItem() && !player.hasEffect(EffectInit.transfusion_saturation), "Rack injected");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void switchingHandsDeathAndSpectatorPreventCompletion(GameTestHelper h) {
        for (int scenario=0; scenario<3; scenario++) {
            var player=educatedPlayer(h);
            var stack=vial("minecraft:pig");player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            stack.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
            for(int i=0;i<8;i++) player.tick();
            if(scenario==0) {
                player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                player.setItemInHand(InteractionHand.OFF_HAND,stack);
            } else if(scenario==1) player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));
            else player.setHealth(0);
            for(int i=0;i<16;i++) player.tick();
            h.assertTrue(BloodSampleData.isFilled(stack) && !player.hasEffect(EffectInit.transfusion_saturation), "Interrupted actor injected");
        }
        var spectator=h.makeMockPlayer(GameType.SPECTATOR);
        var stack=vial("minecraft:pig");spectator.setItemInHand(InteractionHand.MAIN_HAND,stack);
        stack.use(h.getLevel(),spectator,InteractionHand.MAIN_HAND);
        h.assertTrue(!spectator.isUsingItem(), "Spectator injected");h.succeed();
    }
    @GameTest(template = "empty")
    public static void identificationPersistsAndSnapshotMatchesTooltipSelection(GameTestHelper h) {
        var sample=vial("minecraft:polar_bear");BloodSampleData.identify(sample);
        var loaded=ItemStack.parseOptional(h.getLevel().registryAccess(),(CompoundTag)sample.save(h.getLevel().registryAccess()));
        h.assertTrue(ItemStack.isSameItemSameComponents(sample,loaded), "Specimen data did not persist");
        var snapshot=BloodInjectionData.snapshot(false);
        var buf=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
        var codec=com.vincenthuto.hemomancy.common.network.BloodInjectionSyncPacket.STREAM_CODEC;
        codec.encode(buf,new com.vincenthuto.hemomancy.common.network.BloodInjectionSyncPacket(snapshot.json()));
        var packet=codec.decode(buf);buf.release();
        BloodInjectionData.receive(packet.definitions());
        var client=BloodInjectionData.snapshot(true);
        var profile=BloodSampleData.profile(sample,false);
        h.assertTrue(snapshot.resolve(profile).equals(client.resolve(profile)), "Client response disagreed with server");
        BloodInjectionData.clearClient();
        h.assertTrue(BloodInjectionData.snapshot(true).responses().isEmpty() && !BloodInjectionData.snapshot(false).responses().isEmpty(), "Disconnect crossed snapshot sides");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void resistancesMobilityAndHealingHaveOnlyTheirNamedBenefits(GameTestHelper h) {
        var player=educatedPlayer(h);
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2,6,2)));
        player.addEffect(new MobEffectInstance(EffectInit.cryoprotection,200));player.setTicksFrozen(200);player.tick();
        h.assertTrue(!player.canFreeze() && player.getTicksFrozen()==0, "Cryoprotection did not stop freezing");
        h.assertTrue(net.minecraft.world.level.block.PowderSnowBlock.canEntityWalkOnPowderSnow(player), "Powder snow traversal missing");
        player.removeAllEffects();player.addEffect(new MobEffectInstance(EffectInit.web_mobility,200));
        double x=player.getX();player.makeStuckInBlock(net.minecraft.world.level.block.Blocks.COBWEB.defaultBlockState(),new net.minecraft.world.phys.Vec3(.25,.05,.25));
        player.move(net.minecraft.world.entity.MoverType.SELF,new net.minecraft.world.phys.Vec3(1,0,0));
        h.assertTrue(player.getX()-x>.7 && player.getX()-x<.9, "Cobweb movement did not use reduced slowdown");
        player.removeAllEffects();double speed=player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        player.addEffect(new MobEffectInstance(EffectInit.poison_resistance,200));
        player.addEffect(new MobEffectInstance(MobEffects.POISON,100));
        h.assertTrue(!player.hasEffect(MobEffects.POISON) && player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)==speed, "Poison resistance added speed or admitted poison");
        player.addEffect(new MobEffectInstance(EffectInit.wither_resistance,200));player.addEffect(new MobEffectInstance(MobEffects.WITHER,100));
        h.assertTrue(!player.hasEffect(MobEffects.WITHER), "Wither resistance admitted wither");
        player.addEffect(new MobEffectInstance(EffectInit.impaired_recovery,200));player.setHealth(10);player.heal(4);
        h.assertTrue(Math.abs(player.getHealth()-12)<.01, "Incoming healing not halved");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void centrifugeStillAcceptsBalancedSamples(GameTestHelper h) {
        var pos=h.absolutePos(new net.minecraft.core.BlockPos(0,2,0));
        h.getLevel().setBlockAndUpdate(pos,BlockInit.vial_centrifuge.get().defaultBlockState());
        var machine=(com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity)h.getLevel().getBlockEntity(pos);
        machine.setItem(2,vial("minecraft:pig"));machine.setItem(6,vial("minecraft:pig"));
        h.assertTrue(machine.attemptStartup(null)==com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeStartupResult.SUCCESS,"Balanced samples failed startup");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void miningTargetsEarthAndStoneInsteadOfOreOrWood(GameTestHelper h) {
        var player=educatedPlayer(h);player.addEffect(new MobEffectInstance(EffectInit.earthen_mining,200));
        var blocks=new net.minecraft.world.level.block.Block[]{net.minecraft.world.level.block.Blocks.STONE,net.minecraft.world.level.block.Blocks.DIRT,
                net.minecraft.world.level.block.Blocks.IRON_ORE,net.minecraft.world.level.block.Blocks.OAK_LOG};
        for(int i=0;i<blocks.length;i++) {
            var event=new net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed(player,blocks[i].defaultBlockState(),4,net.minecraft.core.BlockPos.ZERO);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
            h.assertTrue(Math.abs(event.getNewSpeed()-(i<2?5:4))<.01,"Mining response affected wrong material: "+blocks[i]);
        }
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void projectileAndExplosionResistanceDoNotReduceOrdinaryDamage(GameTestHelper h) {
        var player=educatedPlayer(h);player.addEffect(new MobEffectInstance(EffectInit.ender_resistance,200));
        player.addEffect(new MobEffectInstance(EffectInit.explosion_resistance,200));
        var arrow=net.minecraft.world.entity.EntityType.ARROW.create(h.getLevel());
        var sources=new net.minecraft.world.damagesource.DamageSource[]{player.damageSources().arrow(arrow,null),player.damageSources().explosion(null,null),player.damageSources().generic()};
        for(int i=0;i<sources.length;i++) {
            player.setHealth(20);player.invulnerableTime=0;player.hurt(sources[i],8);
            h.assertTrue(Math.abs(player.getHealth()-(i<2?14:12))<.01,"Resistance changed wrong damage category: "+sources[i]);
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void twoTendencyFixtureKeepsBothResponsesAndDrawback(GameTestHelper h) {
        var profile=new BloodSampleData.Profile(net.minecraft.resources.ResourceLocation.parse("minecraft:pig"),java.util.List.of(
            com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.CONGEATIO,
            com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.ANIMUS),java.util.List.of(),true);
        var result=BloodInjectionData.snapshot(false).resolve(profile);
        h.assertTrue(result.benefits().size()==2 && result.benefits().getFirst().effect().toString().equals("minecraft:regeneration"),"Two-tendency order or response lost");
        h.assertTrue(result.drawbacks().size()==1 && result.drawbacks().getFirst().effect().toString().equals("minecraft:slowness"),"Congeatio drawback lost");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void packPriorityAndReloadRefreshTheResolvedAnswer(GameTestHelper h) throws Exception {
        var root=java.nio.file.Files.createTempDirectory(java.nio.file.Path.of("."),"blood-response-packs-");
        var packs=new java.util.ArrayList<net.minecraft.server.packs.PackResources>();
        for(int i=0;i<2;i++) {
            var path=root.resolve("pack"+i);var file=path.resolve("data/example/blood_injection/animus.json");
            java.nio.file.Files.createDirectories(file.getParent());
            java.nio.file.Files.writeString(file,"{\"tendency\":\"ANIMUS\",\"benefits\":[{\"effect\":\"minecraft:regeneration\",\"duration\":"+(i==0?200:400)+"}]}");
            packs.add(new net.minecraft.server.packs.PathPackResources(new net.minecraft.server.packs.PackLocationInfo("fixture"+i,
                net.minecraft.network.chat.Component.literal("fixture"),net.minecraft.server.packs.repository.PackSource.DEFAULT,java.util.Optional.empty()),path));
        }
        try(var manager=new net.minecraft.server.packs.resources.MultiPackResourceManager(net.minecraft.server.packs.PackType.SERVER_DATA,packs)) {
            var method=BloodInjectionData.class.getDeclaredMethod("prepare",net.minecraft.server.packs.resources.ResourceManager.class,net.minecraft.util.profiling.ProfilerFiller.class);
            method.setAccessible(true);
            var replacement=(BloodInjectionData.Snapshot)method.invoke(new BloodInjectionData(),manager,net.minecraft.util.profiling.InactiveProfiler.INSTANCE);
            var sample=vial("minecraft:pig");BloodSampleData.identify(sample);
            var profile=BloodSampleData.profile(sample,false);
            h.assertTrue(replacement.responses().size()==1 && replacement.resolve(profile).benefits().getFirst().duration()==400,"Pack priority failed");
            BloodInjectionData.receive(replacement.json());
            h.assertTrue(BloodSampleData.identified(sample) && BloodInjectionData.snapshot(true).resolve(profile).benefits().getFirst().duration()==400,"Reload failed to refresh identified answer");
            BloodInjectionData.clearClient();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void syringePreservesEmptyVialDataAndSkipsUnreadableSamples(GameTestHelper h) {
        var player=educatedPlayer(h);
        var rack=new ItemStack(ItemInit.vial_rack.get());var vials=VialRackItem.getVials(rack);
        vials.set(0,vial("missing:creature"));
        var empty=new ItemStack(ItemInit.bloody_vial.get());
        empty.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Field vessel"));
        var extra=new CompoundTag();extra.putString("addon_marker","preserve");empty.set(DataComponents.CUSTOM_DATA,CustomData.of(extra));
        vials.set(1,empty);VialRackItem.setVials(rack,vials);player.getInventory().setItem(1,rack);
        var syringe=new ItemStack(ItemInit.living_syringe.get());player.setItemInHand(InteractionHand.MAIN_HAND,syringe);
        syringe.getItem().interactLivingEntity(syringe,player,h.spawn(net.minecraft.world.entity.EntityType.PIG,0,3,0),InteractionHand.MAIN_HAND);
        var stored=ItemStack.parseOptional(h.getLevel().registryAccess(),syringe.get(DataComponents.CUSTOM_DATA).copyTag().getCompound("loaded_rack"));
        var loaded=VialRackItem.getVials(stored);
        h.assertTrue(BloodSampleData.rawSource(loaded.get(0)).equals("missing:creature"),"Syringe overwrote missing source");
        h.assertTrue(BloodSampleData.rawSource(loaded.get(1)).equals("minecraft:pig"),"Syringe did not fill next empty vial");
        h.assertTrue(loaded.get(1).getHoverName().getString().equals("Field vessel") && loaded.get(1).get(DataComponents.CUSTOM_DATA).copyTag().getString("addon_marker").equals("preserve"),"Syringe replaced opaque vial data");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void machineInteractionConsumesActionBeforeOffhandInjection(GameTestHelper h) {
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"injection-test"),false);
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        var pos=h.absolutePos(new net.minecraft.core.BlockPos(0,2,0));
        h.getLevel().setBlockAndUpdate(pos,BlockInit.vial_centrifuge.get().defaultBlockState());
        player.setPos(net.minecraft.world.phys.Vec3.atCenterOf(pos).add(0,1,-1));
        player.setItemInHand(InteractionHand.OFF_HAND,vial("minecraft:pig"));
        var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),net.minecraft.core.Direction.UP,pos,false);
        com.vincenthuto.hemomancy.common.event.MachineAccessEvents.awardMachineCrafted(player,BlockInit.vial_centrifuge.get());
        var result=player.gameMode.useItemOn(player,h.getLevel(),ItemStack.EMPTY,InteractionHand.MAIN_HAND,hit);
        h.assertTrue(result.consumesAction() && !player.isUsingItem() && BloodSampleData.isFilled(player.getOffhandItem()),"Machine interaction result="+result+", using="+player.isUsingItem()+", offhandFilled="+BloodSampleData.isFilled(player.getOffhandItem()));
        player.closeContainer();player.discard();h.succeed();
    }

}

