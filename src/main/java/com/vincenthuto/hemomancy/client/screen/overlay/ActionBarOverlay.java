package com.vincenthuto.hemomancy.client.screen.overlay;

import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

/**
 * Vanilla's action bar, word-wrapped. Vanilla draws the message as one centred line, so a long one runs off both
 * edges of the screen. This draws the same fade, colour and backdrop, but splits the message into lines no wider than
 * {@link #wrapWidth} and stacks the extra lines upward, keeping the last line where vanilla puts the only one.
 */
public final class ActionBarOverlay {
    /** Widest line in GUI pixels, about sixty characters; narrower screens keep a gutter on each side instead. */
    private static final int MAX_LINE_WIDTH = 360;
    private static final int SCREEN_GUTTER = 16;
    private static final int LINE_SPACING = 10;

    private ActionBarOverlay() { }

    private static int wrapWidth(int screenWidth) {
        return Math.max(1, Math.min(MAX_LINE_WIDTH, screenWidth - SCREEN_GUTTER * 2));
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Gui gui = minecraft.gui;
        if (gui.overlayMessageString == null || gui.overlayMessageTime <= 0) return;

        float time = gui.overlayMessageTime - deltaTracker.getGameTimeDeltaPartialTick(false);
        int alpha = Math.min(255, (int) (time * 255.0F / 20.0F));
        if (alpha <= 8) return;

        minecraft.getProfiler().push("overlayMessage");
        Font font = gui.getFont();
        List<FormattedCharSequence> lines = font.split(gui.overlayMessageString, wrapWidth(graphics.guiWidth()));
        int widest = 0;
        for (FormattedCharSequence line : lines) widest = Math.max(widest, font.width(line));
        int color = gui.animateOverlayMessageColor
                ? Mth.hsvToArgb(time / 50.0F, 0.7F, 0.6F, alpha)
                : FastColor.ARGB32.color(alpha, -1);

        // Same anchor as vanilla, which lifts the bar above extra health and armour rows.
        int anchor = Math.max(Math.max(gui.leftHeight, gui.rightHeight) + (68 - 59), 68);
        int top = -4 - (lines.size() - 1) * LINE_SPACING;
        graphics.pose().pushPose();
        graphics.pose().translate((float) (graphics.guiWidth() / 2),graphics.guiHeight() - anchor, 0.0F);
        // One backdrop behind every line, so translucent fills don't darken where per-line boxes would overlap.
        int background = minecraft.options.getBackgroundColor(0.0F);
        if (background != 0) {
            graphics.fill(-widest / 2 - 2, top - 2, -widest / 2 + widest + 2, -4 + font.lineHeight + 2,
                    FastColor.ARGB32.multiply(background, color));
        }
        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            graphics.drawString(font, line, -font.width(line) / 2, top + i * LINE_SPACING, color, true);
        }
        graphics.pose().popPose();
        minecraft.getProfiler().pop();
    }
}
