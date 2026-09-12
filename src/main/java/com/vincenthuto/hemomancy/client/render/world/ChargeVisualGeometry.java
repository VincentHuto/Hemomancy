package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import net.minecraft.world.phys.Vec3;

final class ChargeVisualGeometry {
    private ChargeVisualGeometry() {}

    static Vec3 focus(Form form,Vec3 aim) {
        double distance=switch(form) {
            case IRON_CHARGE -> 1.1;
            case LIGHTNING_CHARGE -> 1.15;
            case THREAD_CHARGE,NEEDLE_CHARGE,FAN_CHARGE,LANCE_CHARGE,MORTAR_CHARGE,ANEURYSM_CHARGE -> 1.2;
            case GAZE_CHARGE -> 1.25;
            default -> throw new IllegalArgumentException("Not a centered charge form: "+form);
        };
        return alongAim(aim,distance);
    }

    static Vec3 alongAim(Vec3 aim,double distance) {
        return aim.normalize().scale(distance);
    }
}
