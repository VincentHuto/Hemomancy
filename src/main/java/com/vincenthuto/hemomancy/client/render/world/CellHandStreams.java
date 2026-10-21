package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.item.harbinger.FirstPersonPalm;
import com.vincenthuto.hemomancy.client.render.item.harbinger.HeldWeaponPose;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringAnchors;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Lets the blood drawn into a cell hand ride the absorption streams the cast geometry draws, so the drops stay inside
 * the stream as it trails and swings with the caster instead of flying a straight line to where the palm was when they
 * set off. Streams are kept as offsets from the caster's feet and re-placed on the caster each frame, and their palm end
 * is pulled onto the palm as it is right now.
 */
public final class CellHandStreams {
    /** Absorption draws at most this many streams at once, in slots 0 and up. */
    private static final int DRINKING = 4;
    /** How far from a stream's root a drop may set off and still be taken for that stream's, in blocks. */
    private static final double ROOTED = 1.5;

    private CellHandStreams() {}

    /** The slot of {@code caster}'s drawn absorption stream rooted nearest {@code source}, or -1 when none is close. */
    public static int nearest(Entity caster, Vec3 source, float partial) {
        double time = caster.level().getGameTime() + partial;
        Vec3 feet = caster.getPosition(partial);
        int best = -1;
        double closest = ROOTED * ROOTED;
        for (int slot = 0; slot < DRINKING; slot++) {
            Vec3[] stream = StreamInertia.drawn(caster.getId(), slot, time);
            if (stream == null) continue;
            double distance = feet.add(stream[0]).distanceToSqr(source);
            if (distance < closest) {
                closest = distance;
                best = slot;
            }
        }
        return best;
    }

    /**
     * Where a drop {@code f} of the way from the drained body (0) to the palm (1) along stream {@code slot} sits now,
     * in world space, held {@code lane} off the stream's centre on a path that turns about it. Null when the stream is
     * no longer drawn.
     */
    public static Vec3 along(Entity caster, int slot, double f, double lane, double laneAngle, float partial) {
        Vec3[] stream = StreamInertia.drawn(caster.getId(), slot, caster.level().getGameTime() + partial);
        if (stream == null) return null;
        Vec3 feet = caster.getPosition(partial);
        int n = stream.length;
        double at = Math.clamp(f, 0, 1) * (n - 1);
        int i = Math.min(n - 2, (int) at);
        Vec3 point = feet.add(stream[i].lerp(stream[i + 1], at - i));
        // The stream was drawn last frame; slide its palm end onto the palm as drawn now, easing to nothing at the body.
        Vec3 palm = livePalm(caster);
        if (palm != null) point = point.add(palm.subtract(feet.add(stream[n - 1])).scale(f));
        Vec3 tangent = stream[i + 1].subtract(stream[i]);
        if (lane <= 0 || tangent.lengthSqr() < 1e-8) return point;
        Vec3 side = tangent.cross(CasterFrame.UP).lengthSqr() < 1e-8 ? new Vec3(1, 0, 0) : tangent.cross(CasterFrame.UP).normalize();
        Vec3 normal = side.cross(tangent).normalize();
        double angle = laneAngle + f * 4;
        // Tucked in at both ends, where the stream is rooted and where it pours into the palm.
        double r = lane * Math.sin(Math.PI * f);
        return point.add(side.scale(Math.cos(angle) * r)).add(normal.scale(Math.sin(angle) * r));
    }

    /**
     * The palm the cast geometry draws the streams into, as it is this frame: the drawn first-person cell hand, or the
     * posed third-person hand, or the head of a Living Staff channelling through it. Null when none has been captured lately.
     */
    public static Vec3 livePalm(Entity caster) {
        if (!(caster instanceof Player player)) return null;
        Vec3 staff = staffHead(player);
        if (staff != null) return staff;
        var mc = Minecraft.getInstance();
        if (player == mc.player && mc.options.getCameraType().isFirstPerson()) return FirstPersonPalm.world(player.getMainArm());
        Vec3 captured = OutpouringAnchors.capturedMainHand(player);
        return captured != null && captured.distanceToSqr(player.position()) < 9 ? captured : null;
    }

    /**
     * A Living Staff channels the cell hands' utilities through its head: where that head is drawn this frame while
     * {@code player} holds the staff in use, or null when they are not or that hand was not drawn just now.
     */
    public static Vec3 staffHead(Player player) {
        if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof LivingStaffItem)) return null;
        HumanoidArm arm = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        Vec3 head = HeldWeaponPose.top(player, arm, player.getUseItem());
        return head != null && head.distanceToSqr(player.position()) < 9 ? head : null;
    }
}
