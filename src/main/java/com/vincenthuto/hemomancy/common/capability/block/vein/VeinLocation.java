package com.vincenthuto.hemomancy.common.capability.block.vein;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.vincenthuto.hutoslib.math.DimensionalPosition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class VeinLocation extends DimensionalPosition {

	public static final VeinLocation BLANK = new VeinLocation("blankvein", ResourceLocation.withDefaultNamespace("blankvein"),
			new BlockPos(0, 0, 0));
	static List<String> prefixes = Arrays.asList("Superior", "Inferior", "Major", "Minor", "Cranial", "Caudal",
			"Anterior", "Ventral", "Posterior", "Dorsal", "Proximal", "Lateral", "Medial", "Distal", "Pulmonary",
			"Systemic");
	static List<String> locNames = Arrays.asList("Vena", "Femoral", "Cerebral", "Cephalic", "Frontal", "Oral",
			"Orbital", "Costal", "Pectoral", "Scapular", "Sternal", "Vertebral", "Sacral", "Brachial", "Carpal",
			"Digital", "Azygos");
	static List<String> suffixes = Arrays.asList("Cava", "Vein", "Artery", "Arteriole", "Capillary");

	public static VeinLocation deserializeFromBuf(FriendlyByteBuf buf) {
		VeinLocation s = new VeinLocation(buf.readUtf(), buf.readResourceLocation(), buf.readBlockPos());
		return s;
	}

	public void serializeToBuf(FriendlyByteBuf buf) {
		buf.writeUtf(getName());
		buf.writeResourceLocation(getDimension());
		buf.writeBlockPos(getPosition());
	}

	public static VeinLocation deserializeToLoc(CompoundTag nbt) {
		String name;
		BlockPos bPos;
		ResourceLocation dim;
		UUID uuid;
		if (nbt.contains("dim") && nbt.contains("pos") && nbt.contains("name") && nbt.contains("id")) {
			name = nbt.getString("name");
			uuid = nbt.getUUID("id");
			dim = ResourceLocation.parse(nbt.getString("dim"));
			bPos = NbtUtils.readBlockPos(nbt, "pos").orElse(BlockPos.ZERO);
			VeinLocation loc = new VeinLocation(uuid, name, dim, bPos);
			return loc;
		}

		return VeinLocation.BLANK;
	}

	public static String getRandomName() {
		Random rand = new Random();
		String prefix = prefixes.get(rand.nextInt(prefixes.size()));
		String locationName = locNames.get(rand.nextInt(locNames.size()));
		String suffix = suffixes.get(rand.nextInt(suffixes.size()));
		return prefix + " " + locationName + " " + suffix;
	}

	private String name;

	private UUID uuid;

	/*
	 * to get RL Player().level.dimension().location();
	 */
	public VeinLocation(String name, ResourceLocation dim, BlockPos pos) {
		super(dim, pos);
		this.name = name;
		this.uuid = UUID.randomUUID();
	}

	/*
	 * to get RL Player().level.dimension().location();
	 */
	public VeinLocation(UUID uuid, String name, ResourceLocation dim, BlockPos pos) {
		super(dim, pos);
		this.name = name;
		this.uuid = uuid;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		super.deserializeNBT(provider, nbt);
		if (nbt.contains("name") && nbt.contains("id")) {
			String name = nbt.getString("name");
			UUID uuid = nbt.getUUID("id");
			this.name = name;
			this.uuid = uuid;
		}
	}

	public void deserializeNBT(CompoundTag nbt) {
		if (nbt.contains("name") && nbt.contains("id")) {
			this.name = nbt.getString("name");
			this.uuid = nbt.getUUID("id");
		}
	}

	public String getName() {
		return name;
	}

	public UUID getUUID() {
		return uuid;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = super.serializeNBT(provider);
		nbt.putString("name", this.name);
		nbt.putUUID("id", this.uuid);
		return nbt;
	}

	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("dim", getDimension().toString());
		nbt.put("pos", NbtUtils.writeBlockPos(getPosition()));
		nbt.putString("name", this.name);
		nbt.putUUID("id", this.uuid);
		return nbt;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRandomName() {
		this.name = getRandomName();
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return ("Vein: '" + getName() + "UUID: " + getUUID() + "' Located in: " + getDimension().getPath()
				+ " at Position: " + getPosition());
	}
}
