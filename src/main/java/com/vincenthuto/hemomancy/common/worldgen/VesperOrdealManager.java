package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTwoCombat;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWingedFlightRules;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncVesperFightScene;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Optional;
import java.util.UUID;

/** Owns the isolated, retryable 50x50 Vesper refusal arena. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class VesperOrdealManager {
	private static final String ACTIVE_BLOOM_KEY = Hemomancy.MOD_ID + ":vesper_ordeal_bloom";
	private static final String ACTIVE_BLOOM_ID_KEY = Hemomancy.MOD_ID + ":vesper_ordeal_bloom_id";
	private static final String PENDING_MEMORY_KEY = Hemomancy.MOD_ID + ":vesper_memory_pending";
	private static final int ARENA_X = 4096;
	private static final int ARENA_SPACING = 128;
	private static final int ARENA_HALF = 25;
	private static final int WALL_HEIGHT = 5;

	private VesperOrdealManager() {
	}

	public static boolean enter(ServerPlayer player, QliphothBloomSavedData.BloomEntry bloom) {
		if (ChamberVisitService.isProtected(player)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.chamber_visit.no_ordeals"), true);
			return false;
		}
		ServerLevel arenaLevel = player.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		if (arenaLevel == null) {
			player.displayClientMessage(Component.literal("The refusal has no place to open. The wound remains.")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
			return false;
		}
		BlockPos center = arenaCenter(player);
		ensureArena(arenaLevel, center);
		clearOwnedVespers(arenaLevel, player.getUUID(), center);
		ChamberOfWillManager.get(player.getServer()).rememberReturnPoint(player);
		player.getPersistentData().putLong(ACTIVE_BLOOM_KEY, bloom.center().asLong());
		player.getPersistentData().putUUID(ACTIVE_BLOOM_ID_KEY, bloom.bloomId());
		Vec3 destination = new Vec3(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 12.5);
		player.stopRiding();
		player.changeDimension(new DimensionTransition(arenaLevel, destination, Vec3.ZERO,
				180.0F, 0.0F, DimensionTransition.DO_NOTHING));
		PacketHandler.sendToPlayer(player, PacketSyncVesperFightScene.activate(center));
		spawnCrownedRefusal(arenaLevel, player, bloom.center().asLong(), bloom.bloomId(), center);
		player.displayClientMessage(Component.literal(
				"Strike the exposed throne anchors when Vesper leaves them vulnerable. After the Evening Star falls, use Blood Absorption on the downed body to finish the refusal. If you die, the unsealed wound remains your route back.")
				.withStyle(ChatFormatting.DARK_PURPLE), false);
		return true;
	}

	public static boolean tickArenaPlayer(ServerPlayer player, ServerLevel level) {
		if (!player.getPersistentData().contains(ACTIVE_BLOOM_KEY)) return false;
		if (!hasValidActiveBloom(player)) {
			player.displayClientMessage(Component.literal(
					"The wound that opened this refusal is gone. Return to an open Qliphoth to try again.")
					.withStyle(ChatFormatting.DARK_PURPLE), false);
			if (player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
				ChamberOfWillManager.get(player.getServer()).exitChamber(player);
			} else {
				abandonAttempt(player);
			}
			return true;
		}
		BlockPos center = arenaCenter(player);
		reconcileArenaBoss(level, player, center);
		return true;
	}

	public static boolean isActive(ServerPlayer player) {
		return player.getPersistentData().contains(ACTIVE_BLOOM_KEY);
	}

	public static boolean hasValidActiveBloom(ServerPlayer player) {
		if (!isActive(player)) return false;
		var attempt = player.getPersistentData();
		QliphothBloomSavedData blooms = QliphothBloomSavedData.get(player.getServer().overworld());
		BlockPos center = BlockPos.of(attempt.getLong(ACTIVE_BLOOM_KEY));
		QliphothBloomSavedData.BloomEntry bloom;
		if (attempt.hasUUID(ACTIVE_BLOOM_ID_KEY)) {
			bloom = blooms.getBloomById(attempt.getUUID(ACTIVE_BLOOM_ID_KEY));
		} else if (attempt.contains(ACTIVE_BLOOM_ID_KEY)) {
			return false;
		} else {
			bloom = uniqueLegacyBloom(blooms, center, player.getUUID());
		}
		return bloom != null && bloom.center().equals(center)
				&& bloom.ownerUUID().equals(player.getUUID())
				&& blooms.getState(bloom).isPortalOpen()
				&& HemoCapabilityAccess.getInitiatoryDegree(player)
						.map(degree -> degree.getDegreeNumber() == 7
								&& degree.getArchonPath() == EnumArchonPath.SILENT_PENDING)
						.orElse(false);
	}

	/** Ends an attempt without changing the severed Bloom, so its owner can retry. */
	public static void abandonAttempt(ServerPlayer player) {
		if (!isActive(player)) return;
		PacketHandler.sendToPlayer(player, PacketSyncVesperFightScene.clearScene());
		ServerLevel arena = player.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		if (arena != null) clearOwnedVespers(arena, player.getUUID(), arenaCenter(player));
		player.getPersistentData().remove(ACTIVE_BLOOM_KEY);
		player.getPersistentData().remove(ACTIVE_BLOOM_ID_KEY);
	}

	public static void completeVictory(VesperTheEveningStarEntity vesper) {
		if (!(vesper.level() instanceof ServerLevel level) || vesper.getOrdealOwner() == null) return;
		ServerPlayer owner = level.getServer().getPlayerList().getPlayer(vesper.getOrdealOwner());
		if (owner == null || !owner.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return;
		long bloomOrigin = vesper.getBloomOrigin();
		if (!matchesActiveBloom(owner, bloomOrigin, vesper.getBloomId())) return;

		QliphothBloomSavedData blooms = QliphothBloomSavedData.get(level.getServer().overworld());
		BlockPos bloomPos = BlockPos.of(bloomOrigin);
		QliphothBloomSavedData.BloomEntry bloom = vesper.getBloomId() == null
				? uniqueLegacyBloom(blooms, bloomPos, owner.getUUID())
				: blooms.getBloomById(vesper.getBloomId());
		if (bloom == null || !bloom.center().equals(bloomPos)
				|| !bloom.ownerUUID().equals(owner.getUUID())
				|| !blooms.getState(bloom).isPortalOpen()) return;
		boolean eligibleRefusal = HemoCapabilityAccess.getInitiatoryDegree(owner)
				.map(degree -> degree.getDegreeNumber() == 7
						&& degree.getArchonPath() == EnumArchonPath.SILENT_PENDING)
				.orElse(false);
		if (!eligibleRefusal) return;

		boolean firstVictory = !HarbingerAdvancementGranter.hasAdvancement(owner,
				HarbingerAdvancementGranter.ADV_VESPER_DEFEATED);
		HemoCapabilityAccess.getInitiatoryDegree(owner).ifPresent(degree -> {
			if (degree.getDegreeNumber() == 7 && degree.getArchonPath() == EnumArchonPath.SILENT_PENDING) {
				degree.setArchonPath(EnumArchonPath.SILENT_ARCHON);
				InitiatoryDegreeEvents.syncDegree(owner, degree);
			}
		});
		blooms.sealBloom(bloom);
		HarbingerAdvancementGranter.grantIfNotDone(owner, HarbingerAdvancementGranter.ADV_VESPER_DEFEATED);
		if (firstVictory) {
			var persisted = memoryClaims(owner);
			persisted.putBoolean(PENDING_MEMORY_KEY, true);
			owner.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
		}
		PacketHandler.sendToPlayer(owner, PacketSyncVesperFightScene.clearScene());
		owner.getPersistentData().remove(ACTIVE_BLOOM_KEY);
		owner.getPersistentData().remove(ACTIVE_BLOOM_ID_KEY);
		HarbingerCardinalRiteEvents.syncQliphothBlooms(level.getServer());
		owner.displayClientMessage(Component.literal(
				"The Evening Star breaks. Your refusal holds, and the wound seals behind the name Silent Archon.")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
		ChamberOfWillManager.get(level.getServer()).exitChamber(owner);
		givePendingMemory(owner);
		if (owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(PENDING_MEMORY_KEY)) {
			owner.displayClientMessage(Component.literal("Make room in your inventory to receive the Memory of Vesper."), false);
		}
	}

	public static void copyOrdeal(VesperTheCrownedRefusalEntity from, VesperTheEveningStarEntity to) {
		to.setOrdeal(from.getOrdealOwner(), from.getBloomOrigin(), from.getBloomId());
	}

	private static boolean matchesActiveBloom(ServerPlayer owner, long origin, UUID bloomId) {
		var data = owner.getPersistentData();
		if (!data.contains(ACTIVE_BLOOM_KEY) || data.getLong(ACTIVE_BLOOM_KEY) != origin) return false;
		return !data.hasUUID(ACTIVE_BLOOM_ID_KEY) || (bloomId != null
				&& data.getUUID(ACTIVE_BLOOM_ID_KEY).equals(bloomId));
	}

	private static QliphothBloomSavedData.BloomEntry uniqueLegacyBloom(QliphothBloomSavedData blooms,
			BlockPos center, UUID owner) {
		var matches = blooms.getBlooms().stream()
				.filter(entry -> entry.migratesLegacyProgress()
						&& entry.center().equals(center) && entry.ownerUUID().equals(owner))
				.limit(2).toList();
		return matches.size() == 1 ? matches.getFirst() : null;
	}

	/** Returns strict ordeal bounds for owned bosses or persisted local bounds for command summons. */
	public static Optional<FlightArena> flightArena(VesperTheCrownedRefusalEntity vesper) {
		if (!(vesper.level() instanceof ServerLevel level)) return Optional.empty();
		if (vesper.getOrdealOwner() == null) {
			vesper.ensureSummonedFlightArena();
			Optional<FlightArena> summoned = vesper.summonedFlightArena();
			return VesperWingedFlightRules.arenaAuthority(false, false, summoned.isPresent())
					== VesperWingedFlightRules.ArenaAuthority.SUMMONED ? summoned : Optional.empty();
		}
		ServerPlayer owner = level.getServer().getPlayerList().getPlayer(vesper.getOrdealOwner());
		if (owner == null || owner.level() != vesper.level() || !isActive(owner)) return Optional.empty();
		if (!matchesActiveBloom(owner, vesper.getBloomOrigin(), vesper.getBloomId())) return Optional.empty();
		BlockPos center = arenaCenter(owner);
		if (!new AABB(center).inflate(ARENA_HALF, 12.0D, ARENA_HALF).contains(vesper.position())) {
			return Optional.empty();
		}
		return Optional.of(new FlightArena(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D));
	}

	public record FlightArena(double centerX, double floorY, double centerZ) {
	}

	private static void givePendingMemory(ServerPlayer owner) {
		var persisted = memoryClaims(owner);
		if (!owner.isAlive() || ChamberVisitService.isObservational(owner) || !persisted.getBoolean(PENDING_MEMORY_KEY)) return;
		var memory = ItemInit.memory_of_vesper.get();
		int before = owner.getInventory().countItem(memory);
		owner.getInventory().add(new ItemStack(memory));
		if (owner.getInventory().countItem(memory) > before) {
			persisted.remove(PENDING_MEMORY_KEY);
			owner.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
		}
	}

	private static CompoundTag memoryClaims(ServerPlayer owner) {
		var data = owner.getPersistentData();
		var persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
		// Older earned claims lived outside the data vanilla copies on death.
		if (data.getBoolean(PENDING_MEMORY_KEY)) {
			persisted.putBoolean(PENDING_MEMORY_KEY, true);
			data.put(Player.PERSISTED_NBT_TAG, persisted);
		}
		data.remove(PENDING_MEMORY_KEY);
		return persisted;
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) {
			givePendingMemory(player);
		}
	}

	private static void spawnCrownedRefusal(ServerLevel level, ServerPlayer owner, long bloomOrigin,
			UUID bloomId, BlockPos center) {
		VesperTheCrownedRefusalEntity vesper = EntityInit.vesper_crowned_refusal.get().create(level);
		if (vesper == null) return;
		vesper.setOrdeal(owner.getUUID(), bloomOrigin, bloomId);
		vesper.moveTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() - 8.5, 0.0F, 0.0F);
		vesper.setTarget(owner);
		level.addFreshEntity(vesper);
	}

	private static void reconcileArenaBoss(ServerLevel level, ServerPlayer owner, BlockPos center) {
		Entity ownedVesper = findOwnedVesper(level, owner, center);
		VesperOrdealRecoveryRules.Action action = VesperOrdealRecoveryRules.reconnectAction(
				isActive(owner), owner.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL),
				ownedVesper != null);
		switch (action) {
			case RETARGET -> retargetOwnedVesper(ownedVesper, owner);
			case RESPAWN -> {
				clearOwnedVespers(level, owner.getUUID(), center);
				spawnCrownedRefusal(level, owner,
						owner.getPersistentData().getLong(ACTIVE_BLOOM_KEY),
						owner.getPersistentData().hasUUID(ACTIVE_BLOOM_ID_KEY)
								? owner.getPersistentData().getUUID(ACTIVE_BLOOM_ID_KEY) : null, center);
			}
			default -> {
			}
		}
	}

	static Entity findOwnedVesper(ServerLevel level, ServerPlayer owner, BlockPos center) {
		AABB bounds = new AABB(center).inflate(ARENA_HALF + 4, 12, ARENA_HALF + 4);
		Entity retained = null;
		for (Entity entity : level.getEntities(null, bounds)) {
			boolean matching;
			if (entity instanceof VesperTheCrownedRefusalEntity crowned
					&& owner.getUUID().equals(crowned.getOrdealOwner())) {
				matching = matchesActiveBloom(owner, crowned.getBloomOrigin(), crowned.getBloomId());
			} else if (entity instanceof VesperTheEveningStarEntity evening
					&& owner.getUUID().equals(evening.getOrdealOwner())) {
				matching = matchesActiveBloom(owner, evening.getBloomOrigin(), evening.getBloomId());
			} else {
				continue;
			}
			if (!matching) {
				retireDuplicate(entity);
				continue;
			}
			if (retained == null) {
				retained = entity;
			} else if (preferRecoveredBoss(entity, retained)) {
				retireDuplicate(retained);
				retained = entity;
			} else {
				retireDuplicate(entity);
			}
		}
		return retained;
	}

	private static boolean preferRecoveredBoss(Entity candidate, Entity retained) {
		if (candidate instanceof VesperTheEveningStarEntity
				&& retained instanceof VesperTheCrownedRefusalEntity) return true;
		if (candidate instanceof VesperTheCrownedRefusalEntity
				&& retained instanceof VesperTheEveningStarEntity) return false;
		return candidate.tickCount > retained.tickCount;
	}

	private static void retireDuplicate(Entity entity) {
		if (entity instanceof VesperTheEveningStarEntity evening) VesperPhaseTwoCombat.cancel(evening);
		entity.discard();
	}

	private static void retargetOwnedVesper(Entity entity, ServerPlayer owner) {
		if (entity instanceof VesperTheCrownedRefusalEntity crowned && crowned.isAlive()) {
			crowned.setTarget(owner);
		} else if (entity instanceof VesperTheEveningStarEntity evening && evening.isAlive()) {
			evening.setTarget(owner);
		}
	}

	static BlockPos arenaCenter(ServerPlayer player) {
		int id = ChamberOfWillManager.get(player.getServer()).idFor(player.getUUID());
		return new BlockPos(ARENA_X, ChamberOfWillManager.FLOOR_Y, id * ARENA_SPACING);
	}

	private static void ensureArena(ServerLevel level, BlockPos center) {
		for (int x = -ARENA_HALF; x < ARENA_HALF; x++) {
			for (int z = -ARENA_HALF; z < ARENA_HALF; z++) {
				level.setBlock(center.offset(x, 0, z), Blocks.BARRIER.defaultBlockState(), 2);
				for (int y = 1; y <= WALL_HEIGHT; y++) {
					BlockPos air = center.offset(x, y, z);
					if ((Math.abs(x) == ARENA_HALF - 1 || Math.abs(z) == ARENA_HALF - 1)) {
						level.setBlock(air, Blocks.BARRIER.defaultBlockState(), 2);
					} else if (!level.getBlockState(air).isAir()) {
						level.setBlock(air, Blocks.AIR.defaultBlockState(), 2);
					}
				}
			}
		}
	}

	private static void clearOwnedVespers(ServerLevel level, UUID owner, BlockPos center) {
		AABB bounds = new AABB(center).inflate(ARENA_HALF + 4, 12, ARENA_HALF + 4);
		for (Entity entity : level.getEntities(null, bounds)) {
			if (entity instanceof VesperTheCrownedRefusalEntity crowned && owner.equals(crowned.getOrdealOwner())) {
				crowned.discard();
			} else if (entity instanceof VesperTheEveningStarEntity evening && owner.equals(evening.getOrdealOwner())) {
				VesperPhaseTwoCombat.cancel(evening);
				evening.discard();
			} else if (entity.getPersistentData().getBoolean("HemomancyVesperEncounterPuppet")) {
				entity.discard();
			}
		}
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		boolean inChamber = player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL);
		if (isActive(player) && inChamber && !hasValidActiveBloom(player)) {
			PacketHandler.sendToPlayer(player, PacketSyncVesperFightScene.clearScene());
			givePendingMemory(player);
			return;
		}
		VesperOrdealRecoveryRules.Action action = VesperOrdealRecoveryRules.reconnectAction(
				isActive(player), inChamber, false);
		if (action == VesperOrdealRecoveryRules.Action.ABANDON) {
			abandonAttempt(player);
		} else if (action == VesperOrdealRecoveryRules.Action.RESPAWN) {
			ServerLevel arena = (ServerLevel) player.level();
			BlockPos center = arenaCenter(player);
			ensureArena(arena, center);
			PacketHandler.sendToPlayer(player, PacketSyncVesperFightScene.activate(center));
			reconcileArenaBoss(arena, player, center);
		}
		givePendingMemory(player);
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		memoryClaims(player);
		if (!player.getPersistentData().contains(ACTIVE_BLOOM_KEY)) return;
		abandonAttempt(player);
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !isActive(player)) return;
		if (!event.getTo().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
			abandonAttempt(player);
		}
	}
}
