package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.ManipulationChargeInput;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.manipulation.ferric.IronheartedRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;

public final class IronheartedOverlay {
	private static final ResourceLocation IRON_HEART_EMPTY =
			Hemomancy.rloc("manipulation/ironhearted/iron_heart_empty");
	private static final ResourceLocation IRON_HEART_FULL =
			Hemomancy.rloc("textures/gui/manipulation/ironhearted/iron_heart_full.png");
	private static final ResourceLocation IRON_HEART_PULSE =
			Hemomancy.rloc("textures/gui/manipulation/ironhearted/iron_heart_pulse.png");
	private static final ResourceLocation[] IRON_HEART_CRACK_FRAMES = {
			Hemomancy.rloc("textures/gui/manipulation/ironhearted/iron_heart_crack_0.png"),
			Hemomancy.rloc("textures/gui/manipulation/ironhearted/iron_heart_crack_1.png"),
			Hemomancy.rloc("textures/gui/manipulation/ironhearted/iron_heart_crack_2.png")
	};
	private static Object trackedPlayer;
	private static float previousIronHeartHealth;
	private static int crackedFirstSlot;
	private static int crackedSlotCount;
	private static long crackStartTick = Long.MIN_VALUE;

	private IronheartedOverlay() {
	}

	public static void renderHUD(GuiGraphics graphics, int width, int height) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui) return;
		var state = HemoCapabilityAccess.getPowerGuardrails(minecraft.player);
		float maxIronHeartHealth = IronheartedRules.maxIronHeartHealth(minecraft.player);
		float charge = ManipulationChargeInput.getChargingManipulationName().equals("ironhearted")
				? ManipulationChargeInput.getManipulationChargeProgress() : 0.0F;
		float storedIron = state.getIronHeartHealth();
		long now = minecraft.level == null ? 0L : minecraft.level.getGameTime();
		if (trackedPlayer != minecraft.player) {
			trackedPlayer = minecraft.player;
			previousIronHeartHealth = storedIron;
			crackStartTick = Long.MIN_VALUE;
		} else {
			int removed = IronheartedRules.removedIronHeartSlots(previousIronHeartHealth, storedIron);
			if (removed > 0) {
				crackedFirstSlot = IronheartedRules.ironHeartSlots(storedIron);
				crackedSlotCount = removed;
				crackStartTick = now;
			}
			previousIronHeartHealth = storedIron;
		}
		float shownIron = Mth.clamp(storedIron
				+ charge * IronheartedRules.IRON_HEART_HEALTH_PER_CAST, 0.0F, maxIronHeartHealth);
		int x = width / 2 - 91;
		int y = height - 39;
		if (shownIron > 0.0F) {
			TextureAtlasSprite emptyHeart = minecraft.getGuiSprites().getSprite(IRON_HEART_EMPTY);
			for (int heart = 0; heart < IronheartedRules.ironHeartSlots(shownIron); heart++) {
				float shownFill = Mth.clamp((shownIron - heart * IronheartedRules.HEALTH_PER_HEART)
						/ IronheartedRules.HEALTH_PER_HEART, 0.0F, 1.0F);
				float storedFill = Mth.clamp((storedIron - heart * IronheartedRules.HEALTH_PER_HEART)
						/ IronheartedRules.HEALTH_PER_HEART, 0.0F, 1.0F);
				var formation = IronheartedRules.ironHeartFormation(storedFill, shownFill);
				int heartY = IronheartedRules.ironHeartY(y, heart, minecraft.player.hasEffect(MobEffects.REGENERATION),
						minecraft.gui.getGuiTicks(), Math.max(minecraft.player.getMaxHealth(), minecraft.player.getHealth()));
				drawIronHeart(graphics, emptyHeart, x + heart * 8, heartY, formation.emptyAlpha(), formation.fill(),
						minecraft.level == null ? 0L : minecraft.level.getGameTime());
			}
		}
		int crackFrame = IronheartedRules.ironHeartCrackFrame(crackStartTick, now);
		if (crackFrame >= 0) {
			for (int heart = crackedFirstSlot; heart < crackedFirstSlot + crackedSlotCount; heart++) {
				int heartY = IronheartedRules.ironHeartY(y, heart, minecraft.player.hasEffect(MobEffects.REGENERATION),
						minecraft.gui.getGuiTicks(), Math.max(minecraft.player.getMaxHealth(), minecraft.player.getHealth()));
				graphics.blit(IRON_HEART_CRACK_FRAMES[crackFrame], x + heart * 8, heartY, 0, 0, 9, 9, 9, 9);
			}
		}
	}

	private static void drawIronHeart(GuiGraphics graphics, TextureAtlasSprite emptyHeart, int x, int y,
			float emptyAlpha, float fill, long gameTime) {
		graphics.blit(x, y, 0, 9, 9, emptyHeart, 1.0F, 1.0F, 1.0F, emptyAlpha);
		int filledWidth = Mth.clamp(Math.round(fill * 9.0F), 0, 9);
		if (filledWidth == 0) return;
		boolean pulse = IronheartedRules.ironHeartPulse(gameTime);
		graphics.blit(pulse ? IRON_HEART_PULSE : IRON_HEART_FULL,
				x, y, 0, 0, filledWidth, 9, 9, 9);
	}
}
