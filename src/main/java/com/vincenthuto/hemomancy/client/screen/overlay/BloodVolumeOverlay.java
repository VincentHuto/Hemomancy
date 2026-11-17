package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.bloodhud.HudHeartbeat;
import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout.Rect;
import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout.Reserve;
import com.vincenthuto.hemomancy.client.screen.overlay.bloodhud.HudAssembly;
import com.vincenthuto.hemomancy.client.screen.overlay.bloodhud.LivingHudRenderer;
import com.vincenthuto.hemomancy.client.screen.overlay.bloodhud.TemperReading;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.DegreeTitles;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.TitrationBandolierItem;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeClientPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * The living hematic HUD: the body vessel, an equipped reserve grafted on its inward flank, the Morphling jar and
 * the Temper vials, all wrapped in one binding grown around whatever is showing. The degree frames, liquid fills,
 * reserve art and Apotheos helix are drawn unchanged and never flipped.
 */
public class BloodVolumeOverlay {

    static final int OVERLAY_W = BloodHudLayout.VESSEL_W;
    private static final int OVERLAY_H = BloodHudLayout.VESSEL_H;
    static final float OVERLAY_SCALE = BloodHudLayout.SCALE;
    /** The blood window's rows in the vessel texture. */
    private static final int VESSEL_TOP = 27;
    private static final int VESSEL_H = 87;
    private static final int VESSEL_BOTTOM = VESSEL_TOP + VESSEL_H;
    private static final int APOTHEOS_FILL_FRAMES = 16;
    private static final int[][] POME_POINTS = {
            {0, -20}, {-13, -14}, {13, -14}, {-15, 3}, {15, 3},
            {-17, 24}, {17, 24}, {-17, 43}, {17, 43}
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
    private static final ResourceLocation JAR_BACK_TEXTURE = Hemomancy.rloc("textures/gui/blood_hud/morphling_jar_back.png");
    private static final ResourceLocation JAR_FRONT_TEXTURE = Hemomancy.rloc("textures/gui/blood_hud/morphling_jar_front.png");

    public static BloodVolumeOverlay instance;

    private final Minecraft mc = Minecraft.getInstance();
    private final BloodHudRefreshClock refreshClock = new BloodHudRefreshClock();
    private final LivingHudRenderer living = new LivingHudRenderer();
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
        living.clear();
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

    /**
     * Whether any of the HUD can show for {@code player}: active blood, not barred by the Unstained path, and the
     * Vascularium Charm (vessel, reserve, jar) or the Titration Bandolier (Temper vials) worn.
     */
    public static boolean isHudAvailable(Player player) {
        if (player == null) return false;
        if (UnstainedAccessRules.blocksKnownBloodPowerUse(HemoCapabilityAccess.getUnstainedProgress(player))) return false;
        var bloodCap = HemoCapabilityAccess.getBloodVolume(player);
        if (bloodCap == null || !bloodCap.isActive()) return false;
        return HudVisibilityRules.showsAnything(wearsCharm(player), wearsBandolier(player));
    }

    private static boolean wearsCharm(Player player) {
        return HemoCapabilityAccess.getEquipment(player).getStackInSlot(HarbingerEquipmentMenu.CHARM_SLOT_INDEX)
                .getItem() instanceof VasculariumCharmItem;
    }

    private static boolean wearsBandolier(Player player) {
        return HemoCapabilityAccess.getEquipment(player).getStackInSlot(HarbingerEquipmentMenu.BANDOLIER_SLOT_INDEX)
                .getItem() instanceof TitrationBandolierItem;
    }

    public static BloodHudLayout layout(Player player) {
        if (!isHudAvailable(player)) return new BloodHudLayout(false, Reserve.NONE, false, false, false);
        boolean charm = HudVisibilityRules.showsCharmParts(wearsCharm(player), wearsBandolier(player));
        boolean vials = HudVisibilityRules.showsVials(wearsCharm(player), wearsBandolier(player));
        ItemStack reserve = HemoCapabilityAccess.getEquipment(player).getStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX);
        boolean shown = charm && HemoClientConfig.SHOW_GOURD_HUD.get() && reserve.getItem() instanceof BloodGourdItem
                && HemoCapabilityAccess.getBloodVolume(reserve).isPresent();
        Reserve shape = !shown ? Reserve.NONE : reserve.is(ItemInit.curved_horn.get()) ? Reserve.HORN
                : reserve.is(ItemInit.hemorath_rib.get()) ? Reserve.RIB : Reserve.GOURD;
        boolean jar = charm && EquippedMorphlingOverlay.shown(HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling());
        int corner = HemoClientConfig.HUD_LOCATION.get();
        return new BloodHudLayout(charm && HemoClientConfig.SHOW_BLOOD_VOLUME_HUD.get(), shape,
                vials && HemoClientConfig.SHOW_TEMPER_HUD.get() && TemperMarksOverlay.hasSeen(player), jar,
                corner == 1 || corner == 3);
    }

