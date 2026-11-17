package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.UUID;

/**
 * PT-67 on a world that has the Chamber of Will (the GameTest server has none): the Chamber rite's finale check lets a
 * Degree 6 caster through, and its effect attunes them and starts a real visit in the Chamber. Each check logs
 * {@code CHAMBER_RITE_REVIEW PASS|FAIL}; the client stops after the visit returns.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ChamberRiteClientReview {
	private static boolean started;
	private static UUID observer;
	private static int ticks;
	private static int failures;

	private ChamberRiteClientReview() {
	}

	@SubscribeEvent
	public static void client(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.chamberRiteReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		var server = mc.getSingleplayerServer();
		if (!started) {
			started = true;
			observer = mc.player.getUUID();
			mc.options.pauseOnLostFocus = false;
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(observer);
				check("the Chamber of Will is loaded", server.getLevel(ChamberOfWillManager.CHAMBER_OF_WILL) != null);
				HemoCapabilityAccess.getBloodVolume(player).setActive(true);
				HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(6);
				check("the rite's finale check lets a Degree 6 caster through", ChamberVisitService.riteRefusal(player) == null);
				check("the rite's effect starts a visit", ChamberVisitService.beginRiteVisit(player));
				check("the caster is attuned", HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_CHAMBER_RITE_ATTUNED));
			});
			return;
		}
		ticks++;
		if (ticks == 80) {
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(observer);
				check("the visit is active", ChamberVisitService.isActive(player));
				check("the caster stands in the Chamber",
						player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL));
				check("a second visit is refused while this one runs", ChamberVisitService.riteRefusal(player) != null);
			});
		}
		if (ticks == 100) Screenshot.grab(mc.gameDirectory, "chamber-rite-visit.png", mc.getMainRenderTarget(), message -> {});
		if (ticks == 120) server.execute(() -> ChamberVisitService.returnFromVisit(server.getPlayerList().getPlayer(observer)));
		if (ticks == 200) {
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(observer);
				check("the visit returns the caster", !ChamberVisitService.isActive(player)
						&& player.level().dimension().equals(Level.OVERWORLD));
				Hemomancy.LOGGER.info("CHAMBER_RITE_REVIEW finished with {} failure(s)", failures);
			});
		}
		if (ticks == 230) mc.stop();
	}

	private static void check(String what, boolean passed) {
		if (!passed) failures++;
		Hemomancy.LOGGER.info("CHAMBER_RITE_REVIEW {} {}", passed ? "PASS" : "FAIL", what);
	}
}
