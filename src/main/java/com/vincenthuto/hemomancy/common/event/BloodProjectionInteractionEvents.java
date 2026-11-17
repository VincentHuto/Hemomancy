package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodEndpoint;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.EarthenVeinBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel;
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

	/**
	 * A blood tool used on a blood block, or on anything the selected Sculpting of its verb claims (a furnace under Furnace
	 * Veins, a worn anvil under Sanguine Mending), channels the verb instead of opening the block's own screen.
	 */
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!isBloodToolUse(event.getEntity(), event.getItemStack())
				|| !isBloodInteractionBlockTarget(event.getLevel(), event.getPos())
						&& !sculptingClaims(event.getEntity(), event.getItemStack())) {
			return;
		}
		event.setUseBlock(TriState.FALSE);
		event.setUseItem(TriState.TRUE);
	}

	/** Whether the selected Sculpting of the tool's verb claims what the player aims at. Both sides decide it alike. */
	private static boolean sculptingClaims(Player player, ItemStack stack) {
		return VerbChannel.verbOf(player, stack).flatMap(verb -> SculptingDispatcher.currentClaim(player, verb)).isPresent();
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
				|| held.getItem() instanceof LivingStaffItem && VerbChannel.activeVerb(player) == Verb.PROJECT;
		return projecting && (HemoCapabilityAccess.getBloodVolume(player).getBloodLine().isValid()
						&& HemoCapabilityAccess.getBloodVolume(player).getBloodLine().hasNpcMember(target.getUUID()));
	}

	static boolean isBloodToolUse(Player player, ItemStack stack) {
		if (stack.getItem() instanceof BloodAbsorptionItem || stack.getItem() instanceof BloodProjectionItem) {
			return true;
		}
		// The staff always channels the active verb, so it always takes a blood block over the block's own use.
		return stack.getItem() instanceof LivingStaffItem && !player.isShiftKeyDown();
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
