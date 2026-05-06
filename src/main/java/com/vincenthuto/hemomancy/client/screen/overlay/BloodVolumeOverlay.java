package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeClientPacket;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

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
            {0, -13}, {-15, -7}, {15, -7}, {-27, 5}, {27, 5},
            {-31, 23}, {31, 23}, {-20, 40}, {20, 40}
    };
    private static final int GOURD_W = 18;
    private static final int GOURD_H = 26;
    private static final int HORN_W = 24;

    private static final ResourceLocation VESSEL_BACK_TEXTURE = texture("vessel_back");
    private static final ResourceLocation APOTHEOS_HALO_TEXTURE = texture("halo_apotheos");
    private static final ResourceLocation[] DEGREE_BASE_TEXTURES = textureRange("base_degree_", 9);
    private static final ResourceLocation[] POME_TEXTURES = textureRange("pomes_", 10);
    private static final ResourceLocation[] APOTHEOS_FILL_TEXTURES = textureRange("fill_apotheos_", APOTHEOS_FILL_FRAMES);
    private static final int HORN_H = 18;

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
    private float animTime = 0f;

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

    public void renderHUD(GuiGraphics gfx, int width, int height, float partialTicks) {
        LocalPlayer player = this.mc.player;
        if (player == null) return;

        HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(cap -> {
            if (cap.hasBegunPurification()) return;

            HemoCapabilityAccess.getBloodVolume(player).ifPresent(bloodCap -> {
                if (bloodCap == null || !bloodCap.isActive()) return;
                HemoCapabilityAccess.getScars(player).ifPresent(inv -> {
                    if (inv.getStackInSlot(5).getItem() instanceof VasculariumCharmItem) {
                        PacketHandler.sendToServer(new BloodVolumeClientPacket());

                        int posX = getConfiguredBarX(width);
                        int posY = getConfiguredBarY(player, height);
                        renderBloodBar(gfx, posX, posY, bloodCap, player, mc.level, partialTicks, width, height);
                    }
                });
            });
        });
    }

    private void renderBloodBar(GuiGraphics gfx, int posX, int posY, IBloodVolume bloodCap,
                                Player player, ClientLevel world, float partialTicks, int screenWidth, int screenHeight) {
        gfx.pose().pushPose();
        gfx.pose().translate(posX, posY, 0);
        gfx.pose().scale(OVERLAY_SCALE, OVERLAY_SCALE, 1.0f);
        renderBloodBarScaled(gfx, 0, 0, bloodCap, player, world, partialTicks,
                Math.round(screenWidth / OVERLAY_SCALE), Math.round(screenHeight / OVERLAY_SCALE));
        gfx.pose().popPose();
    }

    private void renderBloodBarScaled(GuiGraphics gfx, int posX, int posY, IBloodVolume bloodCap,
                                      Player player, ClientLevel world, float partialTicks, int screenWidth, int screenHeight) {
        Font fr = mc.font;
        animTime += 0.016f;
        float time = animTime;

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
        renderEquippedGourd(gfx, player, posX, posY, screenWidth, screenHeight, time);

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
            renderApotheosBloodActivity(gfx, x, y, fillOffset, fillHeight, time);
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

        for (int local = fillOffset; local < fillBottom; local++) {
            int vesselLocalY = Mth.clamp(local - VESSEL_TOP, 0, VESSEL_H - 1);
            int halfInner = Math.max(1, vesselHalfWidth(vesselLocalY, false));
            float depth = Mth.clamp((local - fillOffset) / (float) Math.max(fillHeight, 1), 0.0f, 1.0f);
            float pulse = 0.82f + 0.18f * Mth.sin(time * 1.5f + vesselLocalY * 0.11f);
            float fade = 1.0f - corruption * 0.70f;
            int color = multiplyColor(blendColor(0xEED32327, 0xEE520507, depth), pulse * fade);
            if (corruption > 0.0f) {
                color = alphaBlend(color, ((int) (118 * corruption) << 24) | 0x00030106);
            }

            gfx.fill(centerX - halfInner, y + local, centerX + halfInner + 1, y + local + 1, color);

            if (degree >= 6 && (vesselLocalY + (int) (time * 9.0f)) % 19 < 2) {
                int veinX = centerX + Math.round(Mth.sin(time * 0.9f + vesselLocalY * 0.18f) * (halfInner - 3));
                gfx.fill(veinX, y + local, veinX + 2, y + local + 1,
                        alphaBlend(0x6635080A, ((int) (90 * corruption) << 24) | 0x00010002));
            }
        }

        renderBloodBubbles(gfx, x, y, fillOffset, fillHeight, time, corruption);
    }

    private void renderApotheosBloodActivity(GuiGraphics gfx, int x, int y, int fillOffset, int fillHeight, float time) {
        int centerX = x + OVERLAY_W / 2;
        int fillBottom = fillOffset + fillHeight;
        for (int i = 0; i < 3; i++) {
            float phase = time * (0.55f + i * 0.13f) + i * 0.31f;
            int local = fillOffset + Math.floorMod((int) (phase * VESSEL_H), Math.max(fillHeight, 1));
            if (local < fillOffset || local >= fillBottom) {
                continue;
            }
            int vesselLocalY = Mth.clamp(local - VESSEL_TOP, 0, VESSEL_H - 1);
            int halfInner = Math.max(3, vesselHalfWidth(vesselLocalY, false) - 2);
            int drift = Math.round(Mth.sin(time * 1.1f + i * 2.4f) * (halfInner - 2));
            gfx.fill(centerX + drift - 1, y + local, centerX + drift + 2, y + local + 2, 0x70420612);
        }
        renderBloodBubbles(gfx, x, y, fillOffset, fillHeight, time, 1.0f);
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
            gfx.fill(bubbleX, y + localY, bubbleX + 1, y + localY + 1, color);
            if (i % 3 == 0) {
                gfx.fill(bubbleX + 1, y + localY + 1, bubbleX + 2, y + localY + 2, color & 0x88FFFFFF);
            }
        }
    }

    private void renderFillMeniscus(GuiGraphics gfx, int x, int y, int fillOffset, int degree, int pomeProgress, float time) {
        int localY = Mth.clamp(fillOffset - VESSEL_TOP, 0, VESSEL_H - 1);
        int halfInner = Math.max(1, vesselHalfWidth(localY, false));
        int centerX = x + OVERLAY_W / 2;
        int rowY = y + fillOffset;
        int corruptionAlpha = degree >= 7 ? (int) (120 * (pomeProgress / 9.0f)) : 0;
        int meniscus = degree >= 8 ? 0xAA560710 : alphaBlend(0xBBDD2F2F, (corruptionAlpha << 24) | 0x00030106);
        int wave = Math.round(Mth.sin(time * 2.2f + localY * 0.3f) * 1.25f);
        gfx.fill(centerX - halfInner, rowY + wave, centerX + halfInner + 1, rowY + wave + 1, meniscus);
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
        float t = py / (float) Math.max(VESSEL_H - 1, 1);
        float width;
        if (t < 0.12f) {
            width = Mth.lerp(t / 0.12f, 10.0f, 16.0f);
        } else if (t < 0.86f) {
            width = 16.0f - Mth.sin((t - 0.12f) / 0.74f * Mth.PI) * 2.0f;
        } else {
            width = Mth.lerp((t - 0.86f) / 0.14f, 14.0f, 4.0f);
        }
        return Math.max(1, Math.round(width) - (outer ? 0 : 4));
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
                                     float time) {
        HemoCapabilityAccess.getScars(player).ifPresent(scars -> {
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
            int iconW = curvedHorn ? HORN_W : GOURD_W;
            int iconH = curvedHorn ? HORN_H : GOURD_H;
            int centerX = posX + OVERLAY_W / 2;
            int gourdX = centerX - iconW / 2;
            int gourdY = posY + VESSEL_BOTTOM + 6;

            if (gourdY + iconH + 10 > screenHeight) {
                gourdY = Math.max(2, posY - iconH - 8);
            }
            gourdX = Mth.clamp(gourdX, 2, screenWidth - iconW - 2);

            GourdPalette palette = getGourdPalette(gourdStack);
            if (isOpenGourd(gourdStack)) {
                renderGourdGlow(gfx, gourdX, gourdY, iconW, iconH, curvedHorn, time);
            }

            if (curvedHorn) {
                renderCurvedHorn(gfx, gourdX, gourdY + 3, ratio, time);
            } else if (rib) {
                renderHemorathRib(gfx, gourdX, gourdY + 3, ratio, time);
            } else {
                renderTwoLobedGourd(gfx, gourdX, gourdY, ratio, palette, time);
            }

            String volumeText = String.format("%.0f", gourdVolume.getBloodVolume());
            int textX = gourdX + (iconW / 2) - (mc.font.width(volumeText) / 2);
            gfx.drawString(mc.font, Component.literal(volumeText), textX, gourdY + iconH + 1,
                    palette.textColor(), true);
        });
    }

    private boolean isOpenGourd(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getBoolean(BloodGourdItem.TAG_STATE);
    }

    private void renderGourdGlow(GuiGraphics gfx, int x, int y, int iconW, int iconH, boolean curvedHorn, float time) {
        float pulse = 0.72f + 0.28f * Mth.sin(time * 2.1f);
        float cx = x + iconW * (curvedHorn ? 0.55f : 0.50f);
        float cy = y + iconH * (curvedHorn ? 0.58f : 0.54f);
        float rx = iconW * (curvedHorn ? 0.68f : 0.64f);
        float ry = iconH * (curvedHorn ? 0.72f : 0.58f);

        for (int gy = -4; gy < iconH + 5; gy++) {
            for (int gx = -5; gx < iconW + 6; gx++) {
                float dx = (x + gx - cx) / rx;
                float dy = (y + gy - cy) / ry;
                float distance = dx * dx + dy * dy;
                if (distance > 1.0f) {
                    continue;
                }

                float edge = 1.0f - distance;
                int alpha = (int) (Mth.clamp(edge * edge * pulse, 0.0f, 1.0f) * (curvedHorn ? 105 : 125));
                if (alpha <= 0) {
                    continue;
                }

                int red = curvedHorn ? 0xB8 : 0xD4;
                int green = curvedHorn ? 0x1A : 0x22;
                int blue = curvedHorn ? 0x12 : 0x16;
                gfx.fill(x + gx, y + gy, x + gx + 1, y + gy + 1, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }
    }

    private GourdPalette getGourdPalette(ItemStack stack) {
        if (stack.is(ItemInit.blood_gourd_red.get())) {
            return new GourdPalette(0xFF3A0606, 0xFF6A1010, 0xFF9A2220, 0xFFD24438);
        }
        if (stack.is(ItemInit.blood_gourd_black.get())) {
            return new GourdPalette(0xFF050307, 0xFF18111E, 0xFF3A2B40, 0xFF8C6A99);
        }
        return new GourdPalette(0xFF3A2A22, 0xFFC9B7A2, 0xFFFFF5E2, 0xFFEEDCC6);
    }

    private void renderTwoLobedGourd(GuiGraphics gfx, int x, int y, double ratio, GourdPalette palette, float time) {
        int fillLimit = y + GOURD_H - (int) Math.round(GOURD_H * ratio);
        for (int py = 0; py < GOURD_H; py++) {
            for (int px = 0; px < GOURD_W; px++) {
                float upperLobe = ellipseDistance(px, py, 9.5f, 7.0f, 6.2f, 6.6f);
                float lowerLobe = ellipseDistance(px, py, 8.0f, 17.0f, 8.0f, 8.8f);
                float waist = ellipseDistance(px, py, 8.8f, 11.5f, 4.8f, 4.4f);
                float shape = Math.min(Math.min(upperLobe, lowerLobe), waist);
                if (shape > 1.0f) {
                    continue;
                }

                int screenY = y + py;
                boolean border = shape > 0.80f;
                if (border) {
                    gfx.fill(x + px, screenY, x + px + 1, screenY + 1, palette.borderColor());
                    continue;
                }

                float vertical = py / (float) GOURD_H;
                float shine = Mth.clamp(1.0f - ellipseDistance(px, py, 7.0f, 7.0f, 3.0f, 3.5f), 0.0f, 1.0f);
                int color = blendColor(palette.shellLowColor(), palette.shellHighColor(), 1.0f - vertical);
                color = alphaBlend(color, 0x55000000);

                float wave = 1.2f * Mth.sin(time * 2.0f + px * 0.7f);
                int liquidLine = fillLimit + Math.round(wave);
                boolean filled = screenY >= liquidLine;
                if (filled) {
                    float liquidDepth = Mth.clamp((screenY - liquidLine) / (float) Math.max(GOURD_H, 1), 0.0f, 1.0f);
                    float pulse = 0.78f + 0.22f * Mth.sin(time * 1.8f + py * 0.16f);
                    color = blendColor(0xF0C82022, 0xEE520608, liquidDepth);
                    color = multiplyColor(color, pulse);

                    if (Math.abs(screenY - liquidLine) <= 1) {
                        color = alphaBlend(color, 0x99FF5548);
                    }
                    if ((px + py + (int) (time * 12)) % 17 == 0) {
                        color = alphaBlend(color, 0x66FF7A68);
                    }
                }
                if (shine > 0.0f) {
                    int alpha = filled ? (int) (70 * shine) : (int) (38 * shine);
                    color = alphaBlend(color, (alpha << 24) | 0x00FFFFFF);
                }
                gfx.fill(x + px, screenY, x + px + 1, screenY + 1, color);
            }
        }

        int neckPulse = 130 + (int) (35 * Mth.sin(time * 1.6f));
        int neckColor = (neckPulse << 24) | 0x3B261A;
        gfx.fill(x + 8, y - 2, x + 13, y + 1, neckColor);
        gfx.fill(x + 9, y - 4, x + 12, y - 2, neckColor);
        gfx.fill(x + 6, y + 10, x + 12, y + 12, alphaBlend(palette.borderColor(), 0x44000000));
    }

    private void renderHemorathRib(GuiGraphics gfx, int x, int y, double ratio, float time) {
        float fillLimit = y + HORN_H - (float) (HORN_H * ratio);
        for (int py = 0; py < HORN_H; py++) {
            for (int px = 0; px < HORN_W; px++) {
                HornSample sample = sampleHorn(px, py);
                if (!sample.inside()) {
                    continue;
                }

                int screenY = y + py;
                int color;
                if (sample.border()) {
                    color = 0xFF757474;
                } else {
                    float t = Mth.clamp(sample.pathT(), 0.0f, 1.0f);
                    color = blendColor(0xFFe6e1dc, 0xFFd4bda7, t);
                    if (((int) (t * 18.0f) + py) % 4 == 0) {
                        color = alphaBlend(color, 0x55990606);
                    }

                    float wave = 0.9f * Mth.sin(time * 2.0f + px * 0.55f);
                    if (screenY >= fillLimit + wave) {
                        float liquidDepth = Mth.clamp((screenY - fillLimit) / (float) Math.max(HORN_H, 1), 0.0f, 1.0f);
                        float pulse = 0.76f + 0.24f * Mth.sin(time * 1.7f + sample.pathT() * 5.0f);
                        color = multiplyColor(blendColor(0xF0C82022, 0xEE4C0507, liquidDepth), pulse);
                        if (Math.abs(screenY - (fillLimit + wave)) <= 1.0f) {
                            color = alphaBlend(color, 0x99FF5548);
                        }
                    }

                    float shine = Mth.clamp(1.0f - ellipseDistance(px, py, 9.0f, 5.0f, 6.0f, 3.2f), 0.0f, 1.0f);
                    if (shine > 0.0f) {
                        color = alphaBlend(color, ((int) (45 * shine) << 24) | 0x00FFFFFF);
                    }
                }
                gfx.fill(x + px, screenY, x + px + 1, screenY + 1, color);
            }
        }

        gfx.fill(x + 19, y + 3, x + 23, y + 7, 0xFF4A130E);
        gfx.fill(x + 20, y + 4, x + 23, y + 6, 0xFF8C2A20);
        gfx.fill(x + 18, y + 7, x + 21, y + 10, 0xFF2E1B14);
    }

    private void renderCurvedHorn(GuiGraphics gfx, int x, int y, double ratio, float time) {
        float fillLimit = y + HORN_H - (float) (HORN_H * ratio);
        for (int py = 0; py < HORN_H; py++) {
            for (int px = 0; px < HORN_W; px++) {
                HornSample sample = sampleHorn(px, py);
                if (!sample.inside()) {
                    continue;
                }
                int screenY = y + py;
                int color;
                if (sample.border()) {
                    color = 0xFF3B261A;
                } else {
                    float t = Mth.clamp(sample.pathT(), 0.0f, 1.0f);
                    color = blendColor(0xFFEEDCC6, 0xFF8A6B4D, t);
                    if (((int) (t * 18.0f) + py) % 4 == 0) {
                        color = alphaBlend(color, 0x553B261A);
                    }

                    float wave = 0.9f * Mth.sin(time * 2.0f + px * 0.55f);
                    if (screenY >= fillLimit + wave) {
                        float liquidDepth = Mth.clamp((screenY - fillLimit) / (float) Math.max(HORN_H, 1), 0.0f, 1.0f);
                        float pulse = 0.76f + 0.24f * Mth.sin(time * 1.7f + sample.pathT() * 5.0f);
                        color = multiplyColor(blendColor(0xF0C82022, 0xEE4C0507, liquidDepth), pulse);
                        if (Math.abs(screenY - (fillLimit + wave)) <= 1.0f) {
                            color = alphaBlend(color, 0x99FF5548);
                        }
                    }

                    float shine = Mth.clamp(1.0f - ellipseDistance(px, py, 9.0f, 5.0f, 6.0f, 3.2f), 0.0f, 1.0f);
                    if (shine > 0.0f) {
                        color = alphaBlend(color, ((int) (45 * shine) << 24) | 0x00FFFFFF);
                    }
                }
                gfx.fill(x + px, screenY, x + px + 1, screenY + 1, color);
            }
        }

        gfx.fill(x + 19, y + 3, x + 23, y + 7, 0xFF4A130E);
        gfx.fill(x + 20, y + 4, x + 23, y + 6, 0xFF8C2A20);
        gfx.fill(x + 18, y + 7, x + 21, y + 10, 0xFF2E1B14);
    }

    private HornSample sampleHorn(int px, int py) {
        float[][] points = {
                {21.0f, 5.0f},
                {17.5f, 3.2f},
                {12.8f, 3.8f},
                {8.4f, 6.4f},
                {5.8f, 10.0f},
                {7.0f, 14.0f},
                {10.8f, 15.2f},
                {14.3f, 13.0f}
        };

        float bestDistance = Float.MAX_VALUE;
        float bestT = 0.0f;
        for (int i = 0; i < points.length - 1; i++) {
            float ax = points[i][0];
            float ay = points[i][1];
            float bx = points[i + 1][0];
            float by = points[i + 1][1];
            float vx = bx - ax;
            float vy = by - ay;
            float lengthSq = vx * vx + vy * vy;
            float localT = lengthSq > 0 ? Mth.clamp(((px - ax) * vx + (py - ay) * vy) / lengthSq, 0.0f, 1.0f) : 0.0f;
            float cx = ax + vx * localT;
            float cy = ay + vy * localT;
            float dx = px - cx;
            float dy = py - cy;
            float distance = Mth.sqrt(dx * dx + dy * dy);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestT = (i + localT) / (points.length - 1);
            }
        }

        float thickness = 3.35f - bestT * 1.45f;
        boolean inside = bestDistance <= thickness;
        boolean border = inside && bestDistance >= thickness - 0.85f;
        return new HornSample(inside, border, bestT);
    }

    private float ellipseDistance(float px, float py, float cx, float cy, float rx, float ry) {
        float dx = (px - cx) / rx;
        float dy = (py - cy) / ry;
        return dx * dx + dy * dy;
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

    private record GourdPalette(int borderColor, int shellLowColor, int shellHighColor, int textColor) {
    }

    private record HornSample(boolean inside, boolean border, float pathT) {
    }
}
