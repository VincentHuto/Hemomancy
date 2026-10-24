package com.vincenthuto.hemomancy.common.loot.modifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;
import com.vincenthuto.hemomancy.common.item.component.AncientRecordings;

public final class AncientRecordingLootModifier extends LootModifier {
    public static final MapCodec<AncientRecordingLootModifier> CODEC=RecordCodecBuilder.mapCodec(instance->codecStart(instance).apply(instance,AncientRecordingLootModifier::new));
    public AncientRecordingLootModifier(LootItemCondition[] conditions) {super(conditions);}
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot,LootContext context) {
        loot.add(AncientRecordings.cylinder(context.getRandom().nextBoolean()?AncientRecordings.SURVEY:AncientRecordings.QUIETING));return loot;
    }
    @Override public MapCodec<AncientRecordingLootModifier> codec() {return CODEC;}
}
