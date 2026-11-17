package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.event.BloodFeedStyle;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodFlowVisuals;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.verb.CrawlSession;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodStructureFeed;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lignum Mortis: rot threaded through connected wood and fungus, felled on release. Since core-loop Track 2 it is
 * first a Sculpting of Projection ({@link LignumMortisSculpting}: hold Projection on a log). The R channel below is kept
 * for players who have not switched and is marked for removal after one review pass; both share the
 * {@link CrawlSession}, the selector and the felling.
 */
public class LignumMortisManip extends BloodManipulation {
	public static final TagKey<Block> ORGANIC_STRUCTURE_BLOCKS = TagKey.create(Registries.BLOCK,
			Hemomancy.rloc("organic_structure_blocks"));
	public static final TagKey<Block> WORKED_LIGNUM_BLOCKS = TagKey.create(Registries.BLOCK,
			Hemomancy.rloc("worked_lignum_blocks"));

	private static final double BASE_TARGET_RANGE = 12.0D;
	private static final double MOVEMENT_GRACE = 4.0D;
	static final int FEED_VISIBLE_TICKS = 40;
	private static final double FEED_SYNC_RANGE = 64.0D;
	private static final float[] BAND_PROGRESS = { 0.92F, 0.68F, 0.44F, 0.20F };
	private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();
	private final Mode mode;

