package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MycelialSynapseBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.RootedStateHelper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.FruitingBodyCacheBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.world.FungalGardensPlaces;
import com.vincenthuto.hemomancy.common.world.HeartNodeEntry;
import com.vincenthuto.hemomancy.common.world.HeartNodeMoment;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import com.vincenthuto.hemomancy.common.world.HeartNodeService;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The heart-node sanctum: who may plant one, the privacy of its whole envelope (hands, blasts and pistons), the
 * owner's bonus inside it, the fruiting-body cache that keeps an Apotheos's gear when they die in their own envelope,
 * the line that tells the owner they have left it (when their settings ask for it), and the login check that forgets a
 * spawn whose node is gone. Everything here is the planter's alone: there are no members, allies or bloodline in a
 * sanctum.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class HeartNodeEvents {
	/** Each owner's side of their envelope's edge at the last check, and when they were last warned of leaving. */
	private static final Map<UUID, EdgeWatch> EDGE_WATCH = new HashMap<>();

	private record EdgeWatch(boolean inside, long warnedAt) {}

	private HeartNodeEvents() {}

	/** Refusals run early, so a refused node never touches saved data or the old node. */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onPlace(BlockEvent.EntityPlaceEvent event) {
		if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)) return;
		BlockPos pos = event.getPos();
		if (event.getPlacedBlock().is(BlockInit.heart_node.get())) {
			boolean onNetwork = level.getBlockState(pos.below()).is(RootedStateHelper.FUNGAL_NETWORK_GROUND);
			if (!HeartNodeRules.mayPlant(ApotheosBodyEvents.isApotheos(player), FungalGardensPlaces.inDimension(level),
					onNetwork)) {
				event.setCanceled(true);
				tell(player, "message.hemomancy.heart_node.refused");
			} else if (HeartNodeSavedData.get(level).wouldOverlap(player.getUUID(), pos, HeartNodeRules.HEART_RADIUS)) {
				event.setCanceled(true);
				tell(player, "message.hemomancy.heart_node.crowded");
			}
			return;
		}
		if (refusesStranger(level, pos, player)) event.setCanceled(true);
	}

	/** Planting takes effect only once no other handler has refused the placement (cancelled events skip LOWEST). */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onPlanted(BlockEvent.EntityPlaceEvent event) {
		if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player
				&& event.getPlacedBlock().is(BlockInit.heart_node.get())) {
			plant(level, player, event.getPos());
		}
	}

	private static void plant(ServerLevel level, ServerPlayer player, BlockPos pos) {
		long now = level.getGameTime();
		if (level.getBlockEntity(pos) instanceof HeartNodeBlockEntity node) node.plant(player.getUUID(), now);
		// The old node withers; its synapse records go with its entry, and the synapse blocks go inert.
		HeartNodeSavedData.get(level).plant(player.getUUID(), pos, now).ifPresent(old -> {
			if (level.getBlockState(old.heart()).is(BlockInit.heart_node.get())) level.removeBlock(old.heart(), false);
			MycelialSynapseBlock.witherAll(level, old.synapses());
		});
		player.setRespawnPosition(level.dimension(), pos.above(), player.getYRot(), true, false);
		HeartNodeService.announce(level, pos, HeartNodeMoment.PLANTED, 0);
		HeartNodeEnvelopeSync.syncAround(level, pos);
		tell(player, "message.hemomancy.heart_node.planted");
	}

	@SubscribeEvent
	public static void onBreak(BlockEvent.BreakEvent event) {
		if (!(event.getLevel() instanceof ServerLevel level) || !(event.getPlayer() instanceof ServerPlayer player)) return;
		BlockPos pos = event.getPos();
		if (level.getBlockEntity(pos) instanceof FruitingBodyCacheBlockEntity cache) {
			if (!player.isCreative() && !HeartNodeRules.mayOpen(cache.owner(), player.getUUID())) {
				event.setCanceled(true);
				tell(player, "message.hemomancy.fruiting_body_cache.not_yours");
			}
			return;
		}
		if (refusesStranger(level, pos, player)) event.setCanceled(true);
	}

	/** Inside someone else's envelope, a non-creative player neither breaks nor places. */
	private static boolean refusesStranger(ServerLevel level, BlockPos pos, ServerPlayer player) {
		if (player.isCreative() || !FungalGardensPlaces.inDimension(level)) return false;
		if (!HeartNodeSavedData.get(level).strangerTo(player.getUUID(), pos)) return false;
		tell(player, "message.hemomancy.heart_node.not_yours");
		return true;
	}

	/** A blast inside a sanctum breaks nothing of it, whoever set it off. */
	@SubscribeEvent
	public static void onExplosion(ExplosionEvent.Detonate event) {
		if (!(event.getLevel() instanceof ServerLevel level) || !FungalGardensPlaces.inDimension(level)) return;
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		if (data.envelopes().isEmpty()) return;
		event.getAffectedBlocks().removeIf(pos -> data.footprintAt(pos).isPresent());
	}

	/** A piston moves blocks only within the envelope it stands in: nothing is pushed into or pulled out of one. */
	@SubscribeEvent
	public static void onPiston(PistonEvent.Pre event) {
		if (!(event.getLevel() instanceof ServerLevel level) || !FungalGardensPlaces.inDimension(level)) return;
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		if (data.envelopes().isEmpty()) return;
		PistonStructureResolver structure = event.getStructureHelper();
		if (structure == null || !structure.resolve()) return;
		Optional<UUID> home = ownerAt(data, event.getPos());
		Direction push = structure.getPushDirection();
		for (BlockPos pos : structure.getToPush()) {
			if (!home.equals(ownerAt(data, pos)) || !home.equals(ownerAt(data, pos.relative(push)))) {
				event.setCanceled(true);
				return;
			}
		}
		for (BlockPos pos : structure.getToDestroy()) {
			if (!home.equals(ownerAt(data, pos))) {
				event.setCanceled(true);
				return;
			}
		}
	}

	private static Optional<UUID> ownerAt(HeartNodeSavedData data, BlockPos pos) {
		return data.footprintAt(pos).map(HeartNodeEntry::owner);
	}

	/** The owner, alive and inside their own envelope, is fed in proportion to how near the heart they stand. */
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % HeartNodeRules.BONUS_TICKS != 0
				|| !player.isAlive() || !(player.level() instanceof ServerLevel level)
				|| !FungalGardensPlaces.inDimension(level) || !ApotheosBodyEvents.isApotheos(player)) return;
		double strength = HeartNodeSavedData.get(level).ownerStrength(player.getUUID(), player.blockPosition());
		if (strength <= 0.0D) return;
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
		HemoCapabilityAccess.getSporeLoad(player).add(HeartNodeRules.bonusSpores(strength));
	}

	/** The owner stepping out of their own envelope hears of it, at most once every ten seconds. */
	@SubscribeEvent
	public static void onEdgeTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % HeartNodeRules.EDGE_CHECK_TICKS != 0) return;
		HeartNodeEntry entry = player.isAlive() && player.level() instanceof ServerLevel level
				&& FungalGardensPlaces.inDimension(level) ? HeartNodeSavedData.get(level).entry(player.getUUID()) : null;
		if (entry == null || !entry.settings().has(HeartNodeSettings.EDGE_WARNING)) {
			EDGE_WATCH.remove(player.getUUID());
			return;
		}
		long now = player.level().getGameTime();
		boolean inside = entry.footprint().contains(player.blockPosition());
		EdgeWatch last = EDGE_WATCH.get(player.getUUID());
		long warnedAt = last == null ? HeartNodeRules.NEVER_WARNED : last.warnedAt();
		if (last != null && HeartNodeRules.warnsLeaving(last.inside(), inside, warnedAt, now)) {
			tell(player, "message.hemomancy.heart_node.leaving");
			warnedAt = now;
		}
		EDGE_WATCH.put(player.getUUID(), new EdgeWatch(inside, warnedAt));
	}

	public static void logout(ServerPlayer player) {
		EDGE_WATCH.remove(player.getUUID());
	}

	public static void clearSessionState() {
		EDGE_WATCH.clear();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onDrops(LivingDropsEvent event) {
		if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player)
				|| !(player.level() instanceof ServerLevel level) || event.getDrops().isEmpty()) return;
		boolean keepInventory = level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
		if (!HeartNodeRules.shouldCache(ApotheosBodyEvents.isApotheos(player), keepInventory,
				diedInOwnEnvelope(level, player))) return;
		BlockPos spot = cacheSpot(level, player.blockPosition());
		if (spot == null) return;
		level.setBlock(spot, BlockInit.fruiting_body_cache.get().defaultBlockState(), Block.UPDATE_ALL);
		if (!(level.getBlockEntity(spot) instanceof FruitingBodyCacheBlockEntity cache)) return;
		List<net.minecraft.world.item.ItemStack> items = new ArrayList<>();
		for (ItemEntity drop : event.getDrops()) items.add(drop.getItem());
		cache.store(player.getUUID(), items);
		HeartNodeSavedData.get(level).addCache(player.getUUID(), spot);
		event.getDrops().clear();
	}

	/** Only a death in the Gardens, inside the dead player's own envelope, keeps their gear. */
	private static boolean diedInOwnEnvelope(ServerLevel level, ServerPlayer player) {
		return FungalGardensPlaces.inDimension(level)
				&& HeartNodeSavedData.get(level).insideOwnEnvelope(player.getUUID(), player.blockPosition());
	}

	/** The nearest free spot to the death point, kept inside the build height; null when there is none. */
	@Nullable
	static BlockPos cacheSpot(Level level, BlockPos death) {
		int y = Math.max(level.getMinBuildHeight() + 1, Math.min(level.getMaxBuildHeight() - 2, death.getY()));
		BlockPos start = new BlockPos(death.getX(), y, death.getZ());
		for (int radius = 0; radius <= HeartNodeRules.CACHE_SEARCH; radius++) {
			for (BlockPos pos : BlockPos.betweenClosed(start.offset(-radius, -radius, -radius),
					start.offset(radius, radius, radius))) {
				if (Math.max(Math.abs(pos.getX() - start.getX()), Math.max(Math.abs(pos.getY() - start.getY()),
						Math.abs(pos.getZ() - start.getZ()))) != radius) continue;
				if (level.isInWorldBounds(pos) && level.getBlockState(pos).isAir()) return pos.immutable();
			}
		}
		return null;
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		ServerLevel gardens = player.server.getLevel(FungalGardenTravelHelper.FUNGAL_GARDENS);
		BlockPos respawn = player.getRespawnPosition();
		// Only the spawn of a node this player lost is cleared; thrones, beds and /spawnpoint are left alone.
		if (gardens == null || respawn == null || !player.isRespawnForced()
				|| !gardens.dimension().equals(player.getRespawnDimension())) return;
		if (HeartNodeSavedData.get(gardens).takeRemovedSpawn(player.getUUID(), respawn)) {
			player.setRespawnPosition(Level.OVERWORLD, null, 0f, false, false);
		}
	}

	private static void tell(Player player, String key) {
		player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE,
				ChatFormatting.ITALIC), true);
	}
}
