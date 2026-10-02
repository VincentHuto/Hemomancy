package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeClientPacket;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Blood volume HUD overlay.
 * The main Harbinger vessel is assembled from static texture layers so the HUD
 * does not rebuild the crown, frame, and Apotheos helix pixel-by-pixel each frame.
 */
public class BloodVolumeOverlay {

    private static final int OVERLAY_W = 76;
    private static final int OVERLAY_H = 126;
    private static final float OVERLAY_SCALE = 0.6f;
    private static final int VESSEL_W = 32;
    private static final int VESSEL_H = 92;
    private static final int VESSEL_TOP = 26;
    private static final int VESSEL_BOTTOM = VESSEL_TOP + VESSEL_H;
    private static final int APOTHEOS_FILL_FRAMES = 16;
    private static final int[][] POME_POINTS = {
            {0, -20}, {-15, -14}, {15, -14}, {-27, 5}, {27, 5},
            {-28, 23}, {28, 23}, {-28, 40}, {28, 40}
    };
    private static final int GOURD_W = GourdHudFillPixels.WIDTH;
    private static final int GOURD_H = GourdHudFillPixels.HEIGHT;
    private static final int HORN_W = 32;
    private static final int HORN_H = 48;
    private static final int RIB_H = 64;

    private static final ResourceLocation VESSEL_BACK_TEXTURE = texture("vessel_back");
    private static final ResourceLocation GOURD_BACK_TEXTURE = texture("gourd_back");
    private static final ResourceLocation GOURD_HALO_TEXTURE = texture("gourd_halo");
    private static final ResourceLocation GOURD_WHITE_TEXTURE = texture("gourd_frame_white");
    private static final ResourceLocation GOURD_RED_TEXTURE = texture("gourd_frame_red");
    private static final ResourceLocation GOURD_BLACK_TEXTURE = texture("gourd_frame_black");
    private static final ResourceLocation HORN_BACK_TEXTURE = texture("curved_horn_back");
    private static final ResourceLocation HORN_FRAME_TEXTURE = texture("curved_horn_frame");
    private static final ResourceLocation HORN_MASK_TEXTURE = texture("curved_horn_fill_mask");
    private static final ResourceLocation HORN_HALO_TEXTURE = texture("curved_horn_halo");
    private static final ResourceLocation RIB_BACK_TEXTURE = texture("hemorath_rib_back");
    private static final ResourceLocation RIB_FRAME_TEXTURE = texture("hemorath_rib_frame");
    private static final ResourceLocation RIB_MASK_TEXTURE = texture("hemorath_rib_fill_mask");
    private static final ResourceLocation RIB_HALO_TEXTURE = texture("hemorath_rib_halo");
    private static final ResourceLocation APOTHEOS_HALO_TEXTURE = texture("halo_apotheos");
    private static final ResourceLocation[] DEGREE_BASE_TEXTURES = textureRange("base_degree_", 9);
    private static final ResourceLocation[] POME_TEXTURES = textureRange("pomes_", 10);
    private static final ResourceLocation[] APOTHEOS_FILL_TEXTURES = textureRange("fill_apotheos_", APOTHEOS_FILL_FRAMES);

    private static final int BORDER_OUTER = 0xFF330808;
    private static final int BORDER_INNER = 0xFF220606;
    private static final int BAR_BG = 0xFF060102;
    private static final int FERRIC = 0xFF7D4A24;
    private static final int GOLD_SEAL = 0xFFB66D2A;
    private static final int POME_EMPTY = 0xFF120813;
    private static final int POME_FILLED = 0xFF1B061F;
    private static final int POME_STROKE_EMPTY = 0x885A1A39;
    private static final int POME_STROKE_FILLED = 0xFFC73764;

    public static BloodVolumeOverlay instance;

    private final Minecraft mc = Minecraft.getInstance();
    private final BloodHudRefreshClock refreshClock = new BloodHudRefreshClock();
    private DynamicTexture gourdFillTexture;
    private ResourceLocation gourdFillTextureId;
    private int lastGourdFrame = Integer.MIN_VALUE;
    private double lastGourdRatio = Double.NaN;
    private int lastGourdDegree = -1;
    private int lastGourdPomes = -1;
    private DynamicTexture specialFillTexture;
    private ResourceLocation specialFillTextureId;
    private ResourceLocation lastSpecialMaskId;
    private boolean[] hornFillMask;
    private boolean[] ribFillMask;
    private int lastSpecialFrame = Integer.MIN_VALUE;
    private double lastSpecialRatio = Double.NaN;
    private int lastSpecialDegree = -1;
    private int lastSpecialPomes = -1;
    private DynamicTexture bloodFillTexture;
    private ResourceLocation bloodFillTextureId;
    private VesselFillMask vesselFillMask;
    private int lastFillFrame = Integer.MIN_VALUE;
    private int lastFillHeight = -1;
    private int lastFillDegree = -1;
    private int lastFillPomes = -1;

