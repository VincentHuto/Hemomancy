package com.vincenthuto.hemomancy.common.block.functional;

import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.encounter.HarbingerSaintEncounterHooks;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SaintSarcophagusBlock extends Block {
	private static final float BASE_SUCCESS_CHANCE = 0.35F;
	private static final float MAX_TENDENCY_BONUS = 0.40F;

	public SaintSarcophagusBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (level.isClientSide) {
			return InteractionResult.sidedSuccess(true);
		}

		float successChance = computeSuccessChance(player);
		boolean success = level.random.nextFloat() < successChance;
		if (success) {
			awardSaintEnzyme(player);
			level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.1F);
			player.displayClientMessage(Component.translatable("message.hemomancy.saint_sarcophagus.success"), false);
		} else {
			if (level instanceof ServerLevel serverLevel) {
				HarbingerSaintEncounterHooks.spawnSaintBoss(serverLevel, pos, player);
			}
			level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.7F, 1.2F);
			player.displayClientMessage(Component.translatable("message.hemomancy.saint_sarcophagus.fail"), false);
		}

		// Single-use by design: once a Saint encounter resolves, this sarcophagus is
		// consumed to prevent repeat enzyme farming from one structure spawn.
		level.removeBlock(pos, false);
		return InteractionResult.CONSUME;
	}

	private float computeSuccessChance(Player player) {
		float tendencyBonus = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
				.map(cap -> {
					float total = cap.getTotalAlignment();
					if (total <= 0) return 0.0F;
					float lux = cap.getAlignmentByTendency(EnumBloodTendency.LUX);
					return Mth.clamp((lux / total) * MAX_TENDENCY_BONUS, 0.0F, MAX_TENDENCY_BONUS);
				})
				.orElse(0.0F);
		return Mth.clamp(BASE_SUCCESS_CHANCE + tendencyBonus, 0.05F, 0.95F);
	}

	private void awardSaintEnzyme(Player player) {
		ItemStack reward = new ItemStack(ItemInit.saint_enzyme.get());
		if (!player.addItem(reward)) {
			player.drop(reward, false);
		}
	}
}
