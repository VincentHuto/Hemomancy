package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ferric.ConjurationManip;
import com.vincenthuto.hemomancy.common.menu.tile.functional.MnemonicReliquaryMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.EquipManipulationPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class MnemonicReliquaryScreen extends AbstractContainerScreen<MnemonicReliquaryMenu> {

	private static final int GUI_WIDTH = 360;
	private static final int GUI_HEIGHT = 280;
	private static final int BRAIN_RADIUS = 60;
	private static final int VEIN_COUNT = 28;
	private static final int ICON_SIZE = 16;
	private static final int SCREEN_PADDING = 8;

	private float[][] veinParams;
	private int guiLeft;
	private int guiTop;
	private int currentGuiWidth = GUI_WIDTH;
	private int currentGuiHeight = GUI_HEIGHT;
	private int currentBrainRadius = BRAIN_RADIUS;
	private int currentIconSize = ICON_SIZE;
	private float currentLayoutScale = 1.0F;

	private final Map<String, ItemStack> manipItemCache = new HashMap<>();
	private final Map<EnumBloodTendency, List<BloodManipulation>> tendencyGroups = new LinkedHashMap<>();
	private List<BloodManipulation> equippedManips = new ArrayList<>();
	private Set<String> equippedNames = new HashSet<>();
	private int maxSlots;

	private BloodManipulation draggingManip;

	private final List<ManipIcon> knownIcons = new ArrayList<>();
	private final List<ManipIcon> equippedIcons = new ArrayList<>();
	private int refreshTicker;

	public MnemonicReliquaryScreen(MnemonicReliquaryMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = GUI_WIDTH;
		this.imageHeight = GUI_HEIGHT;
	}

	@Override
	protected void init() {
		updateScaledLayout();
		super.init();
		updateScaledLayout();

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

		buildItemCache();
		refreshCapabilityData();
		buildLayout();
	}

	private void updateScaledLayout() {
		this.currentLayoutScale = fitScale(this.width, this.height);
		this.currentGuiWidth = Math.max(1, Math.round(GUI_WIDTH * currentLayoutScale));
		this.currentGuiHeight = Math.max(1, Math.round(GUI_HEIGHT * currentLayoutScale));
		this.currentBrainRadius = Math.max(24, Math.round(BRAIN_RADIUS * currentLayoutScale));
		this.currentIconSize = Math.max(10, Math.round(ICON_SIZE * currentLayoutScale));
		this.imageWidth = currentGuiWidth;
		this.imageHeight = currentGuiHeight;
		this.guiLeft = Math.max(SCREEN_PADDING, (this.width - currentGuiWidth) / 2);
		this.guiTop = Math.max(SCREEN_PADDING, (this.height - currentGuiHeight) / 2);
		this.leftPos = this.guiLeft;
		this.topPos = this.guiTop;
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

	private void buildItemCache() {
		manipItemCache.clear();
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof BloodMemoryItem memItem) {
				BloodManipulation manip = memItem.getManip();
				if (manip != null && !memItem.isRetiredMemoryItem()) {
					manipItemCache.put(manip.getName(), new ItemStack(item));
				}
			}
		}
	}

	private void refreshCapabilityData() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		maxSlots = ManipSlotHelper.getMaxSlots(player);
		equippedManips.clear();
		equippedNames.clear();
		tendencyGroups.clear();

		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			tendencyGroups.put(tend, new ArrayList<>());
		}

		HemoCapabilityAccess.getKnownManipulations(player).ifPresent(known -> {
			List<String> equipped = known.getEquippedManipNames();
			equippedNames.addAll(equipped);

			for (BloodManipulation manip : known.getKnownManips().keySet()) {
				if (ManipulationEquipHelper.isFixedMechanicalManip(manip.getName())
						|| ManipulationRetirementRules.isRetiredManipulation(manip)) {
					continue;
				}
				EnumBloodTendency tend = manip.getTend();
				if (tend != null && tendencyGroups.containsKey(tend)) {
					tendencyGroups.get(tend).add(manip);
				}
			}

			for (String name : equipped) {
				if (ManipulationEquipHelper.isFixedMechanicalManip(name)) {
					continue;
				}
				for (BloodManipulation manip : known.getKnownManips().keySet()) {
					if (manip.getName().equals(name)
							&& !ManipulationRetirementRules.isRetiredManipulation(manip)) {
						equippedManips.add(manip);
						break;
					}
				}
			}
		});

		for (List<BloodManipulation> list : tendencyGroups.values()) {
			list.sort(Comparator.comparingInt(m -> m.getRank().ordinal()));
		}
	}

	private void buildLayout() {
		knownIcons.clear();
		equippedIcons.clear();

		int cx = guiLeft + currentGuiWidth / 2;
		int cy = guiTop + currentGuiHeight / 2;
		int iconGap = scaled(2);

		EnumBloodTendency[] tendencies = EnumBloodTendency.values();
		double angleStep = (2.0 * Math.PI) / tendencies.length;
		int groupRadius = currentBrainRadius + scaled(42);

		for (int t = 0; t < tendencies.length; t++) {
			EnumBloodTendency tend = tendencies[t];
			double angle = -Math.PI / 2 + t * angleStep;
			int groupCx = cx + (int) (groupRadius * Math.cos(angle));
			int groupCy = cy + (int) (groupRadius * Math.sin(angle));

			List<BloodManipulation> manips = tendencyGroups.getOrDefault(tend, Collections.emptyList());
			int count = manips.size();
			int cols = Math.max(1, Math.min(count, 3));
			int rows = (count + cols - 1) / cols;
			int gridW = cols * (currentIconSize + iconGap);
			int gridH = rows * (currentIconSize + iconGap);
			int startX = groupCx - gridW / 2;
			int startY = groupCy - gridH / 2 + scaled(4);

			for (int i = 0; i < count; i++) {
				int col = i % cols;
				int row = i / cols;
				int ix = startX + col * (currentIconSize + iconGap);
				int iy = startY + row * (currentIconSize + iconGap);
				knownIcons.add(new ManipIcon(manips.get(i), ix, iy, tend));
			}
		}

		if (!equippedManips.isEmpty()) {
			int equipCount = equippedManips.size();
			double equipAngleStep = (2.0 * Math.PI) / equipCount;
			int equipRadius = Math.max(scaled(4),
					Math.min(currentBrainRadius - scaled(14), scaled(8) + equipCount * scaled(4)));
			for (int i = 0; i < equipCount; i++) {
				double angle = -Math.PI / 2 + i * equipAngleStep;
				int ix = cx - currentIconSize / 2 + (int) (equipRadius * Math.cos(angle));
				int iy = cy - currentIconSize / 2 + (int) (equipRadius * Math.sin(angle));
				equippedIcons.add(new ManipIcon(equippedManips.get(i), ix, iy, equippedManips.get(i).getTend()));
			}
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		refreshTicker++;
		if (refreshTicker >= 10) {
			refreshTicker = 0;
			refreshCapabilityData();
			buildLayout();
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBg(graphics, partialTicks, mouseX, mouseY);

		int gx = guiLeft;
		int gy = guiTop;

		renderVeinBackground(graphics, gx, gy, currentGuiWidth, currentGuiHeight);
		drawBorder(graphics, gx, gy, currentGuiWidth, currentGuiHeight);
		drawBrainArea(graphics, partialTicks);
		drawTendencyLabels(graphics);
		drawKnownManips(graphics, mouseX, mouseY);
		drawEquippedManips(graphics, mouseX, mouseY);
		drawCounter(graphics);

		if (draggingManip != null) {
			ItemStack stack = iconStackFor(draggingManip);
			if (stack != null) {
				renderScaledItem(graphics, stack, mouseX - currentIconSize / 2, mouseY - currentIconSize / 2);
			}
		}

		drawTooltips(graphics, mouseX, mouseY);
	}

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
			float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
			float microSquiggle = (amplitude * 0.3f)
					* Mth.sin(frequency * 2.7f * step + timeOffset * 1.4f + index);
			float displacement = squiggle + microSquiggle;

			float px = startX + step * cosA * 1.5f - displacement * sinA;
			float py = startY + step * sinA * 1.5f + displacement * cosA;

			int ix = (int) px;
			int iy = (int) py;

			if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) {
				continue;
			}

			float tipFade = 1f;
			if (step < 10) tipFade = step / 10f;
			else if (step > length - 10) tipFade = (length - step) / 10f;

			float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.02f);

			int a = (int) (Mth.clamp(tipFade * pulse * 180, 20, 200));
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

			graphics.fill(ix, iy, ix + thickness, iy + thickness,
					(a << 24) | (r << 16) | (g << 8) | b);
		}
	}

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
	private float animTime = 0f;

	private void drawBrainArea(GuiGraphics graphics, float partialTicks) {
		int cx = guiLeft + currentGuiWidth / 2;
		int cy = guiTop + currentGuiHeight / 2;

		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
				float pulseAlpha = 0.15f + 0.05f * Mth.sin(time * 2.0f);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		for (int ring = currentBrainRadius; ring > 0; ring -= 2) {
			float t = (float) ring / currentBrainRadius;
			int alpha = (int) (pulseAlpha * 255 * (1f - t));
			int red = (int) (180 * (1f - t * 0.5f));
			int color = (Mth.clamp(alpha, 0, 255) << 24) | (Mth.clamp(red, 0, 255) << 16);
			graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}

		int borderAlpha = (int) (100 + 30 * Mth.sin(time * 1.5f));
		int borderColor = (Mth.clamp(borderAlpha, 0, 255) << 24) | (0xAA << 16) | (0x10 << 8) | 0x10;
		int segments = 64;
		for (int i = 0; i < segments; i++) {
			double a1 = (2.0 * Math.PI * i) / segments;
			double a2 = (2.0 * Math.PI * (i + 1)) / segments;
			int x1 = cx + (int) (currentBrainRadius * Math.cos(a1));
			int y1 = cy + (int) (currentBrainRadius * Math.sin(a1));
			int x2 = cx + (int) (currentBrainRadius * Math.cos(a2));
			int y2 = cy + (int) (currentBrainRadius * Math.sin(a2));
			graphics.fill(Math.min(x1, x2), Math.min(y1, y2),
					Math.max(x1, x2) + 1, Math.max(y1, y2) + 1, borderColor);
		}

		RenderSystem.disableBlend();
	}

	private void drawKnownManips(GuiGraphics graphics, int mouseX, int mouseY) {
		for (ManipIcon icon : knownIcons) {
			ItemStack stack = iconStackFor(icon.manip);
			if (stack == null) continue;

			boolean equipped = equippedNames.contains(icon.manip.getName());
			renderScaledItem(graphics, stack, icon.x, icon.y);

			if (equipped) {
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				graphics.fill(icon.x, icon.y, icon.x + currentIconSize, icon.y + currentIconSize, 0x4000CC00);
				RenderSystem.disableBlend();
			}
		}
	}

	private void drawEquippedManips(GuiGraphics graphics, int mouseX, int mouseY) {
		for (ManipIcon icon : equippedIcons) {
			ItemStack stack = iconStackFor(icon.manip);
			if (stack == null) continue;
			renderScaledItem(graphics, stack, icon.x, icon.y);
		}
	}

	private ItemStack iconStackFor(BloodManipulation manip) {
		if (manip == null) {
			return null;
		}
		ItemStack cached = manipItemCache.get(manip.getName());
		if (cached != null) {
			return cached;
		}
		if (manip instanceof ConjurationManip conjuration) {
			ItemStack stack = new ItemStack(conjuration.getItem().get());
			manipItemCache.put(manip.getName(), stack);
			return stack;
		}
		return null;
	}

	private void renderScaledItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
		if (currentIconSize == ICON_SIZE) {
			graphics.renderItem(stack, x, y);
			return;
		}
		float iconScale = currentIconSize / (float) ICON_SIZE;
		graphics.pose().pushPose();
		graphics.pose().translate(x, y, 0.0F);
		graphics.pose().scale(iconScale, iconScale, 1.0F);
		graphics.renderItem(stack, 0, 0);
		graphics.pose().popPose();
	}

	private void drawCounter(GuiGraphics graphics) {
		int cx = guiLeft + currentGuiWidth / 2;
		int y = guiTop + scaled(6);
		String text = "Memorized: " + equippedManips.size() + " / " + maxSlots;
		graphics.drawCenteredString(font, text, cx, y, 0xFFDD2222);
	}

	private void drawTendencyLabels(GuiGraphics graphics) {
		int cx = guiLeft + currentGuiWidth / 2;
		int cy = guiTop + currentGuiHeight / 2;
		EnumBloodTendency[] tendencies = EnumBloodTendency.values();
		double angleStep = (2.0 * Math.PI) / tendencies.length;
		int labelRadius = currentBrainRadius + scaled(42);
		int iconGap = scaled(2);

		for (int t = 0; t < tendencies.length; t++) {
			EnumBloodTendency tend = tendencies[t];
			double angle = -Math.PI / 2 + t * angleStep;
			int lx = cx + (int) (labelRadius * Math.cos(angle));
			int ly = cy + (int) (labelRadius * Math.sin(angle));

			int r = (int) tend.getColor().getRed();
			int g = (int) tend.getColor().getGreen();
			int b = (int) tend.getColor().getBlue();
			int color = 0xFF000000 | (r << 16) | (g << 8) | b;

			List<BloodManipulation> manips = tendencyGroups.getOrDefault(tend, Collections.emptyList());
			int count = manips.size();
			int cols = Math.max(1, Math.min(count, 3));
			int rows = (count + cols - 1) / cols;
			int gridH = rows * (currentIconSize + iconGap);

			String name = HLTextUtils.convertInitToLang(tend.name());
			int textHalf = font.width(name) / 2;
			int textX = Mth.clamp(lx, guiLeft + textHalf + scaled(4),
					guiLeft + currentGuiWidth - textHalf - scaled(4));
			int textY = Mth.clamp(ly - gridH / 2 - scaled(10), guiTop + scaled(4),
					guiTop + currentGuiHeight - font.lineHeight - scaled(4));
			graphics.drawCenteredString(font, name, textX, textY, color);
		}
	}

	private void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		if (draggingManip != null) return;

		BloodManipulation hovered = getHoveredManip(mouseX, mouseY);
		if (hovered == null) return;

		List<Component> tooltip = new ArrayList<>();
		tooltip.add(Component.literal(hovered.getProperName()).withStyle(s -> s.withBold(true)));
		tooltip.add(Component.literal("Tendency: " + HLTextUtils.convertInitToLang(hovered.getTend().name())));
		tooltip.add(Component.literal("Rank: " + HLTextUtils.convertInitToLang(hovered.getRank().name())));
		tooltip.add(Component.literal("Type: " + HLTextUtils.convertInitToLang(hovered.getType().name())));
		tooltip.add(Component.literal(String.format("Cost: %.1f", hovered.getCost())));
		if (hovered.getCooldownTicks() > 0) {
			tooltip.add(Component.literal(String.format("Cooldown: %.1fs", hovered.getCooldownTicks() / 20.0)));
		}
		if (equippedNames.contains(hovered.getName())) {
			tooltip.add(Component.literal("Equipped").withStyle(s -> s.withColor(0x55FF55)));
		}

		graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
	}

	private BloodManipulation getHoveredManip(int mouseX, int mouseY) {
		for (ManipIcon icon : equippedIcons) {
			if (mouseX >= icon.x && mouseX < icon.x + currentIconSize
					&& mouseY >= icon.y && mouseY < icon.y + currentIconSize) {
				return icon.manip;
			}
		}
		for (ManipIcon icon : knownIcons) {
			if (mouseX >= icon.x && mouseX < icon.x + currentIconSize
					&& mouseY >= icon.y && mouseY < icon.y + currentIconSize) {
				return icon.manip;
			}
		}
		return null;
	}

	private boolean isInsideBrain(int mouseX, int mouseY) {
		int cx = guiLeft + currentGuiWidth / 2;
		int cy = guiTop + currentGuiHeight / 2;
		int dx = mouseX - cx;
		int dy = mouseY - cy;
		return (dx * dx + dy * dy) <= (currentBrainRadius * currentBrainRadius);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

		int mx = (int) mouseX;
		int my = (int) mouseY;

		for (ManipIcon icon : equippedIcons) {
			if (mx >= icon.x && mx < icon.x + currentIconSize
					&& my >= icon.y && my < icon.y + currentIconSize) {
				PacketHandler.sendToServer(
						new EquipManipulationPacket(icon.manip.getName(), false));
				equippedNames.remove(icon.manip.getName());
				equippedManips.remove(icon.manip);
				buildLayout();
				return true;
			}
		}

		for (ManipIcon icon : knownIcons) {
			if (mx >= icon.x && mx < icon.x + currentIconSize
					&& my >= icon.y && my < icon.y + currentIconSize) {
				if (!equippedNames.contains(icon.manip.getName())) {
					draggingManip = icon.manip;
				}
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (draggingManip != null) {
			int mx = (int) mouseX;
			int my = (int) mouseY;

			if (isInsideBrain(mx, my) && equippedManips.size() < maxSlots
					&& !equippedNames.contains(draggingManip.getName())) {
				PacketHandler.sendToServer(
						new EquipManipulationPacket(draggingManip.getName(), true));
				equippedNames.add(draggingManip.getName());
				equippedManips.add(draggingManip);
				buildLayout();
			}
			draggingManip = null;
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		// Intentionally left empty.
		// Calling super.renderBackground(...) from renderBg causes recursive
		// AbstractContainerScreen.renderBackground -> renderBg -> ... in 1.21.1.
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
	}

	private record ManipIcon(BloodManipulation manip, int x, int y, EnumBloodTendency tendency) {
	}
}
