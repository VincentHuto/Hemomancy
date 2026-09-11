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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

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
					EquippedMorphlingOverlayPlacement.iconYForBloodBar(barY, barHeight) + 18 - 62);
			renderDistractedStatus(gfx, barOnLeft, barX, barY, barWidth);
			return;
		}

		String itemPath = BuiltInRegistries.ITEM.getKey(equipped.getItem()).getPath();
		MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath(itemPath);
		if (visual == null) {
			renderLegacyIcon(gfx, equipped, barOnLeft, barX, barY, barWidth, barHeight);
			renderDistractedStatus(gfx, barOnLeft, barX, barY, barWidth);
			return;
		}
		var attachment = EquippedMorphlingOverlayPlacement.attachment(barOnLeft,
				BloodVolumeOverlay.getVesselEdgeX(barOnLeft, barX),
				BloodVolumeOverlay.getVesselCenterY(barY), HemoClientConfig.MORPHLING_HUD_SCALE.get().floatValue());
		boolean primal = MorphlingItem.isPrimal(equipped);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		renderSprite(gfx, visual, attachment, EquippedMorphlingOverlayPlacement.shouldMirror(barOnLeft), time);
		if (primal) {
			renderPrimalMotes(gfx, attachment, time, visual.accentColor());
		}
		// At large scales near the screen top, put the meter beside the body.
		renderBondMeter(gfx, equipped, attachment.bondMeterX(barOnLeft), attachment.bondMeterY());
		renderDistractedStatus(gfx, barOnLeft, barX, barY, barWidth);
		RenderSystem.disableBlend();
	}

	private void renderDistractedStatus(GuiGraphics gfx, boolean barOnLeft, int barX, int barY, int barWidth) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || !BoundSummonBehavior.hasEquippedMorphling(mc.player)
				|| MarionetteCrossbarItem.activeSummonsForOwner(mc.player).isEmpty()) return;
		String text = net.minecraft.network.chat.Component.translatable("morphling.interference.distracted").getString();
		int x = barOnLeft ? barX : barX + barWidth - mc.font.width(text);
		gfx.drawString(mc.font, text, x, barY - 12, 0xFFD88A96, true);
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

	private void renderLegacyIcon(GuiGraphics gfx, ItemStack equipped, boolean barOnLeft,
			int barX, int barY, int barWidth, int barHeight) {
		int x = EquippedMorphlingOverlayPlacement.iconXForBloodBar(barOnLeft, barX, barWidth);
		int y = EquippedMorphlingOverlayPlacement.iconYForBloodBar(barY, barHeight);
		gfx.renderItem(equipped, x, y);
	}

	private void renderSprite(GuiGraphics gfx, MorphlingHudVisuals.Visual visual,
			EquippedMorphlingOverlayPlacement.Attachment attachment, boolean mirror, float time) {
		ResourceLocation texture = Hemomancy.rloc("textures/gui/morphling_overlay/" + visual.textureName() + ".png");
		EquippedMorphlingOverlayPlacement.SpriteBlit blit = EquippedMorphlingOverlayPlacement.spriteBlit(mirror);
		gfx.pose().pushPose();
		gfx.pose().translate(attachment.anchorX(), attachment.anchorY(), 0.0f);
		float scale = EquippedMorphlingOverlayPlacement.morphlingRenderScale(attachment.scale(), time);
		gfx.pose().scale(scale, scale, 1.0f);
		gfx.pose().translate(-attachment.mouthX(), -EquippedMorphlingOverlayPlacement.MOUTH_Y, 0.0f);
		gfx.blit(texture, 0, 0, blit.width(), EquippedMorphlingOverlayPlacement.ATTACHED_SIZE,
				blit.uOffset(), 0, blit.uWidth(), EquippedMorphlingOverlayPlacement.ATTACHED_SIZE,
				EquippedMorphlingOverlayPlacement.ATTACHED_SIZE, EquippedMorphlingOverlayPlacement.ATTACHED_SIZE);
		gfx.pose().popPose();
	}

	private void renderPrimalMotes(GuiGraphics gfx, EquippedMorphlingOverlayPlacement.Attachment attachment,
			float time, int color) {
		for (int i = 0; i < 4; i++) {
			float phase = time * 1.7f + i * 1.57f;
			int moteX = Math.round(attachment.left() + attachment.size() * (0.5f + Mth.cos(phase) * 0.45f));
			int moteY = Math.round(attachment.top() + attachment.size() * (0.5f + Mth.sin(phase * 1.23f) * 0.45f));
			gfx.fill(moteX, moteY, moteX + 2, moteY + 2, color);
		}
	}

}
