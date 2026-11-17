package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.SpecimenJarRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.client.screen.widget.TemperPhials;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.menu.tile.functional.SpecimenVesselMenu;
import com.vincenthuto.hemomancy.common.mission.alchemist.BloodResearch;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.BroodRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.KeptQuirks;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.KeptSpecimen;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessel;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessels;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.VatVigorRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.VesselView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Locale;

/**
 * A Specimen Jar's or Vat's screen (core-loop round two, P6), in place of the chat readout: the creature in its glass
 * with its Vigor, the keeper and the buffer, its blood as far as the viewer has read it (hollow phials while unread, its
 * Tempers once examined, a draw's yield and the kind's quirks once studied), and its brood with the one button that
 * pairs or parts it. Values come from the menu's data; what the viewer knows comes from their own synced research.
 */
public class SpecimenVesselScreen extends AbstractContainerScreen<SpecimenVesselMenu> {
	private static final int WIDTH = 300;
	private static final int HEIGHT = 236;
	private static final int WELL_X = 10, WELL_Y = 22, WELL_W = 84, WELL_H = 104;
	private static final int RIGHT_X = 104;
	private static final int BROOD_Y = 194;
	private static final int BUTTON_W = 84, BUTTON_H = 16;
	private static final int[] RINGS = {0xFF6B1010, 0xFF4A0C0C, 0xFF300808, 0xFF1A0404};
	private static final int CORNER = 0xFF8A1818;
	private static final int TITLE = 0xFFE0A0A0;
	private static final int TEXT = 0xFFE4D6D0;
	private static final int DIM = 0xFF9C8A86;
	private static final int FAINT = 0xFF6E5E5C;
	private static final int BAR_BACK = 0xFF140608;
	private static final int BAR_RIM = 0xFF3A1014;
	private static final VeinBackgroundRenderer.Style STYLE = new VeinBackgroundRenderer.Style(
			VeinBackgroundRenderer.Field.MACHINE.seed(20261111L), VeinBackgroundRenderer.Palette.MACHINE,
			VeinBackgroundRenderer.Speckles.Scattered.MACHINE);

	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(STYLE);

