package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.tile.RadiantPortalRendertype;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipmentItemHandler;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ScarletVanityBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import org.joml.Matrix4f;

public class ScarletVanityRenderer implements BlockEntityRenderer<ScarletVanityBlockEntity> {
	private static final ResourceLocation BLOOD_POOL_TEXTURE = Hemomancy.rloc("textures/block/sanguine_tran_pane.png");
	private static final int FULL_BRIGHT = 15728880;
	private static final int DISPLAY_ITEM_MAX_LIGHT = 11;
	private static final float BLOOD_POOL_RED = 1.0F;
	private static final float BLOOD_POOL_GREEN = 0.06F;
	private static final float BLOOD_POOL_BLUE = 0.035F;
	private static final float BLOOD_POOL_ALPHA = 0.92F;
	private static final float POOL_WAVE_HEIGHT = 0.035f;
	private static final float POOL_WAVE_INTENSITY = 30f;
	private static final float POOL_STEP = 0.125f;

	public ScarletVanityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(ScarletVanityBlockEntity te, float partialTicks, PoseStack poseStack,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		renderBloodBowl(te, partialTicks, poseStack, bufferIn);
		renderPlayerReflection(te, partialTicks, poseStack);
		int itemLight = displayItemLight(te, combinedLightIn);
		renderEquippedItems(te, poseStack, bufferIn, itemLight, combinedOverlayIn);
	}

