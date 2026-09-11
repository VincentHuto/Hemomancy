package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManipulationBoundaryStyleTest {
    private ManipulationBoundaryStyle style(Form form,float age,int count) {
        return ManipulationBoundaryStyle.forForm(form,3,count,age);
    }
    @Test void fieldRingsKeepTheirNominalRadiusAndCloudGroundOffset() {
        for(Form form:new Form[]{Form.WELL,Form.STILLNESS,Form.BEACON,Form.FURNACE,Form.MAGNET,Form.CLOUD})
            assertEquals(3,style(form,12,1).radius());
        assertEquals(-9.945,style(Form.CLOUD,12,1).height(),1e-6);
        assertNotEquals(style(Form.WELL,12,1).color(),style(Form.FURNACE,12,1).color());
    }
    @Test void expandingRangeCuesKeepTheirOriginalTiming() {
        assertEquals(1.5,style(Form.RUPTURE,4,1).radius());
        assertEquals(2,style(Form.UPDRAFT,6,4).radius());
        assertEquals(4,style(Form.UPDRAFT,24,4).radius());
        assertNull(style(Form.UPDRAFT,12,2));
    }
    @Test void objectRingsAndLocatorsAreNotReplacedByRangeRings() {
        for(Form form:new Form[]{Form.CROWN,Form.FORGE,Form.COMMAND,Form.ORE})assertNull(style(form,12,1));
    }
}
