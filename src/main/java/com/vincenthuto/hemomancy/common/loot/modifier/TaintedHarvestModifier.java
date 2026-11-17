package com.vincenthuto.hemomancy.common.loot.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.MycelialLanternBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.world.farm.BloodFedGround;
import com.vincenthuto.hemomancy.common.world.farm.BloodFedGrowthRules;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Tainted harvest (core-loop round two, P7): a ripe crop broken on over-fed ground (a fresh planting basin or a feeding
 * run beside it, level 2) now and then gives up one more item with a Harbinger's blood in it. Only crops of the data's
 * {@code crops} tag that do not need blood to grow; a drip or a stain (level 1) never taints, and the cloche's sealed bed
 * is never asked. The chance and the item are this modifier's data.
 */
public final class TaintedHarvestModifier extends LootModifier {
	public static final MapCodec<TaintedHarvestModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).and(instance.group(
			TagKey.codec(Registries.BLOCK).fieldOf("crops").forGetter(m -> m.crops),
			ResourceLocation.CODEC.fieldOf("item").forGetter(m -> m.item),
			Codec.FLOAT.optionalFieldOf("chance", BloodFedGrowthRules.TAINT_CHANCE).forGetter(m -> m.chance)))
			.apply(instance, TaintedHarvestModifier::new));

	private final TagKey<Block> crops;
	private final ResourceLocation item;
	private final float chance;

	public TaintedHarvestModifier(LootItemCondition[] conditions, TagKey<Block> crops, ResourceLocation item, float chance) {
		super(conditions);
		this.crops = crops;
		this.item = item;
		this.chance = chance;
	}

	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
		BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
		Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
		if (state == null || origin == null || !(context.getLevel() instanceof ServerLevel level)) return loot;
		if (!state.is(crops) || state.is(BloodFedGround.NEEDS_BLOOD)) return loot;
		BlockPos pos = BlockPos.containing(origin);
		// The cloche harvests through its own loot at its own position: a sealed bed, never tainted.
		if (level.getBlockState(pos).getBlock() instanceof MycelialLanternBlock) return loot;
		boolean ripe = BloodFedGround.ripe(state);
		if (!ripe) return loot;
		if (!BloodFedGrowthRules.taints(BloodFedGround.level(level, pos), true, context.getRandom().nextFloat(), chance)) return loot;
		BuiltInRegistries.ITEM.getOptional(item).ifPresent(tainted -> {
			loot.add(new ItemStack(tainted));
			// The breaker with their blood awake finds the first one: the tainted-harvest first.
			if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer breaker
					&& HemoCapabilityAccess.getBloodVolume(breaker).isActive()) {
				HarbingerAdvancementGranter.grantIfNotDone(breaker, HarbingerAdvancementGranter.ADV_TAINTED_HARVEST);
			}
		});
		return loot;
	}

	@Override
	public MapCodec<? extends LootModifier> codec() {
		return CODEC;
	}
}
