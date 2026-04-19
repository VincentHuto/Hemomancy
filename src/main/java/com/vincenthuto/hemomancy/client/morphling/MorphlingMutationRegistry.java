package com.vincenthuto.hemomancy.client.morphling;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingAttachmentExampleModel;
import com.vincenthuto.hemomancy.client.morphling.MorphlingModelAttachment.AttachmentPoint;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;

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
        // Bat — HEAD attachment example
        register(ItemInit.morphling_bat.get(),
                MorphlingVisualMutation.builder(0.24f, 0.10f, 0.42f, 0.38f)
                        .pulse(0.06f).emissive()
                        .attach(exampleAttachment(AttachmentPoint.HEAD, 0f, -6f, -2f, 0.55f))
                        .build());

        // Spider — BODY attachment example
        register(ItemInit.morphling_spider.get(),
                MorphlingVisualMutation.builder(0.10f, 0.10f, 0.18f, 0.42f)
                        .attach(exampleAttachment(AttachmentPoint.BODY, 0f, 5f, 2f, 0.75f))
                        .build());

        // Fungal — RIGHT_ARM attachment example
        register(ItemInit.morphling_fungal.get(),
                MorphlingVisualMutation.builder(0.12f, 0.48f, 0.12f, 0.36f)
                        .swirl(SWIRL_TEX, 0.008f).emissive()
                        .attach(exampleAttachment(AttachmentPoint.RIGHT_ARM, 0f, 4f, 0f, 0.5f))
                        .build());

        // Leeches — LEFT_ARM attachment example
        register(ItemInit.morphling_leeches.get(),
                MorphlingVisualMutation.builder(0.55f, 0.03f, 0.03f, 0.42f)
                        .pulse(0.04f)
                        .attach(exampleAttachment(AttachmentPoint.LEFT_ARM, 0f, 4f, 0f, 0.5f))
                        .build());

        // Chitinite — RIGHT_LEG attachment example
        register(ItemInit.morphling_chitinite.get(),
                MorphlingVisualMutation.builder(0.55f, 0.42f, 0.08f, 0.36f)
                        .attach(exampleAttachment(AttachmentPoint.RIGHT_LEG, 0f, 8f, 0f, 0.6f))
                        .build());

        // Serpent — LEFT_LEG attachment example
        register(ItemInit.morphling_serpent.get(),
                MorphlingVisualMutation.builder(0.10f, 0.48f, 0.16f, 0.36f)
                        .swirl(SWIRL_TEX, 0.012f)
                        .attach(exampleAttachment(AttachmentPoint.LEFT_LEG, 0f, 8f, 0f, 0.6f))
                        .build());

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

    private static MorphlingModelAttachment exampleAttachment(AttachmentPoint point,
            float offX, float offY, float offZ, float scale) {
        return MorphlingModelAttachment.of(point, offX, offY, offZ, scale,
                Lazy.of(() -> new MorphlingAttachmentExampleModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingAttachmentExampleModel.layerFor(point)))),
                SWIRL_TEX);
    }
}
