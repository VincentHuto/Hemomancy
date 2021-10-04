package com.vincenthuto.hemomancy.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.vincenthuto.hutoslib.math.DimensionalPosition;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;

public class VeinLocation extends DimensionalPosition {

	public static final VeinLocation BLANK = new VeinLocation("blankvein", new ResourceLocation("blankvein"),
			new BlockPos(0, 0, 0));
	private String name;
	static List<String> prefixes = Arrays.asList("Superior", "Inferior", "Major", "Minor", "Cranial", "Caudal",
			"Anterior", "Ventral", "Posterior", "Dorsal", "Proximal", "Lateral", "Medial", "Distal", "Pulmonary",
			"Systemic");
	static List<String> locNames = Arrays.asList("Vena", "Femoral", "Cerebral", "Cephalic", "Frontal", "Oral",
			"Orbital", "Costal", "Pectoral", "Scapular", "Sternal", "Vertebral", "Sacral", "Brachial", "Carpal",
			"Digital", "Azygos");
	static List<String> suffixes = Arrays.asList("Cava", "Vein", "Artery", "Arteriole", "Capillary");

	/*
	 * to get RL Player().level.dimension().location();
	 */
	public VeinLocation(String name, ResourceLocation dim, BlockPos pos) {
		super(dim, pos);
		this.name = name;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = super.serializeNBT();
		nbt.putString("name", this.name);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		super.deserializeNBT(nbt);
		if (nbt.contains("name")) {
			String name = nbt.getString("dim");
			this.name = name;
		}
	}

	public static VeinLocation deserializeToLoc(CompoundTag nbt) {
		String name;
		BlockPos bPos;
		ResourceLocation dim;
		if (nbt.contains("dim") && nbt.contains("pos") && nbt.contains("name")) {
			name = nbt.getString("name");
			dim = new ResourceLocation(nbt.getString("dim"));
			bPos = NbtUtils.readBlockPos(nbt.getCompound("pos"));
			VeinLocation loc = new VeinLocation(name, dim, bPos);
			return loc;
		}

		return VeinLocation.BLANK;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static String getRandomName() {
		Random rand = new Random();
		String prefix = prefixes.get(rand.nextInt(prefixes.size()));
		String locationName = locNames.get(rand.nextInt(locNames.size()));
		String suffix = suffixes.get(rand.nextInt(suffixes.size()));
		return prefix + " " + locationName + " " + suffix;
	}

	public void setRandomName() {
		this.name = getRandomName();
	}

	@Override
	public String toString() {
		return ("Vein: '" + getName() + "' Located in: " + getDimension().getPath() + " at Position: " + getPosition());
	}
}
