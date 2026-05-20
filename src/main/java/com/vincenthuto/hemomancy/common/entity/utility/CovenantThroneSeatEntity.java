package com.vincenthuto.hemomancy.common.entity.utility;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.CovenantThroneBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.tile.functional.CovenantThroneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class CovenantThroneSeatEntity extends Entity {
	private static final String TAG_THRONE_X = "ThroneX";
	private static final String TAG_THRONE_Y = "ThroneY";
	private static final String TAG_THRONE_Z = "ThroneZ";

	@Nullable
	private BlockPos thronePos;

	public CovenantThroneSeatEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.noPhysics = true;
		this.setInvisible(true);
	}

	public CovenantThroneSeatEntity(Level level, BlockPos thronePos, double x, double y, double z) {
		this(EntityInit.covenant_throne_seat.get(), level);
		this.thronePos = thronePos.immutable();
		this.setPos(x, y, z);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Nullable
	public BlockPos getThronePos() {
		return thronePos;
	}

	public boolean isForThrone(BlockPos pos) {
		return thronePos != null && thronePos.equals(pos);
	}

	@Override
	public void tick() {
		super.tick();
		this.noPhysics = true;
		this.setDeltaMovement(0.0D, 0.0D, 0.0D);

		if (level().isClientSide) {
			return;
		}

		if (thronePos == null || !isValidThrone()) {
			clearThroneState(null);
			discard();
			return;
		}

		if (getPassengers().isEmpty()) {
			clearThroneState(null);
			discard();
			return;
		}

		Entity rider = getFirstPassenger();
		if (rider instanceof Player player) {
			clearOtherPassengers(player.getUUID());
		}
	}

	private boolean isValidThrone() {
		BlockState state = level().getBlockState(thronePos);
		return state.is(BlockInit.covenant_throne.get());
	}

	private void clearOtherPassengers(UUID keptPlayer) {
		for (Entity passenger : getPassengers()) {
			if (!(passenger instanceof Player) || !passenger.getUUID().equals(keptPlayer)) {
				passenger.stopRiding();
			}
		}
	}

	private void clearThroneState(@Nullable UUID playerId) {
		if (thronePos == null) return;
		if (level().getBlockEntity(thronePos) instanceof CovenantThroneBlockEntity throne) {
			throne.clearSeatedPlayer(playerId);
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.contains(TAG_THRONE_X)) {
			thronePos = new BlockPos(tag.getInt(TAG_THRONE_X), tag.getInt(TAG_THRONE_Y), tag.getInt(TAG_THRONE_Z));
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (thronePos != null) {
			tag.putInt(TAG_THRONE_X, thronePos.getX());
			tag.putInt(TAG_THRONE_Y, thronePos.getY());
			tag.putInt(TAG_THRONE_Z, thronePos.getZ());
		}
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}
}
