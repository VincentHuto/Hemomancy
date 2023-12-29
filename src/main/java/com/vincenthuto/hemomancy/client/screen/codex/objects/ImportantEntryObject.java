package com.vincenthuto.hemomancy.client.screen.codex.objects;

import com.vincenthuto.hemomancy.client.screen.codex.AbstractProgressionCodexScreen;
import com.vincenthuto.hemomancy.client.screen.codex.HemoCodexHelper;
import com.vincenthuto.hemomancy.client.screen.codex.HemoProgressionScreen;
import com.vincenthuto.hemomancy.client.screen.codex.ProgressionEntry;
import com.vincenthuto.hemomancy.client.screen.codex.BookEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ImportantEntryObject extends EntryObject {

    public ImportantEntryObject(AbstractProgressionCodexScreen screen,int chapter, ProgressionEntry entry, int posX, int posY) {
        super(screen, chapter, entry, posX, posY);
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics guiGraphics, float xOffset, float yOffset, int mouseX, int mouseY, float partialTicks) {
        int posX = offsetPosX(xOffset);
        int posY = offsetPosY(yOffset);
        HemoCodexHelper. renderTransparentTexture(HemoProgressionScreen.FADE_TEXTURE, guiGraphics.pose(), posX - 13, posY - 13, 1, 252, 58, 58, 512, 512);
        HemoCodexHelper. renderTexture(HemoProgressionScreen.FRAME_TEXTURE, guiGraphics.pose(), posX, posY, 34, getFrameTextureV(), width, height, 512, 512);
        HemoCodexHelper. renderTexture(HemoProgressionScreen.FRAME_TEXTURE, guiGraphics.pose(), posX, posY, 133, getBackgroundTextureV(), width, height, 512, 512);
        guiGraphics.renderItem(entry.getIconStack(), posX + 8, posY + 8);
    }
}