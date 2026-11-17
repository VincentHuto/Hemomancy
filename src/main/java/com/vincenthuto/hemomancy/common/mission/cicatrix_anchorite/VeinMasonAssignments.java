package com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumBloodFlow;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public final class VeinMasonAssignments {
    private static final String FIRST_ROUTE_BUILD = "hemomancy:vein_mason_first_route_build";
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

    private VeinMasonAssignments() {}

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

    /** PT-22: a section left Varicose by a Thelemic cast counts once, even when it was Varicose before the cast. */
    public static boolean strainCounts(EnumBloodFlow after) { return after == EnumBloodFlow.VARICOSE; }

    /** PT-22: any section already Varicose counts once. A Dead section does not. */
    public static boolean strainCounts(Collection<EnumBloodFlow> sections) {
        return sections.stream().anyMatch(VeinMasonAssignments::strainCounts);
    }

    public static void onThelemicStrain(ServerPlayer player, EnumBloodFlow after) {
        if (mayRecordStrain(player) && (strainCounts(after) || strainCounts(sectionFlows(player)))) {
            grant(player, D5_VARICOSE);
        }
    }

    /** PT-22: the Mason's fifth-Degree tree opening counts a section that is already Varicose. */
    public static void onD5TreeOpened(ServerPlayer player) {
        if (mayRecordStrain(player) && strainCounts(sectionFlows(player))) grant(player, D5_VARICOSE);
    }

    /**
     * PT-29 (NQ-10-11): on a server with {@code vascularDegradationOnManipEnabled} off, no cast strains a vein, so the
     * first Thelemic Memory cast after the fourth-Degree reward counts as the strain.
     */
    public static void onThelemicCastWithoutDegradation(ServerPlayer player) {
        if (mayRecordStrain(player)) grant(player, D5_VARICOSE);
    }

    private static boolean mayRecordStrain(ServerPlayer player) {
        return canStartD5(HemoCapabilityAccess.getPlayerDegreeNumber(player),
                HarbingerAdvancementGranter.isVeinMasonRewardClaimed(player)) && !has(player, D5_VARICOSE);
    }

    private static List<EnumBloodFlow> sectionFlows(ServerPlayer player) {
        var vascular = HemoCapabilityAccess.getVascularSystem(player);
        return Arrays.stream(EnumVeinSections.values()).map(vascular::getBloodFlowBySection).toList();
    }

    /** PT-14: the pattern the Mason gave for {@code slot}, stored when he gave it. */
    public static Optional<ResourceLocation> issuedLesson(ServerPlayer player, String slot) {
        return HemoCapabilityAccess.issuedScarLessons(player).get(slot);
    }

    public static void recordIssuedLesson(ServerPlayer player, String slot, ResourceLocation patternScarId) {
        HemoCapabilityAccess.issuedScarLessons(player).record(slot, patternScarId);
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
        if (step == RoutingStep.NONE || has(player, D6_SECOND_ROUTE)) return;
        String build = HemoCapabilityAccess.getScarState(player).getActiveCerebralScars().stream().map(ResourceLocation::toString).sorted()
                        .collect(java.util.stream.Collectors.joining(";"));
        if (build.isEmpty()) return;
        CompoundTag durable = durable(player);
        if (step == RoutingStep.FIRST || !durable.contains(FIRST_ROUTE_BUILD)) {
            durable.putString(FIRST_ROUTE_BUILD, build);
            saveDurable(player, durable);
            grant(player, D6_FIRST_ROUTE);
            if (step != RoutingStep.FIRST) player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.hemomancy.vein_mason_assignments.your_current_scar_route_is_recorded"), false);
            return;
        }
        if (step == RoutingStep.SECOND) {
            if (build.equals(durable.getString(FIRST_ROUTE_BUILD))) return;
            grant(player, D6_SECOND_ROUTE);
            grant(player, D6_READY);
        }
    }

    public static void onChangedLoadout(ServerPlayer player, Collection<ResourceLocation> before,
            Collection<ResourceLocation> after) {
        if (has(player, D6_FIRST_ROUTE) && changedLoadout(before, after)) grant(player, D6_LOADOUT);
    }

    public static void refreshD5(ServerPlayer player) {
        boolean fortified = HemoCapabilityAccess.getInitiatoryDegree(player).hasHematicFortification();
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

    private static CompoundTag durable(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static void saveDurable(ServerPlayer player, CompoundTag durable) {
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, durable);
    }

    private static ResourceLocation id(String path) {
        return Hemomancy.rloc("hemomancy/" + path);
    }

    public enum RoutingStep { NONE, FIRST, SECOND }
}
