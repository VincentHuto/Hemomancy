package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.VanityMirrorRules;
import com.vincenthuto.hemomancy.client.render.world.VanityMirrorView;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipmentItemHandler;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ScarletVanityBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;

public class ScarletVanityRenderer implements BlockEntityRenderer<ScarletVanityBlockEntity> {
	private static final int DISPLAY_ITEM_MAX_LIGHT = 11;

	public ScarletVanityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public AABB getRenderBoundingBox(ScarletVanityBlockEntity te) {
		// The mirror stands in the block above.
		return new AABB(te.getBlockPos()).expandTowards(0.0D, 1.0D, 0.0D);
	}

	@Override
	public void render(ScarletVanityBlockEntity te, float partialTicks, PoseStack poseStack,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		renderMirrorGlass(te, partialTicks, poseStack, bufferIn);
		int itemLight = displayItemLight(te, combinedLightIn);
		renderEquippedItems(te, poseStack, bufferIn, itemLight, combinedOverlayIn);
	}

	/**
	 * The mirror's glass, one quad the shader cuts to the frame's arch: smoke, with the players in front of it drawn in
	 * by {@link VanityMirrorView}.
	 */
	private static void renderMirrorGlass(ScarletVanityBlockEntity te, float partialTicks, PoseStack poseStack,
			MultiBufferSource bufferIn) {
		if (te.getLevel() == null) {
			return;
		}
		Direction facing = te.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
		VanityMirrorView.request(te.getBlockPos(), facing);
		int live = VanityMirrorView.textureFor(te.getBlockPos());
		int light = LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above());
		VertexConsumer glass = bufferIn.getBuffer(
				HemoRenderTypes.vanityMirror(AnimationTime.gameTime(te.getLevel(), partialTicks), live));

		poseStack.pushPose();
		// The pose faces the vanity's front, north, along -z, as its model is authored.
		poseStack.translate(0.5D, 0.0D, 0.5D);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
		poseStack.translate(-0.5D, 0.0D, -0.5D);
		PoseStack.Pose pose = poseStack.last();
		float x0 = VanityMirrorRules.GLASS_X0 / 16.0F;
		float x1 = VanityMirrorRules.GLASS_X1 / 16.0F;
		float y0 = VanityMirrorRules.GLASS_Y0 / 16.0F;
		float y1 = VanityMirrorRules.GLASS_Y1 / 16.0F;
		float z = VanityMirrorRules.GLASS_Z / 16.0F;
		glassVertex(glass, pose, x0, y0, z, VanityMirrorRules.GLASS_X0, VanityMirrorRules.GLASS_Y0, light);
		glassVertex(glass, pose, x0, y1, z, VanityMirrorRules.GLASS_X0, VanityMirrorRules.GLASS_Y1, light);
		glassVertex(glass, pose, x1, y1, z, VanityMirrorRules.GLASS_X1, VanityMirrorRules.GLASS_Y1, light);
		glassVertex(glass, pose, x1, y0, z, VanityMirrorRules.GLASS_X1, VanityMirrorRules.GLASS_Y0, light);
		poseStack.popPose();
	}

	private static void glassVertex(VertexConsumer glass, PoseStack.Pose pose, float x, float y, float z, float u, float v,
			int light) {
		glass.addVertex(pose.pose(), x, y, z).setColor(-1).setUv(u, v).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
	}

	private void renderEquippedItems(ScarletVanityBlockEntity te, PoseStack poseStack, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		var equipment = HemoCapabilityAccess.getEquipment(player);
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
