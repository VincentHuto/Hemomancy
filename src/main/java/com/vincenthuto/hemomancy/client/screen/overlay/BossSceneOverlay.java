package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneDirector;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * The screen side of a boss scene, in order: the fade, the scene's own art, the letterbox, the title card under the top
 * bar, and the skip hint. Drawn above the HUD from {@code GuiLayerRegistrar}.
 */
public final class BossSceneOverlay {
	private static final float LETTERBOX = 0.11F;
	private static final int SKIP_HINT_AFTER = 20;

	private BossSceneOverlay() {
	}

	public static void render(GuiGraphics graphics, int width, int height, float partial) {
		BossSceneDirector director = BossSceneDirector.INSTANCE;
		if (!director.presenting()) return;
		BossScene scene = director.scene();
		float t = director.time(partial);
		Minecraft minecraft = Minecraft.getInstance();
		float time = (minecraft.player == null ? 0 : minecraft.player.tickCount) + partial;
		graphics.drawManaged(() -> {
			float fade = scene.fade(t);
			if (fade > 0.002F) GuiCanvas.of(graphics).rect(0, 0, width, height, GuiCanvas.argb(fade, 0), GuiCanvas.argb(fade, 0));
			scene.overlay(graphics, width, height, t, time);
			float bars = HemoEasing.smoothstep(scene.letterbox(t)) * height * LETTERBOX;
			if (bars > 0.05F) {
				GuiCanvas canvas = GuiCanvas.of(graphics);
				canvas.rect(0, 0, width, bars, 0xFF000000, 0xFF000000);
				canvas.rect(0, height - bars, width, height, 0xFF000000, 0xFF000000);
			}
			TitleCard card = scene.card(director.boss());
			if (card != null) card(graphics, card, width, height, t);
			if (t > SKIP_HINT_AFTER && scene.skippable(t)) skipHint(graphics, width, height, bars, director.skipProgress());
		});
	}

	private static void card(GuiGraphics graphics, TitleCard card, int width, int height, float t) {
		float alpha = card.alpha(t);
		if (alpha < 0.02F) return;
		Font font = Minecraft.getInstance().font;
		Component name = Component.translatable(card.nameKey());
		Component epithet = Component.translatable(card.epithetKey());
		// the upper third: the story's chat lines fill the lower one during these scenes
		float cx = width * 0.5F, nameY = height * 0.24F;
		float scale = 2.0F;
		int nameWidth = font.width(name);
		// the epithet rises into place above the name as the card fades in
		float lift = (1.0F - alpha) * 4.0F;
		int muted = GuiCanvas.mix(0xFF000000 | card.ruleColor(), 0xFFFFFFFF, 0.45F) & 0xFFFFFF;
		// a soft dark band so the card reads over a bright sky or the boss itself
		float bandHalf = nameWidth * scale * 0.5F + 40.0F;
		float bandTop = nameY - 18.0F, bandBottom = nameY + font.lineHeight * scale + 8.0F;
		GuiCanvas band = GuiCanvas.of(graphics);
		int shade = GuiCanvas.argb(alpha * 0.42F, 0x080406), clearShade = GuiCanvas.argb(0.0F, 0x080406);
		band.hrect(cx - bandHalf, bandTop, cx, bandBottom, clearShade, shade);
		band.hrect(cx, bandTop, cx + bandHalf, bandBottom, shade, clearShade);
		text(graphics, font, epithet, cx, nameY - 13.0F + lift, 1.0F, muted, alpha * 0.9F);
		text(graphics, font, name, cx, nameY, scale, 0xF4ECE0, alpha);
		float half = (nameWidth * scale * 0.5F + 26.0F) * card.rule(t);
		float ruleY = nameY + font.lineHeight * scale + 3.0F;
		if (half > 0.5F) {
			GuiCanvas canvas = GuiCanvas.of(graphics);
			int solid = GuiCanvas.argb(alpha, card.ruleColor());
			int clear = GuiCanvas.argb(0.0F, card.ruleColor());
			canvas.hrect(cx - half, ruleY, cx, ruleY + 1.0F, clear, solid);
			canvas.hrect(cx, ruleY, cx + half, ruleY + 1.0F, solid, clear);
			canvas.quad(cx, ruleY - 2.5F, solid, cx - 2.5F, ruleY + 0.5F, solid, cx, ruleY + 3.5F, solid, cx + 2.5F,
					ruleY + 0.5F, solid);
		}
	}

	private static void text(GuiGraphics graphics, Font font, Component text, float cx, float y, float scale, int rgb,
			float alpha) {
		int a = Mth.clamp(Math.round(alpha * 255.0F), 4, 255);
		PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.translate(cx, y, 0.0F);
		pose.scale(scale, scale, 1.0F);
		graphics.drawString(font, text, -font.width(text) / 2, 0, a << 24 | rgb, true);
		pose.popPose();
	}

	private static void skipHint(GuiGraphics graphics, int width, int height, float bars, float progress) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		Component hint = Component.translatable("boss.hemomancy.scene.skip", minecraft.options.keyShift.getTranslatedKeyMessage());
		int x = width - font.width(hint) - 8;
		int y = (int) (height - Math.max(bars, 14.0F) * 0.5F - font.lineHeight * 0.5F);
		graphics.drawString(font, hint, x, y, 0x90E8E0D8, false);
		if (progress > 0.0F) {
			GuiCanvas.of(graphics).rect(x, y + font.lineHeight + 1, x + font.width(hint) * progress, y + font.lineHeight + 2,
					0xC0E8E0D8, 0xC0E8E0D8);
		}
	}
}
