package com.vincenthuto.hemomancy.client.render.cinematic;

import java.util.function.Supplier;
import java.util.function.IntFunction;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Plays one boss scene at a time on this client. It keeps the scene's clock, the skip (sneak held for
 * {@link #SKIP_TICKS} ticks drops the presentation, but input stays held while the scene says the server still binds
 * the player), and the camera type a cinematic shot borrowed, so it goes back however the scene ends. The client
 * events ({@code BossSceneClientEvents}) and the screen layer ({@code BossSceneOverlay}) only read it.
 */
public final class BossSceneDirector {
	public static final int SKIP_TICKS = 20;
	/** A boss further than this is out of frame: the shot gives the player back their own view. */
	public static final double SHOT_RANGE = 96.0D;

	public static final BossSceneDirector INSTANCE = new BossSceneDirector(id -> {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.level == null ? null : minecraft.level.getEntity(id);
	});

	private final IntFunction<Entity> resolver;
	@Nullable
	private BossScene scene;
	private int entityId;
	private int elapsed = -1;
	private int skipHeld;
	private boolean skipped;
	@Nullable
	private CameraType heldCamera;
	@Nullable
	private Float anchorYaw;

	BossSceneDirector(IntFunction<Entity> resolver) {
		this.resolver = resolver;
	}

	public void start(ResourceLocation id, int entityId, int offset) {
		Supplier<BossScene> factory = BossSceneRegistry.get(id);
		if (factory == null) {
			Hemomancy.LOGGER.warn("No boss scene registered for {}", id);
			return;
		}
		end();
		this.entityId = entityId;
		this.scene = factory.get();
		this.anchorYaw = null;
		this.elapsed = Math.max(0, offset);
		this.skipHeld = 0;
		this.skipped = false;
	}

	public void tick() {
		if (!active()) return;
		boss();
		scene.tick(elapsed);
		if (++elapsed > scene.length()) end();
	}

	/** Called every client tick with whether sneak is held. */
	public void holdSkip(boolean held) {
		if (!presenting() || !scene.skippable(elapsed)) {
			skipHeld = 0;
			return;
		}
		skipHeld = held ? skipHeld + 1 : 0;
		if (skipHeld >= SKIP_TICKS && !skipped) {
			skipped = true;
			scene.skipped();
		}
	}

	public void clear() {
		end();
	}

	/** The local player was cloned (respawn or dimension change): only a scene made to cross one survives it. */
	public void playerCloned() {
		if (active() && !scene.keepsAcrossDimensions()) end();
	}

	private void end() {
		if (scene != null) scene.end();
		scene = null;
		elapsed = -1;
		skipHeld = 0;
		skipped = false;
	}

	public boolean active() {
		return scene != null && elapsed >= 0;
	}

	/** Active and not skipped: the letterbox, card, HUD hiding and camera apply. */
	public boolean presenting() {
		return active() && !skipped;
	}

	@Nullable
	public BossScene scene() {
		return scene;
	}

	public float time(float partial) {
		return elapsed + partial;
	}

	/** 0..1 while sneak is held toward a skip. */
	public float skipProgress() {
		return skipHeld / (float) SKIP_TICKS;
	}

	public boolean inputLocked(float partial) {
		if (!active()) return false;
		float t = time(partial);
		return skipped ? scene.keepsInputAfterSkip(t) : scene.inputLocked(t);
	}

	public boolean hudHidden(float partial) {
		return presenting() && scene.hudHidden(time(partial));
	}

	/** The boss the scene frames, while the client still has it. */
	@Nullable
	public Entity boss() {
		if (!active()) return null;
		Entity entity = resolver.apply(entityId);
		if (entity == null || entity.isRemoved()) return null;
		if (anchorYaw == null) anchorYaw = anchorFor(entity);
		return entity;
	}

	private float anchorFor(Entity boss) {
		CinematicShot shot = scene.shot();
		Entity viewer = Minecraft.getInstance().getCameraEntity();
		if (shot == null || !shot.fromViewer() || viewer == null) return boss.getYRot();
		return CinematicShot.yawToward(boss.position(), viewer.position());
	}

	/** The boss's facing when the client first had it: shots are laid out from it, so a boss turning mid-scene doesn't swing the camera. */
	public float anchorYaw() {
		return anchorYaw == null ? 0.0F : anchorYaw;
	}

	/** Whether the camera follows the scene's shot this frame. */
	public boolean usesShot() {
		if (!presenting() || scene.shot() == null || !scene.shotActive(elapsed)) return false;
		if (!HemoClientConfig.cinematicBossScenes()) return false;
		Entity boss = boss();
		if (boss == null) return false;
		Entity viewer = Minecraft.getInstance().getCameraEntity();
		return viewer == null || viewer.distanceToSqr(boss) <= SHOT_RANGE * SHOT_RANGE;
	}

	/** Borrows the camera for a shot: remembers the player's camera type once and returns the type to use. */
	public CameraType holdCamera(CameraType current) {
		if (heldCamera == null) heldCamera = current;
		return CameraType.THIRD_PERSON_BACK;
	}

	public boolean holdsCamera() {
		return heldCamera != null;
	}

	/** The player's own camera type, once, after a shot; null when nothing is held. */
	@Nullable
	public CameraType releaseCamera() {
		CameraType type = heldCamera;
		heldCamera = null;
		return type;
	}
}
