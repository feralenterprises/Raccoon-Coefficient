package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Wave 2 set-combination tests. Charge/durability is intentionally out of
 * scope. These are deterministic source tests, NOT native RuneLite evidence.
 */
public class SetEffectDetectorTest
{
	private static final int HEAD = 0, BODY = 4, LEGS = 7, GLOVES = 9, WEAPON = 3;
	private final SetEffectDetector detector = new SetEffectDetector();

	private static class Gear implements SetEffectDetector.ItemNameSource
	{
		final List<WornItem> worn = new ArrayList<WornItem>();
		final Map<Integer, String> names = new HashMap<Integer, String>();
		int next = 100;

		Gear wear(int slot, String name)
		{
			int id = next++;
			worn.add(new WornItem(slot, id));
			names.put(id, name);
			return this;
		}

		@Override
		public String nameOf(int id) { return names.get(id); }
	}

	private SetEffectDetector.State status(Gear gear, String key)
	{
		for (SetEffectDetector.Effect effect : detector.evaluate(gear.worn, gear))
		{
			if (effect.getKey().equals(key)) { return effect.getState(); }
		}
		throw new AssertionError("Missing catalog rule: " + key);
	}

	@Test
	public void voidHelmAndFourPiecesRequired()
	{
		Gear melee = new Gear().wear(HEAD, "Void melee helm").wear(BODY, "Void knight top")
			.wear(LEGS, "Void knight robe").wear(GLOVES, "Void knight gloves");
		assertEquals(SetEffectDetector.State.ACTIVE, status(melee, "void.melee"));
		assertEquals(SetEffectDetector.State.INACTIVE, status(melee, "void.magic"));

		Gear missingGloves = new Gear().wear(HEAD, "Void melee helm").wear(BODY, "Void knight top")
			.wear(LEGS, "Void knight robe");
		assertEquals(SetEffectDetector.State.INACTIVE, status(missingGloves, "void.melee"));
	}

	@Test
	public void eliteRequiresBothElitePiecesAndCorrectHelm()
	{
		Gear elite = new Gear().wear(HEAD, "Void ranger helm").wear(BODY, "Elite void top")
			.wear(LEGS, "Elite void robe").wear(GLOVES, "Void knight gloves");
		assertEquals(SetEffectDetector.State.ACTIVE, status(elite, "void.ranged"));
		assertEquals(SetEffectDetector.State.ACTIVE, status(elite, "void.elite.ranged"));
		assertEquals(SetEffectDetector.State.INACTIVE, status(elite, "void.elite.magic"));

		Gear mixed = new Gear().wear(HEAD, "Void ranger helm").wear(BODY, "Elite void top")
			.wear(LEGS, "Void knight robe").wear(GLOVES, "Void knight gloves");
		assertEquals(SetEffectDetector.State.ACTIVE, status(mixed, "void.ranged"));
		assertEquals(SetEffectDetector.State.INACTIVE, status(mixed, "void.elite.ranged"));
	}

	@Test
	public void obsidianRequiresAnActualObsidianMeleeWeapon()
	{
		Gear withWeapon = new Gear().wear(HEAD, "Obsidian helmet")
			.wear(BODY, "Obsidian platebody").wear(LEGS, "Obsidian platelegs")
			.wear(WEAPON, "Tzhaar-ket-om");
		assertEquals(SetEffectDetector.State.ACTIVE, status(withWeapon, "obsidian"));
		Gear wrongWeapon = new Gear().wear(HEAD, "Obsidian helmet")
			.wear(BODY, "Obsidian platebody").wear(LEGS, "Obsidian platelegs")
			.wear(WEAPON, "Dragon scimitar");
		assertEquals(SetEffectDetector.State.INACTIVE, status(wrongWeapon, "obsidian"));
	}

	@Test
	public void barrowsRequireMatchingFourPieces()
	{
		Gear full = new Gear().wear(HEAD, "Dharok's helm").wear(BODY, "Dharok's platebody")
			.wear(LEGS, "Dharok's platelegs").wear(WEAPON, "Dharok's greataxe");
		assertEquals(SetEffectDetector.State.ACTIVE, status(full, "barrows.dharok"));
		assertEquals(SetEffectDetector.State.INACTIVE, status(full, "barrows.guthan"));

		Gear mixed = new Gear().wear(HEAD, "Dharok's helm").wear(BODY, "Dharok's platebody")
			.wear(LEGS, "Dharok's platelegs").wear(WEAPON, "Guthan's warspear");
		assertEquals(SetEffectDetector.State.INACTIVE, status(mixed, "barrows.dharok"));
	}

