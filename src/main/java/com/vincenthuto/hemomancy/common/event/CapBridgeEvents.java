package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.CapBridgeBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.world.FungalGardensPlaces;
import com.vincenthuto.hemomancy.common.world.GardensTerrainRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** An Apotheos's bare hand on a Gardens cap grows it out into a bridge. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CapBridgeEvents {
	private CapBridgeEvents() {}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player)
				|| player.isSpectator()) return;
		Level level = player.level();
		BlockPos cap = event.getPos();
		if (!level.getBlockState(cap).is(CapBridgeBlock.BRIDGING_CAPS)) return;
		if (!GardensTerrainRules.mayBridge(ApotheosBodyEvents.isApotheos(player), FungalGardensPlaces.inDimension(level),
				player.getMainHandItem().isEmpty())) return;
		Direction face = event.getFace() == null ? Direction.UP : event.getFace();
		Direction direction = GardensTerrainRules.bridgeDirection(face, player.getDirection());
		BlockPos start = cap.relative(direction);
		if (!level.getBlockState(start).isAir()) return;
		CapBridgeBlock bridge = (CapBridgeBlock) BlockInit.cap_bridge.get();
		level.setBlock(start, bridge.seed(direction), CapBridgeBlock.UPDATE_ALL);
		level.playSound(null, start, SoundEvents.FUNGUS_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);
		player.swing(InteractionHand.MAIN_HAND, true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
	}
}
