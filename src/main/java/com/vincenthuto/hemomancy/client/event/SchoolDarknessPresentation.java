package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SchoolDarknessPresentation {
    private SchoolDarknessPresentation() {}
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog event) {
        if (!(event.getCamera().getEntity() instanceof LivingEntity viewer)
                || !SchoolStates.has(viewer, SchoolState.OBSCURED)) return;
        double strength = Minecraft.getInstance().options.darknessEffectScale().get();
        if (strength <= 0) return;
        event.setNearPlaneDistance((float)(event.getNearPlaneDistance() * (1 - strength) + 4 * strength));
        event.setFarPlaneDistance((float)(event.getFarPlaneDistance() * (1 - strength) + 12 * strength));
        event.setCanceled(true);
    }
}

