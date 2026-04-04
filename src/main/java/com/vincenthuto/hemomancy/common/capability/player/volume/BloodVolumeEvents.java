package com.vincenthuto.hemomancy.common.capability.player.volume;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.IBloodTile;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodVolumeEvents {
	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			BloodVolumeProvider provider = new BloodVolumeProvider();
			event.addCapability(Hemomancy.rloc("bloodvolume"), provider);
		}
	}

	@SubscribeEvent
	public static void attachCapabilitiesItemStack(final AttachCapabilitiesEvent<ItemStack> event) {
		if (event.getObject().getItem() instanceof BloodGourdItem) {
			event.addCapability(Hemomancy.rloc("bloodvolume"), new BloodVolumeProvider());
		}
	}

	@SubscribeEvent
	public static void attachCapabilitiesTile(final AttachCapabilitiesEvent<BlockEntity> event) {
		if (event.getObject() instanceof IBloodTile) {
			event.addCapability(Hemomancy.rloc("bloodvolume"), new BloodVolumeProvider());
		}
	}

	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		Player player = event.player;
		if (player.level().isClientSide) return;

		player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
			if (volume.isActive() && HemoServerConfig.BLOOD_REGEN_ENABLED.get()) {
				int interval = HemoServerConfig.BLOOD_REGEN_INTERVAL.get();
				if (player.tickCount % interval == 0 && !volume.isFull()) {
					double regenRate = HemoServerConfig.BLOOD_REGEN_RATE.get();
					volume.fill(regenRate);
					syncVolume((ServerPlayer) player, volume);
				}
			}
		});
	}

	/**
	 * When the player takes damage, drain blood proportional to the damage dealt.
	 * Wounds cause blood loss — this is the core cost of being reckless in combat.
	 */
	@SubscribeEvent
	public static void onPlayerDamaged(LivingDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;
		if (!HemoServerConfig.BLOOD_DRAIN_ON_DAMAGE_ENABLED.get()) return;

		player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
			if (volume.isActive()) {
				double drainAmount = event.getAmount() * HemoServerConfig.BLOOD_DRAIN_PER_DAMAGE.get();
				volume.drain(drainAmount);
				syncVolume((ServerPlayer) player, volume);

				// Warn the player when blood is critically low
				if (volume.getBloodVolume() < volume.getMaxBloodVolume() * 0.1 && volume.getBloodVolume() > 0) {
					player.displayClientMessage(
							Component.literal("Your blood runs thin...")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
				}
			}
		});
	}

	/**
	 * When the player kills a living entity, gain blood from the slain creature.
	 * Bloodless entities (skeletons, golems, etc.) yield nothing.
	 * Bosses yield significantly more.
	 */
	@SubscribeEvent
	public static void onEntityKilledByPlayer(LivingDeathEvent event) {
		DamageSource source = event.getSource();
		if (source == null || !(source.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;
		if (!HemoServerConfig.BLOOD_GAIN_ON_KILL_ENABLED.get()) return;

		LivingEntity victim = event.getEntity();

		// Bloodless entities yield no blood
		if (HemoEntityPredicates.NOBLOOD.test(victim)) return;

		player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
			if (volume.isActive()) {
				double baseGain = HemoServerConfig.BLOOD_GAIN_PER_KILL.get();

				// Scale with victim max health — bigger creatures have more blood
				double healthScale = Math.max(1.0, victim.getMaxHealth() / 20.0);
				double gain = baseGain * healthScale;

				// Boss multiplier
				if (!victim.canChangeDimensions()) {
					gain *= HemoServerConfig.BLOOD_GAIN_BOSS_MULTIPLIER.get();
				}

				volume.fill(gain);
				syncVolume((ServerPlayer) player, volume);
			}
		});
	}

	// ───── Utility ─────

	public static void syncVolume(ServerPlayer player, IBloodVolume volume) {
		PacketHandler.CHANNELBLOODVOLUME.send(
				PacketDistributor.PLAYER.with(() -> player),
				new BloodVolumeServerPacket(volume));
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		syncVolume(player, volume);
		player.displayClientMessage(
				Component.literal(
						"Welcome! Current Blood Volume: " + ChatFormatting.GOLD + volume.getBloodVolume() + "ml"),
				false);
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			Player peorig = event.getOriginal();
			peorig.revive();
			IBloodVolume bloodVolumeOld = peorig.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(IllegalStateException::new);

			Player playernew = event.getEntity();
			peorig.reviveCaps();

			IBloodVolume bloodVolumeNew = playernew.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(IllegalStateException::new);
			bloodVolumeNew.setActive(bloodVolumeOld.isActive());
			bloodVolumeNew.setBloodVolume(bloodVolumeOld.getBloodVolume());
			bloodVolumeNew.setMaxBloodVolume(bloodVolumeOld.getMaxBloodVolume());
			bloodVolumeNew.setBloodLine(bloodVolumeOld.getBloodLine());
			peorig.invalidateCaps();
		}

	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		syncVolume(player, volume);
		player.displayClientMessage(
				Component.literal("Welcome! Blood Active? " + ChatFormatting.LIGHT_PURPLE + volume.isActive()), false);
		player.displayClientMessage(
				Component.literal(
						"Welcome! Current Blood Volume: " + ChatFormatting.GOLD + volume.getBloodVolume() + "ml"),
				false);
		player.displayClientMessage(
				Component.literal("Welcome! Current Bloodline: " + ChatFormatting.GOLD + volume.getBloodLine().name),
				false);
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerRespawnEvent event) {
		Player playernew = event.getEntity();
		if (!playernew.level().isClientSide) {
			IBloodVolume bloodVolumeNew = playernew.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(IllegalStateException::new);
			syncVolume((ServerPlayer) playernew, bloodVolumeNew);
		}
	}

}
