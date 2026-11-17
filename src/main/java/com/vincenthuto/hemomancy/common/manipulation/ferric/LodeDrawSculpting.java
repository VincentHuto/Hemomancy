package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.event.BloodFeedStyle;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffGrowth;
import com.vincenthuto.hemomancy.common.manipulation.BloodFlowVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.CrawlSession;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodStructureFeed;
import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Lode Draw as a Sculpting of Projection (core-loop Track 8): hold Projection on an ore and the draw crawls through
 * the connected blocks of its lode ({@link OreVeins}) one block a pulse while blood flows; letting go breaks what it
 * reached, and a draw with nothing more to reach breaks at once. It harvests at the tier the Living Staff's rings have
 * earned, so it cannot pull diamonds before the second ring, and its drops are plain. It claims only ore it could
 * harvest, so anything else falls through to the plain verb.
 *
 * <p>The draw is a session map keyed by player UUID, cleared through {@code TransientServerStateEvents}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LodeDrawSculpting implements VerbSculpting {
	/** How long a draw waits for the player to look back at its lode or let go. */
	static final int DANGLE_TICKS = 40;
	private static final int FEED_VISIBLE_TICKS = 40;
	private static final double FEED_SYNC_RANGE = 64.0D;
	private static final float FEED_PROGRESS = 0.45F;
	private static final Map<UUID, Draw> DRAWS = new HashMap<>();

	private static final class Draw {
		final CrawlSession session;
		final int ring;
		long lastPulse;

		Draw(CrawlSession session, int ring, long now) {
			this.session = session;
			this.ring = ring;
			this.lastPulse = now;
		}
	}

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Block block && OreVeins.isOre(block.state())
				&& LivingStaffGrowth.canHarvest(block.state(), LivingStaffGrowth.ring(player))
				&& !StrandSculpting.takesBlood(player.level(), block);
	}

	@Override
	public int cadence() {
		return StaffMiningRules.LODE_CADENCE;
	}

	@Override
	public double bloodPerPulse() {
		return StaffMiningRules.LODE_BLOOD_PER_PULSE;
	}

	/**
	 * The draw breaks what it reaches, so a player who may not break the struck ore is refused before any blood is
	 * paid. It is asked again on every pulse, so a draw refused mid-channel stops unpaid (CN-13).
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
		Draw running = DRAWS.get(player.getUUID());
		if (running != null && running.session.dimension().equals(level.dimension())
				&& touches(running.session, block.pos())) {
			return;
		}
		if (running != null) discard(player, running);
		int ring = LivingStaffGrowth.ring(player);
		CrawlSession session = OreVeins.crawl(level, block.pos(), state -> LivingStaffGrowth.canHarvest(state, ring));
		DRAWS.put(player.getUUID(), new Draw(session, ring, level.getGameTime()));
		level.playSound(null, block.pos(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.7F, 0.6F);
		sync(level, player, session);
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(player.level() instanceof ServerLevel level)) return;
		Draw draw = DRAWS.get(player.getUUID());
		if (draw == null) {
			// The last draw broke (or lapsed) while the player held on: the paid pulse starts a fresh one here.
			begin(player, target);
			draw = DRAWS.get(player.getUUID());
			if (draw == null) return;
		}
		draw.lastPulse = level.getGameTime();
		CrawlSession session = draw.session;
		List<BlockPos> claimed = session.advance(level, StaffMiningRules.lodeBlocksPerPulse(pulse.workScale()));
		if (!claimed.isEmpty()) sync(level, player, session);
		BlockPos shown = claimed.isEmpty() ? session.latest() : claimed.get(claimed.size() - 1);
		if (shown != null) tendril(player, shown);
		if (session.isDone()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.lode_draw.complete", session.size())
					.withStyle(ChatFormatting.DARK_RED), true);
			breakLode(player);
		}
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		if (released) breakLode(player);
	}

	/** Whether {@code pos} is part of the draw or touches a block it holds: the same lode, so the draw goes on. */
	static boolean touches(CrawlSession crawl, BlockPos pos) {
		if (crawl.contains(pos)) return true;
		for (BlockPos near : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
			if (crawl.contains(near)) return true;
		}
		return false;
	}

	/** Breaks what the player's draw reached, with a plain tool of the Staff's tier, and forgets it. */
	static void breakLode(ServerPlayer player) {
		Draw draw = DRAWS.remove(player.getUUID());
		if (draw == null || player.getServer() == null) return;
		ServerLevel level = player.getServer().getLevel(draw.session.dimension());
		if (level == null) return;
		clearFeed(level, player, draw.session.origin());
		if (!player.level().dimension().equals(draw.session.dimension())) return;
		int broken = OreVeins.mine(level, player, draw.session, LivingStaffGrowth.harvestTool(draw.ring), null, null);
		if (broken > 0) {
			Vec3 origin = Vec3.atCenterOf(draw.session.origin());
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.FERRIC_IMPACT, origin,
					origin.add(0.0D, 0.9D, 0.0D), 0.8D, 14);
			level.playSound(null, draw.session.origin(), SoundEvents.DEEPSLATE_BREAK, SoundSource.PLAYERS, 1.0F, 0.7F);
		}
	}

	private static void discard(ServerPlayer player, Draw draw) {
		DRAWS.remove(player.getUUID());
		if (player.getServer() == null) return;
		ServerLevel level = player.getServer().getLevel(draw.session.dimension());
		if (level != null) clearFeed(level, player, draw.session.origin());
	}

	/** The claimed lode wears the infusion veins while blood flows into it. */
	private static void sync(ServerLevel level, Player player, CrawlSession session) {
		BlockPos origin = session.origin();
		PacketHandler.sendNear(level, origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D, FEED_SYNC_RANGE,
				new PacketBloodStructureFeed(session.selected(), FEED_PROGRESS, FEED_VISIBLE_TICKS, false,
						channelId(player), BloodFeedStyle.DRAWN, Vec3.ZERO));
	}

	private static void clearFeed(ServerLevel level, Player player, BlockPos origin) {
		PacketHandler.sendNear(level, origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D, FEED_SYNC_RANGE,
				new PacketBloodStructureFeed(List.of(), 0.0F, 1, true, channelId(player)));
	}

	private static long channelId(Player player) {
		UUID id = player.getUUID();
		long base = Long.rotateLeft(id.getMostSignificantBits(), 7) ^ id.getLeastSignificantBits() ^ 0x10DED4A11L;
		return base == 0L ? 0x10DEL : base;
	}

	private static void tendril(ServerPlayer player, BlockPos target) {
		BloodFlowVisuals.toward(player, Vec3.atCenterOf(target), target.asLong() ^ player.getUUID().getMostSignificantBits(),
				BloodFlowPacket.Style.LIVING);
	}

	/** How many blocks the player's draw holds; for tests and the review client. */
	public static int drawSize(Player player) {
		Draw draw = DRAWS.get(player.getUUID());
		return draw == null ? 0 : draw.session.size();
	}

	/**
	 * A draw the session stopped feeding: breaks it if the player let go of the verb, lets it lapse if they kept
	 * channelling elsewhere for {@link #DANGLE_TICKS}, and drops it if they died or changed dimension.
	 */
	@SubscribeEvent
	public static void tick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		Draw draw = DRAWS.get(player.getUUID());
		if (draw == null) return;
		long idle = player.level().getGameTime() - draw.lastPulse;
		if (!player.isAlive() || !player.level().dimension().equals(draw.session.dimension())) {
			discard(player, draw);
		} else if (!player.isUsingItem() && idle > 1) {
			breakLode(player);
		} else if (idle > DANGLE_TICKS) {
			discard(player, draw);
		}
	}

	public static void clearSessionState() {
		DRAWS.clear();
	}

	public static void logout(Player player) {
		DRAWS.remove(player.getUUID());
	}
}
