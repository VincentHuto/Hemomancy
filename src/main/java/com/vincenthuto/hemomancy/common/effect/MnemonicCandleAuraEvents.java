package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.shared.MnemonicCandleBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MnemonicCandleAuraEvents {
	private MnemonicCandleAuraEvents() {
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		if (serverPlayer.level().getGameTime() % MnemonicCandleRules.AURA_INTERVAL_TICKS != 0) {
			return;
		}
		boolean activeBlood = HemoCapabilityAccess.getBloodVolume(serverPlayer)
				.map(IBloodVolume::isActive)
				.orElse(false);
		if (!activeBlood || !hasLitMnemonicCandleNearby(serverPlayer)) {
			return;
		}
		serverPlayer.addEffect(new MobEffectInstance(EffectInit.mnemonic_candle_aura,
				MnemonicCandleRules.AURA_DURATION_TICKS, 0, true, true, true));
	}

	private static boolean hasLitMnemonicCandleNearby(ServerPlayer player) {
		Level level = player.level();
		int radius = (int) Math.ceil(MnemonicCandleRules.AURA_RADIUS);
		BlockPos center = player.blockPosition();
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius),
				center.offset(radius, radius, radius))) {
			if (pos.distSqr(center) > MnemonicCandleRules.AURA_RADIUS * MnemonicCandleRules.AURA_RADIUS) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			if (state.is(BlockInit.mnemonic_candle.get())
					&& state.hasProperty(MnemonicCandleBlock.LIT)
					&& state.getValue(MnemonicCandleBlock.LIT)) {
				return true;
			}
		}
		return false;
	}
}
