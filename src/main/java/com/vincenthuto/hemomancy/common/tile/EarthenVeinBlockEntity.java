package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.block.vein.EarthenVeinLocProvider;
import com.vincenthuto.hemomancy.common.capability.block.vein.IEarthenVeinLoc;
import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EarthenVeinBlockEntity extends BlockEntity {

	public final AnimationState idleAnimationState = new AnimationState();

	static final String TAG_VEIN_LOC = "veinlocation";
	static final String TAG_NAME = "name";
	public int time;
	public float flip;
	public float oFlip;
	public float flipT;
	public float flipA;
	public float open;
	public float oOpen;
	public float rot;
	public float oRot;
	public float tRot;
	private static final RandomSource RANDOM = RandomSource.create();

	public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {

		if (level.getBlockEntity(pos) instanceof EarthenVeinBlockEntity te) {
			if (!te.hasTicked) {
				if (!level.isClientSide) {
					if (te.getName() == "") {
						te.setName(VeinLocation.getRandomName());
					}
					te.getCapability(EarthenVeinLocProvider.VEIN_CAPA).ifPresent((cap) -> {
						cap.setVeinLoc(
								new VeinLocation(te.getName(), te.getLevel().dimension().location(), te.getBlockPos()));
					});
				}
				te.setHasTicked(true);
				te.sendUpdates();
			}
			if (level.isClientSide) {
				te.oOpen = te.open;
				te.oRot = te.rot;
				Player player = level.getNearestPlayer((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
						(double) pos.getZ() + 0.5D, 3.0D, false);
				if (player != null) {
					double d0 = player.getX() - ((double) pos.getX() + 0.5D);
					double d1 = player.getZ() - ((double) pos.getZ() + 0.5D);
					te.tRot = (float) Mth.atan2(d1, d0);
					te.open += 0.1F;
					if (te.open < 0.5F || RANDOM.nextInt(40) == 0) {
						float f1 = te.flipT;

						do {
							te.flipT += (float) (RANDOM.nextInt(4) - RANDOM.nextInt(4));
						} while (f1 == te.flipT);
					}
				} else {
				//	te.tRot += 0.02F;
					te.open -= 0.1F;
				}

				while (te.rot >= (float) Math.PI) {
					te.rot -= ((float) Math.PI * 2F);
				}

				while (te.rot < -(float) Math.PI) {
					te.rot += ((float) Math.PI * 2F);
				}

				while (te.tRot >= (float) Math.PI) {
					te.tRot -= ((float) Math.PI * 2F);
				}

				while (te.tRot < -(float) Math.PI) {
					te.tRot += ((float) Math.PI * 2F);
				}

				float f2;
				for (f2 = te.tRot - te.rot; f2 >= (float) Math.PI; f2 -= ((float) Math.PI * 2F)) {
				}

				while (f2 < -(float) Math.PI) {
					f2 += ((float) Math.PI * 2F);
				}

				te.rot += f2 * 0.4F;
				te.open = Mth.clamp(te.open, 0.0F, 1.0F);
				++te.time;
				te.oFlip = te.flip;
				float f = (te.flipT - te.flip) * 0.4F;
				float f3 = 0.2F;
				f = Mth.clamp(f, -0.2F, 0.2F);
				te.flipA += (f - te.flipA) * 0.9F;
				te.flip += te.flipA;
			}

		}

	}

	IEarthenVeinLoc locCap = getCapability(EarthenVeinLocProvider.VEIN_CAPA).orElseThrow(IllegalStateException::new);
	String name = "";

	boolean hasTicked = false;

	public EarthenVeinBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.earthen_vein.get(), pos, state);
	}

	public VeinLocation getLoc() {
		return locCap.getVeinLocation();
	}

	public String getName() {
		return name;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag compound = super.getUpdateTag();
		compound.put(TAG_VEIN_LOC, locCap.getVeinLocation().serializeNBT());
		compound.putString(TAG_NAME, getName());
		return compound;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		locCap.setVeinLoc(VeinLocation.deserializeToLoc(tag.getCompound(TAG_VEIN_LOC)));
		name = tag.getString(TAG_NAME);

	}

	public boolean isHasTicked() {
		return hasTicked;
	}

	// NBT JUNK
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		locCap.setVeinLoc(VeinLocation.deserializeToLoc(tag.getCompound(TAG_VEIN_LOC)));
		name = tag.getString(TAG_NAME);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		CompoundTag tag = pkt.getTag();
		locCap.setVeinLoc(VeinLocation.deserializeToLoc(tag.getCompound(TAG_VEIN_LOC)));
		name = tag.getString(TAG_NAME);

	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		compound.put(TAG_VEIN_LOC, locCap.getVeinLocation().serializeNBT());
		compound.putString(TAG_NAME, getName());
	}

	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	public void setHasTicked(boolean hasTicked) {
		this.hasTicked = hasTicked;
	}

	public void setLoc(VeinLocation locCap) {
		this.locCap.setVeinLoc(locCap);
	}

	public void setName(String name) {
		this.name = name;
	}

}
