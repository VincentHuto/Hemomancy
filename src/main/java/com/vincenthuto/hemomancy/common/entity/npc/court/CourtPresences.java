package com.vincenthuto.hemomancy.common.entity.npc.court;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.MarimoHeartBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Brined Court's two presences (Track 10, Tier 1): Marimo in the Lighthouse Garden's undercroft and Hydra at the
 * Vent Sanctum. The player never fights either. Marimo is his moss (unbreakable blocks with a heart that notices the
 * living); Hydra is its chimney heads and body, and its largest head rises when the Head-tender calls it. Each gives a
 * player its first-sight scene once, recorded in {@code CourtProgress}.
 *
 * <p>Inside a Court site the set pieces that are the poles or their people (the petrified followers, Hydra's crowns
 * and body tissue) refuse breaking and blasts outside Creative; Marimo's own moss refuses everywhere.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CourtPresences {
	public static final String MARIMO = "marimo";
	public static final String HYDRA = "hydra";
	/** How close the living must come to Marimo's Heart for it to notice them. */
	public static final double MARIMO_RANGE = 9.0D;
	/** How close a player must come to the Head-tender for it to call the largest head. */
	public static final double HEAD_TENDER_RANGE = 12.0D;
	/** How far round the Head-tender it looks for the head to call: the Sanctum's chimneys stand within this. */
	public static final int CROWN_SEARCH = 28;
	public static final int CROWN_SEARCH_DOWN = 12;
	public static final int CROWN_SEARCH_UP = 24;
	public static final int CHORUS_LINES = 6;
	/**
	 * After a Head-tender finds no crown to call (one set down from a spawn egg, far from any chimney), it waits this
	 * long before looking again: the search reads about 120k blocks.
	 */
	public static final int CROWN_RESCAN_TICKS = 600;

	private CourtPresences() {
	}

	public static List<ServerPlayer> livingNear(ServerLevel level, BlockPos pos, double range) {
		Vec3 centre = Vec3.atCenterOf(pos);
		return level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(range),
				player -> player.isAlive() && !player.isSpectator() && player.distanceToSqr(centre) <= range * range);
	}

	// --- Marimo ---------------------------------------------------------------------------------------------------

	/** The first time {@code player} comes down to Marimo's Heart: his scene, and the anchored greeting them. */
	public static boolean marimoFirstSight(ServerPlayer player, BlockPos heart) {
		if (!CourtLife.markPresenceSeen(player, MARIMO)) return false;
		BossScenes.playTo(player, BossPresentationIds.SCENE_MARIMO_PRESENCE);
		say(player, chorusName(), Component.translatable("hemomancy.court.presence.marimo.first"));
		return true;
	}

	/** The anchored speak for Marimo when the heart is touched: one of his chorus's lines, and a pulse through the moss. */
	public static void chorus(ServerPlayer player, BlockPos heart) {
		int line = player.getRandom().nextInt(CHORUS_LINES);
		say(player, chorusName(), Component.translatable("hemomancy.court.presence.marimo.chorus." + line));
		if (player.level().getBlockEntity(heart) instanceof MarimoHeartBlockEntity entity) entity.pulse();
	}

	private static Component chorusName() {
		return Component.translatable("hemomancy.court.presence.marimo.chorus");
	}

	// --- Hydra ----------------------------------------------------------------------------------------------------

	/** Once a second: a player who comes near the Head-tender for the first time has the largest head called for them. */
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
		if (!player.isAlive() || player.isSpectator() || CourtLife.hasSeenPresence(player, HYDRA)) return;
		ServerLevel level = player.serverLevel();
		List<CourtMemberEntity> tenders = level.getEntitiesOfClass(CourtMemberEntity.class,
				player.getBoundingBox().inflate(HEAD_TENDER_RANGE),
				member -> member.role() == CourtRole.HEAD_TENDER && member.isAlive()
						&& member.crownScanDue(level.getGameTime()));
		if (!tenders.isEmpty()) callHead(level, tenders.get(0), player);
	}

	/**
	 * The Head-tender calls Hydra's largest head for {@code player}: it rises from the tallest crowned chimney near the
	 * tender, and the player has the first-sight scene. Returns the head, or null when no crowned chimney is near.
	 */
	@Nullable
	public static HydraRisingHead callHead(ServerLevel level, CourtMemberEntity tender, ServerPlayer player) {
		BlockPos crown = tallestCrown(level, tender.blockPosition());
		if (crown == null) {
			tender.deferCrownScan(level.getGameTime() + CROWN_RESCAN_TICKS);
			return null;
		}
		HydraRisingHead head = HydraRisingHead.rise(level, Vec3.atBottomCenterOf(crown), player);
		CourtLife.markPresenceSeen(player, HYDRA);
		BossScenes.playTo(player, head, BossPresentationIds.SCENE_HYDRA_RISE, 0);
		tender.speakTo(player);
		say(player, tender.getDisplayName(), Component.translatable("hemomancy.court.presence.hydra.call"));
		return head;
	}

	/** The highest Hydra Crown within the Sanctum's reach of {@code origin} (the threshold chimney is the tallest). */
	@Nullable
	public static BlockPos tallestCrown(Level level, BlockPos origin) {
		BlockPos best = null;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dy = CROWN_SEARCH_UP; dy >= -CROWN_SEARCH_DOWN; dy--) {
			for (int dx = -CROWN_SEARCH; dx <= CROWN_SEARCH; dx++) {
				for (int dz = -CROWN_SEARCH; dz <= CROWN_SEARCH; dz++) {
					cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
					if (!level.isLoaded(cursor) || !level.getBlockState(cursor).is(BlockInit.hydra_crown.get())) continue;
					if (best == null || origin.distSqr(cursor) < origin.distSqr(best)) best = cursor.immutable();
				}
			}
			if (best != null) return best;
		}
		return null;
	}

	/** A line spoken to {@code player} in the Court's "speaker: line" form. */
	public static void say(ServerPlayer player, Component speaker, Component line) {
		player.displayClientMessage(Component.translatable("hemomancy.court.presence.says", speaker, line)
				.withStyle(ChatFormatting.GRAY), false);
	}

	// --- Never broken -----------------------------------------------------------------------------------------------

	/** Marimo's own moss, refused everywhere outside Creative. */
	public static boolean isMarimo(BlockState state) {
		return state.is(BlockInit.marimo_moss.get()) || state.is(BlockInit.marimo_heart.get());
	}

	/** The poles' set pieces and their people, refused inside a Court site outside Creative. */
	public static boolean isProtectedInSite(BlockState state) {
		return state.is(BlockInit.petrified_follower.get()) || state.is(BlockInit.hydra_crown.get())
				|| state.is(BlockInit.kindled_tissue.get());
	}

	/** Whether breaking {@code state} is refused: Marimo always, the others only inside a Court site; never in Creative. */
	public static boolean refusesBreak(BlockState state, boolean insideSite, boolean creative) {
		if (creative) return false;
		return isMarimo(state) || (insideSite && isProtectedInSite(state));
	}

	private static boolean insideSite(LevelAccessor level, BlockPos pos) {
		return level instanceof ServerLevel server && CourtLife.houseAt(server, pos) != null;
	}

	@SubscribeEvent
	public static void onBreak(BlockEvent.BreakEvent event) {
		BlockState state = event.getState();
		if (!isMarimo(state) && !isProtectedInSite(state)) return;
		Player player = event.getPlayer();
		if (!refusesBreak(state, insideSite(event.getLevel(), event.getPos()), player.getAbilities().instabuild)) return;
		event.setCanceled(true);
		player.displayClientMessage(Component.translatable(isMarimo(state) ? "hemomancy.court.presence.refused.marimo"
				: "hemomancy.court.presence.refused.site").withStyle(ChatFormatting.GRAY), true);
	}

	@SubscribeEvent
	public static void onExplosion(ExplosionEvent.Detonate event) {
		Level level = event.getLevel();
		event.getAffectedBlocks().removeIf(pos -> {
			BlockState state = level.getBlockState(pos);
			if (isMarimo(state)) return true;
			return isProtectedInSite(state) && insideSite(level, pos);
		});
	}
}
