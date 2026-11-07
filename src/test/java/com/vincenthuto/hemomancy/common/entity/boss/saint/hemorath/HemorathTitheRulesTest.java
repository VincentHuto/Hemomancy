package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HemorathTitheRulesTest {
	@Test
	void spendingNearHimAccruesAndTheLitanyRaisesIt() {
		assertEquals(40.0F, HemorathTitheRules.accrue(0.0F, 40.0F, false));
		assertEquals(60.0F, HemorathTitheRules.accrue(0.0F, 40.0F, true));
		assertEquals(10.0F, HemorathTitheRules.accrue(10.0F, -5.0F, false));
		assertEquals(24.0D, HemorathTitheRules.RANGE);
	}

	@Test
	void theReckoningCollectsAsDamageCappedAtSixteen() {
		assertEquals(0.0F, HemorathTitheRules.damage(0.0F));
		assertEquals(3.0F + 400.0F / 80.0F, HemorathTitheRules.damage(400.0F), 1.0E-4F);
		assertEquals(16.0F, HemorathTitheRules.damage(5000.0F));
		assertEquals(0, HemorathTitheRules.weight(149.0F));
		assertEquals(7, HemorathTitheRules.weight(150.0F));
		assertEquals(30, HemorathTitheRules.weight(9000.0F));
		assertEquals(3, HemorathTitheRules.links(399.0F));
		assertEquals(10, HemorathTitheRules.links(5000.0F));
	}

	@Test
	void paymentIsTwentyATickAndNeverMoreThanOwed() {
		assertEquals(20.0F, HemorathTitheRules.pay(300.0F, 20.0F));
		assertEquals(5.0F, HemorathTitheRules.pay(5.0F, 20.0F));
		assertEquals(12.0F, HemorathTitheRules.pay(300.0F, 12.0F));
	}

	@Test
	void theLedgerFindsTheMostIndebtedAndSavesPerPlayer() {
		HemorathTitheLedger ledger = new HemorathTitheLedger();
		UUID a = UUID.randomUUID(), b = UUID.randomUUID();
		ledger.accrue(a, 100.0F, false);
		ledger.accrue(b, 250.0F, false);
		assertEquals(b, ledger.mostIndebted(List.of(a, b)));
		assertEquals(250.0F, ledger.max(List.of(a, b)));
		assertEquals(20.0F, ledger.pay(b, 30.0F));
		assertEquals(230.0F, ledger.debt(b));
		CompoundTag tag = new CompoundTag();
		ledger.save(tag);
		HemorathTitheLedger loaded = new HemorathTitheLedger();
		loaded.load(tag);
		assertEquals(100.0F, loaded.debt(a));
		loaded.clear(a);
		assertEquals(0.0F, loaded.debt(a));
		assertNull(new HemorathTitheLedger().mostIndebted(List.of(a)));
	}
}
