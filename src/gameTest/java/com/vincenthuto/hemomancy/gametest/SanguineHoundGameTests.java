package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.SanguineHoundEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("removal")
public final class SanguineHoundGameTests {
	private SanguineHoundGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
	public static void ruptureCreatesEphemeralBloodCurs(GameTestHelper helper) {
		SanguineHoundEntity hound = EntityInit.sanguine_hound.get().create(helper.getLevel());
		helper.assertTrue(hound != null, "Sanguine Hound fixture must create");
		hound.setPos(helper.absolutePos(new BlockPos(4, 2, 4)).getCenter());
		hound.hemomancy$setOwnerUUID(UUID.randomUUID());
		helper.getLevel().addFreshEntity(hound);
		hound.ruptureIntoCurs();

		List<SanguineHoundEntity> curs = helper.getLevel().getEntitiesOfClass(SanguineHoundEntity.class,
				hound.getBoundingBox().inflate(4.0D), SanguineHoundEntity::isBloodCur);
		helper.assertTrue(curs.size() >= 3 && curs.size() <= 5,
				"Rupture must create three to five blood curs");
		helper.assertTrue(curs.stream().allMatch(cur -> cur.hemomancy$getOwnerUUID() == null),
				"Blood curs must not consume persistent summon slots");
		curs.forEach(SanguineHoundEntity::discard);
		helper.succeed();
	}
}
