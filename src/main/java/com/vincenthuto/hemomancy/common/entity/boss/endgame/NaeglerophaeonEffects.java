package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.DuctilisLightningEffects;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Authored Ductilis light cues for the predator's charged core and attacks. */
final class NaeglerophaeonEffects {
	private static final LightningTestConfig ARC = new LightningTestConfig(
			LightningTestConfig.Backend.BOLT, 0xE8F52D52, 0xE8F52D52, 0xFFD085FF,
			36.0F, 0.0F, 0.0F, 0.0F, 48.0F, 1.7F, 6, 6, 0.06F, 0.012F, false, 0L, false, 10);
	private static final ParticleColor GOLD = new ParticleColor(224, 40, 112);
	private static final ParticleColor BLOOD = new ParticleColor(175, 20, 38);
	private static final ParticleColor MENDING = new ParticleColor(255, 228, 112);
	private static final ParticleColor VIOLET = new ParticleColor(170, 53, 255);
	private static final ParticleColor ASH = new ParticleColor(92, 84, 82);
	private static final LightningTestConfig REPAIR_ARC = new LightningTestConfig(
			LightningTestConfig.Backend.BOLT, 0xE8FFE84A, 0xE8FFE84A, 0xFFFFFFFF,
			16, 0, 0, 0, 46, 1.6F, 6, 5, .06F, .012F, false, 0, false, 10);
	private static final LightningTestConfig PURPLE = new LightningTestConfig(
			LightningTestConfig.Backend.BOLT, 0xDDAA35FF, 0xDDAA35FF, 0xFFF3CBFF,
			36, 0, 0, 0, 48, 1.7F, 6, 6, .04F, .009F, false, 0, false, 10);
	private static final LightningTestConfig SPARK = new LightningTestConfig(
			LightningTestConfig.Backend.BOLT, 0xE8F52D52, 0xE8F52D52, 0xFFFFE0F0,
			8, 0, 0, 0, 30, 2.4F, 3, 3, .035F, .007F, false, 0, false, 10);
	private static final LightningTestConfig BLAST = new LightningTestConfig(
			LightningTestConfig.Backend.BOLT, 0xF0F52D52, 0xF0F52D52, 0xFFFFF6D8,
			96, 0, 0, 0, 60, 3.2F, 10, 8, .16F, .045F, false, 0, false, 10);

	private NaeglerophaeonEffects() {}

	static void coil(ServerLevel level, Vec3 center) {
		for (int i = 0; i < 3; i++) {
			Vec3 tip = center.add((level.random.nextDouble() - 0.5) * 2.0,
					(level.random.nextDouble() - 0.5) * 1.4,
					(level.random.nextDouble() - 0.5) * 2.0);
			LightningTesterSpawner.spawn(level, center, tip, ARC);
		}
		level.sendParticles(HemoParticleData.glow(GOLD), center.x, center.y, center.z,
				6, 0.5, 0.4, 0.5, 0.0);
	}

	static void telegraph(ServerLevel level, Vec3 from, Vec3 to) {
		level.sendParticles(HemoParticleData.glow(GOLD), from.x, from.y, from.z,
				12, 0.45, 0.45, 0.45, 0.0);
		LightningTesterSpawner.spawn(level, from, from.lerp(to, 0.35), ARC);
		level.playSound(null, from.x, from.y, from.z, SoundInit.ENTITY_NAEGLEROPHAEON_CHARGE.get(),
				SoundSource.HOSTILE, 1.1F, 0.85F);
	}

	static void zap(ServerLevel level, Vec3 from, Vec3 to) {
		LightningTesterSpawner.spawn(level, from, to, ARC);
		LightningTesterSpawner.spawn(level, from.add(0, .12, 0), to, PURPLE);
		level.sendParticles(HemoParticleData.glow(GOLD), to.x, to.y, to.z,
				16, 0.3, 0.5, 0.3, 0.0);
		level.playSound(null, from.x, from.y, from.z, SoundInit.ENTITY_NAEGLEROPHAEON_ZAP.get(),
				SoundSource.HOSTILE, 1.1F, 1.0F);
	}

	static void blink(ServerLevel level, Vec3 from, Vec3 to) {
		LightningTesterSpawner.spawn(level, from, to, ARC);
		coil(level, to);
		level.playSound(null, to.x, to.y, to.z, SoundInit.ENTITY_NAEGLEROPHAEON_BLINK.get(),
				SoundSource.HOSTILE, 1.0F, 1.2F);
	}

