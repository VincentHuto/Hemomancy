package com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.OfferingStandBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.OfferingStandBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rests an offering stand's candidate in its dish, turning slowly so it reads from any side: on the stand's lacquer
 * dish, or on the ice skin of Velorum's frozen bowl.
 */
public class OfferingStandRenderer implements BlockEntityRenderer<OfferingStandBlockEntity> {
	/** The dish floor in block pixels (offering_stand.py DISH_TOP). */
	private static final float DISH_TOP = 13f;
	/** The bowl's ice skin in block pixels (offering_stand.py BOWL_DISH). */
	private static final float BOWL_DISH = 4.25f;
	/** The candidate is shown to be read: in an unlit hall it still takes this much block light. */
	private static final int MIN_ITEM_LIGHT = 9;

	private final ItemRenderer itemRenderer;

	public OfferingStandRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(OfferingStandBlockEntity stand, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
			int light, int overlay) {
		ItemStack held = stand.held();
		if (held.isEmpty() || stand.getLevel() == null) return;
		float time = AnimationTime.gameTime(stand.getLevel(), partialTick);
		int above = LevelRenderer.getLightColor(stand.getLevel(), stand.getBlockPos().above());
		above = LightTexture.pack(Math.max(MIN_ITEM_LIGHT, LightTexture.block(above)), LightTexture.sky(above));
		poseStack.pushPose();
		poseStack.translate(0.5, dish(stand) / 16f + 0.12, 0.5);
		poseStack.mulPose(Axis.YP.rotationDegrees(time * 0.8f + stand.getBlockPos().asLong() % 360));
		poseStack.scale(0.55f, 0.55f, 0.55f);
		itemRenderer.renderStatic(held, ItemDisplayContext.GROUND, above, overlay, poseStack, buffers, stand.getLevel(), 0);
		poseStack.popPose();
	}

	private static float dish(OfferingStandBlockEntity stand) {
		BlockState state = stand.getBlockState();
		return state.hasProperty(OfferingStandBlock.STYLE)
				&& state.getValue(OfferingStandBlock.STYLE) == OfferingStandBlock.Style.BOWL ? BOWL_DISH : DISH_TOP;
	}
}
