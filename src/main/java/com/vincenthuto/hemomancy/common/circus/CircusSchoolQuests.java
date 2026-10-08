package com.vincenthuto.hemomancy.common.circus;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;

public final class CircusSchoolQuests {
    private CircusSchoolQuests() {}
    public static String nextStep(ServerPlayer player, String role) {
        var lesson = CircusCurriculum.forTeacher(role).orElse(null);
        if (lesson == null) return "preparation";
        if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < lesson.degree()) return "degree";
        if (!CircusApprenticeshipProgress.instructionSatisfied(player, lesson.summon())) return "lesson";
        if (!CircusApprenticeshipProgress.knows(player, lesson.summon())) return "ordeal";
        return CircusApprenticeshipProgress.practicalComplete(player, lesson.summon()) ? "mastered" : "practical";
    }
    public static boolean inspect(ServerPlayer player) {
        if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < 3) return false;
        for (ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (stack.getItem() instanceof MarionetteCrossbarItem && MarionetteCrossbarItem.validateControl(stack, player, false)
                    && MarionetteCrossbarItem.getThread(stack) > 0) {
                var state = CircusApprenticeshipProgress.state(player);
                UUID id = MarionetteCrossbarItem.ensureCrossbarId(stack);
                if (!state.hasUUID("primer.crossbar") || !id.equals(state.getUUID("primer.crossbar"))) {
                    state.remove("primer.call"); state.remove("primer.command"); state.remove("primer.recall");
                }
                state.putUUID("primer.crossbar", id);
                player.displayClientMessage(Component.translatable("hemomancy.circus.school.inspected"), false);
                return true;
            }
        }
        player.displayClientMessage(Component.translatable("hemomancy.circus.school.inspect_failed"), false);
        return false;
    }
    public static void observeControl(ServerPlayer player, UUID crossbar, String action, String summon) {
        var state = CircusApprenticeshipProgress.state(player);
        if (state.hasUUID("primer.crossbar") && state.getUUID("primer.crossbar").equals(crossbar)
                && CircusApprenticeshipProgress.knows(player, summon)) {
            if (action.equals("call")) { state.putString("primer.shape", summon); state.putBoolean("primer.call", true); }
            else if (summon.equals(state.getString("primer.shape")) && state.getBoolean("primer.call")) {
                if (action.equals("command") && MarionetteCrossbarItem.activeSummonsForOwner(player).stream().anyMatch(body ->
                        body instanceof com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon bound
                        && crossbar.equals(bound.hemomancy$getCrossbarUUID()) && summon.equals(bound.hemomancy$getSummonName()))) state.putBoolean("primer.command", true);
                if (action.equals("recall") && state.getBoolean("primer.command")) state.putBoolean("primer.recall", true);
            }
        }
        if (action.equals("recall")) CircusPracticalController.recall(player, crossbar, summon);
    }
    public static void testimony(ServerPlayer player, String role) {
        CircusApprenticeshipProgress.state(player).putBoolean("testimony." + role, true);
    }
    public static boolean report(ServerPlayer player) {
        var state = CircusApprenticeshipProgress.state(player);
        boolean delivered = false;
        if (state.getBoolean("primer.call") && state.getBoolean("primer.command") && state.getBoolean("primer.recall"))
            delivered |= finish(player, "empty_ring", 100);
        if (HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 4 && state.getBoolean("testimony.circus_stilt_walker")
                && state.getBoolean("testimony.circus_knife_thrower")
                && CircusApprenticeshipProgress.practicalComplete(player, "scarlet_mummer")
                && CircusApprenticeshipProgress.practicalComplete(player, "marrow_spitter")) delivered |= finish(player, "wrong_audience", 150);
        if (state.getBoolean("clue.stage") && state.getBoolean("clue.loft") && state.getBoolean("clue.quarters")
                && state.getBoolean("testimony.circus_understudy") && state.getBoolean("clue.carousel"))
            delivered |= finish(player, "missing_understudy", 150);
        player.displayClientMessage(Component.translatable("hemomancy.circus.school." + (delivered ? "report_complete" : "report_incomplete")), false);
        return delivered;
    }
    private static boolean finish(ServerPlayer player, String quest, int points) {
        var state = CircusApprenticeshipProgress.state(player);
        if (state.getBoolean("quest." + quest)) return false;
        state.putBoolean("quest." + quest, true);
        CircusPlayerProgress.awardMilestone(player, quest, points);
        return true;
    }
    public static boolean guestEligible(ServerPlayer player) {
        var state = CircusApprenticeshipProgress.state(player);
        return CircusSchoolRules.canEarnGuestReward(player.serverLevel().getDayTime() / 24000,
                state.contains("guest.day") ? state.getLong("guest.day") : -1, state.getInt("guest.pending"));
    }
    public static void guestReward(ServerPlayer player) {
        if (!guestEligible(player)) return;
        var state = CircusApprenticeshipProgress.state(player);
        state.putLong("guest.day", player.serverLevel().getDayTime() / 24000);
        state.putInt("guest.pending", 4);
        claim(player);
    }
    public static boolean claim(ServerPlayer player) {
        var state = CircusApprenticeshipProgress.state(player);
        int pending = state.getInt("guest.pending");
        if (pending <= 0) return false;
        // Slot insertion preserves exact overflow even for creative players with a full inventory.
        for (int slot = 0; slot < player.getInventory().items.size() && pending > 0; slot++) {
            ItemStack held = player.getInventory().items.get(slot);
            if (held.isEmpty()) { player.getInventory().items.set(slot, new ItemStack(ItemInit.puppeteering_thread.get(), pending)); pending = 0; }
            else if (held.is(ItemInit.puppeteering_thread.get())) {
                int inserted = Math.min(pending, Math.max(0, held.getMaxStackSize() - held.getCount()));
                held.grow(inserted); pending -= inserted;
            }
        }
        state.putInt("guest.pending", pending);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.displayClientMessage(Component.translatable("hemomancy.circus.school.reward", pending), false);
        return pending == 0;
    }
}
