package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/** Direct GUI slide; all positions are in a square field beneath the brass mask. */
public final class HematicMicroscopeOverlay {
    private static final ResourceLocation FRAME = texture("frame"), PLASMA = texture("plasma");
    private static final ResourceLocation[] CELLS = Arrays.stream(EnumBloodTendency.values())
            .map(tendency -> texture("tendencies/" + tendency.name().toLowerCase(Locale.ROOT)))
            .toArray(ResourceLocation[]::new);
    private static final ResourceLocation[] MOTIFS = Arrays.stream(MicroscopeComposition.Motif.values())
            .map(motif -> texture("properties/" + motif.name().toLowerCase(Locale.ROOT)))
            .toArray(ResourceLocation[]::new);
    private static final ResourceLocation DIMMING = texture("screen_dimming"), READOUT = texture("readout");
    private static final ResourceLocation PROGRESS_TRACK = texture("progress_track"), PROGRESS_FILL = texture("progress_fill");
    private static final ResourceLocation DUCTILIS_LINK = texture("ductilis_link");
    private static final ResourceLocation BASE_CELL = Hemomancy.rloc("textures/particle/particle_blood_cell.png");
    private static float opacity = 1;
    private HematicMicroscopeOverlay() {}
    private static ResourceLocation texture(String name) { return Hemomancy.rloc("textures/gui/microscope/" + name + ".png"); }

