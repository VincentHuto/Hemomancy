package com.vincenthuto.hemomancy.common.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BloodRoutingSavedData extends SavedData {
    private static final String DATA_NAME = "hemomancy_direct_blood_routes";
    private static final String TAG_LINKS = "Links";
    private static final String TAG_POS = "Pos";
    private static final String TAG_DATA = "Data";
    private static final SavedData.Factory<BloodRoutingSavedData> FACTORY =
            new SavedData.Factory<>(BloodRoutingSavedData::new, BloodRoutingSavedData::load, null);

    private final Map<Long, DirectBloodLinkData> links = new HashMap<>();

    public static BloodRoutingSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static BloodRoutingSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        BloodRoutingSavedData data = new BloodRoutingSavedData();
        if (tag.contains(TAG_LINKS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_LINKS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                DirectBloodLinkData link = DirectBloodLinkData.load(entry.getCompound(TAG_DATA));
                if (link != null) {
                    data.links.put(entry.getLong(TAG_POS), link);
                }
            }
        }
        return data;
    }

    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<Long, DirectBloodLinkData> entry : links.entrySet()) {
            CompoundTag linkTag = new CompoundTag();
            linkTag.putLong(TAG_POS, entry.getKey());
            linkTag.put(TAG_DATA, entry.getValue().save());
            list.add(linkTag);
        }
        tag.put(TAG_LINKS, list);
        return tag;
    }

    @Nullable
    public DirectBloodLinkData getLink(BlockPos pos) {
        return links.get(pos.asLong());
    }

    public void setLink(BlockPos pos, DirectBloodLinkData link) {
        links.put(pos.asLong(), link);
        setDirty();
    }

    public void removeLink(BlockPos pos) {
        if (links.remove(pos.asLong()) != null) {
            setDirty();
        }
    }

    public List<Map.Entry<BlockPos, DirectBloodLinkData>> entries() {
        List<Map.Entry<BlockPos, DirectBloodLinkData>> snapshot = new ArrayList<>();
        for (Map.Entry<Long, DirectBloodLinkData> entry : links.entrySet()) {
            snapshot.add(Map.entry(BlockPos.of(entry.getKey()), entry.getValue()));
        }
        return snapshot;
    }

    public List<Map.Entry<BlockPos, DirectBloodLinkData>> entriesForOwner(UUID owner) {
        List<Map.Entry<BlockPos, DirectBloodLinkData>> snapshot = new ArrayList<>();
        if (owner == null) {
            return snapshot;
        }
        for (Map.Entry<Long, DirectBloodLinkData> entry : links.entrySet()) {
            if (owner.equals(entry.getValue().owner())) {
                snapshot.add(Map.entry(BlockPos.of(entry.getKey()), entry.getValue()));
            }
        }
        return snapshot;
    }
}
