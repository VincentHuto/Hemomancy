package com.vincenthuto.hemomancy.capa.block.vein;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.vincenthuto.hutoslib.math.DimensionalPosition;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;

public class VeinLocation extends DimensionalPosition {

	public static final VeinLocation BLANK = new VeinLocation("blankvein", new ResourceLocation("blankvein"),
			new BlockPos(0, 0, 0));
	static List<String> prefixes = Arrays.asList("Superior", "Inferior", "Major", "Minor", "Cranial", "Caudal",
			"Anterior", "Ventral", "Posterior", "Dorsal", "Proximal", "Lateral", "Medial", "Distal", "Pulmonary",
			"Systemic");
	static List<String> locNames = Arrays.asList("Vena", "Femoral", "Cerebral", "Cephalic", "Frontal", "Oral",
			"Orbital", "Costal", "Pectoral", "Scapular", "Sternal", "Vertebral", "Sacral", "Brachial", "Carpal",
			"Digital", "Azygos");
	static List<String> suffixes = Arrays.asList("Cava", "Vein", "Artery", "Arteriole", "Capillary");
	public static VeinLocation deserializeToLoc(CompoundTag nbt) {
		String name;
		BlockPos bPos;
		ResourceLocation dim;
		UUID uuid;
		if (nbt.contains("dim") && nbt.contains("pos") && nbt.contains("name") && nbt.contains("id")) {
			name = nbt.getString("name");
			uuid = nbt.getUUID("id");
			dim = new ResourceLocation(nbt.getString("dim"));
			bPos = NbtUtils.readBlockPos(nbt.getCompound("pos"));
			VeinLocation loc = new VeinLocation(uuid,name, dim, bPos);
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
	public void deserializeNBT(CompoundTag nbt) {
		super.deserializeNBT(nbt);
		if (nbt.contains("name") && nbt.contains("id")) {
			String name = nbt.getString("dim");
			UUID uuid = nbt.getUUID("id");
			this.name = name;
			this.uuid = uuid;
		}
	}

	public String getName() {
		return name;
	}

	public UUID getUUID() {
		return uuid;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = super.serializeNBT();
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
