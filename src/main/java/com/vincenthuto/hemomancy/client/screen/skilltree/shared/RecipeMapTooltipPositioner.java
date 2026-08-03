package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import org.joml.Vector2ic;

final class RecipeMapTooltipPositioner implements ClientTooltipPositioner {
	private CuePosition cuePosition = new CuePosition(0, 0);

	@Override
	public Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY,
			int tooltipWidth, int tooltipHeight) {
		Vector2ic position = DefaultTooltipPositioner.INSTANCE.positionTooltip(
				screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight);
		cuePosition = new CuePosition(position.x() + tooltipWidth - 16,
				position.y() + tooltipHeight - 20);
		return position;
	}

	CuePosition cuePosition() {
		return cuePosition;
	}

	record CuePosition(int x, int y) {
	}
}
