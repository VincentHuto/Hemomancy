package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IronHeartAttachmentTest {
    @Test void anchorStaysLeftAndForwardThroughFullTurn() {
        for(int yaw=0;yaw<360;yaw+=15) {
            PoseStack poses=new PoseStack();
            IronHeartAttachment.apply(poses,yaw,1,false);
            Vector3f anchor=poses.last().pose().transformPosition(new Vector3f());
            double angle=Math.toRadians(yaw);
            assertEquals(.17,anchor.x*Math.cos(angle)+anchor.z*Math.sin(angle),1e-6);
            assertEquals(.35,-anchor.x*Math.sin(angle)+anchor.z*Math.cos(angle),1e-6);
            assertEquals(1.25,anchor.y,1e-6);
            Vector3f facing=poses.last().pose().transformDirection(new Vector3f(0,0,1));
            assertEquals(-Math.sin(angle),facing.x,1e-6);
            assertEquals(Math.cos(angle),facing.z,1e-6);
        }
    }

    @Test void crouchedAndScaledBodyKeepsItsAttachment() {
        PoseStack poses=new PoseStack();
        IronHeartAttachment.apply(poses,0,2,true);
        Vector3f anchor=poses.last().pose().transformPosition(new Vector3f());
        assertEquals(.34,anchor.x,1e-6);
        assertEquals(2.04,anchor.y,1e-6);
        assertEquals(.84,anchor.z,1e-6);
    }
}
