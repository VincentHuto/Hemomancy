package com.vincenthuto.hemomancy.common.manipulation.animus;

import static com.vincenthuto.hemomancy.common.manipulation.animus.HematicRiposteRules.*;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.damage.SchoolDamageSource;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Hematic Riposte's server parry window. A frontal melee hit inside the window is cancelled and its attacker stunned;
 * a frontal hostile projectile is swatted aside with its owner unchanged. Cancelling at NORMAL priority runs after
 * {@code SchoolCombatEvents.prepare} only records pending state, and before Sanguine Ward or Iron Hearts can spend
 * anything, so a parried hit builds and consumes nothing.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class HematicRiposteEvents {
	private static final Map<UUID, Riposte> RIPOSTES = new HashMap<>();
	private static final ParticleColor ANIMUS_RED = new ParticleColor(214, 18, 48);
	private static final ParticleColor HOT_CORE = new ParticleColor(255, 120, 110);

	private HematicRiposteEvents() {
	}

	public static void clearSessionState() {
		RIPOSTES.clear();
	}

	public static void arm(ServerPlayer player, int side, int tilt) {
		RIPOSTES.put(player.getUUID(), new Riposte(player.level().getGameTime(), side));
		// A keyed cue would only refresh while alive; retire it so every cast replays the full swing.
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.RIPOSTE, 0, 0, 0);
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.RIPOSTE, 1.0D, VISUAL_TICKS, encodeSeed(side, tilt));
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundInit.MANIPULATION_HEMATIC_RIPOSTE_EMERGE.get(), SoundSource.PLAYERS, 0.8F,
				0.9F + player.getRandom().nextFloat() * 0.2F);
	}

	/** True while the player's riposte window is open; exposed for fixtures and other reactive systems. */
	public static boolean isParrying(ServerPlayer player) {
		return active(player) != null;
	}

	private static Riposte active(ServerPlayer player) {
		Riposte riposte = RIPOSTES.get(player.getUUID());
		if (riposte == null) return null;
		long now = player.level().getGameTime();
		if (now - riposte.castTick > PARRY_CLOSE) {
			RIPOSTES.remove(player.getUUID());
			return null;
		}
		if (!inWindow(riposte.castTick, now) || !player.isAlive() || Paralysis.blocksActions(player)) return null;
		return riposte;
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
		Riposte riposte = active(player);
		if (riposte == null) return;
		DamageSource source = event.getSource();
		Entity direct = source.getDirectEntity();
		Entity causing = source.getEntity();
		boolean withinReach = direct != null && direct.distanceToSqr(player) <= MELEE_REACH * MELEE_REACH;
		Kind kind = classify(direct instanceof Projectile, direct != null && direct == causing && direct instanceof LivingEntity,
				excluded(source), withinReach);
		if (kind == Kind.MELEE) {
			LivingEntity attacker = (LivingEntity) direct;
			if (attacker == player || !facing(player, attacker.position().subtract(player.position()))) return;
			event.setCanceled(true);
			parryMelee(player, riposte, attacker);
		} else if (kind == Kind.PROJECTILE) {
			// Fallback for projectiles that never posted an impact event: negate, but leave their motion alone.
			Projectile shot = (Projectile) direct;
			if (!ManipulationCombatHelper.hostileProjectile(player, shot) || !facing(player, incoming(player, shot))) return;
			event.setCanceled(true);
			if (riposte.swatted.add(shot.getId())) {
				feedback(player, riposte, shot.position(), shot.getDeltaMovement().scale(-1), true);
				if (damaging(shot)) refund(player, riposte);
			}
		}
	}

	@SubscribeEvent
	public static void onProjectileImpact(ProjectileImpactEvent event) {
		Projectile shot = event.getProjectile();
		if (shot.level().isClientSide || !(event.getRayTraceResult() instanceof EntityHitResult hit)
				|| !(hit.getEntity() instanceof ServerPlayer player)) return;
		Riposte riposte = active(player);
		if (riposte == null) return;
		if (riposte.swatted.contains(shot.getId())) {
			event.setCanceled(true);
			return;
		}
		if (!ManipulationCombatHelper.hostileProjectile(player, shot) || !facing(player, incoming(player, shot))) return;
		event.setCanceled(true);
		riposte.swatted.add(shot.getId());
		double[] d = swatDirection(player.yBodyRot, riposte.side);
		Vec3 away = new Vec3(d[0], d[1], d[2]);
		// Redirect in place: a cancelled impact keeps flying on this velocity, and the owner never changes hands.
		double speed = Math.max(SWAT_MIN_SPEED, shot.getDeltaMovement().length());
		shot.setDeltaMovement(away.scale(speed));
		shot.hasImpulse = true;
		feedback(player, riposte, shot.position(), away, true);
		if (damaging(shot)) refund(player, riposte);
	}

	private static void parryMelee(ServerPlayer player, Riposte riposte, LivingEntity attacker) {
		if (ManipulationCombatHelper.canHarm(player, attacker) && riposte.stunned.add(attacker.getId())) {
			attacker.knockback(KNOCKBACK, player.getX() - attacker.getX(), player.getZ() - attacker.getZ());
			if (attacker instanceof ServerPlayer struck) struck.hurtMarked = true;
			// Paralysis would mark bosses Conductive instead; a parry only staggers them.
			if (!ManipulationReactiveEvents.isBoss(attacker))
				Paralysis.apply(attacker, attacker instanceof Player ? STUN_PLAYER : STUN_MOB);
			ManipulationParticles.impact(attacker, EnumBloodTendency.ANIMUS, EnumBloodTendency.DUCTILIS,
					attacker.position().subtract(player.position()));
		}
		Vec3 toward = attacker.getEyePosition().subtract(player.getEyePosition());
		Vec3 dir = toward.lengthSqr() < 1.0E-6D ? player.getLookAngle() : toward.normalize();
		Vec3 contact = player.getEyePosition().add(0, -0.35D, 0).add(dir.scale(0.7D));
		feedback(player, riposte, contact, dir, false);
		refund(player, riposte);
	}

	private static void feedback(ServerPlayer player, Riposte riposte, Vec3 contact, Vec3 direction, boolean projectile) {
		ServerLevel level = player.serverLevel();
		Vec3 dir = direction.lengthSqr() < 1.0E-6D ? player.getLookAngle() : direction.normalize();
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.RIPOSTE_STRIKE, contact, contact.add(dir),
				projectile ? 0.8D : 1.0D, 10, projectile ? 2 : 1);
		if (!projectile)
			PacketHandler.sendClawSlash(contact, dir, ANIMUS_RED, riposte.side < 0, 0.65F, 48, level);
		level.sendParticles(HemoParticleData.hitGlow(HOT_CORE), contact.x, contact.y, contact.z,
				projectile ? 4 : 6, 0.15D, 0.15D, 0.15D, 0.02D);
		level.sendParticles(HemoParticleData.bloodCell(ANIMUS_RED), contact.x, contact.y, contact.z,
				projectile ? 6 : 10, 0.25D, 0.2D, 0.25D, 0.06D);
		long now = level.getGameTime();
		if (riposte.lastSoundTick == now) return;
		riposte.lastSoundTick = now;
		if (projectile) {
			level.playSound(null, contact.x, contact.y, contact.z, SoundInit.MANIPULATION_HEMATIC_RIPOSTE_SWAT.get(),
					SoundSource.PLAYERS, 0.9F, 1.0F);
			level.playSound(null, contact.x, contact.y, contact.z, SoundInit.MANIPULATION_HEMATIC_RIPOSTE_PARRY.get(),
					SoundSource.PLAYERS, 0.5F, 1.1F);
		} else {
			level.playSound(null, contact.x, contact.y, contact.z, SoundInit.MANIPULATION_HEMATIC_RIPOSTE_PARRY.get(),
					SoundSource.PLAYERS, 1.0F, 1.0F);
			level.playSound(null, contact.x, contact.y, contact.z, SoundEvents.PLAYER_ATTACK_STRONG,
					SoundSource.PLAYERS, 0.9F, 1.3F);
		}
	}

	private static void refund(ServerPlayer player, Riposte riposte) {
		if (riposte.refunded) return;
		riposte.refunded = true;
		ManipulationInit.hematic_riposte.get().capCooldown(player, REFUND_COOLDOWN);
	}

	private static boolean facing(ServerPlayer player, Vec3 towardSource) {
		return isFrontal(player.yBodyRot, towardSource.x, towardSource.z);
	}

	/** Direction from the player back toward where a projectile came from. */
	private static Vec3 incoming(ServerPlayer player, Projectile shot) {
		Vec3 motion = shot.getDeltaMovement();
		return motion.horizontalDistanceSqr() > 1.0E-4D ? motion.scale(-1) : shot.position().subtract(player.position());
	}

	private static boolean excluded(DamageSource source) {
		if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypes.THORNS)
				|| source.is(DamageTypeTags.WITCH_RESISTANT_TO) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
			return true;
		return source instanceof SchoolDamageSource school && school.context().kind() != SchoolHitContext.Kind.DIRECT;
	}

	private static boolean damaging(Projectile shot) {
		return shot instanceof AbstractArrow || shot instanceof AbstractHurtingProjectile || shot instanceof ShulkerBullet
				|| shot instanceof LlamaSpit || shot instanceof ThrownPotion;
	}

	private static void clear(Entity entity) {
		RIPOSTES.remove(entity.getUUID());
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		clear(event.getEntity());
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		clear(event.getEntity());
	}

	@SubscribeEvent
	public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		clear(event.getEntity());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) clear(player);
	}

	private static final class Riposte {
		final long castTick;
		final int side;
		final Set<Integer> stunned = new HashSet<>();
		final Set<Integer> swatted = new HashSet<>();
		boolean refunded;
		long lastSoundTick = Long.MIN_VALUE;

		Riposte(long castTick, int side) {
			this.castTick = castTick;
			this.side = side;
		}
	}
}