    public void clearCaches() {
        refreshClock.reset();
        hornFillMask = null;
        ribFillMask = null;
        vesselFillMask = null;
        lastSpecialMaskId = null;
        if (specialFillTextureId != null) {
            mc.getTextureManager().release(specialFillTextureId);
            specialFillTexture = null;
            specialFillTextureId = null;
        }
        lastSpecialFrame = Integer.MIN_VALUE;
        lastSpecialRatio = Double.NaN;
        lastSpecialDegree = -1;
        lastSpecialPomes = -1;
        if (gourdFillTextureId != null) {
            mc.getTextureManager().release(gourdFillTextureId);
            gourdFillTexture = null;
            gourdFillTextureId = null;
            lastGourdFrame = Integer.MIN_VALUE;
            lastGourdRatio = Double.NaN;
            lastGourdDegree = -1;
            lastGourdPomes = -1;
        }
        if (bloodFillTextureId != null) {
            mc.getTextureManager().release(bloodFillTextureId);
            bloodFillTexture = null;
            bloodFillTextureId = null;
            lastFillFrame = Integer.MIN_VALUE;
            lastFillHeight = -1;
            lastFillDegree = -1;
            lastFillPomes = -1;
        }
    }

    public static boolean isConfiguredOnLeftSide() {
        int positionLoc = HemoClientConfig.HUD_LOCATION.get();
        return positionLoc == 0 || positionLoc == 2;
    }

    public static int getConfiguredBarX(int screenWidth) {
        int positionLoc = HemoClientConfig.HUD_LOCATION.get();
        return switch (positionLoc) {
            case 1, 3 -> screenWidth - getBarWidth() - 4;
            default -> 4;
        };
    }

    public static int getConfiguredBarY(Player player, int screenHeight) {
        int positionLoc = HemoClientConfig.HUD_LOCATION.get();
        return switch (positionLoc) {
            case 1 -> player != null && player.getActiveEffects().isEmpty() ? 4 : 30;
            case 2, 3 -> screenHeight - getBarHeight() - 30;
            default -> 4;
        };
    }

    public static int getBarWidth() {
        return Math.round(OVERLAY_W * OVERLAY_SCALE);
    }

    public static int getBarHeight() {
        return Math.round(OVERLAY_H * OVERLAY_SCALE);
    }

    static float getVesselEdgeX(boolean barOnLeft, int barX) {
        return barX + (OVERLAY_W / 2.0f + (barOnLeft ? VESSEL_W : -VESSEL_W) / 2.0f) * OVERLAY_SCALE;
    }

    static float getVesselCenterY(int barY) {
        return barY + (VESSEL_TOP + VESSEL_H / 2.0f) * OVERLAY_SCALE;
    }

    public void renderHUD(GuiGraphics gfx, int width, int height, float partialTicks) {
        LocalPlayer player = this.mc.player;
        if (player == null) return;

        HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(cap -> {
            if (UnstainedAccessRules.blocksKnownBloodPowerUse(cap)) return;

            HemoCapabilityAccess.getBloodVolume(player).ifPresent(bloodCap -> {
                if (bloodCap == null || !bloodCap.isActive()) return;
                HemoCapabilityAccess.getEquipment(player).ifPresent(inv -> {
                    if (inv.getStackInSlot(5).getItem() instanceof VasculariumCharmItem) {
                        if (refreshClock.due(player.tickCount)) {
                            PacketHandler.sendToServer(new BloodVolumeClientPacket());
                        }

                        int posX = getConfiguredBarX(width);
                        int posY = getConfiguredBarY(player, height);
						float animationTime = EquippedMorphlingOverlayPlacement.animationTimeSeconds(
								mc.level.getGameTime(), partialTicks);
						renderBloodBar(gfx, posX, posY, bloodCap, player, mc.level, animationTime, width, height);
						HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(morphlingCap -> {
							ItemStack equipped = morphlingCap.getEquippedMorphling();
							if (EquippedMorphlingOverlay.instance != null) {
								EquippedMorphlingOverlay.instance.renderForBloodBar(gfx, equipped,
										isConfiguredOnLeftSide(), posX, posY, getBarWidth(), getBarHeight(),
										animationTime);
							}
						});
                    }
                });
            });
        });
    }

    private void renderBloodBar(GuiGraphics gfx, int posX, int posY, IBloodVolume bloodCap,
                                Player player, ClientLevel world, float animationTime, int screenWidth, int screenHeight) {
        gfx.pose().pushPose();
        gfx.pose().translate(posX, posY, 0);
        gfx.pose().scale(OVERLAY_SCALE, OVERLAY_SCALE, 1.0f);
        renderBloodBarScaled(gfx, 0, 0, bloodCap, player, world, animationTime,
                Math.round((screenWidth - posX) / OVERLAY_SCALE),
                Math.round((screenHeight - posY) / OVERLAY_SCALE));
        gfx.pose().popPose();
    }

