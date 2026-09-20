package com.vincenthuto.hemomancy.common.succession;

import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.UUID;

public final class SuccessionSamples {
    public static final String KEY = "hemomancy_identity";
    private SuccessionSamples() {}
    public static CompoundTag identity(ItemStack vial) {
        return vial.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(KEY);
    }
    public static void fill(ItemStack vial, LivingEntity donor, UUID bloodline, String profession) {
        if (!(donor.level() instanceof ServerLevel level)) return;
        var identity = new CompoundTag(); identity.putUUID("Donor", donor.getUUID());
        identity.putString("Type", BuiltInRegistries.ENTITY_TYPE.getKey(donor.getType()).toString());
        identity.putString("Name", donor.getName().getString()); identity.putString("Profession", profession);
        if (bloodline != null) identity.putUUID("Bloodline", bloodline);
        identity.putUUID("Token", SuccessionSavedData.get(level).issueSample(identity));
        var data = vial.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putString(BloodVialItem.TAG_ENTITY_TYPE, identity.getString("Type")); data.putBoolean(BloodVialItem.TAG_STATE, true);
        data.put(KEY, identity); vial.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }
}
