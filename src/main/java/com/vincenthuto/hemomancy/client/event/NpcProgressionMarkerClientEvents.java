package com.vincenthuto.hemomancy.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.NpcProgressionMarkerClientState;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class NpcProgressionMarkerClientEvents {
	private static final ResourceLocation HARBINGER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
			"hemomancy", "textures/entity/npc/progression_sigil.png");
	private static final ResourceLocation UNSTAINED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
			"hemomancy", "textures/entity/npc/unstained_progression_sigil.png");

	private NpcProgressionMarkerClientEvents() {
	}

	@SubscribeEvent
	public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
		var entity = event.getEntity();
		DialogueAttention attention = NpcProgressionMarkerClientState.attention(entity.getId());
		if (attention == DialogueAttention.NONE || entity.isInvisible()) return;

		float time = entity.tickCount + event.getPartialTick();
		float speed = attention == DialogueAttention.URGENT ? 0.22F : 0.10F;
		float pulse = 0.5F + 0.5F * (float) Math.sin(time * speed);
		float size = (attention == DialogueAttention.URGENT ? 0.34F : 0.29F) + pulse * 0.035F;
		float alpha = (attention == DialogueAttention.URGENT ? 0.82F : 0.62F) + pulse * 0.18F;

		PoseStack pose = event.getPoseStack();
		pose.pushPose();
		pose.translate(0.0F, entity.getBbHeight() + 0.48F + pulse * 0.04F, 0.0F);
		pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
		pose.scale(-size, -size, size);

		ResourceLocation entityType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		VertexConsumer vertices = event.getMultiBufferSource()
				.getBuffer(RenderType.entityTranslucent(textureFor(entityType)));
		renderQuad(vertices, pose.last(), alpha);
		pose.popPose();
	}

	static ResourceLocation textureFor(ResourceLocation entityType) {
		return entityType.getPath().startsWith("unstained_") ? UNSTAINED_TEXTURE : HARBINGER_TEXTURE;
	}

	static void renderQuad(VertexConsumer vertices, PoseStack.Pose pose, float alpha) {
		addVertex(vertices, pose, -0.5F, -0.5F, 0.0F, 1.0F, alpha);
		addVertex(vertices, pose, 0.5F, -0.5F, 1.0F, 1.0F, alpha);
		addVertex(vertices, pose, 0.5F, 0.5F, 1.0F, 0.0F, alpha);
		addVertex(vertices, pose, -0.5F, 0.5F, 0.0F, 0.0F, alpha);
	}

	private static void addVertex(VertexConsumer vertices, PoseStack.Pose pose, float x, float y, float u, float v,
			float alpha) {
		vertices.addVertex(pose.pose(), x, y, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
				.setNormal(pose, 0.0F, 0.0F, 1.0F);
	}
}
