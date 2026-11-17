package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Living blood (core-loop Track 3): a blood machine remembers who placed it, for {@code BloodDrawRights}. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LivingBloodEvents {
	private LivingBloodEvents() {
	}

	@SubscribeEvent
	public static void onPlace(BlockEvent.EntityPlaceEvent event) {
		if (!(event.getEntity() instanceof Player player) || !(event.getLevel() instanceof Level level)
				|| level.isClientSide) return;
		BlockEntity blockEntity = level.getBlockEntity(event.getPos());
		if (!(blockEntity instanceof IBloodReservoir reservoir) || !reservoir.holdsBlood()) return;
		ReservoirBlood.settled(blockEntity).setOwner(player.getUUID());
		blockEntity.setChanged();
	}
}