	public SpecimenVesselScreen(SpecimenVesselMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = WIDTH;
		imageHeight = HEIGHT;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		if (overButton(mouseX, mouseY)) {
			BroodRules.Refusal refusal = refusal();
			if (!pressable() && refusal != BroodRules.Refusal.NONE) {
				graphics.renderTooltip(font, Component.translatable(refusal.key()), mouseX, mouseY);
			} else {
				graphics.renderTooltip(font, Component.translatable(menu.flag(VesselView.PAIRED)
						? "screen.hemomancy.specimen_vessel.part.tooltip" : "screen.hemomancy.specimen_vessel.breed.tooltip"),
						mouseX, mouseY);
			}
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		// Everything is drawn in renderBg, inside the panel's own chrome.
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = leftPos, y = topPos;
		float time = minecraft == null || minecraft.level == null ? 0.0F : AnimationTime.gameTime(minecraft.level, partialTick);
		veins.draw(graphics, x, y, imageWidth, imageHeight, time * 0.05F);
		ScreenDrawUtils.drawRingBorder(graphics, x, y, imageWidth, imageHeight, RINGS);
		ScreenDrawUtils.drawCornerAccents(graphics, x, y, imageWidth, imageHeight, 2, CORNER);
		SpecimenVessel vessel = menu.vessel();
		if (vessel == null || !vessel.kept().occupied()) return;
		KeptSpecimen kept = vessel.kept();
		Component name = SpecimenJarData.getSpecimenName(kept.specimen());
		graphics.drawCenteredString(font, title, x + imageWidth / 2, y + 7, FAINT);
		drawWell(graphics, vessel, kept, x + WELL_X, y + WELL_Y, time);
		drawVigor(graphics, kept, x + WELL_X, y + WELL_Y + WELL_H + 4);
		int ry = drawHeader(graphics, name, kept, x + RIGHT_X, y + 20);
		drawBlood(graphics, kept, x + RIGHT_X, ry + 4, imageWidth - RIGHT_X - 10, y + BROOD_Y - 6);
		drawBrood(graphics, kept, x + 10, y + BROOD_Y, imageWidth - 20, mouseX, mouseY);
	}

	// ── The glass ──

	private void drawWell(GuiGraphics graphics, SpecimenVessel vessel, KeptSpecimen kept, int x, int y, float time) {
		graphics.fill(x, y, x + WELL_W, y + WELL_H, 0xAA0A0405);
		ScreenDrawUtils.drawBevelBorder(graphics, x - 1, y - 1, WELL_W + 2, WELL_H + 2, 0xFF120405, 0xFF5A1C20, 0xFF2A0A0C);
		// A faint red wash, as the vat's glass reads in the world.
		graphics.fillGradient(x + 1, y + 1, x + WELL_W - 1, y + WELL_H - 1, 0x10C04050, 0x30801820);
		Entity display = SpecimenJarRenderer.display((BlockEntity) vessel, kept);
		if (!(display instanceof LivingEntity living)) return;
		// A long body turns in the glass, head and all, so it is fitted by nearly twice its width (a cow's head overhangs
		// its box by half a block).
		float width = Math.max(0.3F, living.getBbWidth() * 1.9F);
		float height = Math.max(0.3F, living.getBbHeight());
		float fit = Math.min((WELL_W - 16) / width, (WELL_H - 22) / height) * (kept.husk() ? 0.7F : 1.0F);
		float scale = Math.max(6.0F, Math.min(56.0F, fit));
		float yaw = kept.husk() ? 30.0F : time * 1.2F;
		float bodyRot = living.yBodyRot, yRot = living.getYRot(), xRot = living.getXRot(), head = living.yHeadRot,
				headO = living.yHeadRotO;
		int deathTime = living.deathTime;
		try {
			living.yBodyRot = 0.0F;
			living.setYRot(0.0F);
			living.setXRot(0.0F);
			living.yHeadRot = 0.0F;
			living.yHeadRotO = 0.0F;
			// A husk lies fallen in its glass, in the death pose the world draws it with.
			living.deathTime = kept.husk() ? 19 : 0;
			Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI).rotateY((float) Math.toRadians(yaw));
			Vector3f translate = new Vector3f(0.0F, living.getBbHeight() / 2.0F, 0.0F);
			graphics.enableScissor(x + 1, y + 1, x + WELL_W - 1, y + WELL_H - 1);
			// The death pose tips the body over at its feet; shift it back so it lies across the well's floor.
			float shift = kept.husk() ? living.getBbHeight() * scale * 0.27F : 0.0F;
			float floorY = y + WELL_H * 0.56F;
			InventoryScreen.renderEntityInInventory(graphics, x + WELL_W / 2.0F + shift, floorY, scale / living.getScale(),
					translate, pose, null, living);
		} catch (RuntimeException unrenderable) {
			// Some creatures cannot be drawn outside the world; the glass stays empty rather than closing the screen.
		} finally {
			graphics.disableScissor();
			living.yBodyRot = bodyRot;
			living.setYRot(yRot);
			living.setXRot(xRot);
			living.yHeadRot = head;
			living.yHeadRotO = headO;
			living.deathTime = deathTime;
		}
		if (kept.husk()) {
			// What is left lies dark, as the world draws it at almost no light: the shade sits in front of the body and
			// leaves only a dim, blood-dark shape of it.
			graphics.pose().pushPose();
			graphics.pose().translate(0.0F, 0.0F, 400.0F);
			graphics.fill(x + 1, y + 1, x + WELL_W - 1, y + WELL_H - 1, 0xC8070203);
			graphics.pose().popPose();
		}
	}

	private void drawVigor(GuiGraphics graphics, KeptSpecimen kept, int x, int y) {
		if (kept.husk()) {
			graphics.drawCenteredString(font, Component.translatable("screen.hemomancy.specimen_vessel.husk"),
					x + WELL_W / 2, y + 2, DIM);
			return;
		}
		double vigor = menu.data(VesselView.VIGOR_X100) / 100.0D;
		int colour = vigor >= VatVigorRules.HIGH_BAND ? 0xFFC8343C : vigor >= VatVigorRules.RATTLE_BELOW ? 0xFF8E3A3C : 0xFF5E2A30;
		bar(graphics, x, y, WELL_W, 6, (float) (vigor / VatVigorRules.MAX), colour);
		Component word = Component.translatable("message.hemomancy.menagerie.vigor." + VatVigorRules.word(vigor));
		graphics.drawCenteredString(font, Component.translatable("screen.hemomancy.specimen_vessel.vigor",
				Math.round(vigor), word), x + WELL_W / 2, y + 10, TEXT);
	}

	// ── The right column ──

