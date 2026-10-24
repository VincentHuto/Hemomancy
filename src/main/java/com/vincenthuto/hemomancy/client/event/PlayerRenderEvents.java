package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.morphling.MorphlingPlayerPartVisibility;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * Hides and restores player model parts covered by an equipped morphling.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class PlayerRenderEvents {

    private PlayerRenderEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderPlayerSize(RenderPlayerEvent.Pre event) {
        MorphlingPlayerPartVisibility.apply(event.getEntity(), event.getRenderer());
    }

    @SubscribeEvent
    public static void restoreMorphlingHiddenPlayerParts(RenderPlayerEvent.Post event) {
        MorphlingPlayerPartVisibility.restore(event.getRenderer());
    }
}
