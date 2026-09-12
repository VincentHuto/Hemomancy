package com.vincenthuto.hemomancy.common.damage;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.CombatWeaponCarrierProjectile;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.TendencyWeaponHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.TendencyDamageCarrier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

public final class SchoolDamage {
    private static final String PROJECTILE_CONTEXT = "hemomancy:school_hit";
    private static final ThreadLocal<SchoolHitContext> ACTIVE = new ThreadLocal<>();
    private static final ThreadLocal<LivingEntity> ACTIVE_OWNER = new ThreadLocal<>();
    private record Swing(long tick, SchoolHitContext hit) {}
    private static final Map<LivingEntity, Swing> SWINGS = new WeakHashMap<>();

    private SchoolDamage() {}

    public static final class Scope implements AutoCloseable {
        private final SchoolHitContext previous;
        private final LivingEntity previousOwner;
        private Scope(SchoolHitContext context, LivingEntity owner) {
            previous = ACTIVE.get(); previousOwner = ACTIVE_OWNER.get();
            ACTIVE.set(context);
            if (owner != null) ACTIVE_OWNER.set(owner);
        }
        @Override public void close() {
            if (previous == null) ACTIVE.remove(); else ACTIVE.set(previous);
            if (previousOwner == null) ACTIVE_OWNER.remove(); else ACTIVE_OWNER.set(previousOwner);
        }
    }

    public static Scope scope(SchoolHitContext context) { return new Scope(context, null); }
    public static Scope scope(SchoolHitContext context, LivingEntity owner) { return new Scope(context, owner); }
    public static Scope cast(BloodManipulation manipulation, LivingEntity caster, float charge) {
        if (caster instanceof net.minecraft.server.level.ServerPlayer player
                && !manipulation.getName().equals("sanguine_marionette")
                && com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager.isMarionetteChannel(player))
            com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.stop(player, false);
        return new Scope(context(manipulation, caster).withCharge(charge), caster);
    }
    @Nullable public static SchoolHitContext current() { return ACTIVE.get(); }

    public static SchoolHitContext context(BloodManipulation manipulation, @Nullable LivingEntity caster) {
        SchoolHitContext active = current();
        if (active != null && active.ability().getPath().equals(manipulation.getName())
                && (caster == null || caster.getUUID().equals(active.owner()))) return active;
        return SchoolHitContext.direct(Hemomancy.rloc(manipulation.getName()), manipulation.getTend(),
                manipulation.getSecondaryTend(), caster);
    }

    public static void captureSwing(Player player) {
        SchoolHitContext hit = weaponContext(player, player.getMainHandItem());
        if (hit != null) SWINGS.put(player, new Swing(player.level().getGameTime(),
                hit.withCharge(player.getAttackStrengthScale(0.5f))));
    }

    @Nullable public static SchoolHitContext weaponContext(LivingEntity owner, ItemStack weapon) {
        SchoolHitContext origin = projectileContext(owner);
        if (origin != null && !(owner instanceof Player)) return origin.newAttack();
        var primary = owner instanceof WillEntity will ? will.getSchool()
                : TendencyWeaponHelper.getWeaponTendency(weapon).orElse(null);
        if (primary == null) return null;
        var ability = owner instanceof WillEntity ? Hemomancy.rloc("will_melee")
                : BuiltInRegistries.ITEM.getKey(weapon.getItem());
        return SchoolHitContext.direct(ability, primary,
                TendencyWeaponHelper.getWeaponSecondaryTendency(weapon).orElse(null), owner);
    }

