package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.EnzymeItem;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class SomaticLoomInteractionEvents {
	private SomaticLoomInteractionEvents() {
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!event.getEntity().isShiftKeyDown()
				|| !(event.getItemStack().getItem() instanceof EnzymeItem)
				|| !isSomaticLoomTarget(event.getLevel(), event.getPos())) {
			return;
		}
		event.setUseBlock(TriState.TRUE);
	}

	private static boolean isSomaticLoomTarget(Level level, BlockPos pos) {
		if (level.getBlockState(pos).is(BlockInit.somatic_loom.get())) {
			return true;
		}
		if (level.getBlockEntity(pos) instanceof FillerBlockEntity filler) {
			BlockPos mainPos = filler.getMainBlockPos();
			return mainPos != null && level.getBlockState(mainPos).is(BlockInit.somatic_loom.get());
		}
		return false;
	}
}
