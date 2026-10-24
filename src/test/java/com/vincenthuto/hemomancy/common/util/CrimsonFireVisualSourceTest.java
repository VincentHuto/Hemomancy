package com.vincenthuto.hemomancy.common.util;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrimsonFireVisualSourceTest {
	@Test
	void crimsonFireIsWiredFromHelperToRenderer() {
		String helper = SourceAssertions.readMainJava("common/block/harbinger/CrimsonFireHelper.java");
		String packetHandler = SourceAssertions.readMainJava("common/network/PacketHandler.java");
		String packet = SourceAssertions.readMainJava("common/network/particle/EntityVisualPacket.java");
		String clientState = SourceAssertions.readMainJava("client/data/CrimsonFireClientState.java");
		String renderer = SourceAssertions.readMainJava("client/render/CrimsonFireRenderer.java");
		String clientEvents = SourceAssertions.readMainJava("client/event/ClientTickEvents.java")
				+ SourceAssertions.readMainJava("client/event/ScreenOverlayEvents.java");
		String mixin = SourceAssertions.readMainJava("mixin/core/MixinEntityRenderDispatcher.java");
		String mixinConfig = SourceAssertions.read("src/main/resources/hemomancy.mixins.json");
		String crimsonBlock = SourceAssertions.readMainJava("common/block/harbinger/CrimsonFlameBlock.java");
		String ignition = SourceAssertions.readMainJava("common/manipulation/flammeus/SanguineIgnitionManip.java");
		String combustion = SourceAssertions.readMainJava("common/manipulation/flammeus/VitricCombustionManip.java");
		String torch = SourceAssertions.readMainJava("common/item/harbinger/tool/living/LivingTorchItem.java");

		assertContains("helper class exists", helper, "class CrimsonFireHelper");
		assertContains("helper exposes crimson ignite", helper, "igniteCrimson(Entity target, float seconds)");
		assertContains("helper uses vanilla fire behavior", helper, "target.igniteForSeconds(seconds)");
		assertContains("helper exposes visual-only marker", helper, "markCrimson(Entity target, int durationTicks)");
		assertContains("helper can mirror current burn duration", helper,
				"markCrimsonForRemainingFire(Entity target, int minimumTicks)");
		assertContains("helper reads actual remaining fire", helper, "target.getRemainingFireTicks()");
		assertContains("helper syncs tracking clients", helper, "PacketHandler.sendCrimsonFireVisual(living, durationTicks)");

		assertContains("packet registered", packetHandler, "EntityVisualPacket.TYPE");
		assertContains("helper sends the crimson fire kind", packetHandler, "EntityVisualPacket.Kind.CRIMSON_FIRE");
		assertContains("packet helper exists", packetHandler, "sendCrimsonFireVisual(LivingEntity target, int durationTicks)");
		assertContains("packet tracks entity and self", packetHandler, "sendTracking(target");
		assertContains("packet writes entity id", packet, "buffer.writeVarInt(packet.entityId())");
		assertContains("packet writes duration", packet, "buffer.writeVarInt(packet.durationTicks())");
		assertContains("packet updates client state",
				SourceAssertions.readMainJava("client/network/ClientboundEffectHandlers.java"),
				"CrimsonFireClientState.markActive(packet.entityId(), packet.durationTicks())");

		assertContains("client state stores ids", clientState, "ACTIVE_UNTIL_BY_ENTITY_ID");
		assertContains("client state requires actual fire", clientState, "entity.isOnFire()");
		assertContains("client state expires entries", clientState, "ACTIVE_UNTIL_BY_ENTITY_ID.entrySet().removeIf");
		assertContains("client event ticks state", clientEvents, "CrimsonFireClientState.tick()");
		assertContains("client event handles fire overlay", clientEvents, "onRenderBlockScreenEffect");
		assertContains("client event checks fire overlay", clientEvents, "RenderBlockScreenEffectEvent.OverlayType.FIRE");

		assertContains("renderer uses crimson flame atlas sprite", renderer, "Hemomancy.rloc(\"block/crimson_flames\")");
		assertContains("renderer has screen overlay method", renderer, "renderScreenOverlay");
		assertContains("renderer has entity flame method", renderer, "renderEntityFlames");
		assertContains("renderer uses cutout block sheet", renderer, "Sheets.cutoutBlockSheet()");

		assertContains("mixin config registers dispatcher mixin", mixinConfig, "\"MixinEntityRenderDispatcher\"");
		assertContains("mixin targets entity dispatcher", mixin, "EntityRenderDispatcher");
		assertContains("mixin draws crimson entity fire", mixin, "CrimsonFireRenderer.renderEntityFlames");
		assertContains("mixin suppresses vanilla fire", mixin, "hemomancy$suppressVanillaFireForCrimson");
		assertContains("mixin checks crimson state", mixin, "CrimsonFireClientState.isActive(entity)");

		assertContains("crimson block marks visual state", crimsonBlock,
				"CrimsonFireHelper.markCrimsonForRemainingFire(entity, 10)");
		int vanillaFire = crimsonBlock.indexOf("super.entityInside(state, level, pos, entity);");
		int crimsonMark = crimsonBlock.indexOf("CrimsonFireHelper.markCrimsonForRemainingFire(entity, 10);");
		assertTrue(vanillaFire >= 0 && vanillaFire < crimsonMark, "crimson block marks after vanilla fire");
		assertContains("sanguine ignition captures school state", ignition, "SchoolDamage.cast");
		assertNotContains("ignition uses Searing instead of vanilla burn", ignition, "igniteCrimson");
		assertContains("vitric combustion captures its payoff school", combustion, "SchoolDamage.cast");
		assertNotContains("combustion consumes heat", combustion, "igniteCrimson");
		assertContains("living torch uses school burn", torch, "HemoDamageTypes.livingTorchBreath");
	}
}
