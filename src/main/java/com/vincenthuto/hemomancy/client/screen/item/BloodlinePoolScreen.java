package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.data.BloodlinePoolClientData;
import com.vincenthuto.hemomancy.client.data.FaneBoundaryClientData;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A standalone screen that lets the player view their bloodline's shared blood pool,
 * donate blood (lump or trickle), and configure auto-draw settings.
 */
public class BloodlinePoolScreen extends Screen {

	private static final int GUI_WIDTH = 340;
	private static final int GUI_HEIGHT = 336;
	private static final int SCREEN_PADDING = 8;
	private static final int VEIN_COUNT = 28;

	private float[][] veinParams;
	private float currentLayoutScale = 1.0F;
	private int currentGuiWidth = GUI_WIDTH;
	private int currentGuiHeight = GUI_HEIGHT;
	private int guiLeft;
	private int guiTop;
	private BloodVolumeBarWidget.Bounds vesselBarBounds = BloodVolumeBarWidget.Bounds.EMPTY;

	// Widgets
	private EditBox donateAmountField;
	private Button donateButton;
	private Button trickleToggleButton;
	private EditBox trickleRateField;
	private Button autoDrawToggleButton;
	private EditBox autoDrawThresholdField;
	private Button faneSightButton;
	private EditBox messageField;
	private Button sendMessageButton;
	private Button kickPrevButton;
	private Button kickNextButton;
	private Button kickMemberButton;
	private int kickTargetIndex;

	// Cached capability data
	private boolean trickleEnabled;
	private double trickleRate;
	private boolean autoDrawEnabled;
	private double autoDrawThreshold;

	public BloodlinePoolScreen() {
		super(Component.literal("Bloodline Pool"));
	}

	public static void openScreen() {
		// Request fresh pool data from the server before opening
		PacketHandler.sendToServer(new PacketRequestPoolData());
		Minecraft.getInstance().setScreen(new BloodlinePoolScreen());
	}

	@Override
	protected void init() {
		super.init();
		updateScaledLayout();

		// Seed vein parameters
		Random rand = new Random(42L);
		veinParams = new float[VEIN_COUNT][9];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();
			veinParams[i][1] = rand.nextFloat();
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
			veinParams[i][3] = 0.3f + rand.nextFloat() * 0.7f;
			veinParams[i][4] = 8f + rand.nextFloat() * 18f;
			veinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;
			veinParams[i][6] = 60 + rand.nextInt(120);
			veinParams[i][7] = 1 + rand.nextInt(3);
			veinParams[i][8] = rand.nextFloat();
		}

