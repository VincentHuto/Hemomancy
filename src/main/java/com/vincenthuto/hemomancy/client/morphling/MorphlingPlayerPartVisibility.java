package com.vincenthuto.hemomancy.client.morphling;

import com.vincenthuto.hemomancy.client.morphling.MorphlingModelAttachment.AttachmentPoint;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class MorphlingPlayerPartVisibility {
    private static final Map<PlayerModel<?>, VisibilitySnapshot> SNAPSHOTS = new IdentityHashMap<>();
    private static final ThreadLocal<Set<AttachmentPoint>> ACTIVE_HIDDEN_PARTS =
            ThreadLocal.withInitial(Set::of);

    private MorphlingPlayerPartVisibility() {
    }

    public static void apply(Player player, PlayerRenderer renderer) {
        restore(renderer);

        boolean hasMycophantTendril = hasMycophantTendril(player);
        if (!HemoClientConfig.RENDER_MORPHLING_MUTATION_LAYER.get() && !hasMycophantTendril) {
            return;
        }

        Set<AttachmentPoint> hiddenParts = hiddenPartsFor(player);
        if (hiddenParts.isEmpty()) {
            return;
        }

        ACTIVE_HIDDEN_PARTS.set(hiddenParts);
        PlayerModel<?> model = renderer.getModel();
        SNAPSHOTS.put(model, VisibilitySnapshot.capture(model));
        hiddenParts.forEach(part -> setVisible(model, part, false));
    }

    public static void restore(PlayerRenderer renderer) {
        PlayerModel<?> model = renderer.getModel();
        VisibilitySnapshot snapshot = SNAPSHOTS.remove(model);
        if (snapshot != null) {
            snapshot.restore(model);
        }
        ACTIVE_HIDDEN_PARTS.remove();
    }

    public static void applyToArmorModel(HumanoidModel<?> model, EquipmentSlot slot) {
        Set<AttachmentPoint> hiddenParts = ACTIVE_HIDDEN_PARTS.get();
        if (hiddenParts.isEmpty()) {
            return;
        }

        switch (slot) {
            case HEAD -> {
                if (hiddenParts.contains(AttachmentPoint.HEAD)) {
                    model.head.visible = false;
                    model.hat.visible = false;
                }
            }
            case CHEST -> {
                if (hiddenParts.contains(AttachmentPoint.BODY)) {
                    model.body.visible = false;
                }
                if (hiddenParts.contains(AttachmentPoint.ARMS)) {
                    model.rightArm.visible = false;
                    model.leftArm.visible = false;
                }
            }
            case LEGS -> {
                if (hiddenParts.contains(AttachmentPoint.BODY)) {
                    model.body.visible = false;
                }
                if (hiddenParts.contains(AttachmentPoint.LEGS)) {
                    model.rightLeg.visible = false;
                    model.leftLeg.visible = false;
                }
            }
            case FEET -> {
                if (hiddenParts.contains(AttachmentPoint.LEGS)) {
                    model.rightLeg.visible = false;
                    model.leftLeg.visible = false;
                }
            }
            default -> {
            }
        }
    }

    public static boolean shouldHideHeldItem(HumanoidArm arm) {
        return ACTIVE_HIDDEN_PARTS.get().contains(AttachmentPoint.ARMS);
    }

    public static boolean isHeadHidden() {
        return ACTIVE_HIDDEN_PARTS.get().contains(AttachmentPoint.HEAD);
    }

    private static Set<AttachmentPoint> hiddenPartsFor(Player player) {
        if (hasMycophantTendril(player)) {
            return Set.of(AttachmentPoint.HEAD);
        }

        return HemoCapabilityAccess.getEquippedMorphling(player)
                .map(cap -> cap.getEquippedMorphling())
                .map(stack -> {
                    MorphlingVisualMutation mutation = MorphlingMutationRegistry.get(stack);
                    if (mutation == null || mutation.modelAttachment == null) {
                        return Set.<AttachmentPoint>of();
                    }
                    int maturity = MorphlingItem.getMaturityLevel(stack);
                    return mutation.modelAttachment.hiddenPlayerParts(maturity);
                })
                .orElse(Set.of());
    }

    private static boolean hasMycophantTendril(Player player) {
        return HemoCapabilityAccess.getScars(player)
                .map(inv -> inv.getStackInSlot(HarbingerEquipmentMenu.CHARM_SLOT_INDEX).is(ItemInit.mycophant_tendril.get()))
                .orElse(false);
    }

    private static void setVisible(PlayerModel<?> model, AttachmentPoint part, boolean visible) {
        switch (part) {
            case HEAD -> {
                model.head.visible = visible;
                model.hat.visible = visible;
            }
            case BODY -> {
                model.body.visible = visible;
                model.jacket.visible = visible;
            }
            case ARMS -> {
                model.rightArm.visible = visible;
                model.rightSleeve.visible = visible;
                model.leftArm.visible = visible;
                model.leftSleeve.visible = visible;
            }
            case LEGS -> {
                model.rightLeg.visible = visible;
                model.rightPants.visible = visible;
                model.leftLeg.visible = visible;
                model.leftPants.visible = visible;
            }
        }
    }

    private record VisibilitySnapshot(
            boolean head,
            boolean hat,
            boolean body,
            boolean jacket,
            boolean rightArm,
            boolean rightSleeve,
            boolean leftArm,
            boolean leftSleeve,
            boolean rightLeg,
            boolean rightPants,
            boolean leftLeg,
            boolean leftPants) {

        static VisibilitySnapshot capture(PlayerModel<?> model) {
            return new VisibilitySnapshot(
                    model.head.visible,
                    model.hat.visible,
                    model.body.visible,
                    model.jacket.visible,
                    model.rightArm.visible,
                    model.rightSleeve.visible,
                    model.leftArm.visible,
                    model.leftSleeve.visible,
                    model.rightLeg.visible,
                    model.rightPants.visible,
                    model.leftLeg.visible,
                    model.leftPants.visible);
        }

        void restore(PlayerModel<?> model) {
            model.head.visible = head;
            model.hat.visible = hat;
            model.body.visible = body;
            model.jacket.visible = jacket;
            model.rightArm.visible = rightArm;
            model.rightSleeve.visible = rightSleeve;
            model.leftArm.visible = leftArm;
            model.leftSleeve.visible = leftSleeve;
            model.rightLeg.visible = rightLeg;
            model.rightPants.visible = rightPants;
            model.leftLeg.visible = leftLeg;
            model.leftPants.visible = leftPants;
        }
    }
}
