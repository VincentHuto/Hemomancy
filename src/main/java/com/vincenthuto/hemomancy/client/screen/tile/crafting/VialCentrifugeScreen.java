package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.StartCentrifugeButtonPacket;
import com.vincenthuto.hemomancy.common.tile.ReservoirReading;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeRotorRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeStartupResult;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Optional;

/**
 * Vial Centrifuge screen, drawn as the swing-bucket bench machine: the reservoir jar on the left (flask in above,
 * empty flask out below), the rotor in its splash bowl in the middle with the start button on the hub, the
 * fraction receivers on the right fed by the bowl's spout, and the powder tray under the bowl. Each opposite
 * pair of buckets is joined by its rotor arm, which warns while only one side is loaded. During a spin the hub
 * turns with the block entity's own rotor, the buckets swing out with its speed and the bowl rim fills with
 * progress. Fully programmatic, no texture files.
 */
public class VialCentrifugeScreen extends AbstractContainerScreen<VialCentrifugeMenu> {

	// ── Colors (matching the Hemomancy blood-themed style) ──
	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;
	private static final int IRON_DARK = 0xFF1E0C0B;
	private static final int IRON = 0xFF3A1A18;
	private static final int IRON_LIGHT = 0xFF5A2A26;
	private static final int ARM_LOADED = 0xFF8A1A1A;
	private static final int WARN = 0xFFE07028;
	private static final int WARN_DIM = 0xFFA83A18;

	private static final int CRAFT_AREA_HEIGHT = 120;

	// Rotor geometry (GUI-relative); the bucket slots sit at BUCKET_RADIUS in VialCentrifugeMenu
	private static final int ROTOR_CX = 80;
	private static final int ROTOR_CY = 54;
	private static final int BUCKET_RADIUS = 28;
	private static final int BOWL_RADIUS = 42;
	private static final int RIM_WIDTH = 3;
	private static final int HUB_RADIUS = 8;

	// Reservoir jar and fraction rack (GUI-relative)
	private static final int JAR_X = 15, JAR_Y = 40, JAR_W = 10, JAR_H = 50;
	private static final int RACK_X1 = 126, RACK_Y1 = 18, RACK_X2 = 168, RACK_Y2 = 96;
	/** Faint strata behind the receiver rows, light plasma on top to dense red below. */
	private static final int[] STRATA = {0x24E0B070, 0x22C88468, 0x22A03030, 0x26601010};

	final VialCentrifugeBlockEntity te;
	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(VeinBackgroundRenderer.Style.MACHINE);

	private BloodVolumeBarWidget.Bounds bloodBarBounds = BloodVolumeBarWidget.Bounds.EMPTY;
	private int btnX1, btnY1, btnX2, btnY2;
	private float animTime = 0f;

	public VialCentrifugeScreen(VialCentrifugeMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.te = menu.getTe();
		this.imageWidth = 176;
		this.imageHeight = 216;
		this.inventoryLabelY = CRAFT_AREA_HEIGHT + 3;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = 6;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	// ───── Main render ─────

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);

		BloodVolumeBarWidget.renderTooltip(graphics, font, bloodBarBounds, ReservoirReading.of(te), mouseX, mouseY);

