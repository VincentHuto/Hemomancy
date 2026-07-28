package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Comparator;

/**
 * Prototype bossbar-style ceremony readout. It deliberately combines color,
 * labels, bar shapes, and pips so none of the live cues depend on color alone.
 */
public final class CardinalRiteOverlay {
	public static CardinalRiteOverlay instance;
	private static final int WIDTH = CardinalRiteOverlayGeometry.HUD_WIDTH;
	private final CardinalRiteProgressSmoother progressSmoother = new CardinalRiteProgressSmoother();
	private long lastRenderNanos;

	public void renderHUD(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTicks) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui) return;
		ActiveRiteClientData.RiteEntry rite = ActiveRiteClientData.getActiveRites().stream()
				.filter(entry -> !entry.isUnstained() && !"LEGACY".equals(entry.getPhase()))
				.filter(entry -> entry.getCenter().distToCenterSqr(minecraft.player.position()) <= 128.0D * 128.0D)
				.min(Comparator.comparingDouble(
						entry -> entry.getCenter().distToCenterSqr(minecraft.player.position())))
				.orElse(null);
		if (rite == null) {
			progressSmoother.clear();
			lastRenderNanos = 0L;
			return;
		}

		int x = (screenWidth - WIDTH) / 2;
		int y = 8;
		long now = System.nanoTime();
		double elapsedSeconds = lastRenderNanos == 0L ? 0.0D : (now - lastRenderNanos) / 1_000_000_000.0D;
		lastRenderNanos = now;
		long riteKey = rite.getCenter().asLong() * 31L + rite.getRecipeId().hashCode();
		double displayedProgress = progressSmoother.update(riteKey, rite.getProgress(), elapsedSeconds);
		int checklistHeight = rite.getChecklist().size() * 10;
		graphics.fill(x - 5, y - 4, x + WIDTH + 5, y + 55 + checklistHeight, 0xAA13070A);
		String title = readable(rite.getRecipeId().getPath()) + " · " + readable(rite.getPhase());
		drawCentered(graphics, title, screenWidth / 2, y, 0xFFF3D5D9);
		drawBar(graphics, x, y + 12, displayedProgress, 0xFF651423, 0xFFE33A53);

		double instability = rite.getInstability() / 100.0D;
		int instabilityColor = rite.getInstability() >= 70 ? 0xFFFF304F
				: rite.getInstability() >= 40 ? 0xFFF2A541 : 0xFF67C587;
		graphics.drawString(minecraft.font, "Instability " + rite.getInstability() + "%", x, y + 23,
				0xFFEADFE1, false);
		drawBar(graphics, x + CardinalRiteOverlayGeometry.INSTABILITY_BAR_OFFSET, y + 24,
				CardinalRiteOverlayGeometry.instabilityBarWidth(), instability,
				0xFF241A1C, instabilityColor);

		String pips = "Rings " + pips(rite.getCompletedRings(), rite.getTotalRings())
				+ "  Waves " + Math.min(rite.getCurrentWave() + 1, Math.max(1, rite.getTotalWaves()))
				+ "/" + Math.max(1, rite.getTotalWaves());
		graphics.drawString(minecraft.font, pips, x, y + 34, 0xFFFFC4CB, false);
		String resources = rite.getCommittedBloodMl() + "/" + rite.getUpfrontBloodMl() + "ml"
				+ (rite.getCarriedIchorMl() > 0 ? "  Ichor " + rite.getCarriedIchorMl() + "ml" : "")
				+ "  Allies " + rite.getAllyCount()
				+ (rite.getSharedBloodMl() >= 0 ? "  Pool " + rite.getSharedBloodMl() + "ml" : "");
		graphics.drawString(minecraft.font, resources, x, y + 44, 0xFFD8B0B5, false);
		if (!rite.getChecklist().isEmpty()) {
			int lineY = y + 57;
			for (String line : rite.getChecklist()) {
				int color = line.contains("missing") || line.startsWith("Dry") ? 0xFFFF6578
						: line.startsWith("Optional") ? 0xFFD8B0B5 : 0xFFFFD36A;
				drawCentered(graphics, line, screenWidth / 2, lineY, color);
				lineY += 10;
			}
		} else if (!rite.getCue().isBlank()) {
			drawCentered(graphics, readable(rite.getCue()), screenWidth / 2, y + 57, 0xFFFFD36A);
		}
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

	private static String pips(int complete, int total) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < total; i++) result.append(i < complete ? "◆" : "◇");
		return result.toString();
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

	private static void drawCentered(GuiGraphics graphics, String text, int centerX, int y, int color) {
		Minecraft minecraft = Minecraft.getInstance();
		graphics.drawString(minecraft.font, text, centerX - minecraft.font.width(text) / 2, y, color, true);
	}
}
