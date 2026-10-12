package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.FerricConstructEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

/**
 * Friendly shots pass through a Ferric Rampart. The wall is a pickable, invulnerable body, so without this a
 * vanilla arrow's failed hurt would deflect it back. A cancelled impact keeps the projectile flying on its velocity.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class FerricRampartEvents {
    private FerricRampartEvents() {
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult hit
                && hit.getEntity() instanceof FerricConstructEntity wall
                && wall.letsThrough(event.getProjectile())) event.setCanceled(true);
    }
}
