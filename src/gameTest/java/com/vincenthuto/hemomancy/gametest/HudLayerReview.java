package com.vincenthuto.hemomancy.gametest;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.CircusPerceptionOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.SanguineOmenOverlay;
import com.vincenthuto.hemomancy.client.sound.AntecedentClientEffects;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.AntecedentEffectPacket;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.function.Supplier;

/**
 * With hemomancy.hudLayerReview, draws the HUD over a fixed test pattern once per scene: each full-screen effect that
 * draws before the HUD (white humor, morphic nectar, the Circus edge, the Sanguine Omen world grade) and the
 * Antecedent scar marker that draws after it, alone and stacked. The game time, the frame's partial tick and the
 * vignette are pinned, so a before and an after run of a pristine HudLayerReview save compare pixel for pixel. Shots
 * land in the game directory's screenshots folder as {@code hud-<label>-<scene>.png}. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class HudLayerReview {
    private static final String SAVE = "HudLayerReview";
    private static final int WARM_UP_TICKS = 80;
    private static final int SETTLE_TICKS = 12;
    private static final long PINNED_GAME_TIME = 48_000L;

    /** {@code fluid} fills the player's feet and eye blocks (air when null); {@code client} sets the effect state. */
    private record Scene(String name, Supplier<BlockState> fluid, Runnable client) {}

    private static final Runnable CIRCUS = () -> CircusPerceptionOverlay.setState(200, true);
    private static final Runnable OMEN = () -> {
        SanguineOmenOverlay.instance.start(10, 0.85F, 7, SanguineOmenOverlay.Mode.WORLD_GRADE);
        for (int i = 0; i < 3; i++) SanguineOmenOverlay.instance.tick();
    };
    private static final Runnable SCAR = () -> {
        var player = Minecraft.getInstance().player;
        AntecedentClientEffects.accept(new AntecedentEffectPacket(1, player.blockPosition().offset(6, 0, 3)));
    };

    private static final List<Scene> SCENES = List.of(
            new Scene("plain", null, () -> {}),
            new Scene("white_humor", () -> BlockInit.WHITE_HUMOR_BLOCK.get().defaultBlockState(), () -> {}),
            new Scene("morphic_nectar", () -> BlockInit.MORPHIC_NECTAR_BLOCK.get().defaultBlockState(), () -> {}),
            new Scene("circus", null, CIRCUS),
            new Scene("omen_grade", null, OMEN),
            // The scar marker lasts 15 game ticks, and the game time stays pinned from here on.
            new Scene("scar", null, SCAR),
            new Scene("stacked", () -> BlockInit.MORPHIC_NECTAR_BLOCK.get().defaultBlockState(), () -> {
                CIRCUS.run();
                OMEN.run();
            }),
            new Scene("stacked_white_humor", () -> BlockInit.WHITE_HUMOR_BLOCK.get().defaultBlockState(), OMEN));

    private static int ticks;
    private static int scene = -1;
    private static int sceneTicks;
    private static int shots;
    private static BlockPos anchor;

    private HudLayerReview() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.hudLayerReview")) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        var server = mc.getSingleplayerServer();
        if (!SAVE.equals(server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().getFileName().toString()))
            throw new IllegalStateException("HUD layer review requires the disposable " + SAVE + " save");
        mc.options.pauseOnLostFocus = false;
        int tick = ticks++;
        if (tick == 0) {
            mc.options.guiScale().set(2);
            mc.resizeDisplay();
            var uuid = mc.player.getUUID();
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String cmd : new String[]{"time set 6000", "gamerule doDaylightCycle false", "weather clear",
                        "gamerule doMobSpawning false", "effect clear @s", "tick freeze"})
                    server.getCommands().performPrefixedCommand(source, cmd);
                player.setGameMode(GameType.CREATIVE);
                anchor = player.blockPosition().above(40);
                player.serverLevel().setBlockAndUpdate(anchor.below(), Blocks.STONE.defaultBlockState());
                player.teleportTo(player.serverLevel(), anchor.getX() + .5, anchor.getY(), anchor.getZ() + .5, 0, 0);
            });
        }
        if (tick < WARM_UP_TICKS || anchor == null) return;
        if (scene >= 0 && ++sceneTicks < SETTLE_TICKS) return;
        if (scene >= 0) {
            // Effects time themselves from the game time, so they start from the pinned one too.
            mc.level.setGameTime(PINNED_GAME_TIME);
            SCENES.get(scene).client.run();
            try {
                capture(SCENES.get(scene).name);
            } catch (RuntimeException e) {
                Hemomancy.LOGGER.error("HUD_LAYER_REVIEW {} failed", SCENES.get(scene).name, e);
            }
            CircusPerceptionOverlay.clear();
            SanguineOmenOverlay.instance.clear();
        }
        if (++scene == SCENES.size()) {
            Hemomancy.LOGGER.info("HUD_LAYER_REVIEW captured {}", shots);
            mc.stop();
            return;
        }
        sceneTicks = 0;
        Supplier<BlockState> fluid = SCENES.get(scene).fluid;
        var uuid = mc.player.getUUID();
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            BlockState state = fluid == null ? Blocks.AIR.defaultBlockState() : fluid.get();
            player.serverLevel().setBlockAndUpdate(anchor, state);
            player.serverLevel().setBlockAndUpdate(anchor.above(), state);
            player.teleportTo(player.serverLevel(), anchor.getX() + .5, anchor.getY(), anchor.getZ() + .5, 0, 0);
        });
    }

    private static void capture(String name) {
        var mc = Minecraft.getInstance();
        var window = mc.getWindow();
        int width = window.getGuiScaledWidth(), height = window.getGuiScaledHeight();
        var target = mc.getMainRenderTarget();
        target.bindWrite(true);
        RenderSystem.clearColor(0, 0, 0, 1);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        pinPartialTick(mc);
        float far = ClientHooks.getGuiFarPlane();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, (float) (window.getWidth() / window.getGuiScale()),
                (float) (window.getHeight() / window.getGuiScale()), 0, 1000, far), VertexSorting.ORTHOGRAPHIC_Z);
        var modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.translation(0, 0, 10000F - far);
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        var graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
        // A fixed scene for the world grade to read and the overlays to tint.
        for (int band = 0; band < 8; band++) {
            int top = height * band / 8, bottom = height * (band + 1) / 8;
            int hue = band * 0x1F;
            graphics.fillGradient(0, top, width, bottom, 0xFF000000 | hue << 16 | (0xC0 - hue) << 8 | 0x40,
                    0xFF000000 | (0xE0 - hue) << 16 | 0x30 << 8 | hue);
        }
        graphics.fill(width / 3, height / 3, width * 2 / 3, height * 2 / 3, 0xFFE8E0C8);
        graphics.flush();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        mc.gui.render(graphics, DeltaTracker.ONE);
        graphics.flush();
        modelView.popMatrix();
        RenderSystem.applyModelViewMatrix();
        String label = System.getProperty("hemomancy.hudLayerReviewLabel", "after");
        Screenshot.grab(mc.gameDirectory, "hud-" + label + "-" + name + ".png", target, message -> {});
        shots++;
        Hemomancy.LOGGER.info("HUD_LAYER_REVIEW captured {}", name);
    }

    /**
     * Effects that read the frame timer rather than the HUD's delta see the same partial tick in every run, and the
     * vignette, which eases toward the light level every tick, is drawn at full strength.
     */
    private static void pinPartialTick(Minecraft mc) {
        try {
            var residual = DeltaTracker.Timer.class.getDeclaredField("deltaTickResidual");
            residual.setAccessible(true);
            residual.setFloat(mc.getTimer(), 1.0F);
            var vignette = net.minecraft.client.gui.Gui.class.getDeclaredField("vignetteBrightness");
            vignette.setAccessible(true);
            vignette.setFloat(mc.gui, 1.0F);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot pin the frame's partial tick", e);
        }
    }
}
