package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.ClientEvents;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.manipulation.BodyIdiomRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;

public final class BodyIdiomOverlay {
	private static final ResourceLocation IRON_HEART_EMPTY =
			Hemomancy.rloc("body_idiom/iron_heart_empty");
	private static final ResourceLocation IRON_HEART_FULL =
			Hemomancy.rloc("textures/gui/body_idiom/iron_heart_full.png");
	private static final ResourceLocation IRON_HEART_PULSE =
			Hemomancy.rloc("textures/gui/body_idiom/iron_heart_pulse.png");
	private static final ResourceLocation[] IRON_HEART_CRACK_FRAMES = {
			Hemomancy.rloc("textures/gui/body_idiom/iron_heart_crack_0.png"),
			Hemomancy.rloc("textures/gui/body_idiom/iron_heart_crack_1.png"),
			Hemomancy.rloc("textures/gui/body_idiom/iron_heart_crack_2.png")
	};
	private static Object trackedPlayer;
	private static float previousIronHeartHealth;
	private static int crackedFirstSlot;
	private static int crackedSlotCount;
	private static long crackStartTick = Long.MIN_VALUE;

	private BodyIdiomOverlay() {
	}

	public static void renderHUD(GuiGraphics graphics, int width, int height) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui) return;
		var state = HemoCapabilityAccess.getPowerGuardrails(minecraft.player);
		float maxIronHeartHealth = BodyIdiomRules.maxIronHeartHealth(minecraft.player);
		String chargingManipulation = ClientEvents.getChargingManipulationName();
		float chargeProgress = ClientEvents.getManipulationChargeProgress();
		float charge = chargingManipulation.equals("ironhearted") ? chargeProgress : 0.0F;
		float storedIron = state.getIronHeartHealth();
		long now = minecraft.level == null ? 0L : minecraft.level.getGameTime();
		if (trackedPlayer != minecraft.player) {
			trackedPlayer = minecraft.player;
			previousIronHeartHealth = storedIron;
			crackStartTick = Long.MIN_VALUE;
		} else {
			int removed = BodyIdiomRules.removedIronHeartSlots(previousIronHeartHealth, storedIron);
			if (removed > 0) {
				crackedFirstSlot = BodyIdiomRules.ironHeartSlots(storedIron);
				crackedSlotCount = removed;
				crackStartTick = now;
			}
			previousIronHeartHealth = storedIron;
		}
		float shownIron = Mth.clamp(storedIron
				+ charge * BodyIdiomRules.IRON_HEART_HEALTH_PER_CAST, 0.0F, maxIronHeartHealth);
		int x = width / 2 - 91;
		int y = height - 39;
		if (shownIron > 0.0F) {
			TextureAtlasSprite emptyHeart = minecraft.getGuiSprites().getSprite(IRON_HEART_EMPTY);
			for (int heart = 0; heart < BodyIdiomRules.ironHeartSlots(shownIron); heart++) {
				float shownFill = Mth.clamp((shownIron - heart * BodyIdiomRules.HEALTH_PER_HEART)
						/ BodyIdiomRules.HEALTH_PER_HEART, 0.0F, 1.0F);
				float storedFill = Mth.clamp((storedIron - heart * BodyIdiomRules.HEALTH_PER_HEART)
						/ BodyIdiomRules.HEALTH_PER_HEART, 0.0F, 1.0F);
				var formation = BodyIdiomRules.ironHeartFormation(storedFill, shownFill);
				int heartY = BodyIdiomRules.ironHeartY(y, heart, minecraft.player.hasEffect(MobEffects.REGENERATION),
						minecraft.gui.getGuiTicks(), Math.max(minecraft.player.getMaxHealth(), minecraft.player.getHealth()));
				drawIronHeart(graphics, emptyHeart, x + heart * 8, heartY, formation.emptyAlpha(), formation.fill(),
						minecraft.level == null ? 0L : minecraft.level.getGameTime());
			}
		}
		int crackFrame = BodyIdiomRules.ironHeartCrackFrame(crackStartTick, now);
		if (crackFrame >= 0) {
			for (int heart = crackedFirstSlot; heart < crackedFirstSlot + crackedSlotCount; heart++) {
				int heartY = BodyIdiomRules.ironHeartY(y, heart, minecraft.player.hasEffect(MobEffects.REGENERATION),
						minecraft.gui.getGuiTicks(), Math.max(minecraft.player.getMaxHealth(), minecraft.player.getHealth()));
				graphics.blit(IRON_HEART_CRACK_FRAMES[crackFrame], x + heart * 8, heartY, 0, 0, 9, 9, 9, 9);
			}
		}
		if (!chargingManipulation.isEmpty() && !chargingManipulation.equals("ironhearted")) {
			drawChargeBar(graphics, width, height, chargeProgress);
		}

		boolean refractory = now < state.getBlackheartedCooldownUntil();
		if (state.getNecroticSaturation() > 0.0F || refractory) {
			int barWidth = 43;
			int barY = y + 9;
			graphics.fill(x, barY, x + barWidth + 2, barY + 5, 0xDD130D17);
			int fill = Math.round(barWidth * state.getNecroticSaturation()
					/ BodyIdiomRules.NECROTIC_SATURATION_CAP);
			graphics.fill(x + 1, barY + 1, x + 1 + fill, barY + 4,
					refractory ? 0xFF4B3549 : 0xFF4D162F);
			graphics.drawString(minecraft.font, refractory ? "REFRACTORY" : "NECROSIS",
					x + barWidth + 5, barY - 2, refractory ? 0xFF8E788A : 0xFFB85B78, true);
		}
	}

	private static void drawChargeBar(GuiGraphics graphics, int width, int height, float progress) {
		int barWidth = 52;
		int x = width / 2 - barWidth / 2;
		int y = height - 51;
		graphics.fill(x - 1, y - 1, x + barWidth + 1, y + 5, 0xDD130D17);
		graphics.fill(x, y, x + Math.round(barWidth * progress), y + 4, 0xFF9E1B32);
	}

	private static void drawIronHeart(GuiGraphics graphics, TextureAtlasSprite emptyHeart, int x, int y,
			float emptyAlpha, float fill, long gameTime) {
		graphics.blit(x, y, 0, 9, 9, emptyHeart, 1.0F, 1.0F, 1.0F, emptyAlpha);
		int filledWidth = Mth.clamp(Math.round(fill * 9.0F), 0, 9);
		if (filledWidth == 0) return;
		boolean pulse = BodyIdiomRules.ironHeartPulse(gameTime);
		graphics.blit(pulse ? IRON_HEART_PULSE : IRON_HEART_FULL,
				x, y, 0, 0, filledWidth, 9, 9, 9);
	}
}
