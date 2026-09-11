package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Small surface patches follow real collision faces; render frames never raycast. */
final class ThermalSurface {
    record Patch(Vec3 center,double halfWidth) {}
    private final List<Patch> patches=new ArrayList<>();
    private Vec3 sampled;
    private long updated=Long.MIN_VALUE;
    List<Patch> patches() {return patches;}
    Vec3 onSurface(Vec3 origin,Vec3 local) {
        Patch closest=null;double distance=Double.POSITIVE_INFINITY;
        for(Patch patch:patches) {
            Vec3 delta=patch.center.subtract(origin).subtract(local);
            double d=delta.x*delta.x+delta.z*delta.z;
            if(d<distance){distance=d;closest=patch;}
        }
        return closest==null?local:new Vec3(local.x,closest.center.y-origin.y+local.y,local.z);
    }

    void update(ClientLevel level,Form form,Vec3 origin,double radius) {
        if(form!=Form.BLOOM && form!=Form.COMMUNION && form!=Form.BLACKHEART_RUPTURE && form!=Form.MORTEM_BURST && form!=Form.FURNACE && form!=Form.STILLNESS && form!=Form.IGNITION && form!=Form.CRYOGENIC_PULSE && form!=Form.ICE)return;
        long now=level.getGameTime();
        if(sampled!=null && now-updated<5)return;
        if(sampled!=null && sampled.distanceToSqr(origin)<.16 && now-updated<20)return;
        updated=now;sampled=origin;patches.clear();
        double r=Math.min(8,Math.max(.5,radius)),step=Math.max(.65,r/3);
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++) {
            double dx=x*step,dz=z*step;
            if(dx*dx+dz*dz>r*r)continue;
            Vec3 at=origin.add(dx,1.3,dz);
            var hit=level.clip(new ClipContext(at,at.add(0,-3.6,0),ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.ANY,net.minecraft.world.phys.shapes.CollisionContext.empty()));
            if(hit.getType()==HitResult.Type.BLOCK && hit.getDirection()==net.minecraft.core.Direction.UP)
                patches.add(new Patch(hit.getLocation().add(0,.028,0),step*.79));
        }
    }
}
