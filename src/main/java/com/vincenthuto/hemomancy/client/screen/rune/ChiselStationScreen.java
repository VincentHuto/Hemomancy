package com.vincenthuto.hemomancy.client.screen.rune;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.rune.pattern.ItemRunePattern;
import com.vincenthuto.hemomancy.common.menu.ChiselStationMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.runes.PacketChiselCraftingEvent;
import com.vincenthuto.hemomancy.common.network.capa.runes.PacketUpdateChiselRunes;
import com.vincenthuto.hemomancy.common.recipe.ChiselRecipe;
import com.vincenthuto.hemomancy.common.tile.ChiselStationBlockEntity;
import com.vincenthuto.hutoslib.client.screen.HLButtonTextured;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;

import net.minecraft.client.Minecraft;
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
	public ChiselButton[][] runePatternButtonArray = new ChiselButton[8][8];
	int CLEARBUTTONID = 70;
	HLButtonTextured clearButton;
	int CHISELBUTTONID = 71;
	HLButtonTextured chiselButton;
	public byte[][] pattern = ChiselRecipe.blank();

	public ChiselStationScreen(ChiselStationMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.leftPos = 0;
		this.topPos = 0;
		this.imageWidth = 176;
		this.imageHeight = 186;
		this.playerInv = inv;
		this.te = screenContainer.getTe();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);

		List<Component> cat1 = new ArrayList<Component>();
		cat1.add(Component.literal("Clear Runes"));
		if (clearButton.isHoveredOrFocused()) {
			graphics.renderComponentTooltip(font, cat1, left + guiWidth - (guiWidth - 120), top + guiHeight - (170));
		}
		List<Component> cat9 = new ArrayList<Component>();
		cat9.add(Component.literal("Chisel Rune"));
		if (chiselButton.isHoveredOrFocused()) {
			graphics.renderComponentTooltip(font, cat9, left + guiWidth - (guiWidth - 120), top + guiHeight - (150));
		}

	}

	@Override
	public void renderBackground(GuiGraphics graphics) {
		super.renderBackground(graphics);
		for (int i = 0; i < renderables.size(); i++) {
			renderables.get(i).render(graphics, 0, 00, 10);
		}
		/// THIS IS FOR THE BLUEPRINT THING
//		if (te.getLevel().getGameTime() % 2 == 0) {
//			if (te.getItem(4) != null && this.te.getItem(4) != ItemStack.EMPTY) {
//				Minecraft.getInstance().textureManager.bindForSetup(GUI_Chisel);
//				if (te.getItem(4).getItem() instanceof ItemRunePattern) {
//					ItemRunePattern runePattern = (ItemRunePattern) te.getItem(4).getItem();
//					int incPattern = 100;
//					for (int i = 0; i < runePatternButtonArray.length; i++) {
//						for (int j = 0; j < runePatternButtonArray.length; j++) {
//
//							renderables.add(runePatternButtonArray[i][j] = new ChiselButton(GUI_Chisel, incPattern,
//									left + guiWidth - (guiWidth + 80 - (i * 8)), top + guiHeight - (160 - (j * 8)), 8,
//									8, 176, 0, runePattern.getRecipe().getActivatedRunes().contains(incPattern - 100),
//									(press) -> {
//									}));
//							incPattern++;
//						}
//					}
//				}
//			} else {
//				for (int i = 0; i < renderables.size(); i++) {
//					if (renderables.get(i) instanceof HLButtonTextured) {
//						HLButtonTextured test = (HLButtonTextured) renderables.get(i);
//						if (test.getId() > 99) {
//							renderables.remove(test);
//						}
//
//					}
//				}
//			}
//		}
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
//		RenderSystem.setShader(GameRenderer::getPositionTexShader);
//		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//		RenderSystem.setShaderTexture(0, GUI_Chisel);
		graphics.blit(GUI_Chisel, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
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
						te.runesList[i][j] == 0 ? false : true, (press) -> {
							if (press instanceof ChiselButton) {
								ChiselButton button = (ChiselButton) press;
								if (button.getId() <= 64) {
									if (renderables.get(button.getId()) instanceof ChiselButton) {
										ChiselButton test = (ChiselButton) renderables.get(button.getId());
										if (test.getState() == false) {
											test.setState(true);
											pattern[test.getI()][test.getJ()] = 1;
											PacketHandler.CHANNELRUNES
													.sendToServer(new PacketUpdateChiselRunes(pattern));
										} else if (test.getState() == true) {
											test.setState(false);
											pattern[test.getI()][test.getJ()] = 0;
											PacketHandler.CHANNELRUNES
													.sendToServer(new PacketUpdateChiselRunes(pattern));
										}
									}
								}
							}
						}));
				inc++;
			}
		}
		this.addRenderableWidget(clearButton = new HLButtonTextured(GUI_Chisel, CLEARBUTTONID,
				left + guiWidth - (guiWidth - 120), top + guiHeight - (170), 16, 16, 176, 16, (press) -> {
					for (int i = 0; i < 64; i++) {
						if (renderables.get(i) instanceof HLButtonTextured) {
							HLButtonTextured test = (HLButtonTextured) renderables.get(i);
							if (test.getState() == true) {
								test.setState(false);
								pattern = ChiselRecipe.blank();
								PacketHandler.CHANNELRUNES.sendToServer(new PacketUpdateChiselRunes(pattern));
							}
						}
					}
				}));
		this.addRenderableWidget(chiselButton = new HLButtonTextured(GUI_Chisel, CHISELBUTTONID,
				left + guiWidth - (guiWidth - 120), top + guiHeight - (150), 16, 16, 176, 48, (press) -> {
					if (te.contents.get(3).getItem() != Items.AIR) {
						PacketHandler.CHANNELRUNES.sendToServer(new PacketChiselCraftingEvent());
						for (int i = 0; i < 64; i++) {
							if (renderables.get(i) instanceof HLButtonTextured) {
								HLButtonTextured test = (HLButtonTextured) renderables.get(i);
								if (test.getState() == true) {
									test.setState(false);
									pattern = ChiselRecipe.blank();
									PacketHandler.CHANNELRUNES.sendToServer(new PacketUpdateChiselRunes(pattern));
								}

							}
						}
					}
				}));

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