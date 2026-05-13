package com.vincenthuto.hemomancy.client.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingBatHeadAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingChitiniteLegAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingFungalHeadModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingLeechArmAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingSerpentLegAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingSpiderBodyAttachmentModel;
import com.vincenthuto.hemomancy.client.morphling.MorphlingModelAttachment.AttachmentPoint;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Client-side registry mapping each morphling item to its visual mutation.
 * Call {@link #init()} once during client setup.
 */
@OnlyIn(Dist.CLIENT)
public class MorphlingMutationRegistry {

    private static final Map<Item, MorphlingVisualMutation> REGISTRY = new HashMap<>();

    // Reuse the existing avatar_glow texture as the energySwirl base pattern
    private static final ResourceLocation SWIRL_TEX =
            Hemomancy.rloc("textures/models/armor/avatar_glow.png");

    public static void init() {
        // Bat — head crest attachment example
        register(ItemInit.morphling_bat.get(),
                MorphlingVisualMutation.builder(0.24f, 0.10f, 0.42f, 0.38f)
                        .pulse(0.06f).emissive()
                        .attach(batHeadAttachment())
                        .build());

        // Spider — torso carapace attachment example
        register(ItemInit.morphling_spider.get(),
                MorphlingVisualMutation.builder(0.10f, 0.10f, 0.18f, 0.42f)
                        .attach(spiderBodyAttachment())
                        .build());

        // Fungal — head-replacement mushroom parasite
        register(ItemInit.morphling_fungal.get(),
                MorphlingVisualMutation.builder(0.78f, 0.18f, 0.05f, 0.34f)
                        .swirl(SWIRL_TEX, 0.008f).emissive()
                        .attach(fungalHeadAttachment())
                        .build());

        // Leeches — left-arm cluster attachment example
        register(ItemInit.morphling_leeches.get(),
                MorphlingVisualMutation.builder(0.55f, 0.03f, 0.03f, 0.42f)
                        .pulse(0.04f)
                        .attach(leechArmAttachment())
                        .build());

        // Chitinite — right-leg plating attachment example
        register(ItemInit.morphling_chitinite.get(),
                MorphlingVisualMutation.builder(0.55f, 0.42f, 0.08f, 0.36f)
                        .attach(chitiniteLegAttachment())
                        .build());

        // Serpent — left-leg coil attachment example
        register(ItemInit.morphling_serpent.get(),
                MorphlingVisualMutation.builder(0.10f, 0.48f, 0.16f, 0.36f)
                        .swirl(SWIRL_TEX, 0.012f)
                        .attach(serpentLegAttachment())
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

    private static final ResourceLocation BAT_HEAD_ATTACHMENT_TEX =
            Hemomancy.rloc("textures/models/morphling/bat_head_attachment.png");
    private static final ResourceLocation SPIDER_BODY_ATTACHMENT_TEX =
            Hemomancy.rloc("textures/models/morphling/spider_body_attachment.png");
    private static final ResourceLocation LEECH_ARM_ATTACHMENT_TEX =
            Hemomancy.rloc("textures/models/morphling/leech_arm_attachment.png");
    private static final ResourceLocation CHITINITE_LEG_ATTACHMENT_TEX =
            Hemomancy.rloc("textures/models/morphling/chitinite_leg_attachment.png");
    private static final ResourceLocation SERPENT_LEG_ATTACHMENT_TEX =
            Hemomancy.rloc("textures/models/morphling/serpent_leg_attachment.png");
    private static final ResourceLocation FUNGAL_HEAD_TEX =
            Hemomancy.rloc("textures/models/morphling/fungal_head.png");

    private static MorphlingModelAttachment batHeadAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.HEAD, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingBatHeadAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingBatHeadAttachmentModel.LAYER_LOCATION))),
                BAT_HEAD_ATTACHMENT_TEX);
    }

    private static MorphlingModelAttachment spiderBodyAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.BODY, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingSpiderBodyAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingSpiderBodyAttachmentModel.LAYER_LOCATION))),
                SPIDER_BODY_ATTACHMENT_TEX);
    }

    private static MorphlingModelAttachment leechArmAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.LEFT_ARM, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingLeechArmAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingLeechArmAttachmentModel.LAYER_LOCATION))),
                LEECH_ARM_ATTACHMENT_TEX);
    }

    private static MorphlingModelAttachment chitiniteLegAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.RIGHT_LEG, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingChitiniteLegAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingChitiniteLegAttachmentModel.LAYER_LOCATION))),
                CHITINITE_LEG_ATTACHMENT_TEX);
    }

    private static MorphlingModelAttachment serpentLegAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.LEFT_LEG, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingSerpentLegAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingSerpentLegAttachmentModel.LAYER_LOCATION))),
                SERPENT_LEG_ATTACHMENT_TEX);
    }

    private static MorphlingModelAttachment fungalHeadAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.HEAD, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingFungalHeadModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingFungalHeadModel.LAYER_LOCATION))),
                FUNGAL_HEAD_TEX)
                .hideAttachedPart();
    }
}
