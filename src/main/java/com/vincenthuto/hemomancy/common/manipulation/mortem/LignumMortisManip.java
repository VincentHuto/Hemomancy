package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.VeinMinerHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodStructureFeed;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LignumMortisManip extends BloodManipulation {
	public static final TagKey<Block> ORGANIC_STRUCTURE_BLOCKS = TagKey.create(Registries.BLOCK,
			Hemomancy.rloc("organic_structure_blocks"));
	public static final TagKey<Block> WORKED_LIGNUM_BLOCKS = TagKey.create(Registries.BLOCK,
			Hemomancy.rloc("worked_lignum_blocks"));

	private static final double BASE_TARGET_RANGE = 12.0D;
	private static final double MOVEMENT_GRACE = 4.0D;
	private static final int FEED_VISIBLE_TICKS = 40;
	private static final double FEED_SYNC_RANGE = 64.0D;
	private static final float[] BAND_PROGRESS = { 0.92F, 0.68F, 0.44F, 0.20F };
	private static final int OUTER_BLOOD = 0x58A40020;
	private static final int INNER_BLOOD = 0x98FF4058;
	private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();
	private final Mode mode;

	public LignumMortisManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		this(name, cost, alignLevel, xpCost, type, rank, tendency, section, Mode.STANDARD);
	}

	public LignumMortisManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section, Mode mode) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
		this.mode = mode;
	}

	@Override
	public boolean canContinueChannel(Player player, Level world) {
		Session session = SESSIONS.get(player.getUUID());
		if (session == null) return findOrigin(player, world) != null;
		if (!session.dimension.equals(world.dimension())) return false;
		double allowedDistance = targetRange(player) + MOVEMENT_GRACE;
		return isSelectable(world, session.origin, session.mode)
				&& player.distanceToSqr(Vec3.atCenterOf(session.origin)) <= allowedDistance * allowedDistance;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(player instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel serverLevel)) return;
		Session existing = SESSIONS.get(player.getUUID());
		if (existing != null) {
			syncAll(serverLevel, player, existing);
			return;
		}

		BlockPos origin = findOrigin(player, world);
		if (origin == null) return;
		int radius = maxRadius(player);
		Session session = new Session(world.dimension(), origin, radius, mode);
		session.claim(origin);
		SESSIONS.put(player.getUUID(), session);
		world.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL,
				SoundSource.PLAYERS, 0.7F, 0.8F);
		syncAll(serverLevel, player, session);
		spawnBloodTendril(serverLevel, serverPlayer, origin);
	}

	@Override
	public void tickContinuousAction(Player player, Level world) {
		if (!(player instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel serverLevel)) return;
		Session session = SESSIONS.get(player.getUUID());
		if (session == null) return;

		if (session.selected.size() < LignumMortisRules.MAX_BLOCKS) {
			session.crawlBudget += LignumMortisRules.blocksPerSecond(session.farthestDistance) / 20.0D;
		}
		boolean expanded = false;
		while (session.crawlBudget >= 1.0D && session.selected.size() < LignumMortisRules.MAX_BLOCKS) {
			BlockPos next = session.nextEligible(serverLevel);
			if (next == null) {
				session.crawlBudget = 0.0D;
				break;
			}
			session.crawlBudget -= 1.0D;
			syncBand(serverLevel, player, session, session.band(next));
			spawnBloodTendril(serverLevel, serverPlayer, next);
			session.ticksSinceArc = 0;
			expanded = true;
		}
		if (!expanded && ++session.ticksSinceArc >= 5) {
			spawnBloodTendril(serverLevel, serverPlayer, session.latest);
			session.ticksSinceArc = 0;
		}
	}

	@Override
	public void finishContinuousAction(Player player, boolean released) {
		Session session = SESSIONS.remove(player.getUUID());
		if (session != null && player instanceof ServerPlayer serverPlayer && player.getServer() != null) {
			ServerLevel sessionLevel = player.getServer().getLevel(session.dimension);
			if (sessionLevel != null) clearFeed(sessionLevel, player, session.origin);
			if (released && sessionLevel != null && player.level().dimension().equals(session.dimension)) {
				dismantle(sessionLevel, serverPlayer, session);
			}
		}
		super.finishContinuousAction(player, released);
	}

	@Override
	public void clearContinuousSession(UUID playerId) {
		SESSIONS.remove(playerId);
	}

	private BlockPos findOrigin(Player player, Level world) {
		Vec3 start = player.getEyePosition(1.0F);
		Vec3 end = start.add(player.getViewVector(1.0F).scale(targetRange(player)));
		BlockHitResult hit = world.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE,
				ClipContext.Fluid.NONE, player));
		if (hit.getType() != HitResult.Type.BLOCK) return null;
		BlockPos pos = hit.getBlockPos();
		return isSelectable(world, pos, mode) ? pos.immutable() : null;
	}

	private int maxRadius(Player player) {
		int mastery = HemoCapabilityAccess.getKnownManipulations(player)
				.map(known -> known.getManipLevel(this))
				.map(level -> level.getCurrentLevel()).orElse(0);
		double mortem = HemoCapabilityAccess.getBloodTendency(player)
				.map(tendency -> (double) tendency.getAlignmentByTendency(EnumBloodTendency.MORTEM)).orElse(0.0D);
		double animus = HemoCapabilityAccess.getBloodTendency(player)
				.map(tendency -> (double) tendency.getAlignmentByTendency(EnumBloodTendency.ANIMUS)).orElse(0.0D);
		return LignumMortisRules.maxRadius(mastery, mortem, animus,
				SkillPointHelper.getSanguineReachMultiplier(player));
	}

	private static double targetRange(Player player) {
		return BASE_TARGET_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
	}

	private static boolean isSelectable(Level level, BlockPos pos, Mode mode) {
		if (!level.hasChunkAt(pos) || level.getBlockEntity(pos) != null) return false;
		BlockState state = level.getBlockState(pos);
		TagKey<Block> tag = mode == Mode.WORKED ? WORKED_LIGNUM_BLOCKS : ORGANIC_STRUCTURE_BLOCKS;
		return state.is(tag) && state.getDestroySpeed(level, pos) >= 0.0F;
	}

	private static void dismantle(ServerLevel level, ServerPlayer player, Session session) {
		for (BlockPos pos : session.selected) {
			if (!isSelectable(level, pos, session.mode) || pos.distSqr(session.origin) > session.radius * session.radius
					|| !VeinMinerHelper.hasBreakPermission(player, pos)) continue;
			BlockState state = level.getBlockState(pos);
			Block.dropResources(state, level, pos, null, player, ItemStack.EMPTY);
			level.destroyBlock(pos, false, player);
		}
	}

	private static void syncAll(ServerLevel level, Player player, Session session) {
		for (int band = 0; band < BAND_PROGRESS.length; band++) syncBand(level, player, session, band);
	}

	private static void syncBand(ServerLevel level, Player player, Session session, int band) {
		List<BlockPos> positions = session.positionsInBand(band);
		long channelId = channelId(player, band);
		PacketBloodStructureFeed packet = positions.isEmpty()
				? new PacketBloodStructureFeed(List.of(), BAND_PROGRESS[band], 1, true, channelId)
				: new PacketBloodStructureFeed(positions, BAND_PROGRESS[band], FEED_VISIBLE_TICKS, false, channelId);
		PacketDistributor.sendToPlayersNear(level, null, session.origin.getX() + 0.5D,
				session.origin.getY() + 0.5D, session.origin.getZ() + 0.5D, FEED_SYNC_RANGE, packet);
	}

	private static void clearFeed(ServerLevel level, Player player, BlockPos origin) {
		for (int band = 0; band < BAND_PROGRESS.length; band++) {
			PacketDistributor.sendToPlayersNear(level, null, origin.getX() + 0.5D, origin.getY() + 0.5D,
					origin.getZ() + 0.5D,
					FEED_SYNC_RANGE, new PacketBloodStructureFeed(List.of(), 0.0F, 1, true,
							channelId(player, band)));
		}
	}

	private static long channelId(Player player, int band) {
		UUID id = player.getUUID();
		long base = id.getMostSignificantBits() ^ Long.rotateLeft(id.getLeastSignificantBits(), 1);
		if (base == 0L) base = 4L;
		return (base & ~3L) | band;
	}

	private static void spawnBloodTendril(ServerLevel level, ServerPlayer player, BlockPos target) {
		Vec3 forward = player.getViewVector(1.0F).normalize();
		Vec3 right = new Vec3(-forward.z, 0.0D, forward.x).normalize();
		double side = player.getMainArm() == HumanoidArm.RIGHT ? 0.30D : -0.30D;
		Vec3 start = player.getEyePosition().add(forward.scale(0.14D)).add(right.scale(side)).add(0.0D, -0.38D, 0.0D);
		Vec3 end = Vec3.atCenterOf(target);
		long seed = level.random.nextLong() ^ player.getUUID().getLeastSignificantBits() ^ target.asLong();
		TendrilEffectConfig config = TendrilEffectConfig.defaults()
				.withColors(INNER_BLOOD, OUTER_BLOOD)
				.withRange((float) Math.max(8.0D, start.distanceTo(end) + 4.0D))
				.withLifecycle(2, 4, 5)
				.withShape(16, 2, 0.065F, 0.04F)
				.withBranching(2, 1, 0.2F, 0.7F)
				.withWrithe(0.1F, 0.055F, 0.6F, 0.04F)
				.withBlendColors(false)
				.withFixedSeed(true, seed);
		TendrilEffectSpawner.spawn(level, new TendrilAnchor.Point(start), new TendrilAnchor.Point(end), config);
	}

	private static final class Session {
		private final ResourceKey<Level> dimension;
		private final BlockPos origin;
		private final int radius;
		private final Mode mode;
		private final List<BlockPos> selected = new ArrayList<>();
		private final Set<BlockPos> selectedSet = new HashSet<>();
		private final Set<BlockPos> queued = new HashSet<>();
		private final ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
		private double farthestDistance;
		private double crawlBudget;
		private BlockPos latest;
		private int ticksSinceArc;

		private Session(ResourceKey<Level> dimension, BlockPos origin, int radius, Mode mode) {
			this.dimension = dimension;
			this.origin = origin;
			this.radius = radius;
			this.mode = mode;
		}

		private void claim(BlockPos pos) {
			BlockPos immutable = pos.immutable();
			selected.add(immutable);
			selectedSet.add(immutable);
			latest = immutable;
			farthestDistance = Math.max(farthestDistance, Math.sqrt(immutable.distSqr(origin)));
			enqueueNeighbors(immutable);
		}

		private BlockPos nextEligible(ServerLevel level) {
			while (!frontier.isEmpty()) {
				BlockPos candidate = frontier.removeFirst();
				if (candidate.distSqr(origin) > radius * radius) continue;
				if (mode == Mode.CANOPY && level.getBlockState(candidate).is(BlockTags.LEAVES)) {
					enqueueNeighbors(candidate);
					continue;
				}
				if (!isSelectable(level, candidate, mode)) continue;
				claim(candidate);
				return candidate;
			}
			return null;
		}

		private void enqueueNeighbors(BlockPos center) {
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						if (x == 0 && y == 0 && z == 0) continue;
						BlockPos neighbor = center.offset(x, y, z).immutable();
						if (!selectedSet.contains(neighbor) && queued.add(neighbor)) frontier.addLast(neighbor);
					}
				}
			}
		}

		private int band(BlockPos pos) {
			return LignumMortisRules.overlayBand(Math.sqrt(pos.distSqr(origin)), radius);
		}

		private List<BlockPos> positionsInBand(int band) {
			return selected.stream().filter(pos -> band(pos) == band).toList();
		}
	}

	public enum Mode {
		STANDARD,
		CANOPY,
		WORKED
	}
}
