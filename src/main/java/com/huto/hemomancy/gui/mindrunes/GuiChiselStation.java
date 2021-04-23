package com.huto.hemomancy.gui.mindrunes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.container.ContainerChiselStation;
import com.huto.hemomancy.gui.GuiButtonTextured;
import com.huto.hemomancy.item.rune.pattern.ItemRunePattern;
import com.huto.hemomancy.item.tool.ItemKnapper;
import com.huto.hemomancy.network.PacketChiselCraftingEvent;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.PacketUpdateChiselRunes;
import com.huto.hemomancy.recipe.RecipeChiselStation;
import com.huto.hemomancy.tile.TileEntityChiselStation;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class GuiChiselStation extends ContainerScreen<ContainerChiselStation> {
	private static final ResourceLocation GUI_Chisel = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/chisel_station.png");

	private final PlayerInventory playerInv;
	private final TileEntityChiselStation te;
	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;
	public GuiButtonTextured[][] runeButtonArray = new GuiButtonTextured[8][8];
	public GuiButtonTextured[][] runePatternButtonArray = new GuiButtonTextured[8][8];

	int CLEARBUTTONID = 70;
	GuiButtonTextured clearButton;
	int CHISELBUTTONID = 71;
	GuiButtonTextured chiselButton;
	public List<Integer> activatedRuneList = new ArrayList<Integer>();

	public GuiChiselStation(ContainerChiselStation screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		this.guiLeft = 0;
		this.guiTop = 0;
		this.xSize = 176;
		this.ySize = 186;
		this.playerInv = inv;
		this.te = screenContainer.getTe();
	}

	@SuppressWarnings({ "deprecation", "unused" })
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		Minecraft.getInstance().textureManager.bindTexture(GUI_Chisel);
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;

		GlStateManager.pushMatrix();
		{
			GlStateManager.enableAlphaTest();
			GlStateManager.enableBlend();
			GlStateManager.color4f(1, 1, 1, 1);
			Minecraft.getInstance().textureManager.bindTexture(GUI_Chisel);

		}
		GlStateManager.popMatrix();
		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).renderWidget(matrixStack, mouseX, mouseY, 10);
		}

		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);

		GlStateManager.pushMatrix();
		{
			GlStateManager.translatef(-80, 0, 10);
			for (int i = 0; i < 64; i++) {

				if (buttons.get(i).isHovered()) {
					renderTooltip(matrixStack, new StringTextComponent("Rune:" + i), mouseX, mouseY);
				}
			}

			List<ITextComponent> cat1 = new ArrayList<ITextComponent>();
			cat1.add(new StringTextComponent(I18n.format("Clear Runes")));
			if (clearButton.isHovered()) {
				func_243308_b(matrixStack, cat1, left + guiWidth - (guiWidth - 120), top + guiHeight - (170));
			}
			List<ITextComponent> cat9 = new ArrayList<ITextComponent>();
			cat9.add(new StringTextComponent(I18n.format("Chisel Rune")));
			if (chiselButton.isHovered()) {
				func_243308_b(matrixStack, cat9, left + guiWidth - (guiWidth - 120), top + guiHeight - (150));
			}
		}
		GlStateManager.popMatrix();

		if (te.getWorld().getGameTime() % 2 == 0) {
			if (te.getStackInSlot(4) != null && this.te.getStackInSlot(4) != ItemStack.EMPTY) {
				Minecraft.getInstance().textureManager.bindTexture(GUI_Chisel);
				if (te.getStackInSlot(4).getItem() instanceof ItemRunePattern) {
					GlStateManager.translatef(0, 0, -1);
					ItemRunePattern runePattern = (ItemRunePattern) te.getStackInSlot(4).getItem();
					int incPattern = 100;
					for (int i = 0; i < runePatternButtonArray.length; i++) {
						for (int j = 0; j < runePatternButtonArray.length; j++) {

							buttons.add(runePatternButtonArray[i][j] = new GuiButtonTextured(GUI_Chisel, incPattern,
									left + guiWidth - (guiWidth + 80 - (i * 8)), top + guiHeight - (160 - (j * 8)), 8,
									8, 176, 0, runePattern.getRecipe().getActivatedRunes().contains(incPattern - 100),
									null, new IPressable() {
										@Override
										public void onPress(Button press) {
										}
									}));
							incPattern++;
						}
					}
				}
			} else {
				for (int i = 0; i < buttons.size(); i++) {
					if (buttons.get(i) instanceof GuiButtonTextured) {
						GuiButtonTextured test = (GuiButtonTextured) buttons.get(i);
						if (test.getId() > 99) {
							buttons.remove(test);
						}

					}
				}
			}
		}

	}

	@Override
	public void renderBackground(MatrixStack matrixStack) {
		super.renderBackground(matrixStack);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
		this.font.drawString(matrixStack, this.te.getDisplayName().getUnformattedComponentText(), 8, 6, 65444444);
		this.font.drawString(matrixStack, this.playerInv.getDisplayName().getUnformattedComponentText(), 8,
				this.ySize - 92, 000000);
		GlStateManager.pushMatrix();
		if (te.hasValidRecipe()) {
			RecipeChiselStation currentRecipe = te.getCurrentRecipe();
			GlStateManager.translatef(0, 0, 10);
			minecraft.getItemRenderer().renderItemAndEffectIntoGUI(currentRecipe.getOutput(), 145, 44);
			GlStateManager.scalef(0.45f, 0.5f, 0.5f);
			renderTooltip(matrixStack,
					new StringTextComponent(I18n.format(currentRecipe.getOutput().getTranslationKey())), 250, 150);

		}
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		if (te.hasValidRecipe()) {
			Minecraft.getInstance().textureManager.bindTexture(GUI_Chisel);
			if (te.areRunesMatching()) {
				GuiUtils.drawTexturedModalRect(162 - 42, 45 + 32, 176, 96, 16, 16, 10f);
			} else {
				GuiUtils.drawTexturedModalRect(162 - 42, 45 + 32, 176, 80, 16, 16, 10f);
			}
			if (te.getStackInSlot(3).getItem() instanceof ItemKnapper) {
				GlStateManager.enableAlphaTest();
				GuiUtils.drawTexturedModalRect(162 - 42 + 16, 45 + 32, 176 + 16, 96, 16, 16, 10f);
			} else {
				GlStateManager.enableAlphaTest();
				GuiUtils.drawTexturedModalRect(162 - 42 + 16, 45 + 32, 176 + 16, 96 - 16, 16, 16, 10f);
			}
		}

		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		// Prevents the runes from disappering on resize or jei check
		for (int l = 0; l < runeButtonArray.length; l++) {
			for (int m = 0; m < runeButtonArray.length; m++) {
				for (int k = 0; k < this.getActivatedRuneList().size(); k++) {
					if (runeButtonArray[l][m].getId() == this.getActivatedRuneList().get(k)) {
						runeButtonArray[l][m].setState(true);
					}
				}
			}
		}

		GlStateManager.popMatrix();

	}

	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		this.renderBackground(matrixStack);
		Minecraft.getInstance().getTextureManager().bindTexture(GUI_Chisel);
		GuiUtils.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize, 0f);

	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	protected void init() {
		super.init();
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		buttons.clear();
		int inc = 0;
		for (int i = 0; i < runeButtonArray.length; i++) {
			for (int j = 0; j < runeButtonArray.length; j++) {
				this.addButton(runeButtonArray[i][j] = new GuiButtonTextured(GUI_Chisel, inc,
						left + guiWidth - (guiWidth - 50 - (i * 8)), top + guiHeight - (160 - (j * 8)), 8, 8, 176, 0,
						false, null, new IPressable() {
							@Override
							public void onPress(Button press) {
								if (press instanceof GuiButtonTextured) {
									GuiButtonTextured button = (GuiButtonTextured) press;
									if (button.getId() <= 64) {
										if (buttons.get(button.getId()) instanceof GuiButtonTextured) {
											GuiButtonTextured test = (GuiButtonTextured) buttons.get(button.getId());
											if (test.getState() == false) {
												test.setState(true);
												activatedRuneList.add(test.getId());
												PacketHandler.HANDLER
														.sendToServer(new PacketUpdateChiselRunes(activatedRuneList));
											} else if (test.getState() == true) {
												test.setState(false);
												activatedRuneList.remove(Integer.valueOf(test.getId()));
												PacketHandler.HANDLER
														.sendToServer(new PacketUpdateChiselRunes(activatedRuneList));
											}

										}

									}

								}
							}

						}));

				inc++;
			}
		}
		this.addButton(clearButton = new GuiButtonTextured(GUI_Chisel, CLEARBUTTONID,
				left + guiWidth - (guiWidth - 120), top + guiHeight - (170), 16, 16, 176, 16, null, (press) -> {
					// PacketHandler.INSTANCE.sendToServer(new PacketChiselCraftingEvent());
					for (int i = 0; i < 64; i++) {
						if (buttons.get(i) instanceof GuiButtonTextured) {
							GuiButtonTextured test = (GuiButtonTextured) buttons.get(i);
							if (test.getState() == true) {
								test.setState(false);
								activatedRuneList.clear();
								PacketHandler.HANDLER.sendToServer(new PacketUpdateChiselRunes(activatedRuneList));
							}
						}
					}
				}));
		this.addButton(chiselButton = new GuiButtonTextured(GUI_Chisel, CHISELBUTTONID,
				left + guiWidth - (guiWidth - 120), top + guiHeight - (150), 16, 16, 176, 48, null, (press) -> {
					if (te.chestContents.get(3).getItem() != Items.AIR) {
						PacketHandler.HANDLER.sendToServer(new PacketChiselCraftingEvent());
						for (

								int i = 0; i < 64; i++) {
							if (buttons.get(i) instanceof GuiButtonTextured) {
								GuiButtonTextured test = (GuiButtonTextured) buttons.get(i);
								if (test.getState() == true) {
									test.setState(false);
									activatedRuneList.clear();
									PacketHandler.HANDLER.sendToServer(new PacketUpdateChiselRunes(activatedRuneList));
								}

							}
						}
					}
				}));

	}

	public List<Integer> getActivatedRuneList() {
		return activatedRuneList;
	}

	@Override
	protected <T extends IGuiEventListener> T addListener(T listener) {
		return super.addListener(listener);
	}

	public static int[] convertIntegers(List<Integer> integers) {
		int[] ret = new int[integers.size()];
		Iterator<Integer> iterator = integers.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().intValue();
		}
		return ret;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

}