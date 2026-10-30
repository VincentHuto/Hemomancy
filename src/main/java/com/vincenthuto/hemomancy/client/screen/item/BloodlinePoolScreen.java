package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.client.data.BloodlinePoolClientData;
import com.vincenthuto.hemomancy.client.data.FaneBoundaryClientData;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
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
import java.util.UUID;
import java.util.function.Consumer;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.util.HemoText;

/**
 * A standalone screen that lets the player view their bloodline's shared blood pool,
 * donate blood (lump or trickle), and configure auto-draw settings. The left column holds the pool in three titled
 * panels on one grid (each row an action on the left, then a label and field lined up on the right); the right column
 * holds the fane map with the fane-facing buttons under it.
 */
public class BloodlinePoolScreen extends Screen {

	// Layout, in unscaled pixels.
	private static final int GUI_WIDTH = 520;
	private static final int GUI_HEIGHT = 320;
	private static final int SCREEN_PADDING = 8;
	private static final int EDGE = 16;
	private static final int LEFT_WIDTH = 320;
	private static final int RIGHT_X = EDGE + LEFT_WIDTH + 10;
	private static final int RIGHT_WIDTH = GUI_WIDTH - RIGHT_X - EDGE;
	private static final int CONTENT_TOP = 36;
	private static final int POOL_TOP = CONTENT_TOP;
	private static final int POOL_HEIGHT = 104;
	private static final int TITHE_TOP = POOL_TOP + POOL_HEIGHT + 6;
	private static final int TITHE_HEIGHT = 86;
	private static final int KIN_TOP = TITHE_TOP + TITHE_HEIGHT + 6;
	private static final int KIN_HEIGHT = 66;
	private static final int CONTENT_BOTTOM = KIN_TOP + KIN_HEIGHT;
	/** A panel's first row sits under its heading; rows step by {@code ROW_STEP}. */
	private static final int FIRST_ROW = 18;
	private static final int ROW = 18;
	private static final int ROW_STEP = 22;
	private static final int INSET = 8;
	private static final int ACTION_WIDTH = 116;
	private static final int FIELD_WIDTH = 78;
	private static final int FIELD_X = EDGE + LEFT_WIDTH - INSET - FIELD_WIDTH;
	private static final int SEND_WIDTH = 64;
	private static final int BANISH_WIDTH = 72;
	private static final int RESIDENTS_TOP = CONTENT_BOTTOM - ROW;
	private static final int FANE_SIGHT_TOP = RESIDENTS_TOP - ROW_STEP;

	private static final int PANEL_FILL = 0xC0080203;
	private static final int PANEL_EDGE = 0xFF6A1218;
	private static final int HEADING = 0xFFFF8A8A;
	private static final int TEXT = 0xFFFFD9D0;
	private static final int LABEL = 0xFFCC9A9A;
	private static final int MUTED = 0xFFAA7A7A;

	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(VeinBackgroundRenderer.Style.CRIMSON);
	private FaneMapPanel faneMap;
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
	/** False outside a bloodline: the pool's controls are shown but have nothing to act on. */
	private boolean poolActive = true;

	// Cached capability data
	private boolean trickleEnabled;
	private double trickleRate;
	private boolean autoDrawEnabled;
	private double autoDrawThreshold;

