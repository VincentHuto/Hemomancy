package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SilentSlippingNoClipSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path DOCS_ROOT = Path.of("docs");

	private SilentSlippingNoClipSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String handler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/SilentArchonArmorAbilityHandler.java"));
		String packetHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/PacketHandler.java"));
		String shardPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/particle/SpawnMonolithShatterBurstPacket.java"));
		String shardRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/world/SanguineMonolithShatterRenderer.java"));
		String entityMixin = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/mixin/core/MixinEntity.java"));
		String localPlayerMixin = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/mixin/core/MixinLocalPlayer.java"));
		String mixins = read(RESOURCE_ROOT.resolve("hemomancy.mixins.json"));
		String docs = read(DOCS_ROOT.resolve("HEMOMANCY_REFERENCE.md"));

		assertContains("handler exposes unified no-clip active query", handler,
				"isSilentSlippingNoClipActive(Player player)");
		assertContains("handler checks client sync timer", handler, "CLIENT_SILENT_SLIPPING_UNTIL_KEY");
		assertContains("handler checks server state", handler, "player instanceof ServerPlayer serverPlayer");
		assertContains("handler exposes no-clip applier", handler, "applySilentSlippingNoClip(Player player)");
		assertContains("handler no-clip applier sets noPhysics", handler, "player.noPhysics = true");
		assertContains("server movement uses no-clip applier", handler, "applySilentSlippingNoClip(player);");
		assertContains("handler tracks solid slipping state", handler, "SILENT_SLIPPING_INSIDE_SOLID_KEY");
		assertContains("handler updates solid transition pulse", handler, "updateSilentSlippingSolidTransition(player)");
		assertContains("handler detects solid entry and exit", handler, "wasInsideSolid != insideSolid");
		assertContains("handler sends slipping shard pulse", handler, "sendSilentSlippingShardPulse(player)");
		assertContains("handler uses shard-only monolith visual", handler, "PacketHandler.sendMonolithShardPulse");
		assertDoesNotContain("silent slipping no longer emits direct portal particles", handler,
				"player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL,\n\t\t\t\tplayer.getX()");

		assertContains("packet handler exposes shard-only helper", packetHandler, "sendMonolithShardPulse");
		assertContains("shard helper sends shard-only packet", packetHandler,
				"new SpawnMonolithShatterBurstPacket(pos, true)");
		assertContains("shatter packet has shard-only mode", shardPacket, "boolean shardOnly");
		assertContains("shatter packet encodes shard-only mode", shardPacket, "buf.writeBoolean(msg.isShardOnly())");
		assertContains("shatter packet decodes shard-only mode", shardPacket, "buf.readBoolean()");
		assertContains("shatter packet routes shard-only renderer", shardPacket, "spawnShardPulse");
		assertContains("renderer exposes shard-only pulse", shardRenderer, "spawnShardPulse");
		assertContains("renderer uses smaller shard-only count", shardRenderer, "SHARD_PULSE_COUNT");

		assertContains("entity mixin targets base entity movement", entityMixin, "@Mixin(Entity.class)");
		assertContains("entity mixin hooks move", entityMixin, "method = \"move\"");
		assertContains("entity mixin runs before collision resolution", entityMixin, "at = @At(\"HEAD\")");
		assertContains("entity mixin only handles players", entityMixin, "instanceof Player player");
		assertContains("entity mixin reinforces silent slip no-clip", entityMixin,
				"SilentArchonArmorAbilityHandler.applySilentSlippingNoClip(player)");
		assertContains("entity mixin registered", mixins, "\"MixinEntity\"");

		assertContains("local player mixin targets client player", localPlayerMixin, "@Mixin(LocalPlayer.class)");
		assertContains("local player mixin applies no-clip before aiStep push-out", localPlayerMixin,
				"hemomancy$applySilentSlippingNoClipBeforePushOut");
		assertContains("local player no-clip hook runs at aiStep head", localPlayerMixin,
				"@Inject(method = \"aiStep\", at = @At(\"HEAD\"), remap = false)");
		assertContains("local player no-clip hook calls shared applier", localPlayerMixin,
				"SilentArchonArmorAbilityHandler.applySilentSlippingNoClip((LocalPlayer) (Object) this)");
		assertContains("local player mixin registered", mixins, "\"MixinLocalPlayer\"");

		assertContains("docs mention movement-collision reinforcement", docs,
				"reinforced at the movement collision boundary");
		assertContains("docs mention shard-only slipping pulses", docs, "small shard-only pulses");
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

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": still contains " + forbidden);
		}
	}
}
