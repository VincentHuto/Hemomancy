package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.MemoryGrantResult;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.MemoryGrantStatus;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class MemoryBrazierRite {
	private MemoryBrazierRite() {
	}

	public static double tryAbsorb(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		if (!(level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier)) {
			return 0.0D;
		}
		ItemStack offering = brazier.getOfferingForMatching();
		BloodMemoryItem memory = offering.getItem() instanceof BloodMemoryItem bloodMemory ? bloodMemory : null;
		boolean lit = state.hasProperty(BrazierBlock.RITUAL_PHASE)
				&& state.getValue(BrazierBlock.RITUAL_PHASE) > 0;
		if (!MemoryBrazierAbsorptionRules.shouldAttempt(lit, memory != null,
				BloodAbsorptionItem.isChannelingBloodAbsorption(player), maxAmount)) {
			if (memory != null) {
				brazier.resetItemAbsorptionProgress();
			}
			return 0.0D;
		}

		MemoryGrantResult check = KnownManipulationGrantHelper.checkMemoryGrant(player, memory.getManip(), memory);
		if (!check.success()) {
			brazier.resetItemAbsorptionProgress();
			reportFailure(level, player, check);
			return maxAmount;
		}
		int progress = BrazierItemAbsorptionRite.advance(level, pos, player, brazier,
				"memory:" + memory.getManip().getName(), offering);
		if (!BrazierItemAbsorptionRite.isComplete(progress)) {
			return maxAmount;
		}

		MemoryGrantResult result = KnownManipulationGrantHelper.grantMemory(player, memory.getManip(), memory);
		if (!result.success()) {
			brazier.resetItemAbsorptionProgress();
			reportFailure(level, player, result);
			return maxAmount;
		}

		BrazierItemAbsorptionRite.complete(level, pos, brazier);
		player.displayClientMessage(Component.literal(result.status() == MemoryGrantStatus.GRANTED_EQUIPPED
					? "The burning memory enters your blood and settles into an open manipulation slot."
					: "The burning memory enters your blood. Recall it at a Mnemonic Reliquary.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		level.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 0.65F, 0.8F);
		level.sendParticles(GlowParticleFactory.createData(new ParticleColor(190, 0, 24)),
				pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
				32, 0.45D, 0.35D, 0.45D, 0.03D);
		return maxAmount;
	}

	private static void reportFailure(ServerLevel level, ServerPlayer player, MemoryGrantResult result) {
		if (level.getGameTime() % 20L != 0L) {
			return;
		}
		String text = switch (result.status()) {
			case ALREADY_KNOWN -> "Your blood already carries this memory.";
			case NO_ACTIVE_BLOOD -> "The memory finds no living blood to enter.";
			case RANK_TOO_LOW -> "This memory requires Degree " + result.requiredDegree() + ".";
			case MASTERY_TOO_LOW -> "Master " + result.familyId().replace('_', ' ') + " to stage "
					+ result.requiredMastery() + " before absorbing this memory.";
			case RETIRED -> "This memory has gone dormant.";
			default -> "The burning memory will not take.";
		};
		player.displayClientMessage(Component.literal(text).withStyle(ChatFormatting.DARK_RED), true);
	}
}
