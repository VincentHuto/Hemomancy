package com.vincenthuto.hemomancy.client.morphling;

import com.vincenthuto.hemomancy.client.morphling.MorphlingModelAttachment.AttachmentPoint;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class MorphlingPlayerPartVisibility {
    private static final Map<PlayerModel<?>, VisibilitySnapshot> SNAPSHOTS = new IdentityHashMap<>();

    private MorphlingPlayerPartVisibility() {
    }

    public static void apply(Player player, PlayerRenderer renderer) {
        restore(renderer);

        if (!HemoClientConfig.RENDER_MORPHLING_MUTATION_LAYER.get()) {
            return;
        }

        Set<AttachmentPoint> hiddenParts = hiddenPartsFor(player);
        if (hiddenParts.isEmpty()) {
            return;
        }

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
    }

    private static Set<AttachmentPoint> hiddenPartsFor(Player player) {
        return HemoCapabilityAccess.getEquippedMorphling(player)
                .map(cap -> cap.getEquippedMorphling())
                .map(MorphlingMutationRegistry::get)
                .map(mutation -> mutation.modelAttachment)
                .map(MorphlingModelAttachment::hiddenPlayerParts)
                .orElse(Set.of());
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
            case RIGHT_ARM -> {
                model.rightArm.visible = visible;
                model.rightSleeve.visible = visible;
            }
            case LEFT_ARM -> {
                model.leftArm.visible = visible;
                model.leftSleeve.visible = visible;
            }
            case RIGHT_LEG -> {
                model.rightLeg.visible = visible;
                model.rightPants.visible = visible;
            }
            case LEFT_LEG -> {
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
