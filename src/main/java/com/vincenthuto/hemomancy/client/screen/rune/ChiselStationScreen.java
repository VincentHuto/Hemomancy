package com.vincenthuto.hemomancy.client.screen.rune;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.ChiselStationMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.runes.PacketChiselCraftingEvent;
import com.vincenthuto.hemomancy.common.network.capa.runes.PacketLoadChiselPattern;
import com.vincenthuto.hemomancy.common.network.capa.runes.PacketUpdateChiselRunes;
import com.vincenthuto.hemomancy.common.recipe.ChiselRecipe;
import com.vincenthuto.hemomancy.common.item.rune.pattern.ItemRunePattern;
import com.vincenthuto.hemomancy.common.tile.ChiselStationBlockEntity;
import com.vincenthuto.hutoslib.client.screen.HLButtonTextured;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ChiselStationScreen extends AbstractContainerScreen<ChiselStationMenu> {
	private static final ResourceLocation GUI_Chisel = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/chisel_station.png");

	private final Inventory playerInv;
	private final ChiselStationBlockEntity te;
	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;
	public ChiselButton[][] runeButtonArray = new ChiselButton[8][8];
	int CLEARBUTTONID = 70;
	HLButtonTextured clearButton;
	int CHISELBUTTONID = 71;
	HLButtonTextured chiselButton;
	int LOADPATTERNBUTTONID = 72;
	HLButtonTextured loadPatternButton;
	public byte[][] pattern = ChiselRecipe.blank();
	public byte[][] preview = ChiselRecipe.blank();
	private ItemStack lastPatternSlotItem = ItemStack.EMPTY;

	// --- Click-and-drag rune painting state ---
	/** Whether the player is currently dragging across the rune grid. */
	private boolean isDragging = false;
	/** The paint mode for the current drag: true = activate runes, false = deactivate. */
	private boolean dragPaintOn = true;
	/** Tracks which buttons have already been toggled during this drag to avoid re-processing. */
	private final boolean[][] dragVisited = new boolean[8][8];

	public ChiselStationScreen(ChiselStationMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.leftPos = 0;
		this.topPos = 0;
		this.imageWidth = 176;
		this.imageHeight = 186;
		this.playerInv = inv;
		this.te = screenContainer.getTe();
		// Sync local pattern from TE so reopening the screen preserves grid state
		if (te.runesList != null) {
			for (int i = 0; i < te.runesList.length && i < pattern.length; i++) {
				pattern[i] = te.runesList[i].clone();
			}
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		// Render glowing borders: red for confirmed, purple for preview
		ChiselButtonGlowRenderer.renderGlowGrid(graphics, runeButtonArray, pattern, preview);
		this.renderTooltip(graphics, mouseX, mouseY);

		List<Component> cat1 = new ArrayList<Component>();
		cat1.add(Component.literal("Clear Runes"));
		if (clearButton.isHovered()) {
			graphics.renderComponentTooltip(font, cat1, mouseX, mouseY);
		}
		List<Component> cat9 = new ArrayList<Component>();
		cat9.add(Component.literal("Chisel Rune"));
		if (chiselButton.isHovered()) {
			graphics.renderComponentTooltip(font, cat9, mouseX, mouseY);
		}
		List<Component> cat10 = new ArrayList<Component>();
		cat10.add(Component.literal("Load Pattern"));
		if (loadPatternButton.isHovered()) {
			graphics.renderComponentTooltip(font, cat10, mouseX, mouseY);
		}

	}

	@Override
	public void renderBackground(GuiGraphics graphics) {
		super.renderBackground(graphics);
		for (int i = 0; i < renderables.size(); i++) {
			renderables.get(i).render(graphics, 0, 00, 10);
		}
		// Auto-detect when a pattern item is placed/removed in slot 4
		ItemStack currentPatternSlot = te.getItem(4);
		if (!ItemStack.isSameItemSameTags(currentPatternSlot, lastPatternSlotItem)) {
			lastPatternSlotItem = currentPatternSlot.copy();
			if (currentPatternSlot.getItem() instanceof ItemRunePattern) {
				ItemRunePattern runePattern = (ItemRunePattern) currentPatternSlot.getItem();
				ChiselRecipe patternRecipe = runePattern.getRecipe();
				if (patternRecipe != null && patternRecipe.getPattern() != null) {
					loadPatternIntoGrid(patternRecipe.getPattern());
				}
			} else {
				// Pattern was removed — clear the grid and preview
				pattern = ChiselRecipe.blank();
				preview = ChiselRecipe.blank();
				refreshButtonsFromPattern();
				PacketHandler.CHANNELRUNES.sendToServer(new PacketUpdateChiselRunes(pattern));
			}
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int x, int y) {
		graphics.drawString(font, "Chisel Station", 8, 4, 0);
		graphics.drawString(font, "Chisel Station", 8, 4, 65444444);

		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();
		graphics.drawString(font, this.playerInv.getDisplayName(), 8, this.imageHeight - 92,
				000000);

		if (te.hasValidRecipe()) {
			ChiselRecipe currentRecipe = te.getCurrentRecipe();
			graphics.drawString(font, currentRecipe.getResultItem().getDescriptionId(), 120, 65, 0);
			graphics.renderItem(currentRecipe.getResultItem(), 145, 44);
			if (te.areRunesMatching()) {
				RenderSystem.setShaderTexture(0, GUI_Chisel); // Cap

				graphics.blit(GUI_Chisel, 162 - 42, 45 + 32, 176, 96, 16, 16);
			} else {
				RenderSystem.setShaderTexture(0, GUI_Chisel); // Cap

				graphics.blit(GUI_Chisel, 162 - 42, 45 + 32, 176, 80, 16, 16);
			}
			if (te.getItem(3).getItem() instanceof ItemKnapper) {
				RenderSystem.setShaderTexture(0, GUI_Chisel); // Cap
				graphics.blit(GUI_Chisel, 162 - 42 + 16, 45 + 32, 176 + 16, 96, 16, 16);
			} else {
				RenderSystem.setShaderTexture(0, GUI_Chisel); // Cap
				graphics.blit(GUI_Chisel, 162 - 42 + 16, 45 + 32, 176 + 16, 96 - 16, 16, 16);
			}
		}
		matrixStack.popPose();

	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
		this.renderBackground(graphics);
		graphics.blit(GUI_Chisel, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

	/**
	 * Loads a pattern as a preview overlay (purple glow).
	 * The player must click each rune to confirm it (turns red).
	 */
	private void loadPatternIntoGrid(byte[][] sourcePattern) {
		// Clear any existing confirmed pattern
		pattern = ChiselRecipe.blank();
		refreshButtonsFromPattern();
		PacketHandler.CHANNELRUNES.sendToServer(new PacketUpdateChiselRunes(pattern));
		// Set the preview (purple highlight, client-side only)
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				preview[i][j] = sourcePattern[i][j];
			}
		}
	}

	/**
	 * Refreshes all button visual states from the current local pattern array.
	 */
	private void refreshButtonsFromPattern() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				runeButtonArray[i][j].setState(pattern[i][j] != 0);
			}
		}
	}

	@Override
	protected void init() {
		super.init();
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		renderables.clear();
		int inc = 0;
		for (int i = 0; i < runeButtonArray.length; i++) {
			for (int j = 0; j < runeButtonArray.length; j++) {
				this.addRenderableWidget(runeButtonArray[i][j] = new ChiselButton(GUI_Chisel, inc, i, j,
						left + guiWidth - (guiWidth - 50 - (j * 8)), top + guiHeight - (160 - (i * 8)), 8, 8, 176, 0,
						te.runesList[i][j] != 0, (press) -> {
							// Handled by click-and-drag system in mouseClicked/mouseDragged/mouseReleased
						}));
				inc++;
			}
		}
		this.addRenderableWidget(clearButton = new HLButtonTextured(GUI_Chisel, CLEARBUTTONID,
				left + guiWidth - (guiWidth - 120), top + guiHeight - (170), 16, 16, 176, 16, (press) -> {
					pattern = ChiselRecipe.blank();
					preview = ChiselRecipe.blank();
					refreshButtonsFromPattern();
					PacketHandler.CHANNELRUNES.sendToServer(new PacketUpdateChiselRunes(pattern));
				}));
		this.addRenderableWidget(chiselButton = new HLButtonTextured(GUI_Chisel, CHISELBUTTONID,
				left + guiWidth - (guiWidth - 120), top + guiHeight - (150), 16, 16, 176, 48, (press) -> {
					if (te.contents.get(3).getItem() != Items.AIR) {
						PacketHandler.CHANNELRUNES.sendToServer(new PacketChiselCraftingEvent());
						pattern = ChiselRecipe.blank();
						preview = ChiselRecipe.blank();
						refreshButtonsFromPattern();
						PacketHandler.CHANNELRUNES.sendToServer(new PacketUpdateChiselRunes(pattern));
					}
				}));
		this.addRenderableWidget(loadPatternButton = new HLButtonTextured(GUI_Chisel, LOADPATTERNBUTTONID,
				left + 28, top + 80, 16, 16, 176, 32, (press) -> {
					ItemStack patternStack = te.getItem(4);
					if (patternStack.getItem() instanceof ItemRunePattern runePattern) {
						ChiselRecipe patternRecipe = runePattern.getRecipe();
						if (patternRecipe != null && patternRecipe.getPattern() != null) {
							loadPatternIntoGrid(patternRecipe.getPattern());
						}
					}
				}));

	}

	// --- Click-and-drag rune painting ---

	/**
	 * Finds the ChiselButton under the given mouse coordinates and applies
	 * the current drag paint mode to it (if not already visited this drag).
	 */
	private void paintRuneAt(double mouseX, double mouseY) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				ChiselButton button = runeButtonArray[i][j];
				if (!dragVisited[i][j]
						&& mouseX >= button.getX() && mouseX < button.getX() + button.getWidth()
						&& mouseY >= button.getY() && mouseY < button.getY() + button.getHeight()) {
					dragVisited[i][j] = true;
					if (dragPaintOn) {
						// Activate rune
						button.setState(true);
						pattern[i][j] = 1;
					} else {
						// Deactivate rune
						button.setState(false);
						pattern[i][j] = 0;
					}
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) { // Left click only
			// Check if click landed on any rune button
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					ChiselButton cb = runeButtonArray[i][j];
					if (mouseX >= cb.getX() && mouseX < cb.getX() + cb.getWidth()
							&& mouseY >= cb.getY() && mouseY < cb.getY() + cb.getHeight()) {
						// Start a drag session
						isDragging = true;
						// Determine paint mode from the first button clicked:
						// If it has a preview waiting and isn't confirmed yet, paint ON
						// If it's already active, paint OFF (toggle off)
						// If it's inactive, paint ON
						if (preview[i][j] != 0 && pattern[i][j] == 0) {
							dragPaintOn = true;
						} else {
							dragPaintOn = (pattern[i][j] == 0);
						}
						// Clear visited tracking
						for (int x = 0; x < 8; x++) {
							for (int y = 0; y < 8; y++) {
								dragVisited[x][y] = false;
							}
						}
						// Paint the first button
						paintRuneAt(mouseX, mouseY);
						return true;
					}
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (isDragging && button == 0) {
			paintRuneAt(mouseX, mouseY);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (isDragging && button == 0) {
			isDragging = false;
			// Sync the final pattern to the server in one batch
			PacketHandler.CHANNELRUNES.sendToServer(new PacketUpdateChiselRunes(pattern));
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
			this.onClose();
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

}