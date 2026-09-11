package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;

/** Visual range rings use the spell's nominal radius; server targeting is unchanged. */
record ManipulationBoundaryStyle(float radius,double height,int color) {
    static ManipulationBoundaryStyle forForm(Form form,float r,int count,float age) {
        return switch(form) {
            case WELL -> new ManipulationBoundaryStyle(r,.055,0x767181);
            case STILLNESS -> new ManipulationBoundaryStyle(r,.055,0xB6DBE8);
            case BEACON -> new ManipulationBoundaryStyle(r,.055,0xFFF5DD);
            case FURNACE -> new ManipulationBoundaryStyle(r,.055,0xD3754D);
            case CLOUD -> new ManipulationBoundaryStyle(r,-9.945,0xD73750);
            case MAGNET -> new ManipulationBoundaryStyle(r,.055,0xCE9D9D);
            case RUPTURE -> new ManipulationBoundaryStyle(r*Math.min(1,age/8),.055,0xB40C38);
            case UPDRAFT -> count==4?new ManipulationBoundaryStyle(4*Math.min(1,age/12),.055,0xFFAF8A):
                    count==3?new ManipulationBoundaryStyle(.7F,.055,0xFFD6A0):null;
            default -> null;
        };
    }
}
