package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.animus.SummonThrallManip;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.KnownManipulationServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.manips.SyncTrackingAvatarPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Collection;
import java.util.Collections;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class KnownManipulationEvents {	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
				.orElseThrow(IllegalStateException::new);
		PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerTickEvent.Post event) {
		event.getEntity().refreshDimensions();
		syncPlayerEvent(event.getEntity());
	}

	@SubscribeEvent
	public static void onPlayerDamage(LivingDamageEvent.Pre e) {

		// Radiant Protection
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
					.orElseThrow(NullPointerException::new);
			if (known.isAvatarActive()) {
				double dist = e.getEntity().distanceToSqr(player);
				HitResult trace = e.getEntity().pick(dist, 0, false);
				if (e.getEntity().level() instanceof ServerLevel serverLevel) {
					PacketHandler.sendAvatarHitParticles(trace.getLocation(), ParticleColor.WHITE, 16f, serverLevel);
				}
				e.setNewDamage(0);

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
	public static void playerRespawn(PlayerEvent.PlayerRespawnEvent e) {
		syncPlayerEvent(e.getEntity());
	}

	public static void syncPlayerEvent(Player playerEntity) {
		if (playerEntity instanceof ServerPlayer s) {
			HemoCapabilityAccess.getKnownManipulations(s).ifPresent(capa -> {
				PacketHandler.sendToPlayer(s, new KnownManipulationServerPacket(capa));
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
		
		HemoCapabilityAccess.getKnownManipulations(entity).ifPresent(manips -> {
			if(manips.getLastVeinMineStart() != BlockPos.ZERO) {
				manips.setLastVeinMineStart(BlockPos.ZERO);
			}
		});
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
				.orElseThrow(IllegalStateException::new);
		KnownManipulationGrantHelper.grantDegreeOneUtilities(player);
		PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));

	}

	@SubscribeEvent
	public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		// Clean up any pending thrall awaiting target selection
		SummonThrallManip.clearPendingThrall(event.getEntity().getUUID());
	}

	public static void syncAvatar(Player player, Collection<? extends Player> receivers, boolean isAvatarActive) {
		SyncTrackingAvatarPacket pkt = new SyncTrackingAvatarPacket(player.getId(), isAvatarActive);
		for (Player receiver : receivers) {
			PacketHandler.sendToPlayer((ServerPlayer) receiver, pkt);
		}
	}

	private static void syncAvatars(Player player, Collection<? extends Player> receivers) {
		HemoCapabilityAccess.getKnownManipulations(player).ifPresent(manips -> {
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

		// 3. Skill: Vital Link â€” chance to heal when using a manipulation
		double vitalLinkChance = SkillPointHelper.getVitalLinkChance(player);
		if (vitalLinkChance > 0 && player.level().random.nextDouble() < vitalLinkChance) {
			player.heal(2.0f); // Heal 2.0 health (1 heart) on successful proc
		}

		// 4. Grant XP to the manipulation's level and check for rank-up
		HemoCapabilityAccess.getKnownManipulations(player).ifPresent(known -> {
			ManipLevel level = known.getManipLevel(manip);
			if (level != null && level != ManipLevel.BLANK) {
				level.setXp(level.getXp() + 1.0);
				if (level.tryLevelUp()) {
					player.displayClientMessage(
							net.minecraft.network.chat.Component.translatable(
									"hemomancy.manipulation.levelup",
									manip.getProperName(),
									level.getCurrentLevel()),
							false);
				}
			}
			PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
		});

		// 5. Grant skill-point currency based on manipulation rank
		int spGain = switch (manip.getRank()) {
			case HUMILIS      -> 1;
			case MEDIOCRITAS   -> 2;
			case SUMMA         -> 3;
			case MAGISTER      -> 4;
			case PERFECTUS     -> 5;
		};
		SkillProgress progress = HemoCapabilityAccess.requireSkillProgress(player);
		progress.addSkillPoints(spGain);

		// 6. Check manipulation-use milestones (first use, tiered totals)
		SkillPointGainEvents.onManipulationUsed(player);

		// Sync skill tree (which includes skill-point balance) back to client
		SkillPointGainEvents.syncSkills(player);
	}

}
