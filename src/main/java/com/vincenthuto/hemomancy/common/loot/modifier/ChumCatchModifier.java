package com.vincenthuto.hemomancy.common.loot.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.world.chum.ChumPools;
import com.vincenthuto.hemomancy.common.world.chum.ChumRules;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.LootModifier;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Catches from a chum pool (core-loop Track 7): fishing with the hook inside a pool sometimes brings up one of the
 * pool's catches instead, at {@link ChumRules#catchChance} by the pool's strength, much more often in its home waters
 * (the biomes in {@code home_waters}). A pool a Temper catalyst coloured draws from its school's table when the data
 * has one, otherwise the plain table. The tables are this modifier's data, added a school at a time.
 */
public final class ChumCatchModifier extends LootModifier {
	public record Catch(ResourceLocation item, int weight, int count) {
		public static final Codec<Catch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.fieldOf("item").forGetter(Catch::item),
				Codec.INT.optionalFieldOf("weight", 1).forGetter(Catch::weight),
				Codec.INT.optionalFieldOf("count", 1).forGetter(Catch::count)).apply(instance, Catch::new));
	}

	public static final MapCodec<ChumCatchModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).and(instance.group(
			TagKey.codec(Registries.BIOME).fieldOf("home_waters").forGetter(m -> m.homeWaters),
			Catch.CODEC.listOf().fieldOf("catches").forGetter(m -> m.catches),
			Codec.unboundedMap(Codec.STRING, Catch.CODEC.listOf()).optionalFieldOf("by_school", Map.of())
					.forGetter(m -> m.bySchool))).apply(instance, ChumCatchModifier::new));

	private final TagKey<Biome> homeWaters;
	private final List<Catch> catches;
	private final Map<String, List<Catch>> bySchool;

	public ChumCatchModifier(LootItemCondition[] conditions, TagKey<Biome> homeWaters, List<Catch> catches,
			Map<String, List<Catch>> bySchool) {
		super(conditions);
		this.homeWaters = homeWaters;
		this.catches = List.copyOf(catches);
		this.bySchool = Map.copyOf(bySchool);
	}

	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
		Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
		if (origin == null || !(context.getLevel() instanceof ServerLevel level)) return loot;
		ChumPools.Pool pool = ChumPools.at(level, origin);
		if (pool == null) return loot;
		boolean home = level.getBiome(BlockPos.containing(origin)).is(homeWaters);
		if (context.getRandom().nextDouble() >= ChumRules.catchChance(pool.strength(), home)) return loot;
		List<Catch> table = pool.school() == null ? catches
				: bySchool.getOrDefault(pool.school().name().toLowerCase(Locale.ROOT), catches);
		ItemStack caught = pick(table, context.getRandom());
		if (caught.isEmpty()) return loot;
		ObjectArrayList<ItemStack> replaced = new ObjectArrayList<>();
		replaced.add(caught);
		ServerPlayer fisher = fisher(context);
		if (fisher != null && HemoCapabilityAccess.getBloodVolume(fisher).isActive()) {
			HarbingerAdvancementGranter.grantIfNotDone(fisher, HarbingerAdvancementGranter.ADV_CHUM_CATCH);
		}
		return replaced;
	}

	/** Who reeled it in: vanilla's hook names its owner as the attacking entity, and is itself this entity. */
	@Nullable
	private static ServerPlayer fisher(LootContext context) {
		if (context.getParamOrNull(LootContextParams.ATTACKING_ENTITY) instanceof ServerPlayer owner) return owner;
		if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof FishingHook hook
				&& hook.getPlayerOwner() instanceof ServerPlayer owner) return owner;
		return null;
	}

	private static ItemStack pick(List<Catch> table, RandomSource random) {
		int total = table.stream().mapToInt(c -> Math.max(0, c.weight())).sum();
		if (total <= 0) return ItemStack.EMPTY;
		int roll = random.nextInt(total);
		for (Catch entry : table) {
			roll -= Math.max(0, entry.weight());
			if (roll < 0) {
				return BuiltInRegistries.ITEM.getOptional(entry.item())
						.map(item -> new ItemStack(item, Math.max(1, entry.count()))).orElse(ItemStack.EMPTY);
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public MapCodec<? extends LootModifier> codec() {
		return CODEC;
	}
}
