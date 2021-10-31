package com.vincenthuto.hemomancy.gui.mindrunes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.MenuRunes;

import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerExpandedScreen extends EffectRenderingInventoryScreen<MenuRunes> {

	public static final ResourceLocation background = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/gui_mind_runes.png");

	private float oldMouseX;
	private float oldMouseY;

	public PlayerExpandedScreen(MenuRunes container, Inventory inventory, Component name) {
		super(container, inventory, name);
	}

	// Replacing tick because im lazy
	@Override
	protected boolean isHovering(int p_97768_, int p_97769_, int p_97770_, int p_97771_, double p_97772_,
			double p_97773_) {
		return super.isHovering(p_97768_, p_97769_, p_97770_, p_97771_, p_97772_, p_97773_);
	}

	@Override
	protected void init() { // init
		this.renderables.clear(); // this.renderables
		super.init();
		this.resetGuiLeft();
	}

	@Override
	protected void renderLabels(PoseStack matrixStack, int p_146979_1_, int p_146979_2_) { // drawGuiContainerForegroundLayer
		if (this.minecraft != null) { // this.minecraft
			this.minecraft.font.draw(matrixStack, new TranslatableComponent("container.crafting"), 115 - 18, 8 + 22,
					4210752);
		}
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) { // render
		this.renderBackground(matrixStack); // renderBackground
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY); // renderHoveredToolTip
		this.oldMouseX = mouseX;
		this.oldMouseY = mouseY;
	}

	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) { // drawGuiContainerBackgroundLayer
		if (this.minecraft != null) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, background);

			int k = this.leftPos;
			int l = this.topPos;
			this.blit(matrixStack, k, l, 0, 0, this.getXSize(), this.getYSize()); // blit
			this.blit(matrixStack, k, l, 0, 0, this.imageWidth, this.imageHeight); // blit
			drawTexturedQuad(0, 0, 0, 0, 0, 0, 0, 0, 0);
			for (int i1 = 0; i1 < this.menu.slots.size(); ++i1) {
				Slot slot = this.menu.slots.get(i1);
				if (slot.hasItem() && slot.getMaxStackSize() == 1) {
					this.blit(matrixStack, k + slot.x, l + slot.y, 200, 0, 16, 16);
				}
			}
			InventoryScreen.renderEntityInInventory(k + 51, l + 75, 30, k + 51 - this.oldMouseX,
					l + 75 - 50 - this.oldMouseY, this.minecraft.player);
		}
	}
	
	private void drawTexturedQuad(int x, int y, int width, int height, float tx, float ty, float tw, float th,
			float z) {
		Tesselator tess = Tesselator.getInstance();
		BufferBuilder buffer = tess.getBuilder();

		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		buffer.vertex((double) x + 0, (double) y + height, z).uv(tx, ty + th).endVertex();
		buffer.vertex((double) x + width, (double) y + height, z).uv(tx + tw, ty + th).endVertex();
		buffer.vertex((double) x + width, (double) y + 0, z).uv(tx + tw, ty).endVertex();
		buffer.vertex((double) x + 0, (double) y + 0, z).uv(tx, ty).endVertex();

		tess.end();
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
		this.leftPos = (this.width - this.imageWidth) / 2; // width
	}
}