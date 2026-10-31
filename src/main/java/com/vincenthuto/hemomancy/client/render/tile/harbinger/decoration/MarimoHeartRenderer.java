package com.vincenthuto.hemomancy.client.render.tile.harbinger.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.MarimoHeartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;

/**
 * Marimo's Heart's pulse: two additive shells of the heart's own moss round the block, so its gold filaments and
 * nodes glow out of the grey. The inner one breathes
 * slowly all the time; both swell and brighten when the heart pulses for an arrival or a touch, the outer one far out
 * and faint, as the pulse spreads into the mass.
 */
public class MarimoHeartRenderer implements BlockEntityRenderer<MarimoHeartBlockEntity> {
	/** Additive: the dark moss adds almost nothing, the gold filaments and nodes add light. */
	private static final RenderType GLOW = RenderType.eyes(InventoryMenu.BLOCK_ATLAS);
	private static final int GOLD_R = 255, GOLD_G = 214, GOLD_B = 130;

	public MarimoHeartRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(MarimoHeartBlockEntity heart, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
			int light, int overlay) {
		if (heart.getLevel() == null) return;
		double time = AnimationTime.gameTime(heart.getLevel(), (double) partialTick);
		float breath = 0.5F + 0.5F * (float) Math.sin(time * 0.05D);
		float pulse = heart.pulse(time);
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
				.apply(Hemomancy.rloc("block/marimo_heart"));
		VertexConsumer vertices = buffers.getBuffer(GLOW);
		shell(poseStack, vertices, sprite, 0.035F + 0.02F * breath + 0.18F * pulse, 0.22F + 0.12F * breath + 0.45F * pulse);
		if (pulse > 0.01F) shell(poseStack, vertices, sprite, 0.25F + 0.9F * pulse, 0.35F * pulse * (1.0F - pulse * 0.4F));
	}

	private static void shell(PoseStack poseStack, VertexConsumer vertices, TextureAtlasSprite sprite, float grow, float alpha) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.5F, 0.5F);
		float s = 0.5F + grow;
		PoseStack.Pose pose = poseStack.last();
		float k = Math.min(1.0F, alpha);
		int r = (int) (GOLD_R * k), g = (int) (GOLD_G * k), b = (int) (GOLD_B * k);
		for (Direction face : Direction.values()) {
			float nx = face.getStepX(), ny = face.getStepY(), nz = face.getStepZ();
			float[][] corners = corners(face, s);
			float[][] uvs = { { sprite.getU0(), sprite.getV1() }, { sprite.getU1(), sprite.getV1() },
					{ sprite.getU1(), sprite.getV0() }, { sprite.getU0(), sprite.getV0() } };
			for (int i = 0; i < 4; i++) {
				vertices.addVertex(pose, corners[i][0], corners[i][1], corners[i][2]).setColor(r, g, b, 255)
						.setUv(uvs[i][0], uvs[i][1]).setOverlay(OverlayTexture.NO_OVERLAY)
						.setLight(LightTexture.FULL_BRIGHT).setNormal(pose, nx, ny, nz);
			}
		}
		poseStack.popPose();
	}

	/** A cube face of half-size {@code s} round the origin, wound counter-clockwise seen from outside. */
	private static float[][] corners(Direction face, float s) {
		return switch (face) {
			case UP -> new float[][] { { -s, s, s }, { s, s, s }, { s, s, -s }, { -s, s, -s } };
			case DOWN -> new float[][] { { -s, -s, -s }, { s, -s, -s }, { s, -s, s }, { -s, -s, s } };
			case NORTH -> new float[][] { { s, -s, -s }, { -s, -s, -s }, { -s, s, -s }, { s, s, -s } };
			case SOUTH -> new float[][] { { -s, -s, s }, { s, -s, s }, { s, s, s }, { -s, s, s } };
			case WEST -> new float[][] { { -s, -s, -s }, { -s, -s, s }, { -s, s, s }, { -s, s, -s } };
			case EAST -> new float[][] { { s, -s, s }, { s, -s, -s }, { s, s, -s }, { s, s, s } };
		};
	}

	@Override
	public AABB getRenderBoundingBox(MarimoHeartBlockEntity heart) {
		return new AABB(heart.getBlockPos()).inflate(1.5D);
	}
}