	private void renderBloodBowl(ScarletVanityBlockEntity te, float partialTicks, PoseStack poseStack,
			MultiBufferSource bufferIn) {
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.89f, 0.5f);
		poseStack.scale(0.14f, 0.14f, 0.14f);
		poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));

		Matrix4f mat = poseStack.last().pose();
		float gameTime = te.getLevel() == null ? 0.0F : te.getLevel().getGameTime();
		float time = (gameTime + partialTicks) / 10.0F;
		VertexConsumer vertex = bufferIn.getBuffer(RadiantPortalRendertype.textWithWaterShader(BLOOD_POOL_TEXTURE));

		for (float x = -1; x < 1; x += POOL_STEP) {
			float u = (x + 1) * 0.5f;
			for (float y = -1; y < 1; y += POOL_STEP) {
				float v = (y + 1) * 0.5f;
				float distance = (float) Math.sqrt(x * x + y * y);

				if (distance <= 1.05f) {
					addWavyVertex(vertex, mat, x, y, time, u, v);
					addWavyVertex(vertex, mat, x, y + POOL_STEP, time, u, v + POOL_STEP * 0.5f);
					addWavyVertex(vertex, mat, x + POOL_STEP, y + POOL_STEP, time,
							u + POOL_STEP * 0.5f, v + POOL_STEP * 0.5f);
					addWavyVertex(vertex, mat, x + POOL_STEP, y, time, u + POOL_STEP * 0.5f, v);

					addWavyVertex(vertex, mat, x + POOL_STEP, y, time, u + POOL_STEP * 0.5f, v);
					addWavyVertex(vertex, mat, x + POOL_STEP, y + POOL_STEP, time,
							u + POOL_STEP * 0.5f, v + POOL_STEP * 0.5f);
					addWavyVertex(vertex, mat, x, y + POOL_STEP, time, u, v + POOL_STEP * 0.5f);
					addWavyVertex(vertex, mat, x, y, time, u, v);
				}
			}
		}

		poseStack.popPose();
	}

	private void renderPlayerReflection(ScarletVanityBlockEntity te, float partialTicks, PoseStack poseStack) {
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		Direction facing = te.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
		float previousYRot = player.getYRot();
		float previousYRotO = player.yRotO;
		float previousBodyRot = player.yBodyRot;
		float previousBodyRotO = player.yBodyRotO;
		float previousHeadRot = player.yHeadRot;
		float previousHeadRotO = player.yHeadRotO;
		EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		MultiBufferSource.BufferSource playerBuffers = Minecraft.getInstance().renderBuffers().bufferSource();

		poseStack.pushPose();
		try {
			poseStack.translate(0.5D, 0.98D, 0.5D);
			poseStack.mulPose(Axis.YP.rotationDegrees(playerReflectionYawForFacing(facing)));
			poseStack.scale(0.1F, 0.1F, 0.1F);

			player.setYRot(0.0F);
			player.yRotO = 0.0F;
			player.yBodyRot = 0.0F;
			player.yBodyRotO = 0.0F;
			player.yHeadRot = 0.0F;
			player.yHeadRotO = 0.0F;
			entityRenderDispatcher.setRenderShadow(false);
			playerBuffers.getBuffer(RenderType.lines());
			entityRenderDispatcher.render(player, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, poseStack, playerBuffers, FULL_BRIGHT);
		} finally {
			entityRenderDispatcher.setRenderShadow(true);
			player.setYRot(previousYRot);
			player.yRotO = previousYRotO;
			player.yBodyRot = previousBodyRot;
			player.yBodyRotO = previousBodyRotO;
			player.yHeadRot = previousHeadRot;
			player.yHeadRotO = previousHeadRotO;
			poseStack.popPose();
		}
	}

	private void renderEquippedItems(ScarletVanityBlockEntity te, PoseStack poseStack, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		HemoCapabilityAccess.getEquipment(player).ifPresent(equipment -> {
			Direction facing = te.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
			poseStack.pushPose();
			poseStack.translate(0.5D, 0.0D, 0.5D);
			poseStack.mulPose(Axis.YP.rotationDegrees(rotationForFacing(facing)));
			poseStack.translate(-0.5D, 0.0D, -0.5D);

			renderEquippedItem(te, facing, equipment, HarbingerEquipmentMenu.GOURD_SLOT_INDEX, poseStack, bufferIn,
					combinedLightIn, combinedOverlayIn, 0.25D, 0.88D, 0.36D, -18.0F, 0.42F);
			renderEquippedItem(te, facing, equipment, HarbingerEquipmentMenu.CHARM_SLOT_INDEX, poseStack, bufferIn,
					combinedLightIn, combinedOverlayIn, 0.72D, 0.885D, 0.36D, 24.0F, 0.34F);
			renderEquippedItem(te, facing, equipment, HarbingerEquipmentMenu.JAR_SLOT_INDEX, poseStack, bufferIn,
					combinedLightIn, combinedOverlayIn, 0.74D, 0.89D, 0.70D, -36.0F, 0.36F);
			renderEquippedItem(te, facing, equipment, HarbingerEquipmentMenu.FITTING_SLOT_INDEX, poseStack, bufferIn,
					combinedLightIn, combinedOverlayIn, 0.28D, 0.89D, 0.70D, 36.0F, 0.30F);

			poseStack.popPose();
		});
	}

	private static void renderEquippedItem(ScarletVanityBlockEntity te, Direction facing, IHarbingerEquipmentItemHandler equipment, int slot,
                                           PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn,
                                           double x, double y, double z, float yaw, float scale) {
		ItemStack stack = equipment.getStackInSlot(slot);
		if (stack.isEmpty()) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(x, y, z);
		poseStack.mulPose(Axis.YP.rotationDegrees(itemYawForFacing(facing, yaw)));
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.scale(scale, scale, scale);
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, combinedLightIn,
				combinedOverlayIn, poseStack, bufferIn, te.getLevel(), 0);
		poseStack.popPose();
	}

	private static float rotationForFacing(Direction facing) {
		return switch (facing) {
		case NORTH -> 180.0F;
		case EAST -> 90.0F;
		case WEST -> 270.0F;
		default -> 0.0F;
		};
	}

	private static float itemYawForFacing(Direction facing, float yaw) {
		return switch (facing) {
		case NORTH, SOUTH -> -yaw;
		default -> yaw;
		};
	}

	private static float playerReflectionYawForFacing(Direction facing) {
		return switch (facing) {
		case NORTH -> 180.0F;
		case SOUTH -> 0.0F;
		case EAST -> 90.0F;
		case WEST -> 270.0F;
		default -> 0.0F;
		};
	}

	private static void addWavyVertex(VertexConsumer vertex, Matrix4f mat, float x, float y, float time, float u,
			float v) {
		float distance = (float) Math.sqrt(x * x + y * y);
		float wave = (float) Math.sin(distance * POOL_WAVE_INTENSITY + time) * POOL_WAVE_HEIGHT;
		vertex.addVertex(mat, x, y, wave).setColor(BLOOD_POOL_RED, BLOOD_POOL_GREEN, BLOOD_POOL_BLUE, BLOOD_POOL_ALPHA).setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT);
	}

	private static int displayItemLight(ScarletVanityBlockEntity te, int fallbackLight) {
		if (te.getLevel() == null) {
			return capPackedLight(fallbackLight, DISPLAY_ITEM_MAX_LIGHT);
		}
		int tableLight = LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above());
		return capPackedLight(tableLight, DISPLAY_ITEM_MAX_LIGHT);
	}

	private static int capPackedLight(int packedLight, int maxLight) {
		int blockLight = (packedLight >> 4) & 0xF;
		int skyLight = (packedLight >> 20) & 0xF;
		return (Math.min(blockLight, maxLight) << 4) | (Math.min(skyLight, maxLight) << 20);
	}
}
