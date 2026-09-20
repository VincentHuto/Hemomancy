package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteStaffPlantingClientState;
import com.vincenthuto.hemomancy.client.sound.MicroscopeExaminationSound;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodInjectionData;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.network.PacketHematicMicroscopeViewing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;

public final class HematicMicroscopeClientState {
    private static final Map<Integer, Animation> ACTIVE = new HashMap<>();
    private static ClientLevel world;
    private HematicMicroscopeClientState() { }

    public static void clear() {
        ACTIVE.values().forEach(Animation::stopSound);
        ACTIVE.clear();
    }
    private static void checkWorld() {
        var current = Minecraft.getInstance().level;
        if (world != current) { clear(); world = current; }
    }
    public static void accept(PacketHematicMicroscopeViewing packet) {
        checkWorld();
        if (world == null) return;
        var old = ACTIVE.get(packet.entityId());
        if (old != null && old.packet.session() > packet.session()) return;
        if (packet.phase() == PacketHematicMicroscopeViewing.Phase.START) {
            if (old != null && old.packet.session() == packet.session()) return;
            if (old != null) old.stopSound();
            ACTIVE.put(packet.entityId(), new Animation(packet));
        } else if (old != null && old.packet.session() == packet.session()) {
            switch (packet.phase()) {
                case COMPLETE -> old.playback.complete();
                case RELEASE -> old.playback.release(world.getGameTime());
                case CANCEL -> ACTIVE.remove(packet.entityId());
                default -> { }
            }
            old.stopSound();
        }
    }
    public static Animation animation(LivingEntity player) {
        checkWorld();
        if (world == null || player == null) return null;
        var a = ACTIVE.get(player.getId());
        if (a == null || !a.valid(player)) return null;
        // Local release must close the lens and loop before the server's reply arrives.
        if (player == Minecraft.getInstance().player && !player.isUsingItem() && !a.playback.released()) {
            a.playback.release(world.getGameTime());
            a.stopSound();
        }
        return a.playback.expired(world.getGameTime()) ? null : a;
    }
    public static void tick() {
        checkWorld();
        if (world == null) return;
        ACTIVE.entrySet().removeIf(entry -> {
            var a = entry.getValue();
            var entity = world.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity player) || animation(player) == null) {
                a.stopSound();
                return true;
            }
            if (player.isUsingItem() && a.playback.shouldLoop(world.getGameTime())) {
                if (a.sound == null) {
                    a.sound = new MicroscopeExaminationSound(player, a);
                    Minecraft.getInstance().getSoundManager().play(a.sound);
                }
            } else a.stopSound();
            return false;
        });
    }
    public static final class Animation {
        public final PacketHematicMicroscopeViewing packet;
        public final HematicMicroscopePlayback playback;
        private final BloodInjectionData.Snapshot definitions = BloodInjectionData.snapshot(true);
        private final BloodProfileData.Snapshot profiles = BloodProfileData.snapshot(true);
        private final int selectedSlot;
        private final long receivedAt;
        private MicroscopeExaminationSound sound;
        Animation(PacketHematicMicroscopeViewing packet) {
            this.packet = packet;
            receivedAt = world.getGameTime();
            playback = new HematicMicroscopePlayback(Math.min(receivedAt, packet.startedAt()), packet.examining());
            var local = Minecraft.getInstance().player;
            selectedSlot = local == null ? -1 : local.getInventory().selected;
        }
        private boolean valid(LivingEntity player) {
            if (!player.isAlive() || player.isRemoved() || player.isSpectator()
                    || CardinalRiteStaffPlantingClientState.isAnimating(player) || WarpChairPlayerPose.isSeated(player)
                    || definitions != BloodInjectionData.snapshot(true)
                    || profiles != BloodProfileData.snapshot(true)
                    || !ItemStack.matches(player.getMainHandItem(), packet.instrument())
                    || !sameSample(player.getOffhandItem(), packet.sample())) return false;
            if (player == Minecraft.getInstance().player && Minecraft.getInstance().player.getInventory().selected != selectedSlot) return false;
            if (playback.released()) {
                // RELEASE is sent before vanilla clears its replicated use flag.
                boolean oldRemoteUse = player != Minecraft.getInstance().player
                        && playback.awaitingReleaseMetadata(world.getGameTime())
                        && player.getUsedItemHand() == InteractionHand.MAIN_HAND;
                return !player.swinging && (!player.isUsingItem() || oldRemoteUse);
            }
            // A newly tracked entity receives its use flag before equipment. Vanilla can
            // retain an empty useItem even after its main-hand stack arrives.
            if (player.isUsingItem()) return player.getUsedItemHand() == InteractionHand.MAIN_HAND
                    && (player != Minecraft.getInstance().player || ItemStack.matches(player.getUseItem(), packet.instrument()));
            return player == Minecraft.getInstance().player || world.getGameTime() - receivedAt <= 3;
        }
        private static boolean sameSample(ItemStack held, ItemStack snapshot) {
            if (ItemStack.matches(held, snapshot)) return true;
            var copy = held.copy();
            copy.remove(DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get());
            var original = snapshot.copy();
            original.remove(DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get());
            return ItemStack.matches(copy, original);
        }
        public float poseTick(float partialTick) { return playback.poseTick(world.getGameTime(), partialTick); }
        public float overlayAlpha(float partialTick) { return playback.overlayAlpha(world.getGameTime(), partialTick); }
        private void stopSound() {
            if (sound != null) { sound.finish(); Minecraft.getInstance().getSoundManager().stop(sound); sound = null; }
        }
    }
}
