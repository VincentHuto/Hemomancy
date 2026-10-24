package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.Comparator;
import java.util.Locale;

/**
 * Bossbar-style readout for the nearest Harbinger ceremony. It deliberately
 * combines color, labels, bar shapes, and pips so none of the live cues depend
 * on color alone.
 */
public final class CardinalRiteOverlay {
	public static CardinalRiteOverlay instance;
	private static final int WIDTH = CardinalRiteOverlayGeometry.HUD_WIDTH;
	private final CardinalRiteProgressSmoother progressSmoother = new CardinalRiteProgressSmoother();
	private long lastRenderNanos;

	public void renderHUD(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTicks) {
		if (HemoClientConfig.cardinalRiteHud() != HemoClientConfig.CardinalRiteHudMode.FULL) return;
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui) return;
		ActiveRiteData.RiteEntry rite = ActiveRiteClientData.getActiveRites().stream()
				.filter(entry -> !entry.isUnstained() && !entry.isLegacy())
				.filter(entry -> entry.getCenter().distToCenterSqr(minecraft.player.position()) <= 128.0D * 128.0D)
				.min(Comparator.comparingDouble(
						entry -> entry.getCenter().distToCenterSqr(minecraft.player.position())))
				.orElse(null);
		if (rite == null) {
			progressSmoother.clear();
			lastRenderNanos = 0L;
			return;
		}

		graphics.pose().pushPose();
		graphics.pose().scale(CardinalRiteOverlayGeometry.HUD_SCALE,
				CardinalRiteOverlayGeometry.HUD_SCALE, 1.0F);
		screenWidth = CardinalRiteOverlayGeometry.virtualScreenWidth(screenWidth);
		int x = (screenWidth - WIDTH) / 2;
		int y = 8;
		long now = System.nanoTime();
		double elapsedSeconds = lastRenderNanos == 0L ? 0.0D : (now - lastRenderNanos) / 1_000_000_000.0D;
		lastRenderNanos = now;
		long riteKey = rite.getCenter().asLong() * 31L + rite.getRecipeId().hashCode();
		double displayedProgress = progressSmoother.update(riteKey, rite.getProgress(), elapsedSeconds);
		int checklistHeight = rite.getChecklist().size() * 10;
		graphics.fill(x - 5, y - 4, x + WIDTH + 5, y + 55 + checklistHeight, 0xAA13070A);
		Component title = Component.translatable("screen.hemomancy.cardinal_rite_overlay.title",
				riteName(minecraft, rite), phaseName(rite.getPhase()));
		drawCentered(graphics, title, screenWidth / 2, y, 0xFFF3D5D9);
		drawBar(graphics, x, y + 12, displayedProgress, 0xFF651423, 0xFFE33A53);

        if (rite.isSuccession()) {
            drawCentered(graphics, Component.translatable(rite.getCue()), screenWidth / 2, y + 28, 0xFFFFD36A);
            drawCentered(graphics, Component.translatable("hemomancy.succession.rite_hint"),
                    screenWidth / 2, y + 42, 0xFFEADFE1);
            graphics.pose().popPose();
            return;
        }

		double instability = rite.getInstability() / 100.0D;
		int instabilityColor = rite.getInstability() >= 70 ? 0xFFFF304F
				: rite.getInstability() >= 40 ? 0xFFF2A541 : 0xFF67C587;
		graphics.drawString(minecraft.font, Component.translatable("screen.hemomancy.cardinal_rite_overlay.instability",
				rite.getInstability()), x, y + 23, 0xFFEADFE1, false);
		drawBar(graphics, x + CardinalRiteOverlayGeometry.INSTABILITY_BAR_OFFSET, y + 24,
				CardinalRiteOverlayGeometry.instabilityBarWidth(), instability,
				0xFF241A1C, instabilityColor);

