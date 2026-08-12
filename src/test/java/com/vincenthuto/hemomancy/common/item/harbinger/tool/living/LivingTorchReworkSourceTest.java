package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class LivingTorchReworkSourceTest {
	@Test
	void meleeStatsAndCrimsonIgnitionRemainIntact() throws IOException {
		String item = read("common/item/harbinger/tool/living/LivingTorchItem.java");
		assertContains(item, "super(speedIn, attackDamageIn, -2.1f, EnumBloodTendency.FLAMMEUS");
		assertContains(item, "CrimsonFireHelper.igniteCrimson(target, 4)");
	}

	@Test
	void serverChannelOwnsUseBloodTargetsAndCleanup() throws IOException {
		String item = read("common/item/harbinger/tool/living/LivingTorchItem.java");
		assertContains(item, "getUseDuration");
		assertContains(item, "72000");
		assertContains(item, "player.startUsingItem(hand)");
		assertContains(item, "HemoCapabilityAccess.getBloodVolume(player)");
		assertContains(item, "LivingTorchBreathRules.isDamagePulse");
		assertContains(item, "player.hasLineOfSight(target)");
		assertContains(item, "CrimsonFireHelper.igniteCrimson(target, 4)");
		assertContains(item, "PacketHandler.syncPlayerAnimation");
		assertContains(item, "stopUsingItem");
		assertContains(item, "HemoDamageTypes.livingTorchBreath");
	}

	@Test
	void breathDamageIsFireAndBypassesVanillaHurtCooldownForExactCadence() throws IOException {
		String damageType = Files.readString(Path.of(
				"src/main/resources/data/hemomancy/damage_type/living_torch_breath.json"));
		String fireTag = Files.readString(Path.of(
				"src/main/resources/data/minecraft/tags/damage_type/is_fire.json"));
		String cooldownTag = Files.readString(Path.of(
				"src/main/resources/data/minecraft/tags/damage_type/bypasses_cooldown.json"));
		assertContains(damageType, "living_torch_breath");
		assertContains(fireTag, "hemomancy:living_torch_breath");
		assertContains(cooldownTag, "hemomancy:living_torch_breath");
	}

	@Test
	void reusablePosePacketAndMixinsAreRegistered() throws IOException {
		String packet = read("common/network/capa/harbinger/PacketSyncPlayerAnimation.java");
		String handler = read("common/network/PacketHandler.java");
		String humanoid = read("mixin/core/MixinHumanoidModel.java");
		String handLayer = read("mixin/core/MixinPlayerItemInHandLayer.java");
		String localPlayer = read("mixin/core/MixinLocalPlayer.java");
		assertContains(packet, "PlayerAnimationKind");
		assertContains(handler, "PacketSyncPlayerAnimation.TYPE");
		assertContains(humanoid, "PlayerAnimationClientState.applyThirdPersonPose");
		assertContains(handLayer, "PlayerAnimationClientState");
		assertContains(localPlayer, "LivingTorchBreathRules.MOVEMENT_MULTIPLIER");
	}

	@Test
	void authoredEffectsAndLoopAudioHaveExplicitCleanup() throws IOException {
		String effects = read("client/player/LivingTorchBreathEffects.java");
		String client = read("client/event/LivingTorchClientEvents.java");
		String layer = read("client/render/layer/player/LivingTorchBreathLayer.java");
		String layerEvents = read("client/event/LayerEvents.java");
		String item = read("common/item/harbinger/tool/living/LivingTorchItem.java");
		String sounds = Files.readString(Path.of("src/main/resources/assets/hemomancy/sounds.json"));
		assertContains(effects, "BloodCellParticleFactory");
		assertFalse(effects.contains("AbsorbedBloodCellParticleFactory"),
				"torch blood cells must travel outward instead of interpolating back toward their emitter");
		assertContains(effects, "EmberParticleFactory");
		assertFalse(effects.contains("GlowParticleFactory"),
				"long-lived glow particles leave a detached world-space trail behind a moving player");
		assertFalse(effects.contains("DarkGlowParticleFactory"),
				"the breath core must use the same short authored lifetime as the flame stream");
		assertContains(layer, "getParentModel().translateToHand(arm, poseStack)");
		assertContains(layer, "LivingTorchRenderPlacement.tipFromCurrentPose");
		assertContains(layerEvents, "new LivingTorchBreathLayer");
		assertFalse(client.contains("RenderPlayerEvent.Post"),
				"third-person emission must run inside the player render layer's live model pose stack");
		assertFalse(item.contains("LivingTorchBreathEffects.emit"),
				"server gameplay must not emit a camera-centered visual stream");
		assertContains(client, "PlayerAnimationClientState");
		assertContains(client, "stopBreathLoop");
		assertContains(sounds, "item.living_torch.breath_loop");
	}

	private static String read(String relative) throws IOException {
		return Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy", relative));
	}

	private static void assertContains(String text, String expected) {
		assertTrue(text.contains(expected), () -> "Expected source to contain: " + expected);
	}
}
