package com.vincenthuto.hemomancy.client.model.morphling;

import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingStrainModels;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Client-side registry mapping each morphling item to its visual mutation: the strain row's body tint, the way that
 * tint moves, and one generated attachment per anchor from {@link MorphlingStrainModels}. Call {@link #init()} once
 * during client setup.
 */
@OnlyIn(Dist.CLIENT)
public class MorphlingMutationRegistry {

    private static final Map<Item, MorphlingVisualMutation> REGISTRY = new HashMap<>();

    public static void init() {
        for (MorphlingStrain strain : MorphlingStrains.ALL) {
            register(strain.item().get(), mutationFor(strain));
        }
    }

    static MorphlingVisualMutation mutationFor(MorphlingStrain strain) {
        int tint = strain.tintColor();
        MorphlingVisualMutation.Builder builder = MorphlingVisualMutation.builder(
                (tint >> 16 & 0xFF) / 255f, (tint >> 8 & 0xFF) / 255f, (tint & 0xFF) / 255f, (tint >>> 24) / 255f);
        tintMotion(strain, builder);
        for (MorphlingStrainModels.Anchor anchor : MorphlingStrainModels.forStrain(strain.id())) {
            builder.attach(new MorphlingStrainAttachment(anchor));
        }
        return builder.build();
    }

    /** The palette table's motion for the tint itself (the attachments carry the rest). */
    private static void tintMotion(MorphlingStrain strain, MorphlingVisualMutation.Builder builder) {
        if (strain == MorphlingStrains.SUNBURST) builder.pulse(0.05f).emissive();        // slow warm pulse
        else if (strain == MorphlingStrains.MERMAIDS_PURSE) builder.pulse(0.09f);        // lateral-line shimmer
        else if (strain == MorphlingStrains.DEAD_MANS_ROPE) builder.pulse(0.12f).emissive(); // streaming pulse
        else if (strain == MorphlingStrains.HUSHBELL) builder.pulse(0.026f);              // held breath
        else if (strain == MorphlingStrains.MARROWPLUME) builder.pulse(0.04f);
        else if (strain == MorphlingStrains.KINBUD) builder.pulse(0.07f);                 // amber flicker over pale skin
    }

    public static void register(Item item, MorphlingVisualMutation mutation) {
        REGISTRY.put(item, mutation);
    }

    @Nullable
    public static MorphlingVisualMutation get(ItemStack stack) {
        return stack.isEmpty() ? null : REGISTRY.get(stack.getItem());
    }
}
