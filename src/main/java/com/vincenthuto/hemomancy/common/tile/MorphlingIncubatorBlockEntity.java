package com.vincenthuto.hemomancy.common.tile;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.common.block.entity.SimpleInventoryBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MorphlingIncubatorBlockEntity extends SimpleInventoryBlockEntity implements IBloodTile {

	IBloodVolume volume = getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);
	static final String TAG_BLOOD_LEVEL = "bloodLevel";
	int spinningProgress;
	int spinningTotalTime;
	int canSpin;
	int timeToCraft = 200;

	public final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			switch (index) {
			case 0:
				return spinningProgress;
			case 1:
				return spinningTotalTime;
			case 2:
				return canSpin;
			default:
				return 0;
			}
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public void set(int index, int val) {
			switch (index) {
			case 0:
				spinningProgress = val;
				break;
			case 1:
				spinningTotalTime = val;
				break;
			case 2:
				canSpin = val;
			}
		}
	};

	public MorphlingIncubatorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.morphling_incubator.get(), pos, state, 5);
	}

	public static void clientTick(Level level, BlockPos worldPosition, BlockState state,
			MorphlingIncubatorBlockEntity self) {
	}

	@Override
	public void onLoad() {
		volume.setActive(true);
		volume.setMaxBloodVolume(2000f);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState p_155016_, MorphlingIncubatorBlockEntity te) {
		// System.out.println(te.spinningProgress);
		if (te.spinningProgress > 0) {
			te.spinningProgress--;
			// System.out.println(te.spinningProgress);
			te.sendUpdates();
			setChanged(level, pos, p_155016_);
			HLParticleUtils.spawnPoof((ServerLevel) level, pos, ParticleTypes.MYCELIUM);

			if (te.spinningProgress == 1) {
				System.out.println("outputResults");
				HLParticleUtils.spawnPoof((ServerLevel) level, pos, ParticleTypes.REVERSE_PORTAL);

				te.outputResults();
			}
//			if (!te.inventory.isEmpty()) {
//				if (!((te.checkBalancedSpots(2, 6) && te.checkBalancedSpots(3, 7) && te.checkBalancedSpots(4, 8)
//						&& te.checkBalancedSpots(9, 5)))) {
//					if (te.dataAccess.get(0) > 0) {
//						te.dataAccess.set(0, 0);
//					}
//				}
//			}
		}
	}

	public List<ItemStack> getCatalystStacks() {
		return inventory.subList(1, 4);
	}

	public ItemStack getCenterStack() {
		return inventory.get(0);
	}

	private void outputResults() {
		System.out.println("Done");
		System.out.println(spinningProgress);
		if (spinningProgress <= 1) {
			System.out.println("");
			for (int i = 1; i < 5; i++) {
				System.out.println(i);
				inventory.set(i, ItemStack.EMPTY);
			}
	         double d0 = (double)Mth.randomBetween(level.random, -0.2F, 0.2F);
	         double d1 = (double)Mth.randomBetween(level.random, -0.2F, 0.2F);
	         double d2 = (double)Mth.randomBetween(level.random, -0.2F, 0.2F);
			inventory.set(0, ItemStack.EMPTY);
			getLevel().addFreshEntity(new ItemEntity(
					level, worldPosition.getX(),
					worldPosition.getY()+1,
					worldPosition.getZ(),
					new ItemStack(ItemInit.noctifly_agaric.get()),
					d0,d1, d2));
		}
	}

	public boolean attemptStartup() {
		if (dataAccess.get(0) <= 0) {
			dataAccess.set(0, 200);
			return true;

		}
		return false;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		this.spinningProgress = tag.getInt("SpinTime");
		this.spinningTotalTime = tag.getInt("SpinTimeTotal");
	}

	@Override
	public void load(CompoundTag pTag) {
		super.load(pTag);
		this.spinningProgress = pTag.getInt("SpinTime");
		this.spinningTotalTime = pTag.getInt("SpinTimeTotal");
		if (pTag != null) {
			volume.setBloodVolume(pTag.getFloat(TAG_BLOOD_LEVEL));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag pTag) {
		super.saveAdditional(pTag);
		pTag.putInt("SpinTime", this.spinningProgress);
		pTag.putInt("SpinTimeTotal", this.spinningTotalTime);
		if (pTag != null) {
			pTag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		}
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		tag.putInt("SpinTime", this.spinningProgress);
		tag.putInt("SpinTimeTotal", this.spinningTotalTime);
		tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		return tag;
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		if (pkt.getTag() != null) {
			CompoundTag tag = pkt.getTag();
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		}

	}

	public IBloodVolume getBloodCapability() {
		return volume;
	}

	public double getBloodVolume() {
		return volume.getBloodVolume();
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.hemomancy.morphlingincubator");
	}

}