    private void renderBloodBarScaled(GuiGraphics gfx, int posX, int posY, IBloodVolume bloodCap,
                                      Player player, ClientLevel world, float animationTime, int screenWidth, int screenHeight) {
        Font fr = mc.font;
        float time = animationTime;

        double vol = bloodCap.getBloodVolume();
        double maxVol = bloodCap.getMaxBloodVolume();
        double ratio = maxVol > 0 ? Mth.clamp(vol / maxVol, 0, 1) : 0;

        int pomeProgress = HemoCapabilityAccess.getInitiatoryDegree(player)
                .map(d -> d.getTotalPomesConsumed())
                .orElse(0);
        pomeProgress = Mth.clamp(pomeProgress, 0, 9);

        int degreeNumber = Mth.clamp(HemoCapabilityAccess.getPlayerDegreeNumber(player), 0, 8);
        if (degreeNumber >= 8) {
            pomeProgress = 9;
        }
        boolean isApotheos = degreeNumber >= 8;

        int centerX = posX + OVERLAY_W / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (isApotheos) {
            blitOverlay(gfx, APOTHEOS_HALO_TEXTURE, posX, posY);
        }
        blitOverlay(gfx, VESSEL_BACK_TEXTURE, posX, posY);
        renderLayeredBloodFill(gfx, posX, posY, ratio, degreeNumber, pomeProgress, time);
        blitOverlay(gfx, DEGREE_BASE_TEXTURES[degreeNumber], posX, posY);
        if (degreeNumber >= 7) {
            renderPomeSockets(gfx, posX, posY, pomeProgress);
        }
        renderEquippedGourd(gfx, player, posX, posY, screenWidth, screenHeight,
                degreeNumber, pomeProgress, time);

        RenderSystem.disableBlend();

        drawScaledCenteredString(gfx, fr, Component.translatable("hemomancy.degree." + degreeNumber),
                centerX, posY  +125, 0xFFD9A29A, 0.55f);
        String volText = String.format("%.0f", vol);
        int textX = centerX - (fr.width(volText) / 2);
        gfx.drawString(fr, Component.literal(volText).append("ml"), textX, posY + 130, 0xFFE05A5A, true);
    }

    private static ResourceLocation texture(String name) {
        return Hemomancy.rloc("textures/gui/blood_overlay/" + name + ".png");
    }

    private static ResourceLocation[] textureRange(String prefix, int count) {
        ResourceLocation[] textures = new ResourceLocation[count];
        for (int i = 0; i < count; i++) {
            textures[i] = texture(prefix + i);
        }
        return textures;
    }

    private void blitOverlay(GuiGraphics gfx, ResourceLocation texture, int x, int y) {
        gfx.blit(texture, x, y, 0, 0, OVERLAY_W, OVERLAY_H, OVERLAY_W, OVERLAY_H);
    }

    private void renderPomeSockets(GuiGraphics gfx, int x, int y, int pomeProgress) {
        if (!HemoClientConfig.RENDER_CROWN_POMES_AS_ITEMS.get()) {
            blitOverlay(gfx, POME_TEXTURES[pomeProgress], x, y);
            return;
        }

        blitOverlay(gfx, POME_TEXTURES[0], x, y);
        ItemStack pomeStack = new ItemStack(ItemInit.qliphoth_pome.get());
        int centerX = x + OVERLAY_W / 2;
        int vesselY = y + VESSEL_TOP;
        for (int i = 0; i < pomeProgress && i < POME_POINTS.length; i++) {
            int itemX = centerX + POME_POINTS[i][0] - 8;
            int itemY = vesselY + POME_POINTS[i][1] - 8;
            gfx.renderItem(pomeStack, itemX, itemY);
        }
    }

    private void renderLayeredBloodFill(GuiGraphics gfx, int x, int y, double ratio, int degree, int pomeProgress, float time) {
        if (ratio <= 0.0D) {
            return;
        }

        int fillOffset = VESSEL_TOP + VESSEL_H - (int) Math.round(VESSEL_H * ratio);
        int fillHeight = VESSEL_BOTTOM - fillOffset;
        if (fillHeight <= 0) {
            return;
        }

        if (degree >= 8) {
            int frame = Math.floorMod((int) (time * 8.0f), APOTHEOS_FILL_FRAMES);
            ResourceLocation fillTexture = APOTHEOS_FILL_TEXTURES[frame];
            gfx.blit(fillTexture, x, y + fillOffset, 0, fillOffset, OVERLAY_W, fillHeight, OVERLAY_W, OVERLAY_H);
            renderBloodBubbles(gfx, x, y, fillOffset, fillHeight, time, 1.0f);
        } else {
            renderProceduralBloodFill(gfx, x, y, fillOffset, fillHeight, degree, pomeProgress, time);
        }

        renderFillMeniscus(gfx, x, y, fillOffset, degree, pomeProgress, time);
    }

    private void renderProceduralBloodFill(GuiGraphics gfx, int x, int y, int fillOffset, int fillHeight,
                                           int degree, int pomeProgress, float time) {
        int centerX = x + OVERLAY_W / 2;
        int fillBottom = fillOffset + fillHeight;
        float corruption = degree >= 7 ? Mth.clamp(pomeProgress / 9.0f, 0.0f, 1.0f) : 0.0f;

        if (bloodFillTexture == null) {
            VesselFillMask mask = vesselFillMask();
            bloodFillTexture = new DynamicTexture(new NativeImage(mask.width(), mask.height(), false));
            bloodFillTextureId = mc.getTextureManager().register("blood_vessel_fill", bloodFillTexture);
        }
        int frame = (int) (time * 60.0f);
        if (frame != lastFillFrame || fillHeight != lastFillHeight || degree != lastFillDegree
                || pomeProgress != lastFillPomes) {
            VesselFillMask mask = vesselFillMask();
            int[] pixels = BloodFillPixels.render(mask.pixels(), mask.width(), mask.height(),
                    fillHeight / (double) VESSEL_H, degree, pomeProgress, frame / 60.0f);
            uploadPixels(bloodFillTexture, pixels, mask.width(), mask.height());
            lastFillFrame = frame;
            lastFillHeight = fillHeight;
            lastFillDegree = degree;
            lastFillPomes = pomeProgress;
        }
        blitOverlay(gfx, bloodFillTextureId, x, y);

        for (int local = fillOffset; local < fillBottom; local++) {
            int vesselLocalY = Mth.clamp(local - VESSEL_TOP, 0, VESSEL_H - 1);
            int halfInner = Math.max(1, vesselHalfWidth(vesselLocalY, false));
            if (degree >= 6 && (vesselLocalY + (int) (time * 9.0f)) % 19 < 2) {
                int veinX = centerX + Math.round(Mth.sin(time * 0.9f + vesselLocalY * 0.18f) * (halfInner - 3));
                if (vesselFillMask().contains(veinX - x, local) && vesselFillMask().contains(veinX - x + 1, local))
                    gfx.fill(veinX, y + local, veinX + 2, y + local + 1,
                        alphaBlend(0x6635080A, ((int) (90 * corruption) << 24) | 0x00010002));
            }
        }

        renderBloodBubbles(gfx, x, y, fillOffset, fillHeight, time, corruption);
    }

