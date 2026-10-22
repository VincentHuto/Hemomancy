package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.SynapticLoadoutLockout;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneBoundaryRelation;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneEvents;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodlineChatRateLimit;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** S-8: {@code TransientServerStateEvents} ends a player's session state when the player logs out. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SessionStateGameTests {
	private SessionStateGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "session_state", timeoutTicks = 20)
	public static void logoutDropsThePlayersSessionEntries(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			var player = captured.player;
			var id = player.getUUID();
			long now = helper.getLevel().getGameTime();
			for (int i = 0; i < BloodlineChatRateLimit.MESSAGES; i++) BloodlineChatRateLimit.tryAcquire(id, now);
			FoundingFaneEvents.setPreviewRelation(player, FaneBoundaryRelation.MEMBER);
			SynapticLoadoutLockout.start(id, now);
			helper.assertTrue(!BloodlineChatRateLimit.tryAcquire(id, now), "The chat limit must be full before logout");
			helper.assertTrue(FoundingFaneEvents.previewRelation(player).isPresent(), "The preview must be set before logout");
			helper.assertTrue(SynapticLoadoutLockout.remaining(id, now) > 0, "The lockout must run before logout");

			NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(player));

			helper.assertTrue(BloodlineChatRateLimit.tryAcquire(id, now), "Logout must forget the sender's chat window");
			helper.assertTrue(FoundingFaneEvents.previewRelation(player).isEmpty(), "Logout must drop the fane preview");
			helper.assertTrue(SynapticLoadoutLockout.remaining(id, now) == 0, "Logout must end the apply lockout");
			helper.succeed();
		}
	}
}
