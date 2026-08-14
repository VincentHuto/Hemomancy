package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityWretchedWill extends BloodConstructEntity {
	public EntityWretchedWill(EntityType<? extends EntityWretchedWill> type, Level worldIn) {
		super(type, worldIn);

	}

	public EntityWretchedWill(Level worldIn, LivingEntity creator) {
		super(EntityInit.wretched_will.get(), worldIn);
		this.creator = creator;
	}

	@Override
	public void tick() {
		super.tick();

		if (creator instanceof Player player) {
			Vec3 playerPos = player.getEyePosition();
			this.setPos(playerPos.add(0, 1, 0));
			this.setYBodyRot(player.yHeadRot);
			this.setXRot(player.getXRot());
		}
	}

}
