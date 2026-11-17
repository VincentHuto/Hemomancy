package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.block.harbinger.ground.LivingVeinBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Water flows over a living vein (CQ-44, NQ-02-4). A fluid tick on flowing water sets the cell to a water block, or to
 * air once the water is gone; in a vein's cell that would break the vein. There the tick settles the same new water,
 * then the vein takes it as its own water state ({@link LivingVeinBlock#holdWater}) and the water spreads on as usual.
 */
@Mixin(FlowingFluid.class)
public abstract class MixinLivingVeinWater {
	@Shadow(remap = false)
	protected abstract FluidState getNewLiquid(Level level, BlockPos pos, BlockState blockState);

	@Shadow(remap = false)
	protected abstract int getSpreadDelay(Level level, BlockPos pos, FluidState currentState, FluidState newState);

	@Shadow(remap = false)
	protected abstract void spread(Level level, BlockPos pos, FluidState state);

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
	private void hemomancy$flowOverLivingVein(Level level, BlockPos pos, FluidState state, CallbackInfo ci) {
		if (state.isSource()) return;
		BlockState vein = level.getBlockState(pos);
		if (!(vein.getBlock() instanceof LivingVeinBlock)) return;
		FluidState next = getNewLiquid(level, pos, vein);
		int delay = getSpreadDelay(level, pos, state, next);
		if (!next.equals(state)) {
			LivingVeinBlock.holdWater(level, pos, vein, next);
			if (!next.isEmpty()) level.scheduleTick(pos, next.getType(), delay);
		}
		spread(level, pos, next);
		ci.cancel();
	}
}
