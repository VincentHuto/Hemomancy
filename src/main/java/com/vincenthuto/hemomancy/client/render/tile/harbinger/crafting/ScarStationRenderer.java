package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ScarStationBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Lays the station's slots out on the baked model from tools/model_export/scar_station.py: the blank
 * on the scar grid, the catalyst in its cup and the knapper across its rest (all pitched with the
 * carving bed), and the pattern and finished scar pinned to the backboard.
 */
public class ScarStationRenderer implements BlockEntityRenderer<ScarStationBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final int DISPLAY_ITEM_MAX_LIGHT = 11;
	private static final float PX = 1.0F / 16.0F;

	// Anchors shared with tools/model_export/scar_station.py (model pixels, north-facing).
	private static final float BED_ORIGIN_Y = 11.0F;
	private static final float BED_ORIGIN_Z = 2.5F;
	private static final float BED_PITCH = -22.5F;
	private static final float BED_TOP = 12.0F;
	private static final float GRID_X = 9.5F;
	private static final float GRID_Z = 7.8F;
	private static final float CUP_X = 3.7F;
	private static final float CUP_Z = 9.0F;
	private static final float REST_X = 3.7F;
	private static final float REST_Z = 5.0F;
	private static final float BOARD_FRONT = 13.4F;
	private static final float PATTERN_X = 5.3F;
	private static final float OUTPUT_X = 10.7F;
	private static final float PIN_Y = 20.0F;

	/** Generated items' ground display lifts them 2px along their own up axis; undone so anchors are centres. */
	private static final float GROUND_LIFT = 2.0F * PX;

	public ScarStationRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public boolean shouldRenderOffScreen(ScarStationBlockEntity te) {
		return true;
	}

	@Override
	public void render(ScarStationBlockEntity te, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		int itemLight = displayItemLight(te, combinedLight);

		poseStack.pushPose();
		poseStack.translate(0.5F, 0.0F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(rotationForFacing(te.getBlockState().getValue(FACING))));
		poseStack.translate(-0.5F, 0.0F, -0.5F);

		poseStack.pushPose();
		poseStack.translate(0.0F, BED_ORIGIN_Y * PX, BED_ORIGIN_Z * PX);
		poseStack.mulPose(Axis.XP.rotationDegrees(BED_PITCH));
		poseStack.translate(0.0F, -BED_ORIGIN_Y * PX, -BED_ORIGIN_Z * PX);
		renderLying(te, te.contents.get(0), GRID_X, BED_TOP + 0.6F, GRID_Z, 0.0F, 0.78F, poseStack, buffer, itemLight,
				combinedOverlay);
		renderLying(te, te.contents.get(3), REST_X, BED_TOP + 1.15F, REST_Z, 45.0F, 0.7F, poseStack, buffer, itemLight,
				combinedOverlay);
		renderUpright(te, te.contents.get(1), CUP_X, BED_TOP + 2.4F, CUP_Z, 0.5F, poseStack, buffer, itemLight,
				combinedOverlay);
		poseStack.popPose();

		renderUpright(te, te.contents.get(4), PATTERN_X, PIN_Y, BOARD_FRONT - 0.5F, 0.6F, poseStack, buffer, itemLight,
				combinedOverlay);
		renderUpright(te, te.contents.get(2), OUTPUT_X, PIN_Y, BOARD_FRONT - 0.5F, 0.6F, poseStack, buffer, itemLight,
				combinedOverlay);
		poseStack.popPose();
	}

	/** Flat on its back, front up, its top edge pointing away from the mason; {@code yaw} turns it on the surface. */
	private static void renderLying(ScarStationBlockEntity te, ItemStack stack, float x, float y, float z, float yaw,
			float scale, PoseStack poseStack, MultiBufferSource buffer, int itemLight, int overlay) {
		if (stack.isEmpty()) {
			return;
		}
		poseStack.pushPose();
		poseStack.translate(x * PX, y * PX, z * PX);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F + yaw));
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		renderItem(te, stack, scale, poseStack, buffer, itemLight, overlay);
		poseStack.popPose();
	}

	/** Standing, facing the mason. */
	private static void renderUpright(ScarStationBlockEntity te, ItemStack stack, float x, float y, float z, float scale,
			PoseStack poseStack, MultiBufferSource buffer, int itemLight, int overlay) {
		if (stack.isEmpty()) {
			return;
		}
		poseStack.pushPose();
		poseStack.translate(x * PX, y * PX, z * PX);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		renderItem(te, stack, scale, poseStack, buffer, itemLight, overlay);
		poseStack.popPose();
	}

	private static void renderItem(ScarStationBlockEntity te, ItemStack stack, float scale, PoseStack poseStack,
			MultiBufferSource buffer, int itemLight, int overlay) {
		poseStack.scale(scale, scale, scale);
		poseStack.translate(0.0F, -GROUND_LIFT, 0.0F);
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, itemLight, overlay,
				poseStack, buffer, te.getLevel(), 0);
	}

	/** The model's turn for each facing, matching the blockstate (north y0, east y90, south y180, west y270). */
	private static float rotationForFacing(Direction facing) {
		return switch (facing) {
			case SOUTH -> 180.0F;
			case WEST -> 90.0F;
			case EAST -> -90.0F;
			default -> 0.0F;
		};
	}

	private static int displayItemLight(ScarStationBlockEntity te, int fallbackLight) {
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
