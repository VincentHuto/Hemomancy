package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.AimLayer;
import com.vincenthuto.hemomancy.client.player.CastingTrack;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastLane;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;

/** Lookup of authored tracks: school/rank bases, bespoke styles and Still Art stages. Bespoke tracks are complete and take no aim layer. */
public final class CastingTracks {
    private CastingTracks() {}
    public static CastingTrack base(EnumBloodTendency school,EnumManipulationRank rank) {
        return switch(school) {
            case ANIMUS -> AnimusTracks.get(rank);
            case FLAMMEUS -> FlammeusTracks.get(rank);
            case DUCTILIS -> DuctilisTracks.get(rank);
            case LUX -> LuxTracks.get(rank);
            case MORTEM -> MortemTracks.get(rank);
            case CONGEATIO -> CongeatioTracks.get(rank);
            case FERRIC -> FerricTracks.get(rank);
            case TENEBRIS -> TenebrisTracks.get(rank);
        };
    }
    public static CastingTrack style(CastStyle style) {
        if(style==null || style==CastStyle.SCHOOL)return null;
        var track=StyleTracks.get(style);
        if(track==null)track=PerfectusTracks.get(style);
        if(track==null)track=GameplayTracks.get(style);
        if(track==null)track=ChannelTracks.get(style);
        return track;
    }
    public static boolean bespoke(CastStyle style) { return style!=null && style!=CastStyle.SCHOOL; }
    public static CastingTrack resolve(CastingAnimationPacket packet) {
        if(packet.lane()==CastLane.UNSTAINED)return StillArtTracks.get(packet.stage()==null?EnumClarityStage.AWAKENED:packet.stage());
        var bespoke=style(packet.style());
        return bespoke!=null?bespoke:base(packet.school(),packet.rank());
    }
    /**
     * Whether a packet update must re-resolve the track and aim layer: true when any input of {@link #resolve} or the aim
     * choice differs (manipulation, lane, school, rank, style, purpose, stage, ending). Phase, sequence, clock and charge
     * fields only advance the playback.
     */
    public static boolean presentationChanged(CastingAnimationPacket old,CastingAnimationPacket next) {
        return !java.util.Objects.equals(old.manipulation(),next.manipulation()) || old.lane()!=next.lane()
                || old.school()!=next.school() || old.rank()!=next.rank() || old.style()!=next.style()
                || old.purpose()!=next.purpose() || old.stage()!=next.stage() || old.ending()!=next.ending();
    }
    /** A Still Art's ending is its aim layer; the Harbinger lane takes purpose endings instead, so this is null there. */
    public static AimLayer ending(CastingAnimationPacket packet) {
        return packet.lane()==CastLane.UNSTAINED?StillArtEndings.get(packet.ending()):null;
    }
}
