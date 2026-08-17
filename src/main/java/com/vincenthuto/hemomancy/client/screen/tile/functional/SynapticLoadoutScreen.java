package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemoryEntryKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemorySlotRef;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.SynapticLoadoutSlotHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationClientPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
	private int lastRenderedSlots = -1;
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
		addRenderableWidget(new SynapticButton(leftArrowX(carouselX, sidePanel, gap), carouselCenterY(frameY) - 10,
				22, 20, Component.literal("<"), button -> cycle(-1)));
		addRenderableWidget(new SynapticButton(rightArrowX(carouselX, sidePanel, gap), carouselCenterY(frameY) - 10,
				22, 20, Component.literal(">"), button -> cycle(1)));
		nameField = new EditBox(font, controlX + 8, controlY + 24, CONTROL_RAIL_W - 16, 18,
				Component.translatable("screen.hemomancy.synaptic_loadouts.name"));
		nameField.setMaxLength(ManipulationLoadout.MAX_NAME_LENGTH);
		addRenderableWidget(nameField);
		applyButton = addRenderableWidget(new SynapticButton(controlX + (CONTROL_RAIL_W - CONTROL_BUTTON_W) / 2,
				controlY + 50, CONTROL_BUTTON_W, 20, Component.literal("Apply"), button -> sendApply()));
		renameButton = addRenderableWidget(new SynapticButton(controlX + (CONTROL_RAIL_W - CONTROL_BUTTON_W) / 2,
				controlY + 74, CONTROL_BUTTON_W, 20, Component.literal("Rename"), button -> sendRename()));
		saveButton = addRenderableWidget(new SynapticButton(controlX + (CONTROL_RAIL_W - CONTROL_BUTTON_W) / 2,
				controlY + 98, CONTROL_BUTTON_W, 20, Component.literal("Save"), button -> sendSaveOrOverwrite()));
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
				&& displayLoadout(known, selectedIndex).isEmpty()) {
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
		if (lastRenderedSlots != slots) {
			lastRenderedSlots = slots;
			updateNameField();
			updateButtons();
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
		ManipulationLoadout selectedLoadout = displayLoadout(known, selectedIndex);
		if (slots > 1) {
			drawLoadoutPanel(gfx, displayLoadout(known, previousIndex(slots)), leftSidePanelX(centerX, sidePanel, gap),
					centerY - sidePanel / 2, sidePanel, false);
			drawLoadoutPanel(gfx, displayLoadout(known, nextIndex(slots)), rightSidePanelX(centerX, gap),
					centerY - sidePanel / 2, sidePanel, false);
		}
		drawLoadoutPanel(gfx, selectedLoadout, centerX - PANEL / 2, centerY - PANEL / 2,
				PANEL, true);
		drawFooterText(gfx, selectedLoadout, frameX + CONTROL_RAIL_W + 26,
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
			if (MemorySlotRef.fromStorageKey(names.get(i)).kind() == MemoryEntryKind.MANIPULATION) {
				gfx.blit(MEMORY_BASE, ix, iy, 0, 0, 16, 16, 16, 16);
			}
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
			lines.add(Component.literal(loadout.manipNames().size() + " remembered powers")
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
				drawJaggedNeuralArc(gfx, i, animTime, x, y, w, h);
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

	private void drawJaggedNeuralArc(GuiGraphics gfx, int index, float time, int x, int y, int w, int h) {
		float[] p = neuralVeinParams[index];
		float startX = x + p[0] * w;
		float startY = y + p[1] * h;
		float baseAngle = p[2];
		float speed = p[3];
		float amplitude = p[4];
		int length = (int) p[6];
		int thickness = (int) p[7];
		float brightness = p[8];

		float angleDrift = baseAngle + 0.08f * Mth.sin(time * speed * 0.5f + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);

		int baseRed = (int) (120 + 80 * brightness);
		int baseGreen = (int) (82 + 76 * brightness);
		int baseBlue = (int) (8 + 24 * brightness);
		int segments = Math.max(5, length / 14);
		float segmentLength = (float) length / segments;
		float lastX = startX;
		float lastY = startY;

		for (int segment = 1; segment <= segments; segment++) {
			float progress = (float) segment / segments;
			float previousProgress = (float) (segment - 1) / segments;
			float side = jaggedOffset(index, segment, time, amplitude);
			float endX = startX + segment * segmentLength * cosA * 1.5f - side * sinA;
			float endY = startY + segment * segmentLength * sinA * 1.5f + side * cosA;
			float pulse = 0.72f + 0.28f * Mth.sin(time * 2.8f + index * 0.7f + segment * 0.45f);
			float tipFade = Math.min(progress * 3f, (1f - previousProgress) * 3f);
			int a = (int) Mth.clamp(tipFade * pulse * 172, 18, 190);
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse, 0, 255);
			int color = (a << 24) | (r << 16) | (g << 8) | b;
			drawNeuralLine(gfx, Math.round(lastX), Math.round(lastY), Math.round(endX), Math.round(endY),
					thickness, color, x, y, w, h);

			if (segment > 1 && segment < segments && segment % 3 == 0) {
				float branchLength = segmentLength * (0.8f + 0.3f * ((index + segment) % 3));
				float branchSign = ((index + segment) & 1) == 0 ? 1f : -1f;
				float branchX = endX - branchSign * sinA * branchLength + cosA * branchLength * 0.35f;
				float branchY = endY + branchSign * cosA * branchLength + sinA * branchLength * 0.35f;
				drawNeuralLine(gfx, Math.round(endX), Math.round(endY), Math.round(branchX), Math.round(branchY),
						Math.max(1, thickness - 1), color & 0x99FFFFFF, x, y, w, h);
			}
			lastX = endX;
			lastY = endY;
		}
	}

	private float jaggedOffset(int index, int segment, float time, float amplitude) {
		int hash = index * 73471 + segment * 19349663;
		hash ^= hash >>> 13;
		hash *= 1274126177;
		float base = ((hash & 0xFF) / 255f - 0.5f) * 2f;
		float flicker = Mth.sin(time * 1.2f + index * 1.7f + segment * 2.1f) * 0.18f;
		return (base + flicker) * amplitude;
	}

	private void drawNeuralLine(GuiGraphics gfx, int x0, int y0, int x1, int y1,
			int thickness, int color, int clipX, int clipY, int clipW, int clipH) {
		int dx = x1 - x0;
		int dy = y1 - y0;
		int steps = Math.max(Math.abs(dx), Math.abs(dy));
		if (steps == 0) {
			drawNeuralPoint(gfx, x0, y0, thickness, color, clipX, clipY, clipW, clipH);
			return;
		}
		for (int step = 0; step <= steps; step++) {
			float t = (float) step / steps;
			int px = Math.round(x0 + dx * t);
			int py = Math.round(y0 + dy * t);
			drawNeuralPoint(gfx, px, py, thickness, color, clipX, clipY, clipW, clipH);
		}
	}

	private void drawNeuralPoint(GuiGraphics gfx, int px, int py, int thickness,
			int color, int clipX, int clipY, int clipW, int clipH) {
		if (px + thickness < clipX || px >= clipX + clipW || py + thickness < clipY || py >= clipY + clipH) {
			return;
		}
		gfx.fill(px, py, px + thickness, py + thickness, color);
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
		MemorySlotRef ref = MemorySlotRef.fromStorageKey(manipName);
		if (ref.kind() == MemoryEntryKind.MUSCLE_MEMORY) {
			return Hemomancy.rloc("textures/item/tincture_" + ref.id() + ".png");
		}
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
		return displayLoadout(known, selectedIndex);
	}

	private ManipulationLoadout displayLoadout(IKnownManipulations known, int slotIndex) {
		if (known == null || slotIndex < 0 || slotIndex >= unlockedSlots()
				|| slotIndex >= known.getLoadouts().size()) {
			return ManipulationLoadout.empty(slotIndex);
		}
		return known.getLoadout(slotIndex);
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
			tooltip.add(Component.literal(loadout.manipNames().size() + " remembered powers")
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

	private static void drawCenteredFittingString(GuiGraphics gfx, Font font, String text, int centerX, int y,
			int maxWidth, int color, boolean shadow) {
		String drawn = text;
		if (font.width(drawn) > maxWidth) {
			drawn = font.plainSubstrByWidth(drawn, Math.max(0, maxWidth - font.width("..."))) + "...";
		}
		gfx.drawString(font, drawn, centerX - font.width(drawn) / 2, y, color, shadow);
	}

	private static class SynapticButton extends Button {
		protected SynapticButton(int x, int y, int width, int height, Component message, OnPress onPress) {
			super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
		}

		@Override
		protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
			Minecraft minecraft = Minecraft.getInstance();
			boolean hot = isHoveredOrFocused();
			int fill = active ? (hot ? 0xFF5E4310 : 0xFF221707) : 0xFF100B04;
			int edge = active ? (hot ? 0xFFFFD66E : 0xFFC79A24) : 0xFF4E3710;
			int bottom = active ? 0xFF6C4D12 : 0xFF231808;
			int text = active ? 0xFFFFE8B2 : 0xFF8E7A4E;

			gfx.fill(getX(), getY(), getX() + width, getY() + height, 0xFF050301);
			gfx.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, fill);
			gfx.fill(getX(), getY(), getX() + width, getY() + 1, edge);
			gfx.fill(getX(), getY() + height - 1, getX() + width, getY() + height, bottom);
			gfx.fill(getX(), getY(), getX() + 1, getY() + height, edge);
			gfx.fill(getX() + width - 1, getY(), getX() + width, getY() + height, edge);
			if (hot && active) {
				gfx.fill(getX() + 2, getY() + 2, getX() + width - 2, getY() + height - 2, 0x22FFE08A);
			}
			drawCenteredFittingString(gfx, minecraft.font, getMessage().getString(), getX() + width / 2,
					getY() + (height - 8) / 2, Math.max(1, width - 8), text, false);
		}
	}
}
