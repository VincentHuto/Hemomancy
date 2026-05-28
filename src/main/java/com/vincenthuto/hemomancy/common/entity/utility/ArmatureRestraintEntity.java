package com.vincenthuto.hemomancy.common.entity.utility;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.HematicArmatureBlock;
import com.vincenthuto.hemomancy.common.tile.crafting.HematicArmatureBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class ArmatureRestraintEntity extends Entity {
	private static final String TAG_ARMATURE_X = "ArmatureX";
	private static final String TAG_ARMATURE_Y = "ArmatureY";
	private static final String TAG_ARMATURE_Z = "ArmatureZ";

	@Nullable
	private BlockPos armaturePos;

	public ArmatureRestraintEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.noPhysics = true;
		this.setInvisible(true);
	}

	public ArmatureRestraintEntity(Level level, BlockPos armaturePos, double x, double y, double z) {
		this(EntityInit.hematic_armature_restraint.get(), level);
		this.armaturePos = armaturePos.immutable();
		this.setPos(x, y, z);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Nullable
	public BlockPos getArmaturePos() {
		return armaturePos;
	}

	public boolean isForArmature(BlockPos pos) {
		return armaturePos != null && armaturePos.equals(pos);
	}

	@Override
	protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
		if (hasPassenger(passenger)) {
			callback.accept(passenger, getX(), getY(), getZ());
		}
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		if (armaturePos == null) {
			return super.getDismountLocationForPassenger(passenger);
		}
		BlockState state = level().getBlockState(armaturePos);
		Direction facing = state.hasProperty(HematicArmatureBlock.FACING)
				? state.getValue(HematicArmatureBlock.FACING)
				: Direction.SOUTH;
		return dismountLocation(armaturePos, facing);
	}

	public static Vec3 dismountLocation(BlockPos armaturePos, Direction facing) {
		Direction exit = facing.getOpposite();
		return new Vec3(armaturePos.getX() + 0.5D + exit.getStepX() * 1.15D,
				armaturePos.getY() + 0.1D,
				armaturePos.getZ() + 0.5D + exit.getStepZ() * 1.15D);
	}

	@Override
	public boolean shouldRiderSit() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		this.noPhysics = true;
		this.setDeltaMovement(0.0D, 0.0D, 0.0D);

		if (level().isClientSide) {
			return;
		}

		if (armaturePos == null || !isValidArmature()) {
			clearArmatureState(null);
			discard();
			return;
		}

		if (getPassengers().isEmpty()) {
			clearArmatureState(null);
			discard();
			return;
		}

		Entity rider = getFirstPassenger();
		if (rider instanceof Player player) {
			if (player.isShiftKeyDown()) {
				clearArmatureState(player.getUUID());
				Vec3 dismount = getDismountLocationForPassenger(player);
				player.stopRiding();
				player.teleportTo(dismount.x, dismount.y, dismount.z);
				discard();
				return;
			}
			player.setPose(Pose.STANDING);
			clearOtherPassengers(player.getUUID());
		}
	}

	private boolean isValidArmature() {
		BlockState state = level().getBlockState(armaturePos);
		return state.is(BlockInit.hematic_armature.get());
	}

	private void clearOtherPassengers(UUID keptPlayer) {
		for (Entity passenger : getPassengers()) {
			if (!(passenger instanceof Player) || !passenger.getUUID().equals(keptPlayer)) {
				passenger.stopRiding();
			}
		}
	}

	private void clearArmatureState(@Nullable UUID playerId) {
		if (armaturePos == null) return;
		if (level().getBlockEntity(armaturePos) instanceof HematicArmatureBlockEntity armature) {
			armature.clearRestrainedPlayer(playerId);
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.contains(TAG_ARMATURE_X)) {
			armaturePos = new BlockPos(tag.getInt(TAG_ARMATURE_X), tag.getInt(TAG_ARMATURE_Y),
					tag.getInt(TAG_ARMATURE_Z));
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (armaturePos != null) {
			tag.putInt(TAG_ARMATURE_X, armaturePos.getX());
			tag.putInt(TAG_ARMATURE_Y, armaturePos.getY());
			tag.putInt(TAG_ARMATURE_Z, armaturePos.getZ());
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
