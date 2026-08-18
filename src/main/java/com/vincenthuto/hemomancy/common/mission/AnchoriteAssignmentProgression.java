package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumBloodFlow;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.HashSet;

public final class AnchoriteAssignmentProgression {
    public static final ResourceLocation D5_VARICOSE = id("vein_mason_d5_varicose");
    public static final ResourceLocation D5_DIAGNOSED = id("vein_mason_d5_diagnosed");
    public static final ResourceLocation D5_TREATED = id("vein_mason_d5_treated");
    public static final ResourceLocation D5_READY = id("vein_mason_d5_ready");
    public static final ResourceLocation D5_REWARD = id("vein_mason_d5_reward_claimed");
    public static final ResourceLocation D6_REFERRAL = id("vein_mason_d6_referral");
    public static final ResourceLocation D6_COUNSEL = id("vein_mason_d6_counsel");
    public static final ResourceLocation D6_FIRST_ROUTE = id("vein_mason_d6_first_route");
    public static final ResourceLocation D6_LOADOUT = id("vein_mason_d6_loadout_changed");
    public static final ResourceLocation D6_SECOND_ROUTE = id("vein_mason_d6_second_route");
    public static final ResourceLocation D6_READY = id("vein_mason_d6_ready");
    public static final ResourceLocation D6_REWARD = id("vein_mason_d6_reward_claimed");

    private AnchoriteAssignmentProgression() {}

    public static boolean canStartD5(int degree, boolean d4Reward) { return degree >= 5 && d4Reward; }
    public static boolean canStartD6(int degree, boolean d5Reward) { return degree >= 6 && d5Reward; }

    public static boolean changedLoadout(Collection<ResourceLocation> before, Collection<ResourceLocation> after) {
        return !new HashSet<>(before).equals(new HashSet<>(after));
    }

    public static RoutingStep nextRoutingStep(boolean counsel, boolean firstRoute, boolean changedLoadout) {
        if (!counsel) return RoutingStep.NONE;
        if (!firstRoute) return RoutingStep.FIRST;
        return changedLoadout ? RoutingStep.SECOND : RoutingStep.NONE;
    }

    public static void onThelemicStrain(ServerPlayer player, EnumBloodFlow before, EnumBloodFlow after) {
        if (canStartD5(HemoCapabilityAccess.getPlayerDegreeNumber(player),
                HarbingerAdvancementGranter.isVeinMasonRewardClaimed(player))
                && before != EnumBloodFlow.VARICOSE && after == EnumBloodFlow.VARICOSE) {
            grant(player, D5_VARICOSE);
        }
    }

    public static void onDiagnosis(ServerPlayer player) {
        if (has(player, D5_VARICOSE)) grant(player, D5_DIAGNOSED);
        refreshD5(player);
    }

    public static void onTreatment(ServerPlayer player) {
        if (has(player, D5_DIAGNOSED)) grant(player, D5_TREATED);
        refreshD5(player);
    }

    public static void onFortification(ServerPlayer player) { refreshD5(player); }

    public static void onReferral(ServerPlayer player) {
        if (canStartD6(HemoCapabilityAccess.getPlayerDegreeNumber(player), has(player, D5_REWARD))) grant(player, D6_REFERRAL);
    }

    public static void onCounsel(ServerPlayer player) {
        if (has(player, D6_REFERRAL)) grant(player, D6_COUNSEL);
    }

    public static void onMatchingNoeticCast(ServerPlayer player) {
        RoutingStep step = nextRoutingStep(has(player, D6_COUNSEL), has(player, D6_FIRST_ROUTE), has(player, D6_LOADOUT));
        if (step == RoutingStep.FIRST) grant(player, D6_FIRST_ROUTE);
        if (step == RoutingStep.SECOND) {
            grant(player, D6_SECOND_ROUTE);
            grant(player, D6_READY);
        }
    }

    public static void onChangedLoadout(ServerPlayer player, Collection<ResourceLocation> before,
            Collection<ResourceLocation> after) {
        if (has(player, D6_FIRST_ROUTE) && changedLoadout(before, after)) grant(player, D6_LOADOUT);
    }

    public static void refreshD5(ServerPlayer player) {
        boolean fortified = HemoCapabilityAccess.getInitiatoryDegree(player)
                .map(degree -> degree.hasHematicFortification()).orElse(false);
        if (has(player, D5_VARICOSE) && has(player, D5_DIAGNOSED) && has(player, D5_TREATED) && fortified) {
            grant(player, D5_READY);
        }
    }

    public static boolean has(ServerPlayer player, ResourceLocation advancement) {
        return HarbingerAdvancementGranter.hasAdvancement(player, advancement);
    }

    public static void grant(ServerPlayer player, ResourceLocation advancement) {
        HarbingerAdvancementGranter.grantIfNotDone(player, advancement);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("hemomancy", "hemomancy/" + path);
    }

    public enum RoutingStep { NONE, FIRST, SECOND }
}
