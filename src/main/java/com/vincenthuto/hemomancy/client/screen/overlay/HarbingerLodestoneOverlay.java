package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.item.harbinger.CovenantWaybillItem;
import com.vincenthuto.hemomancy.common.item.harbinger.HarbingerLodestoneGuidance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class HarbingerLodestoneOverlay {
	public static HarbingerLodestoneOverlay instance;

	public void renderHUD(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTicks) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.level == null) return;

		ItemStack lodestone = heldLodestone(minecraft.player.getMainHandItem())
				? minecraft.player.getMainHandItem()
				: minecraft.player.getOffhandItem();
		if (!heldLodestone(lodestone)) return;

		BlockPos target = CovenantWaybillItem.target(lodestone, minecraft.level);
		if (target == null) return;

		double alignment = HarbingerLodestoneGuidance.alignment(
				Mth.rotLerp(partialTicks, minecraft.player.yRotO, minecraft.player.getYRot()),
				target.getX() + 0.5D - minecraft.player.getX(),
				target.getZ() + 0.5D - minecraft.player.getZ());
		if (alignment <= 0.0D) return;

		ManipCooldownOverlay.renderRedVignette(
				graphics, screenWidth, screenHeight, (float) alignment * 0.6F);
	}

	private static boolean heldLodestone(ItemStack stack) {
		return stack.getItem() instanceof CovenantWaybillItem;
	}
}
