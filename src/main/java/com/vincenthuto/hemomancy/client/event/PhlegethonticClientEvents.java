package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.ExcoriatedSagittaryModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.PhlegethonticBombardierModel;
import com.vincenthuto.hemomancy.client.render.entity.mob.arthropod.PhlegethonticBombardierRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.ExcoriatedSagittaryRenderer;
import com.vincenthuto.hemomancy.client.render.entity.projectile.RecallBarbRenderer;
import com.vincenthuto.hemomancy.common.init.*;
import net.minecraft.client.renderer.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class PhlegethonticClientEvents {
    private PhlegethonticClientEvents() {}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ExcoriatedSagittaryModel.LAYER_LOCATION,ExcoriatedSagittaryModel::createBodyLayer);
        event.registerLayerDefinition(PhlegethonticBombardierModel.LAYER_LOCATION,PhlegethonticBombardierModel::createBodyLayer);
        event.registerLayerDefinition(RecallBarbRenderer.LAYER,RecallBarbRenderer::layer);
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.excoriated_sagittary.get(),ExcoriatedSagittaryRenderer::new);
        event.registerEntityRenderer(EntityInit.phlegethontic_bombardier.get(),PhlegethonticBombardierRenderer::new);
        event.registerEntityRenderer(EntityInit.recall_barb.get(),RecallBarbRenderer::new);
    }
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(FluidInit.PHLEGETHONTIC_ICHOR.get(),RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FluidInit.PHLEGETHONTIC_ICHOR_FLOWING.get(),RenderType.translucent());
        });
    }
}
