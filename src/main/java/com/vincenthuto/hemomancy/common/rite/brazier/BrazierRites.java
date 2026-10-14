package com.vincenthuto.hemomancy.common.rite.brazier;

import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.LivingWeaponGraftRite;
import com.vincenthuto.hemomancy.common.rite.BrazierItemAbsorptionRite;
import com.vincenthuto.hemomancy.common.rite.MemoryBrazierRite;
import com.vincenthuto.hemomancy.common.rite.ScarBrazierRite;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** The Iron Brazier's offering rites and the absorption lifecycle they share. */
public final class BrazierRites {
	/** Tried in order; the first rite that handles an absorption tick owns it. */
	public static final List<BrazierRite<?>> RITES = List.of(MemoryBrazierRite.INSTANCE, ScarBrazierRite.INSTANCE,
			LivingWeaponGraftRite.INSTANCE);

	private BrazierRites() {
	}

	/** The brazier's absorption endpoint once its own blood has been dealt with. */
	public static double absorb(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		for (BrazierRite<?> rite : RITES) {
			double handled = run(rite, level, pos, state, player, maxAmount);
			if (handled > 0.0D) {
				return handled;
			}
		}
		return 0.0D;
	}

	/**
	 * One absorption tick of {@code rite}: preflight, then channel progress, then completion. Returns the blood the
	 * endpoint reports as drawn: {@code maxAmount} while the rite holds the channel, 0 when it does not apply.
	 */
	public static <P> double run(BrazierRite<P> rite, ServerLevel level, BlockPos pos, BlockState state,
			ServerPlayer player, double maxAmount) {
		if (!(level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier)) {
			return 0.0D;
		}
		ItemStack offering = brazier.getOfferingForMatching();
		if (!rite.accepts(offering)) {
			return 0.0D;
		}
		BrazierRite.Channel channel = new BrazierRite.Channel(level, pos, player, brazier, offering,
				BrazierBlock.isLit(state), maxAmount);
		BrazierRite.Gate<P> gate = rite.preflight(channel);
		if (!gate.passed()) {
			if (gate.resetProgress()) {
				brazier.resetItemAbsorptionProgress();
			}
			return gate.handled() ? maxAmount : 0.0D;
		}
		int progress = BrazierItemAbsorptionRite.advance(level, pos, player, brazier, gate.channelKey(), offering);
		if (!BrazierItemAbsorptionRite.isComplete(progress)) {
			return maxAmount;
		}
		BrazierRite.Completion completion = rite.complete(channel, gate.plan());
		if (completion.consumeOffering()) {
			BrazierItemAbsorptionRite.complete(level, pos, brazier);
		} else {
			brazier.resetItemAbsorptionProgress();
		}
		return completion.handled() ? maxAmount : 0.0D;
	}

	/** Action-bar feedback for a refused channel, at most once a second while absorption is held. */
	public static void messageEverySecond(ServerLevel level, Player player, String text, ChatFormatting color) {
		if (level.getGameTime() % 20L == 0L) {
			player.displayClientMessage(Component.literal(text).withStyle(color), true);
		}
	}
}