    /** Called at launch, while the paid cast's scope and the launch weapon still exist. */
    public static void captureProjectile(Projectile projectile) {
        if (projectile.getPersistentData().contains(PROJECTILE_CONTEXT)) return;
        SchoolHitContext hit = current();
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity living ? living : null;
        if (hit == null && projectile instanceof TendencyDamageCarrier carrier && carrier.getDamageTendency() != null) {
            hit = SchoolHitContext.direct(BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType()),
                    carrier.getDamageTendency(), carrier.getSecondaryDamageTendency(), owner);
        }
        if (hit == null && owner != null) {
            ItemStack weapon = projectile instanceof CombatWeaponCarrierProjectile carrier ? carrier.getCombatWeaponItem()
                    : projectile instanceof AbstractArrow arrow && arrow.getWeaponItem() != null
                    ? arrow.getWeaponItem() : ItemStack.EMPTY;
            hit = weaponContext(owner, weapon);
        }
        if (hit != null) projectile.getPersistentData().put(PROJECTILE_CONTEXT, hit.withDirect(projectile).save());
    }

    public static void capture(Entity entity, SchoolHitContext hit) {
        if (hit != null) entity.getPersistentData().put(PROJECTILE_CONTEXT, hit.withDirect(entity).save());
    }

    public static void captureSpawn(Entity entity) {
        if (entity instanceof Projectile projectile) captureProjectile(projectile);
        else if (current() != null && !entity.getPersistentData().contains(PROJECTILE_CONTEXT))
            capture(entity, current());
    }

    /** Custom collision code must retain the actual projectile, even for a native melee damage type. */
    public static DamageSource projectileSource(DamageSource original, Entity projectile, @Nullable Entity owner) {
        DamageSource nativeSource = new DamageSource(original.typeHolder(), projectile, owner, original.sourcePositionRaw());
        SchoolHitContext hit = projectileContext(projectile);
        return hit == null ? nativeSource : attributed(nativeSource, hit, owner);
    }

    @Nullable public static SchoolHitContext projectileContext(Entity entity) {
        return entity == null ? null : SchoolHitContext.load(entity.getPersistentData().getCompound(PROJECTILE_CONTEXT));
    }

    @Nullable public static Entity owner(LivingEntity victim, SchoolHitContext context) {
        LivingEntity active = ACTIVE_OWNER.get();
        if (active != null && active.getUUID().equals(context.owner())) return active;
        return context.owner() != null && victim.level() instanceof ServerLevel level ? level.getEntity(context.owner()) : null;
    }

    public static DamageSource attributed(DamageSource original, SchoolHitContext hit, @Nullable Entity owner) {
        return new SchoolDamageSource(original, hit.withDirect(original.getDirectEntity()), owner);
    }

    /** Preserve the native damage type; only authored school metadata opts a hit in. */
    public static DamageSource wrap(LivingEntity victim, DamageSource original) {
        if (original instanceof SchoolDamageSource || victim.level().isClientSide) return original;
        // A living summon starts a new attack; its spawn identity is only the school/owner template.
        SchoolHitContext hit = original.getDirectEntity() instanceof LivingEntity ? null
                : projectileContext(original.getDirectEntity());
        if (hit == null) hit = current();
        if (hit == null && (original.is(DamageTypes.PLAYER_ATTACK) || original.is(DamageTypes.MOB_ATTACK))
                && original.getEntity() instanceof LivingEntity attacker) {
            Swing swing = SWINGS.get(attacker);
            if (swing != null && swing.tick == attacker.level().getGameTime()) hit = swing.hit;
            if (hit == null) {
                hit = weaponContext(attacker, attacker.getMainHandItem());
                if (hit != null) SWINGS.put(attacker, new Swing(attacker.level().getGameTime(), hit));
            }
        }
        return hit == null ? original : attributed(original, hit, owner(victim, hit));
    }

    public static boolean hurt(LivingEntity target, DamageSource source, float damage, int duration, int levels) {
        SchoolHitContext hit = current();
        if (hit == null) hit = projectileContext(source.getDirectEntity());
        return target.hurt(hit == null ? source : attributed(source, hit.withApplication(duration, levels),
                owner(target, hit)), damage);
    }

    private static final String RETORT_CONTEXT = "hemomancy:iron_retort_hit";

    public static void captureRetort(LivingEntity entity) {
        entity.getPersistentData().put(RETORT_CONTEXT,
                context(com.vincenthuto.hemomancy.common.init.ManipulationInit.iron_retort.get(), entity).save());
    }

    public static SchoolHitContext retortContext(LivingEntity entity) {
        SchoolHitContext hit = SchoolHitContext.load(entity.getPersistentData().getCompound(RETORT_CONTEXT));
        return hit == null ? context(com.vincenthuto.hemomancy.common.init.ManipulationInit.iron_retort.get(), entity) : hit;
    }

    public static void clearRetort(LivingEntity entity) { entity.getPersistentData().remove(RETORT_CONTEXT); }

    public static DamageSource reaction(LivingEntity owner, String ability,
            com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency school) {
        return reaction(owner, SchoolHitContext.direct(Hemomancy.rloc(ability), school, null, owner));
    }

    public static DamageSource reaction(LivingEntity owner, SchoolHitContext hit) {
        SchoolHitContext parent = current();
        if (parent != null) hit = new SchoolHitContext(hit.ability(), hit.primary(), hit.secondary(), hit.owner(),
                hit.direct(), parent.rootAttack(), hit.kind(), hit.role(), hit.charge(), hit.duration(), hit.levels());
        return attributed(owner.damageSources().magic(), hit.child(SchoolHitContext.Kind.REACTION), owner);
    }
}
