package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GearScoreCalculatorTest
{
	@Test
	public void wave1WeightsMatchTheCoefficientTable()
	{
		GearScoreCoefficients coefficients = GearScoreCoefficients.wave1();
		assertEquals(0, new BigDecimal("0.1").compareTo(coefficients.getAttackPerPoint()));
		assertEquals(0, new BigDecimal("0.1").compareTo(coefficients.getDefencePerPoint()));
		assertEquals(0, new BigDecimal("1.0").compareTo(coefficients.getMeleeStrengthPerPoint()));
		assertEquals(0, new BigDecimal("1.0").compareTo(coefficients.getRangedStrengthPerPoint()));
		assertEquals(0, new BigDecimal("1.0").compareTo(coefficients.getMagicDamagePerPoint()));
		assertEquals(0, new BigDecimal("0.5").compareTo(coefficients.getPrayerPerPoint()));
	}

	@Test
	public void slotIndexesMatchRuneLiteEquipmentSlots()
	{
		assertEquals(EquipmentInventorySlot.HEAD.getSlotIdx(), EquipmentSlot.HEAD);
		assertEquals(EquipmentInventorySlot.WEAPON.getSlotIdx(), EquipmentSlot.WEAPON);
		assertEquals(EquipmentInventorySlot.BODY.getSlotIdx(), EquipmentSlot.BODY);
		assertEquals(EquipmentInventorySlot.SHIELD.getSlotIdx(), EquipmentSlot.SHIELD);
		assertEquals(EquipmentInventorySlot.RING.getSlotIdx(), EquipmentSlot.RING);
		assertEquals(EquipmentInventorySlot.AMMO.getSlotIdx(), EquipmentSlot.AMMO);
	}

	@Test
	public void tenAttackPointsScoreOneAndEveryStyleCounts()
	{
		GearScoreCalculator calculator = GearScoreCalculator.wave1();
		assertDisplayed("1.0", calculator.score(worn(item(3, 1)), source(1, attack(10, 0, 0, 0, 0))));
		assertDisplayed("1.0", calculator.score(worn(item(3, 2)), source(2, attack(0, 0, 0, 10, 0))));
		assertDisplayed("1.0", calculator.score(worn(item(3, 3)), source(3, attack(2, 2, 2, 2, 2))));
		assertDisplayed("2.0", calculator.score(worn(item(3, 4)), source(4, attack(0, 0, 0, 10, 10))));
	}

	@Test
	public void positiveBonusesUseTheWave1Weights()
	{
		EquipmentBonus bonus = bonus(EquipmentSlot.WEAPON, false,
			10, 0, 0, 0, 0,
			10, 0, 0, 0, 0,
			3, 4, "2.5", 3);
		GearScoreBreakdown score = GearScoreCalculator.wave1().score(worn(item(EquipmentSlot.WEAPON, 1)), source(1, bonus));

		assertDisplayed("13.0", score);
		assertEquals(0, score.getSetBonus().compareTo(BigDecimal.ZERO));
		assertEquals(0, score.getWeaponPassive().compareTo(BigDecimal.ZERO));
		assertEquals(0, score.displayedTotal().compareTo(score.getBaseEquipment()));
		assertTrue(score.isComplete());
	}

	@Test
	public void negativeBonusesStayNegative()
	{
		EquipmentBonus bonus = bonus(EquipmentSlot.BODY, false,
			0, 0, 0, 0, 0,
			0, 0, 0, -65, 0,
			0, 0, "0", 0);
		GearScoreBreakdown score = GearScoreCalculator.wave1().score(worn(item(EquipmentSlot.BODY, 7)), source(7, bonus));

		assertDisplayed("-6.5", score);
		assertTrue(score.isComplete());
		assertTrue(score.displayedTotal().signum() < 0);
	}

	@Test
	public void emptyEquipmentScoresZero()
	{
		GearScoreCalculator calculator = GearScoreCalculator.wave1();
		assertDisplayed("0.0", calculator.score(Collections.<WornItem>emptyList(), source()));
		assertDisplayed("0.0", calculator.score(null, null));
		assertTrue(calculator.score(worn(item(EquipmentSlot.WEAPON, -1)), source()).isComplete());
	}

	@Test
	public void mixedEquipmentSumsEachSlotOnce()
	{
		MapSource source = source(
			1, meleeSlash(50, 40),
			2, defence(0, 0, 0, -20, 0),
			3, prayer(2),
			4, rangedStrength(10));
		List<WornItem> worn = worn(
			item(EquipmentSlot.WEAPON, 1),
			item(EquipmentSlot.BODY, 2),
			item(EquipmentSlot.HEAD, 3),
			item(EquipmentSlot.AMMO, 4));

		assertDisplayed("63.0", GearScoreCalculator.wave1().score(worn, source));
	}

	@Test
	public void verifiedZeroIsNotUnknown()
	{
		EquipmentBonus zero = bonus(EquipmentSlot.RING, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "0", 0);
		GearScoreBreakdown score = GearScoreCalculator.wave1().score(worn(item(EquipmentSlot.RING, 9)), source(9, zero));
		assertDisplayed("0.0", score);
		assertTrue(score.isComplete());
		assertEquals(0, score.getUnknownItems());
	}

	@Test
	public void unknownItemIsNotCountedAsZero()
	{
		GearScoreBreakdown score = GearScoreCalculator.wave1().score(
			worn(item(EquipmentSlot.WEAPON, 5), item(EquipmentSlot.SHIELD, 6)),
			source(6, defence(10, 0, 0, 0, 0)));

		assertFalse(score.isComplete());
		assertEquals(1, score.getUnknownItems());
		assertDisplayed("1.0", score);
		assertEquals("Gear Score: 1.0 ?", GearScoreText.equipmentLine(score));
	}

	@Test
	public void unknownEquipmentAloneIsNotAVerifiedZero()
	{
		GearScoreBreakdown score = GearScoreCalculator.wave1().score(worn(item(EquipmentSlot.HEAD, 5)), source());
		assertDisplayed("0.0", score);
		assertFalse(score.isComplete());
		assertEquals("Gear Score: 0.0 ?", GearScoreText.equipmentLine(score));
	}

	@Test
	public void decimalRoundingUsesHalfUp()
	{
		GearScoreCalculator calculator = GearScoreCalculator.wave1();
		assertDisplayed("0.0", calculator.score(worn(item(3, 1)), source(1, magic("0.04"))));
		assertDisplayed("0.1", calculator.score(worn(item(3, 2)), source(2, magic("0.05"))));
		assertDisplayed("0.1", calculator.score(worn(item(3, 3)), source(3, magic("0.14"))));
		assertDisplayed("0.2", calculator.score(worn(item(3, 4)), source(4, magic("0.15"))));
		assertDisplayed("1.3", calculator.score(worn(item(3, 5)), source(5, magic("1.25"))));
		assertDisplayed("-1.3", calculator.score(worn(item(3, 6)), source(6, magic("-1.25"))));
	}

	@Test
	public void coefficientsCanChangeWithoutRewritingTheSum()
	{
		GearScoreCoefficients doubledAttack = new GearScoreCoefficients(
			new BigDecimal("1.0"),
			new BigDecimal("0.1"),
			new BigDecimal("1.0"),
			new BigDecimal("1.0"),
			new BigDecimal("1.0"),
			new BigDecimal("0.5"));
		GearScoreBreakdown score = new GearScoreCalculator(doubledAttack)
			.score(worn(item(3, 1)), source(1, attack(1, 0, 0, 0, 0)));
		assertDisplayed("1.0", score);
	}

	@Test
	public void equipmentChangeReplacesThePreviousTotal()
	{
		GearScoreCalculator calculator = GearScoreCalculator.wave1();
		MapSource source = source(1, melee(10), 2, meleeSlash(18, 4));
		assertDisplayed("10.0", calculator.score(worn(item(EquipmentSlot.WEAPON, 1)), source));
		assertDisplayed("18.4", calculator.score(worn(item(EquipmentSlot.WEAPON, 2)), source));
	}

	@Test
	public void twoHandedWeaponRemovesTheShield()
	{
		MapSource source = source(
			1, melee(10),
			2, defence(40, 0, 0, 0, 0),
			3, twoHandMelee(20));
		List<WornItem> worn = worn(item(EquipmentSlot.WEAPON, 1), item(EquipmentSlot.SHIELD, 2));

		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(3, worn, source);
		assertEquals(GearScoreComparison.Verdict.BETTER, comparison.getVerdict());
		assertEquals("20.0", comparison.getItemScore().toPlainString());
		assertEquals("6.0", comparison.getDelta().toPlainString());
	}

	@Test
	public void shieldReplacesATwoHandedWeapon()
	{
		MapSource source = source(1, twoHandMelee(20), 2, defence(40, 0, 0, 0, 0));
		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(
			2,
			worn(item(EquipmentSlot.WEAPON, 1)),
			source);

		assertEquals(GearScoreComparison.Verdict.WORSE, comparison.getVerdict());
		assertEquals("-16.0", comparison.getDelta().toPlainString());
	}

	@Test
	public void oneHandedWeaponLeavesTheShieldEquipped()
	{
		MapSource source = source(1, melee(10), 2, defence(40, 0, 0, 0, 0), 3, melee(12));
		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(
			3,
			worn(item(EquipmentSlot.WEAPON, 1), item(EquipmentSlot.SHIELD, 2)),
			source);

		assertEquals("2.0", comparison.getDelta().toPlainString());
	}

	@Test
	public void shieldDoesNotRemoveAOneHandedWeapon()
	{
		MapSource source = source(1, melee(10), 2, defence(40, 0, 0, 0, 0));
		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(
			2,
			worn(item(EquipmentSlot.WEAPON, 1)),
			source);

		assertEquals("4.0", comparison.getDelta().toPlainString());
	}

	@Test
	public void ammunitionComparesOnlyTheAmmoSlot()
	{
		MapSource source = source(
			1, melee(10),
			2, defence(40, 0, 0, 0, 0),
			3, rangedStrength(7),
			4, rangedStrength(4));
		List<WornItem> worn = worn(
			item(EquipmentSlot.WEAPON, 1),
			item(EquipmentSlot.SHIELD, 2),
			item(EquipmentSlot.AMMO, 3));

		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(4, worn, source);
		assertEquals("-3.0", comparison.getDelta().toPlainString());
		assertEquals(GearScoreComparison.Verdict.WORSE, comparison.getVerdict());
	}

	@Test
	public void emptyAmmoSlotComparesAgainstZero()
	{
		MapSource source = source(1, defence(30, 0, 0, 0, 0), 2, rangedStrength(7));
		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(
			2,
			worn(item(EquipmentSlot.SHIELD, 1)),
			source);

		assertEquals("7.0", comparison.getDelta().toPlainString());
		assertEquals(GearScoreComparison.Verdict.BETTER, comparison.getVerdict());
	}

	@Test
	public void twoHandedFlagOnAmmoDoesNotRemoveTheShield()
	{
		EquipmentBonus ammo = bonus(EquipmentSlot.AMMO, true, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, "0", 0);
		MapSource source = source(1, defence(50, 0, 0, 0, 0), 2, ammo);
		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(
			2,
			worn(item(EquipmentSlot.SHIELD, 1)),
			source);

		assertEquals("5.0", comparison.getDelta().toPlainString());
	}

	@Test
	public void ringComparisonDoesNotTouchTheWeapon()
	{
		MapSource source = source(1, melee(10), 2, prayer(2), 3, prayer(4));
		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(
			3,
			worn(item(EquipmentSlot.WEAPON, 1), item(EquipmentSlot.RING, 2)),
			source);

		assertEquals("1.0", comparison.getDelta().toPlainString());
		assertEquals("2.0", comparison.getItemScore().toPlainString());
	}

	@Test
	public void unknownReplacedItemBlocksTheComparison()
	{
		MapSource source = source(2, melee(8));
		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(
			2,
			worn(item(EquipmentSlot.WEAPON, 1)),
			source);

		assertFalse(comparison.isItemKnown() && comparison.getDelta() != null);
		assertTrue(comparison.isItemKnown());
		assertNull(comparison.getDelta());
		assertEquals(GearScoreComparison.Verdict.UNKNOWN, comparison.getVerdict());
		assertEquals("8.0", comparison.getItemScore().toPlainString());
	}

	@Test
	public void unknownShieldBlocksATwoHandedComparison()
	{
		MapSource source = source(3, twoHandMelee(20));
		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(
			3,
			worn(item(EquipmentSlot.WEAPON, 1), item(EquipmentSlot.SHIELD, 2)),
			source);

		assertNull(comparison.getDelta());
		assertEquals("20.0", comparison.getItemScore().toPlainString());
	}

	@Test
	public void unknownWeaponBlocksAShieldComparison()
	{
		MapSource source = source(2, defence(10, 0, 0, 0, 0));
		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(
			2,
			worn(item(EquipmentSlot.WEAPON, 1)),
			source);

		assertNull(comparison.getDelta());
	}

	@Test
	public void unknownGearInAnotherSlotStillAllowsAKnownSwap()
	{
		MapSource source = source(2, rangedStrength(10), 3, rangedStrength(4));
		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(
			3,
			worn(item(EquipmentSlot.WEAPON, 1), item(EquipmentSlot.AMMO, 2)),
			source);

		assertEquals(GearScoreComparison.Verdict.WORSE, comparison.getVerdict());
		assertEquals("-6.0", comparison.getDelta().toPlainString());
	}

	@Test
	public void hoveringAnUnknownIdDoesNotInventAScore()
	{
		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(99, worn(), source(1, melee(5)));
		assertFalse(comparison.isItemKnown());
		assertNull(comparison.getItemScore());
		assertNull(comparison.getDelta());
	}

	@Test
	public void equalSwapHasNoDelta()
	{
		MapSource source = source(1, melee(10), 2, melee(10));
		GearScoreComparison comparison = GearScoreCalculator.wave1().compare(
			2,
			worn(item(EquipmentSlot.WEAPON, 1)),
			source);

		assertEquals(GearScoreComparison.Verdict.EQUAL, comparison.getVerdict());
		assertEquals("0.0", comparison.getDelta().toPlainString());
	}

	@Test
	public void setBonusAndWeaponPassiveAreAddedAndWave1LeavesThemAtZero()
	{
		SetBonus set = new SetBonus()
		{
			@Override
			public BigDecimal gearScore(List<WornItem> equipment, ItemStatSource stats)
			{
				return new BigDecimal("2.0");
			}
		};
		WeaponPassive passive = new WeaponPassive()
		{
			@Override
			public BigDecimal gearScore(List<WornItem> equipment, ItemStatSource stats)
			{
				return new BigDecimal("0.5");
			}
		};
		GearScoreCalculator calculator = new GearScoreCalculator(
			GearScoreCoefficients.wave1(),
			Collections.singletonList(set),
			Collections.singletonList(passive));

		GearScoreBreakdown score = calculator.score(worn(item(EquipmentSlot.WEAPON, 1)), source(1, melee(1)));
		assertEquals("1.0", score.getBaseEquipment().toPlainString());
		assertEquals("2.0", score.getSetBonus().toPlainString());
		assertEquals("0.5", score.getWeaponPassive().toPlainString());
		assertDisplayed("3.5", score);

		GearScoreBreakdown wave1 = GearScoreCalculator.wave1().score(worn(item(EquipmentSlot.WEAPON, 1)), source(1, melee(1)));
		assertDisplayed("1.0", wave1);
		assertEquals(0, wave1.getSetBonus().compareTo(BigDecimal.ZERO));
		assertEquals(0, wave1.getWeaponPassive().compareTo(BigDecimal.ZERO));
	}

	@Test
	public void unknownExtensionIsNotTreatedAsZero()
	{
		SetBonus unknown = new SetBonus()
		{
			@Override
			public BigDecimal gearScore(List<WornItem> equipment, ItemStatSource stats)
			{
				return null;
			}
		};
		GearScoreCalculator calculator = new GearScoreCalculator(
			GearScoreCoefficients.wave1(),
			Collections.singletonList(unknown),
			Collections.<WeaponPassive>emptyList());

		GearScoreBreakdown score = calculator.score(worn(item(3, 1)), source(1, melee(4)));
		assertFalse(score.isComplete());
		assertFalse(score.isExtensionDataComplete());
		assertEquals(0, score.getSetBonus().compareTo(BigDecimal.ZERO));
		assertDisplayed("4.0", score);

		GearScoreComparison comparison = calculator.compare(1, worn(), source(1, melee(4)));
		assertNull(comparison.getDelta());
		assertEquals("4.0", comparison.getItemScore().toPlainString());
	}

	@Test
	public void setBonusChangesTheSwapDelta()
	{
		SetBonus weaponBonus = new SetBonus()
		{
			@Override
			public BigDecimal gearScore(List<WornItem> equipment, ItemStatSource stats)
			{
				for (int i = 0; i < equipment.size(); i++)
				{
					if (equipment.get(i).getItemId() == 99)
					{
						return new BigDecimal("10.0");
					}
				}
				return BigDecimal.ZERO;
			}
		};
		GearScoreCalculator calculator = new GearScoreCalculator(
			GearScoreCoefficients.wave1(),
			Collections.singletonList(weaponBonus),
			Collections.<WeaponPassive>emptyList());
		MapSource source = source(1, melee(1), 99, melee(1));

		GearScoreComparison comparison = calculator.compare(99, worn(item(EquipmentSlot.WEAPON, 1)), source);
		assertEquals("10.0", comparison.getDelta().toPlainString());
		assertEquals("1.0", comparison.getItemScore().toPlainString());
	}

	private static void assertDisplayed(String expected, GearScoreBreakdown score)
	{
		assertEquals(expected, score.displayedTotal().toPlainString());
	}

	private static WornItem item(int slot, int itemId)
	{
		return new WornItem(slot, itemId);
	}

	private static List<WornItem> worn(WornItem... items)
	{
		return Arrays.asList(items);
	}

	private static MapSource source(Object... pairs)
	{
		Map<Integer, EquipmentBonus> bonuses = new HashMap<Integer, EquipmentBonus>();
		for (int i = 0; i < pairs.length; i += 2)
		{
			bonuses.put((Integer) pairs[i], (EquipmentBonus) pairs[i + 1]);
		}
		return new MapSource(bonuses);
	}

	private static EquipmentBonus attack(int stab, int slash, int crush, int magic, int ranged)
	{
		return bonus(EquipmentSlot.WEAPON, false, stab, slash, crush, magic, ranged, 0, 0, 0, 0, 0, 0, 0, "0", 0);
	}

	private static EquipmentBonus defence(int stab, int slash, int crush, int magic, int ranged)
	{
		return bonus(EquipmentSlot.SHIELD, false, 0, 0, 0, 0, 0, stab, slash, crush, magic, ranged, 0, 0, "0", 0);
	}

	private static EquipmentBonus melee(int strength)
	{
		return bonus(EquipmentSlot.WEAPON, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, strength, 0, "0", 0);
	}

	private static EquipmentBonus meleeSlash(int strength, int slash)
	{
		return bonus(EquipmentSlot.WEAPON, false, 0, slash, 0, 0, 0, 0, 0, 0, 0, 0, strength, 0, "0", 0);
	}

	private static EquipmentBonus twoHandMelee(int strength)
	{
		return bonus(EquipmentSlot.WEAPON, true, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, strength, 0, "0", 0);
	}

	private static EquipmentBonus rangedStrength(int strength)
	{
		return bonus(EquipmentSlot.AMMO, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, strength, "0", 0);
	}

	private static EquipmentBonus prayer(int prayer)
	{
		return bonus(EquipmentSlot.RING, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "0", prayer);
	}

	private static EquipmentBonus magic(String percent)
	{
		return bonus(EquipmentSlot.WEAPON, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, percent, 0);
	}

	private static EquipmentBonus bonus(
		int slot,
		boolean twoHanded,
		int attackStab,
		int attackSlash,
		int attackCrush,
		int attackMagic,
		int attackRanged,
		int defenceStab,
		int defenceSlash,
		int defenceCrush,
		int defenceMagic,
		int defenceRanged,
		int meleeStrength,
		int rangedStrength,
		String magicDamage,
		int prayer)
	{
		return new EquipmentBonus(
			slot,
			twoHanded,
			attackStab,
			attackSlash,
			attackCrush,
			attackMagic,
			attackRanged,
			defenceStab,
			defenceSlash,
			defenceCrush,
			defenceMagic,
			defenceRanged,
			meleeStrength,
			rangedStrength,
			new BigDecimal(magicDamage),
			prayer);
	}

	private static final class MapSource implements ItemStatSource
	{
		private final Map<Integer, EquipmentBonus> bonuses;

		private MapSource(Map<Integer, EquipmentBonus> bonuses)
		{
			this.bonuses = bonuses;
		}

		@Override
		public EquipmentBonus getVerifiedBonus(int itemId)
		{
			return bonuses.get(Integer.valueOf(itemId));
		}
	}
}
