package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.CoagulationRules;
import com.vincenthuto.hutoslib.client.particle.data.ColorParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.registry.HLParticleInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

/**
 * The Blood Formation key: a sword bleeds the body straight into one Sanguine Formation. Core-loop Track 3 prices it
 * above the Sanguine Condenser, so the deliberate way to make Formations stays the cheap one and this is the urgent one.
 */
public final class BodyFormationDraw {
	public static final BloodAccounting.Source FORMATION =
			BloodAccounting.Source.of("blood_formation", Category.SKILL, false);
	/** Twice the Sanguine Cauldron's 150 mL; always one Formation. */
	public static final double COST = 2.0D * CoagulationRules.condenserMlPerFormation(0);

	private BodyFormationDraw() {
	}

	public static void draw(ServerPlayer player) {
		if (ChamberVisitService.isObservational(player)) return;
		if (UnstainedAccessRules.blocksHarbingerProgress(HemoCapabilityAccess.getUnstainedProgress(player))) return;
		if (!(player.getMainHandItem().getItem() instanceof SwordItem)) return;
		if (!BloodAccounting.canPayInFull(player, COST, FORMATION)) {
			player.displayClientMessage(Component.translatable(
					"message.hemomancy.blood_formation_key_press_packet.not_enough_blood_can_be_drawn"), true);
			return;
		}
		BloodAccounting.pay(player, COST, FORMATION);
		player.displayClientMessage(Component.translatable(
				"message.hemomancy.blood_formation_key_press_packet.blood_has_been_drawn_for_a"), true);
		ServerLevel level = player.serverLevel();
		BlockPos pos = player.blockPosition();
		RandomSource random = level.random;
		for (int i = 0; i < 30; i++) {
			level.sendParticles(new ColorParticleData(HLParticleInit.glow.get(), new ParticleColor(255 * random.nextFloat(), 0, 0)),
					pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 1,
					pos.getZ() + random.nextDouble(), 1, 0f, 0.2f, 0f, random.nextInt(3) * 0.015f);
		}
		player.drop(new ItemStack(ItemInit.sanguine_formation.get()), false);
	}
}
