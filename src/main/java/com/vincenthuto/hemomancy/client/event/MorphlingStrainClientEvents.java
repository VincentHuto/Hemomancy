package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.MorphlingStrainTrails;
import com.vincenthuto.hemomancy.client.render.world.OutpouringBrushes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingActiveState;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.PrimalMorphlingRules;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Once a client tick for every visible Morphling wearer: keeps the trail and Lifeline memory the strain world effects
 * draw from, and sheds the strains' small accents on the shared toolkit's particles (ink motes from an Ink Cap,
 * droplets from a Marrowplume, frost from a closed Hushbell, sparks from a Kindled mane). The geometry carries each
 * look; these are accents and stay sparse.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MorphlingStrainClientEvents {
	private MorphlingStrainClientEvents() {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.isPaused()) return;
		for (Player player : mc.level.players()) {
			var equipped = HemoCapabilityAccess.getEquippedMorphling(player);
			MorphlingStrain strain = MorphlingStrains.of(equipped.getEquippedMorphling()).orElse(null);
			if (strain == null) {
				MorphlingStrainTrails.forget(player.getUUID());
				continue;
			}
			int flags = equipped.getActiveFlags();
			int maturity = MorphlingItem.getMaturityLevel(equipped.getEquippedMorphling());
			MorphlingStrainTrails.record(player);
			MorphlingStrainTrails.lifeline(player, MorphlingActiveState.has(flags, MorphlingActiveState.LIFELINE));
			if (mc.getCameraEntity() == player && mc.options.getCameraType().isFirstPerson()) continue;
			if (!player.isInvisible()) {
				accents(player, strain, flags, maturity, player.getRandom());
			} else if (strain == MorphlingStrains.INK_CAP && MorphlingActiveState.has(flags, MorphlingActiveState.DELIQUESCE)) {
				// Deliquesce hides the host but not its ink trail (the trail that darkens hostiles).
				inkTrail(player, player.getRandom());
			}
		}
	}

	private static void accents(Player player, MorphlingStrain strain, int flags, int maturity, RandomSource random) {
		Vec3 feet = player.position();
		double height = player.getBbHeight();
		if (strain == MorphlingStrains.INK_CAP) {
			// Ink drips from the cap's rim from Mature; Folded Away leaves a trail of ink behind a moving host.
			if (maturity >= 3 && random.nextInt(14) == 0) {
				Vec3 rim = feet.add((random.nextDouble() - .5) * .6, height * .82, (random.nextDouble() - .5) * .6);
				OutpouringBrushes.mist(rim, new Vec3(0, -.04, 0), true, .12F, 18);
			}
			if (MorphlingActiveState.has(flags, MorphlingActiveState.FOLDED_AWAY | MorphlingActiveState.DELIQUESCE)) {
				inkTrail(player, random);
			}
		} else if (strain == MorphlingStrains.MARROWPLUME) {
			if (maturity >= 2 && random.nextInt(24) == 0) {
				Vec3 hand = feet.add((random.nextDouble() - .5) * .8, height * .45, (random.nextDouble() - .5) * .8);
				OutpouringBrushes.blood(hand, new Vec3(0, -1, 0), true, 1, true);
			}
			if (MorphlingActiveState.has(flags, MorphlingActiveState.PLUME_CLOUD)) {
				OutpouringBrushes.blood(feet.add(0, height * .7, 0), new Vec3(0, .4, 0), true, 3, true);
			}
		} else if (strain == MorphlingStrains.HUSHBELL) {
			if (MorphlingActiveState.has(flags, MorphlingActiveState.FOLD_SHUT | MorphlingActiveState.CRYPTOBIOSIS)
					&& random.nextInt(6) == 0) {
				OutpouringBrushes.frost(feet.add(0, height * .5, 0), new Vec3(0, .2, 0), 1, true);
			}
		} else if (strain == MorphlingStrains.KINBUD) {
			if (MorphlingActiveState.has(flags, MorphlingActiveState.KINDLED) && random.nextInt(4) == 0) {
				OutpouringBrushes.flames(feet.add(0, height * .85, 0), new Vec3(0, .3, 0), 1, true);
			}
		} else if (strain == MorphlingStrains.SUNBURST) {
			if (maturity >= PrimalMorphlingRules.APEX_LEVEL
					&& MorphlingActiveState.has(flags, MorphlingActiveState.LANTERN_FRONDS) && random.nextInt(10) == 0) {
				Vec3 frond = feet.add((random.nextDouble() - .5), height * .9, (random.nextDouble() - .5));
				OutpouringBrushes.mist(frond, new Vec3(0, .02, 0), false, .2F, 30);
			}
		}
	}

	/** Ink left behind a moving Ink Cap host. */
	private static void inkTrail(Player player, RandomSource random) {
		if (player.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4 && random.nextInt(2) == 0) {
			OutpouringBrushes.mist(player.position().add(0, player.getBbHeight() * .4, 0), new Vec3(0, .01, 0), true, .45F, 40);
		}
	}
}
