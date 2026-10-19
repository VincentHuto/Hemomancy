package com.vincenthuto.hemomancy.common.rite.zone;

import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Every rite space in every dimension, stored in the Overworld like the other rite zones. One per Focus. */
public class RiteSpaceSavedData extends SavedData {
	private static final String DATA_NAME = "hemomancy_rite_spaces";
	private static final SavedData.Factory<RiteSpaceSavedData> FACTORY =
			new SavedData.Factory<>(RiteSpaceSavedData::new, RiteSpaceSavedData::load, null);

	private final List<RiteSpace> spaces = new ArrayList<>();

	public static RiteSpaceSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public List<RiteSpace> spaces() {
		return Collections.unmodifiableList(spaces);
	}

	/** Records a space, replacing any earlier one at the same Focus: the latest completed rite sets the purpose. */
	public void consecrate(RiteSpace space) {
		spaces.removeIf(existing -> sameFocus(existing, space.dimension(), space.focus()));
		spaces.add(space);
		changed();
	}

	/** Ends the space at a Focus. Returns true when there was one. */
	public boolean release(ResourceKey<Level> dimension, BlockPos focus) {
		boolean removed = spaces.removeIf(existing -> sameFocus(existing, dimension, focus));
		if (removed) changed();
		return removed;
	}

	public Optional<RiteSpace> atFocus(ResourceKey<Level> dimension, BlockPos focus) {
		return spaces.stream().filter(space -> sameFocus(space, dimension, focus)).findFirst();
	}

	public Optional<RiteSpace> spaceAt(BlockPos pos, ResourceKey<Level> dimension) {
		return spaces.stream().filter(space -> space.contains(pos, dimension)).findFirst();
	}

	public List<RiteSpace> spacesIn(ResourceKey<Level> dimension) {
		return spaces.stream().filter(space -> space.dimension().equals(dimension)).toList();
	}

	private static boolean sameFocus(RiteSpace space, ResourceKey<Level> dimension, BlockPos focus) {
		return space.dimension().equals(dimension) && space.focus().equals(focus);
	}

	private void changed() {
		setDirty();
		RiteSpaces.markChanged();
	}

	public static RiteSpaceSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		RiteSpaceSavedData data = new RiteSpaceSavedData();
		ListTag list = tag.getList("spaces", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			RiteSpace space = read(list.getCompound(i));
			if (space != null) data.spaces.add(space);
		}
		return data;
	}

	@Nullable
	private static RiteSpace read(CompoundTag tag) {
		ResourceKey<Level> dimension = RiteZone.dimensionKey(tag.getString("Dimension"));
		CardinalRitePurpose purpose = CardinalRitePurpose.byId(tag.getString("Purpose"));
		ResourceLocation floor = ResourceLocation.tryParse(tag.getString("Floor"));
		ResourceLocation rite = ResourceLocation.tryParse(tag.getString("Rite"));
		if (dimension == null || purpose == null || floor == null || rite == null || !tag.hasUUID("Owner")) return null;
		return new RiteSpace(BlockPos.of(tag.getLong("Focus")), dimension, tag.getUUID("Owner"), purpose, floor,
				tag.getInt("Half"), rite, tag.getLong("CreatedTick"));
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, @Nullable HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (RiteSpace space : spaces) {
			CompoundTag entry = new CompoundTag();
			entry.putLong("Focus", space.focus().asLong());
			entry.putString("Dimension", space.dimension().location().toString());
			entry.putUUID("Owner", space.owner());
			entry.putString("Purpose", space.purpose().id());
			entry.putString("Floor", space.floor().toString());
			entry.putInt("Half", space.half());
			entry.putString("Rite", space.rite().toString());
			entry.putLong("CreatedTick", space.createdTick());
			list.add(entry);
		}
		tag.put("spaces", list);
		return tag;
	}
}
