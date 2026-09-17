package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.rite.CardinalRiteStaffPlantingClientState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

public final class CastingPlayerPose {
    private static boolean firstPerson;
    private CastingPlayerPose() {}
    public static void renderFirstPerson(Runnable render) {
        boolean previous=firstPerson;
        firstPerson=true;
        try { render.run(); } finally { firstPerson=previous; }
    }
    public static boolean exclusive(Player player) {
        return player.isSleeping() || CardinalRiteStaffPlantingClientState.isAnimating(player)
                || HematicMicroscopeClientState.animation(player)!=null
                || BloodVialInjectionClientState.animation(player)!=null
                || PlayerAnimationClientState.isBreathing(player);
    }
    public static boolean armAvailable(Player player,HumanoidArm arm,float partial) {
        if(exclusive(player) || LivingStaffMorphClientState.affectsArm(player,arm))return false;
        // Using a bow, map, food or shield owns vanilla hand presentation, including supporting hands.
        if(player.isUsingItem())return false;
        InteractionHand swinging=player.swingingArm==null?InteractionHand.MAIN_HAND:player.swingingArm;
        var swingArm=swinging==InteractionHand.MAIN_HAND?player.getMainArm():player.getMainArm().getOpposite();
        return player.getAttackAnim(partial)<=0 || arm!=swingArm;
    }
    public static float stanceWeight(boolean grounded,boolean specialPose,float horizontalSpeed,float walkAmount) {
        return grounded && !specialPose ? Math.clamp(1-Math.max(horizontalSpeed*20,walkAmount*4),0,1):0;
    }
    public static void apply(Player player,HumanoidModel<?> model,float partial) {
        if(firstPerson)return;
        var animation=CastingAnimationClientState.animation(player);
        if(animation==null || exclusive(player))return;
        boolean right=armAvailable(player,HumanoidArm.RIGHT,partial),left=armAvailable(player,HumanoidArm.LEFT,partial);
        if(!right && !left)return;
        var pose=animation.pose(partial,false);
        if(player.getMainArm()==HumanoidArm.LEFT)pose=pose.mirror();
        float weight=animation.weight(partial);
        boolean special=player.isPassenger() || player.isSwimming() || player.isFallFlying()
                || player.isVisuallyCrawling();
        float stance=stanceWeight(player.onGround(),special,(float)player.getDeltaMovement().horizontalDistance(),
                player.walkAnimation.speed(partial));
        if(right)rotate(model.rightArm,pose.rightArm(),weight);
        if(left)rotate(model.leftArm,pose.leftArm(),weight);
        if(right && left && !special && !player.isUsingItem() && player.getAttackAnim(partial)<=0) {
            // Keep vanilla torso pitch/pivot, including crouching. Pitching this part around its
            // shoulder origin pulls its lower edge away from the fixed leg/waist attachment.
            model.body.yRot+=pose.body().y()*weight;
            model.head.xRot+=pose.head().x()*weight;
            model.head.yRot+=pose.head().y()*weight;
            model.rightArm.x=-(float)Math.cos(model.body.yRot)*5;
            model.rightArm.z=(float)Math.sin(model.body.yRot)*5;
            model.leftArm.x=(float)Math.cos(model.body.yRot)*5;
            model.leftArm.z=-(float)Math.sin(model.body.yRot)*5;
        }
        rotate(model.rightLeg,pose.rightLeg(),weight*stance);
        rotate(model.leftLeg,pose.leftLeg(),weight*stance);
        model.hat.copyFrom(model.head);
        if(model instanceof PlayerModel<?> p) {
            p.rightSleeve.copyFrom(model.rightArm); p.leftSleeve.copyFrom(model.leftArm);
            p.rightPants.copyFrom(model.rightLeg); p.leftPants.copyFrom(model.leftLeg); p.jacket.copyFrom(model.body);
        }
    }
    private static void rotate(ModelPart part,CastingPose.Rotation r,float t) {
        part.xRot+=(r.x()-part.xRot)*t; part.yRot+=(r.y()-part.yRot)*t; part.zRot+=(r.z()-part.zRot)*t;
    }
}
