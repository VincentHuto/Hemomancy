package com.vincenthuto.hemomancy.common.brewing;

import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BrewingResolver {
    private BrewingResolver() {}

    public static BrewingMatch resolve(Level level, AlembicTier tier, ItemStack input,
                                       ItemStack catalyst, ItemStack catalyst2) {
        if (tier == AlembicTier.BASE || input.isEmpty() || catalyst.isEmpty()
                || !tier.hasSecondCatalyst() && !catalyst2.isEmpty()) return null;
        AdvancedBrewData existing = input.get(DataComponentInit.ADVANCED_BREW.get());
        if (tier.hasSecondCatalyst() && existing != null && existing.kind().equals("vessel")
                && !catalyst2.isEmpty()) {
            if (existing.doses() >= 3 || !existing.matchesOutputContents(input.get(DataComponents.POTION_CONTENTS))
                    || !sameFormula(catalyst, catalyst2, existing)) return null;
            ItemStack output = input.copyWithCount(1);
            output.set(DataComponentInit.ADVANCED_BREW.get(), existing.withDoses(BoundBrewRules.refillDoses(existing.doses())));
            return new BrewingMatch("refill", input, catalyst, catalyst2, output, 750, 160);
        }
        if (tier.hasSecondCatalyst() && !catalyst2.isEmpty()) {
            PotionContents base = source(input, false);
            PotionContents first = source(catalyst, false);
            PotionContents second = source(catalyst2, false);
            if (base == null || first == null || second == null || !distinct(base, first, second)) return null;
            ItemStack output = mixture(input, List.of(base, first, second), "vessel", "", 1);
            return new BrewingMatch("bind", input, catalyst, catalyst2, output, 1000, 200);
        }
        if (catalyst.is(Items.POTION)) {
            PotionContents base = source(input, true);
            PotionContents second = source(catalyst, true);
            if (base == null || second == null || !distinct(base, second)) return null;
            String attachment = attachment(input);
            ItemStack output = mixture(input, List.of(base, second), "compound", attachment, 0);
            return new BrewingMatch("compound", input, catalyst, ItemStack.EMPTY, output, 500, 160);
        }
        for (var recipe : level.getRecipeManager().getAllRecipesFor(RecipeInit.advanced_brewing_type.get())) {
            if (recipe.value().matchesStacks(input, catalyst, tier)) {
                AdvancedBrewingRecipe value = recipe.value();
                return new BrewingMatch("refine", input, catalyst, ItemStack.EMPTY,
                        value.getResultItem(level.registryAccess()), value.blood(), value.ticks());
            }
        }
        return null;
    }

    static PotionContents source(ItemStack stack, boolean allowPoise) {
        if (!stack.is(Items.POTION)) return null;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return null;
        AdvancedBrewData data = stack.get(DataComponentInit.ADVANCED_BREW.get());
        if (data != null) {
            if (!allowPoise || !data.kind().equals("refined") || !data.attachment().equals("ferric_poise")
                    || !data.parts().get(0).equals(contents)) return null;
        } else {
            if (contents.potion().isEmpty() || !contents.customEffects().isEmpty()) return null;
            String namespace = BuiltInRegistries.POTION.getKey(contents.potion().get().value()).getNamespace();
            if (!namespace.equals("minecraft") && !namespace.equals("hemomancy")) return null;
        }
        int count = 0;
        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (++count > 1 || effect.getDuration() <= 0 || effect.getEffect().value().isInstantenous()) return null;
        }
        return count == 1 ? contents : null;
    }

    private static String attachment(ItemStack stack) {
        AdvancedBrewData data = stack.get(DataComponentInit.ADVANCED_BREW.get());
        return data == null ? "" : data.attachment();
    }

    private static boolean sameFormula(ItemStack first, ItemStack second, AdvancedBrewData formula) {
        PotionContents a = source(first, false);
        PotionContents b = source(second, false);
        return a != null && b != null &&
                ((sameEffect(a, formula.parts().get(1)) && sameEffect(b, formula.parts().get(2)))
                || (sameEffect(a, formula.parts().get(2)) && sameEffect(b, formula.parts().get(1))));
    }

    private static boolean sameEffect(PotionContents first, PotionContents second) {
        MobEffectInstance a = first.getAllEffects().iterator().next();
        MobEffectInstance b = second.getAllEffects().iterator().next();
        return a.getEffect().equals(b.getEffect()) && a.getAmplifier() == b.getAmplifier()
                && a.getDuration() == b.getDuration();
    }

    private static boolean distinct(PotionContents... parts) {
        List<Object> effects = new ArrayList<>();
        for (PotionContents part : parts) {
            MobEffectInstance effect = part.getAllEffects().iterator().next();
            if (effects.contains(effect.getEffect())) return false;
            effects.add(effect.getEffect());
        }
        return true;
    }

    private static ItemStack mixture(ItemStack base, List<PotionContents> parts, String kind,
                                     String attachment, int doses) {
        ItemStack output = base.copyWithCount(1);
        List<MobEffectInstance> effects = new ArrayList<>();
        for (PotionContents part : parts) for (MobEffectInstance effect : part.getAllEffects())
            effects.add(new MobEffectInstance(effect));
        PotionContents original = parts.get(0);
        output.set(DataComponents.POTION_CONTENTS,
                new PotionContents(Optional.empty(), Optional.of(original.getColor()), effects));
        output.set(DataComponentInit.ADVANCED_BREW.get(), new AdvancedBrewData(kind, parts, attachment, doses));
        output.set(DataComponents.CUSTOM_NAME, Component.translatable(
                kind.equals("vessel") ? "item.hemomancy.bound_potion" : "item.hemomancy.compound_potion",
                base.getHoverName()));
        return output;
    }
}
