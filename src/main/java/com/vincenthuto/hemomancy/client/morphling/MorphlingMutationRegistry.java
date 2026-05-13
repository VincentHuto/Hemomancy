package com.vincenthuto.hemomancy.client.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingBatHeadAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingCentipedeBodyAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingChitiniteLegAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingFungalHeadModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingLeechArmAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingMoleArmAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingMothHeadAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingPestsBodyAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingSerpentLegAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingSpiderBodyAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingTickBodyAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingUrchinBodyAttachmentModel;
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

        // Leeches — paired arm cluster attachment example
        register(ItemInit.morphling_leeches.get(),
                MorphlingVisualMutation.builder(0.55f, 0.03f, 0.03f, 0.42f)
                        .pulse(0.04f)
                        .attach(leechArmAttachment())
                        .build());

        // Chitinite — paired leg plating attachment example
        register(ItemInit.morphling_chitinite.get(),
                MorphlingVisualMutation.builder(0.55f, 0.42f, 0.08f, 0.36f)
                        .attach(chitiniteLegAttachment())
                        .build());

        // Serpent — paired leg coil attachment example
        register(ItemInit.morphling_serpent.get(),
                MorphlingVisualMutation.builder(0.10f, 0.48f, 0.16f, 0.36f)
                        .swirl(SWIRL_TEX, 0.012f)
                        .attach(serpentLegAttachment())
                        .build());

        // Pests — sickly dark verminous tint, fast flutter pulse
        register(ItemInit.morphling_pests.get(),
                MorphlingVisualMutation.builder(0.20f, 0.22f, 0.04f, 0.36f)
                        .pulse(0.10f)
                        .attach(pestsBodyAttachment())
                        .build());

        // Moth — luminous pale cream, slow breath pulse, full glow
        register(ItemInit.morphling_moth.get(),
                MorphlingVisualMutation.builder(0.91f, 0.86f, 0.78f, 0.32f)
                        .pulse(0.05f).emissive()
                        .attach(mothHeadAttachment())
                        .build());

        // Tick — dark vein crimson, slow throb
        register(ItemInit.morphling_tick.get(),
                MorphlingVisualMutation.builder(0.42f, 0.04f, 0.04f, 0.46f)
                        .pulse(0.03f)
                        .attach(tickBodyAttachment())
                        .build());

        // Urchin — barbed dark brown silhouette
        register(ItemInit.morphling_urchin.get(),
                MorphlingVisualMutation.builder(0.36f, 0.23f, 0.12f, 0.40f)
                        .attach(urchinBodyAttachment())
                        .build());

        // Centipede — cool blue-grey segmented overlay
        register(ItemInit.morphling_centipede.get(),
                MorphlingVisualMutation.builder(0.10f, 0.23f, 0.36f, 0.36f)
                        .attach(centipedeBodyAttachment())
                        .build());

        // Mole — earthy brown earthen tint
        register(ItemInit.morphling_mole.get(),
                MorphlingVisualMutation.builder(0.29f, 0.22f, 0.16f, 0.30f)
                        .attach(moleArmAttachment())
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
    private static final ResourceLocation URCHIN_BODY_ATTACHMENT_TEX =
            Hemomancy.rloc("textures/models/morphling/urchin_body_attachment.png");
    private static final ResourceLocation MOTH_HEAD_ATTACHMENT_TEX =
            Hemomancy.rloc("textures/models/morphling/moth_head_attachment.png");
    private static final ResourceLocation CENTIPEDE_BODY_ATTACHMENT_TEX =
            Hemomancy.rloc("textures/models/morphling/centipede_body_attachment.png");
    private static final ResourceLocation PESTS_BODY_ATTACHMENT_TEX =
            Hemomancy.rloc("textures/models/morphling/pests_body_attachment.png");
    private static final ResourceLocation TICK_BODY_ATTACHMENT_TEX =
            Hemomancy.rloc("textures/models/morphling/tick_body_attachment.png");
    private static final ResourceLocation MOLE_ARM_ATTACHMENT_TEX =
            Hemomancy.rloc("textures/models/morphling/mole_arm_attachment.png");

    private static MorphlingModelAttachment batHeadAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.HEAD, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingBatHeadAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingBatHeadAttachmentModel.LAYER_LOCATION))),
                BAT_HEAD_ATTACHMENT_TEX)
                .visibleFrom(2)
                .growthScale(2, 4, 0.42f, 1.0f, 1.10f);
    }

    private static MorphlingModelAttachment spiderBodyAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.BODY, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingSpiderBodyAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingSpiderBodyAttachmentModel.LAYER_LOCATION))),
                SPIDER_BODY_ATTACHMENT_TEX)
                .visibleFrom(2)
                .growthScale(2, 4, 0.34f, 1.0f, 1.12f);
    }

    private static MorphlingModelAttachment leechArmAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.ARMS, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingLeechArmAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingLeechArmAttachmentModel.LAYER_LOCATION))),
                LEECH_ARM_ATTACHMENT_TEX)
                .visibleFrom(2)
                .growthScale(2, 4, 0.40f, 1.0f, 1.14f);
    }

    private static MorphlingModelAttachment chitiniteLegAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.LEGS, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingChitiniteLegAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingChitiniteLegAttachmentModel.LAYER_LOCATION))),
                CHITINITE_LEG_ATTACHMENT_TEX)
                .visibleFrom(2)
                .growthScale(2, 4, 0.36f, 1.0f, 1.10f);
    }

    private static MorphlingModelAttachment serpentLegAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.LEGS, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingSerpentLegAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingSerpentLegAttachmentModel.LAYER_LOCATION))),
                SERPENT_LEG_ATTACHMENT_TEX)
                .visibleFrom(2)
                .growthScale(2, 4, 0.36f, 1.0f, 1.16f);
    }

    private static MorphlingModelAttachment fungalHeadAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.HEAD, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingFungalHeadModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingFungalHeadModel.LAYER_LOCATION))),
                FUNGAL_HEAD_TEX)
                .visibleFrom(2)
                .growthScale(2, 4, 0.26f, 1.0f, 1.18f)
                .hideAttachedPartAt(5);
    }

    private static MorphlingModelAttachment urchinBodyAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.BODY, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingUrchinBodyAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingUrchinBodyAttachmentModel.LAYER_LOCATION))),
                URCHIN_BODY_ATTACHMENT_TEX)
                .visibleFrom(2)
                .growthScale(2, 4, 0.34f, 1.0f, 1.15f);
    }

    private static MorphlingModelAttachment mothHeadAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.HEAD, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingMothHeadAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingMothHeadAttachmentModel.LAYER_LOCATION))),
                MOTH_HEAD_ATTACHMENT_TEX)
                .visibleFrom(2)
                .growthScale(2, 4, 0.34f, 1.0f, 1.12f);
    }

    private static MorphlingModelAttachment centipedeBodyAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.BODY, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingCentipedeBodyAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingCentipedeBodyAttachmentModel.LAYER_LOCATION))),
                CENTIPEDE_BODY_ATTACHMENT_TEX)
                .visibleFrom(2)
                .growthScale(2, 4, 0.38f, 1.0f, 1.14f);
    }

    private static MorphlingModelAttachment pestsBodyAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.BODY, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingPestsBodyAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingPestsBodyAttachmentModel.LAYER_LOCATION))),
                PESTS_BODY_ATTACHMENT_TEX)
                .visibleFrom(2)
                .growthScale(2, 4, 0.32f, 1.0f, 1.12f);
    }

    private static MorphlingModelAttachment tickBodyAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.BODY, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingTickBodyAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingTickBodyAttachmentModel.LAYER_LOCATION))),
                TICK_BODY_ATTACHMENT_TEX)
                .visibleFrom(2)
                .growthScale(2, 4, 0.30f, 1.0f, 1.16f);
    }

    private static MorphlingModelAttachment moleArmAttachment() {
        return MorphlingModelAttachment.of(AttachmentPoint.ARMS, 0f, 0f, 0f, 1f,
                Lazy.of(() -> new MorphlingMoleArmAttachmentModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                MorphlingMoleArmAttachmentModel.LAYER_LOCATION))),
                MOLE_ARM_ATTACHMENT_TEX)
                .visibleFrom(2)
                .growthScale(2, 4, 0.40f, 1.0f, 1.12f);
    }
}
