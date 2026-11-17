package com.vincenthuto.hemomancy.common.tile.harbinger.ground;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.world.ground.GougeCatalysts;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * The catalyst feeder's one stack. Once a second {@link GroundNetwork#feed} looks at the runs beside it and, when one is
 * about to go dark, dissolves one item into it. Hoppers fill it through the block's item handler.
 */
public class CatalystFeederBlockEntity extends HemoBlockEntity {
	private static final String TAG_STOCK = "Stock";
	private final ItemStackHandler stock = new ItemStackHandler(1) {
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return accepts(stack);
		}

		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	};

	public CatalystFeederBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.catalyst_feeder.get(), pos, state);
	}

	public ItemStackHandler stock() {
		return stock;
	}

	/** Only gouge catalysts go in. */
	public boolean accepts(ItemStack stack) {
		return GougeCatalysts.of(stack) != null;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, CatalystFeederBlockEntity feeder) {
		if (level instanceof ServerLevel server && Math.floorMod(server.getGameTime() + pos.asLong(), 20L) == 0L) {
			GroundNetwork.feed(server, feeder);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.put(TAG_STOCK, stock.serializeNBT(registries));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		if (tag.contains(TAG_STOCK)) stock.deserializeNBT(registries, tag.getCompound(TAG_STOCK));
	}
}