	public BloodlinePoolScreen() {
		super(Component.translatable("screen.hemomancy.bloodline_pool.bloodline_pool"));
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
		faneMap = new FaneMapPanel(this.font);

		// Read current settings from capability
		LocalPlayer player = Minecraft.getInstance().player;
		Bloodline line = Bloodline.NOBLOODLINE;
		if (player != null) {
			var vol = HemoCapabilityAccess.getBloodVolume(player);
			trickleEnabled = vol.isTrickleEnabled();
			trickleRate = vol.getTrickleRate();
			autoDrawEnabled = vol.isAutoDrawEnabled();
			autoDrawThreshold = vol.getAutoDrawThreshold();
			line = vol.getBloodLine();
		}

		// ── Tithes: lump offering, trickle, recall ──
		int left = EDGE + INSET;
		int offerRow = TITHE_TOP + FIRST_ROW;
		int trickleRow = offerRow + ROW_STEP;
		int recallRow = trickleRow + ROW_STEP;

		donateButton = new RitualButton(px(left), py(offerRow), scaled(ACTION_WIDTH), widgetHeight(ROW),
				Component.translatable("screen.hemomancy.bloodline_pool.donate"), btn -> {
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
		donateAmountField = field(FIELD_X, offerRow, FIELD_WIDTH, "screen.hemomancy.bloodline_pool.amount");
		donateAmountField.setMaxLength(7);
		donateAmountField.setValue("100");
		donateAmountField.setFilter(s -> s.isEmpty() || s.matches("\\d*\\.?\\d*"));

		trickleToggleButton = new ToggleRitualButton(px(left), py(trickleRow), scaled(ACTION_WIDTH), widgetHeight(ROW),
				Component.translatable("screen.hemomancy.bloodline_pool.trickle_tithe"), trickleEnabled, selected -> {
			trickleEnabled = selected;
			sendPoolSettings();
		});
		addRenderableWidget(trickleToggleButton);
		trickleRateField = field(FIELD_X, trickleRow, FIELD_WIDTH, "screen.hemomancy.bloodline_pool.rate");
		trickleRateField.setMaxLength(6);
		trickleRateField.setValue(String.format("%.2f", trickleRate));
		trickleRateField.setFilter(s -> s.isEmpty() || s.matches("\\d*\\.?\\d*"));
		trickleRateField.setResponder(value -> sendPoolSettings());

		autoDrawToggleButton = new ToggleRitualButton(px(left), py(recallRow), scaled(ACTION_WIDTH), widgetHeight(ROW),
				Component.translatable("screen.hemomancy.bloodline_pool.blood_recall"), autoDrawEnabled, selected -> {
			autoDrawEnabled = selected;
			sendPoolSettings();
		});
		addRenderableWidget(autoDrawToggleButton);
		autoDrawThresholdField = field(FIELD_X, recallRow, FIELD_WIDTH, "screen.hemomancy.bloodline_pool.threshold");
		autoDrawThresholdField.setMaxLength(4);
		autoDrawThresholdField.setValue(String.format("%.0f", autoDrawThreshold * 100));
		autoDrawThresholdField.setFilter(s -> s.isEmpty() || s.matches("\\d*\\.?\\d*"));
		autoDrawThresholdField.setResponder(value -> sendPoolSettings());

		// ── Bloodline: message, then the Progenitor's banish row ──
		int messageRow = KIN_TOP + FIRST_ROW;
		int banishRow = messageRow + ROW_STEP;
		int right = EDGE + LEFT_WIDTH - INSET;
		messageField = field(left, messageRow, LEFT_WIDTH - INSET * 2 - SEND_WIDTH - 6,
				"screen.hemomancy.bloodline_pool.message");
		messageField.setMaxLength(256);
		messageField.setHint(Component.empty());

		sendMessageButton = new RitualButton(px(right - SEND_WIDTH), py(messageRow), scaled(SEND_WIDTH),
				widgetHeight(ROW), Component.translatable("screen.hemomancy.bloodline_pool.send"), btn -> {
			String msg = messageField.getValue().trim();
			if (!msg.isEmpty()) {
				PacketHandler.sendToServer(new PacketBloodlineMessage(msg));
				messageField.setValue("");
			}
		});
		addRenderableWidget(sendMessageButton);

		if (player != null && line.isValid() && player.getUUID().equals(line.getLeaderUUID())) {
			final LocalPlayer localPlayer = player;
			kickPrevButton = new RitualButton(px(left), py(banishRow), widgetHeight(ROW), widgetHeight(ROW),
					Component.literal("<"), btn -> {
				int size = getKickableMembers(localPlayer).size();
				if (size > 0) {
					kickTargetIndex = (kickTargetIndex - 1 + size) % size;
				}
			});
			addRenderableWidget(kickPrevButton);

			kickNextButton = new RitualButton(px(left + ROW + 4), py(banishRow), widgetHeight(ROW), widgetHeight(ROW),
					Component.literal(">"), btn -> {
				int size = getKickableMembers(localPlayer).size();
				if (size > 0) {
					kickTargetIndex = (kickTargetIndex + 1) % size;
				}
			});
			addRenderableWidget(kickNextButton);

			kickMemberButton = new RitualButton(px(right - BANISH_WIDTH), py(banishRow), scaled(BANISH_WIDTH),
					widgetHeight(ROW), Component.translatable("screen.hemomancy.bloodline_pool.banish"), btn -> {
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

		// ── Right column: the fane's own buttons under its map ──
		faneSightButton = new RitualButton(px(RIGHT_X), py(FANE_SIGHT_TOP), scaled(RIGHT_WIDTH), widgetHeight(ROW),
				faneSightLabel(FaneBoundaryClientData.viewMode()), btn -> {
			FaneBoundaryClientData.ViewMode mode = FaneBoundaryClientData.cycleViewMode();
			btn.setMessage(faneSightLabel(mode));
		});
		addRenderableWidget(faneSightButton);
		addRenderableWidget(new RitualButton(px(RIGHT_X), py(RESIDENTS_TOP), scaled(RIGHT_WIDTH), widgetHeight(ROW),
				Component.translatable("hemomancy.succession.residents"), btn -> SuccessionResidentsScreen.open(this)));

		// Outside a bloodline the pool controls have nothing to act on.
		poolActive = line.isValid();
		if (!poolActive) {
			donateButton.active = false;
			trickleToggleButton.active = false;
			autoDrawToggleButton.active = false;
			sendMessageButton.active = false;
			for (EditBox box : new EditBox[] {donateAmountField, trickleRateField, autoDrawThresholdField, messageField}) {
				box.setEditable(false);
			}
		}
	}

	/** A field that fills one grid cell; its drawn frame is the cell, so it lines up with the buttons beside it. */
	private EditBox field(int x, int y, int width, String key) {
		EditBox box = new CenteredEditBox(this.font, px(x) + 2, py(y) + 2, scaled(width) - 4, widgetHeight(ROW) - 4,
				Component.translatable(key));
		styleEditBox(box);
		addRenderableWidget(box);
		return box;
	}

	private int px(int x) {
		return guiLeft + scaled(x);
	}

	private int py(int y) {
		return guiTop + scaled(y);
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

		// Animated vein background
		animTime += 0.016f; // ~60 FPS approximation
		veins.draw(graphics, guiLeft, guiTop, currentGuiWidth, currentGuiHeight, animTime);

		// Border
		ScreenDrawUtils.drawBorder(graphics, guiLeft, guiTop, currentGuiWidth, currentGuiHeight, 0xFF330808, 0xFF220606);
		drawCornerSigils(graphics, guiLeft, guiTop, currentGuiWidth, currentGuiHeight);

		// Title, then the bloodline's name under it
		int centerX = guiLeft + currentGuiWidth / 2;
		graphics.drawCenteredString(this.font, this.title, centerX, py(8), 0xFFFF6A75);

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		Bloodline bloodline = volume.getBloodLine();
		graphics.drawCenteredString(this.font, bloodline.isValid()
				? Component.literal(bloodline.getName()).withStyle(s -> s.withColor(0xFFDD6666).withBold(true))
				: Component.translatable("screen.hemomancy.bloodline_pool.you_are_not_in_a_bloodline")
						.withStyle(s -> s.withColor(0xFFAA4444)), centerX, py(20), 0xFFFFFFFF);

		panel(graphics, EDGE, POOL_TOP, LEFT_WIDTH, POOL_HEIGHT, "screen.hemomancy.bloodline_pool.section.pool");
		panel(graphics, EDGE, TITHE_TOP, LEFT_WIDTH, TITHE_HEIGHT, "screen.hemomancy.bloodline_pool.section.tithes");
		panel(graphics, EDGE, KIN_TOP, LEFT_WIDTH, KIN_HEIGHT, "screen.hemomancy.bloodline_pool.section.bloodline");
		renderPool(graphics, volume, partialTick);
		renderTitheLabels(graphics);
		renderBanishRow(graphics, player, bloodline);
		List<Component> mapTooltip = new ArrayList<>();
		renderFaneMap(graphics, bloodline, mouseX, mouseY, partialTick, mapTooltip);

		// Render widgets on top
		renderEditBoxFrame(graphics, donateAmountField);
		renderEditBoxFrame(graphics, trickleRateField);
		renderEditBoxFrame(graphics, autoDrawThresholdField);
		renderEditBoxFrame(graphics, messageField);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderCenteredEditBoxText(graphics, donateAmountField, "100");
		renderCenteredEditBoxText(graphics, trickleRateField, "0.00");
		renderCenteredEditBoxText(graphics, autoDrawThresholdField, "0");
		renderCenteredEditBoxText(graphics, messageField,
				Component.translatable("screen.hemomancy.bloodline_pool.send_to_bloodline").getString());
		renderVesselTooltip(graphics, mouseX, mouseY);
		if (!mapTooltip.isEmpty()) graphics.renderComponentTooltip(this.font, mapTooltip, mouseX, mouseY);
	}

	/** A titled panel, matching the fane map's. */
	private void panel(GuiGraphics graphics, int x, int y, int w, int h, String headingKey) {
		int left = px(x), top = py(y);
		graphics.fill(left, top, left + scaled(w), top + scaled(h), PANEL_FILL);
		ScreenDrawUtils.drawSimpleBorder(graphics, left, top, scaled(w), scaled(h), PANEL_EDGE);
		graphics.drawString(this.font, Component.translatable(headingKey), left + 5, top + 5, HEADING, true);
	}

	/** The shared pool's sphere, and beside it the player's own vessel and the bloodline's size. */
	private void renderPool(GuiGraphics graphics, IBloodVolume volume, float partialTick) {
		float poolVol = BloodlinePoolClientData.getPoolVolume();
		float poolMax = BloodlinePoolClientData.getPoolMax();
		float ratio = poolMax > 0 ? Mth.clamp(poolVol / poolMax, 0f, 1f) : 0f;
		String poolText = Component.translatable("screen.hemomancy.bloodline_pool.pool_volume_ml",
				formatPoolVolume(poolVol), String.format("%.0f", poolMax)).getString();
		renderBloodSphere(graphics, px(EDGE + 100), py(POOL_TOP + 58), scaled(36), ratio, poolText, partialTick);

		int barX = EDGE + 172, barY = POOL_TOP + FIRST_ROW + 4;
		renderVesselBloodBar(graphics, px(barX), py(barY), volume.getBloodVolume(), volume.getMaxBloodVolume());
		int textX = px(barX + 16);
		graphics.drawString(this.font, Component.translatable("screen.hemomancy.bloodline_pool.vessel"), textX,
				py(barY + 2), LABEL, false);
		graphics.drawString(this.font, Component.translatable("screen.hemomancy.bloodline_pool.pool_volume_ml",
				formatPoolVolume((float) volume.getBloodVolume()), String.format("%.0f", volume.getMaxBloodVolume())),
				textX, py(barY + 14), TEXT, false);
		graphics.drawString(this.font, HemoText.translatable("screen.hemomancy.bloodline_pool.members",
				BloodlinePoolClientData.getMemberCount()), textX, py(barY + 38), MUTED, false);
	}

	/** Each field's label, right-aligned against it, centred on the row. */
	private void renderTitheLabels(GuiGraphics graphics) {
		String[] keys = {"screen.hemomancy.bloodline_pool.offer_ml", "screen.hemomancy.bloodline_pool.rate_label",
				"screen.hemomancy.bloodline_pool.threshold_percent_label"};
		int labelRight = px(FIELD_X) - scaled(6);
		for (int i = 0; i < keys.length; i++) {
			Component label = Component.translatable(keys[i]);
			int y = py(TITHE_TOP + FIRST_ROW + i * ROW_STEP) + (widgetHeight(ROW) - 8) / 2;
			graphics.drawString(this.font, label, labelRight - this.font.width(label), y, LABEL, false);
		}
	}

	/** The Progenitor picks a member to banish; everyone else sees who leads the line. */
	private void renderBanishRow(GuiGraphics graphics, LocalPlayer player, Bloodline bloodline) {
		int rowY = py(KIN_TOP + FIRST_ROW + ROW_STEP);
		if (kickMemberButton == null) {
			if (bloodline.isValid()) {
				graphics.drawString(this.font, Component.translatable("screen.hemomancy.bloodline_pool.progenitor",
						resolvePlayerName(bloodline.getLeaderUUID())), px(EDGE + INSET),
						rowY + (widgetHeight(ROW) - 8) / 2, MUTED, false);
			}
			return;
		}
		List<UUID> kickable = getKickableMembers(player);
		int targetX = kickNextButton.getX() + kickNextButton.getWidth() + scaled(6);
		int targetW = Math.max(48, kickMemberButton.getX() - targetX - scaled(6));
		renderRitualPanel(graphics, targetX, rowY, targetW, kickMemberButton.getHeight(), 0xFF050101, PANEL_EDGE);
		boolean any = !kickable.isEmpty();
		kickMemberButton.active = any;
		kickPrevButton.active = any;
		kickNextButton.active = any;
		String target;
		int colour;
		if (any) {
			kickTargetIndex = Mth.clamp(kickTargetIndex, 0, kickable.size() - 1);
			target = resolvePlayerName(kickable.get(kickTargetIndex));
			colour = 0xFFFFC8C8;
		} else {
			target = Component.translatable("screen.hemomancy.bloodline_pool.no_player_members").getString();
			colour = 0xFFAA6666;
		}
		drawCenteredFittingString(graphics, this.font, target, targetX + targetW / 2,
				rowY + (kickMemberButton.getHeight() - 8) / 2, targetW - scaled(6), colour, false);
	}

	/** The fane map, filling the right column above Fane Sight and Residents. */
	private void renderFaneMap(GuiGraphics graphics, Bloodline bloodline, int mouseX, int mouseY, float partialTick,
			List<Component> tooltip) {
		int top = py(CONTENT_TOP);
		int h = py(FANE_SIGHT_TOP) - scaled(6) - top;
		faneMap.render(graphics, px(RIGHT_X), top, scaled(RIGHT_WIDTH), h,
				bloodline.isValid() ? bloodline.getLeaderUUID() : null, mouseX, mouseY, partialTick, tooltip);
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
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		BloodVolumeBarWidget.renderTooltip(graphics, this.font, vesselBarBounds,
						volume.getBloodVolume(), volume.getMaxBloodVolume(), mouseX, mouseY);
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
		int color = showingHint || !poolActive ? 0xFF7A4C4C : 0xFFFFD9D0;
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

	// Animation clock: the veins, the pool fill and the caret blink read it.
	private float animTime = 0f;

	/** Resolves a UUID to a display name using the client's player info list, falling back to the UUID string. */
	private static String resolvePlayerName(UUID uuid) {
		PlayerInfo info = Minecraft.getInstance().getConnection() != null
				? Minecraft.getInstance().getConnection().getPlayerInfo(uuid)
				: null;
		return info != null ? info.getProfile().getName() : uuid.toString();
	}

	private static List<UUID> getKickableMembers(LocalPlayer player) {
		List<UUID> kickable = new ArrayList<>();
		var vol = HemoCapabilityAccess.getBloodVolume(player);
		Bloodline bloodline = vol.getBloodLine();
		for (UUID member : bloodline.getPlayerUUIDS()) {
			if (!member.equals(player.getUUID())) {
				kickable.add(member);
			}
		}
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

	private static Component faneSightLabel(FaneBoundaryClientData.ViewMode mode) {
		return Component.translatable("screen.hemomancy.bloodline_pool.fane_sight."
				+ mode.name().toLowerCase(java.util.Locale.ROOT));
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
		private final Component label;
		private final Consumer<Boolean> onToggle;
		private boolean selected;

		private ToggleRitualButton(int x, int y, int width, int height, Component label, boolean selected,
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
			setMessage(Component.translatable(selected ? "screen.hemomancy.bloodline_pool.toggle_on"
					: "screen.hemomancy.bloodline_pool.toggle_off", label));
		}
	}
}
