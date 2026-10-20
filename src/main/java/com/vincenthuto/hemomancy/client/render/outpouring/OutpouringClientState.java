package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.client.player.FirstPersonHandRules;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Detail;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Mode;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Stage;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Client facade over the outpouring tracker: packets in, simulated blood and per-player vein inputs out. */
public final class OutpouringClientState {
    public record VeinState(VeinEncoding.Packed packed, boolean active) {}
    private static final VeinState NONE = new VeinState(null, false);
    private static final OutpouringTracker TRACKER = new OutpouringTracker(new OutpouringTracker.Resolver() {
        public EnumVeinSections section(String id) {
            var manipulation = ManipulationInit.getByName(id);
            if (manipulation != null && manipulation.getSection() != null) return manipulation.getSection();
            var preview = OutpouringRules.previewSection(id);
            return preview == null ? EnumVeinSections.ARMS : preview;
        }
        public EnumManipulationType type(CastingAnimationPacket packet) {
            var manipulation = ManipulationInit.getByName(packet.manipulation());
            if (manipulation != null) return manipulation.getType();
            return packet.required() > 0 ? EnumManipulationType.CHARGED : EnumManipulationType.QUICK;
        }
    });

    private OutpouringClientState() {}

    private static long now() {
        var level = Minecraft.getInstance().level;
        return level == null ? 0 : level.getGameTime();
    }

    public static void accept(CastingAnimationPacket packet) { TRACKER.accept(packet, now()); }
    /** The caster left this level (death screen, logout, tracking range). */
    public static void end(java.util.UUID player) { TRACKER.end(player, now()); }

    public static void clear() { TRACKER.clear(); OutpouringAnchors.clear(); OutpouringMarks.clearClient(); }
    static OutpouringTracker tracker() { return TRACKER; }

    /** Emits and steps every caster's blood for this tick, then drops casts whose blood has settled. */
    public static void tick() {
        var mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;
        long now = level.getGameTime();
        int setting = mc.options.particles().get().getId();
        var camera = mc.gameRenderer.getMainCamera().getPosition();
        var blocks = blocks(level);
        for (var cast : TRACKER.all()) {
            if (cast.sim == null) cast.sim = new OutpouringSim(cast.school, cast.secondary, cast.sequence * 31 + cast.entityId);
            var player = level.getEntity(cast.entityId) instanceof Player p && p.getUUID().equals(cast.player) ? p : null;
            if (player != null) { cast.seen = true; cast.lastX = player.getX(); cast.lastY = player.getY(); cast.lastZ = player.getZ(); }
            // The server sends nothing when a caster dies or leaves tracking; the blood draws back in either way.
            if (player == null ? cast.seen : !player.isAlive()) cast.end(now);
            var pulse = cast.pulse(now, 0);
            var profile = OutpouringRules.profile(cast.rank);
            Detail detail = player == null ? Detail.NONE
                    : OutpouringRules.detail(setting, camera.distanceToSqr(player.position()));
            boolean current = TRACKER.current(cast.player) == cast;
            boolean shown = player != null && !player.isInvisible() && (detail == Detail.FULL || detail == Detail.REDUCED);
            double[] outward = new double[15], exits = null;
            if (current && shown && cast.emitting(now)) {
                exits = OutpouringAnchors.exits(player, cast.section, cast.rank, 0, outward);
                boolean burst = pulse.stage() == Stage.PEAK && !cast.burst && cast.mode == Mode.FULL;
                if (pulse.stage() == Stage.PEAK) cast.burst = true;
                float strength = pulse.strength() * (detail == Detail.REDUCED ? .5f : 1);
                cast.sim.emit(exits, exits.length / 3, outward, strength, profile, cast.rank.ordinal(), burst,
                        cast.mode == Mode.WOUND);
            }
            int budget = detail == Detail.FULL || detail == Detail.REDUCED ? Math.max(0, profile.maxMarks() - cast.marks) : 0;
            List<OutpouringSim.Contact> contacts = cast.sim.step(blocks, cast.lastX, cast.lastY, cast.lastZ,
                    pulse.stage() == Stage.WITHDRAW, budget);
            cast.marks += OutpouringMarks.lay(contacts, cast, detail, now);
            if (shown) OutpouringAccents.tick(cast, exits, outward, pulse, contacts, now, detail == Detail.REDUCED);
        }
        TRACKER.tick(now);
        OutpouringMarks.pruneClient(now);
    }

