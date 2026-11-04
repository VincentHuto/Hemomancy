package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.WoodSet;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

/** The vanilla sign block entities accept the wood sets' signs, as they do vanilla's. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class WoodSetEvents {
	private WoodSetEvents() {
	}

	@SubscribeEvent
	public static void addSignsToBlockEntityTypes(BlockEntityTypeAddBlocksEvent event) {
		for (WoodSet set : WoodSet.all()) {
			event.modify(BlockEntityType.SIGN, set.sign().get(), set.wallSign().get());
			event.modify(BlockEntityType.HANGING_SIGN, set.hangingSign().get(), set.wallHangingSign().get());
		}
	}
}
