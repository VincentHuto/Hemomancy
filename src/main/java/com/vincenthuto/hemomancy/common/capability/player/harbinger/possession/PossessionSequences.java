package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules.Scene;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules.Sequence;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules.Step;
import com.vincenthuto.hemomancy.common.entity.PossessionEchoEntity;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PossessionScenePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The staged sequences that carry a will out and home, one per player at most, run from the
 * {@link PossessionSceneRules} tick table. Nothing lasting changes before a sequence's cut: a departure abandoned before
 * it (logout, a lethal blow) leaves the caster standing at the rite with their inventory. A skip runs every step left
 * in order, so it ends in the same state as letting the scenes play. Session state only: a crash mid-departure leaves
 * an echo that dissolves on its own and a held surrogate that {@link PossessionService#releaseOrphans} frees at login.
 */
public final class PossessionSequences {
	/** Scene bodies carry this tag; one seen without its sequence (after a crash) is removed. */
	public static final String SCENE_BODY_TAG = "hemomancy.possession_scene_body";
	/** Players within this range of a scene's anchor see its world effects. */
	public static final double RANGE = 64.0D;

	private static final Map<UUID, Staged> STAGED = new HashMap<>();

	private PossessionSequences() {
	}

	/** One player's running sequence. Begin fields are set by {@link #startBegin}, return fields by {@link #startReturn}. */
	static final class Staged {
		final Sequence sequence;
		int t = -1;
		boolean skipping;
		@Nullable UUID echo;
		@Nullable ResourceLocation echoDimension;
		@Nullable UUID surrogate;
		@Nullable ResourceLocation surrogateDimension;
		Vec3 hold = Vec3.ZERO;
		BlockPos site = BlockPos.ZERO;
		Vec3 daemon = Vec3.ZERO;
		// Begin
		PossessionRules.Offerings offerings = PossessionRules.Offerings.NONE;
		float yaw;
		@Nullable UUID sceneBody;
		// Return
		int mindBrokenTicks;

		Staged(Sequence sequence) {
			this.sequence = sequence;
		}
	}

	public static boolean isStaged(ServerPlayer player) {
		return STAGED.containsKey(player.getUUID());
	}

	@Nullable
	public static Sequence sequence(ServerPlayer player) {
		Staged staged = STAGED.get(player.getUUID());
		return staged == null ? null : staged.sequence;
	}

	/** Whether {@code echo} belongs to a sequence {@code owner} is running, before or after the attachment knows it. */
	public static boolean holdsEcho(ServerPlayer owner, UUID echo) {
		Staged staged = STAGED.get(owner.getUUID());
		return staged != null && echo.equals(staged.echo);
	}

	/** The echo a running departure raised, for its crossing. */
	@Nullable
	static UUID echoOf(ServerPlayer player) {
		Staged staged = STAGED.get(player.getUUID());
		return staged == null ? null : staged.echo;
	}

	/** Whether {@code body} is the surrogate of a running sequence: its verdict waits for the settle tick. */
	public static boolean holdsSurrogate(UUID body) {
		for (Staged staged : STAGED.values()) {
			if (body.equals(staged.surrogate)) return true;
		}
		return false;
	}

	/** Whether {@code body} is the scene facsimile of a running arrival. */
	public static boolean isSceneBody(UUID body) {
		for (Staged staged : STAGED.values()) {
			if (body.equals(staged.sceneBody)) return true;
		}
		return false;
	}

	/** Starts a departure: the echo and stilled surrogate are already in place; the crossing waits for the cut. */
	static void startBegin(ServerPlayer caster, PossessionEchoEntity echo, LivingEntity surrogate,
			PossessionRules.Offerings offerings, BlockPos site, Vec3 daemon) {
		Staged staged = new Staged(Sequence.BEGIN);
		staged.echo = echo.getUUID();
		staged.echoDimension = caster.level().dimension().location();
		staged.surrogate = surrogate.getUUID();
		staged.surrogateDimension = surrogate.level().dimension().location();
		staged.hold = caster.position();
		staged.yaw = caster.getYRot();
		staged.offerings = offerings;
		staged.site = site;
		staged.daemon = daemon;
		STAGED.put(caster.getUUID(), staged);
		play(caster, staged, Scene.DEPARTURE, surrogate.getId(), echo.getId());
		advance(caster, staged);
	}

	/**
	 * Starts a return. A return already running is left alone. A will that is no longer in the Gardens skips the
	 * Gardens half: there is no body there to fall.
	 */
	static void startReturn(ServerPlayer player, boolean broken, int mindBrokenTicks) {
		Staged running = STAGED.get(player.getUUID());
		if (running != null) {
			if (running.sequence != Sequence.BEGIN) return;
			// An arrival interrupted: its scene body goes, and the return starts from the Gardens.
			discardSceneBody(player.server, running);
			STAGED.remove(player.getUUID());
		}
		Possession possession = HemoCapabilityAccess.getPossession(player);
		Staged staged = new Staged(Sequence.returning(broken));
		staged.mindBrokenTicks = mindBrokenTicks;
		staged.echo = possession.echo();
		staged.echoDimension = possession.returnDimension();
		staged.surrogate = possession.surrogate();
		staged.surrogateDimension = possession.surrogateDimension();
		staged.hold = player.position();
		staged.site = possession.returnBlock();
		STAGED.put(player.getUUID(), staged);
		if (PossessionService.inGardens(player.level())) {
			play(player, staged, staged.sequence.first(), player.getId(), -1);
		} else {
			staged.t = staged.sequence.cut() - 1;
		}
		advance(player, staged);
	}

	/** Called every server tick for every player. */
	public static void tick(ServerPlayer player) {
		Staged staged = STAGED.get(player.getUUID());
		if (staged != null) advance(player, staged);
	}

	/** The possessor asked to skip: everything left runs now, without the scenes. */
	public static void skip(ServerPlayer player) {
		Staged staged = STAGED.get(player.getUUID());
		if (staged == null) return;
		staged.skipping = true;
		runTo(player, staged, staged.sequence.settle());
		clearScene(player);
	}

	/**
	 * Ends a sequence at once and without scenes, for logout and for a blow nothing may absorb. A departure before its
	 * cut is undone; anything past a cut is finished as if it had played.
	 */
	public static void abandon(ServerPlayer player) {
		Staged staged = STAGED.get(player.getUUID());
		if (staged == null) return;
		if (staged.sequence == Sequence.BEGIN && staged.t < staged.sequence.cut()) {
			STAGED.remove(player.getUUID());
			undoDeparture(player, staged);
			clearScene(player);
			return;
		}
		skip(player);
	}

	private static void advance(ServerPlayer player, Staged staged) {
		runTo(player, staged, staged.t + 1);
		if (STAGED.get(player.getUUID()) != staged) return;
		hold(player, staged);
	}

	private static void runTo(ServerPlayer player, Staged staged, int to) {
		int from = staged.t;
		staged.t = Math.min(to, staged.sequence.settle());
		for (Step step : PossessionSceneRules.due(staged.sequence, from, staged.t)) {
			if (STAGED.get(player.getUUID()) != staged) return;
			run(player, staged, step);
		}
		PossessionEchoEntity echo = echo(player.server, staged);
		if (echo != null) echo.setManifest(PossessionSceneRules.manifest(staged.sequence, staged.t));
		if (PossessionSceneRules.finished(staged.sequence, staged.t)) STAGED.remove(player.getUUID(), staged);
	}

	private static void run(ServerPlayer player, Staged staged, Step step) {
		switch (staged.sequence) {
			case BEGIN -> {
				if (step == Step.CROSS) crossOut(player, staged);
				else settleArrival(player, staged);
			}
			case RETURN_BROKEN, RETURN_CLEAN -> {
				boolean broken = staged.sequence == Sequence.RETURN_BROKEN;
				if (step == Step.CROSS) crossHome(player, staged, broken);
				else settleReturn(player, staged, broken);
			}
		}
	}

	private static void crossOut(ServerPlayer player, Staged staged) {
		ServerLevel riteLevel = level(player.server, staged.surrogateDimension);
		Entity found = riteLevel == null || staged.surrogate == null ? null : riteLevel.getEntity(staged.surrogate);
		if (!(found instanceof LivingEntity surrogate) || !surrogate.isAlive()
				|| !PossessionService.cross(player, surrogate, staged.offerings, staged.hold, staged.yaw)) {
			// The surrogate is gone (killed, unloaded) or the Gardens refused: the will never leaves.
			STAGED.remove(player.getUUID());
			undoDeparture(player, staged);
			clearScene(player);
			return;
		}
		staged.hold = player.position();
		if (staged.skipping) return;
		LivingEntity body = spawnSceneBody(player);
		if (body != null) staged.sceneBody = body.getUUID();
		play(player, staged, Scene.ARRIVAL, body == null ? player.getId() : body.getId(), -1);
	}

	private static void settleArrival(ServerPlayer player, Staged staged) {
		discardSceneBody(player.server, staged);
	}

	private static void crossHome(ServerPlayer player, Staged staged, boolean broken) {
		PossessionService.finishStaged(player, broken, staged.mindBrokenTicks);
		staged.hold = player.position();
		if (staged.skipping) return;
		PossessionEchoEntity echo = echo(player.server, staged);
		play(player, staged, staged.sequence.second(), echo == null ? player.getId() : echo.getId(),
				echo == null ? -1 : echo.getId());
	}

	private static void settleReturn(ServerPlayer player, Staged staged, boolean broken) {
		PossessionEchoEntity echo = echo(player.server, staged);
		Vec3 facing = Vec3.directionFromRotation(0.0F, echo == null ? player.getYRot() : echo.getYRot());
		if (echo != null) echo.discard();
		PossessionService.settleSurrogate(player.server, staged.surrogate, staged.surrogateDimension, broken);
		if (broken && !staged.skipping && floorBehind(player, facing)) {
			// Thrown back out of the echo a block or two.
			player.knockback(0.9, facing.x, facing.z);
			player.hurtMarked = true;
		}
	}

	/** Whether the ground behind the echo can take the throw: open space over a solid floor, not a ledge. */
	private static boolean floorBehind(ServerPlayer player, Vec3 facing) {
		ServerLevel level = player.serverLevel();
		for (double back = 0.75; back <= 2.0; back += 0.625) {
			Vec3 at = player.position().subtract(facing.x * back, 0.0, facing.z * back);
			BlockPos floor = BlockPos.containing(at.x, at.y - 0.5, at.z);
			if (!level.noCollision(player, player.getBoundingBox().move(at.subtract(player.position())))
					|| !level.getBlockState(floor).isFaceSturdy(level, floor, net.minecraft.core.Direction.UP)) {
				return false;
			}
		}
		return true;
	}

	/** A departure that never crossed: the echo goes, the surrogate is let go, the caster stands where they were. */
	private static void undoDeparture(ServerPlayer player, Staged staged) {
		PossessionEchoEntity echo = echo(player.server, staged);
		if (echo != null) echo.discard();
		SurrogateLedger ledger = SurrogateLedger.get(player.server);
		if (staged.surrogate != null) {
			ServerLevel level = level(player.server, staged.surrogateDimension);
			if (level != null && level.getEntity(staged.surrogate) instanceof LivingEntity surrogate) {
				ledger.forget(staged.surrogate);
				PossessionService.release(surrogate);
			} else {
				// Not loaded: it is let go the next time it is seen.
				ledger.settle(staged.surrogate, false);
			}
		}
	}

	/** Keeps the player where the scene frames them: the camera is elsewhere, and the body must not wander. */
	private static void hold(ServerPlayer player, Staged staged) {
		player.setDeltaMovement(Vec3.ZERO);
		if (player.position().distanceToSqr(staged.hold) > 0.25) {
			if (player.connection != null) {
				player.connection.teleport(staged.hold.x, staged.hold.y, staged.hold.z, player.getYRot(), player.getXRot());
			} else {
				player.teleportTo(staged.hold.x, staged.hold.y, staged.hold.z);
			}
		}
	}

	/** The facsimile the arrival's tendrils seize: stilled, untouchable, gone when the player takes its place. */
	@Nullable
	private static LivingEntity spawnSceneBody(ServerPlayer player) {
		SurrogateForm form = HemoCapabilityAccess.possessionForm(player);
		if (form == null) return null;
		ServerLevel level = player.serverLevel();
		LivingEntity body;
		if (form.facsimileId().isEmpty()) {
			ArmorStand stand = new ArmorStand(level, player.getX(), player.getY(), player.getZ());
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				if (slot.getType() != EquipmentSlot.Type.ANIMAL_ARMOR) stand.setItemSlot(slot, player.getItemBySlot(slot).copy());
			}
			body = stand;
		} else {
			var type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(form.facsimileId()));
			if (!(type.create(level) instanceof LivingEntity created)) return null;
			body = created;
		}
		body.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
		body.setYHeadRot(player.getYRot());
		body.setYBodyRot(player.getYRot());
		if (body instanceof Mob mob) {
			mob.setNoAi(true);
			mob.setPersistenceRequired();
		}
		body.setInvulnerable(true);
		body.setSilent(true);
		body.addTag(SCENE_BODY_TAG);
		level.addFreshEntity(body);
		return body;
	}

	private static void discardSceneBody(MinecraftServer server, Staged staged) {
		if (staged.sceneBody == null) return;
		for (ServerLevel level : server.getAllLevels()) {
			Entity body = level.getEntity(staged.sceneBody);
			if (body != null) body.discard();
		}
		staged.sceneBody = null;
	}

	@Nullable
	private static PossessionEchoEntity echo(MinecraftServer server, Staged staged) {
		if (staged.echo == null) return null;
		ServerLevel level = level(server, staged.echoDimension);
		return level != null && level.getEntity(staged.echo) instanceof PossessionEchoEntity echo ? echo : null;
	}

	@Nullable
	private static ServerLevel level(MinecraftServer server, @Nullable ResourceLocation dimension) {
		return dimension == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
	}

	private static void play(ServerPlayer player, Staged staged, Scene scene, int anchorId, int echoId) {
		if (staged.skipping) return;
		PossessionScenePacket packet = new PossessionScenePacket(player.getId(), scene.ordinal(), anchorId, echoId,
				staged.site, staged.daemon);
		PacketHandler.sendToPlayer(player, packet);
		PacketHandler.sendNear(player.serverLevel(), player, player.getX(), player.getY(), player.getZ(), RANGE, packet);
	}

	private static void clearScene(ServerPlayer player) {
		PossessionScenePacket packet = PossessionScenePacket.clear(player.getId());
		PacketHandler.sendToPlayer(player, packet);
		PacketHandler.sendNear(player.serverLevel(), player, player.getX(), player.getY(), player.getZ(), RANGE, packet);
	}

	/** Logout: the sequence ends now, before the player leaves the world. */
	public static void logout(ServerPlayer player) {
		abandon(player);
	}

	public static void clearSessionState() {
		STAGED.clear();
	}
}
