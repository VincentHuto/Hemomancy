package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.LidlessPassageClientState;
import com.vincenthuto.hemomancy.client.screen.overlay.LidlessPassageView;
import com.vincenthuto.hemomancy.client.sound.LidlessPassageSound;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.lidless.LidlessPassageInputPacket;
import com.vincenthuto.hemomancy.common.util.HeldTraveller;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import javax.annotation.Nullable;

/**
 * Holds the local player while they cross a Lidless Door: first person, no hands, no HUD but chat, keys turned into
 * the input payload and nothing else, and every crossing player hidden.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LidlessPassageClientEvents {
	private static final ResourceLocation SCREEN_EFFECTS = Hemomancy.rloc("screen_effects");
	@Nullable
	private static CameraType previousCamera;
	@Nullable
	private static LidlessPassageSound drone;

	private LidlessPassageClientEvents() {
	}

	public static boolean active() {
		return LidlessPassageClientState.local() != null;
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (active()) {
			if (previousCamera == null) previousCamera = mc.options.getCameraType();
			mc.options.setCameraType(CameraType.FIRST_PERSON);
			HeldTraveller.hold(mc.player);
			if (drone == null || !mc.getSoundManager().isActive(drone)) {
				drone = new LidlessPassageSound();
				mc.getSoundManager().play(drone);
			}
		} else if (previousCamera != null) {
			mc.options.setCameraType(previousCamera);
			previousCamera = null;
			drone = null;
			LidlessPassageView.clear();
			if (mc.player != null) {
				mc.player.noPhysics = mc.player.isSpectator();
			}
		}
	}

	@SubscribeEvent
	public static void movement(MovementInputUpdateEvent event) {
		var view = LidlessPassageClientState.local();
		if (view == null) return;
		Minecraft mc = Minecraft.getInstance();
		int direction = mc.screen == null ? (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0) : 0;
		PacketHandler.sendToServer(new LidlessPassageInputPacket(view.token(), direction,
				mc.screen == null && mc.options.keySprint.isDown(), mc.screen == null && mc.options.keyShift.isDown()));
		Input input = event.getInput();
		input.forwardImpulse = input.leftImpulse = 0;
		input.up = input.down = input.left = input.right = input.jumping = input.shiftKeyDown = false;
	}

	@SubscribeEvent
	public static void interaction(InputEvent.InteractionKeyMappingTriggered event) {
		if (active()) {
			event.setCanceled(true);
			event.setSwingHand(false);
		}
	}

	@SubscribeEvent
	public static void hands(RenderHandEvent event) {
		if (active()) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void blockOverlay(RenderBlockScreenEffectEvent event) {
		if (active()) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void player(RenderPlayerEvent.Pre event) {
		if (LidlessPassageClientState.isCrossing(event.getEntity())) event.setCanceled(true);
	}

	/** The void is bare: every HUD layer but chat and the full-screen effects (which draw the void) waits. */
	@SubscribeEvent
	public static void hud(RenderGuiLayerEvent.Pre event) {
		if (!active()) return;
		ResourceLocation name = event.getName();
		if (name.equals(VanillaGuiLayers.CHAT) || name.equals(SCREEN_EFFECTS)) return;
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void leave(EntityLeaveLevelEvent event) {
		if (event.getLevel().isClientSide() && event.getEntity() instanceof Player player)
			LidlessPassageClientState.forget(player.getId());
	}
}