	public LignumMortisManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTemper temper,
			EnumVeinSections section) {
		this(name, cost, alignLevel, xpCost, type, rank, temper, section, Mode.STANDARD);
	}

	public LignumMortisManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTemper temper,
			EnumVeinSections section, Mode mode) {
		super(name, cost, alignLevel, xpCost, type, rank, temper, section);
		this.mode = mode;
	}

	public Mode mode() {
		return mode;
	}

	@Override
	public boolean canContinueChannel(Player player, Level world) {
		Session session = SESSIONS.get(player.getUUID());
		if (session == null) return findOrigin(player, world) != null;
		if (!session.crawl.dimension().equals(world.dimension())) return false;
		double allowedDistance = targetRange(player) + MOVEMENT_GRACE;
		return isSelectable(world, session.crawl.origin(), mode)
				&& player.distanceToSqr(Vec3.atCenterOf(session.crawl.origin())) <= allowedDistance * allowedDistance;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {
			if (!(player instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel serverLevel)) return;
			Session existing = SESSIONS.get(player.getUUID());
			if (existing != null) {
				syncAll(serverLevel, player, existing.crawl);
				return;
			}
			BlockPos origin = findOrigin(player, world);
			if (origin == null) return;
			Session session = new Session(newCrawl(world, origin, maxRadius(player, this), mode));
			SESSIONS.put(player.getUUID(), session);
			world.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL,
					SoundSource.PLAYERS, 0.7F, 0.8F);
			syncAll(serverLevel, player, session.crawl);
			spawnBloodTendril(serverPlayer, origin);
		}
	}

	@Override
	public void tickContinuousAction(Player player, Level world) {
		if (!(player instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel serverLevel)) return;
		Session session = SESSIONS.get(player.getUUID());
		if (session == null) return;
		CrawlSession crawl = session.crawl;
		List<BlockPos> claimed = crawl.advance(serverLevel,
				LignumMortisRules.blocksPerSecond(crawl.farthestDistance()) / 20.0D);
		for (BlockPos next : claimed) {
			syncBand(serverLevel, player, crawl, crawl.band(next, BAND_PROGRESS.length));
			spawnBloodTendril(serverPlayer, next);
			session.ticksSinceArc = 0;
		}
		if (crawl.isDone()) {
			player.displayClientMessage(completeMessage(crawl), true);
			ManipulationChannelManager.stop(serverPlayer, true);
			return;
		}
		if (claimed.isEmpty() && ++session.ticksSinceArc >= 5) {
			spawnBloodTendril(serverPlayer, crawl.latest());
			session.ticksSinceArc = 0;
		}
	}

	@Override
	public void finishContinuousAction(Player player, boolean released) {
		Session session = SESSIONS.remove(player.getUUID());
		if (session != null && player instanceof ServerPlayer serverPlayer && player.getServer() != null) {
			ServerLevel sessionLevel = player.getServer().getLevel(session.crawl.dimension());
			if (sessionLevel != null) clearFeed(sessionLevel, player, session.crawl.origin());
			if (released && sessionLevel != null && player.level().dimension().equals(session.crawl.dimension())) {
				fell(sessionLevel, serverPlayer, session.crawl, mode);
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

	/** The crawl's radius for {@code player} on {@code manipulation}: mastery, Mortem and Animus, and Sanguine Reach. */
	static int maxRadius(Player player, BloodManipulation manipulation) {
		ManipLevel level = HemoCapabilityAccess.getKnownManipulations(player).getManipLevel(manipulation);
		int mastery = level != null ? level.getCurrentLevel() : 0;
		double mortem = HemoCapabilityAccess.getBloodTemperament(player).getAlignmentByTemper(EnumBloodTemper.MORTEM);
		double animus = HemoCapabilityAccess.getBloodTemperament(player).getAlignmentByTemper(EnumBloodTemper.ANIMUS);
		return LignumMortisRules.maxRadius(mastery, mortem, animus,
				SkillPointHelper.getSanguineReachMultiplier(player));
	}

	private static double targetRange(Player player) {
		return BASE_TARGET_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
	}

	/** Whether the block at {@code pos} is wood or fungus the crawl may claim. Safe on both sides. */
	public static boolean isSelectable(Level level, BlockPos pos, Mode mode) {
		if (!level.isLoaded(pos) || level.getBlockEntity(pos) != null) return false;
		BlockState state = level.getBlockState(pos);
		TagKey<Block> tag = mode == Mode.WORKED ? WORKED_LIGNUM_BLOCKS : ORGANIC_STRUCTURE_BLOCKS;
		return state.is(tag) && state.getDestroySpeed(level, pos) >= 0.0F;
	}

	/** A crawl from {@code origin} through what {@code mode} selects; a canopy crawl passes through leaves. */
	static CrawlSession newCrawl(Level level, BlockPos origin, int radius, Mode mode) {
		return new CrawlSession(level.dimension(), origin, radius, LignumMortisRules.MAX_BLOCKS,
				new CrawlSession.Selector() {
					@Override
					public boolean selectable(Level at, BlockPos pos) {
						return isSelectable(at, pos, mode);
					}

					@Override
					public boolean passesThrough(Level at, BlockPos pos) {
						return mode == Mode.CANOPY && at.getBlockState(pos).is(BlockTags.LEAVES);
					}
				});
	}

	/**
	 * Fells what the crawl reached, as {@code player} with a bare hand ({@code PlayerBlockEdits.breakBlock}); ordinary
	 * logs sometimes leave infested wood.
	 */
	static int fell(ServerLevel level, ServerPlayer player, CrawlSession crawl, Mode mode) {
		return crawl.dismantle(level, player, ItemStack.EMPTY, (pos, state) -> {
			if (mode == Mode.STANDARD && LignumMortisRules.shouldDropInfestedWood(state.is(BlockTags.LOGS),
					level.random.nextInt(LignumMortisRules.INFESTED_WOOD_DROP_ROLLS))) {
				Block.popResource(level, pos, new ItemStack(BlockInit.infested_wood.get()));
			}
			ManipulationParticles.accent(level, EnumBloodTemper.MORTEM, Vec3.atCenterOf(pos), new Vec3(0, -.15, 0));
		});
	}

	static net.minecraft.network.chat.Component completeMessage(CrawlSession crawl) {
		return HemoText.translatable(
				"message.hemomancy.lignum_mortis_manip.lignum_selection_complete_blocks_harvesting_perm", crawl.size());
	}

	static void syncAll(ServerLevel level, Player player, CrawlSession crawl) {
		for (int band = 0; band < BAND_PROGRESS.length; band++) syncBand(level, player, crawl, band);
	}

	static void syncBand(ServerLevel level, Player player, CrawlSession crawl, int band) {
		List<BlockPos> positions = crawl.inBand(band, BAND_PROGRESS.length);
		long channelId = channelId(player, band);
		PacketBloodStructureFeed packet = positions.isEmpty()
				? new PacketBloodStructureFeed(List.of(), BAND_PROGRESS[band], 1, true, channelId)
				: new PacketBloodStructureFeed(positions, BAND_PROGRESS[band], FEED_VISIBLE_TICKS, false, channelId,
						BloodFeedStyle.PLAIN, Vec3.ZERO);
		BlockPos origin = crawl.origin();
		PacketHandler.sendNear(level, origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D,
				FEED_SYNC_RANGE, packet);
	}

	static void clearFeed(ServerLevel level, Player player, BlockPos origin) {
		for (int band = 0; band < BAND_PROGRESS.length; band++) {
			PacketHandler.sendNear(level, origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D,
					FEED_SYNC_RANGE, new PacketBloodStructureFeed(List.of(), 0.0F, 1, true, channelId(player, band)));
		}
	}

	private static long channelId(Player player, int band) {
		UUID id = player.getUUID();
		long base = id.getMostSignificantBits() ^ Long.rotateLeft(id.getLeastSignificantBits(), 1);
		if (base == 0L) base = 4L;
		return (base & ~3L) | band;
	}

	static void spawnBloodTendril(ServerPlayer player, BlockPos target) {
		BloodFlowVisuals.lignum(player, Vec3.atCenterOf(target),
				target.asLong() ^ player.getUUID().getLeastSignificantBits());
	}

	private static final class Session {
		private final CrawlSession crawl;
		private int ticksSinceArc;

		private Session(CrawlSession crawl) {
			this.crawl = crawl;
		}
	}

	public enum Mode {
		STANDARD,
		CANOPY,
		WORKED
	}
}
