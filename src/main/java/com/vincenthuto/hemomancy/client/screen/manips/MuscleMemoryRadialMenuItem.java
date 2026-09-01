package com.vincenthuto.hemomancy.client.screen.manips;

import com.vincenthuto.hemomancy.client.screen.radial.BlitRadialMenuItem;
import com.vincenthuto.hemomancy.client.screen.radial.DrawingContext;
import com.vincenthuto.hemomancy.client.screen.radial.GenericRadialMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class MuscleMemoryRadialMenuItem extends BlitRadialMenuItem {
    private final Supplier<MuscleMemoryRadialPresentation.State> presentation;
    private final DoubleSupplier reserveFraction;
    private final String sectionBadge;
    private final Runnable click;
    private final List<Component> tooltip;

    public MuscleMemoryRadialMenuItem(GenericRadialMenu owner, ResourceLocation texture, List<Component> tooltip,
            Supplier<MuscleMemoryRadialPresentation.State> presentation,
            DoubleSupplier reserveFraction, String sectionBadge, Runnable click) {
        super(owner, -1, texture, null, 0, 0, 16, 16, 16, 16, tooltip.getFirst(),
                presentation.get().iconTint());
        this.presentation = presentation;
        this.reserveFraction = reserveFraction;
        this.sectionBadge = sectionBadge;
        this.click = click;
        this.tooltip = List.copyOf(tooltip);
    }

    @Override
    public void drawTooltips(DrawingContext context) {
        context.graphics.renderTooltip(context.font, tooltip, Optional.empty(), (int) context.x, (int) context.y);
    }

    @Override
    public int getBackgroundColor(int fallbackColor) {
        return presentation.get().backgroundColor();
    }

    @Override
    public void draw(DrawingContext context) {
        super.draw(context);
        int left = (int) context.x - 8;
        int top = (int) context.y - 8;
        int filled = (int) Math.round(16D * Math.max(0D, Math.min(1D, reserveFraction.getAsDouble())));
        context.graphics.fill(left - 1, top + 17, left + 17, top + 21, 0xD0100305);
        context.graphics.fill(left, top + 18, left + 16, top + 20, 0xC030080C);
        context.graphics.fill(left, top + 18, left + filled, top + 20, 0xFFCC3344);
        drawCracks(context, left, top, presentation.get().cracks());
        context.graphics.drawString(context.font, sectionBadge, left + 11, top + 10, 0xFFFFFFFF, true);
    }

    private static void drawCracks(DrawingContext context, int left, int top,
            MuscleMemoryRadialPresentation.CrackSeverity severity) {
        if (severity == MuscleMemoryRadialPresentation.CrackSeverity.NONE) return;
        int color = severity == MuscleMemoryRadialPresentation.CrackSeverity.DEAD ? 0xFF180808 : 0xFFE4A0A0;
        context.graphics.fill(left + 3, top + 1, left + 4, top + 7, color);
        context.graphics.fill(left + 4, top + 6, left + 8, top + 7, color);
        context.graphics.fill(left + 7, top + 6, left + 8, top + 12, color);
        if (severity == MuscleMemoryRadialPresentation.CrackSeverity.DENSE
                || severity == MuscleMemoryRadialPresentation.CrackSeverity.DEAD) {
            context.graphics.fill(left + 11, top + 2, left + 12, top + 9, color);
            context.graphics.fill(left + 8, top + 8, left + 12, top + 9, color);
            context.graphics.fill(left + 9, top + 12, left + 15, top + 13, color);
        }
        if (severity == MuscleMemoryRadialPresentation.CrackSeverity.DEAD) {
            context.graphics.fill(left, top, left + 16, top + 2, 0xAA000000);
            context.graphics.fill(left, top + 14, left + 16, top + 16, 0xAA000000);
        }
    }

    @Override
    public boolean onClick() {
        click.run();
        return true;
    }
}
