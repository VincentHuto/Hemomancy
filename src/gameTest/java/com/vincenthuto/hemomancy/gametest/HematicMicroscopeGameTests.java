package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHematicMicroscopeViewing;
import com.vincenthuto.hemomancy.common.network.PacketHematicMicroscopeViewing.Phase;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("blood_injection_validation")
@PrefixGameTestTemplate(false)
public final class HematicMicroscopeGameTests {
    private static ItemStack vial(String source) {
        var stack = new ItemStack(ItemInit.bloody_vial.get());
        var tag = new CompoundTag();
        tag.putString("entity_type", source);
        tag.putString("addon_marker", "preserve");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
    private static net.minecraft.world.entity.player.Player examining(GameTestHelper h, ItemStack sample) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(0.5, 3, 0.5)));
        player.setNoGravity(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
        player.setItemInHand(InteractionHand.OFF_HAND, sample);
        player.getMainHandItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        return player;
    }
    @GameTest(template = "empty")
    public static void fortyTicksIdentifyOnceWithoutConsuming(GameTestHelper h) {
        var sample = vial("minecraft:pig");
        var before = sample.copy();
        var player = examining(h, sample);
        for (int i = 0; i < 39; i++) player.tick();
        h.assertTrue(!BloodSampleData.identified(sample), "Identified before 40 ticks");
        player.tick();
        h.assertTrue(BloodSampleData.identified(sample), "Did not identify at 40 ticks");
        BloodSampleData.identify(before);
        for (int i = 0; i < 60; i++) {
            player.tick();
            h.assertTrue(player.isUsingItem(), "Viewing stopped on tick " + (41 + i) + "; health=" + player.getHealth()
                    + "; y=" + player.getY() + "; same specimen=" + (player.getOffhandItem() == sample));
        }
        h.assertTrue(player.isUsingItem(), "Continuous viewing stopped");
        h.assertTrue(ItemStack.matches(before, sample), "Examination consumed or fragmented sample identity");
        var restored = ItemStack.parseOptional(h.getLevel().registryAccess(), (CompoundTag) sample.save(h.getLevel().registryAccess()));
        h.assertTrue(ItemStack.matches(restored, sample), "Identification did not persist");
        player.stopUsingItem();
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void replacementsAndInterruptionsCannotComplete(GameTestHelper h) {
        for (int mode = 0; mode < 5; mode++) {
            var sample = vial("minecraft:pig");
            var player = examining(h, sample);
            for (int i = 0; i < 20; i++) player.tick();
            if (mode == 0) player.setItemInHand(InteractionHand.OFF_HAND, sample.copy());
            if (mode == 1) player.stopUsingItem();
            if (mode == 2) sample.set(DataComponents.CUSTOM_DATA, vial("minecraft:polar_bear").get(DataComponents.CUSTOM_DATA));
            if (mode == 3) player.setItemInHand(InteractionHand.MAIN_HAND, player.getMainHandItem().copy());
            if (mode == 4) player.setHealth(0);
            for (int i = 0; i < 45; i++) player.tick();
            h.assertTrue(!BloodSampleData.identified(sample) && !BloodSampleData.identified(player.getOffhandItem()), "Interrupted sample identified: " + mode);
            player.stopUsingItem();
        }
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void invalidInputsNeverIdentifyOrInject(GameTestHelper h) {
        for (var sample : new ItemStack[]{ItemStack.EMPTY, new ItemStack(Items.STONE), new ItemStack(ItemInit.bloody_vial.get()),
                new ItemStack(ItemInit.vial_rack.get()), vial("absent:creature"), vial("bad source")}) {
            var player = examining(h, sample);
            for (int i = 0; i < 50; i++) player.tick();
            h.assertTrue(!BloodSampleData.identified(sample), "Invalid input identified");
            h.assertTrue(!player.isUsingItem() || player.getUsedItemHand() == InteractionHand.MAIN_HAND, "Offhand injection started");
            player.stopUsingItem();
        }
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
        player.setItemInHand(InteractionHand.MAIN_HAND, vial("minecraft:pig"));
        player.getOffhandItem().use(h.getLevel(), player, InteractionHand.OFF_HAND);
        h.assertTrue(!player.isUsingItem(), "Wrong-hand examination started");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void logoutAndReloadInvalidateTheSession(GameTestHelper h) {
        for (boolean reload : new boolean[]{false, true}) {
            var sample = vial("minecraft:pig");
            var player = examining(h, sample);
            for (int i = 0; i < 20; i++) player.tick();
            if (reload) {
                var barrier = new net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier() {
                    @Override public <T> java.util.concurrent.CompletableFuture<T> wait(T value) {
                        return java.util.concurrent.CompletableFuture.completedFuture(value);
                    }
                };
                new BloodInjectionData().reload(barrier, h.getLevel().getServer().getResourceManager(),
                        net.minecraft.util.profiling.InactiveProfiler.INSTANCE, net.minecraft.util.profiling.InactiveProfiler.INSTANCE,
                        Runnable::run, Runnable::run).join();
            } else {
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
                        new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
            }
            for (int i = 0; i < 45; i++) player.tick();
            h.assertTrue(!BloodSampleData.identified(sample), "Stale session completed after logout/reload");
            player.stopUsingItem();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void knownSamplesAndSpoofedCompletion(GameTestHelper h) {
        var sample = vial("minecraft:pig");
        var player = examining(h, sample);
        player.getMainHandItem().finishUsingItem(h.getLevel(), player);
        h.assertTrue(!BloodSampleData.identified(sample), "Early completion identified a sample");
        player.stopUsingItem();
        BloodSampleData.identify(sample);
        var before = sample.copy();
        player = examining(h, sample);
        for (int i = 0; i < 60; i++) player.tick();
        h.assertTrue(ItemStack.matches(before, sample), "Already identified sample changed");
        player.stopUsingItem();
        var spectator = h.makeMockPlayer(GameType.SPECTATOR);
        spectator.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
        spectator.setItemInHand(InteractionHand.OFF_HAND, vial("minecraft:pig"));
        spectator.getMainHandItem().use(h.getLevel(), spectator, InteractionHand.MAIN_HAND);
        h.assertTrue(!spectator.isUsingItem(), "Spectator started examination");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void unknownPropertiesRemainVisibleWithoutAnInjectionDefinition(GameTestHelper h) {
        var profile = BloodSampleData.profile(vial("minecraft:pig"), java.util.List.of());
        h.assertTrue(profile.properties().contains(net.minecraft.resources.ResourceLocation.parse("blood_injection_validation:blood_properties/test_unknown")),
                "Tagged addon property required an injection definition");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void spectatorTransitionCancelsAnActiveExamination(GameTestHelper h) {
        boolean[] spectator = {false};
        var player = new net.minecraft.world.entity.player.Player(h.getLevel(), net.minecraft.core.BlockPos.ZERO, 0,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "microscope-transition")) {
            @Override public boolean isSpectator() { return spectator[0]; }
            @Override public boolean isCreative() { return false; }
        };
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(0.5, 3, 0.5)));
        player.setNoGravity(true);
        var sample = vial("minecraft:pig");
        player.setItemInHand(InteractionHand.OFF_HAND, sample);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
        player.getMainHandItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        for (int i = 0; i < 20; i++) player.tick();
        spectator[0] = true;
        for (int i = 0; i < 40; i++) player.tick();
        h.assertTrue(!player.isUsingItem() && !BloodSampleData.identified(sample), "Spectator transition retained examination");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void independentExaminationsMatchAndMissingSourcesRetainTheirData(GameTestHelper h) {
        var a = vial("minecraft:polar_bear");
        var b = a.copy();
        for (var sample : new ItemStack[]{a, b}) {
            var player = examining(h, sample);
            for (int i = 0; i < 40; i++) player.tick();
            player.stopUsingItem();
        }
        h.assertTrue(BloodSampleData.identified(a) && ItemStack.matches(a, b), "Independent examinations fragmented equivalent samples");
        var tag = a.get(DataComponents.CUSTOM_DATA).copyTag();
        tag.putString("entity_type", "removed:creature");
        a.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        var before = a.copy();
        var player = examining(h, a);
        for (int i = 0; i < 45; i++) player.tick();
        h.assertTrue(ItemStack.matches(before, a), "Missing source lost identification or original custom data");
        h.assertTrue(BloodSampleData.profile(a, BloodInjectionData.snapshot(false).properties()).tendencies().isEmpty(), "Missing source invented stale traits");
        player.stopUsingItem();
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void viewingPacketsMirrorAndCompleteOnceWithoutConsumption(GameTestHelper h) {
        for (var arm : net.minecraft.world.entity.HumanoidArm.values()) {
            var capture = animationPlayer(h);
            var player = capture.player();
            player.setMainArm(arm);
            var sample = vial("minecraft:pig");
            player.setItemInHand(InteractionHand.OFF_HAND, sample);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
            player.getMainHandItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
            var start = capture.packets().getFirst();
            h.assertTrue(start.phase() == Phase.START && start.examining()
                    && start.right() == (arm == net.minecraft.world.entity.HumanoidArm.RIGHT), "Incorrect viewing start/side");
            for (int i = 0; i < 39; i++) player.doTick();
            h.assertTrue(!BloodSampleData.identified(sample) && capture.packets().size() == 1, "Early completion");
            player.doTick();
            h.assertTrue(BloodSampleData.identified(sample) && sample.getCount() == 1 && player.getOffhandItem() == sample,
                    "Examination changed its sample or failed to identify");
            h.assertTrue(capture.packets().size() == 2 && capture.packets().getLast().phase() == Phase.COMPLETE
                    && !capture.packets().getLast().examining(), "Completion did not stop progress sound");
            for (int i = 0; i < 40; i++) player.doTick();
            h.assertTrue(player.isUsingItem() && capture.packets().size() == 2, "Holding the lens replayed completion");
            player.releaseUsingItem();
            h.assertTrue(capture.packets().getLast().phase() == Phase.RELEASE, "Release did not withdraw vial");
            for (var packet : capture.packets()) {
                var buf = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), h.getLevel().registryAccess());
                try {
                    PacketHematicMicroscopeViewing.STREAM_CODEC.encode(buf, packet);
                    var decoded = PacketHematicMicroscopeViewing.STREAM_CODEC.decode(buf);
                    h.assertTrue(decoded.entityId() == player.getId() && decoded.session() == start.session()
                            && decoded.startedAt() == start.startedAt() && decoded.phase() == packet.phase()
                            && decoded.examining() == packet.examining() && decoded.right() == start.right()
                            && ItemStack.matches(decoded.instrument(), packet.instrument())
                            && ItemStack.matches(decoded.sample(), packet.sample()), "Viewing codec lost state");
                } finally { buf.release(); }
            }
            player.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void knownAndMurkySamplesNeverRequestProgressLoop(GameTestHelper h) {
        for (boolean known : new boolean[]{true, false}) {
            var capture = animationPlayer(h);
            var player = capture.player();
            var sample = vial(known ? "minecraft:pig" : "missing:creature");
            if (known) BloodSampleData.identify(sample);
            var before = sample.copy();
            player.setItemInHand(InteractionHand.OFF_HAND, sample);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
            player.getMainHandItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
            for (int i = 0; i < 45; i++) player.doTick();
            h.assertTrue(capture.packets().stream().noneMatch(PacketHematicMicroscopeViewing::examining), "Known or unreadable sample requested loop");
            h.assertTrue(ItemStack.matches(sample, before), "Viewing changed known or unreadable sample");
            player.releaseUsingItem(); player.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void replacementCancelsViewingAndRecovery(GameTestHelper h) {
        for (boolean released : new boolean[]{false, true}) {
            var capture = animationPlayer(h);
            var player = capture.player();
            var sample = vial("minecraft:pig");
            player.setItemInHand(InteractionHand.OFF_HAND, sample);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
            player.getMainHandItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
            for (int i = 0; i < 14; i++) player.doTick();
            if (released) player.releaseUsingItem();
            player.setItemInHand(InteractionHand.OFF_HAND, sample.copy());
            player.doTick();
            h.assertTrue(capture.packets().getLast().phase() == Phase.CANCEL && !player.isUsingItem()
                    && !BloodSampleData.identified(sample), "Identical replacement retained viewing/recovery");
            player.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void newObserverReceivesCurrentViewingSession(GameTestHelper h) {
        var capture = animationPlayer(h);
        var player = capture.player();
        player.setItemInHand(InteractionHand.OFF_HAND, vial("minecraft:pig"));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
        player.getMainHandItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        var start = capture.packets().getFirst();
        for (int i = 0; i < 40; i++) player.doTick();
        var observer = animationPlayer(h);
        observer.packets().clear();
        MicroscopeExamination.tracking(new net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking(observer.player(), player));
        var current = observer.packets().getLast();
        h.assertTrue(current.phase() == Phase.START && current.session() == start.session()
                && current.startedAt() == start.startedAt() && !current.examining() && BloodSampleData.identified(current.sample()),
                "Late tracker replayed an examination or lost the original pose time");
        player.releaseUsingItem(); player.discard(); observer.player().discard();
        h.succeed();
    }

    private static AnimationCapture animationPlayer(GameTestHelper h) {
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "microscope-animation"), false);
        var player = new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(), h.getLevel(),
                cookie.gameProfile(), cookie.clientInformation());
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        var packets = new java.util.ArrayList<PacketHematicMicroscopeViewing>();
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {
                if (packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket payload
                        && payload.payload() instanceof PacketHematicMicroscopeViewing animation)
                    packets.add(animation);
            }
        };
        player.setPos(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1))));
        h.getLevel().addNewPlayer(player);
        return new AnimationCapture(player, packets);
    }

    private record AnimationCapture(net.minecraft.server.level.ServerPlayer player,
            java.util.List<PacketHematicMicroscopeViewing> packets) { }
}
