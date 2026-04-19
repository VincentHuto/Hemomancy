package com.vincenthuto.hemomancy.client.morphling;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side registry mapping each morphling item to its visual mutation.
 * Call {@link #init()} once during client setup.
 */
@OnlyIn(Dist.CLIENT)
public class MorphlingMutationRegistry {

    private static final Map<Item, MorphlingVisualMutation> REGISTRY = new HashMap<>();

    // Reuse the existing avatar_glow texture as the energySwirl base pattern
    private static final ResourceLocation SWIRL_TEX =
            new ResourceLocation(Hemomancy.MOD_ID, "textures/models/armor/avatar_glow.png");

    public static void init() {
        // Bat — dark tenebris purple, pulsing emissive glow
        register(ItemInit.morphling_bat.get(),
                MorphlingVisualMutation.builder(0.24f, 0.10f, 0.42f, 0.38f)
                        .pulse(0.06f).emissive().build());

        // Spider — dark shadowy blue-black overlay
        register(ItemInit.morphling_spider.get(),
                MorphlingVisualMutation.builder(0.10f, 0.10f, 0.18f, 0.42f)
                        .build());

        // Fungal — spore green swirling emissive
        register(ItemInit.morphling_fungal.get(),
                MorphlingVisualMutation.builder(0.12f, 0.48f, 0.12f, 0.36f)
                        .swirl(SWIRL_TEX, 0.008f).emissive().build());

        // Leeches — blood crimson with slow heartbeat pulse
        register(ItemInit.morphling_leeches.get(),
                MorphlingVisualMutation.builder(0.55f, 0.03f, 0.03f, 0.42f)
                        .pulse(0.04f).build());

        // Chitinite — bronze chitin plating tint
        register(ItemInit.morphling_chitinite.get(),
                MorphlingVisualMutation.builder(0.55f, 0.42f, 0.08f, 0.36f)
                        .build());

        // Serpent — scale green with shimmer swirl
        register(ItemInit.morphling_serpent.get(),
                MorphlingVisualMutation.builder(0.10f, 0.48f, 0.16f, 0.36f)
                        .swirl(SWIRL_TEX, 0.012f).build());

        // Pests — sickly dark verminous tint, fast flutter pulse
        register(ItemInit.morphling_pests.get(),
                MorphlingVisualMutation.builder(0.20f, 0.22f, 0.04f, 0.36f)
                        .pulse(0.10f).build());

        // Moth — luminous pale cream, slow breath pulse, full glow
        register(ItemInit.morphling_moth.get(),
                MorphlingVisualMutation.builder(0.91f, 0.86f, 0.78f, 0.32f)
                        .pulse(0.05f).emissive().build());

        // Tick — dark vein crimson, slow throb
        register(ItemInit.morphling_tick.get(),
                MorphlingVisualMutation.builder(0.42f, 0.04f, 0.04f, 0.46f)
                        .pulse(0.03f).build());

        // Urchin — barbed dark brown silhouette
        register(ItemInit.morphling_urchin.get(),
                MorphlingVisualMutation.builder(0.36f, 0.23f, 0.12f, 0.40f)
                        .build());

        // Centipede — cool blue-grey segmented overlay
        register(ItemInit.morphling_centipede.get(),
                MorphlingVisualMutation.builder(0.10f, 0.23f, 0.36f, 0.36f)
                        .build());

        // Mole — earthy brown earthen tint
        register(ItemInit.morphling_mole.get(),
                MorphlingVisualMutation.builder(0.29f, 0.22f, 0.16f, 0.30f)
                        .build());
    }

    public static void register(Item item, MorphlingVisualMutation mutation) {
        REGISTRY.put(item, mutation);
    }

    @Nullable
    public static MorphlingVisualMutation get(ItemStack stack) {
        return stack.isEmpty() ? null : REGISTRY.get(stack.getItem());
    }
}
