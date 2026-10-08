package com.vincenthuto.hemomancy.common.entity.npc.circus;

import net.minecraft.core.registries.BuiltInRegistries;
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
    @Override protected void tickDefense(LivingEntity target) {
        getNavigation().moveTo(target, 1);
        if (tickCount % 30 == 0 && distanceToSqr(target) < 4) doHurtTarget(target);
    }
}
