package com.huto.hemomancy.gui.guide;

import com.huto.hemomancy.Hemomancy;
import com.hutoslib.client.screen.GuiUtils;
import com.mojang.blaze3d.platform.//GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiButtonBookArrow extends Button {

	final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/guidepage.png");

	int buttonWidth = 16;
	int buttonHeight = 14;
	int u = 175;
	int v = 1;
	public int id;

	public GuiButtonBookArrow(int idIn, int x, int y, int widthIn, int heightIn, int uIn, int vIn,
			Button.OnPress pressedAction) {
		super(x, y, widthIn, heightIn, new TextComponent(""), pressedAction);
		this.buttonWidth = widthIn;
		this.buttonHeight = heightIn;
		this.id = idIn;
		this.u = uIn;
		this.v = vIn;

	}

	@SuppressWarnings("deprecation")
	@Override
	public void renderButton(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			// //GlStateManager._enableAlphaTest();
			//GlStateManager._enableBlend();
			Minecraft.getInstance().getTextureManager().bindForSetup(texture);
			if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
				this.isHovered = true;
			} else {
				this.isHovered = false;
			}
			if (this.isHovered) {
				v = 18;
			} else {
				v = 1;
			}
			GuiUtils.drawTexturedModalRect(x, y, u, v, width, height);
		}
	}

	// Stops the clicking noise when the page turn button is pressed
	@Override
	public void playDownSound(SoundManager handler) {
		handler.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f, 1F));
	}

	public int getId() {
		return id;
	}

	public void setid() {
	}
}