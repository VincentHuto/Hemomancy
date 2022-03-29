package com.vincenthuto.hemomancy.gui.mindrunes;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.MenuChiselStation;
import com.vincenthuto.hemomancy.network.PacketChiselCraftingEvent;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.PacketUpdateChiselRunes;
import com.vincenthuto.hemomancy.recipe.ChiselRecipe;
import com.vincenthuto.hemomancy.tile.BlockEntityChiselStation;
import com.vincenthuto.hutoslib.client.screen.GuiButtonTextured;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

public class ScreenChiselStation extends AbstractContainerScreen<MenuChiselStation> {
	private static final ResourceLocation GUI_Chisel = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/chisel_station.png");

	private final Inventory playerInv;
	private final BlockEntityChiselStation te;
	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;
	public ChiselButton[][] runeButtonArray = new ChiselButton[8][8];
	public ChiselButton[][] runePatternButtonArray = new ChiselButton[8][8];
	int CLEARBUTTONID = 70;
	GuiButtonTextured clearButton;
	int CHISELBUTTONID = 71;
	GuiButtonTextured chiselButton;
	public byte[][] pattern = ChiselRecipe.blank();

	public ScreenChiselStation(MenuChiselStation screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.leftPos = 0;
		this.topPos = 0;
		this.imageWidth = 176;
		this.imageHeight = 186;
		this.playerInv = inv;
		this.te = screenContainer.getTe();
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);

		List<Component> cat1 = new ArrayList<Component>();
		cat1.add(new TextComponent(I18n.get("Clear Runes")));
		if (clearButton.isHoveredOrFocused()) {
			renderComponentTooltip(matrixStack, cat1, left + guiWidth - (guiWidth - 120), top + guiHeight - (170));
		}
		List<Component> cat9 = new ArrayList<Component>();
		cat9.add(new TextComponent(I18n.get("Chisel Rune")));
		if (chiselButton.isHoveredOrFocused()) {
			renderComponentTooltip(matrixStack, cat9, left + guiWidth - (guiWidth - 120), top + guiHeight - (150));
		}

	}

	@Override
	public void renderBackground(PoseStack matrixStack) {
		super.renderBackground(matrixStack);
		for (int i = 0; i < renderables.size(); i++) {
			renderables.get(i).render(matrixStack, 0, 00, 10);
		}
		///// THIS IS FOR THE BLUEPRINT THING
//		if (te.getLevel().getGameTime() % 2 == 0) {
//			if (te.getItem(4) != null && this.te.getItem(4) != ItemStack.EMPTY) {
//				Minecraft.getInstance().textureManager.bindForSetup(GUI_Chisel);
//				if (te.getItem(4).getItem() instanceof ItemRunePattern) {
//					ItemRunePattern runePattern = (ItemRunePattern) te.getItem(4).getItem();
//					int incPattern = 100;
//					for (int i = 0; i < runePatternButtonArray.length; i++) {
//						for (int j = 0; j < runePatternButtonArray.length; j++) {
//
//							renderables.add(runePatternButtonArray[i][j] = new GuiButtonTextured(GUI_Chisel, incPattern,
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
//					if (renderables.get(i) instanceof GuiButtonTextured) {
//						GuiButtonTextured test = (GuiButtonTextured) renderables.get(i);
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
	protected void renderLabels(PoseStack matrixStack, int x, int y) {
		this.font.draw(matrixStack, "Chisel Station", 8, 4, 0);
		matrixStack.pushPose();
		this.font.draw(matrixStack, this.te.getDisplayName().getContents(), 8, 6, 65444444);
		this.font.draw(matrixStack, this.playerInv.getDisplayName().getContents(), 8, this.imageHeight - 92, 000000);
		if (te.hasValidRecipe()) {
			ChiselRecipe currentRecipe = te.getCurrentRecipe();
			this.font.draw(matrixStack, I18n.get(currentRecipe.getResultItem().getDescriptionId()), 120, 65, 0);
			minecraft.getItemRenderer().renderAndDecorateItem(currentRecipe.getResultItem(), 145, 44);
			if (te.areRunesMatching()) {
				RenderSystem.setShaderTexture(0, GUI_Chisel); // Cap
				blit(matrixStack, 162 - 42, 45 + 32, 176, 96, 16, 16);
			} else {
				RenderSystem.setShaderTexture(0, GUI_Chisel); // Cap
				blit(matrixStack, 162 - 42, 45 + 32, 176, 80, 16, 16);
			}
			if (te.getItem(3).getItem() instanceof ItemKnapper) {
				RenderSystem.setShaderTexture(0, GUI_Chisel); // Cap
				blit(matrixStack, 162 - 42 + 16, 45 + 32, 176 + 16, 96, 16, 16);
			} else {
				RenderSystem.setShaderTexture(0, GUI_Chisel); // Cap
				blit(matrixStack, 162 - 42 + 16, 45 + 32, 176 + 16, 96 - 16, 16, 16);
			}
		}
		matrixStack.popPose();

	}

	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
		this.renderBackground(matrixStack);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI_Chisel);
		this.blit(matrixStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
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
											PacketHandler.CHANNELMAIN
													.sendToServer(new PacketUpdateChiselRunes(pattern));
										} else if (test.getState() == true) {
											test.setState(false);
											pattern[test.getI()][test.getJ()] = 0;
											PacketHandler.CHANNELMAIN
													.sendToServer(new PacketUpdateChiselRunes(pattern));
										}
									}
								}
							}
						}));
				inc++;
			}
		}
		this.addRenderableWidget(clearButton = new GuiButtonTextured(GUI_Chisel, CLEARBUTTONID,
				left + guiWidth - (guiWidth - 120), top + guiHeight - (170), 16, 16, 176, 16, (press) -> {
					for (int i = 0; i < 64; i++) {
						if (renderables.get(i) instanceof GuiButtonTextured) {
							GuiButtonTextured test = (GuiButtonTextured) renderables.get(i);
							if (test.getState() == true) {
								test.setState(false);
								pattern = ChiselRecipe.blank();
								PacketHandler.CHANNELMAIN.sendToServer(new PacketUpdateChiselRunes(pattern));
							}
						}
					}
				}));
		this.addRenderableWidget(chiselButton = new GuiButtonTextured(GUI_Chisel, CHISELBUTTONID,
				left + guiWidth - (guiWidth - 120), top + guiHeight - (150), 16, 16, 176, 48, (press) -> {
					if (te.contents.get(3).getItem() != Items.AIR) {
						PacketHandler.CHANNELMAIN.sendToServer(new PacketChiselCraftingEvent());
						for (int i = 0; i < 64; i++) {
							if (renderables.get(i) instanceof GuiButtonTextured) {
								GuiButtonTextured test = (GuiButtonTextured) renderables.get(i);
								if (test.getState() == true) {
									test.setState(false);
									pattern = ChiselRecipe.blank();
									PacketHandler.CHANNELMAIN.sendToServer(new PacketUpdateChiselRunes(pattern));
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

}