package com.vincenthuto.hemomancy.common.encounter;

import com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterRules;
import com.vincenthuto.hemomancy.common.worldgen.MycophantNurseryLayout;
import com.vincenthuto.hemomancy.common.capability.PendingDelivery;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketMycophantIntro;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncMycophantFightScene;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

/** Owns the Apotheos hunt, local claim, and solo Morphic Nursery ordeal. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MycophantEncounterManager {
	private static final String ACTIVE_KEY = Hemomancy.MOD_ID + ":mycophant_active";
	private static final String REMATCH_KEY = Hemomancy.MOD_ID + ":mycophant_rematch";
	private static final String CLAIM_KEY = Hemomancy.MOD_ID + ":mycophant_claim_ticks";
	private static final String TENDRIL_PENDING_KEY = Hemomancy.MOD_ID + ":mycophant_tendril_pending";
	/** The first defeat's unique Tendril, retried until it really lands. Repeat rewards drop as before. */
	public static final PendingDelivery.Claim TENDRIL = new PendingDelivery.Claim("mycophant_tendril",
			player -> player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(TENDRIL_PENDING_KEY)
					? List.of(new ItemStack(ItemInit.mycophant_tendril.get())) : List.of(),
			player -> true,
			(player, left) -> {
				if (!left.isEmpty()) return;
				var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
				persisted.remove(TENDRIL_PENDING_KEY);
				player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
			});
	/** Persisted once the hive has yielded to this player: the succession is theirs. */
	public static final String HEIR_KEY = Hemomancy.MOD_ID + ":mycophant_heir";
	private static final int CEILING_Y = MycophantNurseryLayout.CEILING_Y;
	private static final int CLAIM_TICKS = 100;
	private static final int ARENA_X = 8192;
	private static final int ARENA_SPACING = 128;
	private static final int HALF = MycophantNurseryLayout.HALF;

	private MycophantEncounterManager() {}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (player.getPersistentData().contains(CLAIM_KEY)) tickClaim(player);
		else tickGardensPlayer(player);
	}

	public static void tickGardensPlayer(ServerPlayer player) {
		if (isActive(player) || !player.level().dimension().equals(FungalGardenTravelHelper.FUNGAL_GARDENS)) return;
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		if (degree.getMycophantRetryCooldownTicks() > 0 && player.isAlive()
				&& !player.isCreative() && !player.isSpectator())
			degree.setMycophantRetryCooldownTicks(degree.getMycophantRetryCooldownTicks() - 1);
		if (degree.getMycophantRetryCooldownTicks() > 0) return;
		if (!MycophantEncounterRules.shouldAccumulate(degree.getDegreeNumber(), degree.getArchonPath(),
				degree.isMycophantDefeated(), true, player.isAlive(), player.isCreative(), player.isSpectator(), false)) return;
		int duration = huntDuration();
		int old = degree.getMycophantExposureTicks();
		int next = Math.min(duration, old + 1);
		degree.setMycophantExposureTicks(next);
		var oldStage = MycophantEncounterRules.huntStage(old, duration);
		var stage = MycophantEncounterRules.huntStage(next, duration);
		if (stage != oldStage) announce(player, stage);
		cue(player, stage);
		if (stage == MycophantEncounterRules.HuntStage.CLAIM) beginClaim(player, false);
		if (player.tickCount % 20 == 0) InitiatoryDegreeEvents.syncDegree(player, degree);
	}

	public static void beginClaim(ServerPlayer player, boolean rematch) {
		if (isActive(player) || player.getPersistentData().contains(CLAIM_KEY)
				|| ChamberVisitService.isProtected(player)) return;
		player.getPersistentData().putInt(CLAIM_KEY, CLAIM_TICKS);
		player.getPersistentData().putBoolean(REMATCH_KEY, rematch);
		player.displayClientMessage(Component.translatable("message.hemomancy.mycophant.claim")
				.withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC), true);
		player.level().playSound(null, player.blockPosition(), SoundInit.ENTITY_MYCOPHANT_SUMMON.get(),
				SoundSource.HOSTILE, 1.2F, 0.65F);
		// the same seed lays the same hyphae across the view at the capture and at the waking
		PacketHandler.sendToPlayer(player, new PacketMycophantIntro(PacketMycophantIntro.CAPTURE, sceneSeed(player)));
	}

	/**
	 * The root claim: the player is held where they stand while hyphae close over their view (client side) and roots
	 * of spores spiral in around them and climb, until they are pulled under into the Nursery.
	 */
	private static void tickClaim(ServerPlayer player) {
		int left = player.getPersistentData().getInt(CLAIM_KEY) - 1;
		player.getPersistentData().putInt(CLAIM_KEY, left);
		player.setDeltaMovement(0.0D, Math.min(0.0D, player.getDeltaMovement().y), 0.0D);
		player.hurtMarked = true;
		player.resetFallDistance();
		if (left % 3 == 0 && player.level() instanceof ServerLevel level) {
			float closing = 1.0F - left / (float) CLAIM_TICKS;
			double radius = 0.7D + (1.0F - closing) * 3.6D;
			int roots = 5;
			for (int i = 0; i < roots; i++) {
				double angle = Math.PI * 2.0D * i / roots + closing * 4.0D;
				double height = 0.1D + closing * 1.6D;
				level.sendParticles(HemoParticleData.sporiticSpore(new ParticleColor(0.48F, 0.72F, 0.34F)),
						player.getX() + Math.cos(angle) * radius, player.getY() + height,
						player.getZ() + Math.sin(angle) * radius, 1, 0.03D, 0.06D, 0.03D, 0.0D);
				level.sendParticles(HemoParticleData.sporiticSpore(new ParticleColor(0.92F, 0.66F, 0.24F)),
						player.getX() + Math.cos(angle + 0.3D) * radius * 0.9D, player.getY() + height * 0.6D,
						player.getZ() + Math.sin(angle + 0.3D) * radius * 0.9D, 1, 0.02D, 0.03D, 0.02D, 0.0D);
			}
		}
		if (left == CLAIM_TICKS / 2)
			player.level().playSound(null, player.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 1.4F, 0.55F);
		if (left <= 0) enterNursery(player);
	}

	private static int sceneSeed(ServerPlayer player) {
		return player.getUUID().hashCode();
	}

	public static boolean enterNursery(ServerPlayer player) {
		if (ChamberVisitService.isProtected(player)) {
			player.getPersistentData().remove(CLAIM_KEY);
			PacketHandler.sendToPlayer(player, new PacketMycophantIntro(PacketMycophantIntro.CLEAR, 0));
			player.displayClientMessage(Component.translatable("message.hemomancy.chamber_visit.no_ordeals"), true);
			return false;
		}
		ServerLevel arena = player.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		if (arena == null) return false;
		BlockPos center = arenaCenter(player);
		buildNursery(arena, center);
		clearOwnedBoss(arena, player.getUUID(), center);
		ChamberOfWillManager.get(player.getServer()).rememberReturnPoint(player);
		player.getPersistentData().putBoolean(ACTIVE_KEY, true);
		player.getPersistentData().remove(CLAIM_KEY);
		boolean rematch = player.getPersistentData().getBoolean(REMATCH_KEY);
		player.stopRiding();
		// the candidate wakes behind her, about eleven blocks away, while she tends the brood with her back turned
		player.changeDimension(new DimensionTransition(arena,
				new Vec3(center.getX() + 0.5, center.getY() + 2.1, center.getZ() - 11.5),
				Vec3.ZERO, 0.0F, 0.0F, DimensionTransition.DO_NOTHING));
		PacketHandler.sendToPlayer(player, PacketSyncMycophantFightScene.activate(center));
		MycophantEntity boss = spawnBoss(arena, player, center);
		if (boss != null) boss.setRematchIntro(rematch);
		PacketHandler.sendToPlayer(player, new PacketMycophantIntro(rematch ? PacketMycophantIntro.REMATCH
				: PacketMycophantIntro.WAKING, sceneSeed(player)));
		return true;
	}

	public static boolean tickArenaPlayer(ServerPlayer player, ServerLevel level) {
		if (!isActive(player)) return false;
		BlockPos center = arenaCenter(player);
		MycophantEntity boss = findOwnedBoss(level, player.getUUID(), center);
		if (boss == null) {
			boss = spawnBoss(level, player, center);
			// a boss respawned after a reload resumes the fight with the short rematch opening, never the full waking
			if (boss != null) boss.setRematchIntro(true);
		}
		else boss.setTarget(player);
		return true;
	}

	public static boolean isActive(ServerPlayer player) {
		return player.getPersistentData().getBoolean(ACTIVE_KEY);
	}

	public static void failAttempt(ServerPlayer player) {
		if (!isActive(player)) return;
		boolean rematch = player.getPersistentData().getBoolean(REMATCH_KEY);
		ServerLevel arena = player.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		if (arena != null) clearOwnedBoss(arena, player.getUUID(), arenaCenter(player));
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		degree.setMycophantRetryCooldownTicks(retryCooldown());
		if (!rematch && !degree.isMycophantDefeated()) degree.setMycophantExposureTicks(huntDuration());
		InitiatoryDegreeEvents.syncDegree(player, degree);
		clear(player);
		player.setHealth(Math.max(1.0F, player.getMaxHealth() * 0.35F));
		player.removeAllEffects();
		player.displayClientMessage(Component.translatable("message.hemomancy.mycophant.expelled")
				.withStyle(ChatFormatting.DARK_GREEN), false);
		ChamberOfWillManager.get(player.getServer()).exitChamber(player);
	}

	public static void completeVictory(MycophantEntity boss) {
		if (!(boss.level() instanceof ServerLevel level) || boss.getEncounterOwner() == null) return;
		ServerPlayer owner = level.getServer().getPlayerList().getPlayer(boss.getEncounterOwner());
		if (owner == null || !isActive(owner)) return;
		boolean first = !HemoCapabilityAccess.getInitiatoryDegree(owner).isMycophantDefeated();
		var degree = HemoCapabilityAccess.getInitiatoryDegree(owner);
		degree.setMycophantDefeated(true);
		degree.setMycophantExposureTicks(huntDuration());
		degree.setMycophantRetryCooldownTicks(0);
		InitiatoryDegreeEvents.syncDegree(owner, degree);
		clear(owner);
		ChamberOfWillManager.get(level.getServer()).exitChamber(owner);
		if (first) {
			var persisted = owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
			persisted.putBoolean(TENDRIL_PENDING_KEY, true);
			owner.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
			givePendingTendril(owner);
		}
		else {
			give(owner, new ItemStack(ItemInit.sanguine_quintessence.get()));
			give(owner, new ItemStack(ItemInit.spore_sac.get(), 2 + owner.getRandom().nextInt(3)));
		}
		owner.giveExperiencePoints(180);
		HarbingerAdvancementGranter.grantIfNotDone(owner, HarbingerAdvancementGranter.ADV_MYCOPHANT_DEFEATED);
		var heir = owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
		heir.putBoolean(HEIR_KEY, true);
		owner.getPersistentData().put(Player.PERSISTED_NBT_TAG, heir);
		HarbingerAdvancementGranter.grantIfNotDone(owner, HarbingerAdvancementGranter.ADV_HIVE_HEIR);
		owner.displayClientMessage(Component.translatable("message.hemomancy.mycophant.victory")
				.withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), false);
	}

	/**
	 * A blood manipulation or Primal Morphling ability from {@code player}. In the Nursery it can tear the player out
	 * of her hold, or (landing on her soon after) break a hold or her Wing Guard.
	 */
	public static void onCast(ServerPlayer player) {
		if (!isActive(player) || !(player.level() instanceof ServerLevel level)) return;
		MycophantEntity boss = findOwnedBoss(level, player.getUUID(), arenaCenter(player));
		if (boss != null) boss.onOwnerCast(player);
	}

	/** The Pulp Bloom's spore veil over the player's view. */
	public static void sendBloomVeil(ServerPlayer player) {
		PacketHandler.sendToPlayer(player, new PacketMycophantIntro(PacketMycophantIntro.BLOOM, 0));
	}

	/** At the yielding she offers the Tendril: the hive names the player its heir. */
	public static void proclaimHeir(ServerPlayer player) {
		player.connection.send(new ClientboundSetTitlesAnimationPacket(12, 80, 30));
		player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("message.hemomancy.mycophant.heir.subtitle")
				.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)));
		player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("message.hemomancy.mycophant.heir.title")
				.withStyle(ChatFormatting.DARK_RED)));
	}

	public static boolean tryStartRematch(ServerPlayer player) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		boolean ok = MycophantEncounterRules.canUseLure(degree.getDegreeNumber(), degree.getArchonPath(),
				degree.isMycophantDefeated(), degree.getMycophantRetryCooldownTicks(), isActive(player),
				player.level().dimension().equals(FungalGardenTravelHelper.FUNGAL_GARDENS), player.isAlive());
		if (ok) beginClaim(player, true);
		return ok;
	}

	/**
	 * The stalking. Whispers are her voice somewhere off in the Gardens. Once she watches, the edges of the view creep
	 * with mycelium; in the last stage every other cue is a glimpse of her standing in the fog behind the player, gone
	 * into the ground when they turn to look.
	 */
	private static void cue(ServerPlayer player, MycophantEncounterRules.HuntStage stage) {
		int seconds = MycophantEncounterRules.averageCueIntervalSeconds(stage);
		if (seconds == Integer.MAX_VALUE || player.tickCount % (seconds * 20) != 0) return;
		boolean glimpse = stage == MycophantEncounterRules.HuntStage.HALLUCINATION && player.tickCount / (seconds * 20) % 2 == 0;
		if (!glimpse) {
			// from a distance and a direction, only for this player
			double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
			player.connection.send(new ClientboundSoundPacket(SoundInit.ENTITY_MYCOPHANT_AMBIENT, SoundSource.HOSTILE,
					player.getX() + Math.cos(angle) * 12.0D, player.getY(), player.getZ() + Math.sin(angle) * 12.0D,
					1.4F, 0.6F, player.getRandom().nextLong()));
		}
		if (stage.ordinal() >= MycophantEncounterRules.HuntStage.VIGNETTE.ordinal())
			PacketHandler.sendToPlayer(player, new PacketMycophantIntro(PacketMycophantIntro.WATCH,
					stage == MycophantEncounterRules.HuntStage.HALLUCINATION ? 2 : 1));
		if (glimpse) PacketHandler.sendToPlayer(player, new PacketMycophantIntro(PacketMycophantIntro.GLIMPSE, player.getRandom().nextInt()));
	}

	private static void announce(ServerPlayer player, MycophantEncounterRules.HuntStage stage) {
		String key = switch (stage) {
			case WHISPERS -> "message.hemomancy.mycophant.whispers";
			case VIGNETTE -> "message.hemomancy.mycophant.watches";
			default -> "";
		};
		if (!key.isEmpty()) player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_GREEN), true);
		if (stage == MycophantEncounterRules.HuntStage.HALLUCINATION)
			PacketHandler.sendToPlayer(player, new PacketMycophantIntro(PacketMycophantIntro.GLIMPSE, player.getRandom().nextInt()));
	}

	/** Interior of the barrier-sealed Nursery containing {@code pos}, if any. */
	public static Optional<AABB> arenaInteriorAt(Level level, Vec3 pos) {
		if (!level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return Optional.empty();
		BlockPos center = new BlockPos(ARENA_X, ChamberOfWillManager.FLOOR_Y,
				(int) Math.round(pos.z / ARENA_SPACING) * ARENA_SPACING);
		if (!new AABB(center).inflate(HALF, level.getHeight(), HALF).contains(pos)) return Optional.empty();
		return Optional.of(new AABB(center.getX() - (HALF - 2), center.getY() + 1, center.getZ() - (HALF - 2),
				center.getX() + HALF - 1, center.getY() + CEILING_Y, center.getZ() + HALF - 1));
	}

	private static BlockPos arenaCenter(ServerPlayer player) {
		return new BlockPos(ARENA_X, ChamberOfWillManager.FLOOR_Y,
				ChamberOfWillManager.get(player.getServer()).idFor(player.getUUID()) * ARENA_SPACING);
	}

	/** Rebuilds the Nursery on every entry from {@link MycophantNurseryLayout}; it is owner-bound, so nothing persists. */
	public static void buildNursery(ServerLevel level, BlockPos center) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = -HALF; x < HALF; x++) for (int z = -HALF; z < HALF; z++) for (int y = 0; y <= CEILING_Y; y++) {
			pos.setWithOffset(center, x, y, z);
			level.setBlock(pos, nurseryBlock(MycophantNurseryLayout.at(x, y, z), x, z), 2);
		}
	}

	private static BlockState nurseryBlock(MycophantNurseryLayout.Material material, int x, int z) {
		return switch (material) {
			case AIR -> Blocks.AIR.defaultBlockState();
			case BARRIER -> Blocks.BARRIER.defaultBlockState();
			case SOIL -> BlockInit.erythrocytic_dirt.get().defaultBlockState();
			case FLOOR -> BlockInit.erythrocytic_mycelium.get().defaultBlockState();
			case NECTAR -> FluidInit.MORPHIC_NECTAR.get().defaultFluidState().createLegacyBlock();
			case BONE -> BlockInit.calcified_hyphae.get().defaultBlockState();
			case FLESH -> BlockInit.infected_stem.get().defaultBlockState();
			case CAP -> BlockInit.infected_cap.get().defaultBlockState();
			case LIGHT -> Blocks.SHROOMLIGHT.defaultBlockState();
			case GROWTH -> {
				double pick = MycophantNurseryLayout.hash(x, z, 29);
				yield (pick < 0.45D ? BlockInit.hyphae : pick < 0.75D ? BlockInit.infected_fungus : BlockInit.stinkhorn_fungus)
						.get().defaultBlockState();
			}
		};
	}

	/** She rises in the central pool facing away (+Z), so the candidate wakes behind her. */
	private static MycophantEntity spawnBoss(ServerLevel level, ServerPlayer owner, BlockPos center) {
		MycophantEntity boss = EntityInit.mycophant.get().create(level);
		if (boss == null) return null;
		boss.setEncounterOwner(owner.getUUID());
		boss.moveTo(center.getX() + 0.5, center.getY() + 1.1, center.getZ() + 0.5, 0.0F, 0.0F);
		boss.setYHeadRot(0.0F);
		boss.setYBodyRot(0.0F);
		boss.setTarget(owner);
		level.addFreshEntity(boss);
		return boss;
	}

	private static MycophantEntity findOwnedBoss(ServerLevel level, UUID owner, BlockPos center) {
		for (MycophantEntity boss : level.getEntitiesOfClass(MycophantEntity.class, new AABB(center).inflate(HALF + 4, CEILING_Y + 2, HALF + 4)))
			if (owner.equals(boss.getEncounterOwner())) return boss;
		return null;
	}

	private static void clearOwnedBoss(ServerLevel level, UUID owner, BlockPos center) {
		MycophantEntity boss = findOwnedBoss(level, owner, center);
		if (boss != null) {
			boss.discardBrood();
			boss.discard();
		}
	}

	private static void give(ServerPlayer player, ItemStack stack) {
		if (!player.addItem(stack)) player.drop(stack, false);
	}

	private static void givePendingTendril(ServerPlayer player) {
		PendingDelivery.deliver(player, TENDRIL);
	}

	private static void clear(ServerPlayer player) {
		discardBrood(player);
		PacketHandler.sendToPlayer(player, PacketSyncMycophantFightScene.clearScene());
		PacketHandler.sendToPlayer(player, new PacketMycophantIntro(PacketMycophantIntro.CLEAR, 0));
		player.getPersistentData().remove(ACTIVE_KEY);
		player.getPersistentData().remove(REMATCH_KEY);
		player.getPersistentData().remove(CLAIM_KEY);
	}

	private static void discardBrood(ServerPlayer player) {
		ServerLevel arena = player.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		if (arena == null) return;
		MycophantEntity boss = findOwnedBoss(arena, player.getUUID(), arenaCenter(player));
		if (boss != null) {
			boss.releaseHold();
			boss.discardBrood();
		}
	}

	private static int huntDuration() { return HemoServerConfig.MYCOPHANT_HUNT_TICKS == null ? 18_000 : HemoServerConfig.MYCOPHANT_HUNT_TICKS.get(); }
	private static int retryCooldown() { return HemoServerConfig.MYCOPHANT_RETRY_COOLDOWN_TICKS == null ? 6_000 : HemoServerConfig.MYCOPHANT_RETRY_COOLDOWN_TICKS.get(); }

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !isActive(player)) return;
		event.setCanceled(true);
		failAttempt(player);
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (!isActive(player)) return;
		if (!player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) { clear(player); return; }
		PacketHandler.sendToPlayer(player, PacketSyncMycophantFightScene.activate(arenaCenter(player)));
	}

	@SubscribeEvent
	public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && isActive(player)
				&& !event.getTo().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) failAttempt(player);
	}
}
