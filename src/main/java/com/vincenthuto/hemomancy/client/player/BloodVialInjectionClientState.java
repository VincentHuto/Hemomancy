package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.render.world.ManipulationAmbientParticles;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteStaffPlantingClientState;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationAccentPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;

public final class BloodVialInjectionClientState {
    private static final Map<Integer, Animation> ACTIVE = new HashMap<>();
    private static ClientLevel world;
    private BloodVialInjectionClientState() { }

    private static void checkWorld() {
        var current = Minecraft.getInstance().level;
        if (world != current) { ACTIVE.clear(); world = current; }
    }

    public static void accept(PacketBloodVialInjection packet) {
        checkWorld();
        if (world == null) return;
        Animation old = ACTIVE.get(packet.entityId());
        if (old != null && old.packet.startedAt() > packet.startedAt()) return;
        if (packet.phase() == PacketBloodVialInjection.Phase.CANCEL) {
            if (old != null && old.packet.startedAt() == packet.startedAt()) ACTIVE.remove(packet.entityId());
            return;
        }
        if (packet.phase() == PacketBloodVialInjection.Phase.START) {
            if (old == null || old.packet.startedAt() != packet.startedAt())
                ACTIVE.put(packet.entityId(), new Animation(packet, Math.min(world.getGameTime(), packet.startedAt())));
            return;
        }
        if (old == null || old.packet.startedAt() != packet.startedAt()) {
            old = new Animation(packet, world.getGameTime());
            ACTIVE.put(packet.entityId(), old);
        }
        if (old.playback.confirm(world.getGameTime())) {
            old.vial = packet.vial();
            if (world.getEntity(packet.entityId()) instanceof AbstractClientPlayer player) impact(player, old);
        }
    }

    public static Animation animation(LivingEntity entity) {
        checkWorld();
        if (entity == null || !entity.isAlive() || entity.isRemoved()
                || CardinalRiteStaffPlantingClientState.isAnimating(entity) || WarpChairPlayerPose.isSeated(entity)) return null;
        Animation animation = ACTIVE.get(entity.getId());
        if (animation == null || world == null || animation.playback.expired(world.getGameTime())) return null;
        if (entity == Minecraft.getInstance().player && animation.packet.hand() == net.minecraft.world.InteractionHand.MAIN_HAND
                && Minecraft.getInstance().player.getInventory().selected != animation.selectedSlot) return null;
        if (!(entity.getItemInHand(animation.packet.hand()).getItem() instanceof BloodVialItem)) return null;
        if (animation.playback.confirmed()) {
            float elapsed = animation.elapsed(0);
            if (entity.swinging || entity.isUsingItem() && entity.getTicksUsingItem() < 8) return null;
            var held = entity.getItemInHand(animation.packet.hand());
            boolean awaitingInventorySync = elapsed < 19 && ItemStack.isSameItemSameComponents(held, animation.packet.vial());
            if (!awaitingInventorySync && !ItemStack.isSameItemSameComponents(held, animation.vial)) return null;
        }
        return animation;
    }

    public static void tick() {
        checkWorld();
        if (world == null) return;
        ACTIVE.entrySet().removeIf(entry -> {
            var entity = world.getEntity(entry.getKey());
            return !(entity instanceof LivingEntity living) || !living.isAlive() || living.isRemoved()
                    || entry.getValue().playback.expired(world.getGameTime())
                    || entry.getValue().playback.confirmed() && animation(living) == null;
        });
    }

    private static void impact(AbstractClientPlayer player, Animation animation) {
        boolean slim = player.getSkin().model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM;
        var model = new PlayerModel<AbstractClientPlayer>(LayerDefinition.create(
                PlayerModel.createMesh(CubeDeformation.NONE, slim), 64, 64).bakeRoot(), slim);
        if (player.isCrouching()) {
            model.rightArm.y = model.leftArm.y = 5.2F;
            model.rightLeg.y = model.leftLeg.y = 12.2F;
            model.rightLeg.z = model.leftLeg.z = 4;
        }
        BloodVialInjectionPose.apply(model, 16, animation.packet.right());
        var contact = BloodVialInjectionPose.thighContact(model, animation.packet.right());
        double scale = .9375 * player.getScale();
        Vec3 offset = new Vec3(contact.x * scale, (1.501 - contact.y) * scale,
                -contact.z * scale).yRot((float) Math.toRadians(-player.yBodyRot));
        Vec3 at = player.position().add(offset).add(0, player.isCrouching() ? -.125 : 0, 0);
        Vec3 outward = new Vec3(animation.packet.right() ? -.35 : .35, .12, .7)
                .yRot((float) Math.toRadians(-player.yBodyRot));
        for (int i = 0; i < 9; i++) {
            // BloodCellParticle doubles the supplied velocity.
            Vec3 velocity = outward.scale(.035 + world.random.nextDouble() * .025).add(
                    (world.random.nextDouble() - .5) * .025, world.random.nextDouble() * .025,
                    (world.random.nextDouble() - .5) * .025);
            world.addParticle(BloodCellParticleFactory.createData(new ParticleColor(190, 10, 35)),
                    at.x, at.y, at.z, velocity.x, velocity.y, velocity.z);
        }
        ManipulationAmbientParticles.accept(new ManipulationAccentPacket(animation.packet.tendency(), null,
                at, outward.normalize(), -1, false));
        world.playLocalSound(at.x, at.y, at.z, SoundInit.BLOOD_VIAL_INJECT.get(), SoundSource.PLAYERS, .7F, 1, false);
    }

    public static final class Animation {
        public final PacketBloodVialInjection packet;
        public final BloodVialInjectionPlayback playback;
        private final int selectedSlot;
        public ItemStack vial;
        private Animation(PacketBloodVialInjection packet, long start) {
            this.packet = packet;
            this.vial = packet.vial();
            this.playback = new BloodVialInjectionPlayback(start);
            var local = Minecraft.getInstance().player;
            this.selectedSlot = local == null ? -1 : local.getInventory().selected;
        }
        public float elapsed(float partialTick) { return playback.elapsed(world.getGameTime(), partialTick); }
    }
}
