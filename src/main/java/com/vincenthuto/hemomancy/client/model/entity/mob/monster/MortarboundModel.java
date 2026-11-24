package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.vincenthuto.hemomancy.common.entity.mob.monster.MortarboundEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class MortarboundModel extends HierarchicalModel<MortarboundEntity> {
    // A centered entity is eight model units from the back wall.
    private static final float WALL_Z = 8F;
    private static final float CENTER_Y = -24F;

    private final ModelPart root = MortarboundMesh.createRoot();
    private final ModelPart emerged = root.getChild("emerged");
    private final ModelPart embedded = root.getChild("embedded");
    private final ModelPart body = emerged.getChild("body");
    private final ModelPart head = emerged.getChild("head");
    private final ModelPart claw = emerged.getChild("ambush_claw");

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(MortarboundEntity entity, float limbSwing, float limbSwingAmount,
                          float age, float headYaw, float headPitch) {
        float partialTick = HemoEasing.clamp01(age - entity.tickCount);
        pose(entity.emergence(partialTick), entity.attackAnimationTicks(), partialTick, age, limbSwingAmount);
    }

    void pose(float emergence, int attackTicks, float partialTick, float age, float crawlAmount) {
        float unfold = HemoEasing.smoothstep(emergence);
        emerged.visible = unfold > 0F;
        embedded.visible = unfold < 1F;

        // Keep both profiles pinned to the same wall plane as they unfold and recede.
        scaleAtWall(emerged, unfold, unfold, unfold);
        float retract = 1F - unfold;
        float crawl = Mth.sin(age * .35F) * Math.min(crawlAmount, 1F);
        scaleAtWall(embedded, retract * (1F + .045F * crawl), retract * (1F - .04F * crawl), retract);
        embedded.z -= .125F;

        float sweep = clawSweep(attackTicks, partialTick);
        float breath = Mth.sin(age * .09F);
        body.xRot = .024F * breath;
        head.xRot = .06F * breath + .13F * sweep;
        head.zRot = .035F * Mth.sin(age * .14F);
        claw.xRot = -.28F * sweep + .03F * breath;
        claw.yRot = -.55F * sweep;
        claw.zRot = .17F * sweep;
    }

    static float clawSweep(int ticks, float partialTick) {
        if (ticks < 0) {
            return 1F - HemoEasing.smoothstep((MortarboundEntity.STRIKE_RECOVERY_TICKS + ticks + partialTick)
                    / MortarboundEntity.STRIKE_RECOVERY_TICKS);
        }
        if (ticks == 0) return 0F;

        float remaining = ticks - partialTick;
        float drawBack = HemoEasing.ramp(MortarboundEntity.STRIKE_WINDUP_TICKS - remaining, 0F, 11F);
        float release = 1F - HemoEasing.ramp(remaining, 0F, 5F);
        return HemoEasing.lerp(-.7F * drawBack, 1F, release);
    }

    private static void scaleAtWall(ModelPart part, float width, float height, float depth) {
        part.xScale = width;
        part.yScale = height;
        part.zScale = depth;
        part.y = CENTER_Y * (1F - height);
        part.z = WALL_Z * (1F - depth);
    }
}
