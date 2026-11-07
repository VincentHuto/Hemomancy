package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.block.harbinger.CrimsonFireHelper;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Outcome;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Patch;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * What Putriciel's moves do beyond the kit's strike: their sounds (design section 14), the points and anchors a
 * footprint needs, the hazards they leave, the floor they scorch, the faithful the fire burns, and each hit's
 * colonisation and status. Server only: the controller calls {@code onTick} and {@code onHit}.
 * <p>
 * Revision 2026-11-06 (R2): five rot moves sometimes leave rot sludge ({@link PutricielSludgeRules}): Shelf Volley's
 * landings 0, 3 and 6 (instead of their colony), the Censer Drop's crater, every other Offering Grasp's runnel, a Root
 * Snare that strikes no one, and the Bloom's last pulse. In the interval every fire move's live footprint lights the
 * pools it reaches.
 */
final class PutricielMoveEffects {
	/** Shelf Volley's discs spread this far round the quarry, along the way they move, led this many ticks. */
	static final double VOLLEY_SPREAD = 4.0D;
	static final int VOLLEY_LEAD = 10;
	/** Offering Grasp pulls this hard toward the body (about two blocks). */
	static final double GRASP_PULL = 0.6D;
	static final double DROP_TOSS = 0.5D;
	/** The sacs swell one after another, this many ticks apart, eight of them. */
	static final int SAC_GAP = 3, SACS = 8;
	/** The Communion heals 1% of the maximum every 10 ticks. */
	static final float COMMUNION_HEAL = 0.01F;
	static final int COMMUNION_HEAL_EVERY = 10;

	private PutricielMoveEffects() {
	}

