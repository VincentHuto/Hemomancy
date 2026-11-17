package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.verb.CrawlSession;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Lignum Mortis as a Sculpting of Projection: hold Projection on a log (or a mushroom's stem, or worked wood for the
 * worked variant) and the rot crawls from the struck block while blood flows; letting go fells what was reached, and
 * a crawl that has reached everything it can fells at once. Wobbling the aim onto another block the crawl already holds
 * keeps the same crawl going. A crawl the player has looked away from waits {@link #DANGLE_TICKS} for them to come
 * back or let go, then lapses without felling.
 *
 * <p>The crawl is a session map keyed by player UUID, cleared through {@code TransientServerStateEvents}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LignumMortisSculpting implements VerbSculpting {
	/** The beam reaches about as far as the old R cast's 12 blocks. */
	static final double REACH_SCALE = 2.2D;
	/** Blood one pulse costs, before the cost ledger: 16 mL a second at the default cadence. */
	static final double BLOOD_PER_PULSE = 8.0D;
	/** How long a crawl waits for the player to look back at it or let go. */
	static final int DANGLE_TICKS = 40;
	private static final Map<UUID, Crawl> CRAWLS = new HashMap<>();

	private final String manipulation;
	private final LignumMortisManip.Mode mode;

	public LignumMortisSculpting(String manipulation, LignumMortisManip.Mode mode) {
		this.manipulation = manipulation;
		this.mode = mode;
	}

	private static final class Crawl {
		final CrawlSession session;
		final LignumMortisManip.Mode mode;
		long lastPulse;
		boolean announced;

		Crawl(CrawlSession session, LignumMortisManip.Mode mode, long now) {
			this.session = session;
			this.mode = mode;
			this.lastPulse = now;
		}
	}

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Block block && LignumMortisManip.isSelectable(player.level(), block.pos(), mode);
	}

	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}

	@Override
	public double reachScale() {
		return REACH_SCALE;
	}

	/**
	 * The crawl fells what it reaches, so a player who may not break the struck block is refused before any blood is
	 * paid. It is asked again on every pulse, so a crawl refused mid-channel stops unpaid (CN-13).
	 */
	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block)) return false;
		if (ManipulationCombatHelper.mayAlter(player, block.pos()) && PlayerBlockEdits.mayBreak(player, block.pos())) {
			return true;
		}
		player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
				.withStyle(ChatFormatting.DARK_RED), true);
		return false;
	}

	@Override
	public void begin(ServerPlayer player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block) || !(player.level() instanceof ServerLevel level)) return;
		Crawl running = CRAWLS.get(player.getUUID());
		if (running != null && running.mode == mode && running.session.dimension().equals(level.dimension())
				&& touches(running.session, block.pos())) {
			return;
		}
		if (running != null) discard(player, running);
		BloodManipulation registered = ManipulationInit.getByName(manipulation);
		int radius = LignumMortisManip.maxRadius(player, registered);
		Crawl crawl = new Crawl(LignumMortisManip.newCrawl(level, block.pos(), radius, mode), mode, level.getGameTime());
		CRAWLS.put(player.getUUID(), crawl);
		level.playSound(null, block.pos(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 0.6F, 0.8F);
		LignumMortisManip.syncAll(level, player, crawl.session);
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(player.level() instanceof ServerLevel level)) return;
		Crawl crawl = CRAWLS.get(player.getUUID());
		if (crawl == null) {
			// The last crawl fell (or lapsed) while the player held on: the paid pulse starts a fresh one here.
			begin(player, target);
			crawl = CRAWLS.get(player.getUUID());
			if (crawl == null) return;
		}
		crawl.lastPulse = level.getGameTime();
		CrawlSession session = crawl.session;
		double seconds = cadence() / 20.0D;
		List<BlockPos> claimed = session.advance(level,
				LignumMortisRules.blocksPerSecond(session.farthestDistance()) * seconds * pulse.workScale());
		for (BlockPos next : claimed) {
			LignumMortisManip.syncBand(level, player, session, session.band(next, 4));
			LignumMortisManip.spawnBloodTendril(player, next);
		}
		// The beam always shows where it strikes; the newest claim shows how far the rot has run.
		if (target instanceof VerbTarget.Block block) LignumMortisManip.spawnBloodTendril(player, block.pos());
		if (claimed.isEmpty() && session.latest() != null) LignumMortisManip.spawnBloodTendril(player, session.latest());
		if (session.isDone()) {
			player.displayClientMessage(LignumMortisManip.completeMessage(session), true);
			fell(player);
		}
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		if (released) fell(player);
	}

	/** Whether {@code pos} is part of the crawl or touches a block it holds: the same tree, so the crawl goes on. */
	static boolean touches(CrawlSession crawl, BlockPos pos) {
		if (crawl.contains(pos)) return true;
		for (BlockPos near : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
			if (crawl.contains(near)) return true;
		}
		return false;
	}

	/** Fells the player's crawl, if any, and forgets it. */
	static void fell(ServerPlayer player) {
		Crawl crawl = CRAWLS.remove(player.getUUID());
		if (crawl == null || player.getServer() == null) return;
		ServerLevel level = player.getServer().getLevel(crawl.session.dimension());
		if (level == null) return;
		LignumMortisManip.clearFeed(level, player, crawl.session.origin());
		if (player.level().dimension().equals(crawl.session.dimension())) {
			LignumMortisManip.fell(level, player, crawl.session, crawl.mode);
		}
	}

	private static void discard(ServerPlayer player, Crawl crawl) {
		CRAWLS.remove(player.getUUID());
		if (player.getServer() == null) return;
		ServerLevel level = player.getServer().getLevel(crawl.session.dimension());
		if (level != null) LignumMortisManip.clearFeed(level, player, crawl.session.origin());
	}

	/** Whether the player has a crawl running; for tests and the review client. */
	public static int crawlSize(Player player) {
		Crawl crawl = CRAWLS.get(player.getUUID());
		return crawl == null ? 0 : crawl.session.size();
	}

	/**
	 * A crawl the session stopped feeding: fells it if the player let go of the verb, lets it lapse if they kept
	 * channelling elsewhere for {@link #DANGLE_TICKS}, and drops it if they died or changed dimension.
	 */
	@SubscribeEvent
	public static void tick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		Crawl crawl = CRAWLS.get(player.getUUID());
		if (crawl == null) return;
		long idle = player.level().getGameTime() - crawl.lastPulse;
		if (!player.isAlive() || !player.level().dimension().equals(crawl.session.dimension())) {
			discard(player, crawl);
		} else if (!player.isUsingItem() && idle > 1) {
			fell(player);
		} else if (idle > DANGLE_TICKS) {
			discard(player, crawl);
		}
	}

	public static void clearSessionState() {
		CRAWLS.clear();
	}

	public static void logout(Player player) {
		CRAWLS.remove(player.getUUID());
	}
}
