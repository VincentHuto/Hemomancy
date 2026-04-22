package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

    import com.vincenthuto.hemomancy.common.entity.mob.monster.HematicConstructEntity;
    import net.minecraft.client.model.HumanoidModel;
    import net.minecraft.client.model.geom.ModelLayers;
    import net.minecraft.client.renderer.entity.EntityRendererProvider;
    import net.minecraft.client.renderer.entity.MobRenderer;
    import net.minecraft.resources.ResourceLocation;

    public class HematicConstructRenderer extends MobRenderer<HematicConstructEntity, HumanoidModel<HematicConstructEntity>> {

        private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

        public HematicConstructRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
        }

        @Override
        public ResourceLocation getTextureLocation(HematicConstructEntity entity) {
            return TEXTURE;
        }
    }