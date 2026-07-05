package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClawSlashRendererSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private ClawSlashRendererSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		rendererDrawsThreeTaperedRibbons();
		rendererRandomizesShapeAndRotation();
		packetAndClientEventsWireRenderer();
		gloamUsesClawSlashVisuals();
		livingBaghnakhUsesClawSlashVisuals();
		livingBaghnakhAutoClicksWhenAttackCooldownIsReady();
	}

	private static void rendererDrawsThreeTaperedRibbons() throws IOException {
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/ClawSlashRenderer.java");
		assertContains("renderer stores active slash instances", renderer, "ACTIVE_SLASHES");
		assertContains("renderer exposes spawn method", renderer, "public static void spawn(");
		assertContains("renderer exposes tick method", renderer, "public static void tick()");
		assertContains("renderer exposes render method", renderer, "public static void render(PoseStack poseStack, float partialTick)");
		assertContains("renderer draws three claw strokes", renderer, "STROKE_COUNT = 3");
		assertContains("renderer uses tapered width", renderer, "taperedWidth(");
		assertContains("renderer emits glow pass", renderer, "RenderTypeInit.CLAW_SLASH_GLOW");
		assertContains("renderer emits core pass", renderer, "RenderTypeInit.CLAW_SLASH_CORE");
	}

	private static void rendererRandomizesShapeAndRotation() throws IOException {
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/ClawSlashRenderer.java");
		assertContains("renderer has roll variation constant", renderer, "MAX_ROLL_RADIANS");
		assertContains("renderer has yaw variation constant", renderer, "MAX_YAW_RADIANS");
		assertContains("renderer has pitch variation constant", renderer, "MAX_PITCH_RADIANS");
		assertContains("renderer derives seeded signed random values", renderer, "seededSignedUnit(");
		assertContains("renderer rotates slash forward per cast", renderer, "randomizedForward(");
		assertContains("renderer rolls the slash plane per cast", renderer, "rotateAroundAxis(up, forward, roll)");
		assertContains("renderer varies each stroke", renderer, "strokeShapeOffset(");
		assertContains("renderer jitters curve shape", renderer, "curveJitter(");
		assertContains("renderer varies stroke length", renderer, "lengthJitter");
		assertContains("renderer varies stroke arc", renderer, "arcJitter");
	}

	private static void packetAndClientEventsWireRenderer() throws IOException {
		String renderTypes = read("src/main/java/com/vincenthuto/hemomancy/common/init/RenderTypeInit.java");
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String packet = read("src/main/java/com/vincenthuto/hemomancy/common/network/particle/SpawnClawSlashPacket.java");
		String packetHandler = read("src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java");

		assertContains("glow render type registered", renderTypes, "CLAW_SLASH_GLOW");
		assertContains("core render type registered", renderTypes, "CLAW_SLASH_CORE");
		assertContains("client events tick slash renderer", clientEvents, "ClawSlashRenderer.tick()");
		assertContains("client events render slash renderer", clientEvents, "ClawSlashRenderer.render(event.getPoseStack(), partialTick)");
		assertContains("packet type exists", packet, "TYPE = new Type<>(Hemomancy.rloc(\"spawn_claw_slash_packet\"))");
		assertContains("packet queues renderer spawn", packet, "ClawSlashRenderer.spawn(");
		assertContains("packet registered", packetHandler, "net.playToClient(SpawnClawSlashPacket.TYPE");
		assertContains("send helper exists", packetHandler, "sendClawSlash(");
		assertContains("send helper uses nearby clients", packetHandler, "new SpawnClawSlashPacket(pos, forward, color, ambush, scale, level.random.nextInt())");
	}

	private static void gloamUsesClawSlashVisuals() throws IOException {
		String gloam = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/GloamLacerationManip.java");
		String entityEffects = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/EntityManipulationEffects.java");

		assertContains("gloam player cast imports packet handler", gloam,
				"import com.vincenthuto.hemomancy.common.network.PacketHandler;");
		assertContains("gloam player cast sends slash packet", gloam, "PacketHandler.sendClawSlash(");
		assertNotContains("gloam no longer imports generic glow", gloam, "GlowParticleFactory");
		assertContains("entity cast sends slash packet", entityEffects, "PacketHandler.sendClawSlash(");
		assertContains("entity cast uses caster aim", entityEffects, "context.aim()");
	}

	private static void livingBaghnakhUsesClawSlashVisuals() throws IOException {
		String baghnakh = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingBaghnakhItem.java");
		assertContains("baghnakh keeps Tenebris tendency", baghnakh, "EnumBloodTendency.TENEBRIS");
		assertContains("baghnakh overrides hit hook", baghnakh, "public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)");
		assertContains("baghnakh preserves base living weapon behavior", baghnakh, "boolean hit = super.hurtEnemy(stack, target, attacker);");
		assertContains("baghnakh respects restored staff guard", baghnakh, "LivingStaffWeaponFormHelper.wasRestoredOutOfHand(stack, attacker)");
		assertContains("baghnakh only sends from server level", baghnakh, "attacker.level() instanceof ServerLevel serverLevel");
		assertContains("baghnakh sends claw slash visual", baghnakh, "PacketHandler.sendClawSlash(");
		assertContains("baghnakh uses Tenebris slash color", baghnakh, "new ParticleColor(70, 0, 125)");
	}

	private static void livingBaghnakhAutoClicksWhenAttackCooldownIsReady() throws IOException {
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		assertContains("client post tick checks Baghnakh auto attack", clientEvents, "handleLivingBaghnakhAutoAttack()");
		assertContains("auto attack is scoped to living Baghnakh", clientEvents, "player.getMainHandItem().is(ItemInit.living_baghnakh.get())");
		assertContains("auto attack requires held attack key", clientEvents, "minecraft.options.keyAttack.isDown()");
		assertContains("auto attack waits for full vanilla cooldown", clientEvents, "player.getAttackStrengthScale(0.0F) < 1.0F");
		assertContains("auto attack only targets entities", clientEvents, "minecraft.hitResult instanceof EntityHitResult entityHit");
		assertContains("auto attack goes through game mode attack", clientEvents, "minecraft.gameMode.attack(player, entityHit.getEntity())");
		assertContains("auto attack swings the main hand", clientEvents, "player.swing(InteractionHand.MAIN_HAND)");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing `" + expected + "`");
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": found `" + unexpected + "`");
		}
	}
}