    private void renderBloodBubbles(GuiGraphics gfx, int x, int y, int fillOffset, int fillHeight, float time, float corruption) {
        if (fillHeight <= 5) {
            return;
        }

        int centerX = x + OVERLAY_W / 2;
        Random bubbleRand = new Random(7777L);
        for (int i = 0; i < 7; i++) {
            float speed = 0.18f + bubbleRand.nextFloat() * 0.38f;
            float phase = bubbleRand.nextFloat();
            float progress = (time * speed + phase) % 1.0f;
            int localY = fillOffset + fillHeight - 3 - Math.round(progress * Math.max(fillHeight - 5, 1));
            if (localY < fillOffset + 2 || localY >= fillOffset + fillHeight - 1) {
                continue;
            }

            int vesselLocalY = Mth.clamp(localY - VESSEL_TOP, 0, VESSEL_H - 1);
            int halfInner = Math.max(2, vesselHalfWidth(vesselLocalY, false) - 3);
            int bubbleX = centerX - halfInner + bubbleRand.nextInt(Math.max(halfInner * 2, 1));
            int alpha = (int) (Mth.clamp((1.0f - Math.abs(progress - 0.5f) * 1.7f), 0.0f, 1.0f) * 72);
            int color = alphaBlend((alpha << 24) | 0x00FF4A40, ((int) (120 * corruption) << 24) | 0x00100616);
            if (vesselFillMask().contains(bubbleX - x, localY))
                gfx.fill(bubbleX, y + localY, bubbleX + 1, y + localY + 1, color);
            if (i % 3 == 0 && vesselFillMask().contains(bubbleX - x + 1, localY + 1)) {
                gfx.fill(bubbleX + 1, y + localY + 1, bubbleX + 2, y + localY + 2, color & 0x88FFFFFF);
            }
        }
    }

    private void renderFillMeniscus(GuiGraphics gfx, int x, int y, int fillOffset, int degree, int pomeProgress, float time) {
        int localY = Mth.clamp(fillOffset - VESSEL_TOP, 0, VESSEL_H - 1);
        int halfInner = Math.max(1, vesselHalfWidth(localY, false));
        int rowY = y + fillOffset;
        int meniscus = BloodFillPixels.meniscusColor(degree, pomeProgress);
        int wave = Math.round(Mth.sin(time * 2.2f + localY * 0.3f) * 1.25f);
        for (int localX = OVERLAY_W / 2 - halfInner; localX <= OVERLAY_W / 2 + halfInner; localX++) {
            if (vesselFillMask().contains(localX, fillOffset + wave))
                gfx.fill(x + localX, rowY + wave, x + localX + 1, rowY + wave + 1, meniscus);
        }
    }

    private VesselFillMask vesselFillMask() {
        if (vesselFillMask != null) return vesselFillMask;
        try (InputStream stream = mc.getResourceManager().open(VESSEL_BACK_TEXTURE);
             NativeImage image = NativeImage.read(stream)) {
            boolean[] pixels = new boolean[image.getWidth() * image.getHeight()];
            for (int row = 0; row < image.getHeight(); row++) {
                for (int col = 0; col < image.getWidth(); col++) {
                    pixels[row * image.getWidth() + col] = (image.getPixelRGBA(col, row) >>> 24) != 0;
                }
            }
            vesselFillMask = new VesselFillMask(image.getWidth(), image.getHeight(), pixels);
            return vesselFillMask;
        } catch (IOException error) {
            throw new IllegalStateException("Unable to load blood vessel HUD mask", error);
        }
    }

    private record VesselFillMask(int width, int height, boolean[] pixels) {
        boolean contains(int x, int y) {
            if (x < 0 || x >= OVERLAY_W || y < 0 || y >= OVERLAY_H) return false;
            int col = (int) ((x + 0.5) * width / OVERLAY_W);
            int row = (int) ((y + 0.5) * height / OVERLAY_H);
            return pixels[row * width + col];
        }
    }

