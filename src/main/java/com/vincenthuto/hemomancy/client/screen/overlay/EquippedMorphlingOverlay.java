package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/** The equipped Morphling, kept inside the hematic HUD's jar, with its bond meter beside it. */
public class EquippedMorphlingOverlay {

	public static EquippedMorphlingOverlay instance;

	static boolean shown(ItemStack equipped) {
		return !equipped.isEmpty() && HemoClientConfig.MORPHLING_HUD_MODE.get() != HemoClientConfig.MorphlingHudMode.OFF;
	}

	/** Depth the jar's front glass needs to cover an item drawn by {@code renderItem}, which sits at 150 and up to 16 past. */
	public static final float ITEM_GLASS_Z = 200.0f;

	/**
	 * Draws the Morphling centred on ({@code centreX}, {@code centreY}) in a jar window {@code size} wide. Returns
	 * whether it was drawn as an item, whose depth the front glass must be lifted over.
	 */
	public boolean renderInJar(GuiGraphics gfx, ItemStack equipped, float centreX, float centreY, float size,
			boolean mirror, float time) {
		if (!shown(equipped)) return false;
		String itemPath = BuiltInRegistries.ITEM.getKey(equipped.getItem()).getPath();
		MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath(itemPath);
		gfx.pose().pushPose();
		gfx.pose().translate(centreX, centreY, 0.0f);
		if (HemoClientConfig.MORPHLING_HUD_MODE.get() == HemoClientConfig.MorphlingHudMode.LEGACY_ICON || visual == null) {
			float scale = size / EquippedMorphlingOverlayPlacement.ICON_SIZE;
			gfx.pose().scale(scale, scale, 1.0f);
			gfx.renderItem(equipped, -8, -8);
			gfx.pose().popPose();
			return true;
		}
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		ResourceLocation texture = Hemomancy.rloc("textures/gui/morphling_overlay/" + visual.textureName() + ".png");
		EquippedMorphlingOverlayPlacement.SpriteBlit blit = EquippedMorphlingOverlayPlacement.spriteBlit(mirror);
		float scale = EquippedMorphlingOverlayPlacement.morphlingRenderScale(EquippedMorphlingOverlayPlacement.jarScale(size), time);
		gfx.pose().scale(scale, scale, 1.0f);
		int half = EquippedMorphlingOverlayPlacement.ATTACHED_SIZE / 2;
		gfx.blit(texture, -half, -half, blit.width(), EquippedMorphlingOverlayPlacement.ATTACHED_SIZE,
				blit.uOffset(), 0, blit.uWidth(), EquippedMorphlingOverlayPlacement.ATTACHED_SIZE,
				EquippedMorphlingOverlayPlacement.ATTACHED_SIZE, EquippedMorphlingOverlayPlacement.ATTACHED_SIZE);
		gfx.pose().popPose();
		if (MorphlingItem.isPrimal(equipped)) renderPrimalMotes(gfx, centreX, centreY, size, time, visual.accentColor());
		return false;
	}

	/**
	 * The bond meter hangs from ({@code meterX}, {@code meterY}): its left edge, or its right edge when
	 * {@code meterLeftOfAnchor}. The distracted note sits at ({@code statusX}, {@code statusY}).
	 */
	public void renderReadouts(GuiGraphics gfx, ItemStack equipped, int meterX, int meterY, boolean meterLeftOfAnchor,
			int statusX, int statusY, boolean statusRightAligned) {
		if (!shown(equipped)) return;
		renderBondMeter(gfx, equipped, meterLeftOfAnchor ? meterX - 32 : meterX, meterY);
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || !BoundSummonBehavior.hasEquippedMorphling(mc.player)
				|| MarionetteCrossbarItem.activeSummonsForOwner(mc.player).isEmpty()) return;
		Component text = Component.translatable("morphling.interference.distracted");
		int x = statusRightAligned ? statusX - mc.font.width(text) : statusX;
		gfx.drawString(mc.font, text, x, statusY, 0xFFD88A96, true);
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
		gfx.fill(x - 1, y - 1, x + width + 1, y +4, 0xD0100305);
		gfx.fill(x, y, x + width, y + 3, 0xC030080C);
		gfx.fill(x, y, x + filled, y + 3, 0xE0B51E2B);
		String text = String.format("%.0f/%.0f", absorbed, required);
		gfx.drawString(Minecraft.getInstance().font, text, x + (width - Minecraft.getInstance().font.width(text)) / 2,
				y + 5, 0xFFE7B8B8, true);
	}

	private void renderPrimalMotes(GuiGraphics gfx, float centreX, float centreY, float size, float time, int color) {
		for (int i = 0; i < 4; i++) {
			float phase = time * 1.7f + i * 1.57f;
			int moteX = Math.round(centreX + size * Mth.cos(phase) * 0.4f);
			int moteY = Math.round(centreY + size * Mth.sin(phase * 1.23f) * 0.4f);
			gfx.fill(moteX, moteY, moteX + 1, moteY + 1, color);
		}
	}

}
