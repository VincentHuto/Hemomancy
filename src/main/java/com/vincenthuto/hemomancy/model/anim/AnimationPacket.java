package com.vincenthuto.hemomancy.model.anim;

import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;

import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hutoslib.client.HLClientUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class AnimationPacket {
	private final int entityID, animationIndex;

	public AnimationPacket(int entityID, int index) {
		this.entityID = entityID;
		this.animationIndex = index;
	}

	public AnimationPacket(FriendlyByteBuf buf) {
		entityID = buf.readInt();
		animationIndex = buf.readInt();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(entityID);
		buf.writeInt(animationIndex);
	}

	public boolean handle(Supplier<NetworkEvent.Context> context) {
		return DistExecutor.unsafeCallWhenOn(Dist.CLIENT,
				() -> () -> handleAnimationPacket(entityID, animationIndex));
	}

	public static <T extends Entity & IAnimatable> void send(T entity, Animation animation) {
		if (!entity.level.isClientSide) {
			entity.setAnimation(animation);
			PacketHandler.CHANNELMAIN.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
					new AnimationPacket(entity.getId(), ArrayUtils.indexOf(entity.getAnimations(), animation)));
		}
	}

	public static boolean handleAnimationPacket(int entityID, int animationIndex) {
		Level world = Minecraft.getInstance().level;
		IAnimatable entity = (IAnimatable) world.getEntity(entityID);

		if (animationIndex < 0)
			entity.setAnimation(IAnimatable.NO_ANIMATION);
		else
			entity.setAnimation(entity.getAnimations()[animationIndex]);
		return true;
	}
}