    private void renderDegreeOrnament(GuiGraphics gfx, int centerX, int vesselY, int degree, float time) {
        if (degree >= 2) {
            drawSidePlate(gfx, centerX - 22, vesselY + 28, false, BORDER_OUTER, BORDER_INNER);
            drawSidePlate(gfx, centerX + 22, vesselY + 28, true, BORDER_OUTER, BORDER_INNER);
        }
        if (degree >= 3) {
            drawBand(gfx, centerX, vesselY + 30, 19, FERRIC);
            drawBand(gfx, centerX, vesselY + 56, 17, FERRIC);
            drawBand(gfx, centerX, vesselY + 82, 15, FERRIC);
        }
        if (degree >= 4) {
            for (int i = 0; i < 4; i++) {
                int y = vesselY + 38 + i * 11;
                drawLine(gfx, centerX - 17, y, centerX - 12, y + 4, 0xFF9A1C1A);
                drawLine(gfx, centerX + 17, y, centerX + 12, y + 4, 0xFF9A1C1A);
            }
        }
        if (degree >= 5) {
            drawDiamond(gfx, centerX, vesselY + 22, 3, 0xFF2B0707, GOLD_SEAL);
            drawDiamond(gfx, centerX - 18, vesselY + 88, 3, 0xFF2B0707, GOLD_SEAL);
            drawDiamond(gfx, centerX + 18, vesselY + 88, 3, 0xFF2B0707, GOLD_SEAL);
            drawBand(gfx, centerX, vesselY + 26, 20, 0xCCB66D2A);
        }
        if (degree >= 6) {
            float pulse = 0.55f + 0.45f * Mth.sin(time * 1.7f);
            int veinColor = ((int) (120 + 70 * pulse) << 24) | 0x2B0306;
            drawLine(gfx, centerX - 8, vesselY + 32, centerX + 7, vesselY + 100, veinColor);
            drawLine(gfx, centerX + 8, vesselY + 32, centerX - 7, vesselY + 100, veinColor);
        }
        if (degree >= 7) {
            drawCrownArc(gfx, centerX, vesselY, 0x884A1516);
        }
    }

    private void drawSidePlate(GuiGraphics gfx, int x, int y, boolean right, int outer, int inner) {
        int dir = right ? 1 : -1;
        for (int i = 0; i < 52; i++) {
            int inset = i < 10 ? i / 4 : i > 40 ? (52 - i) / 4 : 2;
            int px = x + dir * inset;
            gfx.fill(px - (right ? 0 : 2), y + i, px + (right ? 3 : 1), y + i + 1, outer);
            if (i > 4 && i < 47) {
                gfx.fill(px - (right ? 0 : 1), y + i, px + (right ? 2 : 1), y + i + 1, inner);
            }
        }
    }

    private void drawBand(GuiGraphics gfx, int centerX, int y, int halfWidth, int color) {
        gfx.fill(centerX - halfWidth, y - 1, centerX + halfWidth + 1, y + 2, 0xAA210606);
        gfx.fill(centerX - halfWidth + 2, y, centerX + halfWidth - 1, y + 1, color);
    }

    private void renderCrownVessel(GuiGraphics gfx, int x, int y, double ratio, float corruption, int degree, float time) {
        int centerX = x + VESSEL_W / 2;
        boolean apotheos = degree >= 8;
        int fillTop = y + VESSEL_H - (int) Math.round(VESSEL_H * ratio);

        for (int py = 0; py < VESSEL_H; py++) {
            int halfOuter = vesselHalfWidth(py, true);
            int halfInner = Math.max(0, vesselHalfWidth(py, false));
            int screenY = y + py;

            gfx.fill(centerX - halfOuter, screenY, centerX + halfOuter + 1, screenY + 1, BORDER_OUTER);
            if (halfOuter > halfInner) {
                gfx.fill(centerX - halfOuter + 2, screenY, centerX + halfOuter - 1, screenY + 1, BORDER_INNER);
            }
            gfx.fill(centerX - halfInner, screenY, centerX + halfInner + 1, screenY + 1, BAR_BG);

            if (screenY < fillTop || halfInner <= 1) {
                continue;
            }

            if (apotheos) {
                float depth = Mth.clamp((screenY - fillTop) / (float) Math.max(VESSEL_H, 1), 0.0f, 1.0f);
                float pulse = 0.82f + 0.18f * Mth.sin(time * 1.35f + py * 0.08f);
                int voidColor = multiplyColor(blendColor(0xEE08010B, 0xEE000000, depth), 0.95f);
                int veinColor = multiplyColor(blendColor(0xEEC91F25, 0xEE5C0508, depth), pulse);
                gfx.fill(centerX - halfInner, screenY, centerX + halfInner + 1, screenY + 1, voidColor);

                int pitch = 15;
                int stripeWidth = 5;
                int phase = Math.floorMod((int) (py * 0.55f - time * 10.0f), pitch);
                for (int start = -halfInner - pitch + phase; start <= halfInner; start += pitch) {
                    int x0 = Math.max(-halfInner, start);
                    int x1 = Math.min(halfInner + 1, start + stripeWidth);
                    if (x1 > x0) {
                        gfx.fill(centerX + x0, screenY, centerX + x1, screenY + 1, veinColor);
                    }
                }

                if (Math.abs(screenY - fillTop) <= 1) {
                    gfx.fill(centerX - halfInner, screenY, centerX + halfInner + 1, screenY + 1, 0xAA560710);
                }
                continue;
            }

            for (int px = -halfInner; px <= halfInner; px++) {
                float depth = Mth.clamp((screenY - fillTop) / (float) Math.max(VESSEL_H, 1), 0.0f, 1.0f);
                float pulse = 0.78f + 0.22f * Mth.sin(time * 1.45f + py * 0.11f);
                int color;
                float fade = 1.0f - corruption * 0.76f;
                color = multiplyColor(blendColor(0xEECF2527, 0xEE520507, depth), pulse * fade);
                if (corruption > 0) {
                    color = alphaBlend(color, ((int) (120 * corruption) << 24) | 0x00030106);
                }
                gfx.fill(centerX + px, screenY, centerX + px + 1, screenY + 1, color);
            }

            if (Math.abs(screenY - fillTop) <= 1) {
                int meniscus = alphaBlend(0xBBDD2F2F, ((int) (120 * corruption) << 24) | 0x00030106);
                gfx.fill(centerX - halfInner, screenY, centerX + halfInner + 1, screenY + 1, meniscus);
            }
        }

        int capColor = degree >= 5 ? GOLD_SEAL : BORDER_OUTER;
        gfx.fill(centerX - 6, y - 5, centerX + 7, y + 1, BORDER_OUTER);
        gfx.fill(centerX - 4, y - 4, centerX + 5, y + 1, BORDER_INNER);
        gfx.fill(centerX - 13, y - 1, centerX + 14, y + 3, capColor);
        gfx.fill(centerX - 10, y, centerX + 11, y + 2, BORDER_OUTER);
        gfx.fill(centerX - 12, y + VESSEL_H, centerX + 13, y + VESSEL_H + 3, BORDER_OUTER);

        for (int tick = 1; tick <= 3; tick++) {
            int tickY = y + VESSEL_H - (VESSEL_H * tick / 4);
            gfx.fill(centerX + 17, tickY, centerX + 20, tickY + 1, 0x50FFFFFF);
        }

        Random bubbleRand = new Random(7777L);
        for (int i = 0; i < 7 && ratio > 0; i++) {
            float bSpeed = 0.25f + bubbleRand.nextFloat() * 0.55f;
            float bPhase = bubbleRand.nextFloat() * 100f;
            float bProgress = ((time * bSpeed + bPhase) % 1.0f);
            int by = y + VESSEL_H - (int) (bProgress * VESSEL_H * ratio);
            if (by < fillTop + 2 || by > y + VESSEL_H - 4) continue;
            int localY = Mth.clamp(by - y, 0, VESSEL_H - 1);
            int half = Math.max(2, vesselHalfWidth(localY, false) - 3);
            int bx = centerX - half + bubbleRand.nextInt(half * 2);
            int alpha = (int) (60 * (1f - Math.abs(bProgress - 0.5f) * 2f));
            gfx.fill(bx, by, bx + 1, by + 1, (alpha << 24) | 0x00FF4A40);
        }

        gfx.fill(centerX - 10, y + 8, centerX - 8, y + VESSEL_H - 12, 0x44FF9B8F);
    }

