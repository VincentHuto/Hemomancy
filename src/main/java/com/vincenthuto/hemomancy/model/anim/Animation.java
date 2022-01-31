package com.vincenthuto.hemomancy.model.anim;

import net.minecraft.client.model.Model;

public class Animation
{
    private final int duration;

    public Animation(int duration)
    {
        this.duration = duration;
    }

    public void tick(IAnimatable animatable, int time) {}

    public void animate(Model model) {}

    public int getDuration()
    {
        return duration;
    }

    @Override
    public String toString()
    {
        if (this == IAnimatable.NO_ANIMATION) return "Animation{NONE}";
        return "Animation{duration=" + duration + '}';
    }
}