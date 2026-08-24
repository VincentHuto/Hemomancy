package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

public final class UnstainedJourneyController {
	private static final String VERIFIED_STAGE_KEY = "hemomancy.dev_test.journey.unstained_verified_stage";
	private static final String MODE_KEY = "hemomancy.dev_test.journey.unstained_mode";
	private static final String CURE = "cure";
	private static final String NOVITIATE = "novitiate";
	private static final UnstainedJourneyStage[] NOVITIATE_STAGES = {
			UnstainedJourneyStage.NOVITIATE_GATHER_REMEDIES,
			UnstainedJourneyStage.NOVITIATE_GENTLE_SEPARATION,
			UnstainedJourneyStage.NOVITIATE_STILLWATER_LABOR,
			UnstainedJourneyStage.NOVITIATE_CLEAN_LABOR,
			UnstainedJourneyStage.NOVITIATE_SHELTER_AFFLICTED,
			UnstainedJourneyStage.CLARITY_PREPARED,
			UnstainedJourneyStage.CLARITY_ASCENSION,
			UnstainedJourneyStage.COMPLETE
	};

	private UnstainedJourneyController() { }

	public static UnstainedJourneyResult start(ServerPlayer player) {
		return start(player, CURE);
	}

	public static UnstainedJourneyResult start(ServerPlayer player, String mode) {
		if (ChamberVisitService.isActive(player)
				|| player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
			return UnstainedJourneyResult.fail(UnstainedJourneyStage.PODIUM_SUPPRESSION,
					"Return from the active Chamber of Will visit before starting the journey.");
		}
		HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
		if (!captured.passed()) return fail(captured.message());
		HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
		if (!reset.passed()) return fail(reset.message());
		player.getPersistentData().putString(JourneyRoute.KEY, JourneyRoute.UNSTAINED);
		player.getPersistentData().putString(MODE_KEY, NOVITIATE.equals(mode) ? NOVITIATE : CURE);
		BlockPos origin;
		try {
			origin = HemoJourneyFixtures.findClearOrigin(player);
			if (!player.serverLevel().getEntitiesOfClass(Entity.class, HemoJourneyFixtures.bounds(origin),
					entity -> entity != player).isEmpty()) {
				throw new IllegalStateException("The selected fixture volume contains preexisting entities");
			}
			player.getPersistentData().putLong(HemoJourneyFixtures.ORIGIN_KEY, origin.asLong());
			player.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
					player.serverLevel().dimension().location().toString());
			UnstainedJourneyStage first = NOVITIATE.equals(mode)
					? UnstainedJourneyStage.NOVITIATE_GATHER_REMEDIES : UnstainedJourneyStage.PODIUM_SUPPRESSION;
			prepare(player, first, origin);
			return pass(first, "Unstained " + mode + " journey started. ");
		} catch (RuntimeException exception) {
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			clearKeys(player);
			return fail("Unstained journey fixture preparation failed: " + message(exception)
					+ (restored.passed() ? " Pre-journey state was restored." : " " + restored.message()));
		}
	}

	public static UnstainedJourneyResult next(ServerPlayer player) {
		UnstainedJourneyResult inactive = requireActive(player);
		if (inactive != null) return inactive;
		BlockPos origin = origin(player);
		UnstainedJourneyStage current = currentStage(player);
		if (current == UnstainedJourneyStage.COMPLETE) {
			cleanup(player, origin);
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			if (restored.passed()) clearKeys(player);
			return restored.passed()
					? new UnstainedJourneyResult(true, current,
							"Unstained journey complete; restored the pre-journey player state and removed fixtures.")
					: UnstainedJourneyResult.fail(current, restored.message());
		}
		boolean verified = current.id().equals(player.getPersistentData().getString(VERIFIED_STAGE_KEY));
		if (!verified) {
			UnstainedJourneyResult check = UnstainedJourneyChecks.verify(player, current, origin);
			if (!check.passed()) return check;
			player.getPersistentData().putString(VERIFIED_STAGE_KEY, current.id());
		}
		UnstainedJourneyStage next = next(player, current);
		try {
			ownDrops(player, origin);
			if (next == UnstainedJourneyStage.COMPLETE) cleanup(player, origin);
			else UnstainedJourneyFixtures.prepare(player, next, origin);
			player.getPersistentData().putString(HemoJourneySnapshot.STAGE_KEY, next.id());
			player.getPersistentData().remove(VERIFIED_STAGE_KEY);
			moveAndExplain(player, next, origin);
			return new UnstainedJourneyResult(true, next, next == UnstainedJourneyStage.COMPLETE
					? "All UNSTAINED checkpoints passed. Run journey unstained next once more to restore your snapshot."
					: action(next));
		} catch (RuntimeException exception) {
			return UnstainedJourneyResult.fail(current, "Next UNSTAINED fixture failed: " + message(exception)
					+ ". The verified checkpoint is latched; run journey unstained next to retry.");
		}
	}

	public static UnstainedJourneyResult status(ServerPlayer player) {
		UnstainedJourneyResult inactive = requireActive(player);
		if (inactive != null) return inactive;
		UnstainedJourneyStage stage = currentStage(player);
		if (stage.id().equals(player.getPersistentData().getString(VERIFIED_STAGE_KEY))) {
			return new UnstainedJourneyResult(true, stage,
					"Checkpoint verified and latched. Run journey unstained next to retry the transition.");
		}
		UnstainedJourneyResult check = UnstainedJourneyChecks.verify(player, stage, origin(player));
		return new UnstainedJourneyResult(check.passed(), stage,
				action(stage) + (check.passed() ? " Conditions currently pass." : "\nUnmet:\n" + check.message()));
	}

	public static UnstainedJourneyResult reset(ServerPlayer player) {
		UnstainedJourneyResult inactive = requireActive(player);
		if (inactive != null) return inactive;
		BlockPos origin = origin(player);
		cleanup(player, origin);
		HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
		if (!reset.passed()) return fail(reset.message());
		player.getPersistentData().putString(JourneyRoute.KEY, JourneyRoute.UNSTAINED);
		try {
			UnstainedJourneyStage first = NOVITIATE.equals(player.getPersistentData().getString(MODE_KEY))
					? UnstainedJourneyStage.NOVITIATE_GATHER_REMEDIES : UnstainedJourneyStage.PODIUM_SUPPRESSION;
			prepare(player, first, origin);
			return pass(first, "Unstained journey reset. ");
		} catch (RuntimeException exception) {
			return fail("Unstained journey reset fixture failed: " + message(exception));
		}
	}

	public static UnstainedJourneyResult clear(ServerPlayer player) {
		if (!player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY, Tag.TAG_COMPOUND)) {
			return new UnstainedJourneyResult(true, UnstainedJourneyStage.PODIUM_SUPPRESSION,
					"No active UNSTAINED journey snapshot.");
		}
		if (!JourneyRoute.is(player, JourneyRoute.UNSTAINED)) {
			return UnstainedJourneyResult.fail(UnstainedJourneyStage.PODIUM_SUPPRESSION,
					"The active journey belongs to the Harbinger route.");
		}
		if (player.getPersistentData().contains(HemoJourneyFixtures.ORIGIN_KEY, Tag.TAG_LONG)) {
			cleanup(player, origin(player));
		}
		HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
		if (restored.passed()) clearKeys(player);
		return new UnstainedJourneyResult(restored.passed(), currentStage(player), restored.message());
	}

	private static void prepare(ServerPlayer player, UnstainedJourneyStage stage, BlockPos origin) {
		UnstainedJourneyFixtures.prepare(player, stage, origin);
		player.getPersistentData().putString(HemoJourneySnapshot.STAGE_KEY, stage.id());
		player.getPersistentData().remove(VERIFIED_STAGE_KEY);
		moveAndExplain(player, stage, origin);
	}

	private static void cleanup(ServerPlayer player, BlockPos origin) {
		ownDrops(player, origin);
		HemoJourneyFixtures.cleanup(player, origin);
	}

	private static void ownDrops(ServerPlayer player, BlockPos origin) {
		for (ItemEntity entity : HemoJourneyFixtures.fixtureLevel(player).getEntitiesOfClass(ItemEntity.class,
				HemoJourneyFixtures.bounds(origin))) entity.addTag(HemoJourneyFixtures.entityMarker(origin));
	}

	private static UnstainedJourneyResult requireActive(ServerPlayer player) {
		if (!player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY, Tag.TAG_COMPOUND)) return fail("No active journey snapshot.");
		if (!JourneyRoute.is(player, JourneyRoute.UNSTAINED)) return fail("The active journey belongs to the Harbinger route.");
		if (!player.getPersistentData().contains(HemoJourneyFixtures.ORIGIN_KEY, Tag.TAG_LONG)) return fail("Active journey has no fixture origin.");
		if (!player.getPersistentData().contains(HemoJourneyFixtures.DIMENSION_KEY, Tag.TAG_STRING)) return fail("Active journey has no fixture dimension.");
		try { HemoJourneyFixtures.fixtureLevel(player); }
		catch (RuntimeException exception) { return fail(message(exception)); }
		return null;
	}

	private static UnstainedJourneyStage next(ServerPlayer player, UnstainedJourneyStage stage) {
		if (NOVITIATE.equals(player.getPersistentData().getString(MODE_KEY))) {
			for (int i = 0; i < NOVITIATE_STAGES.length - 1; i++) {
				if (NOVITIATE_STAGES[i] == stage) return NOVITIATE_STAGES[i + 1];
			}
			return UnstainedJourneyStage.COMPLETE;
		}
		return UnstainedJourneyStage.values()[Math.min(stage.ordinal() + 1, UnstainedJourneyStage.COMPLETE.ordinal())];
	}

	private static BlockPos origin(ServerPlayer player) {
		return BlockPos.of(player.getPersistentData().getLong(HemoJourneyFixtures.ORIGIN_KEY));
	}

	private static UnstainedJourneyStage currentStage(ServerPlayer player) {
		return UnstainedJourneyStage.byId(player.getPersistentData().getString(HemoJourneySnapshot.STAGE_KEY));
	}

	private static void moveAndExplain(ServerPlayer player, UnstainedJourneyStage stage, BlockPos origin) {
		if (stage != UnstainedJourneyStage.COMPLETE) {
			BlockPos landing = HemoJourneyController.landing(origin);
			player.teleportTo(HemoJourneyFixtures.fixtureLevel(player), landing.getX() + 0.5D, landing.getY(),
					landing.getZ() + 0.5D, 0.0F, 0.0F);
			player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(origin.above()));
		}
		player.sendSystemMessage(Component.literal(action(stage)));
	}

	private static String action(UnstainedJourneyStage stage) {
		return switch (stage) {
			case NOVITIATE_GATHER_REMEDIES -> "Tell the Zealot you wish to take the vows, then offer four Ghost Pipe and four Lethean Poppies to the Acolyte.";
			case NOVITIATE_GENTLE_SEPARATION -> "Accept the Gentle Separation, then use the supplied Pallid Retort to distill Hemolytic Solution and return to the Acolyte.";
			case NOVITIATE_STILLWATER_LABOR -> "Accept Stillwater Labor, produce four Lethean Dew in the supplied Condenser, and return to the Zealot.";
			case NOVITIATE_CLEAN_LABOR -> "Accept Clean Labor, consecrate all eight supplied blood-infested blocks, and return to the Zealot.";
			case NOVITIATE_SHELTER_AFFLICTED -> "Accept Shelter the Afflicted from the Guardian, then invoke Still Waters while that Guardian is in its area.";
			case PODIUM_SUPPRESSION -> "Use the supplied Hemolytic Solution on the Unstained Podium, then run journey unstained next.";
			case LETHEAN_BAPTISM -> "Invoke the prepared Rite of Lethean Baptism with the Blood Crafting key. It begins treatment and grants no pledge dagger.";
			case GHOST_PIPE_OBSERVANCE -> "Speak to the Acolyte to accept Gather Ghost Pipe, then speak again with the four supplied blooms to fulfill it.";
			case TAINTED_ACOLYTE_OBSERVANCES -> "At the Acolyte, accept and fulfill both the Wreath and Hemolytic Solution Observances with the supplied offerings.";
			case SILVER_VEIL -> "Invoke the prepared Rite of the Silver Veil and receive Verdigris Aura.";
			case CLEANSING_OBSERVANCES -> "Put two supplied bottles in the boosted Stillwater Condenser and wait for four Dew. Fulfill Still Waters with the Zealot and Plating with the Guardian.";
			case PALLID_ICON_OBSERVANCE -> "Accept and fulfill Bear the Pallid Icon with the Zealot.";
			case SILTHMERE_REMEMBRANCE -> "Invoke Silthmere's Remembrance to cross from 95 to 100 Purity.";
			case CLOSED_VEIN -> "Invoke Closed Vein to irreversibly restore your baseline while preserving learned blood knowledge.";
			case CONSECRATED_COPPER_OBSERVANCE -> "Accept and fulfill Consecrate Copper with the Acolyte.";
			case CLARITY_PREPARED -> "Use the supplied Consecrated Copper on the Unstained Podium to prepare Clarity Ascension.";
			case CLARITY_ASCENSION -> "Invoke Clarity Ascension to pledge, receive the Absolution Dagger, and awaken the initial Still Arts.";
			case GLASS_LUNGS -> "Invoke Glass Lungs and pick up its Lethean Chalice.";
			case CHALICE_OBSERVANCE -> "Accept and fulfill Offer the Chalice with the Acolyte.";
			case DISCERNING -> "Wait for the Discerning milestone and its Still Arts, then advance.";
			case PALE_VIGIL -> "Invoke the Pale Vigil to reach Resolute Clarity and receive both wards.";
			case MOON_WASHED_COPPER -> "Invoke Moon-Washed Copper and pick up its Pale Silver Bell.";
			case PALE_WATCH_OBSERVANCE -> "Accept and fulfill Ring the Pale Watch with the Guardian.";
			case RESOLUTE -> "Confirm the Resolute Still Arts have been granted, then advance.";
			case ENLIGHTENED -> "Use the Hemolytic Plating on the Podium to reach 100 Clarity and receive the final Still Art and Vestment.";
			case LETHEAN_FONT -> "Invoke the Lethean Font as the Enlightened capstone and collect its Pallid Icon.";
			case COMPLETE -> "Run journey unstained next once more to restore the pre-journey snapshot.";
		};
	}

	private static UnstainedJourneyResult pass(UnstainedJourneyStage stage, String prefix) {
		return new UnstainedJourneyResult(true, stage, prefix + action(stage));
	}

	private static UnstainedJourneyResult fail(String message) {
		return UnstainedJourneyResult.fail(UnstainedJourneyStage.PODIUM_SUPPRESSION, message);
	}

	private static void clearKeys(ServerPlayer player) {
		player.getPersistentData().remove(HemoJourneyFixtures.ORIGIN_KEY);
		player.getPersistentData().remove(HemoJourneyFixtures.DIMENSION_KEY);
		player.getPersistentData().remove(HemoJourneyFixtures.OWNED_BLOCKS_KEY);
		player.getPersistentData().remove(HemoJourneyFixtures.BASELINE_KEY);
		player.getPersistentData().remove(HemoJourneyFixtures.OWNED_OUTPUTS_KEY);
		player.getPersistentData().remove(VERIFIED_STAGE_KEY);
		player.getPersistentData().remove(MODE_KEY);
		player.getPersistentData().remove(JourneyRoute.KEY);
	}

	private static String message(RuntimeException exception) {
		return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
	}
}
