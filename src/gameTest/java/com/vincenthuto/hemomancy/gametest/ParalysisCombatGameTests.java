package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ParalysisCombatGameTests {
    @GameTest(templateNamespace="hemomancy",template="ductilis_arena",batch="ductilis_combat",timeoutTicks=150)
    public static void paralysisInterruptsMeleeAndDrawnBowsWithoutDisablingDamageOrRecovery(GameTestHelper h) {
        for(int x=3;x<=17;x++)for(int z=3;z<=17;z++)h.setBlock(x,2,z,Blocks.STONE);
        var level=h.getLevel();
        var archer=EntityType.SKELETON.create(level);
        var melee=EntityType.HUSK.create(level);
        var victim=EntityType.COW.create(level);
        archer.setPos(h.absoluteVec(new Vec3(5,3,5)));melee.setPos(h.absoluteVec(new Vec3(13,3,12)));
        victim.setPos(h.absoluteVec(new Vec3(13,3,13)));victim.setNoAi(true);
        victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);victim.setHealth(200);
        archer.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));
        archer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BOW));
        level.addFreshEntity(archer);level.addFreshEntity(melee);level.addFreshEntity(victim);
        archer.setTarget(victim);melee.setTarget(victim);archer.startUsingItem(InteractionHand.MAIN_HAND);
        h.assertTrue(archer.isUsingItem(),"Fixture did not start drawing its bow");
        h.assertTrue(Paralysis.apply(archer,60)&&Paralysis.apply(melee,60),"Combat paralysis was rejected");
        h.assertTrue(!archer.isUsingItem(),"Paralysis left an already drawn bow active");
        h.assertTrue(!melee.doHurtTarget(victim)&&victim.getHealth()==200,"Paralyzed melee attack caused damage");
        float before=melee.getHealth();melee.hurt(level.damageSources().generic(),2);
        h.assertTrue(melee.getHealth()<before,"Paralysis suppressed incoming damage");
        h.runAfterDelay(25,()-> {
            h.assertTrue(victim.getHealth()==200,"Paralyzed combat AI attacked its target");
            h.assertTrue(!archer.isUsingItem()&&level.getEntitiesOfClass(AbstractArrow.class,archer.getBoundingBox().inflate(20),arrow->arrow.getOwner()==archer).isEmpty(),"Paralyzed ranged AI started or released a shot");
            h.assertTrue(!archer.isNoAi()&&!melee.isNoAi(),"Paralysis persisted a NoAI flag");
            archer.removeEffect(EffectInit.paralysis);melee.removeEffect(EffectInit.paralysis);
            melee.discard();
            h.runAfterDelay(70,()-> {
                boolean fired=!level.getEntitiesOfClass(AbstractArrow.class,archer.getBoundingBox().inflate(25),arrow->arrow.getOwner()==archer).isEmpty()||victim.getHealth()<200;
                h.assertTrue(fired,"Ranged combat did not resume after paralysis was dispelled");
                archer.discard();victim.discard();h.succeed();
            });
        });
    }
}
