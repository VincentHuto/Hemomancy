package com.vincenthuto.hemomancy.common.rite.zone;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/** Reads the {@code hemomancy:rite_bound/*} block, item and entity type tags into {@link RiteBoundRules.Requirement}s. */
public final class RiteBound {
	private static final String ANY = "any";
	private static final Map<CardinalRitePurpose, TagKey<Block>> BLOCKS = new EnumMap<>(CardinalRitePurpose.class);
	private static final Map<CardinalRitePurpose, TagKey<Item>> ITEMS = new EnumMap<>(CardinalRitePurpose.class);
	private static final Map<CardinalRitePurpose, TagKey<EntityType<?>>> ENTITIES = new EnumMap<>(CardinalRitePurpose.class);
	public static final TagKey<Block> ANY_BLOCK = TagKey.create(Registries.BLOCK, Hemomancy.rloc("rite_bound/" + ANY));
	public static final TagKey<Item> ANY_ITEM = TagKey.create(Registries.ITEM, Hemomancy.rloc("rite_bound/" + ANY));
	public static final TagKey<EntityType<?>> ANY_ENTITY = TagKey.create(Registries.ENTITY_TYPE, Hemomancy.rloc("rite_bound/" + ANY));

	static {
		for (CardinalRitePurpose purpose : CardinalRitePurpose.values()) {
			BLOCKS.put(purpose, TagKey.create(Registries.BLOCK, Hemomancy.rloc("rite_bound/" + purpose.id())));
			ITEMS.put(purpose, TagKey.create(Registries.ITEM, Hemomancy.rloc("rite_bound/" + purpose.id())));
			ENTITIES.put(purpose, TagKey.create(Registries.ENTITY_TYPE, Hemomancy.rloc("rite_bound/" + purpose.id())));
		}
	}

	private RiteBound() {}

	public static RiteBoundRules.Requirement of(BlockState state) {
		return requirement(BLOCKS, state::is, state.is(ANY_BLOCK));
	}

	/** An item's own tags; a block item also carries its block's requirement, for tooltips. */
	public static RiteBoundRules.Requirement of(ItemStack stack) {
		RiteBoundRules.Requirement own = requirement(ITEMS, stack::is, stack.is(ANY_ITEM));
		if (own.bound() || !(stack.getItem() instanceof BlockItem blockItem)) return own;
		return of(blockItem.getBlock().defaultBlockState());
	}

	/** The item's own tags only: what gates using it, as opposed to placing it. */
	public static RiteBoundRules.Requirement ofUse(ItemStack stack) {
		return requirement(ITEMS, stack::is, stack.is(ANY_ITEM));
	}

	public static RiteBoundRules.Requirement of(EntityType<?> type) {
		return requirement(ENTITIES, type::is, type.is(ANY_ENTITY));
	}

	public static boolean isRiteBound(ItemStack stack) {
		return !stack.isEmpty() && of(stack).bound();
	}

	private static <T> RiteBoundRules.Requirement requirement(Map<CardinalRitePurpose, TagKey<T>> tags,
			Predicate<TagKey<T>> has, boolean any) {
		Set<CardinalRitePurpose> purposes = EnumSet.noneOf(CardinalRitePurpose.class);
		tags.forEach((purpose, tag) -> {
			if (has.test(tag)) purposes.add(purpose);
		});
		return any || !purposes.isEmpty() ? new RiteBoundRules.Requirement(purposes, any) : RiteBoundRules.Requirement.NONE;
	}
}
