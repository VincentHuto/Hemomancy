package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("removal")
public final class ScarletMummerGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private ScarletMummerGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void factoryCreatesOwnedScarletMummer(GameTestHelper helper) {
		ServerPlayer owner = owner(helper);
		Mob mummer = createMummer(helper, owner);
		try {
			helper.assertTrue(mummer != null, "Scarlet Mummer factory path must create a body");
			BoundPuppeteerSummon bound = (BoundPuppeteerSummon) mummer;
			helper.assertTrue(owner.getUUID().equals(bound.hemomancy$getOwnerUUID()),
					"Scarlet Mummer must retain its owner");
			helper.assertTrue(PuppeteerSummonDefinitions.SCARLET_MUMMER.equals(bound.hemomancy$getSummonName()),
					"Scarlet Mummer must retain its stable summon id");
			helper.succeed();
		} finally {
			if (mummer != null) mummer.discard();
			owner.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void performanceRedirectsFourOwnerThreatsThenReleasesThem(GameTestHelper helper) {
		ServerPlayer owner = owner(helper);
		Mob mummer = createMummer(helper, owner);
		List<Zombie> threats = threats(helper, owner, 5);
		helper.assertTrue(mummer != null, "Scarlet Mummer fixture must create");
		helper.getLevel().addFreshEntity(mummer);

		try {
			mummer.tickCount = 160;
			tickPerformance(mummer, owner);
			long redirected = threats.stream().filter(threat -> threat.getTarget() == mummer).count();
			helper.assertTrue(redirected == 4, "Performance must redirect exactly four owner threats");
			for (int i = 0; i < 80; i++) {
				mummer.tickCount++;
				tickPerformance(mummer, owner);
			}
			redirected = threats.stream().filter(threat -> threat.getTarget() == mummer).count();
			helper.assertTrue(redirected == 0, "Redirected threats must be released after the performance");
			helper.succeed();
		} finally {
			mummer.discard();
			threats.forEach(Mob::discard);
			owner.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void performanceEvadesOnlyTheFirstMeleeHit(GameTestHelper helper) {
		floor(helper);
		ServerPlayer owner = owner(helper);
		Mob mummer = createMummer(helper, owner);
		Zombie threat = threats(helper, owner, 1).getFirst();
		helper.assertTrue(mummer != null, "Scarlet Mummer fixture must create");
		mummer.setPos(helper.absolutePos(new BlockPos(4, 2, 4)).getCenter());
		helper.getLevel().addFreshEntity(mummer);

		try {
			mummer.tickCount = 160;
			tickPerformance(mummer, owner);
			float health = mummer.getHealth();
			var position = mummer.position();
			mummer.hurt(mummer.damageSources().mobAttack(threat), 4.0F);
			helper.assertTrue(mummer.getHealth() == health && !mummer.position().equals(position),
					"First melee hit during performance must be canceled by a safe sidestep");
			mummer.hurt(mummer.damageSources().mobAttack(threat), 4.0F);
			helper.assertTrue(mummer.getHealth() < health,
					"A second melee hit in the same performance must deal damage");
			helper.succeed();
		} finally {
			mummer.discard();
			threat.discard();
			owner.discard();
		}
	}

	private static ServerPlayer owner(GameTestHelper helper) {
		ServerPlayer owner = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "scarlet-mummer-test-player"),
				ClientInformation.createDefault()) {
			@Override
			protected ItemCooldowns createItemCooldowns() {
				return new ItemCooldowns();
			}

			@Override
			public void displayClientMessage(Component message, boolean overlay) {
			}
		};
		owner.setPos(helper.absolutePos(new BlockPos(4, 2, 4)).getCenter());
		return owner;
	}

	private static Mob createMummer(GameTestHelper helper, ServerPlayer owner) {
		PuppeteerSummonDefinition definition = PuppeteerSummonDefinitions
				.byName(PuppeteerSummonDefinitions.SCARLET_MUMMER).orElseThrow();
		return PuppeteerSummonFactory.create(definition, helper.getLevel(), owner,
				UUID.randomUUID(), 0).orElse(null);
	}

	private static void tickPerformance(Mob mummer, ServerPlayer owner) {
		try {
			var method = mummer.getClass().getDeclaredMethod("tickPerformance", ServerPlayer.class);
			method.trySetAccessible();
			method.invoke(mummer, owner);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Scarlet Mummer must expose its internal performance tick", exception);
		}
	}

	private static List<Zombie> threats(GameTestHelper helper, ServerPlayer owner, int count) {
		List<Zombie> threats = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Zombie threat = EntityType.ZOMBIE.create(helper.getLevel());
			if (threat == null) throw new AssertionError("Zombie fixture must create");
			threat.setNoAi(true);
			threat.setPos(helper.absolutePos(new BlockPos(2 + i, 2, 5)).getCenter());
			threat.setTarget(owner);
			helper.getLevel().addFreshEntity(threat);
			threats.add(threat);
		}
		return threats;
	}

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x <= 8; x++) for (int z = 0; z <= 8; z++) {
			helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
					Blocks.STONE.defaultBlockState());
		}
	}
}
