package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.AxonalTravelView;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.axonal.AxonalInputPacket;
import com.vincenthuto.hemomancy.common.network.axonal.AxonalStatePacket;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class AxonalTransductionClient {
    private static ClientLevel trackedLevel;
    private static CameraType previousCamera;
    private static long token;
    private static final String GRAVITY = "hemomancy_axonal_client_gravity";
    private AxonalTransductionClient() { }
    public static boolean active() {
        var player = Minecraft.getInstance().player;
        return player != null && AxonalTransductionManager.isTraveling(player);
    }
    public static void update(AxonalStatePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().location().equals(packet.dimension())
                || !(mc.level.getEntity(packet.playerId()) instanceof Player player)) return;
        if (packet.active() && !AxonalTransductionManager.isTraveling(player))
            player.getPersistentData().putBoolean(GRAVITY, player.isNoGravity());
        player.getPersistentData().putBoolean(AxonalTransductionManager.ACTIVE, packet.active());
        if (player == mc.player) {
            token = packet.token();
            player.setPos(packet.position());
        }
        if (packet.active()) AxonalTransductionManager.applyMovement(player);
        else restore(player);
    }
    private static void restore(Player player) {
        if (!AxonalTransductionManager.isTraveling(player) && !player.getPersistentData().contains(GRAVITY)) return;
        player.getPersistentData().remove(AxonalTransductionManager.ACTIVE);
        player.noPhysics = player.isSpectator();
        if (player.getPersistentData().contains(GRAVITY)) {
            player.setNoGravity(player.getPersistentData().getBoolean(GRAVITY));
            player.getPersistentData().remove(GRAVITY);
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (trackedLevel != mc.level) {
            if (trackedLevel != null) for (Player player : trackedLevel.players()) restore(player);
            trackedLevel = mc.level;
            AxonalTravelView.clear();
        }
        if (active()) {
            if (previousCamera == null) previousCamera = mc.options.getCameraType();
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            AxonalTransductionManager.applyMovement(mc.player);
        } else if (previousCamera != null) {
            mc.options.setCameraType(previousCamera);
            previousCamera = null;
            AxonalTravelView.clear();
        }
    }
    @SubscribeEvent public static void movement(MovementInputUpdateEvent event) {
        if (!active()) return;
        Minecraft mc = Minecraft.getInstance();
        int direction = mc.screen == null ? (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0) : 0;
        PacketHandler.sendToServer(new AxonalInputPacket(token, direction, mc.options.keyShift.isDown()));
        var input = event.getInput();
        input.forwardImpulse = input.leftImpulse = 0;
        input.up = input.down = input.left = input.right = input.jumping = input.shiftKeyDown = false;
    }
    @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered event) {
        if (active()) { event.setCanceled(true); event.setSwingHand(false); }
    }
    @SubscribeEvent public static void hands(RenderHandEvent event) { if (active()) event.setCanceled(true); }
    @SubscribeEvent public static void player(RenderPlayerEvent.Pre event) {
        if (AxonalTransductionManager.isTraveling(event.getEntity())) event.setCanceled(true);
    }
    @SubscribeEvent public static void blockOverlay(RenderBlockScreenEffectEvent event) {
        if (active()) event.setCanceled(true);
    }
    @SubscribeEvent public static void world(RenderLevelStageEvent event) { AxonalTravelView.renderWorld(event); }
}
