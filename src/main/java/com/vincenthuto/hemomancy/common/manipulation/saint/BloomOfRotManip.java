package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolDamageSource;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRoot;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielPhase;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Bloom of Rot, Canon Memory of Putriciel, the Befouled Saint (D-1 spec 3.3). The caster kneels and sows the rot round
 * them: every body standing in it is colonised (Rooted, Shelved, Overgrown), the caster too at half rate, and the field
 * stirs Kindling. The second press, once Kindling reaches 40 (or by itself at 100), runs the purifier's fire out across
 * the field and burns the colonisation off. A field left unlit reclaims its caster (NQ-08-8). Player only (NQ-08-5).
 * Imprinted, not learned: no Dynamic Use reduction.
 */
public class BloomOfRotManip extends BloodManipulation {
	private static final ResourceLocation KNEEL = Hemomancy.rloc("bloom_of_rot_kneel");
	private static final ResourceLocation ROOT = Hemomancy.rloc("bloom_of_rot_root");

	public BloomOfRotManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTemper temper,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, temper, section);
	}

	/**
	 * While a field lives the press is the Ignition: free, never a second cooldown, and refused below 40 Kindling. It is
	 * answered here, before any price, so it works from the key and the radial alike.
	 */
	@Override
	protected boolean canPerformAction(Player player, float chargeTicks) {
		if (!super.canPerformAction(player, chargeTicks)) return false;
		CanonGiftState.BloomField field = CanonGiftState.bloom(player.getUUID());
		if (field == null) {
			if (!theirInterval(player)) return true;
			player.displayClientMessage(Component.translatable("message.hemomancy.bloom_of_rot.theirs")
					.withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
			return false;
		}
		if (player instanceof ServerPlayer caster && !field.lit()) {
			if (BloomOfRotRules.canIgnite(field.kindling)) ignite(caster, field);
			else caster.displayClientMessage(Component.translatable("message.hemomancy.bloom_of_rot.not_kindled",
					(int) BloomOfRotRules.IGNITE_AT).withStyle(ChatFormatting.DARK_GREEN), true);
		}
		return false;
	}

	/** No cooldown runs while the field lives: it starts from the field's end, and the Ignition is never stopped by it. */
	@Override
	public boolean ignoresCooldown(Player player) {
		return CanonGiftState.bloom(player.getUUID()) != null;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(player instanceof ServerPlayer caster) || !(world instanceof ServerLevel level)) return;
		Vec3 centre = new Vec3(caster.getX(), Math.floor(caster.getY() + 0.01D), caster.getZ());
		CanonGiftState.BloomField field = new CanonGiftState.BloomField(centre, level.getGameTime(), level.dimension());
		CanonGiftState.sow(caster.getUUID(), field);
		caster.displayClientMessage(Component.translatable("message.hemomancy.bloom_of_rot.sown")
				.withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC), true);
		level.playSound(null, caster.blockPosition(), SoundInit.RITE_AMBIENT_DRONE.get(), SoundSource.PLAYERS, 1.0F, 0.5F);
		show(caster, field);
	}

	/** One server tick of the caster's field. Called by {@link CanonGiftEvents}. */
	static void tick(ServerPlayer caster) {
		CanonGiftState.BloomField field = CanonGiftState.bloom(caster.getUUID());
		ServerLevel level = caster.serverLevel();
		long now = level.getGameTime();
		AttributeModifiers.sync(caster.getAttribute(Attributes.MOVEMENT_SPEED), KNEEL, -1.0D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
				field != null && !field.lit() && now - field.sown < BloomOfRotRules.KNEEL_TICKS);
		if (field == null || !level.dimension().equals(field.dimension)) return;
		if (!field.lit() && theirInterval(caster)) {
			// their fire comes: the field will not take in Putriciel's interval (NQ-08-11)
			caster.displayClientMessage(Component.translatable("message.hemomancy.bloom_of_rot.theirs")
					.withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
			close(caster, field, false);
			return;
		}
		BloomOfRotManip bloom = (BloomOfRotManip) ManipulationInit.bloom_of_rot.get();
		long age = now - field.sown;
		if (field.lit()) {
			bloom.burn(caster, level, field, now);
			if (now - field.ignitedAt >= BloomOfRotRules.CHAR_TICKS) close(caster, field, false);
			return;
		}
		if (age > 0 && age % BloomOfRotRules.KINDLING_TICKS == 0) kindle(caster, field, 1.0D);
		if (age > 0 && age % BloomOfRotRules.PULSE_TICKS == 0) {
			try (var scope = SchoolDamage.cast(bloom, caster, 1)) {
				bloom.rot(caster, level, field, now);
			}
		}
		if (onField(field, caster, now) && now - field.lastCasterGain >= BloomOfRotRules.CASTER_TICKS) {
			field.lastCasterGain = now;
			colonise(caster, caster, 1, now, true);
		}
		if (BloomOfRotRules.ignitesItself(field.kindling)) {
			ignite(caster, field);
			return;
		}
		if (age >= BloomOfRotRules.FIELD_TICKS) {
			close(caster, field, true);
			return;
		}
		if (age % 10 == 0) show(caster, field);
	}

	/** One rot pulse: each harmable body on the field takes the rot and is colonised. */
	private void rot(ServerPlayer caster, ServerLevel level, CanonGiftState.BloomField field, long now) {
		double radius = BloomOfRotRules.radius(now - field.sown);
		for (LivingEntity body : level.getEntitiesOfClass(LivingEntity.class, area(field, radius),
				body -> body != caster && body.isAlive() && !body.isSpectator() && onField(field, body, now))) {
			if (!ManipulationCombatHelper.canHarm(caster, body)) continue;
			CanonGiftState.Colony colony = CanonGiftState.colonies().get(body.getUUID());
			boolean still = colony != null && colony.lastPosition.distanceTo(body.position()) < BloomOfRotRules.STILL_DISTANCE;
			boolean first = field.touched.add(body.getUUID());
			// a slow rot, not a blow: it ignores the hurt cooldown so every pulse lands
			body.invulnerableTime = 0;
			SchoolHitContext hit = context(caster).newAttack();
			strike(caster, body, first ? hit : hit.child(SchoolHitContext.Kind.PERIODIC), BloomOfRotRules.ROT_DAMAGE
					* (float) SkillPointHelper.getCrimsonMasteryMultiplier(caster));
			if (!body.isAlive()) continue;
			int gain = BloomOfRotRules.gain(still, SchoolStates.has(body, SchoolState.RIME),
					SchoolStates.has(body, SchoolState.PRESSURE));
			colonise(caster, body, gain, now, false);
		}
		level.playSound(null, BlockPos.containing(field.centre), SoundInit.ENTITY_MYCOPHANT_HURT_OTHER.get(), SoundSource.PLAYERS,
				1.0F, 0.6F);
	}

	/**
	 * Lays {@code gain} colonisation on {@code body} and answers the stage it reaches: Rooted slows, Shelved holds the
	 * Necrosis, Overgrown roots and hurts and falls back to 6. A boss gains colonisation but is never rooted.
	 */
	static void colonise(ServerPlayer caster, LivingEntity body, int gain, long now, boolean self) {
		CanonGiftState.Colony colony = CanonGiftState.colonies().computeIfAbsent(body.getUUID(),
				ignored -> new CanonGiftState.Colony(caster.getUUID(), body.position(), now));
		int before = colony.value;
		colony.value = BloomOfRotRules.colonise(before, gain);
		colony.caster = caster.getUUID();
		colony.lastPosition = body.position();
		colony.lastField = now;
		colony.lastDecay = now;
		colony.dimension = body.level().dimension();
		int stage = BloomOfRotRules.stage(colony.value);
		if (stage > BloomOfRotRules.stage(before) && body.level() instanceof ServerLevel level)
			level.playSound(null, body.blockPosition(), SoundEvents.FUNGUS_PLACE, SoundSource.PLAYERS, 0.6F, 0.9F + 0.1F * stage);
		boolean boss = HemoEntityPredicates.isBoss(body);
		if (stage >= 1 && !boss)
			body.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BloomOfRotRules.STAGE_HOLD, 0, false, true, true));
		if (stage >= 2)
			SchoolStates.apply(caster, body, SchoolState.NECROSIS, SchoolState.NECROSIS.duration);
		if (stage >= 3) {
			colony.value = BloomOfRotRules.AFTER_OVERGROWN;
			int root = self ? BloomOfRotRules.CASTER_ROOT : BloomOfRotRules.OVERGROWN_ROOT;
			if (!boss) {
				if (body instanceof Player player) SaintRoot.root(player, root, ROOT, false);
				else Paralysis.apply(body, root);
			}
			float damage = self ? BloomOfRotRules.CASTER_OVERGROWN_DAMAGE : BloomOfRotRules.OVERGROWN_DAMAGE;
			body.invulnerableTime = 0;
			if (self) body.hurt(body.damageSources().magic(), damage);
			else ((BloomOfRotManip) ManipulationInit.bloom_of_rot.get()).strike(caster, body,
					context(caster).newAttack().child(SchoolHitContext.Kind.PERIODIC),
					damage * (float) SkillPointHelper.getCrimsonMasteryMultiplier(caster));
		}
		showColony(body, colony);
	}

	/** The second press, or a field at 100 Kindling: the fire runs out from the caster across the field. */
	static void ignite(ServerPlayer caster, CanonGiftState.BloomField field) {
		ServerLevel level = caster.serverLevel();
		long now = level.getGameTime();
		field.ignitedAt = now;
		field.fullFire = BloomOfRotRules.ignitesItself(field.kindling);
		CanonGiftState.Colony own = CanonGiftState.colonies().remove(caster.getUUID());
		if (own != null) ManipulationVisuals.attached(caster, ManipulationVisuals.Form.BLOOM_COLONY, 0, 0, 0);
		ManipulationVisuals.attached(caster, ManipulationVisuals.Form.BLOOM_FIELD, 0, 0, 0);
		double radius = BloomOfRotRules.radius(now - field.sown);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.BLOOM_IGNITION, field.centre, caster.position(), radius,
				BloomOfRotRules.ASH_END, field.fullFire ? 2 : 1);
		level.playSound(null, BlockPos.containing(field.centre), SoundInit.MAUSOLEUM_PUTRICIEL_CRACKLE.get(), SoundSource.PLAYERS,
				1.4F, 0.8F);
		level.playSound(null, BlockPos.containing(field.centre), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.2F, 0.6F);
		caster.displayClientMessage(Component.translatable("message.hemomancy.bloom_of_rot.ignition")
				.withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
	}

	/** Each tick of the Ignition: the fire catches every colonised or standing body it has reached, once. */
	private void burn(ServerPlayer caster, ServerLevel level, CanonGiftState.BloomField field, long now) {
		long age = now - field.ignitedAt;
		if (age > BloomOfRotRules.IGNITION_SPREAD) return;
		double radius = BloomOfRotRules.radius(field.ignitedAt - field.sown);
		double reach = BloomOfRotRules.fireReach(age, radius);
		float mastery = (float) SkillPointHelper.getCrimsonMasteryMultiplier(caster);
		try (var scope = SchoolDamage.cast(this, caster, 1)) {
			for (LivingEntity body : level.getEntitiesOfClass(LivingEntity.class, area(field, reach),
					body -> body != caster && body.isAlive() && !body.isSpectator() && !field.burned.contains(body.getUUID()))) {
				Vec3 offset = body.position().subtract(field.centre);
				if (!BloomOfRotRules.onField(Math.sqrt(offset.x * offset.x + offset.z * offset.z), offset.y, reach)) continue;
				if (!ManipulationCombatHelper.canHarm(caster, body)) continue;
				field.burned.add(body.getUUID());
				CanonGiftState.Colony colony = CanonGiftState.colonies().remove(body.getUUID());
				int colonisation = colony == null ? 0 : colony.value;
				body.invulnerableTime = 0;
				if (strike(caster, body, fire(caster).newAttack(), BloomOfRotRules.ignitionDamage(colonisation, field.kindling) * mastery))
					SchoolStates.apply(caster, body, SchoolState.SEARING, SchoolState.SEARING.duration);
				if (colony != null) ManipulationVisuals.attached(body, ManipulationVisuals.Form.BLOOM_COLONY, 0, 0, 0);
				ManipulationVisuals.burst(level, ManipulationVisuals.Form.CAST_IMPACT, body.position(), field.centre, 1.0D, 16);
			}
		}
	}

	/** The field ends: charred out after its Ignition, or unlit, when the rot reclaims the caster. */
	private static void close(ServerPlayer caster, CanonGiftState.BloomField field, boolean unlit) {
		CanonGiftState.closeBloom(caster.getUUID());
		ManipulationVisuals.attached(caster, ManipulationVisuals.Form.BLOOM_FIELD, 0, 0, 0);
		ServerLevel level = caster.serverLevel();
		CanonGiftState.colonies().entrySet().removeIf(entry -> {
			if (entry.getKey().equals(caster.getUUID()) || !entry.getValue().caster.equals(caster.getUUID())) return false;
			if (level.getEntity(entry.getKey()) instanceof LivingEntity body)
				ManipulationVisuals.attached(body, ManipulationVisuals.Form.BLOOM_COLONY, 0, 0, 0);
			return true;
		});
		if (unlit) {
			level.playSound(null, caster.blockPosition(), SoundEvents.ROOTED_DIRT_PLACE, SoundSource.PLAYERS, 1.0F, 0.5F);
			SchoolStates.apply(caster, caster, SchoolState.NECROSIS, BloomOfRotRules.RECLAIM_NECROSIS);
			colonise(caster, caster, BloomOfRotRules.RECLAIM_COLONISATION, level.getGameTime(), true);
			caster.displayClientMessage(Component.translatable("message.hemomancy.bloom_of_rot.reclaimed")
					.withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC), true);
		}
		ManipulationInit.bloom_of_rot.get().restartCooldown(caster);
	}

	/** A caster's body on their own field counts its gain; a body anywhere on it is in the rot. */
	private static boolean onField(CanonGiftState.BloomField field, LivingEntity body, long now) {
		if (body.isInWaterOrBubble() || body.isInLava() || !body.level().dimension().equals(field.dimension)) return false;
		Vec3 offset = body.position().subtract(field.centre);
		return BloomOfRotRules.onField(Math.sqrt(offset.x * offset.x + offset.z * offset.z), offset.y,
				BloomOfRotRules.radius(now - field.sown));
	}

	private static AABB area(CanonGiftState.BloomField field, double radius) {
		return new AABB(field.centre, field.centre).inflate(radius + 0.5D, BloomOfRotRules.FLOOR_DEPTH + 1.0D, radius + 0.5D);
	}

	/**
	 * Damage anything takes on a living, unlit field stirs its Kindling: one for each 3, two and a half times that for
	 * Flammeus or fire. Called by {@link CanonGiftEvents} for each landed hit.
	 */
	static void stir(LivingEntity body, DamageSource source, float damage) {
		if (damage <= 0.0F || CanonGiftState.fields().isEmpty()) return;
		long now = body.level().getGameTime();
		boolean flammeus = source.is(DamageTypeTags.IS_FIRE) || source instanceof SchoolDamageSource school
				&& (school.context().primary() == EnumBloodTemper.FLAMMEUS);
		for (var entry : CanonGiftState.fields().entrySet()) {
			CanonGiftState.BloomField field = entry.getValue();
			if (field.lit() || !onField(field, body, now)) continue;
			double add = BloomOfRotRules.kindlingFromDamage(damage, flammeus);
			if (body.level().getPlayerByUUID(entry.getKey()) instanceof ServerPlayer caster) kindle(caster, field, add);
			else field.kindling = BloomOfRotRules.kindle(field.kindling, add);
		}
	}

	/**
	 * Raises the field's Kindling. In Putriciel's own fight, in a rot phase, the field's Kindling feeds theirs one for
	 * one (NQ-08-11).
	 */
	private static void kindle(ServerPlayer caster, CanonGiftState.BloomField field, double add) {
		double before = field.kindling;
		field.kindling = BloomOfRotRules.kindle(before, add);
		double gained = field.kindling - before;
		if (gained <= 0.0D) return;
		PutricielEntity saint = CanonGifts.fightOf(caster, PutricielEntity.class);
		if (saint != null && saint.currentPhase() != PutricielPhase.INTERVAL) saint.meter().add((float) gained, caster);
	}

	/** Putriciel's interval: the fire is theirs, and a field will not take. */
	private static boolean theirInterval(Player player) {
		PutricielEntity saint = CanonGifts.fightOf(player, PutricielEntity.class);
		return saint != null && saint.currentPhase() == PutricielPhase.INTERVAL;
	}

	/** Colonisation off every field falls by one each 40 ticks, and goes with its body. Called by {@link CanonGiftEvents}. */
	static boolean decay(LivingEntity body, CanonGiftState.Colony colony, long now) {
		if (now - colony.lastField < BloomOfRotRules.DECAY_TICKS || now - colony.lastDecay < BloomOfRotRules.DECAY_TICKS) return false;
		colony.lastDecay = now;
		colony.value--;
		if (colony.value <= 0) {
			ManipulationVisuals.attached(body, ManipulationVisuals.Form.BLOOM_COLONY, 0, 0, 0);
			return true;
		}
		showColony(body, colony);
		return false;
	}

	/** Colonisation a field laid on {@code body}, for tests and the gift events; 0 when none. */
	public static int colonisationOf(LivingEntity body) {
		CanonGiftState.Colony colony = CanonGiftState.colonies().get(body.getUUID());
		return colony == null ? 0 : colony.value;
	}

	private static void show(ServerPlayer caster, CanonGiftState.BloomField field) {
		if (field.kindling > 75.0D && !field.creaked) {
			field.creaked = true;
			caster.serverLevel().playSound(null, BlockPos.containing(field.centre), SoundInit.MAUSOLEUM_PUTRICIEL_CREAK.get(),
					SoundSource.PLAYERS, 1.0F, 0.7F);
		}
		ManipulationVisuals.attached(caster, ManipulationVisuals.Form.BLOOM_FIELD, field.centre,
				field.kindling / BloomOfRotRules.MAX_KINDLING, 20, 1);
	}

	private static void showColony(LivingEntity body, CanonGiftState.Colony colony) {
		ManipulationVisuals.attached(body, ManipulationVisuals.Form.BLOOM_COLONY,
				colony.value / (double) BloomOfRotRules.MAX_COLONISATION, 80, 1 + BloomOfRotRules.stage(colony.value));
	}

	private static SchoolHitContext context(ServerPlayer caster) {
		return SchoolHitContext.direct(Hemomancy.rloc("bloom_of_rot"), EnumBloodTemper.MORTEM, EnumBloodTemper.FLAMMEUS, caster);
	}

	/** The Ignition: Flammeus cleansing fire that releases a body's banked Necrosis as its one payoff. */
	private static SchoolHitContext fire(ServerPlayer caster) {
		return SchoolHitContext.direct(Hemomancy.rloc("bloom_of_rot"), EnumBloodTemper.FLAMMEUS, EnumBloodTemper.MORTEM, caster);
	}

	private boolean strike(ServerPlayer caster, LivingEntity target, SchoolHitContext hit, float amount) {
		if (!ManipulationCombatHelper.canHarm(caster, target)) return false;
		return ManipulationParticles.hurt(this, target,
				SchoolDamage.attributed(caster.serverLevel().damageSources().magic(), hit, caster), amount);
	}
}
