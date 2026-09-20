package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.item.unstained.CleansingHemolymphItem;
import net.minecraft.world.item.ItemStack;

/** Selects a microscope presentation without changing the specimen's biological profile. */
public record MicroscopeSpecimenVisual(Kind kind, EnumSaintType saint) {
    public enum Kind { ORDINARY_BLOOD, AHAEMATIC_COLLOID, CLEANSING_HEMOLYMPH, CONSECRATED_SAINT_BLOOD }

    public static MicroscopeSpecimenVisual of(ItemStack stack) {
        if (stack.getItem() instanceof CleansingHemolymphItem)
            return new MicroscopeSpecimenVisual(Kind.CLEANSING_HEMOLYMPH, null);
        if (stack.getItem() instanceof ConsecratedSyringeItem) {
            var saint = ConsecratedSyringeItem.getSaintType(stack);
            return saint == null
                    ? new MicroscopeSpecimenVisual(Kind.ORDINARY_BLOOD, null)
                    : new MicroscopeSpecimenVisual(Kind.CONSECRATED_SAINT_BLOOD, saint);
        }
        if (com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.is(stack))
            return new MicroscopeSpecimenVisual(Kind.AHAEMATIC_COLLOID, null);
        return new MicroscopeSpecimenVisual(Kind.ORDINARY_BLOOD, null);
    }
}
