package com.vincenthuto.hemomancy.common.rite.brazier;

import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * A rite performed by channelling absorption on a lit Iron Brazier until its seated offering is drawn out: the shared
 * Brazier offering lifecycle (ordinary insertion, Blood Projection ignition, held absorption through completion).
 * {@link BrazierRites} runs the lifecycle; a rite only says which offerings it claims, what must hold before the channel
 * may advance, and what completion does.
 *
 * @param <P> the plan a passing preflight hands to {@link #complete}, so completion reuses what preflight validated
 *            instead of checking it again
 */
public interface BrazierRite<P> {
	/** Whether this rite claims the seated offering, whatever the brazier's state or the caster's grip. */
	boolean accepts(ItemStack offering);

	/**
	 * Runs on every absorption tick before channel progress advances, so a caster who stops qualifying mid-channel
	 * loses the channel. Refusals send their own throttled feedback through {@link BrazierRites#messageEverySecond}.
	 */
	Gate<P> preflight(Channel channel);

	/**
	 * Applies the rite once the channel is complete, on the same tick as the preflight that produced {@code plan}.
	 * {@link BrazierRites} then consumes the offering and puts the fire out, or keeps the offering and drops the
	 * channel, as the result says.
	 */
	Completion complete(Channel channel, P plan);

	/** One absorption tick on a brazier holding an offering this rite claims. */
	record Channel(ServerLevel level, BlockPos pos, ServerPlayer player, IronBrazierBlockEntity brazier,
			ItemStack offering, boolean lit, double maxAmount) {
	}

	/**
	 * A preflight result. A pass names the channel (progress restarts when the key or the caster changes) and carries
	 * the plan. A refusal says whether channel progress is dropped and whether the tick still counts as handled, which
	 * the absorption endpoint reports as {@link Channel#maxAmount()} instead of 0.
	 */
	record Gate<P>(boolean passed, String channelKey, P plan, boolean resetProgress, boolean handled) {
		public static <P> Gate<P> pass(String channelKey, P plan) {
			return new Gate<>(true, channelKey, plan, false, true);
		}

		public static <P> Gate<P> refuse(boolean resetProgress, boolean handled) {
			return new Gate<>(false, "", null, resetProgress, handled);
		}
	}

	/** A completion result: whether the offering is consumed and whether the tick counts as handled. */
	record Completion(boolean consumeOffering, boolean handled) {
		public static final Completion CONSUMED = new Completion(true, true);

		public static Completion kept(boolean handled) {
			return new Completion(false, handled);
		}
	}
}
