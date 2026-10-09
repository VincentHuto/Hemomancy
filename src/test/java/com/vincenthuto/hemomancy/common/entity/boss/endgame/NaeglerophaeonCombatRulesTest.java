package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonCombatRules.Move;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class NaeglerophaeonCombatRulesTest {
	private static final float MAX = NaeglerophaeonCombatRules.MAX_HEALTH;

	@Test
	void gripStaysBelowTheFaceAndTurnsWithTheVictim() {
		var feet=new Vec3(10,80,10);
		for(float yaw:new float[]{0,90,180,270}) {
			var grip=NaeglerophaeonCombatRules.gripPosition(feet,1.8,yaw).subtract(feet);
			assertEquals(.48,grip.horizontalDistance(),1e-6);
			assertTrue(grip.y<1.2);
		}
	}

	@Test
	void eightDamageBreaksGrab() {
		assertFalse(NaeglerophaeonCombatRules.breaksGrab(7.9F));
		assertTrue(NaeglerophaeonCombatRules.breaksGrab(8.0F));
	}

	@Test
	void phaseTwoStartsAtHalfHealth() {
		assertFalse(NaeglerophaeonCombatRules.phaseTwo(75.1F,MAX));
		assertTrue(NaeglerophaeonCombatRules.phaseTwo(75F,MAX));
	}

	@Test
	void noHitSkipsTheOverloadFinale() {
		float line=NaeglerophaeonCombatRules.overloadLine(MAX);
		assertEquals(15F,line,1e-4);
		assertEquals(line,NaeglerophaeonCombatRules.allowedHealth(-500,MAX,false,false,false),1e-4);
		assertEquals(40F,NaeglerophaeonCombatRules.allowedHealth(40,MAX,false,false,false),1e-4);
		assertTrue(NaeglerophaeonCombatRules.startsOverload(line,MAX,false,false));
		assertFalse(NaeglerophaeonCombatRules.startsOverload(line,MAX,true,false));
		assertFalse(NaeglerophaeonCombatRules.startsOverload(1,MAX,false,true));
	}

	@Test
	void overloadKeepsOnePercentButDrainedAndVoidDamageCanKill() {
		float floor=NaeglerophaeonCombatRules.overloadFloor(MAX);
		assertEquals(1.5F,floor,1e-4);
		assertEquals(floor,NaeglerophaeonCombatRules.allowedHealth(-500,MAX,true,false,false),1e-4);
		assertEquals(-500F,NaeglerophaeonCombatRules.allowedHealth(-500,MAX,false,true,false),1e-4);
		assertEquals(-500F,NaeglerophaeonCombatRules.allowedHealth(-500,MAX,true,false,true),1e-4);
	}

	private static Move choose(double distance,boolean phaseTwo,int crowd,boolean node,Move... ready) {
		Set<Move> available=ready.length==0?EnumSet.noneOf(Move.class):EnumSet.of(ready[0],ready);
		return NaeglerophaeonCombatRules.choose(distance,true,phaseTwo,crowd,node,available::contains);
	}

	@Test
	void movesFollowRangePriority() {
		Move[] all=Move.values();
		assertEquals(Move.LASH,choose(4,false,1,true,all));
		assertEquals(Move.CAPTURE,choose(10,false,1,true,all));
		assertEquals(Move.LUNGE,choose(14,false,1,true,all));
		assertEquals(Move.CONDUCT,choose(22,false,1,true,all));
		assertEquals(Move.VOLLEY,choose(22,false,1,false,all));
		assertEquals(Move.CHARGE,choose(15,false,1,false,Move.CHARGE));
		assertEquals(Move.NONE,choose(30,false,1,true,all));
		assertEquals(Move.NONE,NaeglerophaeonCombatRules.choose(4,false,true,1,true,m->true));
	}

	@Test
	void novaAnswersCrowdsAndPhaseTwo() {
		assertEquals(Move.NOVA,choose(7,false,2,false,Move.NOVA,Move.LUNGE));
		assertEquals(Move.LUNGE,choose(7,false,1,false,Move.NOVA,Move.LUNGE));
		assertEquals(Move.NOVA,choose(9,true,1,false,Move.NOVA,Move.LUNGE));
	}

	@Test
	void phaseTwoQuickensEveryMove() {
		for(Move move:Move.values()) if(move!=Move.NONE)
			assertTrue(NaeglerophaeonCombatRules.cooldown(move,true)<NaeglerophaeonCombatRules.cooldown(move,false),move.name());
		assertTrue(NaeglerophaeonCombatRules.recoveryTicks(true)<NaeglerophaeonCombatRules.recoveryTicks(false));
		assertEquals(5,NaeglerophaeonCombatRules.volleyCount(true));
		assertEquals(2,NaeglerophaeonCombatRules.lungeCount(true));
		assertTrue(NaeglerophaeonCombatRules.drainHeal(true)>NaeglerophaeonCombatRules.drainHeal(false));
	}

	@Test
	void freshBossOpensWithGrabAndBolt() {
		assertEquals(0,NaeglerophaeonCombatRules.openingCooldown(Move.CAPTURE));
		assertEquals(0,NaeglerophaeonCombatRules.openingCooldown(Move.CHARGE));
		assertTrue(NaeglerophaeonCombatRules.openingCooldown(Move.LUNGE)>=100);
		assertTrue(NaeglerophaeonCombatRules.openingCooldown(Move.VOLLEY)>=100);
	}

	@Test
	void brokenNodesReleaseTwoToSixSparksWithinTheCap() {
		for(int roll=-20;roll<20;roll++) {
			int count=NaeglerophaeonCombatRules.homingSparks(roll,0);
			assertTrue(count>=2 && count<=6,"roll "+roll);
		}
		assertEquals(1,NaeglerophaeonCombatRules.homingSparks(4,NaeglerophaeonCombatRules.MAX_HOMING-1));
		assertEquals(0,NaeglerophaeonCombatRules.homingSparks(4,NaeglerophaeonCombatRules.MAX_HOMING));
	}

	@Test
	void sparkTurnIsLimitedAndSpeedIsConstant() {
		Vec3 velocity=new Vec3(NaeglerophaeonCombatRules.HOMING_SPEED,0,0);
		Vec3 next=NaeglerophaeonCombatRules.steer(velocity,new Vec3(0,0,-10));
		assertEquals(NaeglerophaeonCombatRules.HOMING_SPEED,next.length(),1e-6);
		double turned=Math.acos(velocity.normalize().dot(next.normalize()));
		assertEquals(NaeglerophaeonCombatRules.HOMING_TURN,turned,1e-6);
	}

	@Test
	void sparkCatchesAStillPlayerButNotASprintingOne() {
		Vec3 still=new Vec3(10,0,0);
		assertTrue(caught(still,Vec3.ZERO),"a player who stops moving is caught");
		// Sprinting straight away keeps the gap open because sparks are slower than a sprint.
		assertFalse(caught(still,new Vec3(.28,0,0)),"a sprinting player outruns the spark");
	}

	private static boolean caught(Vec3 player,Vec3 playerVelocity) {
		Vec3 spark=Vec3.ZERO, velocity=new Vec3(0,NaeglerophaeonCombatRules.HOMING_SPEED,0);
		for(int tick=0;tick<NaeglerophaeonCombatRules.HOMING_LIFETIME;tick++) {
			velocity=NaeglerophaeonCombatRules.steer(velocity,player.subtract(spark));
			spark=spark.add(velocity);
			player=player.add(playerVelocity);
			if(spark.distanceTo(player)<.6) return true;
		}
		return false;
	}
}