	private int drawHeader(GuiGraphics graphics, Component name, KeptSpecimen kept, int x, int y) {
		int width = imageWidth - RIGHT_X - 10;
		graphics.drawString(font, ScreenDrawUtils.truncateText(font, name.getString(), width), x, y, TITLE, false);
		y += 12;
		String keeper = !menu.flag(VesselView.OWNED) ? "preserved" : menu.flag(VesselView.MAY_DRAW) ? "yours" : "another";
		graphics.drawString(font, Component.translatable("screen.hemomancy.specimen_vessel.keeper." + keeper), x, y, DIM, false);
		y += 12;
		if (menu.flag(VesselView.OWNED) && !kept.husk()) {
			int buffer = menu.data(VesselView.BUFFER), capacity = Math.max(1, menu.data(VesselView.CAPACITY));
			graphics.drawString(font, Component.translatable("screen.hemomancy.specimen_vessel.buffer", buffer, capacity),
					x, y, TEXT, false);
			y += 10;
			bar(graphics, x, y, width, 4, buffer / (float) capacity, 0xFFA0202C);
			y += 8;
			graphics.drawString(font, Component.translatable("screen.hemomancy.specimen_vessel.feed",
					number(menu.data(VesselView.FEED_PRICE_X10) / 10.0D)), x, y, DIM, false);
			y += 10;
			if (menu.flag(VesselView.TENDED)) {
				graphics.drawString(font, Component.translatable("message.hemomancy.menagerie.readout.tended"), x, y, DIM, false);
				y += 10;
			}
		}
		return y;
	}

	private void drawBlood(GuiGraphics graphics, KeptSpecimen kept, int x, int y, int width, int bottom) {
		graphics.fill(x, y, x + width, y + 1, 0x663A1014);
		y += 4;
		Component label = Component.translatable("screen.hemomancy.specimen_vessel.blood");
		graphics.drawString(font, label, x, y + 5, TITLE, false);
		var player = minecraft == null ? null : minecraft.player;
		ResourceLocation type = kept.typeId();
		List<EnumBloodTemper> tempers = BloodProfileData.profile(kept.type(), true).tempers();
		boolean knows = BloodResearch.knows(player, type);
		int phialX = x + font.width(label) + 8;
		if (!knows) {
			int count = Math.max(1, Math.min(3, tempers.size()));
			for (int i = 0; i < count; i++) {
				TemperPhials.drawUnread(graphics, phialX, y, 2);
				phialX += TemperPhials.WIDTH * 2 + 4;
			}
			y = wrap(graphics, Component.translatable("message.hemomancy.menagerie.readout.unread"), x, y + 22, width, FAINT);
			return;
		}
		for (EnumBloodTemper school : tempers) {
			TemperPhials.draw(graphics, phialX, y, 2, school, 1.0F, false);
			phialX += TemperPhials.WIDTH * 2 + 4;
		}
		EnumBloodTemper inherited = kept.inheritedSchool();
		if (inherited != null && !tempers.contains(inherited)) {
			TemperPhials.draw(graphics, phialX + 2, y + 4, 1, inherited, 0.5F, false);
		}
		y += 22;
		y = wrap(graphics, Component.translatable("message.hemomancy.menagerie.readout.schools", SpecimenVessels.schoolList(tempers)), x, y,
				width, TEXT);
		if (!BloodResearch.studied(player, type)) {
			int remaining = Math.max(0, ClinicalBloodProgress.STUDY_INDIVIDUALS - BloodResearch.individuals(player, type));
			wrap(graphics, Component.translatable("message.hemomancy.menagerie.readout.study", remaining), x, y + 2, width, FAINT);
			return;
		}
		if (!kept.husk()) {
			y = wrap(graphics, Component.translatable("message.hemomancy.menagerie.readout.yield",
					number(menu.data(VesselView.TEMPER_YIELD_X100) / 100.0D),
					number(menu.data(VesselView.BLOOD_YIELD_X100) / 100.0D)), x, y + 2, width, TEXT);
		}
		KeptQuirks.Facts facts = SpecimenVessels.quirkFacts(kept.type(), kept.maxHealth(), inherited, true);
		graphics.pose().pushPose();
		graphics.pose().translate(x, y + 3, 0.0F);
		graphics.pose().scale(0.75F, 0.75F, 1.0F);
		int line = 0;
		int room = (int) ((bottom - y - 3) / 0.75F);
		for (Component quirk : KeptQuirks.lines(facts, BloodManipulation::temperName)) {
			for (FormattedCharSequence part : font.split(Component.translatable("screen.hemomancy.specimen_vessel.quirk", quirk),
					(int) (width / 0.75F))) {
				if ((line + 1) * 9 > room) break;
				graphics.drawString(font, part, 0, line * 9, DIM, false);
				line++;
			}
		}
		graphics.pose().popPose();
	}

	// ── The brood ──

