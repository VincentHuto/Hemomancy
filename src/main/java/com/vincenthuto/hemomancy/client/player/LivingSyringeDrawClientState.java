package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSyringeItem;
import com.vincenthuto.hemomancy.common.network.PacketLivingSyringeDraw;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class LivingSyringeDrawClientState {
    private static final Map<Integer, Animation> ACTIVE = new HashMap<>();
    private static ClientLevel world;

    private LivingSyringeDrawClientState() { }

    /** Forgets everything, so nothing outlives the world it was seen in. */
    public static void clear() {
        ACTIVE.clear();
        world = null;
    }

    private static void checkWorld() {
        var current = Minecraft.getInstance().level;
        if (world != current) { ACTIVE.clear(); world = current; }
    }

    public static void accept(PacketLivingSyringeDraw packet) {
        checkWorld();
        if (world != null) ACTIVE.put(packet.entityId(), new Animation(packet, world.getGameTime()));
    }

    public static Animation animation(LivingEntity entity) {
        checkWorld();
        if (entity == null || world == null || !entity.isAlive() || entity.isRemoved()) return null;
        Animation animation = ACTIVE.get(entity.getId());
        if (animation == null || animation.elapsed(0) >= LivingSyringeDrawTimeline.DURATION) return null;
        return entity.getItemInHand(animation.packet.hand()).getItem() instanceof LivingSyringeItem ? animation : null;
    }

    public static Collection<Animation> active() {
        checkWorld();
        return ACTIVE.values();
    }

    public static void tick() {
        checkWorld();
        if (world == null) return;
        ACTIVE.entrySet().removeIf(entry -> {
            if (!(world.getEntity(entry.getKey()) instanceof LivingEntity living) || animation(living) == null) return true;
            entry.getValue().spawnCells();
            return false;
        });
    }

    public static final class Animation {
        public final PacketLivingSyringeDraw packet;
        private final long start;
        private Vec3 wound, vial;

        private Animation(PacketLivingSyringeDraw packet, long start) {
            this.packet = packet;
            this.start = start;
        }

        public float elapsed(float partialTick) {
            return world.getGameTime() - start + partialTick;
        }

        public Entity target() {
            return world.getEntity(packet.targetId());
        }

        public ParticleColor color() {
            int rgb = packet.bloodColor();
            return new ParticleColor(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255);
        }

        /** Whether the living-blood ribbon suits this mob; other bloods are carried by tinted cells alone. */
        public boolean redBlood() {
            int rgb = packet.bloodColor();
            return (rgb >> 16 & 255) > 60 && (rgb >> 8 & 255) < 70 && (rgb & 255) < 70;
        }

        /** Remembers this frame's path ends, for the cells spawned on the next tick. */
        public void path(Vec3 wound, Vec3 vial) {
            this.wound = wound;
            this.vial = vial;
        }

        private void spawnCells() {
            float tick = elapsed(0);
            if (wound == null || tick < LivingSyringeDrawTimeline.BLOOD_START || tick >= LivingSyringeDrawTimeline.BLOOD_END) return;
            var cell = HemoParticleData.bloodCell(color());
            Vec3 across = vial.subtract(wound);
            int count = redBlood() ? 2 : 5;
            float head = LivingSyringeDrawTimeline.head(tick), tail = LivingSyringeDrawTimeline.tail(tick);
            for (int i = 0; i < count; i++) {
                double along = Mth.lerp(world.random.nextDouble(), tail, head);
                Vec3 at = wound.add(across.scale(along));
                // BloodCellParticle doubles the supplied velocity.
                Vec3 velocity = across.scale(.04);
                world.addParticle(cell, at.x + (world.random.nextDouble() - .5) * .08, at.y + (world.random.nextDouble() - .5) * .08,
                        at.z + (world.random.nextDouble() - .5) * .08, velocity.x, velocity.y, velocity.z);
            }
        }
    }
}
