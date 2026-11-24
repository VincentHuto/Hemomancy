package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.manips.ManipulationIconResolver;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import com.vincenthuto.hemomancy.common.manipulation.ferric.ConjurationManip;
import com.vincenthuto.hemomancy.common.menu.tile.functional.MnemonicReliquaryMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.EquipManipulationPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.util.HemoText;

/**
 * The open casket holds the Temper drawers and the preserved Noetic relic.
 * Its front plate lays out Thelemic tinctures beside the vascular regions they serve.
 */
public class MnemonicReliquaryScreen extends AbstractContainerScreen<MnemonicReliquaryMenu> {

	private static final ResourceLocation ATLAS = Hemomancy.rloc("textures/gui/mnemonic_reliquary.png");
	private static final int ATLAS_WIDTH = 424;
	private static final int ATLAS_HEIGHT = 512;
	private static final int GUI_WIDTH = 424;
	private static final int GUI_HEIGHT = 344;
	private static final int ICON_SIZE = 16;
	private static final int SCREEN_PADDING = 8;
	private static final long FAMILY_HOLD_MILLIS = 600L;
	private static final int FAMILY_DROPDOWN_BACKGROUND = 0xFF2A080D;
	private static final int FAMILY_MODE_BADGE = 0xFF4A0B14;
	private static final int FAMILY_MODE_MARKER = 0xFFFFD7DB;

	// Casket layout in unscaled panel pixels.
	private static final int EAVE_BOTTOM = 42;
	private static final int WALL_W = 104;
	private static final int FRONT_TOP = 174;
	private static final int PLINTH_TOP = 310;
	private static final int BRAIN_CY = 106;
	private static final int BRAIN_RX = 77;
	private static final int BRAIN_RY = 46;
	private static final int SOCKET_RX = 61;
	private static final int SOCKET_RY = 34;
	private static final int VASCULAR_CENTER = GUI_WIDTH / 2 + 7;
	private static final int TINCTURE_X = WALL_W + 23;
	private static final int VASCULAR_LABEL_X = VASCULAR_CENTER + 52;
	private static final int DRAWER_LABEL_H = 12;
	private static final int DRAWER_GAP = 4;
	private static final EnumBloodTemper[] LEFT_WALL = { EnumBloodTemper.ANIMUS, EnumBloodTemper.FLAMMEUS,
			EnumBloodTemper.DUCTILIS, EnumBloodTemper.LUX };
	private static final EnumBloodTemper[] RIGHT_WALL = { EnumBloodTemper.MORTEM, EnumBloodTemper.CONGEATIO,
			EnumBloodTemper.FERRIC, EnumBloodTemper.TENEBRIS };
	private static final VascularNode[] VASCULAR_NODES = {
			new VascularNode(EnumVeinSections.HEAD, VASCULAR_CENTER, 200),
			new VascularNode(EnumVeinSections.HEART, VASCULAR_CENTER - 8, 223),
			new VascularNode(EnumVeinSections.ARMS, VASCULAR_CENTER + 37, 246),
			new VascularNode(EnumVeinSections.BODY, VASCULAR_CENTER, 269),
			new VascularNode(EnumVeinSections.LEGS, VASCULAR_CENTER + 9, 292)
	};

	// Text and live accents use the same colors as the painted atlas.
	private static final int BONE = 0xFFD4C4A6;
	private static final int BONE_PALE = 0xFFE2D4B8;
	private static final int BONE_SHADE = 0xFF9C8C70;
	private static final int LACQUER_DEEP = 0xFF5C0C18;
	private static final int CAVITY = 0xFF220E12;
	private static final int VEIN_DIM = 0xFF5A2228;
	private static final int VEIN_CORE = 0xFFEC3440;
	private static final int VEIN_HOT = 0xFFFFAA96;

	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(VeinBackgroundRenderer.Style.CRIMSON);
	private int guiLeft;
	private int guiTop;
	private int currentGuiWidth = GUI_WIDTH;
	private int currentGuiHeight = GUI_HEIGHT;
	private int currentIconSize = ICON_SIZE;
	private float currentLayoutScale = 1.0F;
	private float animTime = 0f;

	private final Map<String, ItemStack> manipItemCache = new HashMap<>();
	private final Map<EnumBloodTemper, List<BloodManipulation>> temperGroups = new LinkedHashMap<>();
	private List<BloodManipulation> equippedManips = new ArrayList<>();
	private List<ThelemicMemory> knownThelemicMemories = new ArrayList<>();
	private List<ThelemicMemory> preparedThelemicMemories = new ArrayList<>();
	private List<ThelemicMemory> equippedThelemicMemories = new ArrayList<>();
	private Set<String> equippedNames = new HashSet<>();
	private final Set<String> availableManipNames = new HashSet<>();
	private int maxSlots;

	private BloodManipulation draggingManip;
	private ThelemicMemory draggingThelemicMemory;
	private ManipIcon heldFamilyIcon;
	private long familyHoldStarted;
	private double familyHoldX;
	private double familyHoldY;
	private boolean familyFanOpen;
	private final List<ManipIcon> familyFanIcons = new ArrayList<>();

	private final List<ManipIcon> knownIcons = new ArrayList<>();
	private final List<ManipIcon> equippedIcons = new ArrayList<>();
	private final List<int[]> emptySockets = new ArrayList<>();
	private final List<PreparedThelemicIcon> preparedThelemicIcons = new ArrayList<>();
	private final List<Drawer> drawers = new ArrayList<>();
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