	private void drawBrood(GuiGraphics graphics, KeptSpecimen kept, int x, int y, int width, int mouseX, int mouseY) {
		graphics.fill(x, y - 4, x + width, y - 3, 0x663A1014);
		BroodRules.State state = BroodRules.State.values()[Math.max(0, Math.min(BroodRules.State.values().length - 1,
				menu.data(VesselView.BROOD_STATE)))];
		Component label = Component.translatable("screen.hemomancy.specimen_vessel.brood");
		graphics.drawString(font, label, x, y, TITLE, false);
		int labelW = font.width(label) + 6;
		Component line = !menu.flag(VesselView.BREEDS) ? Component.translatable("message.hemomancy.menagerie.brood.never")
				: Component.translatable(state.key());
		graphics.drawString(font, ScreenDrawUtils.truncateText(font, line.getString(), width - BUTTON_W - labelW - 6),
				x + labelW, y, state == BroodRules.State.GESTATING ? TEXT : DIM, false);
		int barY = y + 13;
		int cost = menu.data(VesselView.BROOD_COST);
		float progress = menu.data(VesselView.BROOD_PERMILLE) / 1000.0F;
		if (menu.flag(VesselView.PAIRED)) {
			bar(graphics, x, barY, width - BUTTON_W - 6, 6, progress,
					state == BroodRules.State.GESTATING || state == BroodRules.State.AT_TERM ? 0xFFC0283A : 0xFF6A2A34);
			graphics.drawString(font, Component.translatable("screen.hemomancy.specimen_vessel.brood_paid",
					Math.round(progress * cost), cost), x, barY + 10, DIM, false);
		} else if (menu.flag(VesselView.BREEDS)) {
			graphics.drawString(font, ScreenDrawUtils.truncateText(font, Component.translatable(
					"screen.hemomancy.specimen_vessel.brood_cost", cost, Math.round(BroodRules.BROOD_VIGOR)).getString(),
					width - BUTTON_W - 8), x, barY + 2, FAINT, false);
		}
		int bx = buttonX(), by = buttonY();
		boolean enabled = pressable();
		boolean hovered = overButton(mouseX, mouseY);
		graphics.fill(bx, by, bx + BUTTON_W, by + BUTTON_H, !enabled ? 0xCC1A0E10 : hovered ? 0xEE5A1018 : 0xDD3A0A10);
		ScreenDrawUtils.drawBevelBorder(graphics, bx, by, BUTTON_W, BUTTON_H, enabled ? 0xFF8A2A30 : 0xFF3A2224,
				enabled ? 0xFF2A0608 : 0xFF140A0C);
		Component text = Component.translatable(menu.flag(VesselView.PAIRED)
				? "screen.hemomancy.specimen_vessel.part" : "screen.hemomancy.specimen_vessel.breed");
		graphics.drawCenteredString(font, text, bx + BUTTON_W / 2, by + 4, enabled ? TEXT : FAINT);
	}

	private BroodRules.Refusal refusal() {
		return BroodRules.Refusal.byOrdinal(menu.data(VesselView.BROOD_REFUSAL));
	}

	private boolean pressable() {
		return refusal() == BroodRules.Refusal.NONE && (menu.flag(VesselView.PAIRED) || menu.flag(VesselView.BREEDS));
	}

	private int buttonX() {
		return leftPos + imageWidth - 10 - BUTTON_W;
	}

	private int buttonY() {
		return topPos + BROOD_Y + 12;
	}

	private boolean overButton(double mouseX, double mouseY) {
		return mouseX >= buttonX() && mouseX < buttonX() + BUTTON_W && mouseY >= buttonY() && mouseY < buttonY() + BUTTON_H;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && overButton(mouseX, mouseY) && pressable() && minecraft != null && minecraft.gameMode != null) {
			minecraft.gameMode.handleInventoryButtonClick(menu.containerId, SpecimenVesselMenu.BROOD_BUTTON);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	// ── Small parts ──

	private void bar(GuiGraphics graphics, int x, int y, int width, int height, float fraction, int colour) {
		graphics.fill(x, y, x + width, y + height, BAR_BACK);
		ScreenDrawUtils.drawSimpleBorder(graphics, x, y, width, height, BAR_RIM);
		int filled = Math.round((width - 2) * Math.max(0.0F, Math.min(1.0F, fraction)));
		if (filled > 0) graphics.fillGradient(x + 1, y + 1, x + 1 + filled, y + height - 1, colour, darker(colour));
	}

	private static int darker(int colour) {
		int r = ((colour >> 16) & 0xFF) * 3 / 5, g = ((colour >> 8) & 0xFF) * 3 / 5, b = (colour & 0xFF) * 3 / 5;
		return (colour & 0xFF000000) | (r << 16) | (g << 8) | b;
	}

	private int wrap(GuiGraphics graphics, Component text, int x, int y, int width, int colour) {
		for (FormattedCharSequence line : font.split(text, width)) {
			graphics.drawString(font, line, x, y, colour, false);
			y += 10;
		}
		return y;
	}

	private static String number(double value) {
		return value == Math.rint(value) ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.1f", value);
	}
}
