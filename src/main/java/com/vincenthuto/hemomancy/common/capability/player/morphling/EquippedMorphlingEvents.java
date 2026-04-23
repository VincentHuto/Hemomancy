package com.vincenthuto.hemomancy.common.capability.player.morphling;

import net.neoforged.fml.common.EventBusSubscriber;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.item.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.morphling.SyncEquippedMorphlingPacket;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class EquippedMorphlingEvents {

	private static final Map<String, Long> TEMPORARY_WEBS = new ConcurrentHashMap<>();
	public static final int TEMP_WEB_DURATION = 40;

	/**
	 * While a morphling is equipped, it passively drains blood to sustain itself.
	 * The drain rate scales with the morphling's blood cost. If the player runs out
	 * of blood, the morphling is forcefully unequipped.
	 */
	@SubscribeEvent
	public static void playerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide) return;
		if (!HemoServerConfig.MORPHLING_PASSIVE_DRAIN_ENABLED.get()) return;

		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(morphCap -> {
			if (!morphCap.hasMorphling()) return;

			int drainInterval = HemoServerConfig.MORPHLING_DRAIN_INTERVAL.get();
			if (player.tickCount % drainInterval != 0) return;

			HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
				if (!volume.isActive()) return;

				double drainRate = HemoServerConfig.MORPHLING_DRAIN_RATE.get();

				// Scale drain by morphling blood cost if it implements IMorphling
				if (morphCap.getEquippedMorphling().getItem() instanceof IMorphling morphling) {
					int bloodCost = morphling.getBloodCost();
					if (bloodCost > 0) {
						drainRate *= (1.0 + bloodCost / 100.0);
					}
				}

				if (volume.getBloodVolume() > drainRate) {
					volume.drain(drainRate);
					BloodVolumeEvents.syncVolume((ServerPlayer) player, volume);
				} else {
					// Not enough blood â€” unequip the morphling
					morphCap.clearMorphling();
					syncToClient((ServerPlayer) player);
					player.displayClientMessage(
							Component.literal("Your morphling withers from blood starvation...")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
				}
			});

			// Delegate morphling-specific passive effects to the item itself
			if (morphCap.getEquippedMorphling().getItem() instanceof IMorphling morphling) {
				morphling.onEquippedTick(player, morphCap.getEquippedMorphling());
			}
		});
	}

	/**
	 * When the player takes damage while a morphling is equipped, delegate to the
	 * morphling's onEquippedHurt for reactive abilities (swarm retaliation, thorns,
	 * web cocoons, spore clouds, etc.).
	 */
	@SubscribeEvent
	public static void onPlayerHurt(LivingDamageEvent.Pre event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;

		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(morphCap -> {
			if (!morphCap.hasMorphling()) return;
			ItemStack morphStack = morphCap.getEquippedMorphling();
			if (morphStack.getItem() instanceof IMorphling morphling) {
				morphling.onEquippedHurt(player, morphStack, event.getSource(), event.getNewDamage());
			}
		});
	}

	/**
	 * When the player deals damage to a living entity while a morphling is equipped,
	 * delegate to the morphling's onEquippedAttack for on-hit abilities (life steal,
	 * venom strike, predator's mark, etc.).
	 */
	@SubscribeEvent
	public static void onPlayerAttack(LivingDamageEvent.Pre event) {
		LivingEntity target = event.getEntity();
		if (target.level().isClientSide) return;
		if (!(event.getSource().getEntity() instanceof Player player)) return;

		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(morphCap -> {
			if (!morphCap.hasMorphling()) return;
			ItemStack morphStack = morphCap.getEquippedMorphling();
			if (morphStack.getItem() instanceof IMorphling morphling) {
				morphling.onEquippedAttack(player, morphStack, target, event.getNewDamage());
			}
		});
	}

	/**
	 * When the player kills a living entity while a morphling is equipped,
	 * delegate to the morphling's onEquippedKill for on-kill abilities
	 * (bonus XP, carrion harvest, decomposer drops, etc.).
	 */
	@SubscribeEvent
	public static void onPlayerKill(LivingDeathEvent event) {
		LivingEntity victim = event.getEntity();
		if (victim.level().isClientSide) return;
		if (!(event.getSource().getEntity() instanceof Player player)) return;

		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(morphCap -> {
			if (!morphCap.hasMorphling()) return;
			ItemStack morphStack = morphCap.getEquippedMorphling();
			if (morphStack.getItem() instanceof IMorphling morphling) {
				morphling.onEquippedKill(player, morphStack, victim);
			}
		});
	}

	/**
	 * When the player falls while a morphling is equipped, delegate to the
	 * morphling's onEquippedFall for fall-negation abilities (silk tether, etc.).
	 * If the morphling returns true, the fall event is cancelled.
	 */
	@SubscribeEvent
	public static void onPlayerFall(LivingFallEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;
		// Only trigger on falls that would actually cause damage (distance > 3)
		if (event.getDistance() <= 3.0f) return;

		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(morphCap -> {
			if (!morphCap.hasMorphling()) return;
			ItemStack morphStack = morphCap.getEquippedMorphling();
			if (morphStack.getItem() instanceof IMorphling morphling) {
				if (morphling.onEquippedFall(player, morphStack, event.getDistance())) {
					event.setCanceled(true);
				}
			}
		});
	}

	/**
	 * Server-side level tick handler that removes expired temporary web blocks
	 * placed by morphling abilities (e.g. Spider's Silk Tether).
	 */
	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (event.getLevel().isClientSide) return;
		if (TEMPORARY_WEBS.isEmpty()) return;

		long now = event.getLevel().getGameTime();
		String dimKey = event.getLevel().dimension().location().toString();

		Iterator<Map.Entry<String, Long>> it = TEMPORARY_WEBS.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Long> entry = it.next();
			if (now >= entry.getValue()) {
				// Parse the key: "dimension@x,y,z"
				String[] dimAndPos = entry.getKey().split("@");
				if (dimAndPos.length != 2) { it.remove(); continue; }
				if (!dimAndPos[0].equals(dimKey)) continue;

				String[] coords = dimAndPos[1].split(",");
				int x = Integer.parseInt(coords[0]);
				int y = Integer.parseInt(coords[1]);
				int z = Integer.parseInt(coords[2]);
				BlockPos pos = new BlockPos(x, y, z);

				// Only remove if it's still a cobweb (don't break blocks placed by the player)
				if (event.getLevel().getBlockState(pos).is(Blocks.COBWEB)) {
					event.getLevel().removeBlock(pos, false);
				}
				it.remove();
			}
		}
	}

	/**
	 * Places a temporary cobweb block at the given position. The web will
	 * auto-remove after {@link #TEMP_WEB_DURATION} ticks.
	 *
	 * @param level the server level
	 * @param pos   the position to place the web
	 */
	public static void placeTemporaryWeb(Level level, BlockPos pos) {
		if (level.isClientSide) return;
		// Only place if the space is air or replaceable
		if (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).canBeReplaced()) return;

		level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
		long expiryTick = level.getGameTime() + TEMP_WEB_DURATION;
		String key = level.dimension().location().toString() + "@"
				+ pos.getX() + "," + pos.getY() + "," + pos.getZ();
		TEMPORARY_WEBS.put(key, expiryTick);
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		syncToClient((ServerPlayer) event.getEntity());
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		syncToClient((ServerPlayer) event.getEntity());
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerRespawnEvent event) {
		if (!event.getEntity().level().isClientSide) {
			syncToClient((ServerPlayer) event.getEntity());
		}
	}

	public static void syncToClient(ServerPlayer player) {
		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
					new SyncEquippedMorphlingPacket(player.getUUID(), cap.getEquippedMorphling()));
		});
	}

}
