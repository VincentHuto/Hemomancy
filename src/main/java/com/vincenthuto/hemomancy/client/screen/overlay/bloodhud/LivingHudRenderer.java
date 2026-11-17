package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout;
import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout.Rect;
import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout.VialSeat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;


/**
 * Draws the living binding with its heartbeat, the vessel frame's beat, the Temper vials and their trickles. The
 * binding and every beat band are textures built once per (layout, degree); vials and streams are pixel fills the
 * caller batches into one draw.
 */
public final class LivingHudRenderer {
    private static final int BANDS = 8;
    private static final float[] DEEP_RED = {0x28 / 255f, 0, 0x06 / 255f};
    private static final float[] DEEP_AMBER = {0x46 / 255f, 0x16 / 255f, 0};

    private final Minecraft mc = Minecraft.getInstance();
    private final HudAssemblyCache assemblies = new HudAssemblyCache(6);
    private HudMasks masks;
    /** The code, degree and Apotheos flag the binding textures were painted for; -1 when none are. */
    private int bindingCode = -1;
    private int bindingDegree;
    private boolean bindingApotheos;
    private final DynamicTexture[] bindingTextures = new DynamicTexture[BANDS + 1];
    private final ResourceLocation[] bindingIds = new ResourceLocation[BANDS + 1];
    private int vesselBeatFrame = -1;
    private final DynamicTexture[] vesselTextures = new DynamicTexture[BANDS];
    private final ResourceLocation[] vesselIds = new ResourceLocation[BANDS];

    public HudMasks masks() {
        if (masks == null) masks = HudMasksLoader.load(mc.getResourceManager());
        return masks;
    }

    public HudAssembly assembly(BloodHudLayout layout) {
        return assemblies.get(layout, masks());
    }

    /** Releases every texture and cached geometry; the next frame rebuilds from the current resources. */
    public void clear() {
        release(bindingTextures, bindingIds);
        release(vesselTextures, vesselIds);
        bindingCode = -1;
        vesselBeatFrame = -1;
        assemblies.clear();
        masks = null;
    }

    public void drawBinding(GuiGraphics gfx, HudAssembly assembly, int degree, boolean apotheos, float seconds, float bloodRatio) {
        HudBinding binding = assembly.binding();
        int code = assembly.layout().code();
        if (code != bindingCode || degree != bindingDegree || apotheos != bindingApotheos) {
            release(bindingTextures, bindingIds);
            upload(0, binding.paint(degree, apotheos), binding.width(), binding.height(), bindingTextures, bindingIds, "binding");
            for (int band = 0; band < BANDS; band++) {
                upload(band + 1, binding.beatBand(degree, band, BANDS), binding.width(), binding.height(),
                        bindingTextures, bindingIds, "binding_beat");
            }
            bindingCode = code;
            bindingDegree = degree;
            bindingApotheos = apotheos;
        }
        blit(gfx, bindingIds[0], 0, 0, binding.width(), binding.height());
        drawBands(gfx, bindingIds, 1, 0, 0, binding.width(), binding.height(), apotheos, seconds, bloodRatio);
    }

    /** The degree frame's own red (or Apotheos gold) clenching in time with the binding. */
    public void drawVesselBeat(GuiGraphics gfx, ResourceLocation frameTexture, int frameIndex, int x, int y,
                               boolean apotheos, float seconds, float bloodRatio) {
        if (frameIndex != vesselBeatFrame) {
            release(vesselTextures, vesselIds);
            String path = frameTexture.getPath();
            int[] argb = HudMasksLoader.argb(mc.getResourceManager(), path.substring("textures/gui/".length(), path.length() - 4));
            int w = BloodHudLayout.VESSEL_W, h = BloodHudLayout.VESSEL_H;
            for (int band = 0; band < BANDS; band++) {
                int[] mask = new int[argb.length];
                for (int i = 0; i < argb.length; i++) {
                    if (!isRed(argb[i])) continue;
                    int row = i / w;
                    if (Math.min(BANDS - 1, (int) ((h - 1 - row) / (float) (h - 1) * BANDS)) == band) mask[i] = 0xFFFFFFFF;
                }
                upload(band, mask, w, h, vesselTextures, vesselIds, "vessel_beat");
            }
            vesselBeatFrame = frameIndex;
        }
        drawBands(gfx, vesselIds, 0, x, y, BloodHudLayout.VESSEL_W, BloodHudLayout.VESSEL_H, apotheos, seconds, bloodRatio);
    }

