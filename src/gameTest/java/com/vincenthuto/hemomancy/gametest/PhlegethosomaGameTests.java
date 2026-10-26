package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.*;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("phlegethosoma_validation")
@PrefixGameTestTemplate(false)
public final class PhlegethosomaGameTests {
    @GameTest(template="empty",batch="fluke_lifecycle",timeoutTicks=130)
    public static void phasesCoverPersistenceAndDeathCleanup(GameTestHelper h) {
        var level=h.getLevel();
        for(int x=1;x<=19;x++)for(int z=1;z<=23;z++)h.setBlock(new BlockPos(x,1,z),Blocks.BLACKSTONE);
        var boss=EntityInit.phlegethosoma.get().create(level);
        boss.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(9,2,8))));level.addFreshEntity(boss);
        h.assertTrue(!boss.canDrownInFluidType(com.vincenthuto.hemomancy.common.init.FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get()),"Native ichor habitation must not drown the animal");
        var cow=h.spawn(EntityType.COW,new BlockPos(9,2,16));cow.setNoAi(true);boss.setTarget(cow);
        h.runAtTickTime(65,()-> {
            boss.setHealth(72);
        });
        h.runAtTickTime(68,()-> {
            h.assertTrue(boss.phase()==2,"60 percent health must enter Engorged");boss.setHealth(100);
        });
        h.runAtTickTime(70,()-> {
            h.assertTrue(boss.phase()==2,"Feeding must not reset a phase");boss.setHealth(30);
        });
        h.runAtTickTime(73,()-> {
            h.assertTrue(boss.phase()==3,"25 percent health must enter Ruptured");
            CompoundTag saved=new CompoundTag();boss.addAdditionalSaveData(saved);
            var restored=EntityInit.phlegethosoma.get().create(level);restored.readAdditionalSaveData(saved);
            h.assertTrue(restored.phase()==3 && restored.action()==PhlegethosomaEntity.Action.RECOVERY,"Reload retains phase and clears transient attack");
            boss.setYRot(0);
            var clot=new TemporaryCoagulumEntity(EntityInit.temporary_coagulum.get(),level);
            clot.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(9,2,12))));clot.configure(boss);level.addFreshEntity(clot);
            h.assertTrue(clot.canBeCollidedWith() && !boss.visible(cow),"A clot must physically block movement and protect against suction");
            clot.hurt(level.damageSources().generic(),10);h.assertTrue(clot.isRemoved(),"Clots are destructible");
            var last=new TemporaryCoagulumEntity(EntityInit.temporary_coagulum.get(),level);
            last.setPos(boss.position().add(3,0,0));last.configure(boss);level.addFreshEntity(last);
            boss.kill();h.assertTrue(last.isRemoved(),"Boss death clears owned clots");
            cow.discard();
            for(int x=1;x<=19;x++)for(int z=1;z<=23;z++)h.setBlock(new BlockPos(x,1,z),Blocks.AIR);
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="fluke_suction",timeoutTicks=180)
    public static void suctionHasAReadableInterruptibleWindow(GameTestHelper h) {
        for(int x=1;x<=19;x++)for(int z=1;z<=23;z++)h.setBlock(new BlockPos(x,1,z),Blocks.BLACKSTONE);
        var boss=EntityInit.phlegethosoma.get().create(h.getLevel());
        boss.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(9,2,8))));h.getLevel().addFreshEntity(boss);
        var cow=h.spawn(EntityType.COW,new BlockPos(9,2,15));cow.setNoAi(true);boss.setTarget(cow);
        h.runAtTickTime(97,()-> {
            h.assertTrue(boss.action()==PhlegethosomaEntity.Action.SUCTION_WINDUP,"Ordinary combat must reach suction after introduction and recovery");
            boss.hurt(h.getLevel().damageSources().mobAttack(cow),6);
            h.assertTrue(boss.action()==PhlegethosomaEntity.Action.STAGGERED,"Direct attack interrupts suction");
        });
        h.runAtTickTime(120,()->h.assertTrue(boss.action()==PhlegethosomaEntity.Action.STAGGERED,"Stagger must provide a real punish window"));
        h.runAtTickTime(140,()-> {
            h.assertTrue(boss.action()==PhlegethosomaEntity.Action.RECOVERY,"Stagger must end in recovery");
            boss.discard();cow.discard();
            for(int x=1;x<=19;x++)for(int z=1;z<=23;z++)h.setBlock(new BlockPos(x,1,z),Blocks.AIR);
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="fluke_swimming",timeoutTicks=180)
    public static void pursuesRapidlyInsideIchorWithoutDrowning(GameTestHelper h) {
        for(int x=0;x<32;x++)for(int z=0;z<24;z++)for(int y=1;y<=4;y++) {
            boolean wall=y==1 || x==0 || x==31 || z==0 || z==23;
            h.setBlock(new BlockPos(x,y,z),wall?Blocks.BLACKSTONE:
                    com.vincenthuto.hemomancy.common.init.BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get());
        }
        BlockPos pool=h.absolutePos(new BlockPos(15,3,11));
        h.assertTrue(PhlegethosomaSpawnRules.validPool(h.getLevel(),pool),"A broad ichor pool is valid habitat");
        h.setBlock(new BlockPos(15,3,11),Blocks.AIR);
        h.assertTrue(!PhlegethosomaSpawnRules.validPool(h.getLevel(),pool),"Dry gaps reject aquatic placement");
        h.setBlock(new BlockPos(15,3,11),com.vincenthuto.hemomancy.common.init.BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get());
        var boss=EntityInit.phlegethosoma.get().create(h.getLevel());
        Vec3 start=Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(7,3,11)));
        boss.setPos(start);h.getLevel().addFreshEntity(boss);
        var cow=h.spawn(EntityType.COW,new BlockPos(24,3,11));
        cow.setNoAi(true);cow.setNoGravity(true);cow.setInvulnerable(true);boss.setTarget(cow);
        double[] maximum={0};
        h.onEachTick(()-> {
            if(boss.action()==PhlegethosomaEntity.Action.SEEKING && boss.isIchorSwimming())
                maximum[0]=Math.max(maximum[0],boss.getDeltaMovement().length());
        });
        h.runAtTickTime(130,()-> {
            h.assertTrue(boss.isIchorSwimming() || boss.isBreachAttack(),"The animal must swim or be making a committed breach");
            h.assertTrue(boss.position().distanceTo(start)>3,"Ordinary combat must pursue through ichor");
            h.assertTrue(maximum[0]>.28 && maximum[0]<=.61,"Pursuit must be rapid and bounded: "+maximum[0]);
            h.assertTrue(boss.getHealth()==boss.getMaxHealth(),"Native fluid contact must not scald or drown it");
            boss.discard();cow.discard();
            for(int x=0;x<32;x++)for(int z=0;z<24;z++)for(int y=1;y<=4;y++)h.setBlock(new BlockPos(x,y,z),Blocks.AIR);
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="fluke_breach",timeoutTicks=220)
    public static void divesBreachesHitsOnceAndReturnsUnderIchor(GameTestHelper h) {
        breachPool(h,false);
        var boss=EntityInit.phlegethosoma.get().create(h.getLevel());
        Vec3 origin=Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(7,4,11))).add(0,.7,0);
        double surface=h.absolutePos(new BlockPos(7,5,11)).getY();
        boss.setPos(origin);h.getLevel().addFreshEntity(boss);
        var cow=h.spawn(EntityType.COW,new BlockPos(17,5,11));
        cow.setPos(cow.getX(),surface+.05,cow.getZ());cow.setNoAi(true);cow.setNoGravity(true);boss.setTarget(cow);
        boolean[] seen=new boolean[4];
        h.onEachTick(()-> {
            if(boss.action()==PhlegethosomaEntity.Action.DIVE && boss.getBoundingBox().maxY<surface)seen[0]=true;
            if(boss.action()==PhlegethosomaEntity.Action.BREACH && boss.getY()>surface+.5)seen[1]=true;
            if(boss.action()==PhlegethosomaEntity.Action.RETURN_TO_ICHOR)seen[2]=true;
            if(seen[2] && boss.action()==PhlegethosomaEntity.Action.RECOVERY && boss.getBoundingBox().maxY<surface)seen[3]=true;
        });
        h.runAtTickTime(185,()-> {
            h.assertTrue(seen[0] && seen[1] && seen[2] && seen[3],"Combat must fully submerge, breach above the surface, then return underwater: "+java.util.Arrays.toString(seen)+" "+boss.action()+" "+boss.position());
            h.assertTrue(cow.getHealth()==2,"The locked breach must deal eight damage exactly once: "+cow.getHealth());
            h.assertTrue(boss.isIchorSwimming(),"Recovery must take place back inside ichor");
            h.assertTrue(boss.getHealth()==boss.getMaxHealth(),"The leap and return must not inflict self-damage");
            boss.discard();cow.discard();clearBreachPool(h);h.succeed();
        });
    }
    @GameTest(template="empty",batch="fluke_breach_loss",timeoutTicks=220)
    public static void losingTargetDuringDiveStillReturnsToPool(GameTestHelper h) {
        breachPool(h,false);
        var boss=EntityInit.phlegethosoma.get().create(h.getLevel());
        boss.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(7,4,11))).add(0,.7,0));h.getLevel().addFreshEntity(boss);
        var cow=h.spawn(EntityType.COW,new BlockPos(17,5,11));cow.setNoAi(true);cow.setNoGravity(true);boss.setTarget(cow);
        boolean[] cancelled={false};
        h.onEachTick(()-> {
            if(!cancelled[0] && boss.action()==PhlegethosomaEntity.Action.DIVE && boss.actionTime()>5) {
                cancelled[0]=true;cow.discard();boss.setTarget(null);
            }
        });
        h.runAtTickTime(185,()-> {
            h.assertTrue(cancelled[0],"The test must remove its target during an actual dive");
            h.assertTrue(!boss.isBreachAttack() && boss.isIchorSwimming(),"Target loss must end the attack safely underwater");
            boss.discard();cow.discard();clearBreachPool(h);h.succeed();
        });
    }
    @GameTest(template="empty",batch="fluke_breach_cover",timeoutTicks=200)
    public static void lowCeilingRejectsBreachWithoutStrandingAnimal(GameTestHelper h) {
        breachPool(h,true);
        var boss=EntityInit.phlegethosoma.get().create(h.getLevel());
        boss.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(7,4,11))).add(0,.1,0));h.getLevel().addFreshEntity(boss);
        var cow=h.spawn(EntityType.COW,new BlockPos(17,4,11));cow.setNoAi(true);cow.setNoGravity(true);cow.setInvulnerable(true);boss.setTarget(cow);
        h.onEachTick(()->h.assertTrue(!boss.isBreachAttack(),"A solid ceiling must reject the airborne route"));
        h.runAtTickTime(175,()-> {
            h.assertTrue(boss.isIchorSwimming(),"Rejected breach must retain ordinary swimming/combat");
            boss.discard();cow.discard();clearBreachPool(h);h.succeed();
        });
    }
    private static void breachPool(GameTestHelper h,boolean ceiling) {
        for(int x=0;x<32;x++)for(int z=0;z<24;z++) {
            for(int y=1;y<=4;y++)h.setBlock(new BlockPos(x,y,z),y==1 || x==0 || x==31 || z==0 || z==23 ? Blocks.BLACKSTONE:
                    com.vincenthuto.hemomancy.common.init.BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get());
            if(ceiling)h.setBlock(new BlockPos(x,6,z),Blocks.BLACKSTONE);
        }
    }
    @GameTest(template="empty",batch="fluke_breach_dodge",timeoutTicks=220)
    public static void committedLeapCanBeDodgedAndSurvivesPhaseChange(GameTestHelper h) {
        breachPool(h,false);
        var boss=EntityInit.phlegethosoma.get().create(h.getLevel());
        boss.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(7,4,11))).add(0,.7,0));h.getLevel().addFreshEntity(boss);
        var cow=h.spawn(EntityType.COW,new BlockPos(17,5,11));cow.setNoAi(true);cow.setNoGravity(true);boss.setTarget(cow);
        boolean[] dodged={false}, returning={false};
        h.onEachTick(()-> {
            if(!dodged[0] && boss.action()==PhlegethosomaEntity.Action.BREACH) {
                dodged[0]=true;cow.setPos(cow.position().add(0,0,6));
            }
            if(dodged[0] && !returning[0] && boss.action()==PhlegethosomaEntity.Action.RETURN_TO_ICHOR) {
                returning[0]=true;boss.setHealth(70);
            }
        });
        h.runAtTickTime(185,()-> {
            h.assertTrue(dodged[0] && returning[0],"The test must move away from a committed strike and change phase during its return");
            h.assertTrue(cow.getHealth()==cow.getMaxHealth(),"The leap must not track a target that dodged its locked position");
            h.assertTrue(boss.phase()==2 && boss.isIchorSwimming() && !boss.isBreachAttack(),"A phase transition must still complete its return underwater");
            boss.discard();cow.discard();clearBreachPool(h);h.succeed();
        });
    }
    @GameTest(template="empty",batch="fluke_breach_shallow",timeoutTicks=200)
    public static void shallowPoolRetainsOrdinaryCombat(GameTestHelper h) {
        breachPool(h,false);
        for(int x=1;x<31;x++)for(int z=1;z<23;z++)for(int y=2;y<=3;y++)h.setBlock(new BlockPos(x,y,z),Blocks.BLACKSTONE);
        var boss=EntityInit.phlegethosoma.get().create(h.getLevel());
        boss.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(7,4,11))).add(0,.1,0));h.getLevel().addFreshEntity(boss);
        var cow=h.spawn(EntityType.COW,new BlockPos(17,5,11));cow.setNoAi(true);cow.setNoGravity(true);cow.setInvulnerable(true);boss.setTarget(cow);
        h.onEachTick(()->h.assertTrue(!boss.isBreachAttack(),"A one-block-deep pool cannot hide a submerged attack"));
        h.runAtTickTime(175,()-> {
            h.assertTrue(boss.isIchorSwimming(),"Shallow ichor must still support ordinary swimming");
            boss.discard();cow.discard();clearBreachPool(h);h.succeed();
        });
    }
    private static void clearBreachPool(GameTestHelper h) {
        for(int x=0;x<32;x++)for(int z=0;z<24;z++)for(int y=0;y<=6;y++)h.setBlock(new BlockPos(x,y,z),Blocks.AIR);
    }
    @GameTest(template="empty",batch="fluke_breach_obstruction",timeoutTicks=240)
    public static void newCoverRedirectsReturnToReachableOpenIchor(GameTestHelper h) {
        breachPool(h,false);
        for(int x=0;x<32;x++)for(int z=0;z<24;z++) {
            h.setBlock(new BlockPos(x,0,z),Blocks.BLACKSTONE);
            if(x>0 && x<31 && z>0 && z<23)h.setBlock(new BlockPos(x,1,z),com.vincenthuto.hemomancy.common.init.BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get());
        }
        var boss=EntityInit.phlegethosoma.get().create(h.getLevel());
        boss.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(7,4,11))).add(0,.7,0));h.getLevel().addFreshEntity(boss);
        var cow=h.spawn(EntityType.COW,new BlockPos(17,5,11));cow.setNoAi(true);cow.setNoGravity(true);cow.setInvulnerable(true);boss.setTarget(cow);
        boolean[] covered={false}, recoveredWet={false};
        h.onEachTick(()-> {
            if(!covered[0] && boss.action()==PhlegethosomaEntity.Action.RETURN_TO_ICHOR) {
                covered[0]=true;
                BlockPos center=boss.blockPosition();
                for(int x=-2;x<=4;x++)for(int z=-1;z<=1;z++)
                    h.getLevel().setBlock(new BlockPos(center.getX()+x,h.absolutePos(new BlockPos(0,4,0)).getY(),center.getZ()+z),Blocks.BLACKSTONE.defaultBlockState(),3);
            }
            if(covered[0] && boss.action()==PhlegethosomaEntity.Action.RECOVERY && boss.isIchorSwimming())recoveredWet[0]=true;
        });
        h.runAtTickTime(210,()-> {
            h.assertTrue(covered[0] && recoveredWet[0],"New cover must redirect the return to reachable open ichor rather than the covered pool: "+boss.action()+" "+boss.position());
            boss.discard();cow.discard();clearBreachPool(h);h.succeed();
        });
    }
    @GameTest(template="empty",batch="fluke_safety",timeoutTicks=20)
    public static void movementRejectsLavaAndUnsupportedDrops(GameTestHelper h) {
        var cow=h.spawn(EntityType.COW,new BlockPos(2,3,2));cow.setNoAi(true);cow.setNoGravity(true);
        cow.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,3,2))));
        for(int x=1;x<=3;x++)for(int z=1;z<=3;z++)h.setBlock(new BlockPos(x,2,z),Blocks.BLACKSTONE);
        h.assertTrue(PhlegethosomaMovement.safe(cow,Vec3.ZERO),"Supported footing permits movement: " + cow.position() + " " + cow.getBoundingBox());
        h.setBlock(new BlockPos(2,2,2),Blocks.LAVA);
        h.assertTrue(!PhlegethosomaMovement.safe(cow,Vec3.ZERO),"Lava beneath a target rejects suction");
        for(int x=1;x<=3;x++)for(int z=1;z<=3;z++)h.setBlock(new BlockPos(x,2,z),Blocks.AIR);
        h.assertTrue(!PhlegethosomaMovement.safe(cow,Vec3.ZERO),"A drop beyond two blocks rejects suction");
        cow.discard();h.succeed();
    }
}
