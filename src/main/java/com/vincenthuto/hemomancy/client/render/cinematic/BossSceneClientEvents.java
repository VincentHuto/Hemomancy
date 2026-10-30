package com.vincenthuto.hemomancy.client.render.cinematic;

import java.util.Set;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Where the boss scene director meets the game: it ticks the scene and the skip, borrows and returns the camera type
 * for cinematic shots, turns the camera (the shot's look, or the scene's offsets and shake on the player's own view),
 * holds movement input, and hides the vanilla HUD while the scene asks. The camera's position for a shot is placed by
 * {@code MixinCamera} through {@link #shotPosition}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class BossSceneClientEvents {
	private static final BossSceneDirector DIRECTOR = BossSceneDirector.INSTANCE;
	private static final Set<ResourceLocation> HIDDEN = Set.of(VanillaGuiLayers.HOTBAR, VanillaGuiLayers.PLAYER_HEALTH,
			VanillaGuiLayers.ARMOR_LEVEL, VanillaGuiLayers.FOOD_LEVEL, VanillaGuiLayers.AIR_LEVEL,
			VanillaGuiLayers.VEHICLE_HEALTH, VanillaGuiLayers.EXPERIENCE_BAR, VanillaGuiLayers.EXPERIENCE_LEVEL,
			VanillaGuiLayers.JUMP_METER, VanillaGuiLayers.CROSSHAIR, VanillaGuiLayers.EFFECTS, VanillaGuiLayers.BOSS_OVERLAY,
			VanillaGuiLayers.SELECTED_ITEM_NAME);
	/** How close a shot may come to a wall it is pulled in by. */
	private static final double WALL_MARGIN = 0.3D;

	private BossSceneClientEvents() {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null) {
			DIRECTOR.clear();
		} else {
			DIRECTOR.holdSkip(minecraft.options.keyShift.isDown());
			DIRECTOR.tick();
		}
		if (DIRECTOR.usesShot()) {
			CameraType type = DIRECTOR.holdCamera(minecraft.options.getCameraType());
			if (minecraft.options.getCameraType() != type) minecraft.options.setCameraType(type);
		} else if (DIRECTOR.holdsCamera()) {
			minecraft.options.setCameraType(DIRECTOR.releaseCamera());
		}
	}

	/** Respawn and dimension change both clone the local player: nothing of a scene survives either. */
	@SubscribeEvent
	public static void onClone(ClientPlayerNetworkEvent.Clone event) {
		DIRECTOR.playerCloned();
	}

	@SubscribeEvent
	public static void turnCamera(ViewportEvent.ComputeCameraAngles event) {
		if (!DIRECTOR.presenting()) return;
		BossScene scene = DIRECTOR.scene();
		float partial = (float) event.getPartialTick();
		float t = DIRECTOR.time(partial);
		Vec3 shot = shotPosition(partial);
		if (shot != null) {
			float[] look = CinematicShot.lookAngles(shot, lookTarget(DIRECTOR.boss(), scene.shot().at(t), partial));
			event.setYaw(look[0]);
			event.setPitch(look[1]);
			event.setRoll(0.0F);
		}
		CameraOffsets offsets = scene.offsets(t);
		Minecraft minecraft = Minecraft.getInstance();
		float time = (minecraft.player == null ? 0 : minecraft.player.tickCount) + partial;
		// two detuned waves, so the shake never reads as a metronome
		float shake = HemoClientConfig.bossSceneShake() ? offsets.shake() : 0.0F;
		float shakeRoll = shake * (Mth.sin(time * 1.31F) * 0.6F + Mth.sin(time * 2.17F) * 0.4F);
		float shakePitch = shake * (Mth.cos(time * 1.53F) * 0.6F + Mth.sin(time * 2.71F + 1.0F) * 0.4F);
		if (shot != null) {
			// a shot keeps its framing: only the shake rides on it
			event.setRoll(shakeRoll * 0.5F);
			event.setPitch(event.getPitch() + shakePitch * 0.45F);
			return;
		}
		event.setRoll(event.getRoll() + offsets.roll() + shakeRoll * 0.5F);
		event.setPitch(event.getPitch() + offsets.pitch() + shakePitch * 0.45F);
		event.setYaw(event.getYaw() + offsets.yaw());
	}

	/** The camera's position for this frame's shot, or null when the player keeps their own view. */
	@Nullable
	public static Vec3 shotPosition(float partial) {
		if (!DIRECTOR.usesShot()) return null;
		Entity boss = DIRECTOR.boss();
		CinematicShot.Key key = DIRECTOR.scene().shot().at(DIRECTOR.time(partial));
		Vec3 target = lookTarget(boss, key, partial);
		Vec3 wanted = CinematicShot.position(key, boss.getPosition(partial), DIRECTOR.anchorYaw());
		// pull the camera in front of any wall between it and the boss, as vanilla's third person does; collision
		// shapes too, since some blocks draw solid with no visual shape (the Gardens' growths)
		Vec3 near = wanted;
		for (ClipContext.Block shape : new ClipContext.Block[] { ClipContext.Block.VISUAL, ClipContext.Block.COLLIDER }) {
			BlockHitResult hit = boss.level().clip(new ClipContext(target, near, shape, ClipContext.Fluid.NONE, boss));
			if (hit.getType() != HitResult.Type.MISS) near = hit.getLocation();
		}
		if (near == wanted) return wanted;
		Vec3 back = target.subtract(wanted).normalize().scale(WALL_MARGIN);
		return near.add(back);
	}

	private static Vec3 lookTarget(Entity boss, CinematicShot.Key key, float partial) {
		return boss.getPosition(partial).add(0.0D, key.lookHeight(), 0.0D);
	}

	@SubscribeEvent
	public static void holdInput(MovementInputUpdateEvent event) {
		if (!DIRECTOR.inputLocked(0.0F)) return;
		Input input = event.getInput();
		input.forwardImpulse = 0.0F;
		input.leftImpulse = 0.0F;
		input.up = input.down = input.left = input.right = false;
		input.jumping = false;
		input.shiftKeyDown = false;
	}

	@SubscribeEvent
	public static void hideHud(RenderGuiLayerEvent.Pre event) {
		if (!HIDDEN.contains(event.getName())) return;
		// boss bars wait for the letterbox to leave, so a bar never unfurls under the black band
		boolean underLetterbox = event.getName().equals(VanillaGuiLayers.BOSS_OVERLAY) && DIRECTOR.presenting()
				&& DIRECTOR.scene().letterbox(DIRECTOR.time(0.0F)) > 0.02F;
		if (underLetterbox || DIRECTOR.hudHidden(0.0F)) event.setCanceled(true);
	}
}
