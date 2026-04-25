package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.data.BloodlinePoolClientData;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketBloodlineMessage;
import com.vincenthuto.hemomancy.common.network.capa.PacketKickBloodlinePlayer;
import com.vincenthuto.hemomancy.common.network.capa.PacketLumpDonate;
import com.vincenthuto.hemomancy.common.network.capa.PacketRequestPoolData;
import com.vincenthuto.hemomancy.common.network.capa.PacketUpdatePoolSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
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

/**
 * A standalone screen that lets the player view their bloodline's shared blood pool,
 * donate blood (lump or trickle), and configure auto-draw settings.
 */
public class BloodlinePoolScreen extends Screen {

	private static final int GUI_WIDTH = 220;
	private static final int GUI_HEIGHT = 320;
	private static final int VEIN_COUNT = 28;

	private float[][] veinParams;

	// Widgets
	private EditBox donateAmountField;
	private Button donateButton;
	private Checkbox trickleCheckbox;
	private EditBox trickleRateField;
	private Checkbox autoDrawCheckbox;
	private EditBox autoDrawThresholdField;
	private Button applySettingsButton;
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
		PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketRequestPoolData());
		Minecraft.getInstance().setScreen(new BloodlinePoolScreen());
	}

	@Override
	protected void init() {
		super.init();

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

		int centerX = this.width / 2;
		int guiLeft = centerX - GUI_WIDTH / 2;
		int guiTop = this.height / 2 - GUI_HEIGHT / 2;

		// Read current settings from capability
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(vol -> {
				trickleEnabled = vol.isTrickleEnabled();
				trickleRate = vol.getTrickleRate();
				autoDrawEnabled = vol.isAutoDrawEnabled();
				autoDrawThreshold = vol.getAutoDrawThreshold();
			});
		}

		int widgetX = guiLeft + 12;
		int widgetW = GUI_WIDTH - 24;
		int y = guiTop + 100;

		// ── Lump Donate Section ──
		donateAmountField = new EditBox(this.font, widgetX, y, widgetW - 62, 18, Component.literal("Amount"));
		donateAmountField.setMaxLength(7);
		donateAmountField.setValue("100");
		donateAmountField.setFilter(s -> s.isEmpty() || s.matches("\\d*\\.?\\d*"));
		addRenderableWidget(donateAmountField);

		donateButton = Button.builder(Component.literal("Donate"), btn -> {
			try {
				double amount = Double.parseDouble(donateAmountField.getValue());
				if (amount > 0) {
					PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketLumpDonate(amount));
					// Refresh pool data after a short delay (server processes first)
					PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketRequestPoolData());
				}
			} catch (NumberFormatException ignored) {
			}
		}).bounds(widgetX + widgetW - 58, y, 58, 18).build();
		addRenderableWidget(donateButton);

		y += 32;

		// ── Trickle Donation Section ──
		trickleCheckbox = new Checkbox(widgetX, y, 20, 20,
				Component.literal("Trickle Donate"), trickleEnabled);
		addRenderableWidget(trickleCheckbox);

		y += 22;

		trickleRateField = new EditBox(this.font, widgetX + 50, y, 60, 16, Component.literal("Rate"));
		trickleRateField.setMaxLength(6);
		trickleRateField.setValue(String.format("%.2f", trickleRate));
		trickleRateField.setFilter(s -> s.isEmpty() || s.matches("\\d*\\.?\\d*"));
		addRenderableWidget(trickleRateField);

		y += 26;

		// ── Auto-Draw Section ──
		autoDrawCheckbox = new Checkbox(widgetX, y, 20, 20,
				Component.literal("Auto-Draw"), autoDrawEnabled);
		addRenderableWidget(autoDrawCheckbox);

		y += 22;

		autoDrawThresholdField = new EditBox(this.font, widgetX + 70, y, 50, 16, Component.literal("Threshold"));
		autoDrawThresholdField.setMaxLength(4);
		autoDrawThresholdField.setValue(String.format("%.0f", autoDrawThreshold * 100));
		autoDrawThresholdField.setFilter(s -> s.isEmpty() || s.matches("\\d*\\.?\\d*"));
		addRenderableWidget(autoDrawThresholdField);

		y += 26;

		// ── Apply Settings Button ──
		applySettingsButton = Button.builder(Component.literal("Apply Settings"), btn -> {
			boolean trickle = trickleCheckbox.selected();
			double rate = 0.5;
			try {
				rate = Double.parseDouble(trickleRateField.getValue());
			} catch (NumberFormatException ignored) {
			}
			boolean autoDraw = autoDrawCheckbox.selected();
			double threshold = 0.25;
			try {
				threshold = Double.parseDouble(autoDrawThresholdField.getValue()) / 100.0;
			} catch (NumberFormatException ignored) {
			}
			PacketHandler.CHANNELBLOODVOLUME.sendToServer(
					new PacketUpdatePoolSettings(trickle, rate, autoDraw, threshold));
		}).bounds(widgetX, y, widgetW, 20).build();
		addRenderableWidget(applySettingsButton);

		y += 34;

		// ── Bloodline Message Section ──
		messageField = new EditBox(this.font, widgetX, y, widgetW - 58, 18, Component.literal("Message"));
		messageField.setMaxLength(256);
		messageField.setHint(Component.literal("Send to bloodline...").withStyle(s -> s.withColor(0xFF664444)));
		addRenderableWidget(messageField);

		sendMessageButton = Button.builder(Component.literal("Send"), btn -> {
			String msg = messageField.getValue().trim();
			if (!msg.isEmpty()) {
				PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketBloodlineMessage(msg));
				messageField.setValue("");
			}
		}).bounds(widgetX + widgetW - 54, y, 54, 18).build();
		addRenderableWidget(sendMessageButton);

		y += 28;

		// ── Leader Member Management ──
		if (player != null) {
				final LocalPlayer localPlayer = player;
			final int kickRowY = y;
			final int kickPrevX = widgetX;
			final int kickNextX = widgetX + 98;
			final int kickButtonX = widgetX + 122;
			final int kickButtonW = widgetW - 122;
			player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(vol -> {
				Bloodline line = vol.getBloodLine();
				if (line.isValid() && localPlayer.getUUID().equals(line.getLeaderUUID())) {
					kickPrevButton = Button.builder(Component.literal("<"), btn -> {
						int size = getKickableMembers(localPlayer).size();
						if (size > 0) {
							kickTargetIndex = (kickTargetIndex - 1 + size) % size;
						}
					}).bounds(kickPrevX, kickRowY, 18, 18).build();
					addRenderableWidget(kickPrevButton);

					kickNextButton = Button.builder(Component.literal(">"), btn -> {
						int size = getKickableMembers(localPlayer).size();
						if (size > 0) {
							kickTargetIndex = (kickTargetIndex + 1) % size;
						}
					}).bounds(kickNextX, kickRowY, 18, 18).build();
					addRenderableWidget(kickNextButton);

					kickMemberButton = Button.builder(Component.literal("Kick"), btn -> {
						List<UUID> kickable = getKickableMembers(localPlayer);
						if (!kickable.isEmpty()) {
							int idx = Mth.clamp(kickTargetIndex, 0, kickable.size() - 1);
							PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketKickBloodlinePlayer(kickable.get(idx)));
							PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketRequestPoolData());
							kickTargetIndex = 0;
						}
					}).bounds(kickButtonX, kickRowY, kickButtonW, 18).build();
					addRenderableWidget(kickMemberButton);
				}
			});
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);

		int centerX = this.width / 2;
		int centerY = this.height / 2;
		int guiLeft = centerX - GUI_WIDTH / 2;
		int guiTop = centerY - GUI_HEIGHT / 2;

		// Animated vein background
		renderVeinBackground(graphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);

		// Border
		drawBorder(graphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);

		// Title
		graphics.drawCenteredString(this.font, this.title, centerX, guiTop + 6, 0xFFCC3344);

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
			Bloodline bloodline = volume.getBloodLine();

			if (!bloodline.isValid()) {
				graphics.drawCenteredString(this.font,
						Component.literal("You are not in a bloodline."),
						centerX, guiTop + 40, 0xFFAA4444);
				return;
			}

			int y = guiTop + 22;

			// Bloodline name
			graphics.drawCenteredString(this.font,
					Component.literal(bloodline.getName()).withStyle(s -> s.withColor(0xFFDD6666).withBold(true)),
					centerX, y, 0xFFFFFFFF);
			y += 14;

			// Member count
			graphics.drawCenteredString(this.font,
					Component.literal("Members: " + BloodlinePoolClientData.getMemberCount()),
					centerX, y, 0xFFAAAAAA);
			y += 14;

			// Pool volume bar
			float poolVol = BloodlinePoolClientData.getPoolVolume();
			float poolMax = BloodlinePoolClientData.getPoolMax();
			float ratio = poolMax > 0 ? Mth.clamp(poolVol / poolMax, 0f, 1f) : 0f;

			int barX = guiLeft + 12;
			int barW = GUI_WIDTH - 24;
			int barH = 14;

			// Bar background
			graphics.fill(barX, y, barX + barW, y + barH, 0xFF1A0505);
			// Bar fill
			int fillW = (int) (barW * ratio);
			int barColor = lerpColor(ratio);
			graphics.fill(barX, y, barX + fillW, y + barH, barColor);
			// Bar border
			graphics.fill(barX, y, barX + barW, y + 1, 0xFF440808);
			graphics.fill(barX, y + barH - 1, barX + barW, y + barH, 0xFF440808);
			graphics.fill(barX, y, barX + 1, y + barH, 0xFF440808);
			graphics.fill(barX + barW - 1, y, barX + barW, y + barH, 0xFF440808);

			// Pool text
			String poolText = String.format("%.0f / %.0f ml", poolVol, poolMax);
			graphics.drawCenteredString(this.font, poolText, centerX, y + 3, 0xFFFFFFFF);
			y += barH + 8;

			// Your blood bar
			String yourBlood = String.format("Your Blood: %.0f / %.0f ml",
					volume.getBloodVolume(), volume.getMaxBloodVolume());
			graphics.drawCenteredString(this.font, yourBlood, centerX, y, 0xFFCC8888);
			y += 16;

			// Label for donate field
			graphics.drawString(this.font, "Donate (ml):", guiLeft + 12, guiTop + 89, 0xFFCC6666, true);

			// Trickle rate label
			graphics.drawString(this.font, "Rate:", guiLeft + 12,
					trickleRateField.getY() + 4, 0xFFCC6666, true);

			// Auto-draw threshold label
			graphics.drawString(this.font, "Threshold %:", guiLeft + 12,
					autoDrawThresholdField.getY() + 4, 0xFFCC6666, true);

			// Bloodline message label
			graphics.drawString(this.font, "Bloodline Message:", guiLeft + 12,
					messageField.getY() - 11, 0xFFCC6666, true);

			if (kickMemberButton != null) {
				List<UUID> kickable = getKickableMembers(player);
				if (kickable.isEmpty()) {
					graphics.drawString(this.font, "No player members to expel.", guiLeft + 22,
							kickMemberButton.getY() + 5, 0xFFAA6666, false);
					kickMemberButton.active = false;
					kickPrevButton.active = false;
					kickNextButton.active = false;
				} else {
					kickMemberButton.active = true;
					kickPrevButton.active = true;
					kickNextButton.active = true;
					kickTargetIndex = Mth.clamp(kickTargetIndex, 0, kickable.size() - 1);
					String shortTarget = resolvePlayerName(kickable.get(kickTargetIndex));
					graphics.drawString(this.font, "Expel Player:", guiLeft + 22,
							kickMemberButton.getY() + 1, 0xFFCC6666, true);
					graphics.drawString(this.font, shortTarget, guiLeft + 22,
							kickMemberButton.getY() + 10, 0xFFCC8888, false);
				}
			}
		});

		// Render widgets on top
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	// ───── Color Lerp for Pool Bar ─────

	private int lerpColor(float ratio) {
		// Empty = dark red, full = bright red/gold
		int r = (int) Mth.lerp(ratio, 100, 220);
		int g = (int) Mth.lerp(ratio, 10, 60);
		int b = (int) Mth.lerp(ratio, 10, 20);
		return 0xFF000000 | (r << 16) | (g << 8) | b;
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

		float time = com.vincenthuto.hemomancy.client.screen.util.ScreenAnimationClock.tick();
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
		player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(vol -> {
			Bloodline bloodline = vol.getBloodLine();
			for (UUID member : bloodline.getPlayerUUIDS()) {
				if (!member.equals(player.getUUID())) {
					kickable.add(member);
				}
			}
		});
		return kickable;
	}
}
