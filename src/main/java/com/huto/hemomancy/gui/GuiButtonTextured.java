package com.huto.hemomancy.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiButtonTextured extends Button {

	public final ResourceLocation texture;
	public int id, posX, posY, buttonWidth, buttonHeight, u, v, adjV, newV;
	boolean state;
	protected Button.ITooltip onTooltip;
	public static ITextComponent text;
	public Button.IPressable action;

	/***
	 * 
	 * @param texIn          Texture Location
	 * @param idIn           Button Id
	 * @param posXIn         Screen X
	 * @param posYIn         Screen Y
	 * @param buttonWidthIn  Button Size Width
	 * @param buttonHeightIn Button Size Height
	 * @param uIn            Texture X Loc
	 * @param vIn            Texture Y Loc
	 * @param tooltip        Hover Tooltip
	 * @param actionIn       On Pressed Action
	 */

	public GuiButtonTextured(ResourceLocation texIn, int idIn, int posXIn, int posYIn, int buttonWidthIn,
			int buttonHeightIn, int uIn, int vIn, Button.ITooltip tooltip, Button.IPressable actionIn) {
		super(posXIn, posYIn, buttonHeightIn, buttonWidthIn, text, actionIn, tooltip);
		this.texture = texIn;
		this.id = idIn;
		this.posX = posXIn;
		this.posY = posYIn;
		this.width = buttonWidthIn;
		this.height = buttonHeightIn;
		this.u = uIn;
		this.v = vIn;
		this.adjV = vIn + buttonHeightIn;
		this.newV = vIn;
		this.onTooltip = tooltip;
		this.action = actionIn;

	}

	public GuiButtonTextured(ResourceLocation texIn, int idIn, int posXIn, int posYIn, int buttonWidthIn,
			int buttonHeightIn, int uIn, int vIn, boolean stateIn, Button.ITooltip tooltip,
			Button.IPressable actionIn) {
		super(posXIn, posYIn, buttonHeightIn, buttonWidthIn, text, actionIn, tooltip);
		this.texture = texIn;
		this.id = idIn;
		this.posX = posXIn;
		this.posY = posYIn;
		this.width = buttonWidthIn;
		this.height = buttonHeightIn;
		this.u = uIn;
		this.v = vIn;
		this.adjV = vIn + buttonHeightIn;
		this.newV = vIn;
		this.action = actionIn;
		this.state = stateIn;

	}

	@Override
	public void renderWidget(MatrixStack matrix, int mouseX, int mouseY, float particks) {
		
		if (visible) {
			GlStateManager.enableAlphaTest();
			GlStateManager.enableBlend();
			Minecraft.getInstance().getTextureManager().bindTexture(texture);
			if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
				this.isHovered = true;
				v = newV;
				GuiUtil.drawTexturedModalRect(posX, posY, u, adjV, width, height);
			} else if (state == true) {
				v = newV;
				GuiUtil.drawTexturedModalRect(posX, posY, u, adjV, width, height);
			} else {
				this.isHovered = false;
				newV = v;
				GuiUtil.drawTexturedModalRect(posX, posY, u, v, width, height);
			}
			GlStateManager.disableBlend();

			GlStateManager.disableAlphaTest();
		}

	}

	// Stops the clicking noise when the page turn button is pressed
	@Override
	public void playDownSound(SoundHandler handler) {
		handler.play(SimpleSound.master(SoundEvents.ITEM_BOOK_PUT, 1.0f, 1F));
	}

	public Button.IPressable getAction() {
		return action;
	}

	public void setAction(Button.IPressable action) {
		this.action = action;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}
}
