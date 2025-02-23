package com.vincenthuto.hemomancy.client.screen.codex.objects;

import com.vincenthuto.hemomancy.client.screen.codex.AbstractProgressionCodexScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public interface ProgressionObject {

	public   int chapter = 0;
    
	public void setHovering(boolean isHovering);

	public AbstractProgressionCodexScreen getScreen();

	public void setChapter(int chapter);
	public int getChapter();

	public void setHover(float hover);
    
    public int getPosX();
    
    public int getPosY();
    
    public int getWidth();
    
    public int getHeight();
    
    public float getHover();
    
    public  boolean isHovering();
    
    public String getIdentifier();
    public void setIdentifier(String identifier);
	
	public int hoverCap();

	public void render(Minecraft minecraft, GuiGraphics guiGraphics, float xOffset, float yOffset, int mouseX,
			int mouseY, float partialTicks);

	public void lateRender(Minecraft minecraft, GuiGraphics guiGraphics, float xOffset, float yOffset, int mouseX,
			int mouseY, float partialTicks);

	public void click(float xOffset, float yOffset, double mouseX, double mouseY);

	public void exit();

	public boolean isHovering(AbstractProgressionCodexScreen screen, float xOffset, float yOffset, double mouseX,
			double mouseY);

	public int offsetPosX(float xOffset);

	public int offsetPosY(float yOffset);

}
