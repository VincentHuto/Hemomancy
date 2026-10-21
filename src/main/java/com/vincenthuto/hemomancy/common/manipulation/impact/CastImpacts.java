package com.vincenthuto.hemomancy.common.manipulation.impact;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPresentation;
import com.vincenthuto.hemomancy.common.manipulation.impact.CastImpactRules.Delivery;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Map;
import java.util.WeakHashMap;

/** Sends the impact cue for a landed school hit. Presentation only: it never touches damage, states or cooldowns. */
public final class CastImpacts {
    private static final Map<LivingEntity, Long> LAST = new WeakHashMap<>();

    private CastImpacts() {}

    public static void landed(LivingEntity target, DamageSource source, SchoolHitContext hit, float damage) {
        if (!(target.level() instanceof ServerLevel level) || hit.kind() != SchoolHitContext.Kind.DIRECT) return;
        int weight = CastImpactRules.weight(damage);
        long now = level.getGameTime();
        if (!CastImpactRules.allow(LAST.getOrDefault(target, -1L), now, weight)) return;
        LAST.put(target, now);
        var attacker = source.getEntity();
        var direct = source.getDirectEntity();
        // Only real shots are projectiles: clouds, pillars and constructs hit through their own sources but land as their cast does.
        boolean projectile = direct instanceof Projectile && direct != attacker;
        boolean weapon = !ManipulationInit.MANIPS_TYPE_REGISTRY.containsKey(Hemomancy.rloc(hit.ability().getPath()));
        Delivery delivery = CastImpactRules.delivery(projectile, weapon,
                weapon ? null : CastPresentation.forId(hit.ability().getPath()).purpose());
        Vec3 origin = projectile ? direct.position() : attacker != null ? attacker.getEyePosition() : null;
        AABB box = target.getBoundingBox();
        Vec3 centre = box.getCenter().add(0, box.getYsize() * .1, 0);
        Vec3 contact = origin == null || box.contains(origin) || delivery == Delivery.AREA ? centre
                : new Vec3(Math.clamp(origin.x, box.minX, box.maxX), Math.clamp(origin.y, box.minY + box.getYsize() * .25, box.minY + box.getYsize() * .8),
                        Math.clamp(origin.z, box.minZ, box.maxZ));
        Vec3 toward = origin == null ? target.getLookAngle().scale(-1) : contact.subtract(origin);
        if (delivery == Delivery.AREA) toward = new Vec3(toward.x, 0, toward.z);
        if (toward.lengthSqr() < 1e-6) toward = new Vec3(0, 0, 1);
        // Sit just outside the surface the blow struck, so the wound's spray is not buried in the body.
        if (delivery != Delivery.AREA) contact = contact.subtract(toward.normalize().scale(.08));
        double radius = Math.clamp(target.getBbWidth() * 1.4, .6, 2.2);
        ManipulationVisuals.burst(level, ManipulationVisuals.Form.CAST_IMPACT, contact, contact.add(toward.normalize()), radius,
                CastImpactRules.LIFE, CastImpactRules.encode(hit.primary(), hit.secondary(), delivery, weight));
    }
}