    /** Whether the HUD currently shows {@code player}'s Morphling in its jar. */
    public static boolean showsMorphlingJar(Player player) {
        return player != null && layout(player).jar();
    }

    /** The HUD's current assembly for {@code player}, or null before client setup has made the overlay. */
    @Nullable
    public static HudAssembly assembly(Player player) {
        return instance == null ? null : instance.living.assembly(layout(player));
    }

    public static BloodHudLayout.Placement placement(Player player, int width, int height) {
        int corner = HemoClientConfig.HUD_LOCATION.get();
        return layout(player).place(width, height, corner == 0 || corner == 2, corner < 2, showsEffectIcons(player),
                (int) Minecraft.getInstance().getWindow().getGuiScale());
    }

    /** Whether vanilla draws any effect icon in the top-right row; a Morphling's hidden passive does not count. */
    private static boolean showsEffectIcons(Player player) {
        return player != null && player.getActiveEffects().stream()
                .anyMatch(effect -> effect.showIcon() && IClientMobEffectExtensions.of(effect).isVisibleInGui(effect));
    }

    public void renderHUD(GuiGraphics gfx, int width, int height, float partialTicks) {
        LocalPlayer player = mc.player;
        if (!isHudAvailable(player) || mc.level == null) return;
        BloodHudLayout layout = layout(player);
        if (layout.empty()) return;
        if (refreshClock.due(player.tickCount)) PacketHandler.sendToServer(new BloodVolumeClientPacket());
        HudAssembly assembly = living.assembly(layout);
        BloodHudLayout.Placement anchor = placement(player, width, height);
        float time = HudHeartbeat.seconds(mc.level.getGameTime(), partialTicks);

        IBloodVolume bloodCap = HemoCapabilityAccess.getBloodVolume(player);
        double maxVol = bloodCap.getMaxBloodVolume();
        double ratio = maxVol > 0 ? Mth.clamp(bloodCap.getBloodVolume() / maxVol, 0, 1) : 0;
        int actualDegree = Mth.clamp(HemoCapabilityAccess.getPlayerDegreeNumber(player), 0, 8);
        EnumArchonPath archonPath = HemoCapabilityAccess.getInitiatoryDegree(player).getArchonPath();
        // The vessel's eighth-degree art belongs to the Apotheos; a Silentium keeps the seventh vessel.
        int degreeNumber = archonPath == EnumArchonPath.APOTHEOS ? actualDegree : Math.min(actualDegree, 7);
        boolean apotheos = degreeNumber >= 8;
        int pomeProgress = actualDegree >= 8 ? 9
                : Mth.clamp(HemoCapabilityAccess.getInitiatoryDegree(player).getTotalPomesConsumed(), 0, 9);
        ItemStack morphling = HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling();

        gfx.pose().pushPose();
        gfx.pose().translate(anchor.x(), anchor.y(), 0);
        gfx.pose().scale(anchor.scale(), anchor.scale(), 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        living.drawBinding(gfx, assembly, actualDegree, apotheos, time, (float) ratio);
        // The reserve is grafted behind the vessel's flank: draw its chamber first so the vessel overlaps it.
        if (layout.gourd()) renderEquippedGourd(gfx, player, layout.reserveRect().x(), layout.reserveRect().y(),
                degreeNumber, pomeProgress, time, layout);
        if (layout.blood()) renderVessel(gfx, layout.vessel(), layout, ratio, bloodCap.getBloodVolume(), actualDegree,
                archonPath, degreeNumber, pomeProgress, time);
        if (layout.jar()) renderJar(gfx, layout, morphling, time);
        if (layout.temper()) {
            TemperReading[] readings = TemperMarksOverlay.readings(player);
            // Streams and vials are a thousand-odd pixel fills: batch them into one draw instead of one each.
            gfx.drawManaged(() -> {
                if (layout.blood()) living.drawStreams(gfx, assembly, readings, ratio, time);
                living.drawVials(gfx, assembly, readings);
            });
        }
        RenderSystem.disableBlend();
        gfx.pose().popPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        if (layout.jar() && EquippedMorphlingOverlay.instance != null) {
            boolean right = layout.mirrored();
            EquippedMorphlingOverlay.instance.renderReadouts(gfx, morphling,
                    Math.round(anchor.screenX(layout.jarMeterX())), Math.round(anchor.screenY(layout.jarMeterY())), right,
                    Math.round(right ? anchor.screenX(layout.width()) : anchor.x()), Math.max(2, Math.round(anchor.y()) - 10),
                    right);
        }
    }

    private void renderJar(GuiGraphics gfx, BloodHudLayout layout, ItemStack morphling, float time) {
        Rect jar = layout.jarRect();
        Rect window = BloodHudLayout.JAR_WINDOW;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gfx.blit(JAR_BACK_TEXTURE, jar.x(), jar.y(), 0, 0, jar.w(), jar.h(), jar.w(), jar.h());
        boolean item = EquippedMorphlingOverlay.instance != null
                && EquippedMorphlingOverlay.instance.renderInJar(gfx, morphling, jar.x() + window.x() + window.w() / 2f,
                        jar.y() + window.y() + window.h() / 2f, window.w(), layout.mirrored(), time);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gfx.pose().pushPose();
        if (item) gfx.pose().translate(0, 0, EquippedMorphlingOverlay.ITEM_GLASS_Z);
        gfx.blit(JAR_FRONT_TEXTURE, jar.x(), jar.y(), 0, 0, jar.w(), jar.h(), jar.w(), jar.h());
        gfx.pose().popPose();
    }

    private void renderVessel(GuiGraphics gfx, Rect vessel, BloodHudLayout layout, double ratio, double volume,
                              int actualDegree, EnumArchonPath archonPath, int degreeNumber, int pomeProgress, float time) {
        int posX = vessel.x();
        int posY = vessel.y();
        boolean isApotheos = degreeNumber >= 8;
        if (isApotheos) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // The helix halo is authored around the older 76×126 vessel box.
            gfx.blit(APOTHEOS_HALO_TEXTURE, posX - 6, posY + 1, 0, 0, 76, 126, 76, 126);
        }
        blitOverlay(gfx, VESSEL_BACK_TEXTURE, posX, posY);
        renderLayeredBloodFill(gfx, posX, posY, ratio, degreeNumber, pomeProgress, time);
        blitOverlay(gfx, DEGREE_BASE_TEXTURES[degreeNumber], posX, posY);
        living.drawVesselBeat(gfx, DEGREE_BASE_TEXTURES[degreeNumber], degreeNumber, posX, posY, isApotheos, time, (float) ratio);
        if (degreeNumber >= 7) {
            renderPomeSockets(gfx, posX, posY, pomeProgress);
        }
        RenderSystem.disableBlend();

        int centerX = posX + OVERLAY_W / 2;
        String degreeKey = DegreeTitles.titleKey(actualDegree, archonPath).replace("hemomancy.degree.",
                "screen.hemomancy.blood_volume_overlay.degree.");
        drawScaledCenteredString(gfx, mc.font, Component.translatable(degreeKey), centerX, layout.degreeLabelY(), 0xFFD9A29A, 0.65f);
        drawScaledCenteredString(gfx, mc.font, Component.translatable("screen.hemomancy.blood_volume_overlay.ml",
                String.format("%.0f", volume)), centerX, layout.volumeLabelY(), 0xFFE05A5A, 0.8F);
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
        // gfx.fill/renderItem flush batched render types whose clear state disables blending,
        // which would render soft-alpha layers (the D8 frame glow) as opaque.
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
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
        int frame = BloodFillPixels.frame(time);
        if (frame != lastFillFrame || fillHeight != lastFillHeight || degree != lastFillDegree
                || pomeProgress != lastFillPomes) {
            VesselFillMask mask = vesselFillMask();
            int[] pixels = BloodFillPixels.render(mask.pixels(), mask.width(), mask.height(),
                    fillHeight / (double) VESSEL_H, degree, pomeProgress, frame / (float) BloodFillPixels.FILL_FPS);
            uploadPixels(bloodFillTexture, pixels, mask.width(), mask.height());
            lastFillFrame = frame;
            lastFillHeight = fillHeight;
            lastFillDegree = degree;
            lastFillPomes = pomeProgress;
        }
        blitOverlay(gfx, bloodFillTextureId, x, y);

        for (int local = fillOffset; local < fillBottom; local++) {
            int halfInner = Math.max(1, vesselFillMask().halfWidth(local));
            if (degree >= 6 && (local - VESSEL_TOP + (int) (time * 9.0f)) % 19 < 2) {
                int veinX = centerX + Math.round(Mth.sin(time * 0.9f + (local - VESSEL_TOP) * 0.18f) * (halfInner - 3));
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

            int halfInner = Math.max(2, vesselFillMask().halfWidth(localY) - 2);
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
        int rowY = y + fillOffset;
        int meniscus = BloodFillPixels.meniscusColor(degree, pomeProgress);
        int wave = Math.round(Mth.sin(time * 2.2f + (fillOffset - VESSEL_TOP) * 0.3f) * 1.25f);
        for (int localX = 0; localX < OVERLAY_W; localX++) {
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

    /** The vessel back's opaque pixels, drawn 1:1, so art and mask coordinates agree. */
    private record VesselFillMask(int width, int height, boolean[] pixels) {
        boolean contains(int x, int y) {
            return x >= 0 && y >= 0 && x < width && y < height && pixels[y * width + x];
        }

        /** Half the glass's width on this row, measured from the mask. */
        int halfWidth(int y) {
            if (y < 0 || y >= height) return 0;
            int first = -1, last = -1;
            for (int x = 0; x < width; x++) {
                if (!pixels[y * width + x]) continue;
                if (first < 0) first = x;
                last = x;
            }
            return first < 0 ? 0 : (last - first) / 2;
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

    private void renderEquippedGourd(GuiGraphics gfx, Player player, int gourdX, int gourdY,
                                     int degreeNumber, int pomeProgress, float time, BloodHudLayout layout) {
        var scars = HemoCapabilityAccess.getEquipment(player);
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
        int labelY = layout.reserveLabelY();
        drawScaledCenteredString(gfx, mc.font, Component.literal(volumeText), gourdX + iconW / 2,
                labelY, textColor, 0.8F);
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

        int frame = BloodFillPixels.frame(time);
        if (frame != lastGourdFrame || ratio != lastGourdRatio
                || degree != lastGourdDegree || pomeProgress != lastGourdPomes) {
            int[] pixels = GourdHudFillPixels.render(ratio, degree, pomeProgress, frame / (float) BloodFillPixels.FILL_FPS);
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
        int tickFrame = BloodFillPixels.frame(time);
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

    private int alphaBlend(int base, int overlay) {
        float overlayA = alpha(overlay) / 255.0f;
        int a = Math.max(alpha(base), alpha(overlay));
        int r = (int) HemoEasing.crossfade(red(base), red(overlay), overlayA);
        int g = (int) HemoEasing.crossfade(green(base), green(overlay), overlayA);
        int b = (int) HemoEasing.crossfade(blue(base), blue(overlay), overlayA);
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
