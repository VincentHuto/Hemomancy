package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;
import net.minecraft.world.phys.Vec3;
import java.util.Map;
import java.util.WeakHashMap;

public final class VelorumAnimState {
    private static final Map<VelorumEntity,VelorumAnimState> STATES=new WeakHashMap<>();
    private final BossReplayClock[] clocks={new BossReplayClock(),new BossReplayClock(),new BossReplayClock(),new BossReplayClock()};
    private VelorumVisualRules.Phase phase=VelorumVisualRules.legacy(0,false,0);
    private final BossReplayClock breakClock = new BossReplayClock();
    private float pressure, release;
    private Vec3 core=new Vec3(0,1.5,0),claw=new Vec3(1,1.8,0);
    private Vec3[] lanterns={new Vec3(-1,2.5,0),new Vec3(-1,1,0),new Vec3(.5,1,0)};
    private VelorumAnimState() {}
    public static VelorumAnimState of(VelorumEntity boss){return STATES.computeIfAbsent(boss,ignored->new VelorumAnimState());}
    public static void clear(){STATES.clear();}
    public void update(VelorumEntity boss,float partial) {
        pressure = boss.getStillnessPressure() / 100F;
        release = breakClock.sample(0, boss.getStillnessBreakTicks(), boss.tickCount, partial, -1);
        int fr=boss.getFrostRelease(),vr=boss.getVeilRelease(),dr=boss.getDrainRelease(),tick=boss.tickCount;
        float f=clocks[0].sample(fr>0?1:0,fr>0?fr:boss.getFrostUntil(),tick,partial,-1);
        float v=clocks[1].sample(vr>0?1:0,vr>0?vr:boss.getVeilUntil(),tick,partial,-1);
        float d=clocks[2].sample(dr>0?1:0,dr>0?dr:boss.getDrainUntil(),tick,partial,-1);
        float m=clocks[3].sample(0,boss.getMartyrdomTicks(),tick,partial,-1);
        phase=new VelorumVisualRules.Phase(fr>0?boss.getFrostUntil():f,fr>0?f:0,
                vr>0?boss.getVeilUntil():v,vr>0?v:0,dr>0?boss.getDrainUntil():d,dr>0?d:0,m);
    }
    public VelorumVisualRules.Phase phase(){return phase;}
    public float pressure(){return pressure;}
    public float release(){return release;}
    public Vec3 core(){return core;}
    public Vec3 claw(){return claw;}
    public Vec3[] lanterns(){return lanterns;}
    public void attachments(Vec3 core,Vec3 claw,Vec3[] lanterns){this.core=core;this.claw=claw;this.lanterns=lanterns;}
}