		// Read current settings from capability
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			HemoCapabilityAccess.getBloodVolume(player).ifPresent(vol -> {
				trickleEnabled = vol.isTrickleEnabled();
				trickleRate = vol.getTrickleRate();
				autoDrawEnabled = vol.isAutoDrawEnabled();
				autoDrawThreshold = vol.getAutoDrawThreshold();
			});
		}

		int widgetX = guiLeft + scaled(16);
		int widgetW = Math.max(120, currentGuiWidth - scaled(32));
		int y = guiTop + scaled(156);

		// ── Lump Donate Section ──
		int donateButtonW = Math.max(64, scaled(70));
		int fieldGap = scaled(6);
		donateAmountField = new CenteredEditBox(this.font, widgetX, y, Math.max(48, widgetW - donateButtonW - fieldGap),
				widgetHeight(20), Component.literal("Amount"));
		styleEditBox(donateAmountField);
		donateAmountField.setMaxLength(7);
		donateAmountField.setValue("100");
		donateAmountField.setFilter(s -> s.isEmpty() || s.matches("\\d*\\.?\\d*"));
		addRenderableWidget(donateAmountField);

		donateButton = new RitualButton(widgetX + widgetW - donateButtonW, y, donateButtonW, widgetHeight(18),
				Component.literal("Donate"), btn -> {
			try {
				double amount = Double.parseDouble(donateAmountField.getValue());
				if (amount > 0) {
					PacketHandler.sendToServer(new PacketLumpDonate(amount));
					// Refresh pool data after a short delay (server processes first)
					PacketHandler.sendToServer(new PacketRequestPoolData());
				}
			} catch (NumberFormatException ignored) {
			}
		});
		addRenderableWidget(donateButton);

		y += scaled(30);

		// ── Trickle Donation Section ──
		trickleToggleButton = new ToggleRitualButton(widgetX, y, scaled(126), widgetHeight(18),
				"Trickle Tithe", trickleEnabled, selected -> {
			trickleEnabled = selected;
			sendPoolSettings();
		});
		addRenderableWidget(trickleToggleButton);

		trickleRateField = new CenteredEditBox(this.font, widgetX + scaled(226), y, scaled(58), widgetHeight(18),
				Component.literal("Rate"));
		styleEditBox(trickleRateField);
		trickleRateField.setMaxLength(6);
		trickleRateField.setValue(String.format("%.2f", trickleRate));
		trickleRateField.setFilter(s -> s.isEmpty() || s.matches("\\d*\\.?\\d*"));
		trickleRateField.setResponder(value -> sendPoolSettings());
		addRenderableWidget(trickleRateField);

		y += scaled(26);

		// ── Auto-Draw Section ──
		autoDrawToggleButton = new ToggleRitualButton(widgetX, y, scaled(126), widgetHeight(18),
				"Blood Recall", autoDrawEnabled, selected -> {
			autoDrawEnabled = selected;
			sendPoolSettings();
		});
		addRenderableWidget(autoDrawToggleButton);

		autoDrawThresholdField = new CenteredEditBox(this.font, widgetX + scaled(226), y, scaled(58), widgetHeight(18),
				Component.literal("Threshold"));
		styleEditBox(autoDrawThresholdField);
		autoDrawThresholdField.setMaxLength(4);
		autoDrawThresholdField.setValue(String.format("%.0f", autoDrawThreshold * 100));
		autoDrawThresholdField.setFilter(s -> s.isEmpty() || s.matches("\\d*\\.?\\d*"));
		autoDrawThresholdField.setResponder(value -> sendPoolSettings());
		addRenderableWidget(autoDrawThresholdField);

		y += scaled(26);

		// ── Fane Sight Button ──
		faneSightButton = new RitualButton(widgetX, y, widgetW, widgetHeight(20),
				Component.literal(FaneBoundaryClientData.viewMode().label()), btn -> {
			FaneBoundaryClientData.ViewMode mode = FaneBoundaryClientData.cycleViewMode();
			btn.setMessage(Component.literal(mode.label()));
		});
		addRenderableWidget(faneSightButton);

		y += scaled(36);

		// ── Bloodline Message Section ──
		int sendButtonW = Math.max(48, scaled(54));
		messageField = new CenteredEditBox(this.font, widgetX, y, Math.max(58, widgetW - sendButtonW - fieldGap),
				widgetHeight(20), Component.literal("Message"));
		styleEditBox(messageField);
		messageField.setMaxLength(256);
		messageField.setHint(Component.empty());
		addRenderableWidget(messageField);

		sendMessageButton = new RitualButton(widgetX + widgetW - sendButtonW, y, sendButtonW, widgetHeight(18),
				Component.literal("Send"), btn -> {
			String msg = messageField.getValue().trim();
			if (!msg.isEmpty()) {
				PacketHandler.sendToServer(new PacketBloodlineMessage(msg));
				messageField.setValue("");
			}
		});
		addRenderableWidget(sendMessageButton);

		y += scaled(28);

		// ── Leader Member Management ──
		if (player != null) {
			final LocalPlayer localPlayer = player;
			final int kickRowY = y;
			final int kickPrevX = widgetX;
			final int kickButtonX = widgetX + widgetW - scaled(66);
			final int kickButtonW = scaled(66);
			HemoCapabilityAccess.getBloodVolume(player).ifPresent(vol -> {
				Bloodline line = vol.getBloodLine();
				if (line.isValid() && localPlayer.getUUID().equals(line.getLeaderUUID())) {
					kickPrevButton = new RitualButton(kickPrevX, kickRowY, widgetHeight(18), widgetHeight(18),
							Component.literal("<"), btn -> {
						int size = getKickableMembers(localPlayer).size();
						if (size > 0) {
							kickTargetIndex = (kickTargetIndex - 1 + size) % size;
						}
					});
					addRenderableWidget(kickPrevButton);

					kickNextButton = new RitualButton(widgetX + scaled(34), kickRowY, widgetHeight(18), widgetHeight(18),
							Component.literal(">"), btn -> {
						int size = getKickableMembers(localPlayer).size();
						if (size > 0) {
							kickTargetIndex = (kickTargetIndex + 1) % size;
						}
					});
					addRenderableWidget(kickNextButton);

					kickMemberButton = new RitualButton(kickButtonX, kickRowY, kickButtonW, widgetHeight(18),
							Component.literal("Banish"), btn -> {
						List<UUID> kickable = getKickableMembers(localPlayer);
						if (!kickable.isEmpty()) {
							int idx = Mth.clamp(kickTargetIndex, 0, kickable.size() - 1);
							PacketHandler.sendToServer(new PacketKickBloodlinePlayer(kickable.get(idx)));
							PacketHandler.sendToServer(new PacketRequestPoolData());
							kickTargetIndex = 0;
						}
					});
					addRenderableWidget(kickMemberButton);
				}
			});
		}
	}

	private void styleEditBox(EditBox editBox) {
		editBox.setBordered(false);
		editBox.setTextColor(0x00FFFFFF);
		editBox.setTextColorUneditable(0x00FFFFFF);
	}

	private void sendPoolSettings() {
		double rate = 0.5;
		if (trickleRateField != null && !trickleRateField.getValue().isBlank()) {
			try {
				rate = Double.parseDouble(trickleRateField.getValue());
			} catch (NumberFormatException ignored) {
			}
		}

		double threshold = 0.25;
		if (autoDrawThresholdField != null && !autoDrawThresholdField.getValue().isBlank()) {
			try {
				threshold = Double.parseDouble(autoDrawThresholdField.getValue()) / 100.0;
			} catch (NumberFormatException ignored) {
			}
		}

		PacketHandler.sendToServer(new PacketUpdatePoolSettings(trickleEnabled, rate, autoDrawEnabled, threshold));
	}

	private void updateScaledLayout() {
		this.currentLayoutScale = fitScale(this.width, this.height);
		this.currentGuiWidth = Math.max(1, Math.round(GUI_WIDTH * currentLayoutScale));
		this.currentGuiHeight = Math.max(1, Math.round(GUI_HEIGHT * currentLayoutScale));
		this.guiLeft = Math.max(SCREEN_PADDING, (this.width - currentGuiWidth) / 2);
		this.guiTop = Math.max(SCREEN_PADDING, (this.height - currentGuiHeight) / 2);
	}

	private static float fitScale(int screenWidth, int screenHeight) {
		int availableWidth = Math.max(1, screenWidth - SCREEN_PADDING * 2);
		int availableHeight = Math.max(1, screenHeight - SCREEN_PADDING * 2);
		float fit = Math.min(availableWidth / (float) GUI_WIDTH, availableHeight / (float) GUI_HEIGHT);
		return Math.min(1.0F, fit);
	}

	private int scaled(int value) {
		return Math.max(1, Math.round(value * currentLayoutScale));
	}

	private int widgetHeight(int baseHeight) {
		return Math.max(14, scaled(baseHeight));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	/** Suppress the 1.21.1 menu_blur post-effect from Screen#renderBackground. */
	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// intentionally empty
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Do NOT call renderBackground() — it applies blur. This is not a pause screen.

		int centerX = guiLeft + currentGuiWidth / 2;

		// Animated vein background
		renderVeinBackground(graphics, guiLeft, guiTop, currentGuiWidth, currentGuiHeight);

		// Border
		drawBorder(graphics, guiLeft, guiTop, currentGuiWidth, currentGuiHeight);
		drawCornerSigils(graphics, guiLeft, guiTop, currentGuiWidth, currentGuiHeight);

		// Title
		graphics.drawCenteredString(this.font, this.title, centerX, guiTop + scaled(6), 0xFFFF6A75);

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
			Bloodline bloodline = volume.getBloodLine();

			if (!bloodline.isValid()) {
				graphics.drawCenteredString(this.font,
						Component.literal("You are not in a bloodline."),
						centerX, guiTop + scaled(40), 0xFFAA4444);
				return;
			}

			int y = guiTop + scaled(22);

			// Bloodline name
			graphics.drawCenteredString(this.font,
					Component.literal(bloodline.getName()).withStyle(s -> s.withColor(0xFFDD6666).withBold(true)),
					centerX, y, 0xFFFFFFFF);
			y += scaled(14);

			// Member count
			graphics.drawCenteredString(this.font,
					Component.literal("Members: " + BloodlinePoolClientData.getMemberCount()),
					centerX, y, 0xFFAAAAAA);
			y += scaled(14);

			// Pool volume reservoir
			float poolVol = BloodlinePoolClientData.getPoolVolume();
			float poolMax = BloodlinePoolClientData.getPoolMax();
			float ratio = poolMax > 0 ? Mth.clamp(poolVol / poolMax, 0f, 1f) : 0f;

			String poolText = String.format("%s / %.0f ml", formatPoolVolume(poolVol), poolMax);
			int reservoirCenterX = centerX + scaled(18);
			int reservoirCenterY = guiTop + scaled(92);
			renderBloodSphere(graphics, reservoirCenterX, reservoirCenterY, scaled(44), ratio, poolText, partialTick);

			renderVesselBloodBar(graphics, reservoirCenterX - scaled(86), reservoirCenterY - scaled(26),
					volume.getBloodVolume(), volume.getMaxBloodVolume());

			// Label for donate field
			graphics.drawString(this.font, "Offer (ml):", donateAmountField.getX(), donateAmountField.getY() - scaled(14),
					0xFFFF8A8A,
					true);

			// Trickle rate label
			graphics.drawString(this.font, "Rate:", trickleRateField.getX() - scaled(42),
					trickleRateField.getY() + scaled(5), 0xFFFF8A8A, true);

			// Auto-draw threshold label
			graphics.drawString(this.font, "Threshold %:", autoDrawThresholdField.getX() - scaled(86),
					autoDrawThresholdField.getY() + scaled(5), 0xFFFF8A8A, true);

			// Bloodline message label
			graphics.drawString(this.font, "Bloodline Message:", messageField.getX(),
					messageField.getY() - scaled(14), 0xFFFF8A8A, true);

			if (kickMemberButton != null) {
				List<UUID> kickable = getKickableMembers(player);
				int targetX = kickNextButton.getX() + kickNextButton.getWidth() + scaled(6);
				int targetW = Math.max(48, kickMemberButton.getX() - targetX - scaled(6));
				renderRitualPanel(graphics, targetX, kickMemberButton.getY(), targetW, kickMemberButton.getHeight(),
						0xFF050101, 0xFF6A1218);
				if (kickable.isEmpty()) {
					drawCenteredFittingString(graphics, this.font, "No player members", targetX + targetW / 2,
							kickMemberButton.getY() + scaled(5), targetW - scaled(6), 0xFFAA6666, false);
					kickMemberButton.active = false;
					kickPrevButton.active = false;
					kickNextButton.active = false;
				} else {
					kickMemberButton.active = true;
					kickPrevButton.active = true;
					kickNextButton.active = true;
					kickTargetIndex = Mth.clamp(kickTargetIndex, 0, kickable.size() - 1);
					String shortTarget = resolvePlayerName(kickable.get(kickTargetIndex));
					drawCenteredFittingString(graphics, this.font, shortTarget, targetX + targetW / 2,
							kickMemberButton.getY() + scaled(5), targetW - scaled(6), 0xFFFFC8C8, false);
				}
			}
		});

		// Render widgets on top
		renderEditBoxFrame(graphics, donateAmountField);
		renderEditBoxFrame(graphics, trickleRateField);
		renderEditBoxFrame(graphics, autoDrawThresholdField);
		renderEditBoxFrame(graphics, messageField);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderCenteredEditBoxText(graphics, donateAmountField, "100");
		renderCenteredEditBoxText(graphics, trickleRateField, "0.00");
		renderCenteredEditBoxText(graphics, autoDrawThresholdField, "0");
		renderCenteredEditBoxText(graphics, messageField, "Send to bloodline...");
		renderVesselTooltip(graphics, mouseX, mouseY);
	}

	// ───── Color Lerp for Pool Bar ─────

	private static int blendColor(int from, int to, float ratio) {
		float t = Mth.clamp(ratio, 0.0F, 1.0F);
		int a = (int) Mth.lerp(t, (from >>> 24) & 0xFF, (to >>> 24) & 0xFF);
		int r = (int) Mth.lerp(t, (from >>> 16) & 0xFF, (to >>> 16) & 0xFF);
		int g = (int) Mth.lerp(t, (from >>> 8) & 0xFF, (to >>> 8) & 0xFF);
		int b = (int) Mth.lerp(t, from & 0xFF, to & 0xFF);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private static int multiplyColor(int color, float multiplier) {
		float m = Mth.clamp(multiplier, 0.0F, 1.5F);
		int a = (color >>> 24) & 0xFF;
		int r = (int) Mth.clamp(((color >>> 16) & 0xFF) * m, 0, 255);
		int g = (int) Mth.clamp(((color >>> 8) & 0xFF) * m, 0, 255);
		int b = (int) Mth.clamp((color & 0xFF) * m, 0, 255);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private void renderBloodSphere(GuiGraphics graphics, int centerX, int centerY, int radius, float ratio,
			String label, float partialTick) {
		float time = animTime + partialTick;
		int outerRadius = radius + scaled(5);
		for (int row = -outerRadius; row <= outerRadius; row++) {
			int outerHalf = (int) Math.sqrt(Math.max(0, outerRadius * outerRadius - row * row));
			int innerHalf = (int) Math.sqrt(Math.max(0, Math.max(0, outerRadius - scaled(3))
					* Math.max(0, outerRadius - scaled(3)) - row * row));
			int y = centerY + row;
			int rim = Math.abs(row) > outerRadius - scaled(3) ? 0xAA9E1F2A : 0x775D1218;
			graphics.fill(centerX - outerHalf, y, centerX - innerHalf, y + 1, rim);
			graphics.fill(centerX + innerHalf + 1, y, centerX + outerHalf + 1, y + 1, rim);
		}
		int glass = 0xAA5D1218;
		for (int row = -radius; row <= radius; row++) {
			int half = (int) Math.sqrt(Math.max(0, radius * radius - row * row));
			float shade = 1.0F - Math.abs(row) / (float) Math.max(1, radius);
			int alpha = (int) (30 + 34 * shade);
			graphics.fill(centerX - half, centerY + row, centerX + half + 1, centerY + row + 1,
					(alpha << 24) | 0x00210A0D);
			if (row == -radius || row == radius || half <= 2) {
				graphics.fill(centerX - half, centerY + row, centerX + half + 1, centerY + row + 1, glass);
			}
		}

		int fillTop = centerY + radius - Math.round((radius * 2.0F) * ratio);
		for (int row = Math.max(centerY - radius, fillTop); row <= centerY + radius; row++) {
			int local = row - centerY;
			int half = (int) Math.sqrt(Math.max(0, radius * radius - local * local));
			float depth = Mth.clamp((row - fillTop) / (float) Math.max(1, centerY + radius - fillTop), 0.0F, 1.0F);
			float pulse = 0.82F + 0.18F * Mth.sin(time * 2.4F + row * 0.12F);
			int wave = row == fillTop ? Math.round(Mth.sin(time * 5.0F) * scaled(2)) : 0;
			int color = multiplyColor(blendColor(0xEED32327, 0xEE520507, depth), pulse);
			graphics.fill(centerX - half + scaled(2) + wave, row, centerX + half - scaled(1) + wave, row + 1, color);
			if (row == fillTop) {
				graphics.fill(centerX - half + scaled(2) + wave, row, centerX + half - scaled(1) + wave, row + 1,
						0xDDFF5A52);
			}
		}

		drawCenteredFittingString(graphics, this.font, label, centerX, centerY + scaled(2),
				radius * 2 - scaled(12), 0xFFFFA0A0, false);
	}

	private void renderVesselBloodBar(GuiGraphics graphics, int x, int y, double volume, double maxVolume) {
		vesselBarBounds = BloodVolumeBarWidget.render(graphics, x, y, scaled(8), scaled(52),
				volume, maxVolume, animTime, 0xFF330808, 0xFF220606);
	}

	private void renderVesselTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume ->
				BloodVolumeBarWidget.renderTooltip(graphics, this.font, vesselBarBounds,
						volume.getBloodVolume(), volume.getMaxBloodVolume(), mouseX, mouseY));
	}

	private void renderEditBoxFrame(GuiGraphics graphics, EditBox editBox) {
		if (editBox == null) {
			return;
		}
		renderRitualPanel(graphics, editBox.getX() - 2, editBox.getY() - 2,
				editBox.getWidth() + 4, editBox.getHeight() + 4, 0xFF050101,
				editBox.isFocused() ? 0xFFFF6570 : 0xFF6A1218);
	}

	private void renderCenteredEditBoxText(GuiGraphics graphics, EditBox editBox, String hint) {
		if (editBox == null) {
			return;
		}
		String value = editBox.getValue();
		boolean showingHint = value.isEmpty() && !editBox.isFocused();
		String text = showingHint ? hint : value;
		int color = showingHint ? 0xFF7A4C4C : 0xFFFFD9D0;
		drawCenteredFittingString(graphics, this.font, text, editBox.getX() + editBox.getWidth() / 2,
				editBox.getY() + (editBox.getHeight() - 8) / 2, editBox.getWidth() - scaled(8), color, true);
		if (editBox.isFocused() && (int) (animTime * 2.0F) % 2 == 0) {
			int cursorX = editBox.getX() + editBox.getWidth() / 2 + Math.min(this.font.width(text) / 2,
					Math.max(0, editBox.getWidth() / 2 - scaled(5)));
			graphics.fill(cursorX + scaled(1), editBox.getY() + scaled(4), cursorX + scaled(2),
					editBox.getY() + editBox.getHeight() - scaled(4), 0xFFFFD9D0);
		}
	}

	private static void drawCenteredFittingString(GuiGraphics graphics, Font font, String text, int centerX, int y,
			int maxWidth, int color, boolean shadow) {
		if (text == null || text.isEmpty()) {
			return;
		}
		int textWidth = font.width(text);
		if (textWidth <= maxWidth) {
			graphics.drawCenteredString(font, text, centerX, y, color);
			return;
		}
		float scale = Mth.clamp(maxWidth / (float) Math.max(1, textWidth), 0.55F, 1.0F);
		graphics.pose().pushPose();
		graphics.pose().translate(centerX, y, 0);
		graphics.pose().scale(scale, scale, 1.0F);
		graphics.drawString(font, text, -font.width(text) / 2, 0, color, shadow);
		graphics.pose().popPose();
	}

	private void renderRitualPanel(GuiGraphics graphics, int x, int y, int width, int height, int fill, int edge) {
		graphics.fill(x, y, x + width, y + height, fill);
		graphics.fill(x, y, x + width, y + 1, edge);
		graphics.fill(x, y + height - 1, x + width, y + height, 0xFF210407);
		graphics.fill(x, y, x + 1, y + height, edge);
		graphics.fill(x + width - 1, y, x + width, y + height, edge);
		graphics.fill(x + 2, y + 2, x + width - 2, y + 3, 0x663D090D);
	}

	private void drawCornerSigils(GuiGraphics graphics, int x, int y, int width, int height) {
		int color = 0xAA9E1F2A;
		int s = scaled(8);
		drawSigil(graphics, x + scaled(6), y + scaled(6), s, color);
		drawSigil(graphics, x + width - scaled(6) - s, y + scaled(6), s, color);
		drawSigil(graphics, x + scaled(6), y + height - scaled(6) - s, s, color);
		drawSigil(graphics, x + width - scaled(6) - s, y + height - scaled(6) - s, s, color);
	}

	private void drawSigil(GuiGraphics graphics, int x, int y, int size, int color) {
		int cx = x + size / 2;
		int cy = y + size / 2;
		graphics.fill(cx, y, cx + 1, y + size, color);
		graphics.fill(x, cy, x + size, cy + 1, color);
		graphics.fill(x + 2, y + 2, x + size - 2, y + 3, color);
		graphics.fill(x + 2, y + size - 3, x + size - 2, y + size - 2, color);
	}

	// ───── Border (same style as VascularStatusScreen / TendencyViewScreen) ─────

	private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
		int outer = 0xFF330808;
		gfx.fill(x, y, x + w, y + 1, outer);
		gfx.fill(x, y + h - 1, x + w, y + h, outer);
		gfx.fill(x, y, x + 1, y + h, outer);
		gfx.fill(x + w - 1, y, x + w, y + h, outer);

		int inner = 0xFF220606;
		gfx.fill(x + 1, y + 1, x + w - 1, y + 2, inner);
		gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, inner);
		gfx.fill(x + 1, y + 1, x + 2, y + h - 1, inner);
		gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, inner);
	}

	// ───── Procedural Animated Vein Background (matches VascularStatusScreen) ─────
	private float animTime = 0f;

	private void renderVeinBackground(GuiGraphics graphics, int gx, int gy, int gw, int gh) {
		graphics.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		graphics.fill(gx, gy, gx + gw, gy + gh, 0xFF0A0204);

		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 4) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (35 * (1f - t));
			int red = (int) (40 * (1f - t));
			int color = (alpha << 24) | (red << 16);
			graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}
		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
		if (veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawVeinTendril(graphics, i, time, gx, gy, gw, gh);
			}
		}

		Random speckRand = new Random(12345L);
		for (int s = 0; s < 120; s++) {
			int sx = gx + speckRand.nextInt(gw);
			int sy = gy + speckRand.nextInt(gh);
			int sr = 10 + speckRand.nextInt(20);
			int sg = speckRand.nextInt(6);
			int sa = 15 + speckRand.nextInt(25);
			graphics.fill(sx, sy, sx + 1, sy + 1, (sa << 24) | (sr << 16) | (sg << 8));
		}

		RenderSystem.disableBlend();
		graphics.disableScissor();
	}

	private void drawVeinTendril(GuiGraphics graphics, int index, float time,
								 int gx, int gy, int gw, int gh) {
		float[] p = veinParams[index];
		float startX = gx + p[0] * gw;
		float startY = gy + p[1] * gh;
		float baseAngle = p[2];
		float speed = p[3];
		float amplitude = p[4];
		float frequency = p[5];
		int length = (int) p[6];
		int thickness = (int) p[7];
		float brightness = p[8];

		float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.3f + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);
		float timeOffset = time * speed * 2.0f;

		int baseRed = (int) (40 + 50 * brightness);
		int baseGreen = (int) (2 + 8 * brightness);
		int baseBlue = (int) (5 + 5 * brightness);

		for (int step = 0; step < length; step++) {
			float t = step;
			float squiggle = amplitude * Mth.sin(frequency * t + timeOffset);
			float microSquiggle = (amplitude * 0.3f) * Mth.sin(frequency * 2.7f * t + timeOffset * 1.4f + index);
			float displacement = squiggle + microSquiggle;

			float px = startX + t * cosA * 1.5f - displacement * sinA;
			float py = startY + t * sinA * 1.5f + displacement * cosA;

			int ix = (int) px;
			int iy = (int) py;

			if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) continue;

			float tipFade = 1f;
			if (step < 10) tipFade = step / 10f;
			else if (step > length - 10) tipFade = (length - step) / 10f;

			float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.02f);
			int alpha = (int) (Mth.clamp(tipFade * pulse * 180, 20, 200));
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

			int color = (alpha << 24) | (r << 16) | (g << 8) | b;
			graphics.fill(ix, iy, ix + thickness, iy + thickness, color);
		}
	}

	/** Resolves a UUID to a display name using the client's player info list, falling back to the UUID string. */
	private static String resolvePlayerName(UUID uuid) {
		PlayerInfo info = Minecraft.getInstance().getConnection() != null
				? Minecraft.getInstance().getConnection().getPlayerInfo(uuid)
				: null;
		return info != null ? info.getProfile().getName() : uuid.toString();
	}

	private static List<UUID> getKickableMembers(LocalPlayer player) {
		List<UUID> kickable = new ArrayList<>();
		HemoCapabilityAccess.getBloodVolume(player).ifPresent(vol -> {
			Bloodline bloodline = vol.getBloodLine();
			for (UUID member : bloodline.getPlayerUUIDS()) {
				if (!member.equals(player.getUUID())) {
					kickable.add(member);
				}
			}
		});
		return kickable;
	}

	private static String formatPoolVolume(float amount) {
		return Math.abs(amount - Math.round(amount)) < 0.05f
				? String.format("%.0f", amount)
				: String.format("%.1f", amount);
	}

	private static final class CenteredEditBox extends EditBox {
		private CenteredEditBox(Font font, int x, int y, int width, int height, Component message) {
			super(font, x, y, width, height, message);
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			// BloodlinePoolScreen draws centered field text after all widgets render.
		}
	}

	private static class RitualButton extends Button {
		protected RitualButton(int x, int y, int width, int height, Component message, OnPress onPress) {
			super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			Minecraft minecraft = Minecraft.getInstance();
			boolean hot = isHoveredOrFocused();
			int fill = active ? (hot ? 0xFF68141D : 0xFF2B070B) : 0xFF160507;
			int edge = active ? (hot ? 0xFFFF7580 : 0xFF8A1A24) : 0xFF3A1114;
			graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF050101);
			graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, fill);
			graphics.fill(getX(), getY(), getX() + width, getY() + 1, edge);
			graphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFF210407);
			graphics.fill(getX(), getY(), getX() + 1, getY() + height, edge);
			graphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, edge);
			if (hot && active) {
				graphics.fill(getX() + 2, getY() + 2, getX() + width - 2, getY() + height - 2, 0x22FF9098);
			}
			drawCenteredFittingString(graphics, minecraft.font, getMessage().getString(), getX() + width / 2,
					getY() + (height - 8) / 2, Math.max(1, width - 8), active ? 0xFFFFE2DC : 0xFF8A6868,
					false);
		}
	}

	private static final class ToggleRitualButton extends RitualButton {
		private final String label;
		private final Consumer<Boolean> onToggle;
		private boolean selected;

		private ToggleRitualButton(int x, int y, int width, int height, String label, boolean selected,
				Consumer<Boolean> onToggle) {
			super(x, y, width, height, Component.empty(), btn -> {
			});
			this.label = label;
			this.selected = selected;
			this.onToggle = onToggle;
			updateLabel();
		}

		@Override
		public void onPress() {
			selected = !selected;
			onToggle.accept(selected);
			updateLabel();
		}

		private void updateLabel() {
			setMessage(Component.literal((selected ? "[x] " : "[ ] ") + label));
		}
	}
}
