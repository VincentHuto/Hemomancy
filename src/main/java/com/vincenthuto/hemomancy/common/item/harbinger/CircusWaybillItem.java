package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress;
import com.vincenthuto.hemomancy.common.mission.circus.CircusCurriculum;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import com.vincenthuto.hemomancy.common.worldgen.structure.circus.CircusWaybillSearch;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * The Mnemonist's Scarlet Waybill: points to the camp of the next eligible professor who has not taught the player, or
 * (sneaking, or with nothing left to learn) to the nearest unfinished pavilion. Its search runs through
 * {@link CircusWaybillSearch}, off the server thread (tag {@code circus_waybill_targets} or
 * {@code troupe_teacher_<camp>}), and the target is recorded when the answer lands.
 */
public final class CircusWaybillItem extends CovenantWaybillItem {
	private static final String KEY = "item.hemomancy.circus_waybill";

	public CircusWaybillItem(Properties properties) {
		super(properties, Hemomancy.rloc("circus_waybill_targets"), KEY, 160);
	}

	@Override
	protected void attune(ItemStack stack, ServerLevel level, Player player, boolean showFeedback) {
		if (!(player instanceof ServerPlayer serverPlayer)) return;
		if (showFeedback) {
			player.displayClientMessage(Component.translatable(KEY + ".searching")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
		}
		if (!CircusWaybillSearch.begin(player.getUUID())) return;
		TagKey<Structure> tag = targets(player);
		CircusWaybillSearch.search(level, tag, player.blockPosition()).whenComplete((found, error) -> {
			CircusWaybillSearch.finish(player.getUUID());
			if (error != null) {
				Hemomancy.LOGGER.warn("Scarlet Waybill search failed", error);
				return;
			}
			if (serverPlayer.isRemoved() || serverPlayer.level() != level) return;
			ItemStack waybill = heldOrCarried(serverPlayer, stack);
			if (waybill.isEmpty()) return;
			record(waybill, level, serverPlayer, CircusWaybillSearch.pick(level, tag, found), showFeedback);
		});
	}

	/** Never called: {@link #attune} replaces the synchronous search. */
	@Override
	protected BlockPos findTarget(ServerLevel level, Player player) {
		return null;
	}

	/** The next eligible untaught professor's camp, unless sneaking; otherwise the pavilions. */
	private static TagKey<Structure> targets(Player player) {
		if (player.isShiftKeyDown()) return CircusWaybillSearch.PAVILIONS;
		var lesson = CircusCurriculum.lessons().stream()
				.filter(l -> ProgressionGates.hasDegree(player, l.degree())
						&& !CircusApprenticeshipProgress.instructionSatisfied(player, l.summon()))
				.findFirst().orElse(null);
		if (lesson == null) return CircusWaybillSearch.PAVILIONS;
		return TagKey.create(Registries.STRUCTURE, Hemomancy.rloc("troupe_teacher_" + lesson.camp()));
	}
}
