package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodBoltEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LivingCrossbowCombatEvents {
    private LivingCrossbowCombatEvents() {}

    @SubscribeEvent
    public static void successfulImpact(LivingDamageEvent.Post event) {
        if (event.getSource().getDirectEntity() instanceof BloodBoltEntity bolt
                && event.getNewDamage() + event.getReduction(DamageContainer.Reduction.ABSORPTION) > 0) {
            bolt.chainFrom(event.getEntity(), event.getOriginalDamage());
        }
    }
}