    public void drawStreams(GuiGraphics gfx, HudAssembly assembly, TemperReading[] readings, double bloodRatio, float seconds) {
        Rect vessel = assembly.layout().vessel();
        if (vessel == null) return;
        int surface = assembly.vesselSurfaceY(bloodRatio);
        HudMask window = masks().vesselWindow();
        for (VialSeat seat : assembly.vials()) {
            TemperReading reading = readings[seat.school()];
            if (seat.entryX() < 0 || reading == null
                    || !TemperStream.flows(true, reading.held(), reading.cap(), reading.dry())) continue;
            int colour = reading.colour() & 0xFFFFFF;
            int[][] path = assembly.streamPath(seat, surface);
            for (int[] p : path) pixel(gfx, p[0], p[1], 0x8C000000 | colour);
            for (int bead = 0; bead < 3; bead++) {
                int i = Math.floorMod((int) (seconds * 22 + seat.school() * 13 + bead * path.length / 3f), path.length);
                pixel(gfx, path[i][0], path[i][1], 0xFF000000 | lighten(colour, 0.5f));
            }
            int top = Math.max(seat.entryY(), surface) + 1;
            for (int k = 0; k < 7; k++) {
                int px = seat.entryX() + Math.round(Mth.sin(seconds * 1.8f + k * 0.7f + seat.school()) * (k / 4f));
                int py = top + k;
                if (!window.at(px - vessel.x(), py - vessel.y())) continue;
                pixel(gfx, px, py, ((int) (128 * (1 - k / 7f)) << 24) | colour);
            }
        }
    }

    public void drawVials(GuiGraphics gfx, HudAssembly assembly, TemperReading[] readings) {
        if (assembly.bakedVials() != null) {
            drawBakedVials(gfx, assembly, readings);
            return;
        }
        for (VialSeat seat : assembly.vials()) {
            TemperReading reading = readings[seat.school()];
            if (reading == null) {
                TemperVialArt.VialSprite socket = assembly.vialArt().socket(seat.pointsRight());
                for (int i = 0; i < socket.dx().length; i++) {
                    pixel(gfx, seat.mouthX() + socket.dx()[i], seat.mouthY() + socket.dy()[i], socket.argb()[i]);
                }
                continue;
            }
            TemperVialArt.VialSprite sprite = assembly.vialArt().sprite(seat.pointsRight());
            for (int i = 0; i < sprite.dx().length; i++) {
                int argb = sprite.argb()[i];
                pixel(gfx, seat.mouthX() + sprite.dx()[i], seat.mouthY() + sprite.dy()[i], argb);
            }
            int count = TemperVialArt.fillCount(sprite, reading.fraction());
            int liquid = reading.colour() & 0xFFFFFF;
            if (reading.dry()) liquid = mix(liquid, 0x5A5058, 0.5f);
            int surface = count > 0 ? sprite.liquidDy()[count - 1] : 0;
            for (int i = 0; i < count; i++) {
                int dy = sprite.liquidDy()[i];
                int colour = dy == surface ? lighten(liquid, 0.35f)
                        : scale(liquid, 0.85f - 0.25f * Math.min(1f, (dy - surface) / 6f));
                pixel(gfx, seat.mouthX() + sprite.liquidDx()[i], seat.mouthY() + dy, 0xEE000000 | colour);
            }
            // The front of the glass goes over the liquid; a full vial's glass catches the light twice as hard.
            TemperVialArt.VialSprite glass = assembly.vialArt().glass(seat.pointsRight());
            for (int pass = reading.full() ? 2 : 1; pass > 0; pass--) {
                for (int i = 0; i < glass.dx().length; i++) {
                    pixel(gfx, seat.mouthX() + glass.dx()[i], seat.mouthY() + glass.dy()[i], glass.argb()[i]);
                }
            }
        }
    }

