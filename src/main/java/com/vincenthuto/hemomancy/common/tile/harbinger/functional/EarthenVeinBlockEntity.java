package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.capability.block.vein.EarthenVeinLoc;
import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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

import java.util.UUID;

public class EarthenVeinBlockEntity extends BlockEntity {

	public final AnimationState idleAnimationState = new AnimationState();

	static final String TAG_VEIN_LOC = "veinlocation";
	static final String TAG_NAME = "name";
	static final String TAG_TEMPORARY_OWNER = "temporaryOwner";
	static final String TAG_TEMPORARY_EXPIRY = "temporaryExpiry";
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
			if (!level.isClientSide && te.isTemporary() && level.getGameTime() >= te.temporaryExpiry) {
				level.removeBlock(pos, false);
				return;
			}
			if (!te.hasTicked) {
				if (!level.isClientSide) {
					if (te.getName() == "") {
						te.setName(VeinLocation.getRandomName());
					}
					te.setLoc(new VeinLocation(te.getName(), te.getLevel().dimension().location(), te.getBlockPos()));
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

	private final EarthenVeinLoc locCap = new EarthenVeinLoc();
	String name = "";

	boolean hasTicked = false;
	private UUID temporaryOwner;
	private long temporaryExpiry;

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
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag compound = super.getUpdateTag(provider);
		compound.put(TAG_VEIN_LOC, locCap.getVeinLocation().serializeNBT(provider));
		compound.putString(TAG_NAME, getName());
		if (temporaryOwner != null) compound.putUUID(TAG_TEMPORARY_OWNER, temporaryOwner);
		compound.putLong(TAG_TEMPORARY_EXPIRY, temporaryExpiry);
		return compound;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
		super.handleUpdateTag(tag, provider);
		VeinLocation veinLocation = VeinLocation.deserializeToLoc(tag.getCompound(TAG_VEIN_LOC));
		veinLocation.deserializeNBT(provider, tag.getCompound(TAG_VEIN_LOC));
		locCap.setVeinLoc(veinLocation);
		name = tag.getString(TAG_NAME);
		loadTemporaryData(tag);

	}

	public boolean isHasTicked() {
		return hasTicked;
	}

	// NBT JUNK
	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		VeinLocation veinLocation = VeinLocation.deserializeToLoc(tag.getCompound(TAG_VEIN_LOC));
		veinLocation.deserializeNBT(provider, tag.getCompound(TAG_VEIN_LOC));
		locCap.setVeinLoc(veinLocation);
		name = tag.getString(TAG_NAME);
		loadTemporaryData(tag);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
		super.onDataPacket(net, pkt, provider);
		CompoundTag tag = pkt.getTag();
		VeinLocation veinLocation = VeinLocation.deserializeToLoc(tag.getCompound(TAG_VEIN_LOC));
		veinLocation.deserializeNBT(provider, tag.getCompound(TAG_VEIN_LOC));
		locCap.setVeinLoc(veinLocation);
		name = tag.getString(TAG_NAME);
		loadTemporaryData(tag);

	}

	@Override
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
		super.saveAdditional(compound, provider);
		compound.put(TAG_VEIN_LOC, locCap.getVeinLocation().serializeNBT(provider));
		compound.putString(TAG_NAME, getName());
		if (temporaryOwner != null) compound.putUUID(TAG_TEMPORARY_OWNER, temporaryOwner);
		compound.putLong(TAG_TEMPORARY_EXPIRY, temporaryExpiry);
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

	public void makeTemporary(UUID owner, long expiry) {
		temporaryOwner = owner;
		temporaryExpiry = expiry;
		name = "Temporary Earthen Vein";
		setLoc(new VeinLocation(name, level.dimension().location(), worldPosition));
		hasTicked = true;
		sendUpdates();
	}

	public boolean isTemporary() {
		return temporaryOwner != null;
	}

	public boolean isTemporaryOwnedBy(UUID owner) {
		return owner != null && owner.equals(temporaryOwner);
	}

	private void loadTemporaryData(CompoundTag tag) {
		temporaryOwner = tag.hasUUID(TAG_TEMPORARY_OWNER) ? tag.getUUID(TAG_TEMPORARY_OWNER) : null;
		temporaryExpiry = tag.getLong(TAG_TEMPORARY_EXPIRY);
	}

}
