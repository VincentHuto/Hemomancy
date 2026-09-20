package com.vincenthuto.hemomancy.common.succession;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

public final class SuccessionSavedData extends SavedData {
    private static final Factory<SuccessionSavedData> FACTORY = new Factory<>(SuccessionSavedData::new, SuccessionSavedData::load, null);
    public SuccessionLedger ledger = new SuccessionLedger();
    public final Map<UUID, SuccessorRecord> residents = new LinkedHashMap<>();
    private final Map<String, CompoundTag> bequests = new HashMap<>();
    private final Map<UUID, CompoundTag> samples = new HashMap<>();

    public static SuccessionSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, "hemomancy_succession");
    }
    public boolean hasBequest(UUID donor, UUID line, String profession) {
        var entry = bequests.get(donor + "/" + line);
        return entry != null && entry.getString("Profession").equals(profession);
    }
    public void bequeath(UUID donor, UUID line, String profession, String name) {
        var t = new CompoundTag(); t.putUUID("Donor", donor); t.putUUID("Bloodline", line);
        t.putString("Profession", profession); t.putString("Name", name);
        bequests.put(donor + "/" + line, t); setDirty();
    }
    public UUID issueSample(CompoundTag provenance) {
        UUID token = UUID.randomUUID(); samples.put(token, provenance.copy()); setDirty(); return token;
    }
    public boolean authentic(CompoundTag sample) {
        if (!sample.hasUUID("Token")) return false;
        CompoundTag issued = samples.get(sample.getUUID("Token"));
        var identity = sample.copy(); identity.remove("Token");
        return issued != null && issued.equals(identity);
    }
    public void spendSample(CompoundTag sample) {
        if (sample.hasUUID("Token")) { samples.remove(sample.getUUID("Token")); setDirty(); }
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("Ledger", ledger.save());
        var people = new ListTag(); residents.values().forEach(r -> people.add(r.save())); tag.put("Residents", people);
        var gifts = new ListTag(); bequests.values().forEach(t -> gifts.add(t.copy())); tag.put("Bequests", gifts);
        var vials = new ListTag(); samples.forEach((id, t) -> { var e = t.copy(); e.putUUID("Token", id); vials.add(e); });
        tag.put("Samples", vials); return tag;
    }
    public static SuccessionSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        var data = new SuccessionSavedData(); data.ledger = SuccessionLedger.load(tag.getCompound("Ledger"));
        for (Tag raw : tag.getList("Residents", Tag.TAG_COMPOUND)) {
            var person = new SuccessorRecord((CompoundTag) raw); data.residents.put(person.id, person);
        }
        for (Tag raw : tag.getList("Bequests", Tag.TAG_COMPOUND)) {
            var t = (CompoundTag) raw; data.bequests.put(t.getUUID("Donor") + "/" + t.getUUID("Bloodline"), t.copy());
        }
        for (Tag raw : tag.getList("Samples", Tag.TAG_COMPOUND)) {
            var t = ((CompoundTag) raw).copy(); UUID id = t.getUUID("Token"); t.remove("Token"); data.samples.put(id, t);
        }
        return data;
    }
}
