package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.*;
import com.vincenthuto.hemomancy.common.network.BloodProfileSyncPacket;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.*;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;
import java.nio.file.*;
import java.util.*;

@GameTestHolder("blood_injection_validation")
@PrefixGameTestTemplate(false)
public final class BloodProfileGameTests {
    private static ServerPlayer player(GameTestHelper h) {
        return player(h, packet -> {});
    }

    private static ServerPlayer player(GameTestHelper h, java.util.function.Consumer<net.minecraft.network.protocol.Packet<?>> received) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "blood-profile"), false);
        var player = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(player.server, connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { received.accept(packet); }
        };
        player.setGameMode(GameType.SURVIVAL);
        HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(1);
        return player;
    }

    private static void captureProfile(net.minecraft.network.protocol.Packet<?> packet, List<BloodProfileSyncPacket> received) {
        if (packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom
                && custom.payload() instanceof BloodProfileSyncPacket profiles) received.add(profiles);
        if (packet instanceof net.minecraft.network.protocol.game.ClientboundBundlePacket bundle)
            bundle.subPackets().forEach(child -> captureProfile(child, received));
    }

    @GameTest(template = "empty")
    public static void emptyVialsStackToSixtyFourWhileSamplesRemainSingle(GameTestHelper h) {
        var empty = new ItemStack(ItemInit.bloody_vial.get());
        empty.setCount(64);
        var filled = new ItemStack(ItemInit.bloody_vial.get());
        var data = new net.minecraft.nbt.CompoundTag();
        data.putString(BloodVialItem.TAG_ENTITY_TYPE, "minecraft:pig");
        data.putBoolean(BloodVialItem.TAG_STATE, true);
        filled.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(data));
        h.assertTrue(empty.getMaxStackSize() == 64, "Empty vials must stack to 64");
        h.assertTrue(filled.getMaxStackSize() == 1, "Filled samples must remain single");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void samplingOneVialFromAStackLeavesTheRestEmpty(GameTestHelper h) {
        var player = player(h);
        var empty = new ItemStack(ItemInit.bloody_vial.get(), 64);
        player.setItemInHand(InteractionHand.MAIN_HAND, empty);
        var pig = EntityType.PIG.create(h.getLevel());
        empty.getItem().onLeftClickEntity(empty, player, pig);
        h.assertTrue(BloodSampleData.isFilled(player.getMainHandItem()) && player.getMainHandItem().getCount() == 1,
                "Sampling did not place exactly one specimen in hand");
        int remaining = player.getInventory().items.stream().filter(VialRackItem::isEmptyVial).mapToInt(ItemStack::getCount).sum();
        h.assertTrue(remaining == 63, "Sampling did not preserve the other empty vials: " + remaining);
        pig.discard();
        player.discard();
        h.succeed();
    }

    @SuppressWarnings("unchecked")
    @GameTest(template = "empty")
    public static void datapackSyncTargetsJoiningPlayerAndBroadcastsReload(GameTestHelper h) throws Exception {
        var firstPackets = new ArrayList<BloodProfileSyncPacket>();
        var secondPackets = new ArrayList<BloodProfileSyncPacket>();
        var first = player(h, packet -> captureProfile(packet, firstPackets));
        var second = player(h, packet -> captureProfile(packet, secondPackets));
        var players = h.getLevel().getServer().getPlayerList();
        // getPlayers exposes a read-only view; insert the two network fixtures into the backing list.
        var field = net.minecraft.server.players.PlayerList.class.getDeclaredField("players");
        field.setAccessible(true);
        var connected = (List<ServerPlayer>) field.get(players);
        connected.add(first);
        connected.add(second);
        try {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.OnDatapackSyncEvent(players, first));
            h.assertTrue(firstPackets.size() == 1 && secondPackets.isEmpty(), "Login sync reached wrong recipients");
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.OnDatapackSyncEvent(players, null));
            h.assertTrue(firstPackets.size() == 2 && secondPackets.size() == 1, "Reload failed to reach both players");
            h.assertTrue(firstPackets.getLast().definitions().equals(BloodProfileData.snapshot(false).json())
                    && secondPackets.getLast().definitions().equals(firstPackets.getLast().definitions()), "Players received different profiles");
        } finally {
            connected.remove(first);
            connected.remove(second);
            first.discard();
            second.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void migratedProfilesRetainCentrifugeOutputsAndTargetAffinity(GameTestHelper h) {
        var pos = h.absolutePos(new net.minecraft.core.BlockPos(0, 2, 0));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.vial_centrifuge.get().defaultBlockState());
        var machine = (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity) h.getLevel().getBlockEntity(pos);
        var types = List.of(EntityType.PIG, EntityType.IRON_GOLEM, EntityInit.fungling.get());
        var items = List.of(ItemInit.vivacious_enzyme.get(), ItemInit.ferric_enzyme.get(), BlockInit.infected_fungus.get().asItem());
        for (int i = 0; i < types.size(); i++) {
            var target = types.get(i).create(h.getLevel());
            for (int attempt = 0; attempt < 8; attempt++) {
                var result = machine.getResultFromVial(types.get(i));
                h.assertTrue(result.is(items.get(i)) && result.getCount() >= 1
                        && result.getCount() <= (int) (target.getMaxHealth() / 10 * 3 + 1), "Centrifuge output changed: " + types.get(i));
            }
            target.discard();
        }
        var golem = EntityType.IRON_GOLEM.create(h.getLevel());
        h.assertTrue(TendencyWeaponHelper.isOpposingTarget(golem,
                com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.DUCTILIS), "Ferric target lost affinity");
        h.assertTrue(machine.getResultFromVial(EntityType.WARDEN).isEmpty(), "Warden gained ordinary enzymes");
        golem.discard();
        h.succeed();
    }

    private static ItemStack syringe(GameTestHelper h, ServerPlayer player) {
        var syringe = new ItemStack(ItemInit.living_syringe.get());
        var rack = new ItemStack(ItemInit.vial_rack.get());
        VialRackItem.ensureInitialized(rack);
        player.getInventory().setItem(1, rack);
        player.setItemInHand(InteractionHand.MAIN_HAND, syringe);
        syringe.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        return syringe;
    }

    @GameTest(template = "empty")
    public static void looseVialRejectsRestrictedTargetsWithoutChangingAnything(GameTestHelper h) {
        var player = player(h);
        player.getInventory().add(new ItemStack(ItemInit.living_syringe.get()));
        for (var type : List.of(EntityType.IRON_GOLEM, EntityType.VILLAGER, EntityType.WARDEN,
                EntityInit.harbinger_alchemist.get(), EntityInit.chitinite.get(), EntityInit.bog_revenant.get())) {
            var target = type.create(h.getLevel());
            target.setInvulnerable(false);
            var vial = new ItemStack(ItemInit.bloody_vial.get());
            vial.set(DataComponents.CUSTOM_NAME, Component.literal("Keep my vessel"));
            player.setItemInHand(InteractionHand.MAIN_HAND, vial);
            var before = vial.copy();
            float health = target.getHealth();
            boolean cancelled = vial.getItem().onLeftClickEntity(vial, player, target);
            h.assertTrue(cancelled && ItemStack.matches(before, vial), "Restricted sample changed vial: " + type);
            h.assertTrue(target.getHealth() == health, "Rejected sampling damaged target");
            h.assertTrue(!HemoCapabilityAccess.clinicalBlood(player).collected, "Rejection awarded collection evidence");
            target.discard();
        }
        player.discard();
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void syringeCollectsRestrictedBloodAndStillRejectsInvalidCondition(GameTestHelper h) {
        var player = player(h);
        var syringe = syringe(h, player);
        var target = EntityType.IRON_GOLEM.create(h.getLevel());
        player.setItemInHand(InteractionHand.MAIN_HAND, syringe);
        target.setInvulnerable(true);
        var before = syringe.copy();
        syringe.getItem().interactLivingEntity(syringe, player, target, InteractionHand.MAIN_HAND);
        h.assertTrue(ItemStack.matches(before, syringe), "Invulnerable target changed rack");
        target.setInvulnerable(false);
        target.setHealth(1);
        syringe.getItem().interactLivingEntity(syringe, player, target, InteractionHand.MAIN_HAND);
        var sample = VialRackItem.getVials(ItemStack.parseOptional(h.getLevel().registryAccess(), syringe.get(DataComponents.CUSTOM_DATA).copyTag().getCompound(LivingSyringeItem.TAG_LOADED_RACK))).getFirst();
        h.assertTrue(BloodSampleData.entityType(sample) == EntityType.IRON_GOLEM, "Syringe did not collect restricted sample");
        h.assertTrue(HemoCapabilityAccess.clinicalBlood(player).collected, "Successful syringe collection lost evidence");
        target.setHealth(0);
        before = syringe.copy();
        syringe.getItem().interactLivingEntity(syringe, player, target, InteractionHand.MAIN_HAND);
        h.assertTrue(ItemStack.matches(before, syringe), "Dead target changed rack");
        target.discard();
        player.discard();
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void unrestrictedAndSpecialCollectionsKeepTheirBehavior(GameTestHelper h) {
        var player = player(h);
        var vial = new ItemStack(ItemInit.bloody_vial.get());
        var pig = EntityType.PIG.create(h.getLevel());
        player.setItemInHand(InteractionHand.MAIN_HAND, vial);
        vial.getItem().onLeftClickEntity(vial, player, pig);
        h.assertTrue(BloodSampleData.entityType(vial) == EntityType.PIG, "Ordinary loose-vial sampling failed");
        var before = vial.copy();
        vial.getItem().onLeftClickEntity(vial, player, EntityType.IRON_GOLEM.create(h.getLevel()));
        h.assertTrue(ItemStack.matches(before, vial), "Existing sample was overwritten");
        vial = new ItemStack(ItemInit.bloody_vial.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, vial);
        vial.getItem().onLeftClickEntity(vial, player, EntityInit.hemolymphopoda.get().create(h.getLevel()));
        h.assertTrue(player.getMainHandItem().is(ItemInit.cleansing_hemolymph.get()), "Hemolymphopoda special collection changed");
        var syringe = syringe(h, player);
        player.setItemInHand(InteractionHand.MAIN_HAND, syringe);
        syringe.getItem().interactLivingEntity(syringe, player, EntityType.WARDEN.create(h.getLevel()), InteractionHand.MAIN_HAND);
        var sample = VialRackItem.getVials(ItemStack.parseOptional(h.getLevel().registryAccess(), syringe.get(DataComponents.CUSTOM_DATA).copyTag().getCompound(LivingSyringeItem.TAG_LOADED_RACK))).getFirst();
        h.assertTrue(com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.is(sample), "Warden lost its special sample");
        pig.discard();
        player.discard();
        h.succeed();
    }

    @SuppressWarnings("unchecked")
    @GameTest(template = "empty")
    public static void allRegisteredHighHealthMobsHaveAuthoredRestrictions(GameTestHelper h) {
        for (var type : BuiltInRegistries.ENTITY_TYPE) {
            var id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!id.getNamespace().equals("hemomancy") || !DefaultAttributes.hasSupplier(type)) continue;
            if (DefaultAttributes.getSupplier((EntityType<? extends net.minecraft.world.entity.LivingEntity>) type).getValue(Attributes.MAX_HEALTH) > 20)
                h.assertTrue(BloodProfileData.profile(type, false).requiresLivingSyringe(), "Missing high-health restriction: " + id);
        }
        h.assertTrue(!BloodProfileData.profile(EntityInit.fungling.get(), false).requiresLivingSyringe(), "Small unarmored mob restricted");
        h.assertTrue(!BloodProfileData.profile(EntityType.HUSK, false).requiresLivingSyringe(), "Husk substituted for unsupported Camel Husk");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void packOverrideReloadAndClientSyncReplaceWholeProfiles(GameTestHelper h) throws Exception {
        var original = BloodProfileData.snapshot(false);
        var originalClient = BloodProfileData.snapshot(true);
        var root = Files.createTempDirectory(Path.of("."), "blood-profile-packs-");
        var packs = new ArrayList<PackResources>();
        for (int i = 0; i < 2; i++) {
            var path = root.resolve("pack" + i);
            var file = path.resolve("data/minecraft/blood_profiles/pig.json");
            Files.createDirectories(file.getParent());
            Files.writeString(file, i == 0 ? "{\"tendencies\":[\"animus\"],\"properties\":[\"hemomancy:fungal\"]}"
                    : "{\"tendencies\":[\"ferric\"],\"requires_living_syringe\":true}");
            if (i == 1) {
                var dormant = path.resolve("data/absent_mod/blood_profiles/dormant.json");
                Files.createDirectories(dormant.getParent());
                Files.writeString(dormant, "{\"requires_living_syringe\":true}");
                Files.writeString(dormant.resolveSibling("malformed.json"), "{\"requires_living_syringe\":\"true\"}");
            }
            packs.add(new PathPackResources(new PackLocationInfo("fixture" + i, Component.literal("fixture"),
                    PackSource.DEFAULT, Optional.empty()), path));
        }
        var loader = new BloodProfileData();
        var prepare = BloodProfileData.class.getDeclaredMethod("prepare", ResourceManager.class, ProfilerFiller.class);
        var apply = BloodProfileData.class.getDeclaredMethod("apply", BloodProfileData.Snapshot.class, ResourceManager.class, ProfilerFiller.class);
        prepare.setAccessible(true);
        apply.setAccessible(true);
        try (var manager = new MultiPackResourceManager(PackType.SERVER_DATA, packs)) {
            var replacement = (BloodProfileData.Snapshot) prepare.invoke(loader, manager, InactiveProfiler.INSTANCE);
            var id = ResourceLocation.parse("minecraft:pig");
            h.assertTrue(replacement.json().size() == 2 && replacement.get(ResourceLocation.parse("absent_mod:dormant")).requiresLivingSyringe()
                    && replacement.get(ResourceLocation.parse("absent_mod:malformed")).equals(EntityBloodProfile.EMPTY), "Malformed/dormant definitions handled incorrectly");
            var definition = replacement.get(id);
            h.assertTrue(definition.requiresLivingSyringe() && definition.properties().isEmpty()
                    && definition.tendencies().equals(List.of(com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.FERRIC)),
                    "Higher priority pack did not replace entire definition");
            apply.invoke(loader, replacement, manager, InactiveProfiler.INSTANCE);
            var vial = new ItemStack(ItemInit.bloody_vial.get());
            var tag = new net.minecraft.nbt.CompoundTag();
            tag.putString(BloodVialItem.TAG_ENTITY_TYPE, id.toString());
            vial.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
            BloodSampleData.identify(vial);
            var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), h.getLevel().registryAccess());
            try {
                BloodProfileSyncPacket.STREAM_CODEC.encode(buf, new BloodProfileSyncPacket(replacement.json()));
                var decoded = BloodProfileSyncPacket.STREAM_CODEC.decode(buf);
                BloodProfileData.receive(decoded.definitions());
                h.assertTrue(BloodSampleData.profile(vial, true).equals(BloodSampleData.profile(vial, false)), "Client/server sample profiles differ");
            } finally { buf.release(); }
            BloodProfileData.clearClient();
            h.assertTrue(BloodProfileData.profile(EntityType.PIG, true).equals(EntityBloodProfile.EMPTY)
                    && BloodProfileData.profile(EntityType.PIG, false).requiresLivingSyringe(), "Disconnect cleared server snapshot");
            Files.delete(root.resolve("pack0/data/minecraft/blood_profiles/pig.json"));
            Files.delete(root.resolve("pack1/data/minecraft/blood_profiles/pig.json"));
            var removed = (BloodProfileData.Snapshot) prepare.invoke(loader, manager, InactiveProfiler.INSTANCE);
            apply.invoke(loader, removed, manager, InactiveProfiler.INSTANCE);
            h.assertTrue(BloodSampleData.profile(vial, false).tendencies().isEmpty() && BloodSampleData.identified(vial), "Reload retained stale traits or erased identification");
        } finally {
            apply.invoke(loader, original, null, InactiveProfiler.INSTANCE);
            BloodProfileData.receive(originalClient.json());
        }
        h.succeed();
    }
}
