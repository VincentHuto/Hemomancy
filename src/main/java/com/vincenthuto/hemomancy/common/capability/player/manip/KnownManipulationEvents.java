package com.vincenthuto.hemomancy.common.capability.player.manip;

import java.util.Collection;
import java.util.Collections;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.quick.SummonThrallManip;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncSkills;
import com.vincenthuto.hemomancy.common.network.capa.manips.KnownManipulationServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.manips.SyncTrackingAvatarPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KnownManipulationEvents {
	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject().getType() == EntityType.PLAYER) {
			event.addCapability(Hemomancy.rloc("knownmanipulations"), new KnownManipulationProvider());
		}
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(IllegalStateException::new);
		PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> player),
				new KnownManipulationServerPacket(known));
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerTickEvent event) {
		event.player.refreshDimensions();
		syncPlayerEvent(event.player);
	}

	@SubscribeEvent
	public static void onPlayerDamage(LivingDamageEvent e) {

		// Radiant Protection
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(NullPointerException::new);
			if (known.isAvatarActive()) {
				double dist = e.getEntity().distanceToSqr(player);
				HitResult trace = e.getEntity().pick(dist, 0, false);
				PacketHandler.sendAvatarHitParticles(trace.getLocation(), ParticleColor.WHITE, 16f,
						e.getEntity().level().dimension());
				e.setAmount(e.getAmount() * 0);

			}
		}

	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		Entity target = event.getTarget();
		if (target instanceof ServerPlayer) {
			syncAvatars((ServerPlayer) target, Collections.singletonList(event.getEntity()));
		}
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {

		if (event.getOriginal().level().isClientSide)
			return;

		Player peorig = event.getOriginal();
		Player playernew = event.getEntity();
		peorig.reviveCaps();

		peorig.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(oldCap -> {
			playernew.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(newCap -> {
				newCap.setCapa(oldCap);
			});
		});

		peorig.invalidateCaps();

	}

	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.PlayerRespawnEvent e) {
		syncPlayerEvent(e.getEntity());
	}

	public static void syncPlayerEvent(Player playerEntity) {
		if (playerEntity instanceof ServerPlayer s) {
			s.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(capa -> {
				PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> s),
						new KnownManipulationServerPacket(capa));
			});
		}
	}

	@SubscribeEvent
	public static void playerJoin(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer) entity;
			syncAvatars(player, Collections.singletonList(player));

		}
		
		entity.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manips -> {
			if(manips.getLastVeinMineStart() != BlockPos.ZERO) {
				manips.setLastVeinMineStart(BlockPos.ZERO);
			}
		});
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(IllegalStateException::new);
		PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> player),
				new KnownManipulationServerPacket(known));

	}

	@SubscribeEvent
	public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		// Clean up any pending thrall awaiting target selection
		SummonThrallManip.clearPendingThrall(event.getEntity().getUUID());
	}

	public static void syncAvatar(Player player, Collection<? extends Player> receivers, boolean isAvatarActive) {
		SyncTrackingAvatarPacket pkt = new SyncTrackingAvatarPacket(player.getId(), isAvatarActive);
		for (Player receiver : receivers) {
			PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver), pkt);
		}
	}

	private static void syncAvatars(Player player, Collection<? extends Player> receivers) {
		player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manips -> {
			syncAvatar(player, receivers, manips.isAvatarActive());
		});
	}

	/**
	 * Call this from BloodManipulation.performAction (or wherever manipulations are executed)
	 * to apply cross-system consequences when a manipulation is used:
	 * <ul>
	 *   <li>Strain the manipulation's associated vein section</li>
	 *   <li>Shift tendency alignment toward the manipulation's tendency</li>
	 *   <li>Grant XP to the manipulation's level</li>
	 *   <li>Grant skill-point currency (higher-rank manips give more)</li>
	 *   <li>Check for milestone rewards (first use, tiered totals)</li>
	 * </ul>
	 */
	public static void onManipulationUsed(ServerPlayer player, BloodManipulation manip) {
		// 1. Vascular strain on the manip's associated vein section
		VascularSystemEvents.applyManipStrain(player, manip.getSection());

		// 2. Tendency shift toward the manip's tendency
		BloodTendencyEvents.shiftTendencyFromManipUse(player, manip.getTend());

		// 3. Skill: Vital Link — chance to heal when using a manipulation
		double vitalLinkChance = com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper.getVitalLinkChance();
		if (vitalLinkChance > 0 && player.level().random.nextDouble() < vitalLinkChance) {
			player.heal(2.0f); // Heal 2.0 health (1 heart) on successful proc
		}

		// 4. Grant XP to the manipulation's level
		player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(known -> {
			ManipLevel level = known.getManipLevel(manip);
			if (level != null && level != ManipLevel.BLANK) {
				level.setXp(level.getXp() + 1.0);
			}
			PacketHandler.CHANNELKNOWNMANIPS.send(
					PacketDistributor.PLAYER.with(() -> player),
					new KnownManipulationServerPacket(known));
		});

		// 5. Grant skill-point currency based on manipulation rank
		int spGain = switch (manip.getRank()) {
			case HUMILIS      -> 1;
			case MEDIOCRITAS   -> 2;
			case SUMMA         -> 3;
			case MAGISTER      -> 4;
			case PERFECTUS     -> 5;
		};
		SkillPointInit.skillPoints += spGain;

		// 6. Check manipulation-use milestones (first use, tiered totals)
		SkillPointGainEvents.onManipulationUsed(player);

		// Sync skill tree (which includes skill-point balance) back to client
		PacketHandler.CHANNELBLOODVOLUME.send(
				PacketDistributor.PLAYER.with(() -> player),
				new PacketSyncSkills(SkillPointInit.serializeAll()));
	}

}