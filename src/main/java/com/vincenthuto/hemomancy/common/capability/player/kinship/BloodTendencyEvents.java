package com.vincenthuto.hemomancy.common.capability.player.kinship;

import java.util.Map;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodTendencyServerPacket;
import com.vincenthuto.hemomancy.common.tile.VisceralRecallerBlockEntity;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodTendencyEvents {

	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(Hemomancy.rloc("bloodtendency"), new BloodTendencyProvider());
		}
	}

	@SubscribeEvent
	public static void attachCapabilitiesTile(final AttachCapabilitiesEvent<BlockEntity> event) {
		if (event.getObject() instanceof VisceralRecallerBlockEntity) {
			event.addCapability(Hemomancy.rloc("bloodtendency"), new BloodTendencyProvider());
		}
	}

	/**
	 * Killing entities shifts the player's blood tendency based on what they killed.
	 * <ul>
	 *   <li>Warm-blooded (animals, villagers) → ANIMUS (life/vitality)</li>
	 *   <li>Cold-blooded (drowned, water mobs, snow golem) → CONGEATIO (cold/ice)</li>
	 *   <li>Undead (zombies, skeletons, wither) → MORTEM (death)</li>
	 *   <li>Fire entities (blaze, magma cube, strider) → FLAMMEUS (fire)</li>
	 *   <li>Ender entities (enderman, shulker, endermite) → TENEBRIS (darkness)</li>
	 *   <li>Arthropods/vermin (spiders, silverfish) → DUCTILIS (flexible/nervous)</li>
	 *   <li>Iron entities (iron golem) → FERRIC (iron)</li>
	 *   <li>Glowing/phantom/light entities → LUX (light)</li>
	 * </ul>
	 * Only applies when the blood system is active.
	 */
	@SubscribeEvent
	public static void onEntityKilled(LivingDeathEvent event) {
		DamageSource source = event.getSource();
		if (source == null || !(source.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;
		if (!HemoServerConfig.TENDENCY_SHIFT_ON_KILL_ENABLED.get()) return;

		// Only shift tendency when the blood system is active
		boolean bloodActive = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.map(vol -> vol.isActive()).orElse(false);
		if (!bloodActive) return;

		LivingEntity victim = event.getEntity();
		float amount = HemoServerConfig.TENDENCY_SHIFT_AMOUNT.get().floatValue();

		EnumBloodTendency tendency = determineTendencyFromKill(victim);
		if (tendency == null) return;

		player.getCapability(BloodTendencyProvider.TENDENCY_CAPA).ifPresent(cap -> {
			cap.addTendencyAlignment(tendency, amount);
			syncTendency((ServerPlayer) player, cap);
		});
	}

	/**
	 * Determines which blood tendency a killed entity corresponds to.
	 * Returns null if the entity doesn't map to any tendency.
	 */
	private static EnumBloodTendency determineTendencyFromKill(LivingEntity entity) {
		// Bloodless entities don't shift tendency at all
		if (HemoEntityPredicates.NOBLOOD.test(entity)) {
			return null;
		}

		// Infernal / fire-aligned → FLAMMEUS
		if (HemoEntityPredicates.INFERNALBLOOD.test(entity)) {
			return EnumBloodTendency.FLAMMEUS;
		}

		// Ender / dark-aligned → TENEBRIS
		if (HemoEntityPredicates.ENDERBLOOD.test(entity)) {
			return EnumBloodTendency.TENEBRIS;
		}

		// Cold-blooded → CONGEATIO
		if (HemoEntityPredicates.COLDBLOODED.test(entity)) {
			return EnumBloodTendency.CONGEATIO;
		}

		// Plant-like / slime entities → DUCTILIS (flexible, nervous)
		if (HemoEntityPredicates.PLANTBLOOD.test(entity)) {
			return EnumBloodTendency.DUCTILIS;
		}

		// Undead → MORTEM
		if (entity.isInvertedHealAndHarm() || HemoEntityPredicates.UNDEAD.test(entity)) {
			return EnumBloodTendency.MORTEM;
		}

		// Warm-blooded (animals, villagers, players) → ANIMUS
		if (HemoEntityPredicates.WARMBLOODED.test(entity)) {
			return EnumBloodTendency.ANIMUS;
		}

		// Default fallback for anything with blood that doesn't match above
		return EnumBloodTendency.ANIMUS;
	}

	/**
	 * Public helper so other systems (e.g. KnownManipulationEvents) can shift
	 * tendency alignment when the player uses a manipulation of a given tendency.
	 */
	public static void shiftTendencyFromManipUse(ServerPlayer player, EnumBloodTendency tendency) {
		if (!HemoServerConfig.TENDENCY_SHIFT_ON_KILL_ENABLED.get()) return;
		float amount = HemoServerConfig.TENDENCY_SHIFT_ON_MANIP_USE.get().floatValue();
		if (amount <= 0) return;

		player.getCapability(BloodTendencyProvider.TENDENCY_CAPA).ifPresent(cap -> {
			cap.addTendencyAlignment(tendency, amount);
			syncTendency(player, cap);
		});
	}

	// ───── Sync & Lifecycle ─────

	public static void syncTendency(ServerPlayer player, IBloodTendency tendency) {
		PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> player),
				new BloodTendencyServerPacket(tendency.getTendency()));
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumBloodTendency, Float> BloodTendency = BloodTendencyProvider.getPlayerTendency(player);
		PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> player),
				new BloodTendencyServerPacket(BloodTendency));
	}


	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {

		Player peorig = event.getOriginal();
		Player playernew = event.getEntity();
		if (event.isWasDeath()) {
			peorig.reviveCaps();
			IBloodTendency bloodTendencyNew = playernew.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
					.orElseThrow(IllegalStateException::new);
			bloodTendencyNew.setTendency(peorig.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
					.orElseThrow(IllegalArgumentException::new).getTendency());
			peorig.invalidateCaps();
		}

	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumBloodTendency, Float> BloodTendency = BloodTendencyProvider.getPlayerTendency(player);
		PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> player),
				new BloodTendencyServerPacket(BloodTendency));
	}

	@SubscribeEvent
	public static void respawn(PlayerRespawnEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = event.getEntity();
			if (!player.getCommandSenderWorld().isClientSide) {
				IBloodTendency tendency = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
						new BloodTendencyServerPacket(tendency.getTendency()));
			}
		}
	}

}