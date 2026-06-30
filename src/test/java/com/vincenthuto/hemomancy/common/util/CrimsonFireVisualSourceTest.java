package com.vincenthuto.hemomancy.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CrimsonFireVisualSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private CrimsonFireVisualSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String helper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/util/CrimsonFireHelper.java"));
		String packetHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/PacketHandler.java"));
		String packet = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/SyncCrimsonFireVisualS2CPacket.java"));
		String clientState = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/data/CrimsonFireClientState.java"));
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/CrimsonFireRenderer.java"));
		String clientEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String mixin = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/mixin/core/MixinEntityRenderDispatcher.java"));
		String mixinConfig = read(RESOURCE_ROOT.resolve("hemomancy.mixins.json"));
		String crimsonBlock = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/CrimsonFlameBlock.java"));
		String ignition = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/manipulation/flammeus/SanguineIgnitionManip.java"));
		String combustion = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/manipulation/flammeus/VitricCombustionManip.java"));
		String torch = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingTorchItem.java"));

		assertContains("helper class exists", helper, "class CrimsonFireHelper");
		assertContains("helper exposes crimson ignite", helper, "igniteCrimson(Entity target, float seconds)");
		assertContains("helper uses vanilla fire behavior", helper, "target.igniteForSeconds(seconds)");
		assertContains("helper exposes visual-only marker", helper, "markCrimson(Entity target, int durationTicks)");
		assertContains("helper can mirror current burn duration", helper,
				"markCrimsonForRemainingFire(Entity target, int minimumTicks)");
		assertContains("helper reads actual remaining fire", helper, "target.getRemainingFireTicks()");
		assertContains("helper syncs tracking clients", helper, "PacketHandler.sendCrimsonFireVisual(living, durationTicks)");

		assertContains("packet registered", packetHandler, "SyncCrimsonFireVisualS2CPacket.TYPE");
		assertContains("packet helper exists", packetHandler,
				"sendCrimsonFireVisual(LivingEntity target, int durationTicks)");
		assertContains("packet tracks entity and self", packetHandler,
				"PacketDistributor.sendToPlayersTrackingEntityAndSelf(target");
		assertContains("packet writes entity id", packet, "buffer.writeVarInt(packet.entityId())");
		assertContains("packet writes duration", packet, "buffer.writeVarInt(packet.durationTicks())");
		assertContains("packet updates client state", packet,
				"CrimsonFireClientState.markActive(packet.entityId(), packet.durationTicks())");

		assertContains("client state stores ids", clientState, "ACTIVE_UNTIL_BY_ENTITY_ID");
		assertContains("client state requires actual fire", clientState, "entity.isOnFire()");
		assertContains("client state expires entries", clientState,
				"ACTIVE_UNTIL_BY_ENTITY_ID.entrySet().removeIf");
		assertContains("client event ticks state", clientEvents, "CrimsonFireClientState.tick()");
		assertContains("client event handles fire overlay", clientEvents, "onRenderBlockScreenEffect");
		assertContains("client event checks fire overlay", clientEvents,
				"RenderBlockScreenEffectEvent.OverlayType.FIRE");

		assertContains("renderer uses crimson flame atlas sprite", renderer, "hemomancy:block/crimson_flames");
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
		assertOrder("crimson block marks after vanilla fire", crimsonBlock,
				"super.entityInside(state, level, pos, entity);",
				"CrimsonFireHelper.markCrimsonForRemainingFire(entity, 10);");
		assertContains("sanguine ignition uses crimson helper", ignition,
				"CrimsonFireHelper.igniteCrimson(target, FIRE_SECONDS)");
		assertContains("vitric combustion uses crimson helper", combustion,
				"CrimsonFireHelper.igniteCrimson(target, FIRE_SECONDS)");
		assertContains("living torch uses crimson helper", torch,
				"CrimsonFireHelper.igniteCrimson(target, 4)");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertOrder(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex >= secondIndex) {
			throw new AssertionError(label + ": expected " + first + " before " + second);
		}
	}
}
