package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.event.LivingSyringeDrawClientEvents;
import com.vincenthuto.hemomancy.client.player.LivingSyringeDrawClientState;
import com.vincenthuto.hemomancy.client.player.LivingSyringeDrawTimeline;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.item.harbinger.FirstPersonPalm;
import com.vincenthuto.hemomancy.client.render.item.harbinger.HeldWeaponPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/** The blood a Living Syringe pulls: a ribbon running out of the mob and up the fang into the vial. */
public final class LivingSyringeDrawRenderer {
    private static final int SEGMENTS = 14;
    /** How far back from the fang tip towards the grip the blood ends: inside the vial it is filling. */
    private static final double VIAL_DEPTH = .3;

    private LivingSyringeDrawRenderer() { }

    public static void render(PoseStack poses, float partialTick) {
        var active = LivingSyringeDrawClientState.active();
        if (active.isEmpty()) return;
        var mc = Minecraft.getInstance();
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        var buffers = mc.renderBuffers().bufferSource();
        AnimusMortemRenderTypes.begin(AnimationTime.gameTime(mc.level, partialTick));
        poses.pushPose();
        poses.translate(-camera.x, -camera.y, -camera.z);
        for (var animation : active) {
            if (!(mc.level.getEntity(animation.packet.entityId()) instanceof Player player)
                    || LivingSyringeDrawClientState.animation(player) != animation) continue;
            Entity target = animation.target();
            if (target == null) continue;
            float tick = animation.elapsed(partialTick);
            Vec3 vial = vial(player, animation, tick, partialTick);
            Vec3 wound = wound(target, vial, partialTick);
            animation.path(wound, vial);
            if (animation.redBlood() && tick >= LivingSyringeDrawTimeline.BLOOD_START && tick < LivingSyringeDrawTimeline.BLOOD_END)
                draw(poses, buffers, camera, wound, vial, tick, animation.packet.entityId());
        }
        poses.popPose();
        buffers.endBatch(AnimusMortemRenderTypes.ANIMUS_DEPTH);
    }

    private static void draw(PoseStack poses, MultiBufferSource buffers, Vec3 camera, Vec3 wound, Vec3 vial, float tick, int seed) {
        double head = LivingSyringeDrawTimeline.head(tick), tail = LivingSyringeDrawTimeline.tail(tick);
        if (head - tail < .02) return;
        double swell = 1 + .3 * Math.max(0, LivingSyringeDrawTimeline.pull(tick));
        BloodStreams.stream(poses, buffers, path(wound, vial, tail, head, 0, 0, tick), camera, .05 * swell, .028 * swell, seed, .95F);
        // Two thin strands wind round the main flow, so the pull reads as drawn rather than poured.
        for (int strand = 0; strand < 2; strand++) {
            BloodStreams.stream(poses, buffers, path(wound, vial, tail, head, .05, strand * Math.PI, tick), camera,
                    .016, .01, seed + 17 + strand, .85F);
        }
        var view = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 right = new Vec3(view.getLeftVector()).reverse(), up = new Vec3(view.getUpVector());
        for (int bead = 0; bead < 3; bead++) {
            double along = Mth.lerp(Mth.frac(tick * .16 + bead / 3.0), tail, head);
            BloodStreams.droplet(poses, buffers, point(wound, vial, along), right, up, .055 * swell, seed + 41 + bead, .95F);
        }
        if (tail < .05) BloodStreams.droplet(poses, buffers, wound, right, up, .09 * swell, seed + 53, .9F);
    }

    private static List<Vec3> path(Vec3 wound, Vec3 vial, double tail, double head, double radius, double phase, float tick) {
        Vec3 along = vial.subtract(wound).normalize();
        Vec3 side = along.cross(new Vec3(0, 1, 0));
        side = side.lengthSqr() < 1e-4 ? new Vec3(1, 0, 0) : side.normalize();
        Vec3 over = side.cross(along);
        List<Vec3> points = new ArrayList<>(SEGMENTS + 1);
        for (int i = 0; i <= SEGMENTS; i++) {
            double t = Mth.lerp(i / (double) SEGMENTS, tail, head);
            double angle = t * 11 - tick * .5 + phase, r = radius * Math.sin(t * Math.PI);
            points.add(point(wound, vial, t).add(side.scale(Math.cos(angle) * r)).add(over.scale(Math.sin(angle) * r)));
        }
        return points;
    }

    /** A shallow arc, so the blood visibly leaves the mob rather than appearing as a straight line. */
    private static Vec3 point(Vec3 wound, Vec3 vial, double t) {
        double height = (.08 + wound.distanceTo(vial) * .1) * 4 * t * (1 - t);
        return wound.lerp(vial, t).add(0, height, 0);
    }

    private static Vec3 wound(Entity target, Vec3 towards, float partialTick) {
        var box = target.getBoundingBox().move(target.getPosition(partialTick).subtract(target.position()));
        Vec3 surface = new Vec3(Mth.clamp(towards.x, box.minX, box.maxX), Mth.clamp(towards.y, box.minY, box.maxY),
                Mth.clamp(towards.z, box.minZ, box.maxZ));
        return surface.lerp(box.getCenter(), .15);
    }

    private static Vec3 vial(Player player, LivingSyringeDrawClientState.Animation animation, float tick, float partialTick) {
        var mc = Minecraft.getInstance();
        boolean right = animation.packet.right();
        var held = HeldWeaponPose.held(player, right ? HumanoidArm.RIGHT : HumanoidArm.LEFT,
                player.getItemInHand(animation.packet.hand()), Vec3.ZERO);
        if (held == null) return player.getEyePosition(partialTick).add(player.getViewVector(partialTick).scale(.7)).add(0, -.25, 0);
        Vec3 tip = held.grip();
        double farthest = -1;
        for (Vec3[] face : held.faces()) {
            Vec3 centre = face[0].add(face[1]).add(face[2]).add(face[3]).scale(.25);
            double distance = centre.distanceToSqr(held.grip());
            if (distance > farthest) { farthest = distance; tip = centre; }
        }
        Vec3 at = tip.lerp(held.grip(), VIAL_DEPTH);
        if (player == mc.player && mc.options.getCameraType().isFirstPerson()) {
            // The recorded hold is the ordinary one; the drawn syringe has moved from it.
            Vec3 offset = LivingSyringeDrawClientEvents.firstPersonOffset(tick, right);
            at = at.add(FirstPersonPalm.posedOffset(new Vector3f((float) offset.x, (float) offset.y, (float) offset.z)));
        }
        return at;
    }
}
