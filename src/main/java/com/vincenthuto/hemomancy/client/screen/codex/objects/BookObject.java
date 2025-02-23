package com.vincenthuto.hemomancy.client.screen.codex.objects;

import java.util.List;

import com.vincenthuto.hemomancy.client.screen.codex.AbstractProgressionCodexScreen;
import com.vincenthuto.hutoslib.common.data.skilltree.SkillTreeCodeModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class BookObject implements ProgressionObject {

	public final AbstractProgressionCodexScreen screen;
	public int posX;
	public int posY;
	public final int width;
	public final int height;
	public boolean isHovering;
	public float hover;
	public int chapter;
	public List<Integer> parentId;
	public String identifier;
	public SkillTreeCodeModel model;


	public BookObject(AbstractProgressionCodexScreen screen,SkillTreeCodeModel model, String identifier,int chapter, List<Integer> list, int posX, int posY, int width, int height) {
		this.model = model;
		this.screen = screen;
		this.chapter =chapter;
		this.parentId = list;
		this.posX = posX;
		this.posY = posY;
		this.width = width;
		this.height = height;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

	public boolean isHovering() {
		return isHovering;
	}

	public void setHovering(boolean isHovering) {
		this.isHovering = isHovering;
	}

	public AbstractProgressionCodexScreen getScreen() {
		return screen;
	}

	public void setHover(float hover) {
		this.hover = hover;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public float getHover() {
		return hover;
	}

	public int hoverCap() {
		return 20;
	}

	public void render(Minecraft minecraft, GuiGraphics guiGraphics, float xOffset, float yOffset, int mouseX,
			int mouseY, float partialTicks) {

	}

	public void lateRender(Minecraft minecraft, GuiGraphics guiGraphics, float xOffset, float yOffset, int mouseX,
			int mouseY, float partialTicks) {

	}

	public void click(float xOffset, float yOffset, double mouseX, double mouseY) {

	}

	public void exit() {

	}

	public boolean isHovering(AbstractProgressionCodexScreen screen, float xOffset, float yOffset, double mouseX,
			double mouseY) {

		return screen.isHovering(mouseX, mouseY, offsetPosX(xOffset), offsetPosY(yOffset), width, height);
	}

	public int offsetPosX(float xOffset) {
		int guiLeft = (width - screen.bookWidth) / 2;
		return (int) (guiLeft + this.posX + xOffset);
	}

	public int offsetPosY(float yOffset) {
		int guiTop = (height - screen.bookHeight) / 2;
		return (int) (guiTop + this.posY + yOffset);
	}

	@Override
	public int getChapter() {
		return chapter;
	}

	@Override
	public void setChapter(int chapter) {
		this.chapter = chapter;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

}