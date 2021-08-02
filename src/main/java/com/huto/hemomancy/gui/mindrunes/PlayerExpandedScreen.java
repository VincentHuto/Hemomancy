package com.huto.hemomancy.gui.mindrunes;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.container.PlayerExpandedContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerExpandedScreen extends EffectRenderingInventoryScreen<PlayerExpandedContainer> {

	public static final ResourceLocation background = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/gui_mind_runes.png");

	private float oldMouseX;
	private float oldMouseY;

	public PlayerExpandedScreen(PlayerExpandedContainer container, Inventory inventory, Component name) {
		super(container, inventory, name);
	}

	@Override
	public void tick() { // tick
		this.container.runes.setEventBlock(false);
		this.updateActivePotionEffects();
		this.resetGuiLeft();
	}

	@Override
	protected void init() { // init
		this.buttons.clear(); // this.buttons
		super.init();
		this.resetGuiLeft();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int p_146979_1_, int p_146979_2_) { // drawGuiContainerForegroundLayer
		if (this.minecraft != null) { // this.minecraft
			this.minecraft.fontRenderer.drawText(matrixStack, new TranslationTextComponent("container.crafting"),
					115 - 18, 8 + 22, 4210752);
		}
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) { // render
		this.renderBackground(matrixStack); // renderBackground
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY); // renderHoveredToolTip
		this.oldMouseX = (float) mouseX;
		this.oldMouseY = (float) mouseY;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX,
			int mouseY) { // drawGuiContainerBackgroundLayer
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.minecraft != null) {
			this.minecraft.getTextureManager().bindTexture(background);
			int k = this.guiLeft;
			int l = this.guiTop;
			this.blit(matrixStack, k, l, 0, 0, this.xSize, this.ySize); // blit
			for (int i1 = 0; i1 < this.container.inventorySlots.size(); ++i1) {
				Slot slot = this.container.inventorySlots.get(i1);
				if (slot.getHasStack() && slot.getSlotStackLimit() == 1) {
					this.blit(matrixStack, k + slot.xPos, l + slot.yPos, 200, 0, 16, 16);
				}
			}
			InventoryScreen.drawEntityOnScreen(k + 51, l + 75, 30, (float) (k + 51) - this.oldMouseX,
					(float) (l + 75 - 50) - this.oldMouseY, this.minecraft.player);
		}
	}

	// No Longer necccisairy as you cannot acsess runes from INV anymore
	/*
	 * @Override public boolean keyPressed(int keyCode, int scanCode, int what) { //
	 * keyPressed if
	 * (ClientEventSubscriber.KEY_RUNES.isActiveAndMatches(InputMappings.
	 * getInputByCode(keyCode, scanCode))) { if (this.minecraft != null) {
	 * this.minecraft.player.closeScreen(); } return true; } else { return
	 * super.keyPressed(keyCode, scanCode, what); } }
	 */
	private void resetGuiLeft() {
		this.guiLeft = (this.width - this.xSize) / 2; // width
	}
}