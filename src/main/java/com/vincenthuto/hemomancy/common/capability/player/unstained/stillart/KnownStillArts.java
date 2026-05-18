package com.vincenthuto.hemomancy.common.capability.player.unstained.stillart;

import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class KnownStillArts implements IKnownStillArts, INBTSerializable<CompoundTag> {
	private List<String> knownArtNames = new ArrayList<>();
	private String selectedArtName = StillArt.BLANK.getName();

	@Override
	public List<String> getKnownArtNames() {
		return knownArtNames;
	}

	@Override
	public void setKnownArtNames(List<String> names) {
		this.knownArtNames = names != null ? new ArrayList<>(names) : new ArrayList<>();
		if (!knownArtNames.contains(selectedArtName)) {
			selectedArtName = knownArtNames.isEmpty() ? StillArt.BLANK.getName() : knownArtNames.get(0);
		}
	}

	@Override
	public List<StillArt> getKnownArts() {
		List<StillArt> arts = new ArrayList<>();
		for (String name : knownArtNames) {
			StillArt art = StillArt.byName(name);
			if (art != StillArt.BLANK) {
				arts.add(art);
			}
		}
		return arts;
	}

	@Override
	public boolean isKnown(StillArt art) {
		return art != null && art != StillArt.BLANK && knownArtNames.contains(art.getName());
	}

	@Override
	public boolean learnArt(StillArt art) {
		if (art == null || art == StillArt.BLANK || knownArtNames.contains(art.getName())) {
			return false;
		}
		knownArtNames.add(art.getName());
		if (StillArt.BLANK.getName().equals(selectedArtName)) {
			selectedArtName = art.getName();
		}
		return true;
	}

	@Override
	public StillArt getSelectedArt() {
		StillArt selected = StillArt.byName(selectedArtName);
		if (selected == StillArt.BLANK && !knownArtNames.isEmpty()) {
			selectedArtName = knownArtNames.get(0);
			return StillArt.byName(selectedArtName);
		}
		return selected;
	}

	@Override
	public void setSelectedArt(StillArt art) {
		if (art == null || art == StillArt.BLANK || !knownArtNames.contains(art.getName())) {
			return;
		}
		this.selectedArtName = art.getName();
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putString("selected", selectedArtName);
		ListTag knownTag = new ListTag();
		for (String name : knownArtNames) {
			knownTag.add(StringTag.valueOf(name));
		}
		tag.put("known", knownTag);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		selectedArtName = tag.getString("selected");
		List<String> names = new ArrayList<>();
		ListTag knownTag = tag.getList("known", Tag.TAG_STRING);
		for (int i = 0; i < knownTag.size(); i++) {
			names.add(knownTag.getString(i));
		}
		setKnownArtNames(names);
	}
}
