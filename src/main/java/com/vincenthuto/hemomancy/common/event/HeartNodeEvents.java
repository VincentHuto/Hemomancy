package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.RootedStateHelper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.FruitingBodyCacheBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.world.FungalGardensPlaces;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The heart-node sanctum: who may plant one, its privacy, the owner's bonus inside it, the fruiting-body cache that
 * keeps a dead Apotheos's gear, and the login check that forgets a spawn whose node is gone.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class HeartNodeEvents {
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
			} else if (HeartNodeSavedData.get(level).crowds(player.getUUID(), pos)) {
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
		if (level.getBlockEntity(pos) instanceof HeartNodeBlockEntity node) node.setOwner(player.getUUID());
		HeartNodeSavedData.get(level).plant(player.getUUID(), pos).ifPresent(old -> {
			if (level.getBlockState(old).is(BlockInit.heart_node.get())) level.removeBlock(old, false);
		});
		player.setRespawnPosition(level.dimension(), pos.above(), player.getYRot(), true, false);
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

	/** Inside someone else's sanctum, a non-creative player neither breaks nor places. */
	private static boolean refusesStranger(ServerLevel level, BlockPos pos, ServerPlayer player) {
		if (player.isCreative() || !FungalGardensPlaces.inDimension(level)) return false;
		if (!HeartNodeSavedData.get(level).strangerTo(player.getUUID(), pos)) return false;
		tell(player, "message.hemomancy.heart_node.not_yours");
		return true;
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % HeartNodeRules.BONUS_TICKS != 0
				|| !(player.level() instanceof ServerLevel level) || !FungalGardensPlaces.inDimension(level)
				|| !ApotheosBodyEvents.isApotheos(player)) return;
		BlockPos node = HeartNodeSavedData.get(level).nodeOf(player.getUUID());
		if (node == null || !HeartNodeRules.inFootprint(node, player.blockPosition())) return;
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
		HemoCapabilityAccess.getSporeLoad(player).add(HeartNodeRules.BONUS_SPORES);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onDrops(LivingDropsEvent event) {
		if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player)
				|| !(player.level() instanceof ServerLevel level) || event.getDrops().isEmpty()) return;
		boolean keepInventory = level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
		ServerLevel gardens = level.getServer().getLevel(FungalGardenTravelHelper.FUNGAL_GARDENS);
		boolean hasNode = gardens != null && HeartNodeSavedData.get(gardens).nodeOf(player.getUUID()) != null;
		if (!HeartNodeRules.shouldCache(ApotheosBodyEvents.isApotheos(player), keepInventory, hasNode)) return;
		BlockPos spot = cacheSpot(level, player.blockPosition());
		if (spot == null) return;
		level.setBlock(spot, BlockInit.fruiting_body_cache.get().defaultBlockState(), Block.UPDATE_ALL);
		if (!(level.getBlockEntity(spot) instanceof FruitingBodyCacheBlockEntity cache)) return;
		List<net.minecraft.world.item.ItemStack> items = new ArrayList<>();
		for (ItemEntity drop : event.getDrops()) items.add(drop.getItem());
		cache.store(player.getUUID(), items);
		event.getDrops().clear();
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
