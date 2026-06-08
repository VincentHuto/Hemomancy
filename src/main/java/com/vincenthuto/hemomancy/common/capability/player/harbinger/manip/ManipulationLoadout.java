package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public record ManipulationLoadout(String name, String selectedManipName, List<String> manipNames) {
	public static final int MAX_NAME_LENGTH = 24;

	public ManipulationLoadout {
		name = sanitizeName(name, 0);
		selectedManipName = selectedManipName != null ? selectedManipName.trim() : "";
		manipNames = List.copyOf(normalizeManipNames(manipNames));
		if (manipNames.isEmpty()) {
			selectedManipName = "";
		} else if (selectedManipName.isEmpty()
				|| ManipulationEquipHelper.isFixedMechanicalManip(selectedManipName)
				|| !manipNames.contains(selectedManipName)) {
			selectedManipName = manipNames.getFirst();
		}
	}

	public static ManipulationLoadout empty(int slotIndex) {
		return new ManipulationLoadout(defaultName(slotIndex), "", List.of());
	}

	public static ManipulationLoadout of(String name, String selectedManipName, List<String> manipNames, int slotIndex) {
		return new ManipulationLoadout(sanitizeName(name, slotIndex), selectedManipName, manipNames);
	}

	public ManipulationLoadout renamed(String newName, int slotIndex) {
		return of(newName, selectedManipName, manipNames, slotIndex);
	}

	public boolean isEmpty() {
		return manipNames.isEmpty();
	}

	public CompoundTag toTag() {
		CompoundTag tag = new CompoundTag();
		tag.putString("name", name);
		tag.putString("selectedManip", selectedManipName);
		ListTag manipTag = new ListTag();
		for (String manipName : manipNames) {
			manipTag.add(StringTag.valueOf(manipName));
		}
		tag.put("manips", manipTag);
		return tag;
	}

	public static ManipulationLoadout fromTag(CompoundTag tag, int slotIndex) {
		if (tag == null || tag.isEmpty()) {
			return empty(slotIndex);
		}
		List<String> names = new ArrayList<>();
		ListTag manipTag = tag.getList("manips", Tag.TAG_STRING);
		for (int i = 0; i < manipTag.size(); i++) {
			names.add(manipTag.getString(i));
		}
		return of(tag.getString("name"), tag.getString("selectedManip"), names, slotIndex);
	}

	public void writeToBuf(FriendlyByteBuf buf) {
		buf.writeUtf(name);
		buf.writeUtf(selectedManipName);
		buf.writeInt(manipNames.size());
		for (String manipName : manipNames) {
			buf.writeUtf(manipName);
		}
	}

	public static ManipulationLoadout readFromBuf(FriendlyByteBuf buf, int slotIndex) {
		String name = buf.readUtf();
		String selectedManip = buf.readUtf();
		int count = buf.readInt();
		List<String> names = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			names.add(buf.readUtf());
		}
		return of(name, selectedManip, names, slotIndex);
	}

	public static String defaultName(int slotIndex) {
		return "Loadout " + (slotIndex + 1);
	}

	public static String sanitizeName(String name, int slotIndex) {
		String cleaned = name != null ? name.trim() : "";
		if (cleaned.isEmpty()) {
			cleaned = defaultName(slotIndex);
		}
		if (cleaned.length() > MAX_NAME_LENGTH) {
			cleaned = cleaned.substring(0, MAX_NAME_LENGTH);
		}
		return cleaned;
	}

	public static List<String> normalizeManipNames(List<String> names) {
		LinkedHashSet<String> unique = new LinkedHashSet<>();
		if (names != null) {
			for (String name : names) {
				if (name == null) {
					continue;
				}
				String cleaned = name.trim();
				if (!cleaned.isEmpty() && !ManipulationEquipHelper.isFixedMechanicalManip(cleaned)) {
					unique.add(cleaned);
				}
			}
		}
		return new ArrayList<>(unique);
	}
}
