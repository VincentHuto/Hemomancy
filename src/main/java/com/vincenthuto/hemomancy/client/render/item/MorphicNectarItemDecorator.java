package com.vincenthuto.hemomancy.client.render.item;

import com.vincenthuto.hemomancy.common.item.MorphicNectarMutationRules;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public class MorphicNectarItemDecorator implements IItemDecorator {
	@Override
	public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int xOffset, int yOffset) {
		if (!MorphicNectarMutationRules.shouldShowMutation(stack)) {
			return false;
		}

		boolean primal = MorphlingItem.isPrimal(stack);
		long phase = Util.getMillis() / (primal ? MorphicNectarMutationRules.primalTendrilFrameMillis() : 180L);
		if (primal) {
			MorphicNectarTendrilRenderer.renderPrimalOverlay(graphics, xOffset, yOffset, phase);
		} else {
			renderOrganicFrame(graphics, xOffset, yOffset, false, phase);
		}
		return true;
	}

	private static void renderOrganicFrame(GuiGraphics graphics, int x, int y, boolean primal, long phase) {
		int base = MorphicNectarMutationRules.borderColor(primal);
		int flicker = MorphicNectarMutationRules.tendrilColor((int) phase);
		graphics.fill(x, y, x + 16, y + 1, base);
		graphics.fill(x, y + 15, x + 16, y + 16, base);
		graphics.fill(x, y, x + 1, y + 16, base);
		graphics.fill(x + 15, y, x + 16, y + 16, base);

		for (int i = 0; i < (primal ? 3 : 2); i++) {
			int offset = Math.floorMod((int) phase / 2 + i * 6, 14) + 1;
			graphics.fill(x + offset, y, x + offset + 1, y + 2, flicker);
			graphics.fill(x + 15 - offset, y + 14, x + 16 - offset, y + 16,
					MorphicNectarMutationRules.tendrilColor((int) phase + i + 1));
		}
	}
}