    public static void render(GuiGraphics gfx, float partialTicks) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || mc.level == null || !mc.options.getCameraType().isFirstPerson() || mc.options.hideGui || !player.isAlive() || player.isSpectator()
                || !player.isUsingItem() || !(player.getUseItem().getItem() instanceof HematicMicroscopeItem)
                || !(player.getOffhandItem().getItem() instanceof BloodVialItem) || !BloodSampleData.isFilled(player.getOffhandItem())) return;
        var animation = com.vincenthuto.hemomancy.client.player.HematicMicroscopeClientState.animation(player);
        if (animation == null) return;
        opacity = animation.overlayAlpha(partialTicks);
        if (opacity < .02F) return;
        var sample = player.getOffhandItem();
        var profile = BloodSampleData.profile(sample, BloodInjectionData.snapshot(true).properties());
        var composition = MicroscopeComposition.of(profile);
        boolean murky = BloodSampleData.entityType(sample) == null;
        boolean reduced = HemoClientConfig.MICROSCOPE_REDUCED_MOTION.get();
        boolean sparse = HemoClientConfig.MICROSCOPE_LOW_DENSITY.get();
        double time = mc.level.getGameTime() + partialTicks;
        float scale = Math.min(1.5F, Math.min(gfx.guiWidth() / 448F, gfx.guiHeight() / 276F));
        float originX = (gfx.guiWidth() - 448 * scale) / 2;
        float originY = (gfx.guiHeight() - 276 * scale) / 2;
        gfx.pose().pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            gfx.setColor(1, 1, 1, opacity);
            gfx.blit(DIMMING, 0, 0, gfx.guiWidth(), gfx.guiHeight(), 0F, 0F, 16, 16, 16, 16);
            gfx.pose().translate(originX, originY, 0);
            gfx.pose().scale(scale, scale, 1);
            gfx.pose().pushPose();
            try {
                gfx.pose().translate(8, 10, 0);
                // GUI scissor coordinates do not inherit the pose transform in 1.21.1.
                gfx.enableScissor(Mth.floor(originX + 8 * scale), Mth.floor(originY + 10 * scale),
                        Mth.ceil(originX + 264 * scale), Mth.ceil(originY + 266 * scale));
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                gfx.blit(PLASMA, 0, 0, 0, 0, 256, 256, 256, 256);
                drawSlide(gfx, composition, time, reduced, sparse, murky);
                // Draw the opaque brass rim over the cells and the translucent glass backing.
                gfx.setColor(1, 1, 1, opacity);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                gfx.blit(FRAME, 0, 0, 0, 0, 256, 256, 256, 256);
            } finally {
                gfx.disableScissor();
                gfx.pose().popPose();
            }
            drawReadout(gfx, profile, murky, Math.min(1F, player.getTicksUsingItem() / 40F), time);
        } finally {
            opacity = 1;
            gfx.setColor(1, 1, 1, 1);
            gfx.pose().popPose();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    private static void drawSlide(GuiGraphics gfx, MicroscopeComposition composition, double clock, boolean reduced, boolean sparse, boolean murky) {
        double time = clock * (reduced ? 0.2 : 1);
        var random = new Random(composition.seed());
        boolean undead = composition.properties().stream().anyMatch(p -> MicroscopeComposition.motif(p) == MicroscopeComposition.Motif.UNDEAD);
        double drift = undead ? Math.sin(time * 0.006) * 2 : time * 0.12;
        for (int i = 0; i < (sparse ? 12 : 26); i++) {
            float x = (float)(30 + (random.nextDouble() * 190 + drift) % 190);
            float y = (float)(30 + random.nextDouble() * 190 + Math.sin(time * 0.018 + i) * (murky ? 1 : 5));
            sprite(gfx, BASE_CELL, 64, x, y, 11 + random.nextFloat() * 9, i * 41, 0.7F, 0.48F, 0.44F, murky ? 0.15F : 0.38F);
        }
        if (murky) return;
        for (var tendency : composition.tendencies()) {
            var color = tendency.getColor();
            // A small neutral highlight makes Mortem/Ferric legible while retaining the enum palette.
            float r = color.getRed() / 255F * 0.8F + 0.2F, g = color.getGreen() / 255F * 0.8F + 0.2F, b = color.getBlue() / 255F * 0.8F + 0.2F;
            for (int i = 0; i < (sparse ? 6 : 12); i++) {
                float x = 35 + random.nextFloat() * 186, y = 35 + random.nextFloat() * 186;
                double phase = i * 1.73 + time * 0.035;
                float dx = (float)Math.sin(phase) * 5, dy = (float)Math.cos(phase * 0.8) * 4;
                float size = 19 + random.nextFloat() * 7, alpha = 0.9F, angle = i * 31;
                switch (tendency) {
                    case ANIMUS -> {
                        float bud = (float)(Math.sin(phase) + 1) * 4;
                        sprite(gfx, CELLS[EnumBloodTendency.ANIMUS.ordinal()], 32, x + bud, y - bud, size * 0.52F, angle, r, g, b, 0.65F);
                    }
                    case FLAMMEUS -> { dy = (float)(-((time * 0.3 + i * 13) % 22)); size *= 0.92F + 0.08F * (float)Math.sin(phase * 3); }
                    case DUCTILIS -> {
                        alpha = 0.55F + 0.45F * (float)Math.pow(Math.max(0, Math.sin(phase)), 4);
                        sprite(gfx, DUCTILIS_LINK, 32, x + 10, y - 5, 32, 0, 1, 1, 1, 1);
                    }
                    case LUX -> { alpha = 0.6F + 0.4F * (float)Math.sin(time * 0.035 - y * 0.014); angle = 0; }
                    case MORTEM -> { alpha = 0.45F + 0.4F * (float)Math.sin(phase); size *= 0.85F + 0.15F * (float)Math.cos(phase); }
                    case CONGEATIO -> {
                        float lattice = (float)Math.pow((Math.sin(time * 0.014) + 1) / 2, 4);
                        x = Mth.lerp(lattice, x, 54 + (i % 4) * 48); y = Mth.lerp(lattice, y, 70 + (i / 4) * 48);
                        dx *= 0.25F; dy *= 0.25F; angle = 0;
                    }
                    case FERRIC -> {
                        x = 45 + (i % 4) * 16 + (i / 4) * 44; y = 70 + (i / 4) * 49;
                        dx = (float)Math.sin(time * 0.008) * 4; dy = 0; angle = 0;
                    }
                    case TENEBRIS -> {
                        alpha = (float)Math.pow(Math.sin(phase * 0.5), 2);
                        if (!reduced) { dx += (float)Math.floor(phase / (Math.PI * 2)) % 3 * 9; }
                    }
                }
                sprite(gfx, CELLS[tendency.ordinal()], 32, x + dx, y + dy, size, angle, r, g, b, Math.max(0.08F, alpha));
            }
        }
        for (var property : composition.properties()) {
            var motif = MicroscopeComposition.motif(property);
            for (int i = 0; i < (sparse ? 4 : 8); i++) {
                float x = 34 + random.nextFloat() * 184, y = 34 + random.nextFloat() * 184;
                float size = 18, angle = i * 27, alpha = 0.5F;
                switch (motif) {
                    case AQUATIC -> { y -= (float)((time * 0.16 + i * 4) % 18); size = 24; }
                    case FLYING -> { x -= (float)Math.sin(time * 0.02 + i) * 12; y += (float)Math.sin(time * 0.01 + i) * 8; }
                    case VENOMOUS -> { alpha = 0.65F; size = 14; }
                    case ARTHROPOD -> { size = 23; x += (float)Math.sin(time * 0.02 + i) * 3; }
                    case COLD_NATIVE -> { double a = i * Math.PI / 4; x = 128 + (float)Math.cos(a) * 94; y = 128 + (float)Math.sin(a) * 94; size = 28; }
                    case NETHER_NATIVE -> { y -= (float)((time * 0.2 + i) % 12); alpha = 0.4F + 0.2F * (float)Math.sin(time * 0.04 + i); }
                    case ENDER -> { alpha = (float)Math.pow(Math.sin(time * 0.018 + i), 2); if (!reduced) x += ((int)(time / 175) % 2) * 12; }
                    case UNDEAD -> { angle = 0; alpha = 0.35F; }
                    case FUNGAL -> { size = 54; angle = i * 53; }
                    case EXPLOSIVE -> size = 16 + (float)(1 + Math.sin(time * 0.018 + i)) * 5;
                    case BURROWING -> { size = 28; angle = 20; }
                    case NEUTRAL -> size = 9;
                }
                sprite(gfx, MOTIFS[motif.ordinal()], 32, x, y, size, angle, 0.8F, 0.83F, 0.7F, alpha);
            }
        }
    }

    private static void sprite(GuiGraphics gfx, ResourceLocation texture, int cellSize,
                               float x, float y, float size, float angle, float r, float g, float b, float alpha) {
        // Fade rotated cells before they reach the see-through backing outside the lens.
        alpha *= Mth.clamp((121F - (float)Math.hypot(x - 128, y - 128) - size * 0.7072F) / 8F, 0F, 1F);
        if (alpha <= 0) return;
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0);
        gfx.pose().mulPose(Axis.ZP.rotationDegrees(angle));
        gfx.pose().scale(size / cellSize, size / cellSize, 1);
        gfx.setColor(r, g, b, alpha * opacity);
        // Filled GUI primitives tear down their own blend state when flushed.
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gfx.blit(texture, -cellSize / 2, -cellSize / 2, 0, 0, cellSize, cellSize, cellSize, cellSize);
        gfx.setColor(1, 1, 1, opacity);
        gfx.pose().popPose();
    }
    private static void screenSprite(GuiGraphics gfx, ResourceLocation texture, int x, int y, int width, int height) {
        gfx.setColor(1, 1, 1, opacity);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gfx.blit(texture, x, y, 0, 0, width, height, width, height);
    }

    private static void drawReadout(GuiGraphics gfx, BloodSampleData.Profile profile, boolean murky, float progress, double time) {
        var mc = Minecraft.getInstance();
        screenSprite(gfx, READOUT, 272, 18, 168, 240);
        var lines = new ArrayList<Component>();
        lines.add(Component.translatable("item.hemomancy.hematic_microscope"));
        var type = BloodSampleData.entityType(mc.player.getOffhandItem());
        lines.add(type == null ? Component.translatable("message.hemomancy.microscope.provenance") : type.getDescription());
        if (!murky && profile.identified()) {
            lines.add(Component.translatable("gui.hemomancy.microscope.identified"));
            lines.add(Component.translatable("gui.hemomancy.microscope.tendencies"));
            if (profile.tendencies().isEmpty()) lines.add(Component.translatable("gui.hemomancy.microscope.none"));
            for (var tendency : profile.tendencies()) lines.add(Component.translatable("blood_tendency.hemomancy." + tendency.name().toLowerCase(Locale.ROOT)));
            lines.add(Component.translatable("gui.hemomancy.microscope.properties"));
            if (profile.properties().isEmpty()) lines.add(Component.translatable("gui.hemomancy.microscope.none"));
            for (var property : profile.properties()) lines.add(Component.translatableWithFallback(
                    "blood_property." + property.getNamespace() + "." + property.getPath().replace('/', '.'), property.toString()));
        } else if (!murky) lines.add(Component.translatable("gui.hemomancy.microscope.focusing"));
        var wrapped = lines.stream().flatMap(line -> mc.font.split(line, 148).stream()).toList();
        int pageSize = 17, pages = Math.max(1, (wrapped.size() + pageSize - 1) / pageSize);
        int page = (int)(time / 140) % pages;
        for (int i = page * pageSize; i < Math.min(wrapped.size(), (page + 1) * pageSize); i++)
            gfx.drawString(mc.font, wrapped.get(i), 282, 29 + (i % pageSize) * 11, (Math.round(opacity * 255) << 24) | 0xE1D3B4, false);
        if (pages > 1) gfx.drawString(mc.font, Component.translatable("gui.hemomancy.microscope.page", page + 1, pages), 282, 222, (Math.round(opacity * 255) << 24) | 0xAA9672, false);
        screenSprite(gfx, PROGRESS_TRACK, 282, 240, 148, 3);
        int filled = murky ? 0 : (int)(148 * (profile.identified() ? 1 : progress));
        // Reveal the painted fill from left to right without stretching its artwork.
        if (filled > 0) gfx.blit(PROGRESS_FILL, 282, 240, 0, 0, filled, 3, 148, 3);
    }
}
