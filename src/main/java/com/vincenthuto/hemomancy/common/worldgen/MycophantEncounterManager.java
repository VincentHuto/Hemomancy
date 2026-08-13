package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.client.particle.data.SporiticSporeParticleData;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncMycophantFightScene;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.UUID;

/** Owns the Apotheos hunt, local claim, and solo Morphic Nursery ordeal. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MycophantEncounterManager {
	private static final String ACTIVE_KEY = Hemomancy.MOD_ID + ":mycophant_active";
	private static final String REMATCH_KEY = Hemomancy.MOD_ID + ":mycophant_rematch";
	private static final String CLAIM_KEY = Hemomancy.MOD_ID + ":mycophant_claim_ticks";
	private static final int ARENA_X = 8192;
	private static final int ARENA_SPACING = 128;
	private static final int HALF = 25;

	private MycophantEncounterManager() {}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (player.getPersistentData().contains(CLAIM_KEY)) tickClaim(player);
		else tickGardensPlayer(player);
	}

	public static void tickGardensPlayer(ServerPlayer player) {
		if (isActive(player) || !player.level().dimension().equals(FungalGardenTravelHelper.FUNGAL_GARDENS)) return;
		HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree -> {
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
		});
	}

	public static void beginClaim(ServerPlayer player, boolean rematch) {
		if (isActive(player) || player.getPersistentData().contains(CLAIM_KEY)
				|| ChamberVisitService.isProtected(player)) return;
		player.getPersistentData().putInt(CLAIM_KEY, 100);
		player.getPersistentData().putBoolean(REMATCH_KEY, rematch);
		player.displayClientMessage(Component.translatable("message.hemomancy.mycophant.claim")
				.withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC), false);
		player.level().playSound(null, player.blockPosition(), SoundInit.ENTITY_MYCOPHANT_SUMMON.get(),
				SoundSource.HOSTILE, 1.2F, 0.65F);
	}

	private static void tickClaim(ServerPlayer player) {
		int left = player.getPersistentData().getInt(CLAIM_KEY) - 1;
		player.getPersistentData().putInt(CLAIM_KEY, left);
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 4, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 30, 0, false, false));
		if (left % 5 == 0 && player.level() instanceof ServerLevel level) {
			double radius = 0.8 + left / 100.0 * 4.2;
			for (int i = 0; i < 20; i++) {
				double angle = Math.PI * 2.0 * i / 20.0;
				level.sendParticles(new SporiticSporeParticleData(0.48F, 0.72F, 0.34F),
						player.getX() + Math.cos(angle) * radius, player.getY() + 0.2 + (i % 5) * 0.45,
						player.getZ() + Math.sin(angle) * radius, 1, 0.02, 0.04, 0.02, 0.0);
			}
		}
		if (left <= 0) enterNursery(player);
	}

	public static boolean enterNursery(ServerPlayer player) {
		if (ChamberVisitService.isProtected(player)) {
			player.getPersistentData().remove(CLAIM_KEY);
			player.displayClientMessage(Component.translatable("message.hemomancy.chamber_visit.no_ordeals"), true);
			return false;
		}
		ServerLevel arena = player.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
		if (arena == null) return false;
		BlockPos center = arenaCenter(player);
		ensureArena(arena, center);
		clearOwnedBoss(arena, player.getUUID(), center);
		ChamberOfWillManager.get(player.getServer()).rememberReturnPoint(player);
		player.getPersistentData().putBoolean(ACTIVE_KEY, true);
		player.getPersistentData().remove(CLAIM_KEY);
		player.stopRiding();
		player.changeDimension(new DimensionTransition(arena,
				new Vec3(center.getX() + 0.5, center.getY() + 2.1, center.getZ() + 13.5),
				Vec3.ZERO, 180.0F, 0.0F, DimensionTransition.DO_NOTHING));
		PacketHandler.sendToPlayer(player, PacketSyncMycophantFightScene.activate(center));
		spawnBoss(arena, player, center);
		return true;
	}

	public static boolean tickArenaPlayer(ServerPlayer player, ServerLevel level) {
		if (!isActive(player)) return false;
		BlockPos center = arenaCenter(player);
		MycophantEntity boss = findOwnedBoss(level, player.getUUID(), center);
		if (boss == null) spawnBoss(level, player, center);
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
		HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree -> {
			degree.setMycophantRetryCooldownTicks(retryCooldown());
			if (!rematch && !degree.isMycophantDefeated()) degree.setMycophantExposureTicks(huntDuration());
			InitiatoryDegreeEvents.syncDegree(player, degree);
		});
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
		boolean first = HemoCapabilityAccess.getInitiatoryDegree(owner).map(d -> !d.isMycophantDefeated()).orElse(false);
		HemoCapabilityAccess.getInitiatoryDegree(owner).ifPresent(degree -> {
			degree.setMycophantDefeated(true);
			degree.setMycophantExposureTicks(huntDuration());
			degree.setMycophantRetryCooldownTicks(0);
			InitiatoryDegreeEvents.syncDegree(owner, degree);
		});
		if (first) give(owner, new ItemStack(ItemInit.mycophant_tendril.get()));
		else {
			give(owner, new ItemStack(ItemInit.sanguine_quintessence.get()));
			give(owner, new ItemStack(ItemInit.spore_sac.get(), 2 + owner.getRandom().nextInt(3)));
		}
		owner.giveExperiencePoints(180);
		HarbingerAdvancementGranter.grantIfNotDone(owner, HarbingerAdvancementGranter.ADV_MYCOPHANT_DEFEATED);
		clear(owner);
		owner.displayClientMessage(Component.translatable("message.hemomancy.mycophant.victory")
				.withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), false);
		ChamberOfWillManager.get(level.getServer()).exitChamber(owner);
	}

	public static void ruptureCocoon(ServerPlayer player, boolean empowered) {
		if (!isActive(player) || !(player.level() instanceof ServerLevel level)) return;
		MycophantEntity boss = findOwnedBoss(level, player.getUUID(), arenaCenter(player));
		if (boss != null) boss.ruptureCocoon(empowered);
	}

	public static boolean tryStartRematch(ServerPlayer player) {
		return HemoCapabilityAccess.getInitiatoryDegree(player).map(degree -> {
			boolean ok = MycophantEncounterRules.canUseLure(degree.getDegreeNumber(), degree.getArchonPath(),
					degree.isMycophantDefeated(), degree.getMycophantRetryCooldownTicks(), isActive(player),
					player.level().dimension().equals(FungalGardenTravelHelper.FUNGAL_GARDENS), player.isAlive());
			if (ok) beginClaim(player, true);
			return ok;
		}).orElse(false);
	}

	private static void cue(ServerPlayer player, MycophantEncounterRules.HuntStage stage) {
		int seconds = MycophantEncounterRules.averageCueIntervalSeconds(stage);
		if (seconds == Integer.MAX_VALUE || player.tickCount % (seconds * 20) != 0) return;
		player.level().playSound(null, player.blockPosition(), SoundInit.ENTITY_MYCOPHANT_AMBIENT.get(), SoundSource.HOSTILE, 0.8F, 0.6F);
		if (stage.ordinal() >= MycophantEncounterRules.HuntStage.VIGNETTE.ordinal())
			player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 50, 0, false, false));
		if (stage == MycophantEncounterRules.HuntStage.HALLUCINATION)
			player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, 1.0F));
	}

	private static void announce(ServerPlayer player, MycophantEncounterRules.HuntStage stage) {
		String key = switch (stage) {
			case WHISPERS -> "message.hemomancy.mycophant.whispers";
			case VIGNETTE -> "message.hemomancy.mycophant.watches";
			default -> "";
		};
		if (!key.isEmpty()) player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_GREEN), true);
		if (stage == MycophantEncounterRules.HuntStage.HALLUCINATION)
			player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, 1.0F));
	}

	private static BlockPos arenaCenter(ServerPlayer player) {
		return new BlockPos(ARENA_X, ChamberOfWillManager.FLOOR_Y,
				ChamberOfWillManager.get(player.getServer()).idFor(player.getUUID()) * ARENA_SPACING);
	}

	private static void ensureArena(ServerLevel level, BlockPos center) {
		for (int x = -HALF; x < HALF; x++) for (int z = -HALF; z < HALF; z++) {
			level.setBlock(center.offset(x, 0, z), Blocks.BARRIER.defaultBlockState(), 2);
			boolean edge = Math.abs(x) == HALF - 1 || Math.abs(z) == HALF - 1;
			for (int y = 1; y <= 8; y++) level.setBlock(center.offset(x, y, z), edge ? Blocks.BARRIER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
			if (!edge) level.setBlock(center.offset(x, 1, z), Blocks.MYCELIUM.defaultBlockState(), 2);
			if (!edge && (x * x + z * z) % 17 == 0)
				level.setBlock(center.offset(x, 1, z), FluidInit.MORPHIC_NECTAR.get().defaultFluidState().createLegacyBlock(), 2);
			if (Math.abs(x) == HALF - 2 || Math.abs(z) == HALF - 2) {
				for (int y = 2; y <= 5; y++) if ((x + z + y) % 4 != 0)
					level.setBlock(center.offset(x, y, z), Blocks.WARPED_WART_BLOCK.defaultBlockState(), 2);
			}
			if (!edge && (x * 31 + z * 17) % 47 == 0)
				level.setBlock(center.offset(x, 8, z), Blocks.SHROOMLIGHT.defaultBlockState(), 2);
			level.setBlock(center.offset(x, 9, z), Blocks.BARRIER.defaultBlockState(), 2);
		}
	}

	private static void spawnBoss(ServerLevel level, ServerPlayer owner, BlockPos center) {
		MycophantEntity boss = EntityInit.mycophant.get().create(level);
		if (boss == null) return;
		boss.setEncounterOwner(owner.getUUID());
		boss.moveTo(center.getX() + 0.5, center.getY() + 2, center.getZ() - 8.5, 0, 0);
		boss.setTarget(owner);
		level.addFreshEntity(boss);
	}

	private static MycophantEntity findOwnedBoss(ServerLevel level, UUID owner, BlockPos center) {
		for (MycophantEntity boss : level.getEntitiesOfClass(MycophantEntity.class, new AABB(center).inflate(HALF + 4, 12, HALF + 4)))
			if (owner.equals(boss.getEncounterOwner())) return boss;
		return null;
	}

	private static void clearOwnedBoss(ServerLevel level, UUID owner, BlockPos center) {
		MycophantEntity boss = findOwnedBoss(level, owner, center);
		if (boss != null) boss.discard();
	}

	private static void give(ServerPlayer player, ItemStack stack) {
		if (player.getInventory().add(stack)) return;
		player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY() + .5, player.getZ(), stack));
	}

	private static void clear(ServerPlayer player) {
		PacketHandler.sendToPlayer(player, PacketSyncMycophantFightScene.clearScene());
		player.getPersistentData().remove(ACTIVE_KEY);
		player.getPersistentData().remove(REMATCH_KEY);
		player.getPersistentData().remove(CLAIM_KEY);
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
		if (!(event.getEntity() instanceof ServerPlayer player) || !isActive(player)) return;
		if (!player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) { clear(player); return; }
		PacketHandler.sendToPlayer(player, PacketSyncMycophantFightScene.activate(arenaCenter(player)));
	}

	@SubscribeEvent
	public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && isActive(player)
				&& !event.getTo().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) failAttempt(player);
	}
}