    private int vesselHalfWidth(int py, boolean outer) {
        return BloodVesselShape.halfWidth(py, outer);
    }

    private void renderPomeCrown(GuiGraphics gfx, int centerX, int vesselY, int progress, boolean apotheos, float time) {
        int[][] points = {
                {0, -13}, {-15, -7}, {15, -7}, {-27, 5}, {27, 5},
                {-31, 23}, {31, 23}, {-20, 40}, {20, 40}
        };
        float pulse = apotheos ? 0.75f + 0.25f * Mth.sin(time * 2.3f) : 0.45f + 0.18f * Mth.sin(time * 1.7f);

        for (int i = 0; i < points.length; i++) {
            int x = centerX + points[i][0];
            int y = vesselY + points[i][1];
            boolean filled = i < progress;
            int fill = filled ? POME_FILLED : POME_EMPTY;
            int stroke = filled ? POME_STROKE_FILLED : POME_STROKE_EMPTY;
            if (filled && apotheos) {
                renderEllipse(gfx, x, y, 6, 6, ((int) (90 * pulse) << 24) | 0x00481864);
            }
            drawDiamond(gfx, x, y, 4, fill, stroke);
            if (filled) {
                gfx.fill(x, y, x + 1, y + 1, 0xFFCF4B78);
            }
        }
    }

    private void drawCrownArc(GuiGraphics gfx, int centerX, int vesselY, int color) {
        drawLine(gfx, centerX - 31, vesselY + 23, centerX - 27, vesselY + 5, color);
        drawLine(gfx, centerX - 27, vesselY + 5, centerX - 15, vesselY - 7, color);
        drawLine(gfx, centerX - 15, vesselY - 7, centerX, vesselY - 13, color);
        drawLine(gfx, centerX, vesselY - 13, centerX + 15, vesselY - 7, color);
        drawLine(gfx, centerX + 15, vesselY - 7, centerX + 27, vesselY + 5, color);
        drawLine(gfx, centerX + 27, vesselY + 5, centerX + 31, vesselY + 23, color);
    }

    private void renderApotheosHalo(GuiGraphics gfx, int cx, int cy, float time) {
        float pulse = 0.65f + 0.35f * Mth.sin(time * 1.9f);
        renderEllipse(gfx, cx, cy, 36, 48, ((int) (58 * pulse) << 24) | 0x0028083B);
        renderEllipse(gfx, cx, cy, 24, 34, ((int) (42 * pulse) << 24) | 0x00590418);
    }

    private void renderEllipse(GuiGraphics gfx, int cx, int cy, int rx, int ry, int color) {
        for (int y = -ry; y <= ry; y++) {
            float dy = y / (float) ry;
            float row = 1.0f - dy * dy;
            if (row <= 0.0f) {
                continue;
            }
            int half = Math.max(1, Math.round(rx * (float) Math.sqrt(row)));
            gfx.fill(cx - half, cy + y, cx + half + 1, cy + y + 1, color);
        }
    }