		Component pips = Component.translatable("screen.hemomancy.cardinal_rite_overlay.rings",
				pips(rite.getCompletedRings(), rite.getOpenRings(), rite.getTotalRings()),
				waveSummary(rite.getCurrentWave(), rite.getTotalWaves()));
		graphics.drawString(minecraft.font, pips, x, y + 34, 0xFFFFC4CB, false);
		MutableComponent resources = Component.translatable("screen.hemomancy.cardinal_rite_overlay.anchors",
				rite.getCommittedBloodMl(), rite.getUpfrontBloodMl());
		if (rite.getCarriedIchorMl() > 0) {
			resources.append(Component.translatable("screen.hemomancy.cardinal_rite_overlay.ichor", rite.getCarriedIchorMl()));
		}
		resources.append(Component.translatable("screen.hemomancy.cardinal_rite_overlay.allies", rite.getAllyCount()));
		if (rite.getSharedBloodMl() >= 0) {
			resources.append(Component.translatable("screen.hemomancy.cardinal_rite_overlay.pool", rite.getSharedBloodMl()));
		}
		graphics.drawString(minecraft.font, resources, x, y + 44, 0xFFD8B0B5, false);
		if (!rite.getChecklist().isEmpty()) {
			int lineY = y + 57;
			for (Component line : rite.getChecklist()) {
				drawCentered(graphics, line, screenWidth / 2, lineY, checklistColor(line));
				lineY += 10;
			}
		} else if (!rite.getCue().isBlank()) {
			drawCentered(graphics, waveName(rite.getCue()), screenWidth / 2, y + 57, 0xFFFFD36A);
		}
		graphics.pose().popPose();
	}

	private static void drawBar(GuiGraphics graphics, int x, int y, double progress,
			int background, int fill) {
		drawBar(graphics, x, y, WIDTH, progress, background, fill);
	}

	private static void drawBar(GuiGraphics graphics, int x, int y, int width, double progress,
			int background, int fill) {
		graphics.fill(x, y, x + width, y + 7, 0xFF050304);
		graphics.fill(x + 1, y + 1, x + width - 1, y + 6, background);
		int filled = (int) Math.round((width - 2) * Math.max(0.0D, Math.min(1.0D, progress)));
		graphics.fill(x + 1, y + 1, x + 1 + filled, y + 6, fill);
	}

	/** Fed rings solid, woken rings hollow diamonds, sleeping rings hollow circles. */
	static String pips(int complete, int open, int total) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < total; i++) result.append(i < complete ? "◆" : i < open ? "◇" : "○");
		return result.toString();
	}

	static Component waveSummary(int currentWave, int totalWaves) {
		return totalWaves <= 0 ? Component.translatable("screen.hemomancy.cardinal_rite_overlay.no_ordeal")
				: Component.translatable("screen.hemomancy.cardinal_rite_overlay.waves",
						Math.min(currentWave + 1, totalWaves), totalWaves);
	}

	/** Blockers red, optional preparations muted, everything else gold. */
	private static int checklistColor(Component line) {
		String key = line.getContents() instanceof TranslatableContents translatable ? translatable.getKey() : "";
		if (key.endsWith(".focus_medium_mismatch") || key.endsWith(".dry_anchors")) return 0xFFFF6578;
		if (key.endsWith(".optional_sigils") || key.endsWith(".optional_allies_assigned")) return 0xFFD8B0B5;
		return 0xFFFFD36A;
	}

	/** The rite's name, or its recipe path when the recipe is not known here. */
	private static Component riteName(Minecraft minecraft, ActiveRiteData.RiteEntry rite) {
		if (minecraft.level != null) {
			var holder = minecraft.level.getRecipeManager().byKey(rite.getRecipeId()).orElse(null);
			if (holder != null && holder.value() instanceof CardinalRiteRecipe recipe) return recipe.displayName();
		}
		return Component.literal(readable(rite.getRecipeId().getPath()));
	}

	static Component phaseName(String phase) {
		return Component.translatableWithFallback("cardinal_rite_phase.hemomancy." + phase.toLowerCase(Locale.ROOT),
				readable(phase));
	}

	static Component waveName(String wave) {
		return Component.translatableWithFallback("cardinal_rite_wave.hemomancy." + wave, readable(wave));
	}

	private static String readable(String value) {
		String path = value;
		int slash = path.lastIndexOf('/');
		if (slash >= 0) path = path.substring(slash + 1);
		String[] words = path.toLowerCase().split("_");
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (!result.isEmpty()) result.append(' ');
			if (!word.isEmpty()) result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return result.toString();
	}

	private static void drawCentered(GuiGraphics graphics, Component text, int centerX, int y, int color) {
		Minecraft minecraft = Minecraft.getInstance();
		graphics.drawString(minecraft.font, text, centerX - minecraft.font.width(text) / 2, y, color, true);
	}
}
