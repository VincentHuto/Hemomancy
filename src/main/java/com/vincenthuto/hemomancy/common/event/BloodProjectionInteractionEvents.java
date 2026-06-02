package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.tile.FillerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class BloodProjectionInteractionEvents {
	private BloodProjectionInteractionEvents() {
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!isProjectionUse(event.getEntity(), event.getItemStack())
				|| !isBloodVolumeTileTarget(event.getLevel(), event.getPos())) {
			return;
		}
		event.setUseBlock(TriState.FALSE);
		event.setUseItem(TriState.TRUE);
	}

	private static boolean isProjectionUse(Player player, ItemStack stack) {
		if (stack.getItem() instanceof BloodProjectionItem) {
			return true;
		}
		return stack.getItem() instanceof LivingStaffItem
				&& !player.isShiftKeyDown()
				&& LivingStaffItem.isSelectedStaffUtility(player, ManipulationEquipHelper.BLOOD_PROJECTION);
	}

	private static boolean isBloodVolumeTileTarget(Level level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof FillerBlockEntity filler) {
			BlockPos mainPos = filler.getMainBlockPos();
			be = mainPos == null ? null : level.getBlockEntity(mainPos);
		}
		return be != null && HemoCapabilityAccess.getBloodVolume(be).isPresent();
	}
}
