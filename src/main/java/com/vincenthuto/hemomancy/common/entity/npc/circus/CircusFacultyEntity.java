package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/** Faculty outside the original pavilion combat cast. */
public final class CircusFacultyEntity extends CircusPerformerEntity {
    public CircusFacultyEntity(EntityType<? extends CircusFacultyEntity> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.MOVEMENT_SPEED, .25)
                .add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.FOLLOW_RANGE, 24);
    }
    @Override protected String roleId() { return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath(); }
    @Override protected String texturePath() { return "textures/entity/npc/harbinger/circus/" + roleId() + "_0.png"; }
    @Override public boolean participatesInFinale() { return false; }
    @Override protected int performanceDuration() { return 100; }
    @Override protected void tickPerformance(int tick) {
        if (tick % 20 == 0) swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }
    private int attackCooldown;

    /** Every 30 ticks within two blocks: the arm comes up through the melee wind-up, then the blow lands if still in reach. */
    @Override protected void tickDefense(LivingEntity target) {
        getNavigation().moveTo(target, 1);
        if (attackCooldown-- > 0 || distanceToSqr(target) >= 4) return;
        attackCooldown = 29 - defenseMove().total();
        getNavigation().stop();
        beginDefenseMove(target);
    }
    @Override protected void telegraphDefense(ServerLevel server, LivingEntity target, int tick, BossMoveTimeline move) {
        CircusEffects.gather(server, position().add(0, getBbHeight() * 0.85, 0), CircusEffects.BONE, tick / (float) move.windup());
        if (tick % 3 == 0) BossTelegraphs.cone(server, position(), target.position().subtract(position()), 2.0, 90.0, CircusEffects.CRIMSON);
    }
    @Override protected void strikeDefense(LivingEntity target) {
        swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        if (distanceToSqr(target) < 4 && doHurtTarget(target) && level() instanceof ServerLevel server)
            CircusEffects.impact(server, target.position().add(0, 1, 0), 0.6);
    }
}
