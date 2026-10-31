package com.vincenthuto.hemomancy.common.entity.boss.court;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtChampionRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtPresences;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * The usurpation (Brined Court Track 10, Tier 2): a worthy challenger asks the Head-tender to let them challenge the
 * Kindled Champion. The Head-tender checks {@link CourtLife#championGate} again; when it is open, the Champion steps out
 * of the nearest Kindled elder's place to fight ({@link KindledChampionEntity#callOut}). A challenger who dies has lost,
 * and the wait is recorded here as well as by the Champion, so a death the Champion doesn't see still counts.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class KindledChampionTrial {
	/** How far from the Head-tender it looks for the elder whose place the Champion takes. */
	public static final double ELDER_RANGE = 32.0D;

	private KindledChampionTrial() {
	}

	/**
	 * The Head-tender's answer to a challenge: the gate's refusal, or the Champion called out. Returns the Champion, or
	 * null when the gate is shut or a challenge for this player is already under way.
	 */
	@Nullable
	public static KindledChampionEntity challenge(ServerPlayer player, CourtMemberEntity tender) {
		CourtChampionRules.Gate gate = CourtLife.championGate(player);
		if (!gate.open()) {
			player.displayClientMessage(Component.translatable(gate.translationKey()).withStyle(ChatFormatting.GRAY), false);
			return null;
		}
		ServerLevel level = player.serverLevel();
		if (facing(level, player) != null) return null;
		CourtMemberEntity elder = level.getEntitiesOfClass(CourtMemberEntity.class, new AABB(tender.blockPosition()).inflate(ELDER_RANGE),
						member -> member.role() == CourtRole.KINDLED_ELDER && !(member instanceof KindledChampionEntity) && member.isAlive())
				.stream().min(Comparator.comparingDouble(member -> member.distanceToSqr(tender))).orElse(null);
		BlockPos home;
		float yaw;
		String mark;
		if (elder != null) {
			home = elder.home() != null ? elder.home() : elder.blockPosition();
			yaw = elder.home() != null ? elder.homeYaw() : elder.getYRot();
			mark = elder.mark().isEmpty() ? "kindled_elder" : elder.mark();
			elder.discard();
		} else {
			home = tender.blockPosition().relative(tender.getDirection(), 4);
			yaw = tender.getYRot() + 180.0F;
			mark = "kindled_elder";
		}
		tender.speakTo(player);
		CourtPresences.say(player, tender.getDisplayName(), Component.translatable("hemomancy.court.champion.called"));
		return KindledChampionEntity.callOut(level, home, yaw, mark, player);
	}

	/** The Champion currently facing {@code player} near them, if any. */
	@Nullable
	public static KindledChampionEntity facing(ServerLevel level, ServerPlayer player) {
		List<KindledChampionEntity> champions = level.getEntitiesOfClass(KindledChampionEntity.class,
				player.getBoundingBox().inflate(KindledChampionRules.LEASH * 2), champion -> champion.isFacing(player));
		return champions.isEmpty() ? null : champions.get(0);
	}

	/** A challenger who dies has lost: the Champion keeps the title and the wait starts. */
	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		KindledChampionEntity champion = facing(player.serverLevel(), player);
		if (champion != null) champion.end(KindledChampionRules.Outcome.CHALLENGER_DIED, player);
	}
}
