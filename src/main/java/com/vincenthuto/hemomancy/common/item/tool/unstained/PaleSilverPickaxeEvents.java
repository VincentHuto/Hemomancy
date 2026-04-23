package com.vincenthuto.hemomancy.common.item.tool.unstained;

import net.neoforged.fml.common.EventBusSubscriber;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Server-side event handler for the {@link PaleSilverPickaxeItem}.
 * <p>
 * While a player holds the pickaxe in their mainhand, mob spawns within a
 * 4-block radius are suppressed â€” the resonant purity of pale silver keeps
 * the blood at bay.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PaleSilverPickaxeEvents {

	/** Radius (in blocks) around the pickaxe holder within which spawns are denied. */
	private static final double SUPPRESS_RADIUS = 4.0;

	@SubscribeEvent
	public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
		net.minecraft.world.level.ServerLevelAccessor levelAccessor = event.getLevel();
		net.minecraft.server.level.ServerLevel sLevel = levelAccessor.getLevel();

		BlockPos spawnPos = BlockPos.containing(event.getX(), event.getY(), event.getZ());

		for (Player player : sLevel.players()) {
			ItemStack mainhand = player.getItemBySlot(EquipmentSlot.MAINHAND);
			if (!(mainhand.getItem() instanceof PaleSilverPickaxeItem)) continue;

			double distSq = player.blockPosition().distSqr(spawnPos);
			if (distSq <= SUPPRESS_RADIUS * SUPPRESS_RADIUS) {
				event.setResult(Event.Result.DENY);
				return;
			}
		}
	}
}
