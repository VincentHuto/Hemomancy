package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.succession.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/** Explicit test assist. Production successors must be born through a rite. */
public final class SuccessionTestFixtures {
    private SuccessionTestFixtures() {}
    public static SuccessorRecord resident(ServerPlayer player, ProfessionalHarbingerEntity npc, BlockPos workplace) {
        var line=SuccessionResidents.line(player); var tag=new CompoundTag();
        tag.putUUID("Id",npc.getUUID());tag.putUUID("Bloodline",line.getBloodlineUUID());
        tag.putUUID("Donor",java.util.UUID.randomUUID());tag.putUUID("Officiant",player.getUUID());tag.putUUID("FaneOwner",line.getLeaderUUID());
        tag.putString("Profession",SuccessionProfessions.profession(npc));tag.putString("Name","Fixture "+SuccessionProfessions.profession(npc));
        tag.putString("DonorName","Fixture Teacher");tag.putString("OfficiantName",player.getName().getString());
        tag.putString("Dimension",player.level().dimension().location().toString());tag.putLong("Seed",npc.getUUID().getLeastSignificantBits());tag.putLong("Workplace",workplace.asLong());
        var record=new SuccessorRecord(tag);var data=SuccessionSavedData.get(player.serverLevel());
        data.ledger.birth(record.id);data.ledger.claim(record.id,record.place());data.residents.put(record.id,record);data.setDirty();
        npc.initializeSuccessor(record);SuccessionResidents.join(player.serverLevel(),record);return record;
    }
}
