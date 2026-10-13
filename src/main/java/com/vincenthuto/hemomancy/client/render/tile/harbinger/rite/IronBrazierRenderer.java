package com.vincenthuto.hemomancy.client.render.tile.harbinger.rite;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.world.BrazierFlames;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBloodInteractionRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class IronBrazierRenderer implements BlockEntityRenderer<IronBrazierBlockEntity> {
	private final ItemRenderer itemRenderer;

	public IronBrazierRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(IronBrazierBlockEntity te, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
		long gameTime = te.getLevel() != null ? te.getLevel().getGameTime() : 0L;
		float time = gameTime + partialTicks;
		BlockState state = te.getBlockState();
		int phase = state.hasProperty(BrazierBlock.RITUAL_PHASE) ? state.getValue(BrazierBlock.RITUAL_PHASE) : 0;
		float target = BrazierBloodInteractionRules.poolFraction(phase > 0, te.getPooledBlood(),
				BrazierBlock.BLOOD_TO_LIGHT);
		float fill = te.shownPool(target, time);
		if ((phase > 0 || fill > 0.002F) && te.getLevel() != null) {
			// Blood running in or out leads the drawn level; that lag is how hard the surface ripples.
			float pour = Mth.clamp(Math.abs(target - fill) * 8.0F, 0.0F, 1.0F);
			BrazierFlames.render(poseStack, buffer, te.getBlockPos(), phase > 0, phase == 2, fill, pour, time);
		}

		ItemStack displayed = te.getOfferingDisplayStack();
		if (displayed.isEmpty()) {
			return;
		}

		float bob = Mth.sin(time * 0.08F) * 0.035F;
		float spin = (time * 2.0F) % 360.0F;

		poseStack.pushPose();
		poseStack.translate(0.5F, 1.05F + bob, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(spin));
		poseStack.mulPose(Axis.XP.rotationDegrees(12.0F));
		poseStack.scale(0.5F, 0.5F, 0.5F);
		itemRenderer.renderStatic(null, displayed, ItemDisplayContext.FIXED, false,
				poseStack, buffer, te.getLevel(), combinedLight, combinedOverlay, 0);
		poseStack.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(IronBrazierBlockEntity te) {
		// The flame tongues and their licks rise well above the talons.
		return new AABB(te.getBlockPos()).expandTowards(0.0D, 1.0D, 0.0D);
	}
}