    private void drawDiamond(GuiGraphics gfx, int cx, int cy, int r, int fill, int stroke) {
        for (int y = -r; y <= r; y++) {
            int half = r - Math.abs(y);
            gfx.fill(cx - half, cy + y, cx + half + 1, cy + y + 1, fill);
        }
        drawLine(gfx, cx, cy - r, cx + r, cy, stroke);
        drawLine(gfx, cx + r, cy, cx, cy + r, stroke);
        drawLine(gfx, cx, cy + r, cx - r, cy, stroke);
        drawLine(gfx, cx - r, cy, cx, cy - r, stroke);
    }

    private void drawLine(GuiGraphics gfx, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0);
        int sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;

        while (true) {
            gfx.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    private void drawScaledCenteredString(GuiGraphics gfx, Font font, Component text, int centerX, int y, int color, float scale) {
        gfx.pose().pushPose();
        gfx.pose().scale(scale, scale, 1.0f);
        int scaledX = (int) (centerX / scale - font.width(text) / 2.0f);
        int scaledY = (int) (y / scale);
        gfx.drawString(font, text, scaledX, scaledY, color, true);
        gfx.pose().popPose();
    }

    private void renderEquippedGourd(GuiGraphics gfx, Player player, int posX, int posY, int screenWidth, int screenHeight,
                                     int degreeNumber, int pomeProgress, float time) {
        HemoCapabilityAccess.getEquipment(player).ifPresent(scars -> {
            ItemStack gourdStack = scars.getStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX);
            if (!(gourdStack.getItem() instanceof BloodGourdItem)) {
                return;
            }

            IBloodVolume gourdVolume = HemoCapabilityAccess.getBloodVolume(gourdStack).orElse(null);
            if (gourdVolume == null) {
                return;
            }

            double maxVol = gourdVolume.getMaxBloodVolume();
            double ratio = maxVol > 0 ? Mth.clamp(gourdVolume.getBloodVolume() / maxVol, 0, 1) : 0;
            boolean curvedHorn = gourdStack.is(ItemInit.curved_horn.get());
            boolean rib = gourdStack.is(ItemInit.hemorath_rib.get());
            int iconW = GOURD_W;
            int iconH = curvedHorn ? HORN_H : rib ? RIB_H : GOURD_H;
            int centerX = posX + OVERLAY_W / 2;
            int gourdX = centerX - iconW / 2;
            int gourdY = posY + VESSEL_BOTTOM + 30;

            if (gourdY + iconH + 10 > screenHeight) {
                gourdY = posY - iconH - 8;
            }
            gourdX = Mth.clamp(gourdX, 2, screenWidth - iconW - 2);

            int textColor = getGourdTextColor(gourdStack);
            if (isOpenGourd(gourdStack)) {
                ResourceLocation halo = curvedHorn ? HORN_HALO_TEXTURE : rib ? RIB_HALO_TEXTURE : GOURD_HALO_TEXTURE;
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f,
                        0.92f + 0.08f * Mth.sin(time * 2.1f));
                gfx.blit(halo, gourdX - 6, gourdY - 6, 0, 0, 44, iconH + 12, 44, iconH + 12);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }

            if (curvedHorn) {
                renderSpecialGourd(gfx, gourdX, gourdY, ratio, HORN_H, HORN_BACK_TEXTURE,
                        HORN_FRAME_TEXTURE, HORN_MASK_TEXTURE, degreeNumber, pomeProgress, time);
            } else if (rib) {
                renderSpecialGourd(gfx, gourdX, gourdY, ratio, RIB_H, RIB_BACK_TEXTURE,
                        RIB_FRAME_TEXTURE, RIB_MASK_TEXTURE, degreeNumber, pomeProgress, time);
            } else {
                renderTwoLobedGourd(gfx, gourdX, gourdY, ratio, gourdStack,
                        degreeNumber, pomeProgress, time);
            }

            String volumeText = String.format("%.0f", gourdVolume.getBloodVolume());
            int textX = gourdX + (iconW / 2) - (mc.font.width(volumeText) / 2);
            gfx.drawString(mc.font, Component.literal(volumeText), textX, gourdY + iconH + 1,
                    textColor, true);
        });
    }

    private boolean isOpenGourd(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getBoolean(BloodGourdItem.TAG_STATE);
    }

    private int getGourdTextColor(ItemStack stack) {
        if (stack.is(ItemInit.blood_gourd_red.get())) {
            return 0xFFD24438;
        }
        if (stack.is(ItemInit.blood_gourd_black.get())) {
            return 0xFF8C6A99;
        }
        return 0xFFEEDCC6;
    }

    private void renderTwoLobedGourd(GuiGraphics gfx, int x, int y, double ratio, ItemStack stack,
                                      int degree, int pomeProgress, float time) {
        if (gourdFillTexture == null) {
            gourdFillTexture = new DynamicTexture(new NativeImage(GOURD_W, GOURD_H, false));
            gourdFillTextureId = mc.getTextureManager().register("blood_gourd_hud_fill", gourdFillTexture);
        }

        int frame = (int) (time * 60.0f);
        if (frame != lastGourdFrame || ratio != lastGourdRatio
                || degree != lastGourdDegree || pomeProgress != lastGourdPomes) {
            int[] pixels = GourdHudFillPixels.render(ratio, degree, pomeProgress, frame / 60.0f);
            uploadPixels(gourdFillTexture, pixels, GOURD_W, GOURD_H);
            lastGourdFrame = frame;
            lastGourdRatio = ratio;
            lastGourdDegree = degree;
            lastGourdPomes = pomeProgress;
        }
        ResourceLocation frameTexture = stack.is(ItemInit.blood_gourd_red.get()) ? GOURD_RED_TEXTURE
                : stack.is(ItemInit.blood_gourd_black.get()) ? GOURD_BLACK_TEXTURE : GOURD_WHITE_TEXTURE;
        // The corked path skips the halo, so restore blending for the frame itself.
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gfx.blit(GOURD_BACK_TEXTURE, x, y, 0, 0, GOURD_W, GOURD_H, GOURD_W, GOURD_H);
        gfx.blit(gourdFillTextureId, x, y, 0, 0, GOURD_W, GOURD_H, GOURD_W, GOURD_H);
        gfx.blit(frameTexture, x, y, 0, 0, GOURD_W, GOURD_H, GOURD_W, GOURD_H);
    }

    private static void uploadPixels(DynamicTexture texture, int[] pixels, int width, int height) {
        NativeImage image = texture.getPixels();
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int argb = pixels[py * width + px];
                int abgr = (argb & 0xFF00FF00) | ((argb & 0x00FF0000) >>> 16)
                        | ((argb & 0x000000FF) << 16);
                image.setPixelRGBA(px, py, abgr);
            }
        }
        texture.upload();
    }

    private void renderSpecialGourd(GuiGraphics gfx, int x, int y, double ratio, int height,
                                    ResourceLocation back, ResourceLocation frame, ResourceLocation maskId,
                                    int degree, int pomeProgress, float time) {
        if (specialFillTexture == null) {
            specialFillTexture = new DynamicTexture(new NativeImage(SpecialGourdHudFillPixels.WIDTH,
                    SpecialGourdHudFillPixels.HEIGHT, false));
            specialFillTextureId = mc.getTextureManager().register("special_gourd_hud_fill", specialFillTexture);
        }
        boolean[] mask = maskId.equals(HORN_MASK_TEXTURE) ? hornFillMask : ribFillMask;
        if (mask == null) {
            mask = loadSpecialFillMask(maskId, height);
            if (maskId.equals(HORN_MASK_TEXTURE)) {
                hornFillMask = mask;
            } else {
                ribFillMask = mask;
            }
        }
        int tickFrame = (int) (time * 60.0f);
        if (!maskId.equals(lastSpecialMaskId) || tickFrame != lastSpecialFrame || ratio != lastSpecialRatio
                || degree != lastSpecialDegree || pomeProgress != lastSpecialPomes) {
            int[] pixels = SpecialGourdHudFillPixels.render(mask, height, ratio, degree, pomeProgress, time);
            uploadPixels(specialFillTexture, pixels, SpecialGourdHudFillPixels.WIDTH, SpecialGourdHudFillPixels.HEIGHT);
            lastSpecialMaskId = maskId;
            lastSpecialFrame = tickFrame;
            lastSpecialRatio = ratio;
            lastSpecialDegree = degree;
            lastSpecialPomes = pomeProgress;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gfx.blit(back, x, y, 0, 0, HORN_W, height, HORN_W, height);
        gfx.blit(specialFillTextureId, x, y, 0, 0, HORN_W, height, HORN_W, SpecialGourdHudFillPixels.HEIGHT);
        gfx.blit(frame, x, y, 0, 0, HORN_W, height, HORN_W, height);
    }

    private boolean[] loadSpecialFillMask(ResourceLocation id, int height) {
        try (InputStream stream = mc.getResourceManager().open(id);
             NativeImage image = NativeImage.read(stream)) {
            if (image.getWidth() != HORN_W || image.getHeight() != height) {
                throw new IllegalStateException("Invalid special gourd HUD mask dimensions: " + id);
            }
            boolean[] mask = new boolean[HORN_W * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < HORN_W; x++) {
                    mask[y * HORN_W + x] = (image.getPixelRGBA(x, y) >>> 24) > 127;
                }
            }
            return mask;
        } catch (IOException error) {
            throw new IllegalStateException("Unable to load special gourd HUD mask: " + id, error);
        }
    }

    private int blendColor(int from, int to, float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        int a = (int) (alpha(from) + (alpha(to) - alpha(from)) * t);
        int r = (int) (red(from) + (red(to) - red(from)) * t);
        int g = (int) (green(from) + (green(to) - green(from)) * t);
        int b = (int) (blue(from) + (blue(to) - blue(from)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int alphaBlend(int base, int overlay) {
        float overlayA = alpha(overlay) / 255.0f;
        int a = Math.max(alpha(base), alpha(overlay));
        int r = (int) (red(base) * (1.0f - overlayA) + red(overlay) * overlayA);
        int g = (int) (green(base) * (1.0f - overlayA) + green(overlay) * overlayA);
        int b = (int) (blue(base) * (1.0f - overlayA) + blue(overlay) * overlayA);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int multiplyColor(int color, float factor) {
        int a = alpha(color);
        int r = (int) Mth.clamp(red(color) * factor, 0, 255);
        int g = (int) Mth.clamp(green(color) * factor, 0, 255);
        int b = (int) Mth.clamp(blue(color) * factor, 0, 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int alpha(int color) {
        return color >>> 24 & 0xFF;
    }

    private int red(int color) {
        return color >>> 16 & 0xFF;
    }

    private int green(int color) {
        return color >>> 8 & 0xFF;
    }

    private int blue(int color) {
        return color & 0xFF;
    }

}
