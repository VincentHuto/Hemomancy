package com.vincenthuto.hemomancy.tile;

import com.vincenthuto.hemomancy.capa.block.vein.EarthenVeinLocProvider;
import com.vincenthuto.hemomancy.capa.block.vein.IEarthenVeinLoc;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.util.VeinLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityEarthenVein extends BlockEntity {
	VeinLocation clientloc = VeinLocation.BLANK;
	IEarthenVeinLoc locCap = getCapability(EarthenVeinLocProvider.VEIN_CAPA).orElseThrow(IllegalStateException::new);
	static final String TAG_VEIN_LOC = "veinlocation";
	static final String TAG_NAME = "name";
	String name = "";
	boolean hasTicked = false;

	public BlockEntityEarthenVein(BlockPos pos, BlockState state) {
		super(BlockEntityInit.earthen_vein.get(), pos, state);
	}

	public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
		if (level.getBlockEntity(pos)instanceof BlockEntityEarthenVein te) {
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

		}

	}

	// NBT JUNK
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		readPacketNBT(tag);
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		super.save(compound);
		compound.put(TAG_VEIN_LOC, locCap.getVeinLocation().serializeNBT());
		compound.putString(TAG_NAME, getName());
		return compound;
	}

	public void readPacketNBT(CompoundTag tag) {
		locCap.setVeinLoc(VeinLocation.deserializeToLoc(tag.getCompound(TAG_VEIN_LOC)));
		clientloc = VeinLocation.deserializeToLoc(tag.getCompound(TAG_VEIN_LOC));
		name = tag.getString(TAG_NAME);
	}

	public void writePacketNBT(CompoundTag par1CompoundTag) {

	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		CompoundTag tag = pkt.getTag();
		locCap.setVeinLoc(VeinLocation.deserializeToLoc(tag.getCompound(TAG_VEIN_LOC)));
		clientloc = VeinLocation.deserializeToLoc(tag.getCompound(TAG_VEIN_LOC));
		name = tag.getString(TAG_NAME);

	}

	@Override
	public CompoundTag getUpdateTag() {
		return save(new CompoundTag());
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		super.getUpdatePacket();
		CompoundTag tag = new CompoundTag();
		writePacketNBT(tag);
		tag.put(TAG_VEIN_LOC, locCap.getVeinLocation().serializeNBT());
		tag.putString(TAG_NAME, getName());
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		locCap.setVeinLoc(VeinLocation.deserializeToLoc(tag.getCompound(TAG_VEIN_LOC)));
		clientloc = VeinLocation.deserializeToLoc(tag.getCompound(TAG_VEIN_LOC));
		name = tag.getString(TAG_NAME);

	}

	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	public boolean isHasTicked() {
		return hasTicked;
	}

	public void setHasTicked(boolean hasTicked) {
		this.hasTicked = hasTicked;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLoc(VeinLocation locCap) {
		this.locCap.setVeinLoc(locCap);
	}

	public VeinLocation getLoc() {
		return locCap.getVeinLocation();
	}

}
