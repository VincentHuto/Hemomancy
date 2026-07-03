package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.will.WillAmbushState;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillAnchorEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillCombatRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillCompositionRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillSanctuaryRules;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonEvents;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.will.WillPresenceCuePacket;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class WillAmbushDirector {
	private static final double TARGET_SEARCH_RADIUS = 64.0D;
	private static final double PLAYER_CAP_RADIUS = 96.0D;
	private static final ResourceKey<Structure> HARBINGER_OUTPOST = ResourceKey.create(
			Registries.STRUCTURE, Hemomancy.rloc("harbinger_outpost"));

	private WillAmbushDirector() {
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel server)) {
			return;
		}
		if (!boolConfig(HemoServerConfig.WILL_ENABLED, true) || player.isCreative() || player.isSpectator()) return;

		int interval = intConfig(HemoServerConfig.WILL_AMBUSH_CHECK_INTERVAL_TICKS, 200);
		int stagger = player.getId() * 31;
		if ((player.tickCount + stagger) % interval != 0) return;

		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		if (degree < intConfig(HemoServerConfig.WILL_MIN_DEGREE, 4)) return;
		if (!HemoCapabilityAccess.getBloodVolume(player).map(volume -> volume.isActive()).orElse(false)) return;
		if (isSanctuary(server, player.blockPosition())) return;

		WillAmbushState state = HemoCapabilityAccess.getWillAmbushState(player);
		long now = server.getGameTime();
		if (now - state.getLastAmbushGameTime() < intConfig(HemoServerConfig.WILL_AMBUSH_COOLDOWN_TICKS, 24000)) {
			return;
		}
		if (activeNear(server, player.blockPosition(), PLAYER_CAP_RADIUS)
				>= intConfig(HemoServerConfig.WILL_MAX_ACTIVE_PER_PLAYER, 3)) {
			return;
		}
		if (activeInDimension(server) >= intConfig(HemoServerConfig.WILL_MAX_ACTIVE_PER_DIMENSION, 8)) {
			return;
		}

		int tier = WillCompositionRules.tierFor(degree, qliphothDone(player));
		if (tier <= 0) return;
		double chance = chanceFor(server, player, state);
		if (server.random.nextDouble() > chance) return;

		ServerPlayer target = chooseTarget(server, player);
		WillCompositionRules.Composition composition = WillCompositionRules.compose(tier,
				new java.util.Random(server.random.nextLong()), nearbyEligiblePlayers(server, player));
		BlockPos anchorPos = anchorPosition(server, player.blockPosition(), server.random);
		if (anchorPos == null) return;

		WillAnchorEntity anchor = EntityInit.will_anchor.get().create(server);
		if (anchor == null) return;
		anchor.moveTo(anchorPos.getX() + 0.5D, anchorPos.getY(), anchorPos.getZ() + 0.5D,
				server.random.nextFloat() * 360.0F, 0.0F);
		anchor.configure(schoolFor(target), composition, target);
		server.addFreshEntity(anchor);

		for (ServerPlayer nearby : server.getEntitiesOfClass(ServerPlayer.class,
				new AABB(anchor.blockPosition()).inflate(48.0D))) {
			PacketHandler.sendToPlayer(nearby, new WillPresenceCuePacket(anchor.position()));
		}
		state.setLastAmbushGameTime(now);
	}

	private static ServerPlayer chooseTarget(ServerLevel server, ServerPlayer seed) {
		return server.getEntitiesOfClass(ServerPlayer.class, seed.getBoundingBox().inflate(TARGET_SEARCH_RADIUS),
						player -> player.isAlive() && !player.isCreative() && !player.isSpectator()
								&& HemoCapabilityAccess.getPlayerDegreeNumber(player)
								>= intConfig(HemoServerConfig.WILL_MIN_DEGREE, 4))
				.stream()
				.max(Comparator.comparingInt(HemoCapabilityAccess::getPlayerDegreeNumber))
				.orElse(seed);
	}

	private static int nearbyEligiblePlayers(ServerLevel server, ServerPlayer seed) {
		return server.getEntitiesOfClass(ServerPlayer.class, seed.getBoundingBox().inflate(TARGET_SEARCH_RADIUS),
				player -> player.isAlive() && !player.isCreative() && !player.isSpectator()).size();
	}

	private static double chanceFor(ServerLevel server, ServerPlayer player, WillAmbushState state) {
		double chance = doubleConfig(HemoServerConfig.WILL_BASE_CHANCE_PER_CHECK, 0.02D);
		if (terrainRipe(server, player.blockPosition())) {
			chance *= doubleConfig(HemoServerConfig.WILL_TERRAIN_MULTIPLIER, 3.0D);
		}
		if (BloodMoonEvents.isBloodMoonActive(server)) {
			chance *= doubleConfig(HemoServerConfig.WILL_BLOOD_MOON_MULTIPLIER, 2.0D);
		}
		MobEffectInstance drunkenness = player.getEffect(EffectInit.blood_drunkenness);
		if (drunkenness != null) {
			chance *= 1.0D + doubleConfig(HemoServerConfig.WILL_BLOOD_DRUNKENNESS_MULTIPLIER_PER_AMPLIFIER, 0.5D)
					* (drunkenness.getAmplifier() + 1);
		}
		if (state.getHeraldUntilGameTime() > server.getGameTime()) {
			chance *= doubleConfig(HemoServerConfig.WILL_HERALD_MULTIPLIER, 4.0D);
		}
		if (nearOwnedBloom(server, player)) {
			chance *= 1.5D;
		}
		chance *= 1.0D + Math.min(1.5D, state.getHiveAttention() * 0.05D);
		return Math.max(0.0D, Math.min(1.0D, chance));
	}

	private static EnumBloodTendency schoolFor(ServerPlayer target) {
		return HemoCapabilityAccess.getBloodTendency(target)
				.flatMap(tendency -> tendency.getTendency().entrySet().stream()
						.max(Map.Entry.comparingByValue())
						.map(Map.Entry::getKey)
						.map(WillCombatRules::counterSchool))
				.orElse(EnumBloodTendency.ANIMUS);
	}

	private static boolean qliphothDone(ServerPlayer player) {
		return HemoCapabilityAccess.getInitiatoryDegree(player)
				.map(IInitiatoryDegree::isQliphothCommunionDone)
				.orElse(false);
	}

	private static boolean isSanctuary(ServerLevel server, BlockPos pos) {
		boolean insideFane = FoundingFaneSavedData.get(server).findOwnerContaining(pos) != null;
		boolean inChamber = server.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL);
		boolean insideOutpost = insideHarbingerOutpost(server, pos);
		return WillSanctuaryRules.isSanctuary(insideFane, inChamber, insideOutpost);
	}

	private static boolean insideHarbingerOutpost(ServerLevel server, BlockPos pos) {
		StructureStart start = server.structureManager()
				.getStructureWithPieceAt(pos, holder -> holder.is(HARBINGER_OUTPOST));
		return start != null && start.isValid();
	}

	private static boolean terrainRipe(ServerLevel server, BlockPos pos) {
		if (!server.canSeeSky(pos) || server.getMaxLocalRawBrightness(pos) < 7) return true;
		return server.getBlockState(pos.below()).is(BlockInit.erythrocytic_mycelium.get())
				|| server.getBlockState(pos.below()).is(BlockInit.mycelium_erythrocytic_dirt.get())
				|| server.getBlockState(pos.below()).is(BlockInit.qliphoth_bloom.get());
	}

	private static boolean nearOwnedBloom(ServerLevel server, ServerPlayer player) {
		String dimension = server.dimension().location().toString();
		for (QliphothBloomSavedData.BloomEntry bloom : QliphothBloomSavedData.get(server.getServer().overworld()).getBlooms()) {
			if (player.getUUID().equals(bloom.ownerUUID()) && dimension.equals(bloom.dimension())
					&& bloom.center().closerThan(player.blockPosition(), 96.0D)) {
				return true;
			}
		}
		return false;
	}

	private static BlockPos anchorPosition(ServerLevel server, BlockPos origin, RandomSource random) {
		for (int attempt = 0; attempt < 8; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double distance = 18.0D + random.nextDouble() * 18.0D;
			int x = origin.getX() + (int) Math.round(Math.cos(angle) * distance);
			int z = origin.getZ() + (int) Math.round(Math.sin(angle) * distance);
			int y = server.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
			BlockPos pos = new BlockPos(x, y, z);
			if (server.getWorldBorder().isWithinBounds(pos) && !isSanctuary(server, pos)) {
				return pos;
			}
		}
		return null;
	}

	private static int activeNear(ServerLevel server, BlockPos center, double radius) {
		return server.getEntitiesOfClass(WillEntity.class, new AABB(center).inflate(radius),
				will -> will.isAlive() && will.hemomancy$getOwnerUUID() == null).size();
	}

	private static int activeInDimension(ServerLevel server) {
		AABB bounds = new AABB(-30000000, server.getMinBuildHeight(), -30000000,
				30000000, server.getMaxBuildHeight(), 30000000);
		return server.getEntitiesOfClass(WillEntity.class, bounds,
				will -> will.isAlive() && will.hemomancy$getOwnerUUID() == null).size();
	}

	private static boolean boolConfig(net.neoforged.neoforge.common.ModConfigSpec.BooleanValue value, boolean fallback) {
		return value == null ? fallback : value.get();
	}

	private static int intConfig(net.neoforged.neoforge.common.ModConfigSpec.IntValue value, int fallback) {
		return value == null ? fallback : value.get();
	}

	private static double doubleConfig(net.neoforged.neoforge.common.ModConfigSpec.DoubleValue value, double fallback) {
		return value == null ? fallback : value.get();
	}
}
