package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.config.HemoClientConfig;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * HUD overlay that displays the player's currently equipped morphling
 * beside the blood volume bar.
 */
public class EquippedMorphlingOverlay {

	public static EquippedMorphlingOverlay instance;

	public void renderForBloodBar(GuiGraphics gfx, ItemStack equipped, boolean barOnLeft,
			int barX, int barY, int barWidth, int barHeight, float time) {
		if (equipped.isEmpty() || HemoClientConfig.MORPHLING_HUD_MODE.get() == HemoClientConfig.MorphlingHudMode.OFF) {
			return;
		}

		if (HemoClientConfig.MORPHLING_HUD_MODE.get() == HemoClientConfig.MorphlingHudMode.LEGACY_ICON) {
			renderLegacyIcon(gfx, equipped, barOnLeft, barX, barY, barWidth, barHeight);
			renderBondMeter(gfx, equipped,
					EquippedMorphlingOverlayPlacement.iconXForBloodBar(barOnLeft, barX, barWidth),
					EquippedMorphlingOverlayPlacement.iconYForBloodBar(barY, barHeight) + 18);
			return;
		}

		String itemPath = BuiltInRegistries.ITEM.getKey(equipped.getItem()).getPath();
		MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath(itemPath);
		if (visual == null) {
			renderLegacyIcon(gfx, equipped, barOnLeft, barX, barY, barWidth, barHeight);
			return;
		}
		int xOffset = barOnLeft  ? -25 :25;
		int x = EquippedMorphlingOverlayPlacement.attachedXForBloodBar(barOnLeft, barX, barWidth)+xOffset;
		int y = EquippedMorphlingOverlayPlacement.attachedYForBloodBar(barY, barHeight);
		boolean primal = MorphlingItem.isPrimal(equipped);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		renderSprite(gfx, visual, x, y, EquippedMorphlingOverlayPlacement.shouldMirror(barOnLeft));
		if (primal) {
			renderPrimalMotes(gfx, x, y, time, visual.accentColor());
		}
		renderBondMeter(gfx, equipped, x + 8, y + EquippedMorphlingOverlayPlacement.ATTACHED_SIZE - 5);
		RenderSystem.disableBlend();
	}

	private void renderBondMeter(GuiGraphics gfx, ItemStack equipped, int x, int y) {
		double required = MorphlingItem.requiredBondingBlood(equipped);
		if (!MorphlingItem.isPassiveUpkeepEnabled() || required <= 0.0D
				|| MorphlingItem.getMaturityLevel(equipped) >= 4) {
			return;
		}
		double absorbed = Math.min(required, MorphlingItem.getBondingBlood(equipped));
		int width = 32;
		int filled = (int) Math.round(width * absorbed / required);
		gfx.fill(x - 1, y - 1, x + width + 1, y + 4, 0xD0100305);
		gfx.fill(x, y, x + width, y + 3, 0xC030080C);
		gfx.fill(x, y, x + filled, y + 3, 0xE0B51E2B);
		String text = String.format("%.0f/%.0f", absorbed, required);
		gfx.drawString(Minecraft.getInstance().font, text, x + (width - Minecraft.getInstance().font.width(text)) / 2,
				y + 5, 0xFFE7B8B8, true);
	}

	private void renderLegacyIcon(GuiGraphics gfx, ItemStack equipped, boolean barOnLeft,
			int barX, int barY, int barWidth, int barHeight) {
		int x = EquippedMorphlingOverlayPlacement.iconXForBloodBar(barOnLeft, barX, barWidth);
		int y = EquippedMorphlingOverlayPlacement.iconYForBloodBar(barY, barHeight);
		gfx.renderItem(equipped, x, y);
	}

	private void renderSprite(GuiGraphics gfx, MorphlingHudVisuals.Visual visual, int x, int y, boolean mirror) {
		ResourceLocation texture = Hemomancy.rloc("textures/gui/morphling_overlay/" + visual.textureName() + ".png");
		EquippedMorphlingOverlayPlacement.SpriteBlit blit = EquippedMorphlingOverlayPlacement.spriteBlit(mirror);
		gfx.blit(texture, x, y, blit.width(), EquippedMorphlingOverlayPlacement.ATTACHED_SIZE,
				blit.uOffset(), 0, blit.uWidth(), EquippedMorphlingOverlayPlacement.ATTACHED_SIZE,
				EquippedMorphlingOverlayPlacement.ATTACHED_SIZE, EquippedMorphlingOverlayPlacement.ATTACHED_SIZE);
	}

	private void renderPrimalMotes(GuiGraphics gfx, int x, int y, float time, int color) {
		for (int i = 0; i < 4; i++) {
			float phase = time * 1.7f + i * 1.57f;
			int moteX = x + 25 + Math.round(Mth.cos(phase) * (18 + (i & 1) * 3));
			int moteY = y + 24 + Math.round(Mth.sin(phase * 1.23f) * 20);
			gfx.fill(moteX, moteY, moteX + 2, moteY + 2, color);
		}
	}

}
