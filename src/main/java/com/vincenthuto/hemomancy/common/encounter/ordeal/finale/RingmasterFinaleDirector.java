package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealDirector;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.Ending;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirectorRules.Reason;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusSiteStateService;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * The Ringmaster's finale in his Chamber (2026-11-18). Begun from his dialogue at the real pavilion: the real site is
 * held (its Ringmaster, carousel and troupe stand still and nobody else can begin there), a copy of the pavilion is laid
 * on the player's stage in the Chamber with the real site's rotation, his copy and the carousel's are raised on it, and
 * the player is carried across to the matching spot. The fight runs there; its ending is laid on the real site once the
 * player is home ({@link CircusFinaleSettlement}). A finale broken off releases everything and can be begun again.
 * The pieces are the ordeals' own: the per-player stage origin, the Chamber return point, the caught killing blow and
 * the login recovery.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class RingmasterFinaleDirector {
	private static final String ACTIVE_KEY = Hemomancy.MOD_ID + ":finale_active";
	/** Entities raised on a finale's stage carry this tag, so a stage left by a crash is swept on the next begin. */
	public static final String COPY_TAG = "hemomancy_finale_copy";
	private static final Map<UUID, Finale> FINALES = new HashMap<>();
	/** How far into his opening scene, played in the real tent, the player is carried across. */
	public static final int CROSS_AT = 24;

	private RingmasterFinaleDirector() {
	}

	/** One finale under way. */
	public static final class Finale {
		final ResourceKey<Level> siteLevel;
		final BlockPos siteOrigin;
		final Rotation rotation;
		final BlockPos stage;
		FinalePhase phase = FinalePhase.CROSSING;
		int phaseTicks;
		UUID ringmaster;
		UUID carousel;
		/** Ticks left before the crossing, while his opening scene plays in the real tent; -1 once across. */
		int crossIn = CROSS_AT;
		/** Ticks since the crossing while his opening still waits to be handed to his copy; -1 once handed on. */
		int sceneWaiting = -1;
		boolean over;
		/** His moves and the carousel's, as they run on this finale's stage. */
		final RingmasterFinaleMoves.State moves = new RingmasterFinaleMoves.State();
		/** His roosting bats and the blood they have brought him. */
		final FinaleSwarm.State swarm = new FinaleSwarm.State();
		/** At the kneel: how far the owner has drawn his blood off, and when he last refused someone unfit. */
		float absorption;
		long refusedAt = Long.MIN_VALUE / 2;
		/** Set once the finale's ending has been called, so a second blow or channel cannot call it again. */
		boolean ending;
		/** The frozen stand-ins of the real troupe raised for the crossing; they unspool as his opening ends. */
		final java.util.Set<UUID> standIns = new java.util.HashSet<>();
		/** Ticks a crossed player has spent out of his Chamber ({@link RingmasterFinaleDirectorRules#awayTicks}). */
		int away;

		Finale(ResourceKey<Level> siteLevel, BlockPos siteOrigin, Rotation rotation, BlockPos stage) {
			this.siteLevel = siteLevel;
			this.siteOrigin = siteOrigin.immutable();
			this.rotation = rotation;
			this.stage = stage.immutable();
		}

		public BlockPos stage() {
			return stage;
		}

		public BlockPos siteOrigin() {
			return siteOrigin;
		}

		public ResourceKey<Level> siteLevel() {
			return siteLevel;
		}

		public Rotation rotation() {
			return rotation;
		}

		public FinalePhase phase() {
			return phase;
		}

		public int phaseTicks() {
			return phaseTicks;
		}

		public UUID ringmaster() {
			return ringmaster;
		}

		public UUID carousel() {
			return carousel;
		}

		public RingmasterFinaleMoves.State moves() {
			return moves;
		}

		public FinaleSwarm.State swarm() {
			return swarm;
		}

		/** The top of the copy's ring floor at its centre: the carousel's cell. */
		public Vec3 floor() {
			return Vec3.atBottomCenterOf(stage);
		}
	}

	/** Why the finale cannot begin now, or null. */
	@Nullable
	public static Component refusal(ServerPlayer player) {
		if (FINALES.containsKey(player.getUUID())) return Component.translatable("hemomancy.circus.ordeal.refused.running");
		return OrdealDirector.refusal(player);
	}

	/**
	 * Begins the finale for {@code player} at the real site of {@code carousel}: holds the site, lays his stage and
	 * carries the player across. False, with a line, when it cannot begin.
	 */
	public static boolean begin(ServerPlayer player, CircusRingmasterEntity real, CircusCarouselEntity carousel) {
		ServerLevel siteLevel = player.serverLevel();
		BlockPos origin = carousel.encounterOrigin();
		CircusPavilionSavedData data = CircusPavilionSavedData.get(siteLevel);
		// A busy or settled site names itself first, as the old finale did, before anything else is tried.
		if (refuseSite(player, data.site(siteLevel, origin))) return false;
		Component refused = refusal(player);
		if (refused != null) {
			player.displayClientMessage(refused, false);
			return false;
		}
		if (!data.begin(siteLevel, origin, player.getUUID(), CircusRouteRules.Route.NEUTRAL)) {
			refuseSite(player, data.site(siteLevel, origin));
			return false;
		}
		ServerLevel chamber = player.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		ChamberOfWillManager manager = ChamberOfWillManager.get(player.getServer());
		Rotation rotation = CircusSiteStateService.rotation(siteLevel, origin);
		BlockPos stage = FinaleStage.origin(manager.idFor(player.getUUID()));
		sweep(chamber, stage);
		FinaleStage.build(chamber, stage, rotation);
		Finale finale = new Finale(siteLevel.dimension(), origin, rotation, stage);
		CircusRingmasterEntity copy = EntityInit.circus_ringmaster.get().create(chamber);
		CircusCarouselEntity wheel = EntityInit.impaled_procession.get().create(chamber);
		if (copy == null || wheel == null) {
			CircusPavilionSavedData.get(siteLevel).reset(siteLevel, origin, player.getUUID());
			FinaleStage.clear(chamber, stage, rotation);
			return false;
		}
		Vec3 perch = FinaleStage.toCopy(real.position(), origin, stage, rotation);
		copy.markStageCopy();
		copy.addTag(COPY_TAG);
		copy.moveTo(perch.x, perch.y, perch.z, real.getYRot(), real.getXRot());
		copy.setHealth(copy.getMaxHealth());
		copy.setEchoRoles(FinaleEchoes.roleMask(FinaleEchoes.passed(player)));
		wheel.moveTo(stage.getX() + 0.5D, stage.getY(), stage.getZ() + 0.5D, carousel.getYRot(), 0.0F);
		wheel.markStageCopy();
		wheel.addTag(COPY_TAG);
		chamber.addFreshEntity(copy);
		chamber.addFreshEntity(wheel);
		finale.ringmaster = copy.getUUID();
		finale.carousel = wheel.getUUID();
		// The real troupe on the ring is raised too, frozen, so the crossing shows nobody missing.
		FinaleStandIns.raise(siteLevel, chamber, origin, stage, rotation).forEach(standIn -> finale.standIns.add(standIn.getUUID()));
		manager.rememberReturnPoint(player);
		markActive(player, siteLevel.dimension(), origin, rotation, stage);
		FINALES.put(player.getUUID(), finale);
		// His opening scene starts in the real tent; the crossing falls inside it (onSiteTick), so it is never seen.
		com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes.playTo(player, real,
				com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds.SCENE_CIRCUS_RINGMASTER, 0);
		Hemomancy.LOGGER.info("Finale of {} begun at {} (stage {}, {})", player.getGameProfile().getName(), origin, stage, rotation);
		return true;
	}

	/** Records where a finale stands, so a login after a crash can release its site and clear its stage. */
	public static void markActive(ServerPlayer player, ResourceKey<Level> siteLevel, BlockPos origin, Rotation rotation,
			BlockPos stage) {
		net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
		tag.putString("Level", siteLevel.location().toString());
		tag.putLong("Origin", origin.asLong());
		tag.putInt("Rotation", rotation.ordinal());
		tag.putLong("Stage", stage.asLong());
		player.getPersistentData().put(ACTIVE_KEY, tag);
	}

	/** Sends the busy or settled line for a site that cannot be claimed, and answers whether it did. */
	private static boolean refuseSite(ServerPlayer player, CircusPavilionSavedData.Site site) {
		String key = RingmasterFinaleDirectorRules.siteRefusalKey(site.activeOwner(), site.outcome());
		if (key == null) return false;
		player.displayClientMessage(Component.translatable(key).withStyle(net.minecraft.ChatFormatting.DARK_GRAY), false);
		return true;
	}

	/**
	 * Whether a finale still owns {@code entity}, one of the copies raised on a stage. A copy no finale owns (a crash's,
	 * loaded after its stage was cleared) removes itself.
	 */
	public static boolean ownsCopy(Entity entity) {
		UUID id = entity.getUUID();
		for (Finale finale : FINALES.values())
			if (!finale.over && (id.equals(finale.ringmaster) || id.equals(finale.carousel) || finale.standIns.contains(id)))
				return true;
		return false;
	}

	/** Whether a finale holds the ring whose ground {@code home} is on: its finale troupe stands still. */
	public static boolean holdsArenaOf(ResourceKey<Level> level, BlockPos home) {
		for (Finale finale : FINALES.values())
			if (!finale.over && finale.siteLevel.equals(level)
					&& com.vincenthuto.hemomancy.common.entity.npc.circus.CircusArena.of(finale.siteOrigin)
							.holds(Vec3.atBottomCenterOf(home)))
				return true;
		return false;
	}

	public static boolean isInFinale(ServerPlayer player) {
		Finale finale = FINALES.get(player.getUUID());
		return finale != null && !finale.over;
	}

	public static Optional<Finale> finale(UUID player) {
		return Optional.ofNullable(FINALES.get(player));
	}

	/** Whether a finale in the Chamber holds the real site at {@code origin} in {@code level}: its cast stands still. */
	public static boolean holds(ResourceKey<Level> level, BlockPos origin) {
		for (Finale finale : FINALES.values())
			if (!finale.over && finale.siteLevel.equals(level) && finale.siteOrigin.equals(origin)) return true;
		return false;
	}

	/** The finale whose stage holds {@code copy}, by its owner. */
	public static Optional<Map.Entry<UUID, Finale>> ofCopy(Entity copy) {
		return FINALES.entrySet().stream().filter(e -> copy.getUUID().equals(e.getValue().ringmaster)).findFirst();
	}

	/**
	 * Ends the finale: clears the stage and its copies, brings the player home, wakes the real site, and lays the
	 * ending on it, or releases it when {@code ending} is null (the finale broken off).
	 */
	public static void end(ServerPlayer player, @Nullable Ending ending) {
		Finale finale = FINALES.remove(player.getUUID());
		if (finale == null || finale.over) return;
		finale.over = true;
		Hemomancy.LOGGER.info("Finale of {} ended: {}", player.getGameProfile().getName(), ending == null ? "BROKEN OFF" : ending);
		ServerLevel chamber = player.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		if (chamber != null) {
			sweep(chamber, finale.stage);
			FinaleStage.clear(chamber, finale.stage, finale.rotation);
		}
		player.getPersistentData().remove(ACTIVE_KEY);
		if (player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL))
			ChamberOfWillManager.get(player.getServer()).exitChamber(player);
		ServerLevel siteLevel = player.getServer().getLevel(finale.siteLevel);
		if (siteLevel == null) return;
		if (ending != null) CircusFinaleSettlement.apply(siteLevel, finale.siteOrigin, player, ending);
		else if (RingmasterFinaleDirectorRules.onInterrupt(Reason.CAUGHT).releaseSite())
			CircusPavilionSavedData.get(siteLevel).reset(siteLevel, finale.siteOrigin, player.getUUID());
	}

	/** The arena's claim on a player in the Chamber tick: a finale's player is kept to his tent; no room of theirs runs. */
	public static boolean tickArenaPlayer(ServerPlayer player, ServerLevel level) {
		return isInFinale(player);
	}

	/** A killing blow on the finale's player is caught: they are thrown out with nothing dropped, the finale breaks off. */
	public static boolean tryCatch(ServerPlayer player, LivingDamageEvent.Pre event) {
		if (!isInFinale(player) || !com.vincenthuto.hemomancy.common.damage.DamageStages.isLethal(player, event)) return false;
		com.vincenthuto.hemomancy.common.damage.DamageStages.leaveBarelyAlive(player, event);
		player.getServer().execute(() -> end(player, null));
		return true;
	}

	/** Counts down to the crossing in the real site's level, and carries the player across when it falls. */
	@SubscribeEvent
	public static void onSiteTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		for (Map.Entry<UUID, Finale> entry : List.copyOf(FINALES.entrySet())) {
			Finale finale = entry.getValue();
			if (finale.over || finale.crossIn < 0 || !finale.siteLevel.equals(level.dimension())) continue;
			ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
			if (player == null) continue;
			if (--finale.crossIn > 0) continue;
			finale.crossIn = -1;
			cross(player, finale);
		}
	}

	/**
	 * The hidden crossing: the client keeps its last frame of the real tent, the player is carried to the matching
	 * spot in the copy, and his opening scene goes on there from the same tick.
	 */
	private static void cross(ServerPlayer player, Finale finale) {
		ServerLevel chamber = player.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		if (chamber == null) {
			end(player, null);
			return;
		}
		com.vincenthuto.hemomancy.common.network.PacketHandler.sendToPlayer(player,
				new com.vincenthuto.hemomancy.common.network.circus.FinaleCrossingPacket(CROSS_AT));
		Vec3 at = FinaleStage.toCopy(player.position(), finale.siteOrigin, finale.stage, finale.rotation);
		player.stopRiding();
		player.changeDimension(new DimensionTransition(chamber, at, Vec3.ZERO, FinaleStage.yawToCopy(player.getYRot()),
				player.getXRot(), DimensionTransition.DO_NOTHING));
		// His copy is not loaded the instant the player crosses: the opening is handed to it from the Chamber's tick,
		// once it is there, at the tick the scene has reached.
		finale.sceneWaiting = 0;
		Hemomancy.LOGGER.info("Finale of {} crossed at scene tick {}", player.getGameProfile().getName(), CROSS_AT);
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL))
			return;
		for (Map.Entry<UUID, Finale> entry : List.copyOf(FINALES.entrySet())) {
			Finale finale = entry.getValue();
			ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
			if (player == null || finale.over || finale.crossIn >= 0) continue;
			if (!(level.getEntity(finale.ringmaster) instanceof CircusRingmasterEntity copy) || !copy.isAlive()) {
				if (finale.sceneWaiting >= 0) finale.sceneWaiting++;
				// His copy gone from a stage that is loaded is a finale lost.
				if (level.isLoaded(finale.stage) && finale.phase != FinalePhase.CROSSING) end(player, null);
				continue;
			}
			if (finale.sceneWaiting >= 0) {
				com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes.playTo(player, copy,
						com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds.SCENE_CIRCUS_RINGMASTER,
						CROSS_AT + finale.sceneWaiting);
				finale.sceneWaiting = -1;
			}
			finale.phaseTicks++;
			// The stage's chunks load as the player arrives, after begin's sweep: copies a crashed finale left there show up
			// now, and would stand on this stage as a second Ringmaster and carousel. Sweep them while the opening plays.
			if (finale.phase == FinalePhase.CROSSING && finale.phaseTicks % 10 == 1) sweepStrangers(level, finale);
			FinalePhase next = FinaleRules.next(finale.phase, finale.phaseTicks, copy.getHealth() / copy.getMaxHealth());
			if (next != finale.phase) {
				finale.phase = next;
				finale.phaseTicks = 0;
				// He comes down as he truly is: from the reveal to the end his copy wears his true self.
				if (next == FinalePhase.REVEAL) copy.setTrueSelf(true);
				// As his opening ends the real troupe's stand-ins are hauled off on their strings: the show is his now.
				if (next == FinalePhase.RAFTERS) unspoolStandIns(level, finale);
			}
			copy.syncFinale(finale.phase, finale.phaseTicks);
			// His bar comes up as the opening ends (the crossing is the opening) and reads his health to the end.
			if (finale.phase != FinalePhase.CROSSING) {
				if (!copy.bar().viewers().contains(player)) copy.bar().show(player);
				copy.bar().trackHealth(copy);
			}
		}
	}

	/**
	 * The finale's ending is called: on the next server tick (never inside the blow or the channel that called it) the
	 * player is carried home and the ending laid on the real site. Only once.
	 */
	public static void finish(ServerPlayer player, Ending ending) {
		Finale finale = FINALES.get(player.getUUID());
		if (finale == null || finale.over || finale.ending) return;
		finale.ending = true;
		Hemomancy.LOGGER.info("Finale of {} chosen at the kneel: {}", player.getGameProfile().getName(), ending);
		player.getServer().execute(() -> end(player, ending));
	}

	/**
	 * A blow on his copy while he kneels: nothing harms him until the kneel has settled, and then only the owner's own
	 * blow, which kills him (the Liberation). Always taken: his health is held at one.
	 */
	public static boolean kneelBlow(CircusRingmasterEntity copy, net.minecraft.world.damagesource.DamageSource source) {
		var owned = ofCopy(copy);
		if (owned.isEmpty()) return true;
		Finale finale = owned.get().getValue();
		UUID owner = owned.get().getKey();
		// Only a blow the owner deals with their own hand or cast; their arrow in flight or a summon never kills him.
		boolean ownerBlow = FinaleRules.ownBlow(source.getEntity() != null && owner.equals(source.getEntity().getUUID()),
				source.getDirectEntity() != null && owner.equals(source.getDirectEntity().getUUID()));
		if (!FinaleRules.mayKill(finale.phase, finale.phaseTicks, ownerBlow)) return true;
		if (!(copy.level() instanceof ServerLevel level)
				|| !(level.getServer().getPlayerList().getPlayer(owned.get().getKey()) instanceof ServerPlayer player)) return true;
		RingmasterFinaleMoves.say(player, RingmasterFinaleMoves.LINE_KILLED);
		com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects.collapse(level, copy.position());
		finish(player, Ending.KILLED);
		return true;
	}

	/** The blow that would kill his copy brings him to his knees: the kneel holds until the player ends it. */
	public static boolean catchCopy(CircusRingmasterEntity copy, float damage) {
		var owned = ofCopy(copy);
		if (owned.isEmpty()) return false;
		Finale finale = owned.get().getValue();
		if (!FinaleRules.kneelCatches(finale.phase, damage, copy.getHealth())) return false;
		finale.phase = FinalePhase.KNEEL;
		finale.phaseTicks = 0;
		copy.setHealth(1.0F);
		// Whatever was fighting him for the owner (a hound, a bound body) stands down: the kneel is the owner's to end.
		for (net.minecraft.world.entity.Mob mob : copy.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
				copy.getBoundingBox().inflate(32.0D), mob -> mob.getTarget() == copy))
			mob.setTarget(null);
		copy.syncFinale(finale.phase, finale.phaseTicks);
		return true;
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && isInFinale(player)) end(player, null);
	}

	/**
	 * A login after a crash or a stop mid-finale: the finale is gone, so the player goes home, the real site it held is
	 * released for them, and the stage it laid (blocks and copies) is cleared.
	 */
	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !player.getPersistentData().contains(ACTIVE_KEY)) return;
		net.minecraft.nbt.Tag active = player.getPersistentData().get(ACTIVE_KEY);
		player.getPersistentData().remove(ACTIVE_KEY);
		if (isInFinale(player)) return;
		if (player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL))
			ChamberOfWillManager.get(player.getServer()).exitChamber(player);
		if (!(active instanceof net.minecraft.nbt.CompoundTag tag)) return;
		net.minecraft.resources.ResourceLocation levelId = net.minecraft.resources.ResourceLocation.tryParse(tag.getString("Level"));
		BlockPos origin = BlockPos.of(tag.getLong("Origin"));
		Rotation rotation = Rotation.values()[Math.floorMod(tag.getInt("Rotation"), Rotation.values().length)];
		BlockPos stage = BlockPos.of(tag.getLong("Stage"));
		ServerLevel siteLevel = levelId == null ? null
				: player.getServer().getLevel(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, levelId));
		if (siteLevel != null) CircusPavilionSavedData.get(siteLevel).reset(siteLevel, origin, player.getUUID());
		ServerLevel chamber = player.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		if (chamber != null) {
			sweep(chamber, stage);
			FinaleStage.clear(chamber, stage, rotation);
		}
		Hemomancy.LOGGER.info("Finale of {} recovered at login: site {} released, stage {} cleared",
				player.getGameProfile().getName(), origin, stage);
	}

	/**
	 * A crossed player who has left his Chamber by any other way (a teleport, a command, a visit's exit) for
	 * {@link RingmasterFinaleDirectorRules#AWAY_LIMIT} ticks has left the finale: it breaks off. The director's own
	 * return at {@link #end} removes the finale first, so it never counts.
	 */
	@SubscribeEvent
	public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		Finale finale = FINALES.get(player.getUUID());
		if (finale == null || finale.over || finale.ending) return;
		finale.away = RingmasterFinaleDirectorRules.awayTicks(finale.away, finale.crossIn < 0,
				player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL));
		if (RingmasterFinaleDirectorRules.strandedAway(finale.away)) {
			Hemomancy.LOGGER.info("Finale of {} broken off: out of his Chamber for {} ticks", player.getGameProfile().getName(),
					finale.away);
			end(player, null);
		}
	}

	/** Hauls the real troupe's stand-ins off the stage on their strings. */
	private static void unspoolStandIns(ServerLevel chamber, Finale finale) {
		for (UUID id : finale.standIns)
			if (chamber.getEntity(id) instanceof net.minecraft.world.entity.Mob standIn)
				com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetStrings.unspool(standIn);
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && isInFinale(player)) end(player, null);
	}

	/** Removes copies on {@code finale}'s stage that are not its own: a crashed finale's, loaded late. */
	private static void sweepStrangers(ServerLevel chamber, Finale finale) {
		AABB box = new AABB(finale.stage).inflate(48.0D, 24.0D, 48.0D);
		// No real carousel or Ringmaster stands in the Chamber: any but this finale's is a stray (an older stage laid the
		// template's own carousel with its blocks).
		for (Entity entity : chamber.getEntities((Entity) null, box, e -> (e.getTags().contains(COPY_TAG)
				|| e instanceof CircusCarouselEntity || e instanceof CircusRingmasterEntity)
				&& !e.getUUID().equals(finale.ringmaster) && !e.getUUID().equals(finale.carousel)
				&& !finale.standIns.contains(e.getUUID()))) {
			List.copyOf(entity.getPassengers()).forEach(Entity::discard);
			entity.discard();
		}
	}

	/** Removes every copy raised on a stage, a crashed finale's included. */
	private static void sweep(ServerLevel chamber, BlockPos stage) {
		AABB box = new AABB(stage).inflate(48.0D, 24.0D, 48.0D);
		for (Entity entity : chamber.getEntities((Entity) null, box, e -> e.getTags().contains(COPY_TAG))) {
			// The carousel's captives ride it untagged.
			List.copyOf(entity.getPassengers()).forEach(Entity::discard);
			entity.discard();
		}
	}

	public static void clearSessionState() {
		FINALES.clear();
	}
}
