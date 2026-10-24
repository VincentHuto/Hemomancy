package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.MemoryGrantResult;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.MemoryGrantStatus;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.rite.brazier.BrazierRite;
import com.vincenthuto.hemomancy.common.rite.brazier.BrazierRites;
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

/** Burning a Blood Memory into the caster's blood. */
public final class MemoryBrazierRite implements BrazierRite<BloodMemoryItem> {
	public static final MemoryBrazierRite INSTANCE = new MemoryBrazierRite();

	private MemoryBrazierRite() {
	}

	public static double tryAbsorb(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		return BrazierRites.run(INSTANCE, level, pos, state, player, maxAmount);
	}

	@Override
	public boolean accepts(ItemStack offering) {
		return offering.getItem() instanceof BloodMemoryItem;
	}

	@Override
	public Gate<BloodMemoryItem> preflight(Channel channel) {
		ServerPlayer player = channel.player();
		BloodMemoryItem memory = (BloodMemoryItem) channel.offering().getItem();
		if (!MemoryBrazierAbsorptionRules.shouldAttempt(channel.lit(), true,
				BloodAbsorptionItem.isChannelingBloodAbsorption(player), channel.maxAmount())) {
			return Gate.refuse(true, false);
		}
		MemoryGrantResult check = KnownManipulationGrantHelper.checkMemoryGrant(player, memory.getManip(), memory);
		if (!check.success()) {
			reportFailure(channel.level(), player, check);
			return Gate.refuse(true, true);
		}
		return Gate.pass("memory:" + memory.getManip().getName(), memory);
	}

	@Override
	public Completion complete(Channel channel, BloodMemoryItem memory) {
		ServerLevel level = channel.level();
		ServerPlayer player = channel.player();
		BlockPos pos = channel.pos();
		MemoryGrantResult result = KnownManipulationGrantHelper.grantMemory(player, memory.getManip(), memory);
		if (!result.success()) {
			reportFailure(level, player, result);
			return Completion.kept(true);
		}
		player.displayClientMessage((result.status() == MemoryGrantStatus.GRANTED_EQUIPPED ? Component.translatable("message.hemomancy.memory_brazier_rite.the_burning_memory_enters_your_blood") : Component.translatable("message.hemomancy.memory_brazier_rite.the_burning_memory_enters_your_blood_2")).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		level.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 0.65F, 0.8F);
		level.sendParticles(HemoParticleData.glow(new ParticleColor(190, 0, 24)),
				pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
				32, 0.45D, 0.35D, 0.45D, 0.03D);
		return Completion.CONSUMED;
	}

	private static void reportFailure(ServerLevel level, ServerPlayer player, MemoryGrantResult result) {
		Component text = switch (result.status()) {
			case ALREADY_KNOWN -> Component.translatable("message.hemomancy.memory_brazier_rite.already_known");
			case NO_ACTIVE_BLOOD -> Component.translatable("message.hemomancy.memory_brazier_rite.no_active_blood");
			case RANK_TOO_LOW -> Component.translatable("message.hemomancy.memory_brazier_rite.rank_too_low", result.requiredDegree());
			case MASTERY_TOO_LOW -> Component.translatable("message.hemomancy.memory_brazier_rite.mastery_too_low",
					result.familyId().replace('_', ' '), result.requiredMastery());
			case RETIRED -> Component.translatable("message.hemomancy.memory_brazier_rite.retired");
			default -> Component.translatable("message.hemomancy.memory_brazier_rite.will_not_take");
		};
		BrazierRites.messageEverySecond(level, player, text, ChatFormatting.DARK_RED);
	}
}
