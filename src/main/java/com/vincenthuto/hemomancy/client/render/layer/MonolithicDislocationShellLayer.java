package com.vincenthuto.hemomancy.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.armor.SilentArchonArmorRenderHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.util.HashSet;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class MonolithicDislocationShellLayer<T extends LivingEntity, M extends EntityModel<T>>
		extends RenderLayer<T, M> {
	private static final float SHELL_SCALE = 1.035F;
	private static final int SHELL_LIGHT = 0x00F000F0;
	private static final int SHELL_COLOR = 0x55FFFFFF;
	private static final Set<Integer> LAYER_RENDERED_ENTITIES = new HashSet<>();

	public MonolithicDislocationShellLayer(LivingEntityRenderer<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
			float netHeadYaw, float headPitch) {
		if (entity.isInvisible() || !entity.hasEffect(EffectInit.monolithic_dislocation)) {
			return;
		}
		LAYER_RENDERED_ENTITIES.add(entity.getId());

		float seed = Math.floorMod(entity.getUUID().hashCode(), 10007) / 10007.0F;
		VertexConsumer consumer = buffer.getBuffer(HemoRenderTypes.monolithicDislocationShell(
				SilentArchonArmorRenderHelper.timeSeconds(), seed));
		poseStack.pushPose();
		poseStack.scale(SHELL_SCALE, SHELL_SCALE, SHELL_SCALE);
		this.getParentModel().renderToBuffer(poseStack, consumer, SHELL_LIGHT,
				OverlayTexture.NO_OVERLAY, SHELL_COLOR);
		poseStack.popPose();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void renderFallback(RenderLivingEvent.Post event) {
		LivingEntity entity = event.getEntity();
		if (LAYER_RENDERED_ENTITIES.remove(entity.getId())
				|| entity.isInvisible()
				|| !entity.hasEffect(EffectInit.monolithic_dislocation)) {
			return;
		}

		float seed = Math.floorMod(entity.getUUID().hashCode(), 10007) / 10007.0F;
		VertexConsumer consumer = event.getMultiBufferSource().getBuffer(
				HemoRenderTypes.monolithicDislocationShell(SilentArchonArmorRenderHelper.timeSeconds(), seed));
		PoseStack poseStack = event.getPoseStack();
		poseStack.pushPose();
		poseStack.scale(SHELL_SCALE, SHELL_SCALE, SHELL_SCALE);
		event.getRenderer().getModel().renderToBuffer(poseStack, consumer, SHELL_LIGHT,
				OverlayTexture.NO_OVERLAY, SHELL_COLOR);
		poseStack.popPose();
	}
}
