package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.*;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.ChamberOfWillEffects;
import net.minecraft.client.renderer.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class DimensionEffectsRegistrar {

    private DimensionEffectsRegistrar() {
    }

    @SubscribeEvent
    public static void registerDimEffects(RegisterDimensionSpecialEffectsEvent event) {
        DimensionSpecialEffects fungalEffects =
                new FungalRealmsRenderInfo(Float.NaN, true, DimensionSpecialEffects.SkyType.END, true, true);
        event.register(Hemomancy.rloc("fungal_gardens"), fungalEffects);
        // Legacy alias in case an older save still points to hemomancy:renderer.
        event.register(Hemomancy.rloc("renderer"), fungalEffects);

        event.register(Hemomancy.rloc("chamber_of_will"), new ChamberOfWillEffects());

    }
}
