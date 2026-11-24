package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.screen.manips.ManipulationIconResolver;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemoryEntryKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemorySlotRef;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.SynapticLoadoutSlotHelper;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationClientPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.DendriticDistributorBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SynapticLoadoutScreen extends Screen {
	private static final ResourceLocation BACKGROUND = Hemomancy.rloc("textures/gui/synaptic_loadouts.png");
	private static final int PANEL_WIDTH = 404;
	private static final int PANEL_HEIGHT = 238;
	private static final int EDGE_MARGIN = 6;
	private static final int COST_BLOOD = (int) SynapticLoadoutActionPacket.SAVE_BLOOD_COST;
	private static final int COST_XP = SynapticLoadoutActionPacket.SAVE_RAW_XP_COST;
	private static final int EQUIPPED_CENTER = 101;
	private static final int SAVED_CENTER = 302;
	private static final int WHEEL_Y = 139;
	private static final int WHEEL_RADIUS = 32;

	// Match the tips of the painted dendrites in tools/model_export/synaptic_loadouts_gui.py.
	private static final int[][] SOCKETS = {{77, 61}, {119, 49}, {160, 41}, {202, 38}, {244, 41}, {285, 49}, {327, 61}};
	private static final int BONE = 0xFFF1DBBA;
	private static final int MUTED_BONE = 0xFFBDA58E;
	private static final int IRON_EDGE = 0xFF87767D;
	private static final int GOLD = 0xFFFFD98E;
	private static final int DARK = 0xFF1B111B;

	private final BlockPos distributorPos;
	private final List<PatternButton> patternButtons = new ArrayList<>();
	private int selectedIndex;
	private int lastRenderedSlots = -1;
	private boolean confirmOverwrite;
	private float scale;
	private int panelX;
	private int panelY;
	private EditBox nameField;
	private ManipulationLoadout viewedPattern;
	private Button applyButton;
	private Button renameButton;
	private Button saveButton;

	public SynapticLoadoutScreen(BlockPos distributorPos) {
		super(Component.translatable("screen.hemomancy.synaptic_loadouts"));
		this.distributorPos = distributorPos;
		var level = Minecraft.getInstance().level;
		if (level != null && level.getBlockEntity(distributorPos) instanceof DendriticDistributorBlockEntity distributor) {
			this.selectedIndex = distributor.getSelectedSlot();
		}
	}

	public static void openScreen(BlockPos pos) {
		Minecraft.getInstance().setScreen(new SynapticLoadoutScreen(pos));
	}

	@Override
	protected void init() {
		super.init();
		scale = Math.min(1.0F, Math.min(
				Math.max(1, width - EDGE_MARGIN * 2) / (float) PANEL_WIDTH,
				Math.max(1, height - EDGE_MARGIN * 2) / (float) PANEL_HEIGHT));
		panelX = (width - scaled(PANEL_WIDTH)) / 2;
		panelY = (height - scaled(PANEL_HEIGHT)) / 2;
		PacketHandler.sendToServer(new KnownManipulationClientPacket());
		int slots = unlockedSlots();
		selectedIndex = Mth.clamp(selectedIndex, 0, slots - 1);
		lastRenderedSlots = slots;

		patternButtons.clear();
		for (int i = 0; i < SOCKETS.length; i++) {
			patternButtons.add(addRenderableWidget(new PatternButton(i)));
		}
		saveButton = addAction(17, 194, 92, label("save"), this::sendSaveOrOverwrite);
		applyButton = addAction(112, 194, 87, label("apply"), this::sendApply);
		renameButton = addAction(322, 201, 62, label("rename"), this::sendRename);

		nameField = new EditBox(font, x(220), y(201), x(317) - x(220), buttonHeight(),
				Component.translatable("screen.hemomancy.synaptic_loadouts.name"));
		nameField.setMaxLength(ManipulationLoadout.MAX_NAME_LENGTH);
		addRenderableWidget(nameField);
		updateNameField();
		updateButtons();
	}

	private Button addAction(int x, int y, int width, Component text, Runnable action) {
		return addRenderableWidget(new SynapticButton(x(x), y(y), x(x + width) - x(x), buttonHeight(), text, action));
	}

	private int buttonHeight() {
		return Math.max(13, scaled(18));
	}

	private int scaled(int distance) {
		return Math.round(distance * scale);
	}

	private int x(int local) {
		return panelX + scaled(local);
	}

	private int y(int local) {
		return panelY + scaled(local);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Screen#renderBackground applies menu blur. Keep the painted texture sharp.
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!nameField.isFocused() && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT)) {
			int direction = keyCode == GLFW.GLFW_KEY_LEFT ? -1 : 1;
			selectPattern(Math.floorMod(selectedIndex + direction, unlockedSlots()));
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && mouseX >= x(225) && mouseX < x(380)
				&& mouseY >= y(101) && mouseY < y(178) && saveButton.active && currentLoadout().isEmpty()) {
			sendSaveOrOverwrite();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
		renderTransparentBackground(gfx);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		gfx.blit(BACKGROUND, panelX, panelY, scaled(PANEL_WIDTH), scaled(PANEL_HEIGHT),
				0F, 0F, PANEL_WIDTH, PANEL_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT);
		RenderSystem.disableBlend();

		int slots = unlockedSlots();
		if (lastRenderedSlots != slots) {
			lastRenderedSlots = slots;
			selectedIndex = Mth.clamp(selectedIndex, 0, slots - 1);
			confirmOverwrite = false;
			updateNameField();
		}
		IKnownManipulations known = known();
		ManipulationLoadout pattern = displayLoadout(known, selectedIndex);
		if (!pattern.equals(viewedPattern)) {
			if (viewedPattern == null || nameField.getValue().equals(viewedPattern.name())) {
				nameField.setValue(pattern.name());
			}
			viewedPattern = pattern;
			confirmOverwrite = false;
		}
		updateButtons();

		drawCentered(gfx, title, 202, 10, 300, BONE);
		drawCentered(gfx, label("synaptic_patterns_count", slots, SynapticLoadoutSlotHelper.MAX_LOADOUT_SLOTS),
				343, 11, 99, MUTED_BONE);

		drawCentered(gfx, label("equipped_memories"), EQUIPPED_CENTER, 86, 156, BONE);
		drawCentered(gfx, pattern.displayName(selectedIndex), SAVED_CENTER, 86, 156, GOLD);
		Component hovered = null;
		if (known == null) {
			drawCentered(gfx, label("reading_vascular_memory"), EQUIPPED_CENTER, 135, 156, MUTED_BONE);
		} else {
			MemorySlotRef selected = known.getSelectedMemoryRef();
			List<String> current = ManipulationLoadout.normalizeManipNames(known.getEquippedManipNames());
			hovered = drawPreview(gfx, current, selected == null ? "" : selected.storageKey(),
					EQUIPPED_CENTER, "no_equipped_memories", mouseX, mouseY);
		}
		Component patternHover = drawPreview(gfx, pattern.manipNames(), pattern.selectedManipName(),
				SAVED_CENTER, "empty_pattern", mouseX, mouseY);
		if (patternHover != null) hovered = patternHover;

		drawCentered(gfx, costText(), 108, 215, 181, GOLD);
		gfx.drawString(font, Component.translatable("screen.hemomancy.synaptic_loadouts.name"), x(220), y(190), MUTED_BONE, false);
		drawSelectorImpulse(gfx, partialTick);
		super.render(gfx, mouseX, mouseY, partialTick);

		for (PatternButton button : patternButtons) {
			if (button.contains(mouseX, mouseY)) {
				renderPatternTooltip(gfx, button.slot, mouseX, mouseY);
				return;
			}
		}
		if (hovered != null) gfx.renderTooltip(font, hovered, mouseX, mouseY);
	}

	private Component drawPreview(GuiGraphics gfx, List<String> entries, String selected, int center, String emptyKey,
			int mouseX, int mouseY) {
		if (entries.isEmpty()) {
			int left = x(center - 66);
			int top = y(132);
			gfx.fill(left, top, x(center + 66), y(146), DARK);
			drawCentered(gfx, label(emptyKey), center, 135, 130, MUTED_BONE);
			return null;
		}

		Component hovered = null;
		int size = Math.max(1, scaled(16));
		for (int i = 0; i < entries.size(); i++) {
			double angle = -Math.PI / 2 + i * (2 * Math.PI / entries.size());
			int ix = x(center + (int) Math.round(Math.cos(angle) * WHEEL_RADIUS)) - size / 2;
			int iy = y(WHEEL_Y + (int) Math.round(Math.sin(angle) * WHEEL_RADIUS)) - size / 2;
			String entry = entries.get(i);
			if (entry.equals(selected)) {
				ScreenDrawUtils.drawSimpleBorder(gfx, ix - 1, iy - 1, size + 2, size + 2, GOLD);
			}
			MemorySlotRef ref = MemorySlotRef.fromStorageKey(entry);
			if (ref.kind() == MemoryEntryKind.MANIPULATION) {
				blitIcon(gfx, ManipulationIconResolver.base(ref.id()), ix, iy, size);
			}
			blitIcon(gfx, memoryOverlayTexture(ref), ix, iy, size);
			if (mouseX >= ix && mouseX < ix + size && mouseY >= iy && mouseY < iy + size) {
				hovered = memoryName(ref);
			}
		}
		return hovered;
	}

	private static void blitIcon(GuiGraphics gfx, ResourceLocation texture, int x, int y, int size) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		gfx.blit(texture, x, y, size, size, 0F, 0F, 16, 16, 16, 16);
		RenderSystem.disableBlend();
	}

	private static ResourceLocation memoryOverlayTexture(MemorySlotRef ref) {
		if (ref.kind() == MemoryEntryKind.THELEMIC_MEMORY) {
			return Hemomancy.rloc("textures/item/tincture_" + ref.id() + ".png");
		}
		return ManipulationIconResolver.overlay(ref.id());
	}

	private static Component memoryName(MemorySlotRef ref) {
		if (ref.kind() == MemoryEntryKind.THELEMIC_MEMORY) {
			return Component.translatable("item.hemomancy.tincture_" + ref.id());
		}
		var manipulation = ManipulationInit.getByName(ref.id());
		return manipulation == null ? Component.literal(ref.id()) : manipulation.getDisplayName();
	}

	private void drawSelectorImpulse(GuiGraphics gfx, float partialTick) {
		int[] tip = SOCKETS[selectedIndex];
		int elbowX = 202 + (tip[0] - 202) * 2 / 3;
		int elbowY = tip[1] + (73 - tip[1]) / 2 + 5;
		float progress = minecraft.level == null ? 0F
				: (float) ((AnimationTime.gameTime(minecraft.level, (double) partialTick) % 28) / 28);
		int px = x(progress < 0.5F ? Mth.lerpInt(progress * 2, 202, elbowX)
				: Mth.lerpInt(progress * 2 - 1, elbowX, tip[0]));
		int py = y(progress < 0.5F ? Mth.lerpInt(progress * 2, 73, elbowY)
				: Mth.lerpInt(progress * 2 - 1, elbowY, tip[1]));
		gfx.fill(px - 1, py - 1, px + 2, py + 2, 0xFFB77946);
		gfx.fill(px, py, px + 1, py + 1, GOLD);
	}

	private void drawCentered(GuiGraphics gfx, Component text, int x, int y, int maxWidth, int color) {
		drawFittingText(gfx, text, x(x), y(y), scaled(maxWidth), color);
	}

	private void drawFittingText(GuiGraphics gfx, Component text, int center, int y, int maxWidth, int color) {
		String value = text.getString();
		if (font.width(value) > maxWidth) {
			value = font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width("..."))) + "...";
		}
		gfx.drawString(font, value, center - font.width(value) / 2, y, color, false);
	}

	private void renderPatternTooltip(GuiGraphics gfx, int index, int mouseX, int mouseY) {
		List<Component> tooltip = new ArrayList<>();
		if (index >= unlockedSlots()) {
			tooltip.add(label("locked_pattern", index + 1));
			tooltip.add(label("unlock_pattern_hint"));
		} else {
			ManipulationLoadout loadout = displayLoadout(known(), index);
			tooltip.add(loadout.displayName(index));
			tooltip.add(loadout.isEmpty() ? label("empty_pattern") : label("remembered_powers", loadout.manipNames().size()));
		}
		gfx.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
	}

	private void selectPattern(int index) {
		if (index >= unlockedSlots() || index == selectedIndex) return;
		selectedIndex = index;
		confirmOverwrite = false;
		updateNameField();
		updateButtons();
	}

	private void sendApply() {
		confirmOverwrite = false;
		sendAction(SynapticLoadoutActionPacket.Action.APPLY, "");
	}

	private void sendRename() {
		confirmOverwrite = false;
		sendAction(SynapticLoadoutActionPacket.Action.RENAME, nameField.getValue());
	}

	private void sendSaveOrOverwrite() {
		if (!currentLoadout().isEmpty() && !confirmOverwrite) {
			confirmOverwrite = true;
			updateButtons();
			return;
		}
		confirmOverwrite = false;
		sendAction(SynapticLoadoutActionPacket.Action.SAVE_OR_OVERWRITE, nameField.getValue());
	}

	private void sendAction(SynapticLoadoutActionPacket.Action action, String name) {
		PacketHandler.sendToServer(new SynapticLoadoutActionPacket(action, distributorPos, selectedIndex, name));
		updateButtons();
	}

	private void updateNameField() {
		viewedPattern = currentLoadout();
		nameField.setValue(viewedPattern.name());
	}

	private void updateButtons() {
		IKnownManipulations known = known();
		int slots = unlockedSlots();
		for (PatternButton button : patternButtons) {
			button.active = button.slot < slots;
			button.occupied = button.active && !displayLoadout(known, button.slot).isEmpty();
		}
		boolean hasLoadout = !displayLoadout(known, selectedIndex).isEmpty();
		applyButton.active = hasLoadout;
		renameButton.active = hasLoadout;
		saveButton.active = known != null;
		saveButton.setMessage(label(hasLoadout ? (confirmOverwrite ? "confirm" : "overwrite") : "save"));
	}

	private ManipulationLoadout currentLoadout() {
		return displayLoadout(known(), selectedIndex);
	}

	private ManipulationLoadout displayLoadout(IKnownManipulations known, int slotIndex) {
		if (known == null || slotIndex >= unlockedSlots() || slotIndex >= known.getLoadouts().size()) {
			return ManipulationLoadout.empty(slotIndex);
		}
		return known.getLoadout(slotIndex);
	}

	private IKnownManipulations known() {
		return minecraft.player == null ? null : HemoCapabilityAccess.getKnownManipulations(minecraft.player);
	}

	private int unlockedSlots() {
		return minecraft.player == null ? SynapticLoadoutSlotHelper.BASE_LOADOUT_SLOTS
				: SynapticLoadoutSlotHelper.getClientUnlockedSlots();
	}

	private static Component costText() {
		return HemoText.translatable("screen.hemomancy.synaptic_loadout.cost_blood_xp", COST_BLOOD, COST_XP);
	}

	private static Component label(String key, Object... args) {
		return Component.translatable("screen.hemomancy.synaptic_loadout." + key, args);
	}

	private final class PatternButton extends Button {
		private final int slot;
		private boolean occupied;

		private PatternButton(int slot) {
			super(x(SOCKETS[slot][0] - 10), y(SOCKETS[slot][1] - 10), Math.max(1, scaled(20)),
					Math.max(1, scaled(20)), label("numbered_pattern", slot + 1),
					button -> selectPattern(slot), DEFAULT_NARRATION);
			this.slot = slot;
		}

		@Override
		protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
			boolean selected = slot == selectedIndex;
			int left = getX();
			int top = getY();
			int w = getWidth();
			int h = getHeight();
			int edge = !active ? 0xFF6A5149 : selected || isHoveredOrFocused() ? GOLD : IRON_EDGE;
			int fill = !active ? DARK : selected ? 0xFF694127 : occupied ? 0xFF46202B : 0xFF271820;
			gfx.fill(left + 2, top, left + w - 2, top + h, edge);
			gfx.fill(left, top + 2, left + w, top + h - 2, edge);
			gfx.fill(left + 2, top + 2, left + w - 2, top + h - 2, fill);
			drawFittingText(gfx, Component.literal(Integer.toString(slot + 1)), left + w / 2,
					top + (h - font.lineHeight) / 2, w - 2, active ? BONE : MUTED_BONE);
			if (occupied) gfx.fill(left + w / 2 - 1, top + h - 2, left + w / 2 + 2, top + h, GOLD);
		}

		private boolean contains(double mouseX, double mouseY) {
			return mouseX >= getX() && mouseX < getX() + getWidth()
					&& mouseY >= getY() && mouseY < getY() + getHeight();
		}
	}

	private final class SynapticButton extends Button {
		private SynapticButton(int x, int y, int width, int height, Component text, Runnable action) {
			super(x, y, width, height, text, button -> action.run(), DEFAULT_NARRATION);
		}

		@Override
		protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
			boolean hot = active && isHoveredOrFocused();
			int edge = hot ? GOLD : active ? 0xFFAF8A67 : 0xFF65535D;
			int fill = hot ? 0xFF633335 : active ? 0xFF45242E : DARK;
			ScreenDrawUtils.drawSimpleBorder(gfx, getX(), getY(), width, height, edge);
			gfx.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, fill);
			drawFittingText(gfx, getMessage(), getX() + width / 2, getY() + (height - font.lineHeight) / 2,
					Math.max(1, width - 5), active ? BONE : MUTED_BONE);
		}
	}
}
