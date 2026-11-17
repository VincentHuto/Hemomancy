package com.vincenthuto.hemomancy.common.loot.modifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodyFlaskItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

import javax.annotation.Nonnull;

/** Stamps every clotting Flask or Jug in generated loot with the time it dropped (FX-2). Listed last, after the adders. */
public class StampBottledModifier extends LootModifier {
	public static final MapCodec<StampBottledModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
			codecStart(inst).apply(inst, StampBottledModifier::new));

	protected StampBottledModifier(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Nonnull
	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		long now = context.getLevel().getGameTime();
		for (ItemStack stack : generatedLoot) BloodyFlaskItem.stampBottled(stack, now);
		return generatedLoot;
	}

	@Override
	public MapCodec<StampBottledModifier> codec() {
		return CODEC;
	}
}
