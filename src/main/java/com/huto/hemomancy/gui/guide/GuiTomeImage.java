package com.huto.hemomancy.gui.guide;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.gui.GuiUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiTomeImage extends Widget {

	public final ResourceLocation texture;
	public int id, zone, posX, posY, buttonWidth, buttonHeight, u, v;
	boolean state;
	protected Button.ITooltip onTooltip;
	public List<ITextComponent> text;

	int guiWidth = 175;
	int guiHeight = 228;
	int guiLeft = (this.width) / 2 - (this.buttonWidth) / 2;
	int guiTop = (this.height) / 2 - (this.buttonHeight) / 2;
	float scale;

	/***
	 * Full Control With Text
	 * 
	 * @param texIn          Texture Location
	 * @param idIn           widget Id
	 * @param zoneIn         Which image zone to go to 0-11 54x54 each 3 x 4
	 * @param posXIn         Screen X
	 * @param posYIn         Screen Y
	 * @param buttonWidthIn  Button Size Width
	 * @param buttonHeightIn Button Size Height
	 * @param uIn            Texture X Loc
	 * @param vIn            Texture Y Loc
	 */
	public GuiTomeImage(ResourceLocation texIn, int idIn, int zoneIn, int posXIn, int posYIn, int buttonWidthIn,
			int buttonHeightIn, int uIn, int vIn, List<ITextComponent> textIn, float scaleIn) {
		super(posXIn, posYIn, buttonHeightIn, buttonWidthIn, new StringTextComponent(""));
		this.texture = texIn;
		this.id = idIn;
		this.zone = zoneIn;
		this.x = getZoneX(zoneIn);
		this.y = getZoneY(zoneIn);
		this.width = buttonWidthIn;
		this.height = buttonHeightIn;
		this.u = uIn;
		this.v = vIn;
		this.text = textIn;
		this.scale = scaleIn;

	}

	/***
	 * default, image is to size in file, no more no less Let the Logic Work it out
	 * 54 x 54
	 */

	public GuiTomeImage(ResourceLocation texIn, int idIn, int zoneIn) {
		super(0, 0, 54, 54, new StringTextComponent(""));
		this.texture = texIn;
		this.id = idIn;
		this.zone = zoneIn;
		this.x = getZoneX(zoneIn);
		this.y = getZoneY(zoneIn);
		this.width = 54;
		this.height = 54;
		this.u = 0;
		this.v = 0;
		this.text = new ArrayList<ITextComponent>();
		this.scale = 0.01690625F;

	}

	/***
	 * default, image is to size in file, no more no less Let the Logic Work it out
	 * 54 x 54 With Text
	 */

	public GuiTomeImage(ResourceLocation texIn, int idIn, int zoneIn, List<ITextComponent> textIn) {
		super(0, 0, 54, 54, new StringTextComponent(""));
		this.texture = texIn;
		this.id = idIn;
		this.zone = zoneIn;
		this.x = getZoneX(zoneIn);
		this.y = getZoneY(zoneIn);
		this.width = 54;
		this.height = 54;
		this.u = 0;
		this.v = 0;
		this.text = textIn;
		this.scale = 0.01290625F;

	}

	/***
	 * Proper Zone Spacing but allows for image size flexibility
	 */

	public GuiTomeImage(ResourceLocation texIn, int idIn, int zoneIn, int buttonWidthIn, int buttonHeightIn) {
		super(0, 0, buttonHeightIn, buttonWidthIn, new StringTextComponent(""));
		this.texture = texIn;
		this.id = idIn;
		this.zone = zoneIn;
		this.x = getZoneX(zoneIn);
		this.y = getZoneY(zoneIn);
		this.width = buttonWidthIn;
		this.height = buttonHeightIn;
		this.u = 0;
		this.v = 0;
		this.text = new ArrayList<ITextComponent>();
		this.scale = 0.01290625F;

	}

	/***
	 * Proper Zone Spacing but allows for image size flexibility HAS SCALE FACTOR
	 */

	public GuiTomeImage(ResourceLocation texIn, int idIn, int zoneIn, int buttonWidthIn, int buttonHeightIn,
			float scaleIn) {
		super(0, 0, buttonHeightIn, buttonWidthIn, new StringTextComponent(""));
		this.texture = texIn;
		this.id = idIn;
		this.zone = zoneIn;
		this.x = getZoneX(zoneIn);
		this.y = getZoneY(zoneIn);
		this.width = buttonWidthIn;
		this.height = buttonHeightIn;
		this.u = 0;
		this.v = 0;
		this.text = new ArrayList<ITextComponent>();
		this.scale = scaleIn;
	}

	/**
	 * Same as the vanilla render but allows for dynamic control of the image
	 * postion for centering purposes
	 * 
	 * @param matrixStack
	 * @param mouseX
	 * @param mouseY
	 * @param partialTicks
	 * @param centerXIn
	 * @param centerYIn
	 */
	@SuppressWarnings("deprecation")
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, int centerXIn,
			int centerYIn) {
		GlStateManager.enableAlphaTest();
		GlStateManager.enableBlend();

		Minecraft.getInstance().getTextureManager().bindTexture(texture);
		if (mouseX >= x + centerXIn && mouseX <= x + centerXIn + width && mouseY >= y + centerYIn
				&& mouseY <= y + centerYIn + height) {
			this.isHovered = true;
		} else {
			this.isHovered = false;

		}
		GuiUtil.drawScaledTexturedModalRect(x + centerXIn, y + centerYIn, u, v, width, height, scale);
		GlStateManager.disableAlphaTest();
		GlStateManager.disableBlend();

	}

	/**
	 * Old one
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.enableAlphaTest();
		GlStateManager.enableBlend();

		Minecraft.getInstance().getTextureManager().bindTexture(texture);
		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
			this.isHovered = true;
		} else {
			this.isHovered = false;

		}
		GuiUtil.drawScaledTexturedModalRect(x, y, u, v, width, height, scale);
		GlStateManager.disableAlphaTest();
		GlStateManager.disableBlend();

	}

	public int getZoneX(int zoneIn) {
		switch (zoneIn) {
		case 0:
			return 0;
		case 1:
			return 54;
		case 2:
			return 108;
		case 3:
			return 0;
		case 4:
			return 54;
		case 5:
			return 108;
		case 6:
			return 0;
		case 7:
			return 54;
		case 8:
			return 108;
		case 9:
			return 0;
		case 10:
			return 54;
		case 11:
			return 108;
		default:
			return 0;
		}
	}

	public int getZoneY(int zoneIn) {
		switch (zoneIn) {
		case 0:
			return 0;
		case 1:
			return 0;
		case 2:
			return 0;
		case 3:
			return 54;
		case 4:
			return 54;
		case 5:
			return 54;
		case 6:
			return 108;
		case 7:
			return 108;
		case 8:
			return 108;
		case 9:
			return 162;
		case 10:
			return 162;
		case 11:
			return 162;
		default:
			return 0;
		}
	}

	public List<ITextComponent> getText() {
		return text;
	}

	public void setText(List<ITextComponent> text) {
		this.text = text;
	}
}