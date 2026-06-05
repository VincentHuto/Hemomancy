package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Random;

/**
	 * The Ancestral Ledger screen provides GUI buttons for bloodline sanctum actions:
 * <ul>
	 *   <li>Summon recruited NPC Harbingers (within the Founding Sanctum)</li>
	 *   <li>Recall to the sanctum recall point (from anywhere)</li>
	 *   <li>Set the sanctum recall point to current position (leader only, within the Founding Sanctum)</li>
 * </ul>
 * Matches the visual style of BloodlinePoolScreen with procedural vein background.
 */
public class LedgerScreen extends Screen {

	private static final int GUI_WIDTH = 200;
	private static final int GUI_HEIGHT = 180;
	private static final int VEIN_COUNT = 18;

	private float[][] veinParams;

	public LedgerScreen() {
		super(Component.literal("Ancestral Ledger"));
	}

	public static void openScreen() {
		Minecraft.getInstance().setScreen(new LedgerScreen());
	}

	@Override
	protected void init() {
		super.init();

		Random rand = new Random(77L);
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

		int btnW = GUI_WIDTH - 24;
		int btnX = guiLeft + 12;
		int y = guiTop + 70;

		// ── Summon NPCs Button ──
		addRenderableWidget(Button.builder(
				Component.literal("\u2720 Summon Harbingers"),
				btn -> {
					PacketHandler.sendToServer(
							new PacketLedgerAction(PacketLedgerAction.ACTION_SUMMON_NPCS));
					onClose();
				}).bounds(btnX, y, btnW, 20).build());

		y += 28;

		// ── Recall to Sanctum Button ──
		addRenderableWidget(Button.builder(
				Component.literal("\u2302 Recall to Sanctum"),
				btn -> {
					PacketHandler.sendToServer(
							new PacketLedgerAction(PacketLedgerAction.ACTION_RECALL_TO_LODGE));
					onClose();
				}).bounds(btnX, y, btnW, 20).build());

		y += 28;

		// ── Set Sanctum Recall Point Button (leader only) ──
		addRenderableWidget(Button.builder(
				Component.literal("\u2691 Set Sanctum Recall"),
				btn -> {
					PacketHandler.sendToServer(
							new PacketLedgerAction(PacketLedgerAction.ACTION_SET_RECALL_POINT));
					onClose();
				}).bounds(btnX, y, btnW, 20).build());

		y += 28;

		// ── Disband Bloodline Button (leader only) ──
		addRenderableWidget(Button.builder(
				Component.literal("\u2620 Disband Bloodline"),
				btn -> {
					PacketHandler.sendToServer(
							new PacketLedgerAction(PacketLedgerAction.ACTION_DISBAND_BLOODLINE));
					onClose();
				}).bounds(btnX, y, btnW, 20).build());
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics, mouseX, mouseY, partialTick);

		int centerX = this.width / 2;
		int centerY = this.height / 2;
		int guiLeft = centerX - GUI_WIDTH / 2;
		int guiTop = centerY - GUI_HEIGHT / 2;

		// Animated vein background
		renderVeinBackground(graphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);

		// Border
		drawBorder(graphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);

		// Title
		graphics.drawCenteredString(this.font, this.title, centerX, guiTop + 8, 0xFFCC3344);

		// Bloodline info
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
				Bloodline bloodline = volume.getBloodLine();
				if (bloodline.isValid()) {
					graphics.drawCenteredString(this.font,
							Component.literal(bloodline.getName())
									.withStyle(s -> s.withColor(0xFFDD6666).withBold(true)),
							centerX, guiTop + 24, 0xFFFFFFFF);

					int npcCount = bloodline.getNpcMemberCount();
					int memberCount = bloodline.getPlayerUUIDS().size();
					graphics.drawCenteredString(this.font,
							Component.literal("Members: " + memberCount + "  |  Recruits: " + npcCount),
							centerX, guiTop + 38, 0xFFAAAAAA);

					boolean isLeader = bloodline.getLeaderUUID().equals(player.getUUID());
					String role = isLeader ? "Leader" : "Member";
					graphics.drawCenteredString(this.font,
							Component.literal("Role: " + role),
							centerX, guiTop + 52, 0xFF888888);
				} else {
					graphics.drawCenteredString(this.font,
							Component.literal("No bloodline — sign the ledger first."),
							centerX, guiTop + 40, 0xFFAA4444);
				}
			});
		}

		// Render widgets on top
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	// ───── Border (matches BloodlinePoolScreen / VascularStatusScreen style) ─────

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

	// ───── Procedural Animated Vein Background (matches BloodlinePoolScreen) ─────
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
		for (int s = 0; s < 80; s++) {
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
			int alphaVal = (int) (Mth.clamp(tipFade * pulse * 180, 20, 200));
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

			int color = (alphaVal << 24) | (r << 16) | (g << 8) | b;
			graphics.fill(ix, iy, ix + thickness, iy + thickness, color);
		}
	}
}