	static void drain(ServerLevel level, Vec3 from, Vec3 to) {
		LightningTesterSpawner.spawn(level, from, to, ARC);
		level.sendParticles(HemoParticleData.glow(BLOOD), to.x, to.y, to.z,
				8, 0.4, 0.3, 0.4, 0.0);
		level.playSound(null, to.x, to.y, to.z, SoundInit.ENTITY_NAEGLEROPHAEON_DRAIN.get(),
				SoundSource.HOSTILE, 0.9F, 0.8F);
	}

	static void releaseBurst(Player player) {
		DuctilisLightningEffects.hemolymphalPulse(player);
	}

	static void repairing(ServerLevel level,Vec3 core,Vec3 gap) {
		LightningTesterSpawner.spawn(level,core,gap,REPAIR_ARC);
		level.sendParticles(HemoParticleData.glow(MENDING),gap.x,gap.y,gap.z,
				5,.22,.22,.22,0);
	}

	static void repaired(ServerLevel level,Vec3 gap) {
		level.sendParticles(HemoParticleData.glow(MENDING),gap.x,gap.y,gap.z,
				18,.45,.45,.45,0);
		for(int i=0;i<3;i++) {
			double angle=i*Math.PI*2/3;
			LightningTesterSpawner.spawn(level,gap,gap.add(Math.cos(angle)*.7,.25,Math.sin(angle)*.7),REPAIR_ARC);
		}
	}

	/** The locked dash line, drawn while the tendrils flare. */
	static void lungeFlare(ServerLevel level,Vec3 from,Vec3 to) {
		double length=from.distanceTo(to);
		for(double d=1;d<length;d+=1.5) {
			Vec3 at=from.lerp(to,d/length);
			level.sendParticles(HemoParticleData.glow(GOLD),at.x,at.y,at.z,1,.05,.05,.05,0);
		}
		coil(level,from);
		level.playSound(null,from.x,from.y,from.z,SoundInit.ENTITY_NAEGLEROPHAEON_CHARGE.get(),
				SoundSource.HOSTILE,1.2F,1.25F);
	}

	static void lungeTrail(ServerLevel level,Vec3 previous,Vec3 core) {
		LightningTesterSpawner.spawn(level,previous,core,PURPLE);
		level.sendParticles(HemoParticleData.glow(BLOOD),previous.x,previous.y,previous.z,3,.3,.3,.3,0);
	}

	static void lashSweep(ServerLevel level,Vec3 core) {
		for(int i=0;i<6;i++) {
			double angle=i*Math.PI/3+level.random.nextDouble()*.3;
			Vec3 out=new Vec3(Math.cos(angle),(level.random.nextDouble()-.5)*.6,Math.sin(angle));
			Vec3 at=core.add(out.scale(NaeglerophaeonCombatRules.LASH_RADIUS*.55));
			PacketHandler.sendClawSlash(at,out,BLOOD,false,1.6F,48,level);
			LightningTesterSpawner.spawn(level,core,core.add(out.scale(NaeglerophaeonCombatRules.LASH_RADIUS)),ARC);
		}
		level.sendParticles(HemoParticleData.hitGlow(GOLD),core.x,core.y,core.z,10,1.6,.6,1.6,0);
		level.playSound(null,core.x,core.y,core.z,SoundInit.ENTITY_NAEGLEROPHAEON_ZAP.get(),
				SoundSource.HOSTILE,1.2F,.7F);
	}

	/** Marks where a tip spark is headed, then the strike once its travel delay has passed. */
	static void sparkLaunch(ServerLevel level,Vec3 tip,Vec3 aim) {
		LightningTesterSpawner.spawn(level,tip,tip.lerp(aim,.25),SPARK);
		level.sendParticles(HemoParticleData.hitGlow(GOLD),aim.x,aim.y,aim.z,4,.15,.15,.15,0);
	}

	static void sparkStrike(ServerLevel level,Vec3 tip,Vec3 aim) {
		LightningTesterSpawner.spawn(level,tip,aim,ARC);
		level.sendParticles(HemoParticleData.glow(GOLD),aim.x,aim.y,aim.z,8,.25,.25,.25,0);
		level.playSound(null,aim.x,aim.y,aim.z,SoundInit.ENTITY_NAEGLEROPHAEON_ZAP.get(),
				SoundSource.HOSTILE,.7F,1.5F);
	}

	static void conductCharge(ServerLevel level,Vec3 core,Vec3 node) {
		LightningTesterSpawner.spawn(level,core,node,PURPLE);
		level.sendParticles(HemoParticleData.glow(VIOLET),node.x,node.y,node.z,10,.4,.4,.4,0);
	}

