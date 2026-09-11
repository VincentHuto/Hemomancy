package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodEndpoint;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.EarthenVeinBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
		if (!isBloodToolUse(event.getEntity(), event.getItemStack())
				|| !isBloodInteractionBlockTarget(event.getLevel(), event.getPos())) {
			return;
		}
		event.setUseBlock(TriState.FALSE);
		event.setUseItem(TriState.TRUE);
	}

	@SubscribeEvent
	public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
		if (prefersProjectionOverConversation(event.getEntity(), event.getTarget())) {
			event.setCancellationResult(InteractionResult.PASS);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onRightClickEntityAt(PlayerInteractEvent.EntityInteractSpecific event) {
		if (prefersProjectionOverConversation(event.getEntity(), event.getTarget())) {
			event.setCancellationResult(InteractionResult.PASS);
			event.setCanceled(true);
		}
	}

	/** Let the normal item-use path start Projection through a recruited ally. Crouching requests conversation. */
	public static boolean prefersProjectionOverConversation(Player player, Entity target) {
		if (player.isShiftKeyDown()) return false;
		ItemStack held = player.getMainHandItem();
		boolean projecting = held.getItem() instanceof BloodProjectionItem
				|| held.getItem() instanceof LivingStaffItem
						&& LivingStaffItem.isSelectedStaffUtility(player, ManipulationEquipHelper.BLOOD_PROJECTION);
		return projecting && HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.getBloodLine().isValid()
						&& volume.getBloodLine().hasNpcMember(target.getUUID())).orElse(false);
	}

	private static boolean isBloodToolUse(Player player, ItemStack stack) {
		if (stack.getItem() instanceof BloodAbsorptionItem || stack.getItem() instanceof BloodProjectionItem) {
			return true;
		}
		return stack.getItem() instanceof LivingStaffItem
				&& !player.isShiftKeyDown()
				&& (LivingStaffItem.isSelectedStaffUtility(player, ManipulationEquipHelper.BLOOD_ABSORPTION)
						|| LivingStaffItem.isSelectedStaffUtility(player, ManipulationEquipHelper.BLOOD_PROJECTION));
	}

	private static boolean isBloodInteractionBlockTarget(Level level, BlockPos pos) {
		Block block = level.getBlockState(pos).getBlock();
		if (isDirectBloodToolTarget(block.getClass()) || block instanceof BlockBloodEndpoint) {
			return true;
		}
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof FillerBlockEntity filler) {
			BlockPos mainPos = filler.getMainBlockPos();
			be = mainPos == null ? null : level.getBlockEntity(mainPos);
		}
		return be != null && HemoCapabilityAccess.getBloodVolume(be).isPresent();
	}

	static boolean isDirectBloodToolTarget(Class<? extends Block> blockType) {
		return EarthenVeinBlock.class.isAssignableFrom(blockType);
	}
}
