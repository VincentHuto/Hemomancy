package com.vincenthuto.hemomancy.entity.blood;

import com.vincenthuto.hemomancy.init.EntityInit;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class EntityWretchedWill extends EntityBloodConstruct {
	public float deathTicks = 1;

	public EntityWretchedWill(EntityType<? extends EntityWretchedWill> type, Level worldIn) {
		super(type, worldIn);

	}

	public EntityWretchedWill(Level worldIn, LivingEntity creator) {
		super(EntityInit.wretched_will.get(), worldIn);
		this.creator = creator;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		// Prevents it from falling

	}
	
	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
	}
	
	@Override
	public boolean broadcastToPlayer(ServerPlayer p_19937_) {
		return super.broadcastToPlayer(p_19937_);
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	@Override
	public void onAddedToWorld() {
		super.onAddedToWorld();
		if (this.creator != null) {
			if (creator instanceof Player player) {
				System.out.println(creator);
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		System.out.println(creator);

		if (this.creator != null) {
			if (creator instanceof Player player) {
				Vec3 playerPos = player.getEyePosition();
				this.setPos(playerPos.add(0, 1, 0));
				this.setYBodyRot(player.yHeadRot);
				this.setXRot(player.getXRot());
				/*
				 * if (!player.level.isClientSide) { ServerPlayer sPlay = (ServerPlayer) player;
				 * player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 60));
				 * sPlay.setCamera(this); }
				 */
			}
		}
//		if (tickCount > 100) {
//			this.remove(RemovalReason.KILLED);
//		}
	}

}