	static void conductCell(ServerLevel level,Vec3 from,Vec3 cell) {
		LightningTesterSpawner.spawn(level,from,cell,PURPLE);
		level.sendParticles(HemoParticleData.glow(VIOLET),cell.x,cell.y,cell.z,2,.2,.2,.2,0);
	}

	static void novaCharge(ServerLevel level,Vec3 core,double progress) {
		coil(level,core);
		level.sendParticles(HemoParticleData.glow(MENDING),core.x,core.y,core.z,(int)(4+progress*10),.6,.6,.6,0);
	}

	static void novaRing(ServerLevel level,Vec3 core,double radius) {
		int points=Math.max(8,(int)(radius*5));
		for(int i=0;i<points;i++) {
			double angle=i*Math.PI*2/points;
			Vec3 at=core.add(Math.cos(angle)*radius,0,Math.sin(angle)*radius);
			level.sendParticles(HemoParticleData.hitGlow(GOLD),at.x,at.y,at.z,1,.05,.2,.05,0);
		}
		for(int i=0;i<4;i++) {
			double angle=level.random.nextDouble()*Math.PI*2;
			LightningTesterSpawner.spawn(level,core,core.add(Math.cos(angle)*radius,(level.random.nextDouble()-.5),Math.sin(angle)*radius),ARC);
		}
	}

	static void novaBurst(ServerLevel level,Vec3 core) {
		level.playSound(null,core.x,core.y,core.z,SoundInit.ENTITY_NAEGLEROPHAEON_ZAP.get(),
				SoundSource.HOSTILE,1.6F,.55F);
	}

	static void transition(ServerLevel level,Vec3 core) {
		for(int i=0;i<12;i++) {
			double angle=i*Math.PI/6;
			LightningTesterSpawner.spawn(level,core,core.add(Math.cos(angle)*4,(level.random.nextDouble()-.3)*2,Math.sin(angle)*4),
					i%2==0?ARC:PURPLE);
		}
		level.sendParticles(HemoParticleData.glow(VIOLET),core.x,core.y,core.z,24,1,1,1,0);
		level.playSound(null,core.x,core.y,core.z,SoundInit.ENTITY_NAEGLEROPHAEON_CHARGE.get(),
				SoundSource.HOSTILE,1.8F,.55F);
	}

	static void dive(ServerLevel level,Vec3 core,Vec3 node) {
		LightningTesterSpawner.spawn(level,core,node,ARC);
		level.sendParticles(HemoParticleData.glow(GOLD),node.x,node.y,node.z,8,.3,.3,.3,0);
	}

	static void transit(ServerLevel level,Vec3 at) {
		level.sendParticles(HemoParticleData.glow(MENDING),at.x,at.y,at.z,3,.15,.15,.15,0);
	}

	static void blast(ServerLevel level,Vec3 from,Vec3 to) {
		LightningTesterSpawner.spawn(level,from,to,BLAST);
		LightningTesterSpawner.spawn(level,from.add(0,.2,0),to,PURPLE);
		level.playSound(null,from.x,from.y,from.z,SoundInit.ENTITY_NAEGLEROPHAEON_ZAP.get(),
				SoundSource.HOSTILE,2.0F,.5F);
	}

	static void nodeBurst(ServerLevel level,Vec3 node) {
		level.sendParticles(HemoParticleData.hitGlow(MENDING),node.x,node.y,node.z,16,.5,.5,.5,0);
		for(int i=0;i<5;i++) {
			Vec3 tip=node.add((level.random.nextDouble()-.5)*3,(level.random.nextDouble()-.5)*3,(level.random.nextDouble()-.5)*3);
			LightningTesterSpawner.spawn(level,node,tip,REPAIR_ARC);
		}
		level.playSound(null,node.x,node.y,node.z,SoundInit.ENTITY_NAEGLEROPHAEON_BLINK.get(),
				SoundSource.HOSTILE,1.4F,.6F);
	}

	static void homing(ServerLevel level,Vec3 previous,Vec3 at) {
		LightningTesterSpawner.spawn(level,previous,at,SPARK);
		level.sendParticles(HemoParticleData.glow(MENDING),at.x,at.y,at.z,1,.04,.04,.04,0);
	}

	static void homingHit(ServerLevel level,Vec3 at) {
		level.sendParticles(HemoParticleData.hitGlow(GOLD),at.x,at.y,at.z,6,.2,.2,.2,0);
	}

	static void twitch(ServerLevel level,Vec3 core) {
		level.sendParticles(HemoParticleData.darkGlow(ASH),core.x,core.y,core.z,3,.4,.3,.4,0);
	}
}
