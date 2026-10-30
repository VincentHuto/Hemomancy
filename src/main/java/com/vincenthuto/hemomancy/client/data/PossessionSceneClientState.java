package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneDirector;
import com.vincenthuto.hemomancy.client.render.cinematic.scene.PossessionScene;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules.Scene;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PossessionScenePacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The possession scenes this client is showing, one per possessing player in range: which half is playing, its clock,
 * and where its pieces stand (the framed entity, the echo, the rite's Focus, the daemon's start). The world effects
 * ({@code PossessionSceneRenderer}) and the body hiding and posing read it; for the local player it also starts the
 * camera scene. Positions are remembered each tick, so effects carry on where an entity was after it is hidden or gone.
 */
public final class PossessionSceneClientState {
	private static final Map<Integer, Running> RUNNING = new HashMap<>();

	private PossessionSceneClientState() {
	}

	/** One half of a possession's cinematic for one player. */
	public static final class Running {
		private final int player;
		private final Scene scene;
		private final int anchor;
		private final int echo;
		private final BlockPos site;
		private final Vec3 daemon;
		private int age;
		@Nullable private UUID playerUuid;
		private Vec3 anchorPos;
		private Vec3 anchorPosO;
		private float anchorHeight = 1.0F;
		private float anchorWidth = 0.6F;
		private float anchorYaw;
		@Nullable private Vec3 echoPos;
		@Nullable private Vec3 playerPos;

		Running(PossessionScenePacket packet) {
			this.player = packet.player();
			this.scene = Scene.byOrdinal(packet.scene());
			this.anchor = packet.anchor();
			this.echo = packet.echo();
			this.site = packet.site();
			this.daemon = packet.daemon();
			this.anchorPos = Vec3.atBottomCenterOf(site);
			this.anchorPosO = anchorPos;
		}

		public int player() { return player; }
		public Scene scene() { return scene; }
		public int anchorId() { return anchor; }
		public int echoId() { return echo; }
		public BlockPos site() { return site; }
		/** Where the rite's daemon hung when the rite completed. */
		public Vec3 daemon() { return daemon; }
		public int age() { return age; }
		/** Scene time in ticks, with the partial tick. */
		public float time(float partialTick) { return age + partialTick; }
		/** A stable per-player seed for the scene's geometry. */
		public long seed() { return player * 2654435761L + scene.ordinal() * 97L; }
		public Vec3 anchorPos(float partialTick) { return anchorPosO.lerp(anchorPos, partialTick); }
		public float anchorHeight() { return anchorHeight; }
		public float anchorWidth() { return anchorWidth; }
		public float anchorYaw() { return anchorYaw; }
		/** The echo's feet, or the anchor's when the echo is not part of this half. */
		public Vec3 echoPos() { return echoPos != null ? echoPos : anchorPos; }
		/** The possessing player's feet, as last seen. */
		public Vec3 playerPos() { return playerPos != null ? playerPos : anchorPos; }

