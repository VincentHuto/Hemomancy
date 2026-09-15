package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class BombardierFlameAttack {
    private BombardierFlameAttack() {}

    public static void tickServer(PhlegethonticBombardier attacker) {
        int flameTick = attacker.getStateTick();
        Vec3 nozzle = attacker.getAbdominalNozzlePosition(1.0F);
        Vec3 direction = BombardierFlameGeometry.sweepDirection(attacker.getLockedDirection(),
                flameTick, PhlegethonticBombardier.FLAME_DURATION, 50.0);
        if (attacker.level() instanceof ServerLevel server) {
            ManipulationVisuals.burst(server, ManipulationVisuals.Form.BOMBARDIER_SWEEP,
                    nozzle, nozzle.add(direction.scale(PhlegethonticBombardier.MAX_FLAME_RANGE)), .35, 2);
        }
        if (flameTick != 0 && flameTick != 5 && flameTick != 10 && flameTick != 15) return;
        for (LivingEntity target : attacker.level().getEntitiesOfClass(LivingEntity.class,
                attacker.getBoundingBox().inflate(PhlegethonticBombardier.MAX_FLAME_RANGE), attacker::canSpray)) {
            Vec3 center = target.getBoundingBox().getCenter();
            if (!BombardierFlameGeometry.insideCone(nozzle, direction, center,
                    PhlegethonticBombardier.MAX_FLAME_RANGE, 12.0)) continue;
            HitResult hit = attacker.level().clip(new ClipContext(nozzle, center, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, attacker));
            if (hit.getType() != HitResult.Type.MISS && hit.getLocation().distanceToSqr(center) > .09) continue;
            if (target.hurt(attacker.level().damageSources().onFire(), 2.0F)) target.setRemainingFireTicks(80);
        }
    }
}
