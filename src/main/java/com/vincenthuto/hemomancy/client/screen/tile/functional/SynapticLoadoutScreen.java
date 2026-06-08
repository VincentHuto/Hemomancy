package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.SynapticLoadoutSlotHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationClientPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class SynapticLoadoutScreen extends Screen {
	private static final ResourceLocation MEMORY_BASE =
			Hemomancy.rloc("textures/item/memories/memory_blank.png");
	private static final int PANEL = 128;
	private static final int SIDE_PANEL = 96;
	private static final int MIN_SIDE_PANEL = 48;
	private static final int SIDE_GAP = 28;
	private static final int MIN_SIDE_GAP = 14;
	private static final int CONTROL_RAIL_W = 104;
	private static final int CONTROL_RAIL_H = 158;
	private static final int CONTROL_BUTTON_W = 74;
	private static final int CONTROL_CAROUSEL_GUTTER = 18;
	private static final int COST_BLOOD = 100;
	private static final int COST_XP = 25;
	private static final int NEURAL_VEIN_COUNT = 24;
	private static final int NEURAL_BORDER_OUTER = 0xFF7A5A12;
	private static final int NEURAL_BORDER_INNER = 0xFFC79A24;
	private static final int NEURAL_BORDER_DIM = 0xFF4E3710;
	private static final int NEURAL_PANEL_FILL = 0xDD0B0803;
	private static final int NEURAL_PANEL_FILL_SOFT = 0xAA201606;
	private static final int NEURAL_TEXT = 0xFFFFE8B2;
	private static final int NEURAL_TEXT_MUTED = 0xFFC9AD73;

	private final BlockPos distributorPos;
	private int selectedIndex;
	private boolean confirmOverwrite;
	private float animTime;
	private float[][] neuralVeinParams;
	private EditBox nameField;
	private Button applyButton;
	private Button renameButton;
	private Button saveButton;

	public SynapticLoadoutScreen(BlockPos distributorPos) {
		super(Component.translatable("screen.hemomancy.synaptic_loadouts"));
		this.distributorPos = distributorPos;
	}

	public static void openScreen(BlockPos pos) {
		Minecraft.getInstance().setScreen(new SynapticLoadoutScreen(pos));
	}

	@Override
	protected void init() {
		super.init();
		seedNeuralVeins();
		PacketHandler.sendToServer(new KnownManipulationClientPacket());
		int frameW = frameWidth();
		int frameX = (width - frameW) / 2;
		int frameY = (height - frameHeight()) / 2;
		int controlX = frameX + 12;
		int controlY = frameY + 38;
		int carouselX = boundedCarouselCenter(frameX, frameW);
		int sidePanel = sidePanelSize(frameW);
		int gap = sideGap(frameW);
		addRenderableWidget(Button.builder(Component.literal("<"), button -> cycle(-1))
				.bounds(leftArrowX(carouselX, sidePanel, gap), carouselCenterY(frameY) - 10, 22, 20).build());
		addRenderableWidget(Button.builder(Component.literal(">"), button -> cycle(1))
				.bounds(rightArrowX(carouselX, sidePanel, gap), carouselCenterY(frameY) - 10, 22, 20).build());
		nameField = new EditBox(font, controlX + 8, controlY + 24, CONTROL_RAIL_W - 16, 18,
				Component.translatable("screen.hemomancy.synaptic_loadouts.name"));
		nameField.setMaxLength(ManipulationLoadout.MAX_NAME_LENGTH);
		addRenderableWidget(nameField);
		applyButton = addRenderableWidget(Button.builder(Component.literal("Apply"), button -> sendApply())
				.bounds(controlX + (CONTROL_RAIL_W - CONTROL_BUTTON_W) / 2, controlY + 50, CONTROL_BUTTON_W, 20).build());
		renameButton = addRenderableWidget(Button.builder(Component.literal("Rename"), button -> sendRename())
				.bounds(controlX + (CONTROL_RAIL_W - CONTROL_BUTTON_W) / 2, controlY + 74, CONTROL_BUTTON_W, 20).build());
		saveButton = addRenderableWidget(Button.builder(Component.literal("Save"), button -> sendSaveOrOverwrite())
				.bounds(controlX + (CONTROL_RAIL_W - CONTROL_BUTTON_W) / 2, controlY + 98, CONTROL_BUTTON_W, 20).build());
		updateNameField();
		updateButtons();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Screen#renderBackground applies menu blur after custom drawing in 1.21.1.
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_LEFT) {
			cycle(-1);
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_RIGHT) {
			cycle(1);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		IKnownManipulations known = known();
		if (known != null && unlockedSlots() > 0 && centerPanelContains(mouseX, mouseY)
				&& known.getLoadout(selectedIndex).isEmpty()) {
			sendSaveOrOverwrite();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
		int frameW = frameWidth();
		int frameH = frameHeight();
		int frameX = (width - frameW) / 2;
		int frameY = (height - frameH) / 2;
		tickAnimTime();
		renderNeuralVeinBackground(gfx, frameX, frameY, frameW, frameH);
		drawNeuralBorder(gfx, frameX, frameY, frameW, frameH, true);
		drawControlRail(gfx, frameX + 12, frameY + 38, CONTROL_RAIL_H);

		IKnownManipulations known = known();
		int slots = unlockedSlots();
		if (slots == 0) {
			selectedIndex = 0;
		} else if (selectedIndex >= slots) {
			selectedIndex = slots - 1;
		}
		Component title = Component.translatable("screen.hemomancy.synaptic_loadouts");
		gfx.drawString(font, title, (width - font.width(title)) / 2, frameY + 12, NEURAL_TEXT, false);
		String count = slots + " / " + SynapticLoadoutSlotHelper.MAX_LOADOUT_SLOTS + " Synaptic Patterns";
		gfx.drawString(font, count, (width - font.width(count)) / 2, frameY + 25, NEURAL_TEXT_MUTED, false);

		if (known == null) {
			String waiting = "Reading vascular memory...";
			gfx.drawString(font, waiting, (width - font.width(waiting)) / 2, height / 2, NEURAL_TEXT_MUTED, false);
			return;
		}

		int centerX = boundedCarouselCenter(frameX, frameW);
		int centerY = carouselCenterY(frameY);
		int sidePanel = sidePanelSize(frameW);
		int gap = sideGap(frameW);
		if (slots > 1) {
			drawLoadoutPanel(gfx, known.getLoadout(previousIndex(slots)), leftSidePanelX(centerX, sidePanel, gap),
					centerY - sidePanel / 2, sidePanel, false);
			drawLoadoutPanel(gfx, known.getLoadout(nextIndex(slots)), rightSidePanelX(centerX, gap),
					centerY - sidePanel / 2, sidePanel, false);
		}
		drawLoadoutPanel(gfx, known.getLoadout(selectedIndex), centerX - PANEL / 2, centerY - PANEL / 2,
				PANEL, true);
		drawFooterText(gfx, known.getLoadout(selectedIndex), frameX + CONTROL_RAIL_W + 26,
				frameW - CONTROL_RAIL_W - 38, frameY + frameH - 54);
		updateButtons();
		super.render(gfx, mouseX, mouseY, partialTick);
		renderFocusedTooltip(gfx, mouseX, mouseY);
	}

	private int frameWidth() {
		return Math.min(width - 32, 760);
	}

	private int frameHeight() {
		return Math.min(height - 32, 280);
	}

	private int carouselCenterX(int frameX, int frameW) {
		int carouselLeft = carouselAreaLeft(frameX);
		int carouselRight = carouselAreaRight(frameX, frameW);
		return (carouselLeft + carouselRight) / 2;
	}

	private int boundedCarouselCenter(int frameX, int frameW) {
		int preferred = carouselCenterX(frameX, frameW);
		int sidePanel = sidePanelSize(frameW);
		int gap = sideGap(frameW);
		int span = sideCarouselSpan(sidePanel, gap);
		int min = carouselAreaLeft(frameX) + span;
		int max = carouselAreaRight(frameX, frameW) - span;
		if (min > max) {
			return min;
		}
		return Math.max(min, Math.min(max, preferred));
	}

	private int carouselAreaLeft(int frameX) {
		return frameX + 12 + CONTROL_RAIL_W + CONTROL_CAROUSEL_GUTTER;
	}

	private int carouselAreaRight(int frameX, int frameW) {
		return frameX + frameW - 12;
	}

	private int sideCarouselSpan(int sidePanel, int gap) {
		return PANEL / 2 + gap + sidePanel;
	}

	private int sidePanelSize(int frameW) {
		int available = frameW - CONTROL_RAIL_W - CONTROL_CAROUSEL_GUTTER - 24;
		return Math.max(MIN_SIDE_PANEL, Math.min(SIDE_PANEL, (available - PANEL - sideGap(frameW) * 2) / 2));
	}

	private int sideGap(int frameW) {
		int available = frameW - CONTROL_RAIL_W - CONTROL_CAROUSEL_GUTTER - 24;
		return available >= PANEL + SIDE_PANEL * 2 + SIDE_GAP * 2 ? SIDE_GAP : MIN_SIDE_GAP;
	}

	private int carouselCenterY(int frameY) {
		return frameY + 108;
	}

	private int leftSidePanelX(int centerX, int sidePanel, int gap) {
		return centerX - PANEL / 2 - gap - sidePanel;
	}

	private int rightSidePanelX(int centerX, int gap) {
		return centerX + PANEL / 2 + gap;
	}

	private int leftArrowX(int centerX, int sidePanel, int gap) {
		return arrowGapCenter(leftSidePanelX(centerX, sidePanel, gap) + sidePanel, centerX - PANEL / 2) - 11;
	}

	private int rightArrowX(int centerX, int sidePanel, int gap) {
		return arrowGapCenter(centerX + PANEL / 2, rightSidePanelX(centerX, gap)) - 11;
	}

	private int arrowGapCenter(int leftEdge, int rightEdge) {
		return (leftEdge + rightEdge) / 2;
	}

	private void drawControlRail(GuiGraphics gfx, int x, int y, int h) {
		gfx.fill(x, y, x + CONTROL_RAIL_W, y + h, 0xB60D0903);
		drawNeuralBorder(gfx, x, y, CONTROL_RAIL_W, h, false);
		gfx.drawString(font, "Pattern", x + 8, y + 8, NEURAL_TEXT_MUTED, false);
	}

	private void drawLoadoutPanel(GuiGraphics gfx, ManipulationLoadout loadout, int x, int y, int size, boolean focused) {
		int border = focused ? NEURAL_BORDER_INNER : NEURAL_BORDER_DIM;
		int fill = focused ? 0xFF211706 : 0xFF100B04;
		gfx.fill(x, y, x + size, y + size, border);
		gfx.fill(x + 2, y + 2, x + size - 2, y + size - 2, fill);
		if (loadout.isEmpty()) {
			drawEmptyPanel(gfx, x, y, size, focused);
		} else {
			drawRadialPreview(gfx, loadout, x + size / 2, y + size / 2,
					focused ? focusedPreviewRadius(size) : sidePreviewRadius(size), focused);
			String name = loadout.name();
			int color = focused ? NEURAL_TEXT : NEURAL_TEXT_MUTED;
			gfx.drawString(font, trimToWidth(name, size - 10), x + (size - font.width(trimToWidth(name, size - 10))) / 2,
					y + size - 16, color, false);
		}
	}

	private void drawEmptyPanel(GuiGraphics gfx, int x, int y, int size, boolean focused) {
		int plusColor = focused ? NEURAL_TEXT : 0xFF9B834E;
		int cx = x + size / 2;
		int cy = y + size / 2;
		int arm = focused ? 18 : 10;
		gfx.fill(cx - arm, cy - 2, cx + arm, cy + 2, plusColor);
		gfx.fill(cx - 2, cy - arm, cx + 2, cy + arm, plusColor);
		if (focused) {
			String text = "Save current loadout";
			gfx.drawString(font, text, x + (size - font.width(text)) / 2, y + size - 28, NEURAL_TEXT_MUTED, false);
		}
	}

	private void drawRadialPreview(GuiGraphics gfx, ManipulationLoadout loadout, int cx, int cy, int radius, boolean focused) {
		List<String> names = loadout.manipNames();
		int centerColor = focused ? 0xFF5E4310 : 0xFF30240C;
		gfx.fill(cx - 14, cy - 14, cx + 14, cy + 14, centerColor);
		gfx.fill(cx - 12, cy - 12, cx + 12, cy + 12, 0xFF100B04);
		if (names.isEmpty()) {
			return;
		}
		double start = -Math.PI / 2.0D;
		for (int i = 0; i < names.size(); i++) {
			double angle = start + i * (Math.PI * 2.0D / names.size());
			int ix = cx + (int) Math.round(Math.cos(angle) * radius) - 8;
			int iy = cy + (int) Math.round(Math.sin(angle) * radius) - 8;
			gfx.fill(ix - 2, iy - 2, ix + 18, iy + 18, 0xAA765719);
			gfx.blit(MEMORY_BASE, ix, iy, 0, 0, 16, 16, 16, 16);
			gfx.blit(memoryOverlayTexture(names.get(i)), ix, iy, 0, 0, 16, 16, 16, 16);
		}
	}

	private int focusedPreviewRadius(int size) {
		return Math.max(22, size / 2 - 36);
	}

	private int sidePreviewRadius(int size) {
		return Math.max(12, size / 2 - 28);
	}

	private void drawFooterText(GuiGraphics gfx, ManipulationLoadout loadout, int x, int maxWidth, int y) {
		List<Component> lines = new ArrayList<>();
		if (loadout.isEmpty()) {
			lines.add(Component.literal("Save current loadout").withStyle(ChatFormatting.GRAY));
			lines.add(Component.literal("Cost: " + COST_BLOOD + " blood, " + COST_XP + " XP")
					.withStyle(ChatFormatting.GOLD));
		} else {
			lines.add(Component.literal(loadout.manipNames().size() + " remembered manipulations")
					.withStyle(ChatFormatting.GRAY));
			lines.add(Component.literal("Apply and rename are free. Overwrite costs "
					+ COST_BLOOD + " blood, " + COST_XP + " XP.").withStyle(ChatFormatting.GOLD));
		}
		int lineY = y;
		for (Component line : lines) {
			for (FormattedCharSequence segment : font.split(line, maxWidth)) {
				gfx.drawString(font, segment, x + (maxWidth - font.width(segment)) / 2, lineY, NEURAL_TEXT, false);
				lineY += 11;
			}
		}
	}

	private void seedNeuralVeins() {
		Random rand = new Random(91827L);
		neuralVeinParams = new float[NEURAL_VEIN_COUNT][9];
		for (int i = 0; i < NEURAL_VEIN_COUNT; i++) {
			neuralVeinParams[i][0] = rand.nextFloat();
			neuralVeinParams[i][1] = rand.nextFloat();
			neuralVeinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
			neuralVeinParams[i][3] = 0.35f + rand.nextFloat() * 0.75f;
			neuralVeinParams[i][4] = 7f + rand.nextFloat() * 18f;
			neuralVeinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;
			neuralVeinParams[i][6] = 54 + rand.nextInt(120);
			neuralVeinParams[i][7] = 1 + rand.nextInt(2);
			neuralVeinParams[i][8] = rand.nextFloat();
		}
	}

	private void tickAnimTime() {
		animTime += 0.004f;
	}

	private void renderNeuralVeinBackground(GuiGraphics gfx, int x, int y, int w, int h) {
		gfx.enableScissor(x, y, x + w, y + h);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		gfx.fill(x, y, x + w, y + h, 0xFF080603);
		gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, NEURAL_PANEL_FILL_SOFT);
		gfx.fill(x + 3, y + 3, x + w - 3, y + h - 3, NEURAL_PANEL_FILL);

		int cx = x + w / 2;
		int cy = y + h / 2;
		int glowRadius = Math.max(w, h) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 4) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (34 * (1f - t));
			int red = (int) (105 * (1f - t));
			int green = (int) (76 * (1f - t));
			int color = (alpha << 24) | (red << 16) | (green << 8) | 0x08;
			gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}

		if (neuralVeinParams != null) {
			for (int i = 0; i < NEURAL_VEIN_COUNT; i++) {
				drawNeuralTendril(gfx, i, animTime, x, y, w, h);
			}
		}

		Random speckRand = new Random(71423L);
		for (int s = 0; s < 110; s++) {
			int sx = x + speckRand.nextInt(w);
			int sy = y + speckRand.nextInt(h);
			int sr = 54 + speckRand.nextInt(50);
			int sg = 38 + speckRand.nextInt(38);
			int sb = speckRand.nextInt(10);
			int sa = 12 + speckRand.nextInt(28);
			gfx.fill(sx, sy, sx + 1, sy + 1, (sa << 24) | (sr << 16) | (sg << 8) | sb);
		}

		RenderSystem.disableBlend();
		gfx.disableScissor();
	}

	private void drawNeuralTendril(GuiGraphics gfx, int index, float time, int x, int y, int w, int h) {
		float[] p = neuralVeinParams[index];
		float startX = x + p[0] * w;
		float startY = y + p[1] * h;
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

		int baseRed = (int) (120 + 80 * brightness);
		int baseGreen = (int) (82 + 76 * brightness);
		int baseBlue = (int) (8 + 24 * brightness);

		for (int step = 0; step < length; step++) {
			float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
			float microSquiggle = amplitude * 0.3f
					* Mth.sin(frequency * 2.7f * step + timeOffset * 1.4f + index);
			float displacement = squiggle + microSquiggle;
			int ix = (int) (startX + step * cosA * 1.5f - displacement * sinA);
			int iy = (int) (startY + step * sinA * 1.5f + displacement * cosA);

			if (ix + thickness < x || ix >= x + w || iy + thickness < y || iy >= y + h) {
				continue;
			}

			float tipFade = 1f;
			if (step < 10) {
				tipFade = step / 10f;
			} else if (step > length - 10) {
				tipFade = (length - step) / 10f;
			}
			float pulse = 0.72f + 0.28f * Mth.sin(time * 1.7f + index * 0.5f + step * 0.02f);
			int a = (int) Mth.clamp(tipFade * pulse * 165, 18, 185);
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse, 0, 255);
			gfx.fill(ix, iy, ix + thickness, iy + thickness, (a << 24) | (r << 16) | (g << 8) | b);
		}
	}

	private void drawNeuralBorder(GuiGraphics gfx, int x, int y, int w, int h, boolean bright) {
		int outer = bright ? NEURAL_BORDER_OUTER : NEURAL_BORDER_DIM;
		int inner = bright ? NEURAL_BORDER_INNER : NEURAL_BORDER_OUTER;
		gfx.fill(x, y, x + w, y + 1, outer);
		gfx.fill(x, y + h - 1, x + w, y + h, outer);
		gfx.fill(x, y, x + 1, y + h, outer);
		gfx.fill(x + w - 1, y, x + w, y + h, outer);
		gfx.fill(x + 1, y + 1, x + w - 1, y + 2, inner);
		gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, inner);
		gfx.fill(x + 1, y + 1, x + 2, y + h - 1, inner);
		gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, inner);
	}

	private ResourceLocation memoryOverlayTexture(String manipName) {
		String texture = switch (manipName) {
			case "conjure_axe" -> "memory_living_axe_overlay";
			case "conjure_blade" -> "memory_living_blade_overlay";
			case "conjure_claws" -> "memory_living_claws_overlay";
			case "conjure_crossbow" -> "memory_living_crossbow_overlay";
			case "conjure_flail" -> "memory_living_flail_overlay";
			case "conjure_spear" -> "memory_living_spear_overlay";
			case "conjure_staff" -> "memory_living_staff_overlay";
			case "conjure_torch" -> "memory_living_torch_overlay";
			default -> "memory_" + manipName + "_overlay";
		};
		return Hemomancy.rloc("textures/item/memories/" + texture + ".png");
	}

	private void cycle(int direction) {
		int slots = unlockedSlots();
		if (slots <= 0) {
			return;
		}
		selectedIndex = Math.floorMod(selectedIndex + direction, slots);
		confirmOverwrite = false;
		updateNameField();
		updateButtons();
	}

	private int previousIndex(int slots) {
		return Math.floorMod(selectedIndex - 1, slots);
	}

	private int nextIndex(int slots) {
		return Math.floorMod(selectedIndex + 1, slots);
	}

	private void sendApply() {
		PacketHandler.sendToServer(new SynapticLoadoutActionPacket(SynapticLoadoutActionPacket.Action.APPLY,
				distributorPos, selectedIndex, ""));
	}

	private void sendRename() {
		PacketHandler.sendToServer(new SynapticLoadoutActionPacket(SynapticLoadoutActionPacket.Action.RENAME,
				distributorPos, selectedIndex, nameField.getValue()));
	}

	private void sendSaveOrOverwrite() {
		ManipulationLoadout loadout = currentLoadout();
		if (loadout != null && !loadout.isEmpty() && !confirmOverwrite) {
			confirmOverwrite = true;
			updateButtons();
			return;
		}
		PacketHandler.sendToServer(new SynapticLoadoutActionPacket(SynapticLoadoutActionPacket.Action.SAVE_OR_OVERWRITE,
				distributorPos, selectedIndex, nameField.getValue()));
		confirmOverwrite = false;
		updateButtons();
	}

	private void updateNameField() {
		if (nameField == null) {
			return;
		}
		ManipulationLoadout loadout = currentLoadout();
		nameField.setValue(loadout != null ? loadout.name() : ManipulationLoadout.defaultName(selectedIndex));
	}

	private void updateButtons() {
		if (applyButton == null || renameButton == null || saveButton == null) {
			return;
		}
		ManipulationLoadout loadout = currentLoadout();
		boolean hasLoadout = loadout != null && !loadout.isEmpty();
		applyButton.active = hasLoadout;
		renameButton.active = hasLoadout;
		saveButton.setMessage(Component.literal(hasLoadout ? (confirmOverwrite ? "Confirm" : "Overwrite") : "Save"));
	}

	private ManipulationLoadout currentLoadout() {
		IKnownManipulations known = known();
		return known != null ? known.getLoadout(selectedIndex) : ManipulationLoadout.empty(selectedIndex);
	}

	private IKnownManipulations known() {
		return Minecraft.getInstance().player != null
				? HemoCapabilityAccess.getKnownManipulations(Minecraft.getInstance().player).orElse(null)
				: null;
	}

	private int unlockedSlots() {
		return Minecraft.getInstance().player != null
				? SynapticLoadoutSlotHelper.getClientUnlockedSlots()
				: SynapticLoadoutSlotHelper.BASE_LOADOUT_SLOTS;
	}

	private boolean centerPanelContains(double mouseX, double mouseY) {
		int frameW = frameWidth();
		int frameX = (width - frameW) / 2;
		int x = boundedCarouselCenter(frameX, frameW) - PANEL / 2;
		int y = carouselCenterY((height - frameHeight()) / 2) - PANEL / 2;
		return mouseX >= x && mouseX < x + PANEL && mouseY >= y && mouseY < y + PANEL;
	}

	private void renderFocusedTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
		if (!centerPanelContains(mouseX, mouseY)) {
			return;
		}
		ManipulationLoadout loadout = currentLoadout();
		List<Component> tooltip = new ArrayList<>();
		if (loadout == null || loadout.isEmpty()) {
			tooltip.add(Component.literal("Save current loadout"));
			tooltip.add(Component.literal("Cost: " + COST_BLOOD + " blood, " + COST_XP + " XP")
					.withStyle(ChatFormatting.GOLD));
		} else {
			tooltip.add(Component.literal(loadout.name()));
			tooltip.add(Component.literal(loadout.manipNames().size() + " remembered manipulations")
					.withStyle(ChatFormatting.GRAY));
		}
		gfx.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
	}

	private String trimToWidth(String text, int maxWidth) {
		if (font.width(text) <= maxWidth) {
			return text;
		}
		return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("..."))) + "...";
	}
}
