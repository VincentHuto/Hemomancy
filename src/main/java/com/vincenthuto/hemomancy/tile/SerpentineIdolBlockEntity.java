package com.vincenthuto.hemomancy.tile;

import java.util.List;

import com.mojang.math.Vector3d;
import com.vincenthuto.hemomancy.block.idol.BlockSerpentineIdol;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.init.PotionInit;
import com.vincenthuto.hemomancy.particle.factory.SerpentParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class SerpentineIdolBlockEntity extends BlockEntity {

	public SerpentineIdolBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.serpentine_idol.get(), pos, state);
	}

	@SuppressWarnings("unused")
	public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
		if (level.getBlockEntity(pos)instanceof SerpentineIdolBlockEntity te) {

			if (state.getValue(BlockSerpentineIdol.ACTIVE)) {

				Vector3 centerVec = Vector3.fromBlockEntityCenter(te).add(0, 0.125, 0);
				AABB bb = new AABB(pos).inflate(4);
				List<LivingEntity> ents = level.getEntitiesOfClass(LivingEntity.class, bb);
				ents.forEach(e -> {
					if (!(e instanceof Player)) {
						Vector3 t = new Vector3(e.getX(), e.getY(), e.getZ());
						double distance = centerVec.distanceTo(t) + 0.1D;
						Vector3d r = new Vector3d(t.x - centerVec.x, t.y - centerVec.y, t.z - centerVec.z);
						// e.setDeltaMovement(-r.x / 10.2D / distance * 1.3, -r.y / 10.2D / distance,
						// -r.z / 10.2D / distance * 1.3);

						e.addEffect(new MobEffectInstance(PotionInit.blood_binding.get(), 5));
					}
					// System.out.println(e);

				});
				if (level.isClientSide) {

					double time = level.getGameTime();
					level.addParticle(SerpentParticleFactory.createData(new ParticleColor(50, 50, 50)),
							centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
							centerVec.y + Math.sin(time * 0.1) * 0.55f,
							centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 0, 0f, 0.0f);
					level.addParticle(SerpentParticleFactory.createData(new ParticleColor(100, 0, 0)),
							centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
							centerVec.y + Math.sin(time * 0.1) * 0.55f,
							centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05),0, 0f, 0.0f);
					level.addParticle(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
							centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
							centerVec.y + Math.sin(time * 0.1) * 0.55f,
							centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 0, 0f, 0.0f);
					level.addParticle(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
							centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
							centerVec.y + Math.sin(time * 0.1) * 0.55f,
							centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 0, 0f, 0.0f);
				}
			}

		}

	}

}
