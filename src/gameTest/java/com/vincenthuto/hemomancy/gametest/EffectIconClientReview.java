package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.UUID;

/**
 * With hemomancy.effectIconReview=true, shows the effects that once shared one icon, each set beside the effect that
 * kept the old picture, in the inventory's effect list and in the HUD row. Run with effectIconReviewClient; drop
 * {"op":"create","name":"EffectIconReview","seed":12345} after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class EffectIconClientReview {
    private static final String[][] BATCHES = {
            {"hemophagy", "grave_debt", "insatiable_hunger", "transfusion_saturation", "blood_drunkenness"},
            {"neural_overload", "conductive_mark", "mnemonic_screams", "monolithic_dislocation", "impaired_recovery"},
            {"counterfeit_scar", "hemolysis", "arachnid_anastomosis", "web_mobility", "necrosis"},
            {"wither_resistance", "ferric_transfusion", "explosion_resistance", "poison_resistance", "cryoprotection"},
            {"veiled", "ender_resistance", "morphling_kinbud", "stung", "morphling_holdfast"},
            {"pinned", "morphling_ink_cap", "morphling_sealed"}};
    private static final int SETTLE = 30, PER_BATCH = 60;
    private static int ticks;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.effectIconReview")) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        var server = mc.getSingleplayerServer();
        UUID uuid = mc.player.getUUID();
        if (ticks++ < SETTLE) return;
        int batch = (ticks - SETTLE - 1) / PER_BATCH, local = (ticks - SETTLE - 1) % PER_BATCH;
        if (batch >= BATCHES.length) {
            mc.stop();
            return;
        }
        if (local == 0) {
            mc.setScreen(null);
            server.execute(() -> apply(server, uuid, BATCHES[batch]));
        }
        if (local == 20) Screenshot.grab(mc.gameDirectory, "effects-" + batch + "-hud.png", mc.getMainRenderTarget(), message -> {});
        if (local == 25) mc.setScreen(new InventoryScreen(mc.player));
        if (local == 45) Screenshot.grab(mc.gameDirectory, "effects-" + batch + "-inventory.png", mc.getMainRenderTarget(), message -> {});
        if (local == PER_BATCH - 1 && batch == BATCHES.length - 1)
            Hemomancy.LOGGER.info("EFFECT_ICON_REVIEW captured {}", BATCHES.length * 2);
    }

    private static void apply(MinecraftServer server, UUID uuid, String[] ids) {
        var player = server.getPlayerList().getPlayer(uuid);
        // Survival, so the inventory is the one with the effect list; the abilities keep the harmful ones harmless.
        player.setGameMode(GameType.SURVIVAL);
        player.getAbilities().invulnerable = true;
        player.onUpdateAbilities();
        player.removeAllEffects();
        for (String id : ids)
            BuiltInRegistries.MOB_EFFECT.getHolder(Hemomancy.rloc(id))
                    .ifPresent(effect -> player.addEffect(new MobEffectInstance(effect, 20 * 60 * 10, 0, false, false, true)));
    }
}