		if (!this.menu.isSpinning() && overButton(mouseX, mouseY)) {
			graphics.renderTooltip(font, List.of(Component.translatable("message.hemomancy.vial_centrifuge.start")),
					Optional.empty(), mouseX, mouseY);
		}
		if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && !this.hoveredSlot.hasItem()) {
			int index = this.menu.slots.indexOf(this.hoveredSlot);
			if (isBucket(index) && needsCounterweight(index)) {
				graphics.renderTooltip(font, List.of(
						Component.translatable("message.hemomancy.vial_centrifuge.counterweight")),
						Optional.empty(), mouseX, mouseY);
			}
		}
		Component status = statusText();
		if (status != null && font.width(status) > statusWidth()) {
			int sy = this.topPos + this.inventoryLabelY;
			if (mouseX >= this.leftPos + 4 && mouseX < this.leftPos + this.imageWidth - 4 && mouseY >= sy && mouseY < sy + 9)
				graphics.renderTooltip(font, font.split(status, 200), mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && !this.menu.isSpinning() && overButton(mouseX, mouseY)) {
			PacketHandler.sendToServer(new StartCentrifugeButtonPacket());
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private boolean overButton(double mouseX, double mouseY) {
		return mouseX >= btnX1 && mouseX < btnX2 && mouseY >= btnY1 && mouseY < btnY2;
	}

	@Override
	protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
		animTime += 0.004f;
		int gx = this.leftPos;
		int gy = this.topPos;

		veins.draw(gfx, gx, gy, this.imageWidth, CRAFT_AREA_HEIGHT, animTime);
		ScreenDrawUtils.drawBorder(gfx, gx, gy, this.imageWidth, CRAFT_AREA_HEIGHT, BORDER_OUTER, BORDER_INNER);

		Slot firstInventorySlot = this.menu.slots.get(VialCentrifugeMenu.SLOT_COUNT);
		InventoryPanelTextures.blit(gfx, InventoryPanelTextures.BLOODY,
				gx + firstInventorySlot.x - 5, gy + firstInventorySlot.y - 6);

		float speed = VialCentrifugeRotorRules.speedFraction(te.upgradeTier(),
				Mth.lerp(partialTicks, te.prevRotorSpeed, te.rotorSpeed));
		float angle = Mth.lerp(partialTicks, te.prevRotorAngle, te.rotorAngle) * Mth.DEG_TO_RAD;

		renderBowl(gfx, gx + ROTOR_CX, gy + ROTOR_CY);
		renderSpout(gfx, gx, gy);
		renderRack(gfx, gx, gy);
		renderTray(gfx, gx, gy);
		renderJar(gfx, gx, gy);
		renderArms(gfx, gx, gy, speed, angle);
		renderBuckets(gfx, gx, gy, speed);

		for (int i = 0; i < VialCentrifugeMenu.SLOT_COUNT; i++) {
			Slot slot = this.menu.slots.get(i);
			drawSlotBackground(gfx, gx + slot.x, gy + slot.y, i);
		}

		renderCounterweightWarnings(gfx, gx, gy);
		renderHub(gfx, gx + ROTOR_CX, gy + ROTOR_CY, mouseX, mouseY, speed, angle);
	}

	@Override
	protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
		gfx.drawString(font, this.title, this.titleLabelX, 5, 0xFFAA2222, false);
		Component status = statusText();
		if (status == null) return;
		int color = this.menu.isSpinning() ? 0xFFCC2222 : 0xFFFF7777;
		String shown = ScreenDrawUtils.truncateText(font, status.getString(), statusWidth());
		gfx.drawString(font, shown, 6, this.inventoryLabelY, color, false);
	}

	private int statusWidth() {
		return this.imageWidth - 12;
	}

	/** What the machine is doing, or why it would not start; null when there is nothing to say. */
	private Component statusText() {
		if (this.menu.isSpinning()) return Component.translatable("message.hemomancy.vial_centrifuge.spinning");
		VialCentrifugeStartupResult result = this.menu.getStartupResult();
		if (result == VialCentrifugeStartupResult.IDLE || result == VialCentrifugeStartupResult.SUCCESS) return null;
		return Component.translatable(result.translationKey());
	}

	// ───── Balance ─────

	private static boolean isBucket(int index) {
		return index >= VialCentrifugeMenu.VIAL_START && index <= VialCentrifugeMenu.VIAL_END;
	}

	/** The bucket across the rotor from this one; the block entity balances the same pairs. */
	private static int opposite(int index) {
		return VialCentrifugeMenu.VIAL_START + (index - VialCentrifugeMenu.VIAL_START + 4) % 8;
	}

	private boolean loaded(int index) {
		return this.menu.slots.get(index).hasItem();
	}

	/** An empty bucket whose opposite is loaded: the rotor will not start until it takes a counterweight. */
	private boolean needsCounterweight(int index) {
		return !loaded(index) && loaded(opposite(index));
	}

	// ───── Splash bowl and progress rim ─────

	private void renderBowl(GuiGraphics gfx, int cx, int cy) {
		fillDisc(gfx, cx, cy, BOWL_RADIUS, IRON_DARK);
		fillDisc(gfx, cx, cy, BOWL_RADIUS - RIM_WIDTH, 0xF00A0304);
		// Inner shadow so the bowl reads as a basin, not a plate
		fillRing(gfx, cx, cy, BOWL_RADIUS - RIM_WIDTH - 2, BOWL_RADIUS - RIM_WIDTH, 0x60000000);

		double progress = this.menu.isSpinning() ? this.menu.getSpinProgress() / 24.0 : 0;
		double lead = -Math.PI / 2 + progress * Math.PI * 2;
		int inner = BOWL_RADIUS - RIM_WIDTH;
		for (int dy = -BOWL_RADIUS; dy <= BOWL_RADIUS; dy++) {
			for (int dx = -BOWL_RADIUS; dx <= BOWL_RADIUS; dx++) {
				int d2 = dx * dx + dy * dy;
				if (d2 > BOWL_RADIUS * BOWL_RADIUS || d2 < inner * inner) continue;
				double a = Math.atan2(dy, dx);
				double fromTop = a + Math.PI / 2;
				if (fromTop < 0) fromTop += Math.PI * 2;
				int color;
				if (fromTop < progress * Math.PI * 2) {
					float pulse = 0.7f + 0.3f * Mth.sin(animTime * 3f + (float) fromTop * 2f);
					color = 0xDD000000 | ((int) (200 * pulse) << 16) | ((int) (20 * pulse) << 8) | 0x15;
				} else {
					// Lit from above: the top of the rim catches more light than the bottom
					color = dy < 0 ? IRON : 0xFF2A1210;
				}
				gfx.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
			}
		}
		if (progress > 0 && progress < 1) {
			int r = BOWL_RADIUS - RIM_WIDTH / 2 - 1;
			int lx = cx + (int) Math.round(r * Math.cos(lead));
			int ly = cy + (int) Math.round(r * Math.sin(lead));
			float pulse = 0.5f + 0.5f * Mth.sin(animTime * 5f);
			gfx.fill(lx - 1, ly - 1, lx + 2, ly + 2, ((int) (150 * pulse) << 24) | 0xFF3020);
		}
	}

	/** The pour spout from the bowl's upper right into the receiver rack. */
	private void renderSpout(GuiGraphics gfx, int gx, int gy) {
		int x0 = gx + ROTOR_CX + 30, y0 = gy + ROTOR_CY - 30;
		int x3 = gx + RACK_X1 + 2, y3 = gy + RACK_Y1 + 2;
		ScreenDrawUtils.drawCubicTrace(gfx, x0, y0, x0 + 6, y0 - 8, x3 - 8, y3 - 4, x3, y3, 4, IRON_DARK);
		ScreenDrawUtils.drawCubicTrace(gfx, x0, y0, x0 + 6, y0 - 8, x3 - 8, y3 - 4, x3, y3, 2, 0xFF4A1010);
	}

	// ───── Fraction receivers ─────

	private void renderRack(GuiGraphics gfx, int gx, int gy) {
		int x1 = gx + RACK_X1, y1 = gy + RACK_Y1, x2 = gx + RACK_X2, y2 = gy + RACK_Y2;
		gfx.fill(x1, y1, x2, y2, IRON_DARK);
		gfx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xE0100506);
		ScreenDrawUtils.drawBevelBorder(gfx, x1, y1, x2 - x1, y2 - y1, IRON_LIGHT, 0xFF120606);
		Slot first = this.menu.slots.get(VialCentrifugeMenu.OUTPUT_START);
		for (int row = 0; row < STRATA.length; row++) {
			int ry = gy + first.y - 1 + row * 18;
			gfx.fill(x1 + 2, ry, x2 - 2, ry + 18, STRATA[row]);
		}
		// Header channel the spout pours into
		gfx.fill(x1 + 2, y1 + 1, x2 - 2, y1 + 3, 0xFF4A1010);
	}

	// ───── Powder tray ─────

	private void renderTray(GuiGraphics gfx, int gx, int gy) {
		Slot tray = this.menu.slots.get(VialCentrifugeMenu.AUX_OUTPUT_SLOT);
		int tx = gx + tray.x, ty = gy + tray.y;
		// Drain from the bowl's bottom into the tray
		gfx.fill(gx + ROTOR_CX - 1, gy + ROTOR_CY + BOWL_RADIUS - 1, gx + ROTOR_CX + 1, ty - 1, IRON_DARK);
		// A shallow dish, wider than the slot
		gfx.fill(tx - 7, ty + 6, tx + 23, ty + 18, IRON_DARK);
		gfx.fill(tx - 6, ty + 7, tx + 22, ty + 17, IRON);
		gfx.fill(tx - 5, ty + 8, tx + 21, ty + 16, 0xFF241010);
	}

	// ───── Reservoir jar ─────

	private void renderJar(GuiGraphics gfx, int gx, int gy) {
		Slot in = this.menu.slots.get(VialCentrifugeMenu.BLOOD_SLOT);
		Slot out = this.menu.slots.get(VialCentrifugeMenu.FLASK_OUTPUT_SLOT);
		int jx = gx + JAR_X, jy = gy + JAR_Y;
		int midX = jx + JAR_W / 2;
		// Pour funnel from the flask slot into the jar's neck
		gfx.fill(midX - 1, gy + in.y + 17, midX + 1, jy - 4, 0xFF4A1010);
		gfx.fill(midX - 3, jy - 5, midX + 3, jy - 3, IRON);
		// Lid and shoulders
		gfx.fill(jx - 3, jy - 3, jx + JAR_W + 3, jy - 2, IRON_LIGHT);
		// Base plate with a drop to the flask return
		gfx.fill(jx - 4, jy + JAR_H + 2, jx + JAR_W + 4, jy + JAR_H + 4, IRON);
		gfx.fill(midX - 1, jy + JAR_H + 4, midX + 1, gy + out.y - 1, IRON_DARK);

		bloodBarBounds = BloodVolumeBarWidget.render(gfx, jx, jy, JAR_W, JAR_H,
				ReservoirReading.of(te), animTime, BORDER_OUTER, BORDER_INNER);
		// Glass highlight down the jar's left side
		gfx.fill(jx + 1, jy + 2, jx + 2, jy + JAR_H - 2, 0x28FFD0D0);
	}

	// ───── Rotor arms, buckets and hub ─────

	/**
	 * At rest each opposite pair is joined by its arm: iron while both buckets are empty, blood-red while both
	 * are loaded, pulsing warning while only one is. Spinning, the arms fade into a blur that turns with the rotor.
	 */
	private void renderArms(GuiGraphics gfx, int gx, int gy, float speed, float angle) {
		int cx = gx + ROTOR_CX, cy = gy + ROTOR_CY;
		float rest = 1f - Mth.clamp(speed * 3f, 0f, 1f);
		if (rest > 0) {
			float warnPulse = 0.5f + 0.5f * Mth.sin(animTime * 40f);
			for (int i = VialCentrifugeMenu.VIAL_START; i < VialCentrifugeMenu.VIAL_START + 4; i++) {
				boolean a = loaded(i), b = loaded(opposite(i));
				int color = a && b ? ARM_LOADED : a == b ? IRON : lerpColor(WARN_DIM, WARN, warnPulse);
				Slot sa = this.menu.slots.get(i), sb = this.menu.slots.get(opposite(i));
				drawLine(gfx, gx + sa.x + 8, gy + sa.y + 8, gx + sb.x + 8, gy + sb.y + 8, 3, scaleAlpha(color, rest));
			}
		}
		if (speed > 0.01f) {
			// Motion blur: the arms' trailing ghosts, one pair of arms every 45 degrees
			for (int ghost = 0; ghost < 6; ghost++) {
				float a = angle - ghost * 0.12f * (0.3f + speed);
				int alpha = (int) (110 * speed * (1f - ghost / 6f));
				for (int arm = 0; arm < 4; arm++) {
					double t = a + arm * Math.PI / 4;
					int r = BUCKET_RADIUS - 6;
					drawLine(gfx, cx - (int) Math.round(r * Math.cos(t)), cy - (int) Math.round(r * Math.sin(t)),
							cx + (int) Math.round(r * Math.cos(t)), cy + (int) Math.round(r * Math.sin(t)), 2,
							(alpha << 24) | 0x6A1414);
				}
			}
		}
	}

	/**
	 * Each bucket hangs on a yoke at the end of its arm; the bucket's cap, on the side away from the hub, swings
	 * outward as the rotor gains speed, like the model's buckets swing up.
	 */
	private void renderBuckets(GuiGraphics gfx, int gx, int gy, float speed) {
		int swing = Math.round(VialCentrifugeRotorRules.swing(speed) / VialCentrifugeRotorRules.MAX_SWING * 3f);
		for (int i = VialCentrifugeMenu.VIAL_START; i <= VialCentrifugeMenu.VIAL_END; i++) {
			Slot slot = this.menu.slots.get(i);
			int sx = gx + slot.x + 8, sy = gy + slot.y + 8;
			double dx = (sx - gx - ROTOR_CX) / (double) BUCKET_RADIUS;
			double dy = (sy - gy - ROTOR_CY) / (double) BUCKET_RADIUS;
			// Rounded bucket cap beyond the slot, away from the hub
			int capX = sx + (int) Math.round(dx * (10 + swing));
			int capY = sy + (int) Math.round(dy * (10 + swing));
			fillDisc(gfx, capX, capY, 4, IRON_DARK);
			fillDisc(gfx, capX, capY, 3, loaded(i) ? 0xFF5A1414 : IRON);
			// Round bucket body around the slot
			fillDisc(gfx, sx, sy, 10, IRON_DARK);
			fillDisc(gfx, sx, sy, 9, loaded(i) ? 0xFF4A1616 : IRON);
		}
	}

	/** Pulsing ring around each empty bucket whose opposite is loaded. */
	private void renderCounterweightWarnings(GuiGraphics gfx, int gx, int gy) {
		if (this.menu.isSpinning()) return;
		int alpha = (int) (190 + 60 * Mth.sin(animTime * 40f));
		int color = (Math.clamp(alpha, 0, 255) << 24) | (WARN & 0xFFFFFF);
		for (int i = VialCentrifugeMenu.VIAL_START; i <= VialCentrifugeMenu.VIAL_END; i++) {
			if (!needsCounterweight(i)) continue;
			Slot slot = this.menu.slots.get(i);
			fillRing(gfx, gx + slot.x + 8, gy + slot.y + 8, 9, 11, color);
		}
	}

	private void renderHub(GuiGraphics gfx, int cx, int cy, int mouseX, int mouseY, float speed, float angle) {
		btnX1 = cx - HUB_RADIUS;
		btnY1 = cy - HUB_RADIUS;
		btnX2 = cx + HUB_RADIUS;
		btnY2 = cy + HUB_RADIUS;
		boolean idle = !this.menu.isSpinning();
		boolean hovered = idle && overButton(mouseX, mouseY);

		fillDisc(gfx, cx, cy, HUB_RADIUS, hovered ? 0xFFBB3333 : IRON_LIGHT);
		fillDisc(gfx, cx, cy, HUB_RADIUS - 1, hovered ? 0xFF4A0E0E : 0xFF2A0808);

		if (idle && speed < 0.01f) {
			// Play triangle (pointing right)
			int triColor = hovered ? 0xFFFF5555 : 0xFFCC3333;
			int triX = cx - 2;
			for (int col = 0; col < 5; col++) {
				int halfH = 4 - col;
				gfx.fill(triX + col, cy - halfH, triX + col + 1, cy + halfH + 1, triColor);
			}
			return;
		}
		// Turning: a hub bolt circle that follows the rotor, glowing with speed
		float pulse = 0.5f + 0.5f * Mth.sin(animTime * 16f);
		int glow = ((int) (60 + 100 * speed * pulse) << 24) | 0xB01010;
		fillDisc(gfx, cx, cy, HUB_RADIUS - 3, glow);
		for (int bolt = 0; bolt < 4; bolt++) {
			double t = angle + bolt * Math.PI / 2;
			int bx = cx + (int) Math.round(5 * Math.cos(t));
			int by = cy + (int) Math.round(5 * Math.sin(t));
			gfx.fill(bx, by, bx + 1, by + 1, 0xFFE0A0A0);
		}
	}

	// ───── Slot backgrounds ─────

	private void drawSlotBackground(GuiGraphics gfx, int sx, int sy, int slotIndex) {
		ScreenDrawUtils.drawSlotWell(gfx, sx, sy, SLOT_BORDER_DARK, SLOT_BG, SLOT_BORDER_LIGHT);
		if (isBucket(slotIndex)) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x15CC2222);
		} else if (slotIndex >= VialCentrifugeMenu.OUTPUT_START && slotIndex <= VialCentrifugeMenu.OUTPUT_END) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FF4444);
		} else if (slotIndex == VialCentrifugeMenu.BLOOD_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20AA0000);
		} else if (slotIndex == VialCentrifugeMenu.AUX_OUTPUT_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FFAA44);
		} else if (slotIndex == VialCentrifugeMenu.FLASK_OUTPUT_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FF4444);
		}
	}

	// ───── Drawing helpers ─────

	private static void fillDisc(GuiGraphics gfx, int cx, int cy, int r, int color) {
		for (int dy = -r; dy <= r; dy++) {
			int half = (int) Math.floor(Math.sqrt(r * r - dy * dy + r * 0.8));
			gfx.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, color);
		}
	}

	private static void fillRing(GuiGraphics gfx, int cx, int cy, int inner, int outer, int color) {
		for (int dy = -outer; dy <= outer; dy++) {
			int o = (int) Math.floor(Math.sqrt(outer * outer - dy * dy));
			int i = Math.abs(dy) < inner ? (int) Math.floor(Math.sqrt(inner * inner - dy * dy)) : -1;
			if (i < 0) {
				gfx.fill(cx - o, cy + dy, cx + o + 1, cy + dy + 1, color);
			} else {
				gfx.fill(cx - o, cy + dy, cx - i, cy + dy + 1, color);
				gfx.fill(cx + i + 1, cy + dy, cx + o + 1, cy + dy + 1, color);
			}
		}
	}

	private static void drawLine(GuiGraphics gfx, int x0, int y0, int x1, int y1, int thickness, int color) {
		int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
		int lo = -(thickness - 1) / 2;
		int lastX = Integer.MIN_VALUE, lastY = Integer.MIN_VALUE;
		for (int s = 0; s <= steps; s++) {
			int x = x0 + Math.round((x1 - x0) * s / (float) Math.max(1, steps));
			int y = y0 + Math.round((y1 - y0) * s / (float) Math.max(1, steps));
			if (x == lastX && y == lastY) continue;
			gfx.fill(x + lo, y + lo, x + lo + thickness, y + lo + thickness, color);
			lastX = x;
			lastY = y;
		}
	}

	private static int scaleAlpha(int color, float scale) {
		int alpha = (int) (((color >>> 24) & 0xFF) * scale);
		return (alpha << 24) | (color & 0xFFFFFF);
	}

	private static int lerpColor(int a, int b, float t) {
		int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
		int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
		return 0xFF000000 | ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
	}
}