		buildItemCache();
		refreshCapabilityData();
		buildLayout();
	}

	private void updateScaledLayout() {
		this.currentLayoutScale = fitScale(this.width, this.height);
		this.currentGuiWidth = Math.max(1, Math.round(GUI_WIDTH * currentLayoutScale));
		this.currentGuiHeight = Math.max(1, Math.round(GUI_HEIGHT * currentLayoutScale));
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

	/** Panel x in screen pixels. */
	private int px(int value) {
		return guiLeft + Math.round(value * currentLayoutScale);
	}

	/** Panel y in screen pixels. */
	private int py(int value) {
		return guiTop + Math.round(value * currentLayoutScale);
	}

	private void buildItemCache() {
		manipItemCache.clear();
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof BloodMemoryItem memItem) {
				BloodManipulation manip = memItem.getManip();
				if (manip != null) {
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
		knownThelemicMemories.clear();
		preparedThelemicMemories.clear();
		equippedThelemicMemories.clear();
		equippedNames.clear();
		availableManipNames.clear();
		temperGroups.clear();

		for (EnumBloodTemper tend : EnumBloodTemper.values()) {
			temperGroups.put(tend, new ArrayList<>());
		}

		var known = HemoCapabilityAccess.getKnownManipulations(player);
		List<String> equipped = known.getEquippedManipNames();
		equippedNames.addAll(equipped);
		known.getKnownManips().keySet().stream().filter(Objects::nonNull)
				.filter(known::isManipulationAvailable)
				.map(BloodManipulation::getName).forEach(availableManipNames::add);

		Set<String> listedFamilies = new HashSet<>();
		for (BloodManipulation manip : known.getKnownManips().keySet()) {
			if (!known.isManipulationAvailable(manip)) continue;
			if (ManipulationEquipHelper.isFixedMechanicalManip(manip.getName())) {
				continue;
			}
			String displayId = ManipulationFamilyRegistry.baselineId(manip.getName());
			if (!listedFamilies.add(displayId)) continue;
			BloodManipulation displayManip = availableManipNames.contains(displayId)
					? ManipulationInit.getByName(displayId) : manip;
			if (displayManip == null) displayManip = manip;
			EnumBloodTemper tend = displayManip.getTend();
			if (tend != null && temperGroups.containsKey(tend)) {
				temperGroups.get(tend).add(displayManip);
			}
		}

		ManipulationWheelOrder.resolve(known.getManipList(), equipped).stream()
				.map(ManipulationWheelOrder.Entry::manipulation)
				.forEach(equippedManips::add);
		var thelemicState = HemoCapabilityAccess.thelemicMemory(player);
		knownThelemicMemories.addAll(thelemicState.knownMemories());
		knownThelemicMemories.sort(Comparator.comparingInt(Enum::ordinal));
		for (ThelemicMemory memory : knownThelemicMemories) {
			if (thelemicState.reserveTicks(memory) > 0) {
				preparedThelemicMemories.add(memory);
			}
			if (equippedNames.contains(MemorySlotRef.thelemicMemory(memory).storageKey())) {
				equippedThelemicMemories.add(memory);
			}
		}

		for (List<BloodManipulation> list : temperGroups.values()) {
			list.sort(Comparator.comparingInt(m -> m.getRank().ordinal()));
		}
	}

	private void buildLayout() {
		knownIcons.clear();
		equippedIcons.clear();
		emptySockets.clear();
		preparedThelemicIcons.clear();
		drawers.clear();

		layoutWall(LEFT_WALL, 0);
		layoutWall(RIGHT_WALL, GUI_WIDTH - WALL_W);
		layoutTinctures();
		layoutSockets();
	}

	/**
	 * Stacks a drawer per Temper down one side wall. Each drawer is as tall as its memories need and the spare height
	 * is shared out; a wall that cannot hold its memories at full size draws its icons smaller, never past its foot.
	 */
	private void layoutWall(EnumBloodTemper[] tempers, int wallX) {
		int innerLeft = px(wallX + 8);
		int innerWidth = px(wallX + WALL_W - 8) - innerLeft;
		int top = py(EAVE_BOTTOM + 6);
		int bottom = py(PLINTH_TOP - 6);
		int labelH = scaled(DRAWER_LABEL_H);
		int gap = scaled(DRAWER_GAP);
		int iconGap = scaled(2);
		int pad = scaled(3);

		int[] counts = new int[tempers.length];
		for (int t = 0; t < tempers.length; t++) counts[t] = temperGroups.getOrDefault(tempers[t], Collections.emptyList()).size();

		int size = currentIconSize;
		int minSize = Math.max(8, Math.round(currentIconSize * 0.6F));
		int cols;
		int needed;
		while (true) {
			int step = size + iconGap;
			cols = Math.max(1, (innerWidth - pad * 2 + iconGap) / step);
			needed = gap * (tempers.length - 1);
			for (int count : counts) needed += labelH + pad * 2 + Math.max(1, (count + cols - 1) / cols) * step;
			if (needed <= bottom - top || size <= minSize) break;
			size--;
		}
		int step = size + iconGap;
		int spare = Math.max(0, (bottom - top) - needed) / tempers.length;

		int y = top;
		for (int t = 0; t < tempers.length; t++) {
			EnumBloodTemper tend = tempers[t];
			int rows = Math.max(1, (counts[t] + cols - 1) / cols);
			int height = labelH + pad * 2 + rows * step + spare;
			drawers.add(new Drawer(tend, innerLeft, y, innerWidth, height));

			List<BloodManipulation> entries = temperGroups.getOrDefault(tend, Collections.emptyList());
			int gridLeft = innerLeft + pad;
			int gridTop = y + labelH + pad;
			for (int i = 0; i < entries.size(); i++) {
				int ix = gridLeft + (i % cols) * step;
				int iy = gridTop + (i / cols) * step;
				knownIcons.add(new ManipIcon(entries.get(i), ix, iy, size, tend));
			}
			y += height + gap;
		}
	}

	private void layoutTinctures() {
		for (VascularNode node : VASCULAR_NODES) {
			int x = px(TINCTURE_X) - currentIconSize / 2;
			int y = py(node.y) - currentIconSize / 2;
			for (ThelemicMemory memory : preparedThelemicMemories) {
				if (memory.section() != node.section) continue;
				preparedThelemicIcons.add(new PreparedThelemicIcon(memory, x, y, currentIconSize));
				x += currentIconSize + scaled(6);
			}
		}
	}

	/**
	 * A socket on the relic's cortex for every memory carried and every slot still free, round the brain from its
	 * left temple over the crown to its right, leaving the stem's side open.
	 */
	private void layoutSockets() {
		int free = Math.max(0, maxSlots - ManipulationEquipHelper.countNormalEquippedNames(new ArrayList<>(equippedNames)));
		int count = equippedManips.size() + free;
		if (count == 0) return;
		int cx = px(GUI_WIDTH / 2);
		int cy = py(BRAIN_CY);
		double rx = SOCKET_RX * currentLayoutScale;
		double ry = SOCKET_RY * currentLayoutScale;
		double start = Math.toRadians(140);
		double sweep = Math.toRadians(260);
		for (int i = 0; i < count; i++) {
			double angle = count == 1 ? Math.toRadians(270) : start + sweep * i / (count - 1);
			int ix = cx - currentIconSize / 2 + (int) Math.round(rx * Math.cos(angle));
			int iy = cy - currentIconSize / 2 + (int) Math.round(ry * Math.sin(angle));
			if (i < equippedManips.size()) {
				BloodManipulation manip = equippedManips.get(i);
				equippedIcons.add(new ManipIcon(manip, ix, iy, currentIconSize, manip.getTend()));
			} else {
				emptySockets.add(new int[] { ix, iy });
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
		updateFamilyHold(mouseX, mouseY);
		this.renderTransparentBackground(graphics);

		animTime += 0.016f; // ~60 FPS approximation
		drawCasket(graphics);
		drawVascularChannels(graphics);
		graphics.drawCenteredString(font, this.title, px(GUI_WIDTH / 2), py(12), BONE_PALE);
		graphics.drawCenteredString(font, Component.translatable("screen.hemomancy.mnemonic_reliquary.noetic_memories"),
				px(GUI_WIDTH / 2), py(46), BONE);

		drawRelic(graphics);
		drawMemoryVeins(graphics);
		drawSockets(graphics);
		drawEquippedManips(graphics, mouseX, mouseY);
		drawDrawers(graphics);
		drawKnownManips(graphics, mouseX, mouseY);
		drawPreparedTinctures(graphics);
		drawVascularSectionSlots(graphics);
		drawFamilyFan(graphics, mouseX, mouseY);
		drawCounter(graphics);

		if (draggingManip != null) {
			renderManipulationIcon(graphics, draggingManip,
					mouseX - currentIconSize / 2, mouseY - currentIconSize / 2, currentIconSize);
		}
		if (draggingThelemicMemory != null) {
			renderScaledItem(graphics, tinctureStackFor(draggingThelemicMemory),
					mouseX - currentIconSize / 2, mouseY - currentIconSize / 2, currentIconSize);
		}
		drawTooltips(graphics, mouseX, mouseY);
	}

	private float pulse(float speed, float phase) {
		return 0.5F + 0.5F * Mth.sin(animTime * speed + phase);
	}

	// ── The casket ──────────────────────────────────────────────

	private void drawCasket(GuiGraphics graphics) {
		int left = px(WALL_W);
		int top = py(EAVE_BOTTOM);
		veins.draw(graphics, left, top, px(GUI_WIDTH - WALL_W) - left, py(FRONT_TOP) - top, animTime);
		blitAtlas(graphics, guiLeft, guiTop, currentGuiWidth, currentGuiHeight, 0, 0, GUI_WIDTH, GUI_HEIGHT);
	}

	private void drawVascularChannels(GuiGraphics graphics) {
		for (VascularNode node : VASCULAR_NODES) {
			boolean matching = draggingThelemicMemory != null && draggingThelemicMemory.section() == node.section;
			int y = py(node.y);
			int colour = matching ? VEIN_CORE : VEIN_DIM;
			graphics.fill(px(TINCTURE_X + 36), y, px(VASCULAR_LABEL_X - 3), y + 1, colour);
		}
		graphics.drawCenteredString(font, Component.translatable("screen.hemomancy.mnemonic_reliquary.thelemic_memories"),
				px(GUI_WIDTH / 2), py(FRONT_TOP + 4), BONE);
	}

	private static void blitAtlas(GuiGraphics graphics, int x, int y, int width, int height,
			int u, int v, int sourceWidth, int sourceHeight) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		graphics.blit(ATLAS, x, y, width, height, (float) u, (float) v, sourceWidth, sourceHeight, ATLAS_WIDTH, ATLAS_HEIGHT);
		RenderSystem.disableBlend();
	}

	// ── The relic ───────────────────────────────────────────────

	/** The painted relic sits above the cavity's live veins. */
	private void drawRelic(GuiGraphics graphics) {
		int cx = px(GUI_WIDTH / 2);
		int cy = py(BRAIN_CY);
		double rx = BRAIN_RX * currentLayoutScale;
		double ry = BRAIN_RY * currentLayoutScale;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		int haloAlpha = (int) ((draggingManip != null ? 68 : 22) + 24 * pulse(1.6F, 0));
		for (int ring = 3; ring >= 1; ring--) {
			int grow = scaled(ring * 3);
			fillEllipse(graphics, cx, cy, rx + grow, ry + grow, (haloAlpha / ring) << 24 | 0xEC3440);
		}
		RenderSystem.disableBlend();
		int x = px(125);
		int y = py(61);
		blitAtlas(graphics, x, y, px(299) - x, py(165) - y, 0, 344, 174, 104);
	}

	private static void fillEllipse(GuiGraphics graphics, int cx, int cy, double rx, double ry, int colour) {
		int rows = (int) Math.ceil(ry);
		for (int dy = -rows; dy <= rows; dy++) {
			double norm = dy / ry;
			int half = (int) Math.round(rx * Math.sqrt(Math.max(0, 1 - norm * norm)));
			if (half > 0) graphics.fill(cx - half, cy + dy, cx + half, cy + dy + 1, colour);
		}
	}

	/** A vein from the stem to each socket: lit and beating for a carried memory, dim for a free slot. */
	private void drawMemoryVeins(GuiGraphics graphics) {
		int cx = px(GUI_WIDTH / 2);
		int cy = py(BRAIN_CY);
		int stemY = cy + (int) (BRAIN_RY * currentLayoutScale * 0.55);
		for (int[] socket : emptySockets) {
			drawVein(graphics, cx, cy, stemY, socket[0], socket[1], VEIN_DIM, false);
		}
		for (int i = 0; i < equippedIcons.size(); i++) {
			ManipIcon icon = equippedIcons.get(i);
			int colour = FastColor.ARGB32.lerp(pulse(2.4F, i * 0.7F), VEIN_CORE, VEIN_HOT);
			drawVein(graphics, cx, cy, stemY, icon.x, icon.y, colour, true);
		}
	}

	private void drawVein(GuiGraphics graphics, int cx, int cy, int stemY, int iconX, int iconY, int colour, boolean lit) {
		int sx = iconX + currentIconSize / 2;
		int sy = iconY + currentIconSize / 2;
		int c1y = stemY - (int) (BRAIN_RY * currentLayoutScale * 0.4);
		int c2x = cx + (sx - cx) / 2;
		if (lit) {
			ScreenDrawUtils.drawCubicTrace(graphics, cx, stemY, cx, c1y, c2x, sy, sx, sy, 3, 0xFF7A1A22);
		}
		ScreenDrawUtils.drawCubicTrace(graphics, cx, stemY, cx, c1y, c2x, sy, sx, sy, 1, colour);
	}

	/** Bone-rimmed sockets set in the cortex; a carried memory's socket glows. */
	private void drawSockets(GuiGraphics graphics) {
		for (int[] socket : emptySockets) {
			drawSocket(graphics, socket[0], socket[1], false, 0);
		}
		for (int i = 0; i < equippedIcons.size(); i++) {
			drawSocket(graphics, equippedIcons.get(i).x, equippedIcons.get(i).y, true, i);
		}
	}

	private void drawSocket(GuiGraphics graphics, int x, int y, boolean filled, int index) {
		int ring = scaled(2);
		int inner = filled ? FastColor.ARGB32.lerp(pulse(2.4F, index * 0.7F), 0xFF4A0A12, 0xFF8A1622) : CAVITY;
		graphics.fill(x, y, x + currentIconSize, y + currentIconSize, inner);
		int size = currentIconSize + ring * 2;
		blitAtlas(graphics, x - ring, y - ring, size, size, 176, 384, 20, 20);
		if (!filled) {
			int c = currentIconSize / 2;
			graphics.fill(x + c - 1, y + c - 1, x + c + 1, y + c + 1, VEIN_DIM);
		}
	}

	private void drawEquippedManips(GuiGraphics graphics, int mouseX, int mouseY) {
		for (ManipIcon icon : equippedIcons) {
			renderManipulationIcon(graphics, icon.manip, icon.x, icon.y, icon.size);
		}
	}

	// ── The drawers ─────────────────────────────────────────────

	/** Each Temper's drawer: a bone-lipped recess under a label strip with the Temper's colour pip. */
	private void drawDrawers(GuiGraphics graphics) {
		int labelH = scaled(DRAWER_LABEL_H);
		for (Drawer drawer : drawers) {
			blitAtlas(graphics, drawer.x, drawer.y + labelH, drawer.width, drawer.height - labelH, 176, 356, 88, 24);
			blitAtlas(graphics, drawer.x, drawer.y, drawer.width, labelH, 176, 344, 88, 12);
			ScreenDrawUtils.drawSimpleBorder(graphics, drawer.x, drawer.y, drawer.width, drawer.height, BONE_SHADE);

			int pip = Math.max(4, scaled(6));
			int pipX = drawer.x + scaled(3);
			int pipY = drawer.y + (labelH - pip) / 2 + 1;
			graphics.fill(pipX - 1, pipY - 1, pipX + pip + 1, pipY + pip + 1, BONE);
			graphics.fill(pipX, pipY, pipX + pip, pipY + pip, temperColour(drawer.temper));
			graphics.drawString(font, HLTextUtils.convertInitToLang(drawer.temper.name()), pipX + pip + scaled(4),
					drawer.y + (labelH - font.lineHeight) / 2 + 2, BONE_PALE, false);
		}
	}

	private static int temperColour(EnumBloodTemper tend) {
		int r = (int) tend.getColor().getRed();
		int g = (int) tend.getColor().getGreen();
		int b = (int) tend.getColor().getBlue();
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	private void drawKnownManips(GuiGraphics graphics, int mouseX, int mouseY) {
		for (ManipIcon icon : knownIcons) {
			boolean equipped = equippedNames.contains(icon.manip.getName());
			if (equipped) drawCarriedMark(graphics, icon.x, icon.y, icon.size);
			renderManipulationIcon(graphics, icon.manip, icon.x, icon.y, icon.size);
			drawFamilyModeIndicator(graphics, icon);
		}
	}

	/** A memory already carried sits on a lit vein-red bed in its drawer. */
	private void drawCarriedMark(GuiGraphics graphics, int x, int y, int size) {
		graphics.fill(x - 1, y - 1, x + size + 1, y + size + 1, VEIN_CORE);
		graphics.fill(x, y, x + size, y + size, 0xFF6A0E18);
	}

	private void drawFamilyModeIndicator(GuiGraphics graphics, ManipIcon icon) {
		if (unlockedFamilyForms(icon.manip).isEmpty()) return;
		int badgeSize = Math.max(5, scaled(6));
		int badgeX = icon.x + icon.size - badgeSize + 1;
		int badgeY = icon.y - 1;
		int centerX = badgeX + badgeSize / 2;
		int centerY = badgeY + badgeSize / 2;
		graphics.pose().pushPose();
		graphics.pose().translate(0.0F, 0.0F, 200.0F);
		graphics.fill(badgeX, badgeY, badgeX + badgeSize, badgeY + badgeSize, FAMILY_MODE_BADGE);
		graphics.fill(badgeX + 1, centerY, badgeX + badgeSize - 1, centerY + 1, FAMILY_MODE_MARKER);
		graphics.fill(centerX, badgeY + 1, centerX + 1, badgeY + badgeSize - 1, FAMILY_MODE_MARKER);
		graphics.pose().popPose();
	}

	private void drawFamilyFan(GuiGraphics graphics, int mouseX, int mouseY) {
		if (!familyFanOpen || familyFanIcons.isEmpty()) return;
		graphics.pose().pushPose();
		graphics.pose().translate(0.0F, 0.0F, 300.0F);
		int padding = scaled(4);
		int minX = familyFanIcons.stream().mapToInt(ManipIcon::x).min().orElse(0);
		int minY = familyFanIcons.stream().mapToInt(ManipIcon::y).min().orElse(0);
		int maxX = familyFanIcons.stream().mapToInt(icon -> icon.x() + currentIconSize).max().orElse(0);
		int maxY = familyFanIcons.stream().mapToInt(icon -> icon.y() + currentIconSize).max().orElse(0);
		graphics.fill(minX - padding, minY - padding, maxX + padding, maxY + padding,
				FAMILY_DROPDOWN_BACKGROUND);
		ScreenDrawUtils.drawSimpleBorder(graphics, minX - padding, minY - padding, maxX - minX + padding * 2,
				maxY - minY + padding * 2, BONE_SHADE);
		for (ManipIcon icon : familyFanIcons) {
			boolean hovered = contains(icon, mouseX, mouseY);
			graphics.fill(icon.x - 2, icon.y - 2, icon.x + currentIconSize + 2,
					icon.y + currentIconSize + 2, hovered ? 0xFFAA3333 : 0xFF441111);
			if (equippedNames.contains(icon.manip.getName())) drawCarriedMark(graphics, icon.x, icon.y, currentIconSize);
			renderManipulationIcon(graphics, icon.manip, icon.x, icon.y, currentIconSize);
		}
		graphics.pose().popPose();
	}

	private void drawPreparedTinctures(GuiGraphics graphics) {
		for (PreparedThelemicIcon icon : preparedThelemicIcons) {
			if (equippedNames.contains(MemorySlotRef.thelemicMemory(icon.memory).storageKey())) {
				drawCarriedMark(graphics, icon.x, icon.y, icon.size);
			}
			renderScaledItem(graphics, tinctureStackFor(icon.memory), icon.x, icon.y, icon.size);
		}
	}

	private void drawVascularSectionSlots(GuiGraphics graphics) {
		for (VascularNode node : VASCULAR_NODES) {
			int x = px(node.x) - currentIconSize / 2;
			int y = py(node.y) - currentIconSize / 2;
			ThelemicMemory assigned = equippedThelemicMemories.stream()
					.filter(memory -> memory.section() == node.section).findFirst().orElse(null);
			drawSocket(graphics, x, y, assigned != null, node.section.ordinal());
			if (assigned != null) renderScaledItem(graphics, tinctureStackFor(assigned), x, y, currentIconSize);
			boolean matching = draggingThelemicMemory != null && draggingThelemicMemory.section() == node.section;
			if (matching) {
				int rim = scaled(2);
				ScreenDrawUtils.drawSimpleBorder(graphics, x - rim, y - rim,
						currentIconSize + rim * 2, currentIconSize + rim * 2, VEIN_HOT);
			}
			int labelColour = matching ? VEIN_HOT : assigned != null ? BONE_PALE : BONE_SHADE;
			graphics.drawString(font, sectionName(node.section), px(VASCULAR_LABEL_X),
					py(node.y) - font.lineHeight / 2, labelColour, false);
		}
	}

	private ItemStack tinctureStackFor(ThelemicMemory memory) {
		return new ItemStack(BuiltInRegistries.ITEM.get(Hemomancy.rloc("tincture_" + memory.id())));
	}

	private static Component sectionName(EnumVeinSections section) {
		return Component.translatable("screen.hemomancy.mnemonic_reliquary.section."
				+ section.name().toLowerCase(Locale.ROOT));
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

	private void renderScaledItem(GuiGraphics graphics, ItemStack stack, int x, int y, int size) {
		if (size == ICON_SIZE) {
			graphics.renderItem(stack, x, y);
			return;
		}
		float iconScale = size / (float) ICON_SIZE;
		graphics.pose().pushPose();
		graphics.pose().translate(x, y, 0.0F);
		graphics.pose().scale(iconScale, iconScale, 1.0F);
		graphics.renderItem(stack, 0, 0);
		graphics.pose().popPose();
	}

	/** The count of what is carried, on a bone plaque in the lid's inscription band. */
	private void drawCounter(GuiGraphics graphics) {
		int cx = px(GUI_WIDTH / 2);
		int used = ManipulationEquipHelper.countNormalEquippedNames(new ArrayList<>(equippedNames));
		Component text = Component.translatable("screen.hemomancy.mnemonic_reliquary.memorized", used, maxSlots);
		int half = Math.max(scaled(46), font.width(text) / 2 + scaled(6));
		int top = py(23);
		int height = font.lineHeight + scaled(3);
		ScreenDrawUtils.drawSimpleBorder(graphics, cx - half, top, half * 2, height, BONE_SHADE);
		graphics.fill(cx - half + 1, top + 1, cx + half - 1, top + height - 1, LACQUER_DEEP);
		graphics.drawCenteredString(font, text, cx, top + scaled(2), used >= maxSlots ? VEIN_HOT : BONE);
	}

	// ── Tooltips and hit tests ──────────────────────────────────

	private void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		if (draggingManip != null || draggingThelemicMemory != null) return;
		if (familyFanOpen) {
			BloodManipulation hovered = getHoveredFamilyFanManip(mouseX, mouseY);
			if (hovered != null) drawManipTooltip(graphics, mouseX, mouseY, hovered);
			return;
		}
		PreparedThelemicIcon prepared = getHoveredPreparedThelemic(mouseX, mouseY);
		if (prepared != null) {
			ThelemicMemory memory = prepared.memory;
			long seconds = (HemoCapabilityAccess.thelemicMemory(Minecraft.getInstance().player)
					.reserveTicks(memory) + 19L) / 20L;
			List<Component> tooltip = List.of(
					Component.translatable("item.hemomancy.tincture_" + memory.id()).withStyle(s -> s.withBold(true)),
					Component.translatable("screen.hemomancy.mnemonic_reliquary.prepared_thelemic_memory"),
					HemoText.translatable("screen.hemomancy.mnemonic_reliquary.vascular_section", HLTextUtils.convertInitToLang(memory.section().name())),
					HemoText.translatable("screen.hemomancy.mnemonic_reliquary.reserve", String.format(Locale.ROOT, "%d", seconds / 60L), String.format(Locale.ROOT, "%02d", seconds % 60L)));
			graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
			return;
		}

		EnumVeinSections hoveredSection = getHoveredSection(mouseX, mouseY);
		if (hoveredSection != null) {
			ThelemicMemory hoveredMemory = equippedThelemicMemories.stream()
					.filter(memory -> memory.section() == hoveredSection).findFirst().orElse(null);
			List<Component> tooltip = new ArrayList<>();
			tooltip.add(HemoText.translatable("screen.hemomancy.mnemonic_reliquary.vascular_section",
					HLTextUtils.convertInitToLang(hoveredSection.name())).withStyle(s -> s.withBold(true)));
			if (hoveredMemory != null) {
				tooltip.add(Component.translatable("item.hemomancy.tincture_" + hoveredMemory.id()));
			}
			tooltip.add(Component.translatable("screen.hemomancy.mnemonic_reliquary.click_to_cycle_learned_thelemic_memories"));
			graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
			return;
		}

		BloodManipulation hovered = getHoveredManip(mouseX, mouseY);
		if (hovered != null) {
			drawManipTooltip(graphics, mouseX, mouseY, hovered);
			return;
		}
		if (getHoveredEmptySocket(mouseX, mouseY) != null) {
			graphics.renderTooltip(font, Component.translatable("screen.hemomancy.mnemonic_reliquary.empty_socket"),
					mouseX, mouseY);
		}
	}

	private void drawManipTooltip(GuiGraphics graphics, int mouseX, int mouseY, BloodManipulation hovered) {
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(hovered.getDisplayName().withStyle(s -> s.withBold(true)));
		tooltip.add(Component.translatable("screen.hemomancy.mnemonic_reliquary.noetic_memory"));
		tooltip.add(HemoText.translatable("screen.hemomancy.mnemonic_reliquary.temper", HLTextUtils.convertInitToLang(hovered.getTend().name())));
		tooltip.add(HemoText.translatable("screen.hemomancy.mnemonic_reliquary.rank", HLTextUtils.convertInitToLang(hovered.getRank().name())));
		tooltip.add(HemoText.translatable("screen.hemomancy.mnemonic_reliquary.type", HLTextUtils.convertInitToLang(hovered.getType().name())));
		tooltip.add(HemoText.translatable("screen.hemomancy.mnemonic_reliquary.cost", String.format("%.1f", hovered.getCost())));
		if (hovered.getCooldownTicks() > 0) {
			tooltip.add(HemoText.translatable("screen.hemomancy.mnemonic_reliquary.cooldown_s", String.format("%.1f", hovered.getCooldownTicks() / 20.0)));
		}
		if (equippedNames.contains(hovered.getName())) {
			tooltip.add(Component.translatable("screen.hemomancy.mnemonic_reliquary.equipped").withStyle(s -> s.withColor(0x55FF55)));
		}
		boolean knownFamilyIcon = knownIcons.stream()
				.anyMatch(icon -> icon.manip == hovered && contains(icon, mouseX, mouseY));
		if (knownFamilyIcon && !unlockedFamilyForms(hovered).isEmpty()) {
			tooltip.add(Component.translatable("screen.hemomancy.mnemonic_reliquary.hold_to_choose_variation").withStyle(ChatFormatting.LIGHT_PURPLE));
		}

		graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
	}

	private BloodManipulation getHoveredManip(int mouseX, int mouseY) {
		BloodManipulation familyFanManip = getHoveredFamilyFanManip(mouseX, mouseY);
		if (familyFanManip != null) return familyFanManip;
		for (ManipIcon icon : equippedIcons) {
			if (contains(icon, mouseX, mouseY)) return icon.manip;
		}
		for (ManipIcon icon : knownIcons) {
			if (contains(icon, mouseX, mouseY)) return icon.manip;
		}
		return null;
	}

	private int[] getHoveredEmptySocket(int mouseX, int mouseY) {
		for (int[] socket : emptySockets) {
			if (mouseX >= socket[0] && mouseX < socket[0] + currentIconSize
					&& mouseY >= socket[1] && mouseY < socket[1] + currentIconSize) return socket;
		}
		return null;
	}

	private EnumVeinSections getHoveredSection(int mouseX, int mouseY) {
		for (VascularNode node : VASCULAR_NODES) {
			int x = px(node.x) - currentIconSize / 2;
			int y = py(node.y) - currentIconSize / 2;
			if (mouseX >= x && mouseX < x + currentIconSize
					&& mouseY >= y && mouseY < y + currentIconSize) return node.section;
		}
		return null;
	}

	private BloodManipulation getHoveredFamilyFanManip(int mouseX, int mouseY) {
		for (ManipIcon icon : familyFanIcons) {
			if (familyFanOpen && contains(icon, mouseX, mouseY)) return icon.manip;
		}
		return null;
	}

	private void renderManipulationIcon(GuiGraphics graphics, BloodManipulation manip, int x, int y, int size) {
		ItemStack stack = iconStackFor(manip);
		if (stack != null) {
			renderScaledItem(graphics, stack, x, y, size);
			return;
		}
		float scale = size / 16.0F;
		graphics.pose().pushPose();
		graphics.pose().translate(x, y, 0.0F);
		graphics.pose().scale(scale, scale, 1.0F);
		graphics.blit(ManipulationIconResolver.base(manip.getName()), 0, 0, 0, 0, 16, 16, 16, 16);
		graphics.blit(ManipulationIconResolver.overlay(manip.getName()), 0, 0, 0, 0, 16, 16, 16, 16);
		graphics.pose().popPose();
	}

	private PreparedThelemicIcon getHoveredPreparedThelemic(int mouseX, int mouseY) {
		for (PreparedThelemicIcon icon : preparedThelemicIcons) {
			if (mouseX >= icon.x && mouseX < icon.x + icon.size
					&& mouseY >= icon.y && mouseY < icon.y + icon.size) return icon;
		}
		return null;
	}

	private void cycleVascularSection(EnumVeinSections section) {
		ThelemicMemory current = equippedThelemicMemories.stream()
				.filter(memory -> memory.section() == section).findFirst().orElse(null);
		ThelemicMemory next = MemoryEquipRules.nextPreparedInSection(preparedThelemicMemories, current, section);
		if (current == null && next == null) return;
		if (current == null && ManipulationEquipHelper.countNormalEquippedNames(new ArrayList<>(equippedNames)) >= maxSlots) return;
		if (current != null) {
			String currentKey = MemorySlotRef.thelemicMemory(current).storageKey();
			PacketHandler.sendToServer(new EquipManipulationPacket(currentKey, false));
			equippedNames.remove(currentKey);
			equippedThelemicMemories.remove(current);
		}
		if (next != null) {
			String nextKey = MemorySlotRef.thelemicMemory(next).storageKey();
			PacketHandler.sendToServer(new EquipManipulationPacket(nextKey, true));
			equippedNames.add(nextKey);
			equippedThelemicMemories.add(next);
		}
		buildLayout();
	}

	/** Anywhere on the relic, its halo included, takes a dropped memory. */
	private boolean isOverRelic(int mouseX, int mouseY) {
		double dx = (mouseX - px(GUI_WIDTH / 2)) / ((BRAIN_RX + 10) * currentLayoutScale);
		double dy = (mouseY - py(BRAIN_CY)) / ((BRAIN_RY + 10) * currentLayoutScale);
		return dx * dx + dy * dy <= 1.0;
	}

	/** A tincture is set by dropping it on the relic or on its own section's socket. */
	private boolean isTinctureDropTarget(ThelemicMemory memory, int mouseX, int mouseY) {
		return isOverRelic(mouseX, mouseY) || getHoveredSection(mouseX, mouseY) == memory.section();
	}

	private void updateFamilyHold(double mouseX, double mouseY) {
		if (heldFamilyIcon == null || familyFanOpen) return;
		double slop = scaled(4);
		double dx = mouseX - familyHoldX;
		double dy = mouseY - familyHoldY;
		if (dx * dx + dy * dy > slop * slop) {
			draggingManip = heldFamilyIcon.manip;
			heldFamilyIcon = null;
			return;
		}
		if (Util.getMillis() - familyHoldStarted >= FAMILY_HOLD_MILLIS) openFamilyFan();
	}

	private void openFamilyFan() {
		List<BloodManipulation> forms = new ArrayList<>(unmemorizedFamilyForms(heldFamilyIcon.manip));
		if (forms.isEmpty()) {
			draggingManip = heldFamilyIcon.manip;
			heldFamilyIcon = null;
			return;
		}
		if (!equippedNames.contains(heldFamilyIcon.manip.getName())) forms.add(0, heldFamilyIcon.manip);
		familyFanIcons.clear();
		int gap = scaled(4);
		int step = currentIconSize + gap;
		int cols = Math.min(2, forms.size());
		int rows = (forms.size() + cols - 1) / cols;
		// Open below the held memory, or above it when the drawer sits too low on the wall.
		boolean opensUp = heldFamilyIcon.y + heldFamilyIcon.size + rows * step > guiTop + currentGuiHeight;
		boolean rightWall = heldFamilyIcon.x > guiLeft + currentGuiWidth / 2;
		int left = rightWall ? heldFamilyIcon.x + heldFamilyIcon.size - cols * step + gap : heldFamilyIcon.x;
		for (int i = 0; i < forms.size(); i++) {
			int x = left + (i % cols) * step;
			int y = opensUp ? heldFamilyIcon.y - (i / cols + 1) * step
					: heldFamilyIcon.y + heldFamilyIcon.size + gap + (i / cols) * step;
			BloodManipulation form = forms.get(i);
			familyFanIcons.add(new ManipIcon(form, x, y, currentIconSize, form.getTend()));
		}
		familyFanOpen = true;
	}

	private List<BloodManipulation> unlockedFamilyForms(BloodManipulation baseline) {
		return ManipulationFamilyRegistry.family(baseline.getName()).stream()
				.flatMap(family -> family.forms().stream())
				.map(form -> ManipulationInit.getByName(form.id()))
				.filter(Objects::nonNull)
				.filter(form -> availableManipNames.contains(form.getName()))
				.toList();
	}

	private List<BloodManipulation> unmemorizedFamilyForms(BloodManipulation baseline) {
		return unlockedFamilyForms(baseline).stream()
				.filter(form -> !equippedNames.contains(form.getName()))
				.toList();
	}

	private boolean contains(ManipIcon icon, double mouseX, double mouseY) {
		return mouseX >= icon.x && mouseX < icon.x + icon.size
				&& mouseY >= icon.y && mouseY < icon.y + icon.size;
	}

	private void clearFamilyHold() {
		heldFamilyIcon = null;
		familyFanOpen = false;
		familyFanIcons.clear();
	}

	private void tryEquipManip(BloodManipulation manipulation) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (equippedNames.contains(manipulation.getName())) {
			if (player != null) player.displayClientMessage(Component.translatable("screen.hemomancy.mnemonic_reliquary.already_memorized").withStyle(ChatFormatting.YELLOW), true);
			return;
		}
		if (ManipulationEquipHelper.countNormalEquippedNames(new ArrayList<>(equippedNames)) >= maxSlots) {
			if (player != null) player.displayClientMessage(Component.translatable("screen.hemomancy.mnemonic_reliquary.no_free_memory_slots").withStyle(ChatFormatting.RED), true);
			return;
		}
		PacketHandler.sendToServer(new EquipManipulationPacket(manipulation.getName(), true));
		equippedNames.add(manipulation.getName());
		equippedManips.add(manipulation);
		buildLayout();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

		int mx = (int) mouseX;
		int my = (int) mouseY;
		if (familyFanOpen) return true;
		EnumVeinSections section = getHoveredSection(mx, my);
		if (section != null) {
			cycleVascularSection(section);
			return true;
		}

		for (ManipIcon icon : equippedIcons) {
			if (contains(icon, mx, my)) {
				PacketHandler.sendToServer(
						new EquipManipulationPacket(icon.manip.getName(), false));
				equippedNames.remove(icon.manip.getName());
				equippedManips.remove(icon.manip);
				buildLayout();
				return true;
			}
		}

		for (ManipIcon icon : knownIcons) {
			if (contains(icon, mx, my)) {
				if (!unlockedFamilyForms(icon.manip).isEmpty()) {
					heldFamilyIcon = icon;
					familyHoldStarted = Util.getMillis();
					familyHoldX = mouseX;
					familyHoldY = mouseY;
				} else if (!equippedNames.contains(icon.manip.getName())) {
					draggingManip = icon.manip;
				}
				return true;
			}
		}
		for (PreparedThelemicIcon icon : preparedThelemicIcons) {
			if (mx >= icon.x && mx < icon.x + icon.size
					&& my >= icon.y && my < icon.y + icon.size) {
				if (!equippedNames.contains(MemorySlotRef.thelemicMemory(icon.memory).storageKey())) {
					draggingThelemicMemory = icon.memory;
				}
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0 && heldFamilyIcon != null) {
			updateFamilyHold(mouseX, mouseY);
			if (familyFanOpen) {
				familyFanIcons.stream().filter(icon -> contains(icon, mouseX, mouseY)).findFirst()
						.ifPresent(icon -> tryEquipManip(icon.manip));
				clearFamilyHold();
				return true;
			}
			if (heldFamilyIcon != null) {
				draggingManip = heldFamilyIcon.manip;
				heldFamilyIcon = null;
			}
		}
		if (draggingThelemicMemory != null) {
			ThelemicMemory replaced = equippedThelemicMemories.stream()
					.filter(memory -> memory.section() == draggingThelemicMemory.section())
					.findFirst().orElse(null);
			if (isTinctureDropTarget(draggingThelemicMemory, (int) mouseX, (int) mouseY)
					&& (replaced != null
							|| ManipulationEquipHelper.countNormalEquippedNames(new ArrayList<>(equippedNames)) < maxSlots)) {
				String key = MemorySlotRef.thelemicMemory(draggingThelemicMemory).storageKey();
				PacketHandler.sendToServer(new EquipManipulationPacket(key, true));
				if (replaced != null) {
					equippedNames.remove(MemorySlotRef.thelemicMemory(replaced).storageKey());
					equippedThelemicMemories.remove(replaced);
				}
				equippedNames.add(key);
				equippedThelemicMemories.add(draggingThelemicMemory);
				buildLayout();
			}
			draggingThelemicMemory = null;
			return true;
		}
		if (draggingManip != null) {
			int mx = (int) mouseX;
			int my = (int) mouseY;

			if (isOverRelic(mx, my)) tryEquipManip(draggingManip);
			draggingManip = null;
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button == 0 && heldFamilyIcon != null) {
			updateFamilyHold(mouseX, mouseY);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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

	private record ManipIcon(BloodManipulation manip, int x, int y, int size, EnumBloodTemper temper) {
	}

	private record PreparedThelemicIcon(ThelemicMemory memory, int x, int y, int size) {
	}

	private record VascularNode(EnumVeinSections section, int x, int y) {
	}

	private record Drawer(EnumBloodTemper temper, int x, int y, int width, int height) {
	}

}
