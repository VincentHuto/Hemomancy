package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCastingRules;
import com.vincenthuto.hemomancy.common.manipulation.animus.AvatarManifestationRules;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SyncTrackingAvatarPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Collection;
import java.util.Collections;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class KnownManipulationEvents {
	@SubscribeEvent
	public static void refreshPlayerDimensions(PlayerTickEvent.Post event) {
		event.getEntity().refreshDimensions();
	}

	@SubscribeEvent
	public static void onPlayerDamage(LivingDamageEvent.Pre e) {

		// Radiant Protection. The void, /kill and hunger are not blows the avatar can turn.
		if (e.getEntity() instanceof Player player && !bypassesAvatar(e.getSource())) {
			IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
					;
			AvatarManifestationRules.stats(known.getActiveAvatarForm()).ifPresent(stats -> {
				double dist = e.getEntity().distanceToSqr(player);
				HitResult trace = e.getEntity().pick(dist, 0, false);
				if (e.getEntity().level() instanceof ServerLevel serverLevel) {
					PacketHandler.sendAvatarHitParticles(trace.getLocation(), ParticleColor.WHITE, 16f, serverLevel);
				}
				e.setNewDamage((float) (e.getNewDamage() * (1.0D - stats.damageReduction())));
			});
		}

	}

	static boolean bypassesAvatar(DamageSource source) {
		return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.is(DamageTypes.STARVE);
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		Entity target = event.getTarget();
		if (target instanceof ServerPlayer) {
			syncAvatars((ServerPlayer) target, Collections.singletonList(event.getEntity()));
		}
	}

	/** Sends the owner their known manipulations now; the end-of-tick flush then has nothing left to send. */
	public static void syncPlayerEvent(Player playerEntity) {
		if (playerEntity instanceof ServerPlayer s) {
			var known = HemoCapabilityAccess.getKnownManipulations(s);
			PacketHandler.sendToPlayer(s, new KnownManipulationServerPacket(known));
			known.clearSyncDirty();
		}
	}

	@SubscribeEvent
	public static void playerJoin(EntityJoinLevelEvent event) {
		HemoCapabilityAccess.getKnownManipulations(event.getEntity()).ifPresent(manips -> {
			if(manips.getLastVeinMineStart() != BlockPos.ZERO) {
				manips.setLastVeinMineStart(BlockPos.ZERO);
			}
		});
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		KnownManipulationGrantHelper.grantDegreeOneUtilities((ServerPlayer) event.getEntity());
	}

	public static void syncAvatar(Player player, Collection<? extends Player> receivers, String avatarForm) {
		SyncTrackingAvatarPacket pkt = new SyncTrackingAvatarPacket(player.getId(), avatarForm);
		for (Player receiver : receivers) {
			PacketHandler.sendToPlayer((ServerPlayer) receiver, pkt);
		}
	}

	public static void syncAvatar(Player player, Collection<? extends Player> receivers, boolean active) {
		syncAvatar(player, receivers, active ? "summon_avatar" : "");
	}

	private static void syncAvatars(Player player, Collection<? extends Player> receivers) {
		var manips = HemoCapabilityAccess.getKnownManipulations(player);
		syncAvatar(player, receivers, manips.getActiveAvatarForm());
	}

	/** Tells the owner which avatar form they wear, for their own render. */
	public static void syncOwnAvatar(ServerPlayer player) {
		syncAvatars(player, Collections.singletonList(player));
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
		onManipulationUsed(player, manip, 1.0F);
	}

	/**
	 * {@code rewardScale} is the cast's charge fraction for CHARGED manipulations (1 otherwise): alignment and
	 * skill points scale with it, and a cast below half charge earns no mastery use and does not count toward
	 * the manipulation-use milestones.
	 */
	public static void onManipulationUsed(ServerPlayer player, BloodManipulation manip, float rewardScale) {
		com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager.onCast(player);
		// 1. Vascular strain on the manip's associated vein section
		VascularSystemEvents.applyManipStrain(player, manip);

		// 2. Tendency shift toward the manip's tendency
		BloodTendencyEvents.shiftTendencyFromManipUse(player, manip.getTend(), manip.getSecondaryTend(), rewardScale);

		// 3. Skill: Vital Link chance to heal when using a manipulation
		double vitalLinkChance = SkillPointHelper.getVitalLinkChance(player);
		if (vitalLinkChance > 0 && player.level().random.nextDouble() < vitalLinkChance) {
			player.heal(2.0f); // Heal 2.0 health (1 heart) on successful proc
		}

		// 4. Grant XP to the manipulation's level and check for rank-up
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		ManipLevel level = known.getManipLevel(manip);
		if (level != null && ManipulationCastingRules.earnsMasteryUse(rewardScale)) {
			level.setXp(level.getXp() + 1.0);
			if (level.tryLevelUp()) {
				player.displayClientMessage(
						net.minecraft.network.chat.Component.translatable(
								"hemomancy.manipulation.levelup",
								manip.getDisplayName(),
								level.getCurrentLevel()),
						false);
			}
			ManipulationFamilyRegistry.normalizeKnown(known.getKnownManips());
		}
		syncPlayerEvent(player);

		// 5. Grant skill-point currency based on manipulation rank
		int spGain = switch (manip.getRank()) {
			case HUMILIS      -> 1;
			case MEDIOCRITAS   -> 2;
			case SUMMA         -> 3;
			case MAGISTER      -> 4;
			case PERFECTUS     -> 5;
		};
		SkillProgress progress = HemoCapabilityAccess.getSkillProgress(player);
		progress.addSkillPoints(ManipulationCastingRules.scaledSkillPoints(spGain, rewardScale));

		// 6. Check manipulation-use milestones (first use, tiered totals); like mastery, a cast below half
		// charge does not count as a use.
		if (ManipulationCastingRules.earnsMasteryUse(rewardScale)) SkillPointGainEvents.onManipulationUsed(player);

		// Sync skill tree (which includes skill-point balance) back to client
		SkillPointGainEvents.syncSkills(player);
	}

}
