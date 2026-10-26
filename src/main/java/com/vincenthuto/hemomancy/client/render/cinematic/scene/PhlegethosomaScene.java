package com.vincenthuto.hemomancy.client.render.cinematic.scene;
import com.vincenthuto.hemomancy.client.render.cinematic.*;
import net.minecraft.world.entity.Entity;
public final class PhlegethosomaScene implements BossScene {
    @Override public int length() {return 60;}
    @Override public TitleCard card(Entity boss) {return TitleCard.of("phlegethosoma",4,40,0xC84330);}
}
