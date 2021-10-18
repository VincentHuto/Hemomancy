package com.vincenthuto.hemomancy.render.entity.blood;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.blood.EntityWretchedWill;
import com.vincenthuto.hemomancy.init.ModelLayersInit;
import com.vincenthuto.hemomancy.model.entity.ModelWretchedWill;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderWretchedWill extends MobRenderer<EntityWretchedWill, ModelWretchedWill<EntityWretchedWill>> {

	public static final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/wretched_will/modelwretchedwill.png");

	public RenderWretchedWill(EntityRendererProvider.Context p_174447_) {
		super(p_174447_, new ModelWretchedWill<EntityWretchedWill>(p_174447_.bakeLayer(ModelLayersInit.wretched_will)),
				0F);

	}

	@Override
	public void render(EntityWretchedWill ent, float p_115456_, float p_115457_, PoseStack ms, MultiBufferSource buffer,
			int p_115460_) {
		super.render(ent, p_115456_, p_115457_, ms, buffer, p_115460_);
	//	System.out.println(ent.creator);
		
//		if (ent.creator != null) {
//			ms.pushPose();
//			EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(ent.creator);
//			EntityModel<?> model = ((RenderLayerParent<?, ?>) renderer).getModel();
//			if (model instanceof HumanoidModel<?>) {
//				HumanoidModel<?> biModel = (HumanoidModel<?>) model;
//				biModel.head.translateAndRotate(ms);
//			}
//			ms.popPose();
//		}
	}

	@Nonnull
	@Override
	public ResourceLocation getTextureLocation(@Nonnull EntityWretchedWill entity) {
		return texture;
	}

}