package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.player.CastingTrack.Segment;

/** Directional ending keyed on release time: lead arm and head in third person, lead hand in first. */
public record AimLayer(Segment<CastingPose> third, Segment<HandPose> first) {}