    private void drawBakedVials(GuiGraphics gfx, HudAssembly assembly, TemperReading[] readings) {
        BakedVials baked = assembly.bakedVials();
        for (VialSeat seat : assembly.vials()) {
            int school = seat.school();
            TemperReading reading = readings[school];
            if (reading == null) {
                TemperVialArt.VialSprite socket = assembly.vialArt().socket(seat.pointsRight());
                for (int i = 0; i < socket.dx().length; i++) {
                    pixel(gfx, seat.mouthX() + socket.dx()[i], seat.mouthY() + socket.dy()[i], socket.argb()[i]);
                }
                continue;
            }
            baked.forEachArt(school, (x, y, argb) -> pixel(gfx, x, y, argb));
            int[] cells = baked.liquid(school);
            int count = Math.round(cells.length * Math.max(0f, Math.min(1f, reading.fraction())));
            int liquid = reading.colour() & 0xFFFFFF;
            if (reading.dry()) liquid = mix(liquid, 0x5A5058, 0.5f);
            int w = baked.width(), surface = count > 0 ? cells[count - 1] / w : 0;
            for (int i = 0; i < count; i++) {
                int row = cells[i] / w;
                int colour = row == surface ? lighten(liquid, 0.35f) : scale(liquid, 0.85f - 0.25f * Math.min(1f, (row - surface) / 6f));
                pixel(gfx, cells[i] % w, row, 0xEE000000 | colour);
            }
            for (int pass = reading.full() ? 2 : 1; pass > 0; pass--) {
                baked.forEachGlass(school, (x, y, argb) -> pixel(gfx, x, y, argb));
            }
        }
    }

    private void drawBands(GuiGraphics gfx, ResourceLocation[] ids, int first, int x, int y, int w, int h,
                           boolean apotheos, float seconds, float bloodRatio) {
        float[] deep = apotheos ? DEEP_AMBER : DEEP_RED;
        for (int band = 0; band < BANDS; band++) {
            float alpha = HudHeartbeat.darkness(seconds, bloodRatio, (band + 0.5f) / BANDS);
            if (alpha < 0.01f) continue;
            RenderSystem.setShaderColor(deep[0], deep[1], deep[2], alpha);
            blit(gfx, ids[first + band], x, y, w, h);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static boolean isRed(int argb) {
        int a = argb >>> 24, r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        return a > 150 && r > 80 && r > g * 1.5f && r > b * 1.4f;
    }

    private void upload(int slot, int[] argb, int w, int h, DynamicTexture[] textures, ResourceLocation[] ids, String name) {
        NativeImage image = new NativeImage(w, h, false);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int c = argb[y * w + x];
            image.setPixelRGBA(x, y, (c & 0xFF00FF00) | ((c & 0x00FF0000) >>> 16) | ((c & 0xFF) << 16));
        }
        textures[slot] = new DynamicTexture(image);
        ids[slot] = mc.getTextureManager().register("hemomancy_hud_" + name, textures[slot]);
    }

    private void release(DynamicTexture[] textures, ResourceLocation[] ids) {
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] != null) mc.getTextureManager().release(ids[i]);
            ids[i] = null;
            textures[i] = null;
        }
    }

    private static void blit(GuiGraphics gfx, ResourceLocation id, int x, int y, int w, int h) {
        // Fills flush batched render types whose clear state disables blending.
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gfx.blit(id, x, y, 0, 0, w, h, w, h);
    }

    private static void pixel(GuiGraphics gfx, int x, int y, int argb) {
        gfx.fill(x, y, x + 1, y + 1, argb);
    }

    private static int lighten(int rgb, float toward) {
        return mix(rgb, 0xFFFFFF, toward);
    }

    private static int scale(int rgb, float factor) {
        return mix(0, rgb, factor);
    }

    private static int mix(int from, int to, float t) {
        int out = 0;
        for (int shift = 0; shift < 24; shift += 8) {
            int a = (from >> shift) & 0xFF, b = (to >> shift) & 0xFF;
            out |= Mth.clamp(Math.round(a + (b - a) * t), 0, 255) << shift;
        }
        return out;
    }
}
