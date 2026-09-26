package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodBoltEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LivingCrossbowCombatEvents {
    private LivingCrossbowCombatEvents() {}

    @SubscribeEvent
    public static void successfulImpact(LivingDamageEvent.Post event) {
        if (event.getSource().getDirectEntity() instanceof BloodBoltEntity bolt
                && SchoolDamage.hasHealthOrAbsorptionDamage(event)) {
            bolt.chainFrom(event.getEntity(), event.getOriginalDamage());
        }
    }
}