    private static OutpouringSim.Blocks blocks(ClientLevel level) {
        var pos = new BlockPos.MutableBlockPos();
        return new OutpouringSim.Blocks() {
            public boolean solid(int x, int y, int z) {
                pos.set(x, y, z);
                if (!level.hasChunkAt(pos)) return true;
                return level.getBlockState(pos).blocksMotion();
            }
            public boolean liquid(int x, int y, int z) {
                pos.set(x, y, z);
                return level.hasChunkAt(pos) && !level.getBlockState(pos).getFluidState().isEmpty();
            }
        };
    }

    /** Camera-relative Lux orbs as {x, y, z, strength}, for the light pass. */
    static List<double[]> luxLights(Vec3 camera, float partial) {
        List<double[]> lights = new ArrayList<>();
        long now = now();
        for (var cast : TRACKER.all()) {
            if (cast.school != EnumBloodTendency.LUX || cast.sim == null || !lit(cast, camera)) continue;
            float strength = .6f + .4f * cast.pulse(now, partial).strength();
            for (var m : cast.sim.motes()) {
                if (m.kind != OutpouringSim.Kind.ORB) continue;
                float age = m.age + partial;
                float fade = Math.clamp(age / 4f, 0, 1) * Math.clamp((m.life - age) / (m.life * .25f), 0, 1);
                double x = m.prevX + (m.x - m.prevX) * partial, y = m.prevY + (m.y - m.prevY) * partial;
                double z = m.prevZ + (m.z - m.prevZ) * partial;
                lights.add(new double[]{x - camera.x, y - camera.y, z - camera.z, strength * fade});
            }
        }
        return lights;
    }

    /** Camera-relative Tenebris shades as {x, y, z, radius, strength, self}, for the darkness pass. */
    static List<double[]> shades(Vec3 camera, float partial) {
        List<double[]> shades = new ArrayList<>();
        var mc = Minecraft.getInstance();
        long now = now();
        // One shade per caster: a recast replaces the darkness rather than stacking a second sphere.
        for (var cast : TRACKER.casts()) {
            if (cast.school != EnumBloodTendency.TENEBRIS || !lit(cast, camera)) continue;
            float strength = cast.pulse(now, partial).strength();
            if (strength <= .01f) continue;
            double radius = .8 + OutpouringRules.profile(cast.rank).reach() * .9;
            boolean self = mc.player != null && cast.player.equals(mc.player.getUUID()) && mc.options.getCameraType().isFirstPerson();
            Vec3 at = mc.level.getEntity(cast.entityId).getPosition(partial);
            shades.add(new double[]{at.x - camera.x, at.y + 1 - camera.y, at.z - camera.z, radius, strength, self ? 1 : 0});
        }
        return shades;
    }

    /** Screen effects follow the same rules as the blood: a visible caster, near enough, with particles beyond Minimal. */
    private static boolean lit(OutpouringTracker.Cast cast, Vec3 camera) {
        var mc = Minecraft.getInstance();
        var entity = mc.level == null ? null : mc.level.getEntity(cast.entityId);
        if (!(entity instanceof Player player) || player.isInvisible() || !player.isAlive()) return false;
        var detail = OutpouringRules.detail(mc.options.particles().get().getId(), camera.distanceToSqr(player.position()));
        return detail == OutpouringRules.Detail.FULL || detail == OutpouringRules.Detail.REDUCED;
    }

    /** The vein and body-contact inputs for this player's current cast, or inactive when no blood is moving. */
    public static VeinState veins(Player player, float partial) {
        var cast = TRACKER.current(player.getUUID());
        if (cast == null) return NONE;
        var pulse = cast.pulse(now(), partial);
        float contact = (float) OutpouringRules.bodyContact(pulse, cast.rank);
        if (pulse.strength() <= .005f && contact <= .005f) return NONE;
        boolean offHand = FirstPersonHandRules.affectsOffHand(cast.rank, null);
        boolean leftMain = player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT;
        return new VeinState(VeinEncoding.pack(cast.school, cast.secondary, cast.section, cast.rank, pulse.strength(),
                contact, 1, offHand, leftMain, cast.seed), true);
    }
}