		private void sample() {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.level == null) return;
			Entity framed = minecraft.level.getEntity(anchor);
			anchorPosO = anchorPos;
			if (framed != null && !framed.isRemoved()) {
				anchorPos = framed.position();
				if (age == 0) anchorPosO = anchorPos;
				anchorHeight = framed.getBbHeight();
				anchorWidth = framed.getBbWidth();
				anchorYaw = framed.getYRot();
			}
			Entity echoEntity = echo < 0 ? null : minecraft.level.getEntity(echo);
			if (echoEntity != null) echoPos = echoEntity.position();
			Entity possessor = minecraft.level.getEntity(player);
			if (possessor != null) {
				playerPos = possessor.position();
				playerUuid = possessor.getUUID();
			}
		}
	}

	/** A body pose the scene lays over an entity's own render: squash, sink, a tip over sideways and a shudder. */
	public record BodyPose(float scaleXZ, float scaleY, float sink, float tipDeg, float shudder) {
		public static final BodyPose NONE = new BodyPose(1.0F, 1.0F, 0.0F, 0.0F, 0.0F);
	}

	public static void start(PossessionScenePacket packet) {
		Scene scene = Scene.byOrdinal(packet.scene());
		Minecraft minecraft = Minecraft.getInstance();
		boolean local = minecraft.player != null && minecraft.player.getId() == packet.player();
		if (scene == null) {
			RUNNING.remove(packet.player());
			// The server ended the sequence (skip, abandon, a failed crossing): the camera and input come back too.
			if (local && BossSceneDirector.INSTANCE.scene() instanceof PossessionScene) BossSceneDirector.INSTANCE.clear();
			return;
		}
		Running running = new Running(packet);
		running.sample();
		RUNNING.put(packet.player(), running);
		if (local) BossSceneDirector.INSTANCE.start(BossPresentationIds.possessionScene(scene), packet.anchor(), 0);
	}

	public static void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			RUNNING.clear();
			return;
		}
		RUNNING.values().removeIf(running -> {
			running.sample();
			return ++running.age > running.scene.length() + 40;
		});
	}

	/** The local player skipped: their own scene's world effects end with it. */
	public static void skippedLocally() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null) RUNNING.remove(minecraft.player.getId());
	}

	public static void clear() {
		RUNNING.clear();
	}

	public static Collection<Running> running() {
		return RUNNING.values();
	}

	@Nullable
	public static Running of(int player) {
		return RUNNING.get(player);
	}

	/**
	 * Whether an entity is not drawn at all this frame: the possessing player while the echo or a scene body stands
	 * for them, or a surrogate once it has dissolved into the floor.
	 */
	public static boolean hides(Entity entity, float partialTick) {
		for (Running running : RUNNING.values()) {
			float t = running.time(partialTick);
			if (isPlayer(running, entity)) {
				boolean hidden = switch (running.scene) {
					case DEPARTURE -> true;
					case ARRIVAL -> t < PossessionSceneRules.ARRIVAL_CONTROL;
					case BROKEN_GARDENS -> t >= PossessionSceneRules.BROKEN_BURST;
					case CLEAN_GARDENS -> t >= PossessionSceneRules.CLEAN_COLLAPSE_END + 6;
					case BROKEN_RITE -> t < PossessionSceneRules.BROKEN_REMANIFEST_END;
					case CLEAN_RITE -> t < PossessionSceneRules.CLEAN_REMANIFEST_END;
				};
				if (hidden) return true;
			}
			if (running.scene == Scene.DEPARTURE && entity.getId() == running.anchor
					&& t >= PossessionSceneRules.DEPARTURE_DISSOLVE_END) return true;
		}
		return false;
	}

	/** The pose a running scene lays over this entity, or {@link BodyPose#NONE}. */
	public static BodyPose pose(LivingEntity entity, float partialTick) {
		for (Running running : RUNNING.values()) {
			float t = running.time(partialTick);
			boolean player = isPlayer(running, entity);
			boolean anchor = entity.getId() == running.anchor;
			switch (running.scene) {
				case DEPARTURE -> {
					if (anchor) {
						// The surrogate thins and sinks as daemon and body come apart into spores.
						float x = HemoEasing.clamp01((t - PossessionSceneRules.DEPARTURE_DAEMON_ARRIVES)
								/ (PossessionSceneRules.DEPARTURE_DISSOLVE_END - PossessionSceneRules.DEPARTURE_DAEMON_ARRIVES));
						float shudder = t >= PossessionSceneRules.DEPARTURE_DAEMON_ARRIVES - 4 ? 0.04F * (1.0F - x) : 0.0F;
						return new BodyPose(1.0F - 0.55F * x, 1.0F - 0.8F * x, 0.25F * x, 0.0F, shudder);
					}
				}
				case ARRIVAL -> {
					if (anchor) {
						// Held slumped by the tendrils, filled, then shuddering upright as they let go.
						float up = HemoEasing.smoothstep(HemoEasing.clamp01((t - PossessionSceneRules.ARRIVAL_INJECT_END)
								/ (PossessionSceneRules.ARRIVAL_RELEASE_END - PossessionSceneRules.ARRIVAL_INJECT_END)));
						float slump = 1.0F - up;
						float shudder = t > PossessionSceneRules.ARRIVAL_SEIZE_END && t < PossessionSceneRules.ARRIVAL_CONTROL
								? 0.02F + 0.05F * up * (1.0F - up) * 4.0F : 0.0F;
						return new BodyPose(1.0F, 1.0F - 0.18F * slump, 0.06F * slump, 14.0F * slump, shudder);
					}
				}
				case BROKEN_GARDENS -> {
					if (player) {
						// Swells and trembles for an instant before it bursts.
						float x = HemoEasing.clamp01(t / PossessionSceneRules.BROKEN_BURST);
						return new BodyPose(1.0F + 0.25F * x, 1.0F + 0.12F * x, 0.0F, 0.0F, 0.03F + 0.08F * x);
					}
				}
				case CLEAN_GARDENS -> {
					if (player) {
						float stammer = t < PossessionSceneRules.CLEAN_STAMMER_END
								? 0.05F * Mth.abs(Mth.sin(t * 1.7F)) : 0.0F;
						float fall = HemoEasing.smoothstep(HemoEasing.clamp01((t - PossessionSceneRules.CLEAN_STAMMER_END)
								/ (PossessionSceneRules.CLEAN_COLLAPSE_END - PossessionSceneRules.CLEAN_STAMMER_END)));
						float melt = HemoEasing.clamp01((t - PossessionSceneRules.CLEAN_COLLAPSE_END) / 6.0F);
						float stagger = t < PossessionSceneRules.CLEAN_STAMMER_END ? 6.0F * Mth.sin(t * 0.45F) : 0.0F;
						return new BodyPose(1.0F + 0.2F * melt, 1.0F - 0.75F * melt, 0.1F * melt, stagger + 88.0F * fall,
								stammer);
					}
				}
				default -> {
				}
			}
		}
		return BodyPose.NONE;
	}

	/** The possessing player, or the client-side stand-in body drawn for them (which carries their UUID). */
	private static boolean isPlayer(Running running, Entity entity) {
		return entity.getId() == running.player
				|| running.playerUuid != null && running.playerUuid.equals(entity.getUUID());
	}
}