	static void tick(PutricielMove move, SaintMoveContext ctx) {
		if (!(ctx.saint() instanceof PutricielEntity saint)) return;
		int tick = ctx.tick();
		int age = ctx.age();
		boolean active = ctx.stage() == BossMoveTimeline.Stage.ACTIVE;
		int lastActive = ctx.saint().controller().timeline().active() - 1;
		FootprintFrame frame = ctx.frame();
		// a fire move reaching pooled rot lights it where it first strikes (the Ignition's own ring is on the schedule)
		if (active && move.fire() && move != PutricielMove.IGNITION) saint.fireReachesSludge(move.footprint(ctx.inputs()), frame, age);
		switch (move) {
			case CENSER_SWING, CENSER_BACKSWING -> {
				if (tick == 0) saint.sound(SoundEvents.CHAIN_STEP, 0.9F, 0.7F);
				if (ctx.committedNow()) saint.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.6F);
			}
			case CENSER_DROP -> {
				if (tick == 0) saint.sound(SoundEvents.CHAIN_STEP, 1.0F, 0.6F);
				if (ctx.committedNow()) {
					saint.sound(SoundInit.RITE_STAFF_IMPACT.get(), 1.2F, 0.7F);
					saint.burst(frame.aim().add(0.0D, 0.3D, 0.0D), PutricielVisualRules.CENSER_BURST, 2.75F);
					// the fouled crater fills
					saint.laySludge(Patch.disc(frame.aim(), PutricielSludgeRules.DROP_RADIUS));
				}
			}
			case OFFERING_GRASP -> {
				if (ctx.committedNow()) saint.sound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 0.6F);
				// every other Grasp the trench overflows into a runnel along the lane's outer four blocks
				if (active && age == lastActive && PutricielSludgeRules.graspPool(saint.syncedMoveSerial())) {
					saint.laySludge(Patch.runnel(along(frame, PutricielSludgeRules.RUNNEL_FROM),
							along(frame, PutricielSludgeRules.RUNNEL_TO), PutricielSludgeRules.RUNNEL_HALF_WIDTH));
				}
			}
			case ROT_BREATH -> {
				if (ctx.committedNow()) saint.sound(SoundInit.ENTITY_MYCOPHANT_AMBIENT.get(), 1.0F, 1.3F);
				if (active && age == lastActive) {
					saint.spawnHazard(PutricielHazard.ROT_CLOUD, PutricielMove.breathCone(),
							FootprintFrame.simple(frame.origin(), frame.aim(), frame.target(), frame.floorY()));
				}
			}
			case SAC_SWELL -> {
				if (tick < SAC_GAP * SACS && tick % SAC_GAP == 0) {
					saint.sound(SoundEvents.FUNGUS_PLACE, 0.8F, 0.6F + 0.05F * (tick / SAC_GAP));
				}
				if (ctx.committedNow()) saint.sound(SoundInit.ENTITY_MYCOPHANT_SUMMON.get(), 1.3F, 0.8F);
			}
			case SHELF_VOLLEY -> {
				if (tick == 0) ctx.saint().controller().setPoints(volleyPoints(ctx));
				if (ctx.committedNow()) saint.sound(SoundEvents.SHROOMLIGHT_BREAK, 0.9F, 0.7F);
				if (active && age % PutricielMove.VOLLEY_GAP == 0) {
					int index = age / PutricielMove.VOLLEY_GAP;
					List<Vec3> points = frame.points();
					if (index < points.size()) {
						Vec3 at = points.get(index);
						saint.soundAt(at, SoundEvents.MOSS_BREAK, 1.0F, 0.8F);
						// landings 0, 3 and 6 pool instead of leaving a colony; one that cannot pool leaves its colony
						boolean pooled = PutricielSludgeRules.volleyPool(index)
								&& saint.laySludge(Patch.disc(at, PutricielSludgeRules.VOLLEY_RADIUS)) != Outcome.SKIP;
						if (!pooled) {
							saint.spawnHazard(PutricielHazard.ROT_COLONY, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
									PutricielHazard.COLONY_RADIUS, BossFootprint.Height.STANDING), FootprintFrame.simple(at, at, at, frame.floorY()));
						}
					}
				}
			}
			case ROOT_SNARE -> {
				if (tick == 6) saint.sound(SoundEvents.ROOTED_DIRT_BREAK, 1.0F, 0.6F);
				if (ctx.committedNow()) {
					saint.soundAt(frame.aim(), SoundEvents.MANGROVE_ROOTS_BREAK, 1.2F, 0.7F);
					saint.raiseFaithful(frame.aim());
				}
				// the rot meant for the quarry pools at the cross when the Snare strikes no one
				if (active && age == lastActive && PutricielSludgeRules.snarePool(ctx.saint().controller().hitLanded())) {
					saint.laySludge(Patch.disc(frame.aim(), PutricielSludgeRules.SNARE_RADIUS));
				}
			}
			case CENSER_ORBIT -> {
				if (active && age % 20 == 0) saint.sound(SoundEvents.CHAIN_HIT, 1.0F, 0.8F);
			}
			case MOULD_COMMUNION -> {
				if (tick == 0) saint.sound(SoundInit.RITE_OFFERING_DRAWN.get(), 0.8F, 0.6F);
				if (active && age % COMMUNION_HEAL_EVERY == 0) saint.heal(saint.getMaxHealth() * COMMUNION_HEAL);
			}
			case BLOOM_OF_ROT -> {
				if (tick == 0) {
					saint.bloomBegins();
					saint.sound(SoundInit.RITE_AMBIENT_DRONE.get(), 1.0F, 0.5F);
				}
				if (active && age % PutricielMove.BLOOM_PULSE == 0) {
					saint.sound(SoundInit.ENTITY_MYCOPHANT_HURT_OTHER.get(), 1.2F, 0.6F);
					if (age == 0 && ctx.target() != null) saint.raiseFaithful(ctx.target().position());
					// the field drains into two pools at its last pulse
					if (age == PutricielMove.BLOOM_PULSE * 2) saint.drainBloom(frame);
				}
				if (active && age == lastActive) saint.reclaimAsh();
			}
			case IGNITION -> {
				if (tick == 0) saint.ignitionBegins();
				if (ctx.committedNow()) saint.ignite();
				if (active) saint.burnFaithful(move.footprint(ctx.inputs()), frame, age);
			}
			case PURGING_SWEEP -> {
				if (ctx.committedNow()) {
					saint.sound(SoundInit.ITEM_LIVING_TORCH_BREATH_LOOP.get(), 1.0F, 0.9F);
					saint.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.8F);
					saint.scorch(PutricielArenaRules.lineCells(frame.origin(), along(frame, 8.0D), 1.0D,
							PutricielArenaRules.SWEEP_SCORCH));
				}
				if (active && age == lastActive) {
					Vec3 end = along(frame, 8.0D);
					double[] f = BossFootprint.Shapes.forward(frame);
					Vec3 right = new Vec3(-f[1], 0.0D, f[0]).scale(PutricielHazard.WALL_LENGTH / 2.0D);
					Vec3 from = end.subtract(right), to = end.add(right);
					saint.spawnHazard(PutricielHazard.FIRE_WALL, new BossFootprint.Lane(PutricielHazard.WALL_LENGTH,
									PutricielHazard.WALL_HALF_WIDTH, 0, 0.0D, 0.0D, 0.0D, 0, PutricielMove.SWING_HEIGHT),
							FootprintFrame.simple(from, to, to, frame.floorY()));
				}
			}
			case CENSER_CAST -> {
				if (tick == 0) saint.sound(SoundEvents.CHAIN_FALL, 1.0F, 0.7F);
				if (ctx.committedNow()) {
					saint.soundAt(frame.aim(), SoundEvents.BLAZE_SHOOT, 1.1F, 0.7F);
					saint.burst(frame.aim().add(0.0D, 0.2D, 0.0D), PutricielVisualRules.FIRE_IMPACT, 2.5F);
					saint.scorch(PutricielArenaRules.discCells(frame.aim(), 2.5D, PutricielArenaRules.CAST_SCORCH));
					saint.spawnHazard(PutricielHazard.PURGING_FIRE, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
									PutricielHazard.FIRE_RADIUS, BossFootprint.Height.STANDING),
							FootprintFrame.simple(frame.aim(), frame.aim(), frame.aim(), frame.floorY()));
				}
			}
			case CAUTERISING_LANE -> {
				if (tick == 0) {
					Vec3 pyre = saint.cauterisingPyre();
					Vec3 body = saint.position();
					ctx.saint().controller().setPoints(List.of(pyre == null ? along(frame, 12.0D) : pyre, body));
				}
				if (ctx.committedNow()) {
					saint.sound(SoundEvents.FIRE_AMBIENT, 1.4F, 0.7F);
					saint.sound(SoundInit.ENTITY_STAINED_PRIESTESS_FLARE.get(), 0.8F, 0.6F);
					List<Vec3> points = frame.points();
					if (points.size() >= 2) {
						saint.scorch(PutricielArenaRules.lineCells(points.get(0), points.get(1), 1.5D,
								PutricielArenaRules.LANE_SCORCH));
					}
				}
			}
			case PYRE_HEART -> {
				if (ctx.committedNow()) {
					saint.sound(SoundInit.RITE_CULMINATION.get(), 1.2F, 0.7F);
					saint.burst(saint.position().add(0.0D, 1.2D, 0.0D), PutricielVisualRules.FIRE_IMPACT, 3.5F);
				}
				if (active) saint.burnFaithful(move.footprint(ctx.inputs()), frame, age);
			}
			case PYRE_LINES -> {
				if (tick == 0) {
					List<Vec3> pyres = saint.flaredPyres(ctx.inputs().variant());
					for (int i = 0; i < pyres.size(); i++) {
						ctx.saint().controller().setAnchor(i, pyres.get(i));
						saint.soundAt(pyres.get(i), SoundInit.MAUSOLEUM_PUTRICIEL_FLARE.get(), 1.0F, 1.2F);
					}
					saint.sound(SoundInit.RITE_WAVE_START.get(), 1.0F, 0.7F);
				}
				if (ctx.committedNow()) {
					LongArrayList cells = new LongArrayList();
					for (Vec3 anchor : frame.anchors()) {
						int left = PutricielArenaRules.LINES_SCORCH - cells.size();
						if (left <= 0) break;
						cells.addAll(PutricielArenaRules.lineCells(anchor, frame.origin(), 0.6D, Math.min(left, 4)));
					}
					saint.scorch(cells);
				}
			}
			case FULL_PURGE -> {
				if (tick == 0) saint.purgeBegins();
				if (active && age % 16 == 0 && age < 48) {
					int ring = age / 16;
					saint.sound(SoundInit.RITE_ESCALATE.get(), 1.2F, 0.7F + 0.1F * ring);
					saint.burst(saint.position().add(0.0D, 0.2D, 0.0D), PutricielVisualRules.IGNITE, 8.0F);
				}
				if (active) saint.burnFaithful(move.footprint(ctx.inputs()), frame, age);
			}
			case OFFERING -> {
				if (tick == 0) saint.offeringBegins();
			}
			case EMBERING -> {
				if (tick == 0) saint.emberingBegins();
			}
		}
	}

	static void hit(PutricielMove move, SaintMoveContext ctx, LivingEntity target) {
		if (!(ctx.saint() instanceof PutricielEntity saint)) return;
		boolean hurt = SaintMoves.hurt(ctx, target, move.baseDamage());
		Player player = target instanceof Player p ? p : null;
		switch (move) {
			case CENSER_SWING, CENSER_BACKSWING -> colonise(saint, player, 2.0F, hurt);
			case CENSER_DROP -> {
				if (hurt) {
					target.push(0.0D, DROP_TOSS, 0.0D);
					target.hurtMarked = true;
				}
				colonise(saint, player, 3.0F, hurt);
			}
			case OFFERING_GRASP -> {
				// through a raised shield the damage is blocked, the colonisation is not
				if (player != null) saint.colonise(player, 5.0F);
				Vec3 pull = saint.position().subtract(target.position());
				Vec3 flat = new Vec3(pull.x, 0.0D, pull.z);
				if (flat.lengthSqr() > 1.0E-4D) {
					target.setDeltaMovement(target.getDeltaMovement().add(flat.normalize().scale(GRASP_PULL)));
					target.hurtMarked = true;
				}
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, false, true));
			}
			case ROT_BREATH, CENSER_ORBIT -> colonise(saint, player, 1.0F, hurt);
			case SAC_SWELL -> {
				if (hurt) target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, false, true));
				colonise(saint, player, 3.0F, hurt);
			}
			case SHELF_VOLLEY -> colonise(saint, player, 2.0F, hurt);
			case ROOT_SNARE -> {
				if (hurt) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 3, false, false, true));
				colonise(saint, player, 2.0F, hurt);
			}
			case BLOOM_OF_ROT -> colonise(saint, player, 3.0F, hurt);
			case IGNITION -> fire(saint, target, player, 3, -6.0F, hurt);
			case PURGING_SWEEP -> fire(saint, target, player, 3, -2.0F, hurt);
			case CENSER_CAST, CAUTERISING_LANE -> fire(saint, target, player, 4, 0.0F, hurt);
			case PYRE_HEART -> fire(saint, target, player, 5, -10.0F, hurt);
			case PYRE_LINES -> fire(saint, target, player, 3, 0.0F, hurt);
			case FULL_PURGE -> fire(saint, target, player, 3, -3.0F, hurt);
			default -> {
			}
		}
		if (hurt) {
			saint.burst(target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D),
					move.fire() ? PutricielVisualRules.FIRE_IMPACT : PutricielVisualRules.ROT_IMPACT, 0.8F);
		}
	}

	private static void colonise(PutricielEntity saint, Player player, float amount, boolean hurt) {
		if (player != null && hurt) saint.colonise(player, amount);
	}

	private static void fire(PutricielEntity saint, LivingEntity target, Player player, int seconds, float cleanse,
			boolean hurt) {
		if (!hurt) return;
		CrimsonFireHelper.igniteCrimson(target, seconds);
		if (player != null && cleanse < 0.0F) saint.colonise(player, cleanse);
	}

	/** The point {@code distance} along the frame's aim from its origin. */
	static Vec3 along(FootprintFrame frame, double distance) {
		double[] f = BossFootprint.Shapes.forward(frame);
		return new Vec3(frame.origin().x + f[0] * distance, frame.origin().y, frame.origin().z + f[1] * distance);
	}

	/** One disc on the quarry, the rest scattered within 4 blocks along the way they move, led 10 ticks. */
	static List<Vec3> volleyPoints(SaintMoveContext ctx) {
		int count = PutricielMove.volleyDiscs(ctx.inputs());
		LivingEntity target = ctx.target();
		Vec3 at = target == null ? ctx.frame().aim() : target.position();
		Vec3 motion = target == null ? Vec3.ZERO
				: new Vec3(target.getX() - target.xo, 0.0D, target.getZ() - target.zo).scale(VOLLEY_LEAD);
		var random = ctx.saint().getRandom();
		List<Vec3> points = new ArrayList<>(count);
		points.add(at);
		for (int i = 1; i < count; i++) {
			double along = random.nextDouble();
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double spread = 1.5D + random.nextDouble() * (VOLLEY_SPREAD - 1.5D);
			Vec3 p = at.add(motion.scale(along)).add(Math.cos(angle) * spread, 0.0D, Math.sin(angle) * spread);
			points.add(ctx.arena().clamp(new Vec3(p.x, at.y, p.z)));
		}
		return points;
	}
}