	@Test
	public void crystalPiecesActivateIndependentlyWithMatchingBow()
	{
		Gear bowfa = new Gear().wear(BODY, "Crystal body")
			.wear(WEAPON, "Bow of Faerdhinen (c)");
		assertEquals(SetEffectDetector.State.ACTIVE, status(bowfa, "crystal.body"));
		assertEquals(SetEffectDetector.State.INACTIVE, status(bowfa, "crystal.helm"));
		assertEquals(SetEffectDetector.State.INACTIVE, status(bowfa, "crystal.legs"));

		Gear wrongWeapon = new Gear().wear(BODY, "Crystal body").wear(WEAPON, "Magic shortbow");
		assertEquals(SetEffectDetector.State.INACTIVE, status(wrongWeapon, "crystal.body"));
	}

	@Test
	public void inquisitorIsPiecewiseNotAnOldFullSetBonus()
	{
		Gear helmOnly = new Gear().wear(HEAD, "Inquisitor's great helm");
		assertEquals(SetEffectDetector.State.ACTIVE, status(helmOnly, "inquisitor.helm"));
		assertEquals(SetEffectDetector.State.INACTIVE, status(helmOnly, "inquisitor.body"));
		assertEquals(SetEffectDetector.State.INACTIVE, status(helmOnly, "inquisitor.legs"));
		for (SetEffectDetector.Effect effect : detector.evaluate(helmOnly.worn, helmOnly))
		{
			assertFalse("No obsolete full-set multiplier", effect.getKey().equals("inquisitor.full"));
			assertFalse("No numeric coefficients are approved", effect.hasApprovedScore());
		}
	}

	@Test
	public void moonRequiresWeaponAndAllThreeArmourPieces()
	{
		Gear full = new Gear().wear(HEAD, "Blood Moon helm").wear(BODY, "Blood Moon chestplate")
			.wear(LEGS, "Blood Moon tassets").wear(WEAPON, "Dual macuahuitl");
		assertEquals(SetEffectDetector.State.ACTIVE, status(full, "moons.blood"));
		Gear incomplete = new Gear().wear(HEAD, "Blood Moon helm").wear(BODY, "Blood Moon chestplate")
			.wear(LEGS, "Blood Moon tassets").wear(WEAPON, "Dragon scimitar");
		assertEquals(SetEffectDetector.State.INACTIVE, status(incomplete, "moons.blood"));
	}

	@Test
	public void unknownIdentityNeverCertifiesACompleteEffect()
	{
		Gear unknown = new Gear().wear(HEAD, null).wear(BODY, "Void knight top")
			.wear(LEGS, "Void knight robe").wear(GLOVES, "Void knight gloves");
		assertEquals(SetEffectDetector.State.UNKNOWN, status(unknown, "void.melee"));
		assertEquals(SetEffectDetector.State.UNKNOWN, status(unknown, "void.magic"));
		assertEquals(SetEffectDetector.State.UNKNOWN, status(unknown, "void.ranged"));
	}

	@Test
	public void unscoredSetEffectsDoNotFabricateNumbers()
	{
		Gear active = new Gear().wear(HEAD, "Justiciar faceguard")
			.wear(BODY, "Justiciar chestguard").wear(LEGS, "Justiciar legguards");
		PendingSetEffectBonus bridge = new PendingSetEffectBonus(detector, active);
		assertNull(bridge.gearScore(active.worn, null));
		assertEquals(BigDecimal.ZERO, bridge.gearScore(new ArrayList<WornItem>(), null));
		assertTrue(detector.evaluate(active.worn, active).size() >= 20);
	}

	@Test
	public void unavailableEquipmentReturnsUnknown()
	{
		List<SetEffectDetector.Effect> effects = detector.evaluate(null, new Gear());
		for (SetEffectDetector.Effect effect : effects)
		{
			assertEquals(SetEffectDetector.State.UNKNOWN, effect.getState());
		}
	}
}
