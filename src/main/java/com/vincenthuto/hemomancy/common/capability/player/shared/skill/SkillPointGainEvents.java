package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncSkills;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class SkillPointGainEvents {
	private static final String HEMO_ADV_PREFIX = "hemomancy:hemomancy/";
	private static final java.util.Map<String, String> ADV_TO_MILESTONE = java.util.Map.of(
			"root", "strange_seeds"
	);

	public static void syncSkills(ServerPlayer player) {
		PacketHandler.sendToPlayer(player,
				new PacketSyncSkills(HemoCapabilityAccess.requireSkillProgress(player).toSyncTag()));
	}

	@SubscribeEvent
	public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
		Player player = event.getEntity();
		if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) return;

		ResourceLocation advId = event.getAdvancement().id();
		String fullId = advId.toString();
		if (!fullId.startsWith(HEMO_ADV_PREFIX)) return;

		SkillProgress progress = HemoCapabilityAccess.requireSkillProgress(serverPlayer);
		boolean changed = false;
		String advName = fullId.substring(HEMO_ADV_PREFIX.length());
		String milestoneId = ADV_TO_MILESTONE.getOrDefault(advName, advName);
		changed |= progress.tryAwardMilestone(HemoMilestone.byId(milestoneId));

		int total = progress.incrementHemoAdvancements();
		if (total >= 5) changed |= progress.tryAwardMilestone(HemoMilestone.HEMO_SCHOLAR_I);
		if (total >= 10) changed |= progress.tryAwardMilestone(HemoMilestone.HEMO_SCHOLAR_II);

		if (changed) syncSkills(serverPlayer);
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		LivingEntity victim = event.getEntity();
		if (victim.level().isClientSide) return;
		if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
		if (victim.getType().getCategory() != MobCategory.MONSTER) return;

		boolean bloodActive = HemoCapabilityAccess.getBloodVolume(player)
				.map(vol -> vol.isActive() && vol.getBloodVolume() > 0)
				.orElse(false);
		if (!bloodActive) return;

		SkillProgress progress = HemoCapabilityAccess.requireSkillProgress(player);
		int total = progress.incrementKillsWithBlood();
		boolean changed = false;
		if (total >= 10) changed |= progress.tryAwardMilestone(HemoMilestone.BLOOD_SPILLER_I);
		if (total >= 50) changed |= progress.tryAwardMilestone(HemoMilestone.BLOOD_SPILLER_II);
		if (total >= 100) changed |= progress.tryAwardMilestone(HemoMilestone.BLOOD_SPILLER_III);

		if (changed) syncSkills(player);
	}

	public static void onManipulationUsed(ServerPlayer player) {
		SkillProgress progress = HemoCapabilityAccess.requireSkillProgress(player);
		int total = progress.incrementManipulationUses();
		boolean changed = false;

		changed |= progress.tryAwardMilestone(HemoMilestone.FIRST_MANIPULATION);
		if (total >= 25) changed |= progress.tryAwardMilestone(HemoMilestone.MANIPULATION_NOVICE);
		if (total >= 100) changed |= progress.tryAwardMilestone(HemoMilestone.MANIPULATION_ADEPT);
		if (total >= 500) changed |= progress.tryAwardMilestone(HemoMilestone.MANIPULATION_MASTER);

		if (changed) syncSkills(player);
	}

	public static void onRiteCompleted(ServerPlayer player) {
		SkillProgress progress = HemoCapabilityAccess.requireSkillProgress(player);
		int total = progress.incrementRitesCompleted();
		boolean changed = false;

		changed |= progress.tryAwardMilestone(HemoMilestone.FIRST_RITE);
		if (total >= 3) changed |= progress.tryAwardMilestone(HemoMilestone.RITE_PRACTITIONER);
		if (total >= 10) changed |= progress.tryAwardMilestone(HemoMilestone.RITE_VETERAN);

		if (changed) syncSkills(player);
	}

	public static void onDegreeReached(ServerPlayer player, int degree) {
		SkillProgress progress = HemoCapabilityAccess.requireSkillProgress(player);
		boolean changed = switch (degree) {
			case 1 -> progress.tryAwardMilestone(HemoMilestone.SANGUINE_INITIATION);
			case 2 -> progress.tryAwardMilestone(HemoMilestone.VOTARY_ASCENSION);
			case 3 -> progress.tryAwardMilestone(HemoMilestone.INITIATE_ASCENSION);
			case 4 -> progress.tryAwardMilestone(HemoMilestone.ADEPT_ASCENSION);
			case 5 -> progress.tryAwardMilestone(HemoMilestone.ILLUMINATUS_ASCENSION);
			case 6 -> progress.tryAwardMilestone(HemoMilestone.SANCTIFIED_ASCENSION);
			case 7 -> progress.tryAwardMilestone(HemoMilestone.ARCHON_ASCENSION);
			case 8 -> progress.tryAwardMilestone(HemoMilestone.APOTHEOS_ASCENSION);
			default -> false;
		};

		int excess = ManipSlotHelper.getExcessSkillLevels(player);
		if (excess > 0 && SkillPointInit.skill_manip_slots != null) {
			int currentLevel = progress.getLevel(SkillPointInit.skill_manip_slots);
			progress.setSkillLevel(SkillPointInit.skill_manip_slots, currentLevel - excess);
			progress.addSkillPoints(excess);
			changed = true;
		}

		if (changed) syncSkills(player);
	}

	public static void onMorphlingEquipped(ServerPlayer player) {
		SkillProgress progress = HemoCapabilityAccess.requireSkillProgress(player);
		if (progress.tryAwardMilestone(HemoMilestone.FIRST_MORPHLING_BOND)) syncSkills(player);
	}

	public static void onMemoryWeavingCompleted(ServerPlayer player) {
		SkillProgress progress = HemoCapabilityAccess.requireSkillProgress(player);
		if (progress.tryAwardMilestone(HemoMilestone.FIRST_MEMORY_WEAVING)) syncSkills(player);
	}

	public static void onBloodlineJoined(ServerPlayer player) {
		SkillProgress progress = HemoCapabilityAccess.requireSkillProgress(player);
		if (progress.tryAwardMilestone(HemoMilestone.FIRST_BLOODLINE)) syncSkills(player);
	}
}
