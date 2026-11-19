package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealRules.Outcome;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * A Crimson Troupe puppet-body ordeal on a professor's stage in the Chamber of Will (2026-11-17), modelled on the
 * Vesper arena: the rite on the floor hands the caster over when it would manifest the puppet, the caster fights it on
 * the professor's stage with their real inventory while the professor intervenes from above, and the ordeal ends in an
 * {@link Outcome} the rite settles from ({@link #admission}). A killing blow is caught, not suffered: the caster is
 * thrown out with nothing dropped and the rite fails. It is the professor's Chamber, not the caster's: none of the
 * caster's own Chamber progression runs for it.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class OrdealDirector {
	/** On the player: an ordeal was running when they last left, so their next login brings them home. */
	private static final String ACTIVE_KEY = Hemomancy.MOD_ID + ":ordeal_active";
	private static final int MISSING_TICKS = 60;
	private static final int ARRIVAL_GRACE = 40;
	private static final TicketType<ChunkPos> FLOOR_TICKET = TicketType.create("hemomancy_ordeal_floor",
			java.util.Comparator.comparingLong(ChunkPos::toLong), 60);
	/** Running ordeals and the outcomes waiting for their rite, by caster; session state cleared with the server. */
	private static final Map<UUID, Ordeal> ORDEALS = new HashMap<>();

	private OrdealDirector() {
	}

	/** What the rite does this tick while its caster has an ordeal. NONE: there is none, the rite runs as ever. */
	public enum Admission {
		NONE, WAIT, TICK, BREAK
	}

	/** One running or ended ordeal. */
	public static final class Ordeal {
		final ResourceKey<Level> riteLevel;
		final BlockPos riteCenter;
		final OrdealProfessor professor;
		final BlockPos stage;
		final long startedAt;
		UUID puppet;
		UUID professorEntity;
		Outcome outcome;
		/** Ticks the puppet has been missing from the stage without dying: only its death is a win. */
		int missingTicks;

		Ordeal(ResourceKey<Level> riteLevel, BlockPos riteCenter, OrdealProfessor professor, BlockPos stage, long startedAt) {
			this.riteLevel = riteLevel;
			this.riteCenter = riteCenter.immutable();
			this.professor = professor;
			this.stage = stage.immutable();
			this.startedAt = startedAt;
		}

		public BlockPos stage() {
			return stage;
		}

		public OrdealProfessor professor() {
			return professor;
		}

		public Optional<UUID> puppet() {
			return Optional.ofNullable(puppet);
		}

		/** The professor's true self circling the stage, once it has been raised. */
		public Optional<UUID> professorEntity() {
			return Optional.ofNullable(professorEntity);
		}
	}

	/** Why an ordeal could not begin now, or null: a visit, an ordeal, a projection or a possession under way, or no Chamber. */
	public static Component refusal(ServerPlayer caster) {
		if (ORDEALS.containsKey(caster.getUUID()) && ORDEALS.get(caster.getUUID()).outcome == null)
			return Component.translatable("hemomancy.circus.ordeal.refused.running");
		if (ChamberVisitService.isAway(caster) || FungalGardenTravelHelper.isProjectionActive(caster)
				|| caster.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL))
			return Component.translatable("hemomancy.circus.ordeal.refused.away");
		if (caster.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL) == null)
			return Component.translatable("hemomancy.circus.ordeal.refused.no_stage");
		return null;
	}

	/**
	 * Draws the caster onto the professor's stage and manifests the puppet there. The rite's own level is where its
	 * floor stays; it is held while the caster is away ({@link #admission}).
	 *
	 * @return the manifested puppet, or empty when the ordeal could not begin (the caller collapses the rite)
	 */
	public static Optional<Mob> begin(ServerPlayer caster, ServerLevel riteLevel, ActiveCardinalRite rite,
			OrdealProfessor professor, java.util.function.BiFunction<ServerLevel, BlockPos, Optional<Mob>> manifest) {
		if (refusal(caster) != null) return Optional.empty();
		ServerLevel chamber = caster.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		ChamberOfWillManager manager = ChamberOfWillManager.get(caster.getServer());
		BlockPos stage = OrdealRules.stageOrigin(manager.idFor(caster.getUUID()));
		OrdealStage.build(chamber, professor, stage);
		Ordeal ordeal = new Ordeal(riteLevel.dimension(), rite.getCenterPos(), professor, stage, chamber.getGameTime());
		// The puppet is raised on the stage itself, at the far side of the ring from where the caster arrives.
		Mob puppet = manifest.apply(chamber, stage.offset(0, 1, -5)).orElse(null);
		if (puppet == null) {
			OrdealStage.clear(chamber, stage);
			return Optional.empty();
		}
		OrdealTrueSelves.markTrueSelf(puppet);
		ordeal.puppet = puppet.getUUID();
		manager.rememberReturnPoint(caster);
		caster.getPersistentData().putBoolean(ACTIVE_KEY, true);
		ORDEALS.put(caster.getUUID(), ordeal);
		keepFloorLoaded(riteLevel, ordeal);
		caster.stopRiding();
		caster.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(chamber,
				new Vec3(stage.getX() + 0.5D, stage.getY() + 1.0D, stage.getZ() + 6.5D), Vec3.ZERO, 180.0F, 0.0F,
				net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));
		var trueSelf = OrdealTrueSelves.spawnProfessor(chamber, professor, caster, stage);
		if (trueSelf != null) ordeal.professorEntity = trueSelf.getUUID();
		line(caster, professor, "opening");
		return Optional.of(puppet);
	}

	public static boolean isInOrdeal(ServerPlayer player) {
		Ordeal ordeal = ORDEALS.get(player.getUUID());
		return ordeal != null && ordeal.outcome == null;
	}

	public static Optional<Ordeal> ordeal(UUID caster) {
		return Optional.ofNullable(ORDEALS.get(caster));
	}

	/**
	 * What the rite does this tick while its caster has an ordeal: wait while it runs; a win lets the rite go on to
	 * its culmination (the puppet's defeat is already recorded); any other end breaks it.
	 */
	public static Admission admission(UUID caster) {
		Ordeal ordeal = ORDEALS.get(caster);
		if (ordeal == null) return Admission.NONE;
		if (ordeal.outcome == null) return Admission.WAIT;
		ORDEALS.remove(caster);
		return OrdealRules.completesRite(ordeal.outcome)
				? Admission.TICK
				: Admission.BREAK;
	}

	/** Ends the ordeal: clears the stage, brings the caster home, and leaves the outcome for the rite. */
	public static void end(ServerPlayer caster, Outcome outcome) {
		Ordeal ordeal = ORDEALS.get(caster.getUUID());
		if (ordeal == null || ordeal.outcome != null) return;
		ordeal.outcome = outcome;
		Hemomancy.LOGGER.info("Ordeal of {} ({}) ended: {}", caster.getGameProfile().getName(), ordeal.professor.role(), outcome);
		OrdealInterventionsDriver.forget(caster.getUUID());
		if (ordeal.puppet != null) ordeal.professor.puppetMoves().forget(ordeal.puppet);
		ServerLevel chamber = caster.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		if (outcome == Outcome.WON) recordDefeat(caster, ordeal);
		line(caster, ordeal.professor, outcome == Outcome.WON ? "won" : "lost");
		if (chamber != null) {
			clearEntities(chamber, ordeal);
			OrdealStage.clear(chamber, ordeal.stage);
		}
		caster.getPersistentData().remove(ACTIVE_KEY);
		if (caster.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL))
			ChamberOfWillManager.get(caster.getServer()).exitChamber(caster);
	}

	/**
	 * The arena's claim on a player in the Chamber tick: an ordeal's caster is kept on the stage's ring and none of
	 * their own Chamber progression runs.
	 */
	public static boolean tickArenaPlayer(ServerPlayer player, ServerLevel level) {
		Ordeal ordeal = ORDEALS.get(player.getUUID());
		if (ordeal == null || ordeal.outcome != null) return false;
		if (!player.isCreative() && !player.isSpectator()) {
			double dx = player.getX() - (ordeal.stage.getX() + 0.5D), dz = player.getZ() - (ordeal.stage.getZ() + 0.5D);
			if (dx * dx + dz * dz > OrdealStage.RING_RADIUS * OrdealStage.RING_RADIUS || player.getY() < ordeal.stage.getY() - 4)
				player.teleportTo(ordeal.stage.getX() + 0.5D, ordeal.stage.getY() + 1.0D, ordeal.stage.getZ() + 6.5D);
		}
		return true;
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL))
			return;
		for (UUID id : java.util.List.copyOf(ORDEALS.keySet())) {
			Ordeal ordeal = ORDEALS.get(id);
			ServerPlayer caster = level.getServer().getPlayerList().getPlayer(id);
			if (ordeal.outcome != null || caster == null) continue;
			Entity puppet = level.getEntity(ordeal.puppet);
			if (puppet == null || !puppet.isAlive()) {
				// A puppet that died ended the ordeal from its death event; one that is merely gone fails it. A puppet
				// raised before the stage's chunk was entity-ticking is not visible yet: only count once it is, and not in
				// the first moments after arrival.
				boolean settled = level.getGameTime() - ordeal.startedAt > ARRIVAL_GRACE && level.isPositionEntityTicking(ordeal.stage);
				if (settled && ++ordeal.missingTicks > MISSING_TICKS) end(caster, Outcome.LEFT);
				continue;
			}
			ordeal.missingTicks = 0;
			if (level.getGameTime() - ordeal.startedAt > OrdealRules.TIME_LIMIT) {
				end(caster, Outcome.TIMED_OUT);
				continue;
			}
			if (level.getGameTime() % 20 == 0) {
				ServerLevel riteLevel = level.getServer().getLevel(ordeal.riteLevel);
				if (riteLevel == null || !floorStands(riteLevel, ordeal)) {
					end(caster, Outcome.FLOOR_LOST);
					continue;
				}
				keepFloorLoaded(riteLevel, ordeal);
			}
			if (puppet instanceof Mob mob && mob.getTarget() != caster) mob.setTarget(caster);
			OrdealInterventionsDriver.tick(level, caster, ordeal, (Mob) puppet);
			ordeal.professor.puppetMoves().tick(level, caster, (Mob) puppet,
					net.minecraft.world.phys.Vec3.atBottomCenterOf(ordeal.stage()).add(0, 1, 0));
		}
	}

	/** A killing blow on an ordeal's caster is caught: they are thrown out with nothing dropped and the rite fails. */
	public static boolean tryCatch(ServerPlayer player, LivingDamageEvent.Pre event) {
		if (!isInOrdeal(player) || !com.vincenthuto.hemomancy.common.damage.DamageStages.isLethal(player, event)) return false;
		com.vincenthuto.hemomancy.common.damage.DamageStages.leaveBarelyAlive(player, event);
		player.getServer().execute(() -> end(player, Outcome.CAUGHT));
		return true;
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && isInOrdeal(player)) end(player, Outcome.LEFT);
	}

	/** A login after a crash or a stop mid-ordeal: the stage is gone, so the player goes home. */
	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !player.getPersistentData().getBoolean(ACTIVE_KEY))
			return;
		player.getPersistentData().remove(ACTIVE_KEY);
		if (!isInOrdeal(player) && player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL))
			ChamberOfWillManager.get(player.getServer()).exitChamber(player);
	}

	/** The puppet's death is the win; a caster's death by any means the catch missed is a failure. */
	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && isInOrdeal(player)) {
			end(player, Outcome.CAUGHT);
			return;
		}
		if (!(event.getEntity().level() instanceof ServerLevel level)) return;
		for (var entry : java.util.List.copyOf(ORDEALS.entrySet())) {
			Ordeal ordeal = entry.getValue();
			if (ordeal.outcome != null || !event.getEntity().getUUID().equals(ordeal.puppet)) continue;
			ServerPlayer caster = level.getServer().getPlayerList().getPlayer(entry.getKey());
			if (caster != null) end(caster, Outcome.WON);
		}
	}

	private static void recordDefeat(ServerPlayer caster, Ordeal ordeal) {
		ServerLevel riteLevel = caster.getServer().getLevel(ordeal.riteLevel);
		if (riteLevel == null || ordeal.puppet == null) return;
		ActiveCardinalRite rite = CardinalRiteSavedData.get(riteLevel).getActiveRites().get(caster.getUUID());
		if (rite != null) rite.markPuppeteerTrialDefeated(ordeal.puppet);
	}

	private static boolean floorStands(ServerLevel riteLevel, Ordeal ordeal) {
		if (!riteLevel.hasChunkAt(ordeal.riteCenter)) return true;
		return riteLevel.getBlockEntity(ordeal.riteCenter) instanceof CardinalFocusBlockEntity;
	}

	private static void keepFloorLoaded(ServerLevel riteLevel, Ordeal ordeal) {
		ChunkPos chunk = new ChunkPos(ordeal.riteCenter);
		riteLevel.getChunkSource().addRegionTicket(FLOOR_TICKET, chunk, 2, chunk);
	}

	private static void clearEntities(ServerLevel chamber, Ordeal ordeal) {
		for (UUID id : new UUID[] { ordeal.puppet, ordeal.professorEntity }) {
			if (id == null) continue;
			Entity entity = chamber.getEntity(id);
			if (entity != null) entity.discard();
		}
	}

	private static void line(ServerPlayer caster, OrdealProfessor professor, String moment) {
		caster.displayClientMessage(Component.translatable("hemomancy.circus.ordeal." + professor.role() + "." + moment)
				.withStyle(ChatFormatting.DARK_RED), false);
	}

	/** Speaks the professor's line for a ramp of the fight. */
	static void rampLine(ServerPlayer caster, OrdealProfessor professor, OrdealRules.Ramp ramp) {
		line(caster, professor, "ramp." + ramp.name().toLowerCase(java.util.Locale.ROOT));
	}

	public static void clearSessionState() {
		ORDEALS.clear();
	}

}
