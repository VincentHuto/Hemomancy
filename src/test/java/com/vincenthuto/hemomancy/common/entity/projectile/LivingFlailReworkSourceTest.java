package com.vincenthuto.hemomancy.common.entity.projectile;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingFlailReworkSourceTest {
	private static String read(String relative) throws IOException {
		return Files.readString(Path.of(relative));
	}

	private static void contains(String label, String source, String expected) {
		assertTrue(source.contains(expected), label + " missing: " + expected);
	}

	@Test
	void dedicatedProjectileIsRegisteredPersistedAndRendered() throws Exception {
		String entity = read("src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/LivingFlailHeadProjectileEntity.java");
		String persistence = entity + read("src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/LivingFlailProjectileState.java");
		String registry = read("src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java");
		String client = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		contains("entity stores deployment UUID", persistence, "DeploymentId");
		contains("entity stores charge", persistence, "FlailCharge");
		contains("entity stores original hand", persistence, "OriginalHand");
		contains("entity stores tendency", persistence, "Tendency");
		contains("entity registers", registry, "living_flail_head");
		contains("renderer registers", client, "LivingFlailHeadProjectileRenderer");
		String freezingTag = read("src/main/resources/data/minecraft/tags/damage_type/is_freezing.json");
		contains("freeze damage tag", freezingTag, "hemomancy:living_flail_freeze");
	}

	@Test
	void impactUsesProtectionHooksAndAuthoredGlacialEffects() throws Exception {
		String effects = read("src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/LivingFlailImpactEffects.java");
		contains("placement hook", effects, "EventHooks.onBlockPlace");
		contains("authored glow", effects, "GlowParticleFactory");
		contains("authored tendril", effects, "TendrilEffectSpawner");
		contains("authored cells", effects, "BloodCellParticleFactory");
		contains("charge-scaled camera impulse", effects, "LivingFlailImpactPacket");
		contains("source water", effects, "Fluids.WATER");
		contains("ice conversion", effects, "Blocks.ICE");
		contains("snow layers", effects, "SnowLayerBlock.LAYERS");
	}

	@Test
	void itemAndEventsCoverDeploymentAndMultiplayerRecovery() throws Exception {
		String item = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingFlailItem.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffWeaponFormEvents.java");
		String state = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingFlailDeployment.java");
		contains("item release", item, "releaseUsing");
		contains("movement slowdown", item, "CHARGING_MOVEMENT_SCALE");
		contains("durable deployment", state, "DEPLOYMENT_KEY");
		contains("logout reconciliation", events, "LivingFlailDeployment.reconcile");
		contains("dimension reconciliation", events, "playerChangedDimension");
		contains("periodic projectile loss reconciliation", events, "reconcileMissingProjectile");
	}

	@Test
	void firstAndThirdPersonRenderOrbitChainAndHeadlessDeployment() throws Exception {
		String helper = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/LivingFlailRenderHelper.java");
		String projectile = read("src/main/java/com/vincenthuto/hemomancy/client/render/entity/projectile/LivingFlailHeadProjectileRenderer.java");
		contains("charge orbit", helper, "renderChargingOrbit");
		contains("headless deployment", helper, "LivingFlailDeployment.isDeployed");
		contains("maximum glow", helper, "maximumChargeFlash");
		contains("projectile uses flail model", projectile, "LivingFlailModel");
		contains("projectile interpolation", projectile, "getPosition(partialTick)");
	}
}
