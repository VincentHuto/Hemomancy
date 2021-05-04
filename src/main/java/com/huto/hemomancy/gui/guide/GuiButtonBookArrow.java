package com.huto.hemomancy.gui.guide;

import com.huto.hemomancy.Hemomancy;
import com.hutoslib.client.gui.GuiUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
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
			Button.IPressable pressedAction) {
		super(x, y, widthIn, heightIn, new StringTextComponent(""), pressedAction);
		this.buttonWidth = widthIn;
		this.buttonHeight = heightIn;
		this.id = idIn;
		this.u = uIn;
		this.v = vIn;

	}

	@SuppressWarnings("deprecation")
	@Override
	public void renderWidget(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			GlStateManager.enableAlphaTest();
			GlStateManager.enableBlend();
			Minecraft.getInstance().getTextureManager().bindTexture(texture);
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
			GuiUtil.drawTexturedModalRect(x, y, u, v, width, height);
		}
	}

	// Stops the clicking noise when the page turn button is pressed
	@Override
	public void playDownSound(SoundHandler handler) {
		handler.play(SimpleSound.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0f, 1F));
	}

	public int getId() {
		return id;
	}

	public void setid() {
	}
}