package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.UUID;

/** Owns the isolated, retryable 50x50 Vesper refusal arena. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class VesperOrdealManager {
	private static final String ACTIVE_BLOOM_KEY = Hemomancy.MOD_ID + ":vesper_ordeal_bloom";
	private static final String PENDING_MEMORY_KEY = Hemomancy.MOD_ID + ":vesper_memory_pending";
	private static final int ARENA_X = 4096;
	private static final int ARENA_SPACING = 128;
	private static final int ARENA_HALF = 25;
	private static final int WALL_HEIGHT = 5;

	private VesperOrdealManager() {
	}

	public static boolean enter(ServerPlayer player, QliphothBloomSavedData.BloomEntry bloom) {
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
		Vec3 destination = new Vec3(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 12.5);
		player.stopRiding();
		player.changeDimension(new DimensionTransition(arenaLevel, destination, Vec3.ZERO,
				180.0F, 0.0F, DimensionTransition.DO_NOTHING));
		spawnCrownedRefusal(arenaLevel, player, bloom.center().asLong(), center);
		return true;
	}

	public static boolean tickArenaPlayer(ServerPlayer player, ServerLevel level) {
		if (!player.getPersistentData().contains(ACTIVE_BLOOM_KEY)) return false;
		BlockPos center = arenaCenter(player);
		int bound = ARENA_HALF - 1;
		if (Math.abs(player.getX() - center.getX()) > bound
				|| Math.abs(player.getZ() - center.getZ()) > bound
				|| player.getY() < center.getY() - 2) {
			player.teleportTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 12.5);
			player.resetFallDistance();
		}
		return true;
	}

	public static boolean isActive(ServerPlayer player) {
		return player.getPersistentData().contains(ACTIVE_BLOOM_KEY);
	}

	/** Ends an attempt without changing the severed Bloom, so its owner can retry. */
	public static void abandonAttempt(ServerPlayer player) {
		if (!isActive(player)) return;
		ServerLevel arena = player.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		if (arena != null) clearOwnedVespers(arena, player.getUUID(), arenaCenter(player));
		player.getPersistentData().remove(ACTIVE_BLOOM_KEY);
	}

	public static void completeVictory(VesperTheEveningStarEntity vesper) {
		if (!(vesper.level() instanceof ServerLevel level) || vesper.getOrdealOwner() == null) return;
		ServerPlayer owner = level.getServer().getPlayerList().getPlayer(vesper.getOrdealOwner());
		if (owner == null || !owner.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return;
		long bloomOrigin = vesper.getBloomOrigin();
		if (owner.getPersistentData().getLong(ACTIVE_BLOOM_KEY) != bloomOrigin) return;

		QliphothBloomSavedData blooms = QliphothBloomSavedData.get(level.getServer().overworld());
		BlockPos bloomPos = BlockPos.of(bloomOrigin);
		QliphothBloomSavedData.BloomEntry bloom = blooms.getBlooms().stream()
				.filter(entry -> entry.center().equals(bloomPos)
						&& entry.ownerUUID().equals(owner.getUUID()))
				.findFirst().orElse(null);
		if (bloom == null || !blooms.getState(bloomPos).isPortalOpen()) return;
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
		blooms.sealBloom(bloomPos);
		HarbingerAdvancementGranter.grantIfNotDone(owner, HarbingerAdvancementGranter.ADV_VESPER_DEFEATED);
		if (firstVictory) owner.getPersistentData().putBoolean(PENDING_MEMORY_KEY, true);
		owner.getPersistentData().remove(ACTIVE_BLOOM_KEY);
		HarbingerCardinalRiteEvents.syncQliphothBlooms(level.getServer());
		owner.displayClientMessage(Component.literal(
				"The Evening Star breaks. Your refusal holds, and the wound seals behind the name Silent Archon.")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
		ChamberOfWillManager.get(level.getServer()).exitChamber(owner);
		givePendingMemory(owner);
	}

	public static void copyOrdeal(VesperTheCrownedRefusalEntity from, VesperTheEveningStarEntity to) {
		to.setOrdeal(from.getOrdealOwner(), from.getBloomOrigin());
	}

	private static void givePendingMemory(ServerPlayer owner) {
		if (!owner.getPersistentData().getBoolean(PENDING_MEMORY_KEY)) return;
		ItemStack memory = new ItemStack(ItemInit.memory_of_vesper.get());
		if (owner.getInventory().add(memory)) {
			owner.getPersistentData().remove(PENDING_MEMORY_KEY);
			return;
		}
		ItemEntity drop = new ItemEntity(owner.level(), owner.getX(), owner.getY() + 0.5, owner.getZ(), memory);
		drop.setExtendedLifetime();
		drop.setInvulnerable(true);
		if (owner.level().addFreshEntity(drop)) {
			owner.getPersistentData().remove(PENDING_MEMORY_KEY);
		}
	}

	private static void spawnCrownedRefusal(ServerLevel level, ServerPlayer owner, long bloomOrigin, BlockPos center) {
		VesperTheCrownedRefusalEntity vesper = EntityInit.vesper_crowned_refusal.get().create(level);
		if (vesper == null) return;
		vesper.setOrdeal(owner.getUUID(), bloomOrigin);
		vesper.moveTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() - 8.5, 0.0F, 0.0F);
		vesper.setTarget(owner);
		level.addFreshEntity(vesper);
	}

	private static BlockPos arenaCenter(ServerPlayer player) {
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
				evening.discard();
			}
		}
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)
				|| !player.getPersistentData().contains(ACTIVE_BLOOM_KEY)) return;
		abandonAttempt(player);
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (isActive(player)) {
			abandonAttempt(player);
			if (player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
				ChamberOfWillManager.get(player.getServer()).exitChamber(player);
			}
		}
		givePendingMemory(player);
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)
				|| !player.getPersistentData().contains(ACTIVE_BLOOM_KEY)) return;
		abandonAttempt(player);
	}
}
