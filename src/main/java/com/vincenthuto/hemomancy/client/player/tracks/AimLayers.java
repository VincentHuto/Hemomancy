package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.AimLayer;
import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingTrack.*;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPurpose;
import java.util.EnumMap;
import java.util.List;
import static com.vincenthuto.hemomancy.client.player.CastingPose.pose;
import static com.vincenthuto.hemomancy.client.player.HandPose.hand;

/** Purpose endings. Only the lead arm and head (third) and lead hand (first) are read from these poses. */
public final class AimLayers {
    private static final EnumMap<CastPurpose,AimLayer> PURPOSES=new EnumMap<>(CastPurpose.class);
    static {
        // pose(head, lean, twist, rightX, rightZ, leftX, leftZ, stance): positive right z is inward across the chest.
        put(CastPurpose.PROJECTILE,
            third(k(0,pose(0,0,0,-90,0,0,0,0),Easing.LINEAR),k(6,pose(0,0,0,-90,0,0,0,0),Easing.LINEAR)),
            first(k(0,hand(-.22,.08,-.12,-10,0,0),Easing.LINEAR),k(1,hand(-.24,.09,-.18,-12,0,0),Easing.EASE_OUT),k(6,hand(-.22,.08,-.12,-10,0,0),Easing.SMOOTH)));
        put(CastPurpose.SELF,
            third(k(0,pose(2,0,0,-60,10,0,0,0),Easing.LINEAR),k(6,pose(2,0,0,-60,10,0,0,0),Easing.LINEAR)),
            first(k(0,hand(-.15,-.08,.05,8,0,6),Easing.LINEAR),k(6,hand(-.15,-.08,.05,8,0,6),Easing.LINEAR)));
        put(CastPurpose.AREA,
            third(k(0,pose(-2,0,0,-85,-20,0,0,0),Easing.LINEAR),k(6,pose(-2,0,0,-85,-60,0,0,0),Easing.EASE_OUT)),
            first(k(0,hand(.05,.05,-.05,-6,0,-8),Easing.LINEAR),k(6,hand(.25,.05,-.05,-6,0,-14),Easing.EASE_OUT)));
        put(CastPurpose.TARGETED,
            third(k(0,pose(0,0,0,-90,0,0,0,0),Easing.LINEAR),k(6,pose(0,0,0,-90,0,0,0,0),Easing.LINEAR)),
            first(k(0,hand(-.22,.08,-.12,-10,0,0),Easing.LINEAR),k(6,hand(-.22,.08,-.12,-10,0,0),Easing.LINEAR)));
        put(CastPurpose.CONSTRUCTION,
            third(k(0,pose(4,0,0,-80,-10,0,0,0),Easing.LINEAR),k(6,pose(4,0,0,-80,-55,0,0,0),Easing.SMOOTH)),
            first(k(0,hand(-.10,-.10,-.06,4,0,-4),Easing.LINEAR),k(6,hand(.25,-.10,-.06,4,0,-12),Easing.SMOOTH)));
        put(CastPurpose.CONJURATION,
            third(k(0,pose(0,0,0,-70,-15,0,0,0),Easing.LINEAR),k(6,pose(0,0,0,-70,-15,0,0,0),Easing.LINEAR)),
            first(k(0,hand(-.02,-.05,0,0,0,5),Easing.LINEAR),k(6,hand(-.02,-.05,0,0,0,5),Easing.LINEAR)));
        put(CastPurpose.TRAVEL,
            third(k(0,pose(0,0,0,-80,25,0,0,0),Easing.LINEAR),k(6,pose(0,0,0,-80,25,0,0,0),Easing.LINEAR)),
            first(k(0,hand(-.28,.02,-.02,-4,-20,4),Easing.LINEAR),k(6,hand(-.28,.02,-.02,-4,-20,4),Easing.LINEAR)));
        // TRAVEL head: the head keyframe above yaws toward the destination via the base track's twist; see §9.
    }
    private AimLayers() {}
    public static AimLayer purpose(CastPurpose purpose) { return PURPOSES.get(purpose); }
    private static void put(CastPurpose p,Segment<CastingPose> third,Segment<HandPose> first) { PURPOSES.put(p,new AimLayer(third,first)); }
    private static <P extends com.vincenthuto.hemomancy.client.player.Blendable<P>> Keyframe<P> k(int t,P pose,Easing e) { return new Keyframe<>(t,pose,e); }
    @SafeVarargs private static Segment<CastingPose> third(Keyframe<CastingPose>... f) { return new Segment<>(List.of(f)); }
    @SafeVarargs private static Segment<HandPose> first(Keyframe<HandPose>... f) { return new Segment<>(List.of(f)); }
}
