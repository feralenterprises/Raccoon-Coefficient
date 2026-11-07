package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Expected totals come from tools/independent_scores.py, a separate Python
 * implementation of the documented v1 + E1 rules fed with the Wiki DPS
 * calculator dataset (exact fractions, then HALF_UP to one decimal). They are
 * NOT copied from this plugin's output.
 */
public class ExperimentalE1Test
{
	// Item ids (Wiki DPS calculator dataset ids; see RealItems).
	static final int NEITIZNOT = 10828, FIRE_CAPE = 6570, FURY = 6585, BANDOS_CHEST = 11832, TASSETS = 11834,
		BARROWS_GLOVES = 7462, D_BOOTS = 11840, B_RING_I = 11773, WHIP = 4151, D_DEFENDER = 12954, AGS = 11802,
		FANG = 26219, RAPIER = 22324, DRAGON_ARROW = 11212, VOID_MELEE_HELM = 11665, VOID_TOP = 8839,
		VOID_ROBE = 8840, VOID_GLOVES = 8842, ROBIN = 2581, DHIDE_BODY = 2503, DHIDE_CHAPS = 2497,
		DHIDE_VAMBS = 2491, MSB = 861, RUNE_ARROW = 892, CRYSTAL_HELM = 23971, CRYSTAL_BODY = 23975,
		CRYSTAL_LEGS = 23979, BOFA = 25865, AHRIM_HOOD = 4708, AHRIM_TOP = 4712, AHRIM_SKIRT = 4714,
		OCCULT = 12002, TRIDENT = 11905, MAGES_BOOK = 6889, KODAI = 21006, SHADOW = 27275, TWINFLAME = 30634,
		VERAC_HELM = 4753, VERAC_BRASSARD = 4757, VERAC_SKIRT = 4759, VERAC_FLAIL = 4755,
		BLOWPIPE = 12926, VENATOR = 27610, TWISTED_BOW = 20997, RUNE_CROSSBOW = 9185, RUNITE_BOLTS = 9144,
		KARIL_COIF = 4732, KARIL_TOP = 4736, KARIL_SKIRT = 4738, KARIL_XBOW = 4734, BOLT_RACK = 4740,
		DHAROK_HELM = 4716, DHAROK_BODY = 4720, DHAROK_LEGS = 4722, DHAROK_AXE = 4718, AHRIM_STAFF = 4710,
		DAMNED = 12853, GLORY = 1712, VOID_MAGE_HELM = 11663, VOID_RANGER_HELM = 11664, ELITE_TOP = 13072,
		ELITE_ROBE = 13073, COMP_OGRE_BOW = 4827, OGRE_ARROW = 2866, CRYSTAL_BODY_AMLODD = 27769,
		VERAC_HELM_BROKEN = 4980, RUNE_2H = 1319;

	static final int[] MELEE_ARMOUR = {NEITIZNOT, FIRE_CAPE, FURY, BANDOS_CHEST, TASSETS, BARROWS_GLOVES, D_BOOTS, B_RING_I};
	static final int[] RANGED_ARMOUR = {ROBIN, DHIDE_BODY, DHIDE_CHAPS, DHIDE_VAMBS, FIRE_CAPE, FURY, D_BOOTS, B_RING_I};
	static final int[] MAGIC_ARMOUR = {AHRIM_HOOD, AHRIM_TOP, AHRIM_SKIRT, BARROWS_GLOVES, FIRE_CAPE, OCCULT, D_BOOTS, B_RING_I};

	private final RealItems items = new RealItems();
	private final GearScoreCalculator locked = GearScoreCalculator.wave3(items);
	private final GearScoreCalculator e1 = GearScoreCalculator.experimentalE1(items);

	private static int[] with(int[] base, int... extra)
	{
		int[] all = new int[base.length + extra.length];
		System.arraycopy(base, 0, all, 0, base.length);
		System.arraycopy(extra, 0, all, base.length, extra.length);
		return all;
	}

	private void expect(String name, int[] kit, String v1, String experimental)
	{
		List<WornItem> worn = items.kit(kit);
		GearScoreBreakdown base = locked.score(worn, items);
		assertEquals(name + " v1", new BigDecimal(v1), base.displayedTotal());
		GearScoreBreakdown proposed = e1.score(worn, items);
		assertTrue(name + " E1 must be complete", proposed.isComplete());
		assertEquals(name + " E1", new BigDecimal(experimental), proposed.displayedTotal());
		// E1 never changes the locked base subtotal.
		assertEquals(name + " base unchanged", base.getBaseEquipment(), proposed.getBaseEquipment());
	}

	@Test
	public void meleeLoadoutsMatchIndependentReference()
	{
		expect("whip+defender", with(MELEE_ARMOUR, WHIP, D_DEFENDER), "274.0", "274.0");
		expect("AGS 2h", with(MELEE_ARMOUR, AGS), "321.6", "241.3");
		expect("fang+defender", with(MELEE_ARMOUR, FANG, D_DEFENDER), "304.8", "272.1");
		expect("rapier+defender", with(MELEE_ARMOUR, RAPIER, D_DEFENDER), "292.3", "292.3");
		expect("whip+defender+dragon arrows", with(MELEE_ARMOUR, WHIP, D_DEFENDER, DRAGON_ARROW), "334.0", "274.0");
		expect("verac full set", with(new int[] {VERAC_HELM, VERAC_BRASSARD, VERAC_SKIRT, BARROWS_GLOVES,
			FIRE_CAPE, FURY, D_BOOTS, B_RING_I}, VERAC_FLAIL), "255.2", "226.4");
		expect("whip only", new int[] {WHIP}, "90.2", "90.2");
		expect("AGS only", new int[] {AGS}, "157.2", "91.9");
	}

	@Test
	public void voidSetCompleteVersusIncomplete()
	{
		int[] common = {VOID_MELEE_HELM, VOID_TOP, VOID_ROBE, FIRE_CAPE, FURY, D_BOOTS, B_RING_I, WHIP, D_DEFENDER};
		expect("void complete", with(common, VOID_GLOVES), "204.4", "223.8");
		expect("void without gloves", with(common, BARROWS_GLOVES), "224.4", "224.4");
	}

	@Test
	public void rangedLoadoutsMatchIndependentReference()
	{
		expect("msb rune arrows", with(RANGED_ARMOUR, MSB, RUNE_ARROW), "146.4", "184.1");
		expect("msb dragon arrows", with(RANGED_ARMOUR, MSB, DRAGON_ARROW), "157.4", "118.7");
		expect("bofa full crystal", new int[] {CRYSTAL_HELM, CRYSTAL_BODY, CRYSTAL_LEGS, DHIDE_VAMBS, FIRE_CAPE,
			FURY, D_BOOTS, B_RING_I, BOFA}, "223.4", "257.0");
		expect("bofa crystal without helm", new int[] {ROBIN, CRYSTAL_BODY, CRYSTAL_LEGS, DHIDE_VAMBS, FIRE_CAPE,
			FURY, D_BOOTS, B_RING_I, BOFA}, "218.7", "246.7");
	}

	@Test
	public void magicLoadoutsMatchIndependentReference()
	{
		expect("trident", with(MAGIC_ARMOUR, TRIDENT, MAGES_BOOK), "109.3", "208.2");
		expect("kodai", with(MAGIC_ARMOUR, KODAI, MAGES_BOOK), "126.1", "126.1");
		expect("shadow", with(MAGIC_ARMOUR, SHADOW), "106.7", "238.8");
		expect("twinflame", with(MAGIC_ARMOUR, TWINFLAME, MAGES_BOOK), "134.1", "146.8");
		expect("trident only", new int[] {TRIDENT}, "3.6", "102.5");
	}

	@Test
	public void lockedV1MarksUnratedEffectsUnknownButE1RatesThem()
	{
		List<WornItem> fang = items.kit(with(MELEE_ARMOUR, FANG, D_DEFENDER));
		assertFalse("v1 keeps the approved 'unrated passive = ?' contract", locked.score(fang, items).isComplete());
		assertTrue(e1.score(fang, items).isComplete());
		assertEquals("Gear Score: 304.8 ?", GearScoreText.equipmentLine(locked.score(fang, items)));
		assertEquals("Gear Score E1: 272.1", GearScoreText.equipmentLine(e1.score(fang, items)));
	}

	private ExperimentalE1.Line line(int[] kit, String key)
	{
		ExperimentalE1.Result r = new ExperimentalE1(GearScoreCoefficients.wave1()).evaluate(items.kit(kit), items, items);
		for (ExperimentalE1.Line line : r.getLines())
		{
			if (line.getKey().equals(key))
			{
				return line;
			}
		}
		return null;
	}

	@Test
	public void ammoOnlyCountsWhenTheWeaponFiresIt()
	{
		// Melee weapon: arrows do nothing.
		ExperimentalE1.Line melee = line(with(MELEE_ARMOUR, WHIP, D_DEFENDER, DRAGON_ARROW), "weapon.ammo.not_fired");
		assertEquals(new BigDecimal("-60.0"), melee.getPoints().setScale(1));
		// Magic shortbow cannot fire dragon arrows (Arrows page: dark/twisted/3rd age/venator/scorching only).
		assertNotNull(line(with(RANGED_ARMOUR, MSB, DRAGON_ARROW), "weapon.ammo.not_fired"));
		assertNull(line(with(RANGED_ARMOUR, MSB, RUNE_ARROW), "weapon.ammo.not_fired"));
		// Twisted bow fires dragon arrows.
		assertNull(line(new int[] {TWISTED_BOW, DRAGON_ARROW}, "weapon.ammo.not_fired"));
		// Crossbow fires bolts, not arrows; BoFa never fires the ammo slot.
		assertNull(line(new int[] {RUNE_CROSSBOW, RUNITE_BOLTS}, "weapon.ammo.not_fired"));
		assertNotNull(line(new int[] {RUNE_CROSSBOW, RUNE_ARROW}, "weapon.ammo.not_fired"));
		assertNotNull(line(new int[] {BOFA, DRAGON_ARROW}, "weapon.ammo.not_fired"));
	}

	@Test
	public void excludedAndUnknownMechanicsAreExplicit()
	{
		assertEquals(ExperimentalE1.Status.EXCLUDED,
			line(with(MAGIC_ARMOUR, TWINFLAME, MAGES_BOOK), "passive.twinflame.second_hit").getStatus());
		assertEquals(ExperimentalE1.Status.EXCLUDED,
			line(with(MELEE_ARMOUR, FANG, D_DEFENDER), "passive.fang.damage_band").getStatus());
		assertEquals(ExperimentalE1.Status.EXCLUDED, line(new int[] {VENATOR, RUNE_ARROW}, "passive.venator.bounce").getStatus());
		assertEquals(ExperimentalE1.Status.EXCLUDED, line(new int[] {TWISTED_BOW, DRAGON_ARROW}, "passive.twisted_bow.scaling").getStatus());
		assertEquals(ExperimentalE1.Status.EXCLUDED, line(new int[] {KODAI}, "weapon.speed").getStatus());
		// Blowpipe darts are stored inside the weapon and not observable: unknown, never zero.
		GearScoreBreakdown blowpipe = e1.score(items.kit(BLOWPIPE), items);
		assertFalse(blowpipe.isComplete());
		// Base 0.1 x 30 ranged + 20 ranged strength = 23.0, shown with "?".
		assertEquals("Gear Score E1: 23.0 ?", GearScoreText.equipmentLine(blowpipe));
		// No E1 line exists for any special attack or monster-specific bonus.
		for (int[] kit : new int[][] {with(MELEE_ARMOUR, AGS), with(MELEE_ARMOUR, FANG, D_DEFENDER), {TWISTED_BOW, DRAGON_ARROW}})
		{
			for (ExperimentalE1.Line l : new ExperimentalE1(GearScoreCoefficients.wave1()).evaluate(items.kit(kit), items, items).getLines())
			{
				assertFalse(l.getKey(), l.getKey().contains("special"));
				assertFalse(l.getKey(), l.getStatus() == ExperimentalE1.Status.SCORED && l.getKey().contains("twisted"));
			}
		}
	}

	@Test
	public void missingItemDataStaysUnknown()
	{
		items.forgetStats(D_DEFENDER);
		List<WornItem> worn = new ArrayList<WornItem>(items.kit(with(MELEE_ARMOUR, WHIP)));
		worn.add(new WornItem(EquipmentSlot.SHIELD, D_DEFENDER));
		assertFalse(locked.score(worn, items).isComplete());
		GearScoreBreakdown proposed = e1.score(worn, items);
		assertFalse(proposed.isComplete());
		assertTrue(GearScoreText.equipmentLine(proposed).endsWith(" ?"));
	}

	@Test
	public void itemVariantsKeepSetsAndPassives()
	{
		items.alias(900001, VOID_TOP, "Void knight top (or)");
		items.alias(900002, FANG, "Osmumten's fang (or)");
		items.alias(900003, VERAC_HELM, "Verac's helm 50");
		assertNotNull(line(new int[] {VOID_MELEE_HELM, 900001, VOID_ROBE, VOID_GLOVES, WHIP}, "set.void.melee"));
		assertNotNull(line(new int[] {900002}, "passive.fang.accuracy"));
		assertNotNull(line(new int[] {900003, VERAC_BRASSARD, VERAC_SKIRT, VERAC_FLAIL}, "set.barrows.verac"));
	}

	// ---- Hover projection = actual equipped change -----------------------

	/** Independent swap model: replace the slot; 2h clears shield; shield clears a 2h weapon. */
	private List<WornItem> equip(List<WornItem> worn, int candidate)
	{
		EquipmentBonus c = items.getVerifiedBonus(candidate);
		List<WornItem> after = new ArrayList<WornItem>();
		for (WornItem w : worn)
		{
			EquipmentBonus b = items.getVerifiedBonus(w.getItemId());
			boolean removed = w.getSlotIndex() == c.getSlot()
				|| (c.isTwoHanded() && w.getSlotIndex() == EquipmentSlot.SHIELD)
				|| (c.getSlot() == EquipmentSlot.SHIELD && w.getSlotIndex() == EquipmentSlot.WEAPON && b.isTwoHanded());
			if (!removed)
			{
				after.add(w);
			}
		}
		after.add(new WornItem(c.getSlot(), candidate));
		return after;
	}

	private void assertHoverMatchesEquip(GearScorer scorer, int[] kit, int candidate)
	{
		List<WornItem> before = items.kit(kit);
		List<WornItem> after = equip(before, candidate);
		GearScoreComparison hover = scorer.compare(candidate, before, items);
		GearScoreBreakdown b = scorer.score(before, items);
		GearScoreBreakdown a = scorer.score(after, items);
		if (b.isComplete() && a.isComplete())
		{
			assertEquals("hover vs equip " + candidate, a.displayedTotal().subtract(b.displayedTotal()), hover.getDelta());
		}
		else
		{
			assertNull("unsafe comparison must not claim a total delta", hover.getDelta());
			assertEquals(GearScoreComparison.Verdict.UNKNOWN, hover.getVerdict());
		}
	}

	@Test
	public void hoverDeltaEqualsActualEquippedChangeAcrossSwaps()
	{
		int[][] kits = {
			with(MELEE_ARMOUR, WHIP, D_DEFENDER), with(MELEE_ARMOUR, AGS), with(RANGED_ARMOUR, MSB, RUNE_ARROW),
			{VOID_MELEE_HELM, VOID_TOP, VOID_ROBE, BARROWS_GLOVES, FIRE_CAPE, FURY, D_BOOTS, B_RING_I, WHIP, D_DEFENDER},
			{ROBIN, CRYSTAL_BODY, CRYSTAL_LEGS, DHIDE_VAMBS, FIRE_CAPE, FURY, D_BOOTS, B_RING_I, BOFA},
			with(MAGIC_ARMOUR, TRIDENT, MAGES_BOOK)};
		int[] candidates = {AGS, WHIP, D_DEFENDER, FANG, DRAGON_ARROW, RUNE_ARROW, MSB, BOFA, CRYSTAL_HELM,
			VOID_GLOVES, BARROWS_GLOVES, SHADOW, TRIDENT, KODAI, MAGES_BOOK, BLOWPIPE};
		for (GearScorer scorer : new GearScorer[] {locked, e1})
		{
			for (int[] kit : kits)
			{
				for (int candidate : candidates)
				{
					assertHoverMatchesEquip(scorer, kit, candidate);
				}
			}
		}
	}

	@Test
	public void namedSwapsShowExpectedDirections()
	{
		// 2h over 1h+shield: E1 total 274.0 -> 241.3 (AGS 6-tick); v1 274.0 -> 321.6.
		List<WornItem> whip = items.kit(with(MELEE_ARMOUR, WHIP, D_DEFENDER));
		assertEquals(new BigDecimal("47.6"), locked.compare(AGS, whip, items).getDelta());
		assertEquals(new BigDecimal("-32.7"), e1.compare(AGS, whip, items).getDelta());
		// Shield over a 2h removes the 2h weapon: defender 19.4 - AGS 157.2.
		List<WornItem> ags = items.kit(with(MELEE_ARMOUR, AGS));
		assertEquals(new BigDecimal("-137.8"), locked.compare(D_DEFENDER, ags, items).getDelta());
		// Dragon arrows on a magic shortbow: v1 +11.0, E1 118.7 - 184.1 = -65.4.
		List<WornItem> msb = items.kit(with(RANGED_ARMOUR, MSB, RUNE_ARROW));
		assertEquals(new BigDecimal("11.0"), locked.compare(DRAGON_ARROW, msb, items).getDelta());
		assertEquals(new BigDecimal("-65.4"), e1.compare(DRAGON_ARROW, msb, items).getDelta());
		// Completing Void: v1 cannot rate the set -> base-only delta; E1 223.8 - 224.4 = -0.6.
		List<WornItem> voidNoGloves = items.kit(VOID_MELEE_HELM, VOID_TOP, VOID_ROBE, BARROWS_GLOVES, FIRE_CAPE,
			FURY, D_BOOTS, B_RING_I, WHIP, D_DEFENDER);
		GearScoreComparison v1Void = locked.compare(VOID_GLOVES, voidNoGloves, items);
		assertNull(v1Void.getDelta());
		assertEquals(new BigDecimal("-20.0"), v1Void.getBaseDelta());
		assertEquals(new BigDecimal("-0.6"), e1.compare(VOID_GLOVES, voidNoGloves, items).getDelta());
		// Removing a Crystal piece loses that piece's bonus (adding the helm back: 246.7 -> 257.0).
		List<WornItem> noHelm = items.kit(ROBIN, CRYSTAL_BODY, CRYSTAL_LEGS, DHIDE_VAMBS, FIRE_CAPE, FURY, D_BOOTS, B_RING_I, BOFA);
		assertEquals(new BigDecimal("10.3"), e1.compare(CRYSTAL_HELM, noHelm, items).getDelta());
	}

	@Test
	public void selectableScorerSwitchesModelAndLabels()
	{
		final ScoringModel[] model = {ScoringModel.LOCKED_V1};
		SelectableGearScorer scorer = new SelectableGearScorer(new SelectableGearScorer.ModelSource()
		{
			@Override
			public ScoringModel current()
			{
				return model[0];
			}
		}, items);
		List<WornItem> whip = items.kit(with(MELEE_ARMOUR, WHIP, D_DEFENDER));
		assertEquals("Gear Score: 274.0", GearScoreText.equipmentLine(scorer.score(whip, items)));
		assertNull(scorer.compare(AGS, whip, items).getModelLabel());
		model[0] = ScoringModel.EXPERIMENTAL_E1;
		assertEquals("Gear Score E1: 274.0", GearScoreText.equipmentLine(scorer.score(whip, items)));
		GearScoreComparison hover = scorer.compare(AGS, whip, items);
		assertEquals("E1", hover.getModelLabel());
		assertTrue(GearScoreText.tooltip(hover, false).contains("E1 "));
		assertEquals("GS E1: 274.0", GearScoreText.compactEquipmentLine("Gear Score E1: 274.0"));
	}

	// ---- Brought forward from RACCOON-CRYSTAL-WAVE (now E1-only) --------

	@Test
	public void barrowsDamnedAndDharokMatchIndependentReference()
	{
		int[] rest = {BARROWS_GLOVES, FIRE_CAPE, D_BOOTS, B_RING_I};
		expect("karil + damned", with(rest, KARIL_COIF, KARIL_TOP, KARIL_SKIRT, KARIL_XBOW, BOLT_RACK, DAMNED), "176.1", "230.6");
		expect("karil + glory", with(rest, KARIL_COIF, KARIL_TOP, KARIL_SKIRT, KARIL_XBOW, BOLT_RACK, GLORY), "176.1", "215.8");
		expect("dharok 50/99 reference", with(rest, DHAROK_HELM, DHAROK_BODY, DHAROK_LEGS, DHAROK_AXE, FURY), "290.6", "302.2");
		expect("verac + damned", with(rest, VERAC_HELM, VERAC_BRASSARD, VERAC_SKIRT, VERAC_FLAIL, DAMNED), "246.2", "221.3");
		expect("ahrim + damned", with(rest, AHRIM_HOOD, AHRIM_TOP, AHRIM_SKIRT, AHRIM_STAFF, DAMNED), "192.1", "200.2");
	}

	@Test
	public void voidVariantsMatchIndependentReference()
	{
		int[] rest = {VOID_GLOVES, FIRE_CAPE, D_BOOTS, B_RING_I};
		expect("elite void mage + trident", with(rest, VOID_MAGE_HELM, ELITE_TOP, ELITE_ROBE, OCCULT, TRIDENT, MAGES_BOOK), "90.6", "199.2");
		expect("void ranger + comp ogre bow", with(rest, VOID_RANGER_HELM, VOID_TOP, VOID_ROBE, FURY, COMP_OGRE_BOW, OGRE_ARROW), "120.6", "121.7");
		expect("elite void ranger + msb", with(rest, VOID_RANGER_HELM, ELITE_TOP, ELITE_ROBE, FURY, MSB, RUNE_ARROW), "153.7", "206.9");
		// Ogre bows: Void accuracy only (their max hit ignores other gear).
		ExperimentalE1.Line ogre = line(with(rest, VOID_RANGER_HELM, VOID_TOP, VOID_ROBE, FURY, COMP_OGRE_BOW, OGRE_ARROW), "set.void.ranged");
		assertEquals(new BigDecimal("1.12"), ogre.getPoints().setScale(2, java.math.RoundingMode.HALF_UP));
	}

	@Test
	public void crystalWithUnfiredArrowsIsNotDoubleCounted()
	{
		int[] crystal = {CRYSTAL_HELM, CRYSTAL_BODY, CRYSTAL_LEGS, DHIDE_VAMBS, FIRE_CAPE, FURY, D_BOOTS, B_RING_I, BOFA};
		// v1 counts +60 for arrows BoFa never fires; E1 removes them from the base AND from the crystal multiplier.
		expect("bofa crystal + dragon arrows", with(crystal, DRAGON_ARROW), "283.4", "257.0");
		assertEquals(e1.score(items.kit(crystal), items).displayedTotal(),
			e1.score(items.kit(with(crystal, DRAGON_ARROW)), items).displayedTotal());
	}

	@Test
	public void crystalVariantsBowsAndSwaps()
	{
		// Clan recolour counts; explicit inactive / unknown variants do not.
		assertNotNull(line(new int[] {CRYSTAL_BODY_AMLODD, BOFA}, "set.crystal.body"));
		items.alias(900010, CRYSTAL_BODY, "Crystal body (inactive)");
		assertNull(line(new int[] {900010, BOFA}, "set.crystal.body"));
		items.alias(900011, BOFA, "Bow of faerdhinen (c) (Cadarn)");
		assertNotNull(line(new int[] {CRYSTAL_BODY, 900011}, "set.crystal.body"));
		// Wrong bow: no crystal effect at all.
		assertNull(line(new int[] {CRYSTAL_HELM, CRYSTAL_BODY, CRYSTAL_LEGS, MSB, RUNE_ARROW}, "set.crystal.body"));
		// Hovering a whip over the bow removes every crystal piece bonus; hover equals equipped change.
		int[] kit = {CRYSTAL_HELM, CRYSTAL_BODY, CRYSTAL_LEGS, DHIDE_VAMBS, FIRE_CAPE, FURY, D_BOOTS, B_RING_I, BOFA};
		assertHoverMatchesEquip(e1, kit, WHIP);
		assertNull(line(new int[] {CRYSTAL_HELM, CRYSTAL_BODY, CRYSTAL_LEGS, DHIDE_VAMBS, FIRE_CAPE, FURY, D_BOOTS, B_RING_I, WHIP},
			"set.crystal.body"));
	}

	@Test
	public void damnedEffectsNeedTheAmuletAndEveryPiece()
	{
		int[] karil = {KARIL_COIF, KARIL_TOP, KARIL_SKIRT, KARIL_XBOW, BOLT_RACK};
		assertNull(line(with(karil, GLORY), "set.barrows.karil.damned"));
		assertEquals(ExperimentalE1.Status.EXCLUDED, line(with(karil, GLORY), "set.barrows.karil").getStatus());
		assertNotNull(line(with(karil, DAMNED), "set.barrows.karil.damned"));
		assertNull(line(new int[] {KARIL_COIF, KARIL_TOP, DHIDE_CHAPS, KARIL_XBOW, BOLT_RACK, DAMNED}, "set.barrows.karil.damned"));
		// Verac's damned prayer counts once (+7 x 0.5), on top of the amulet's own +3 already in base stats.
		assertEquals(new BigDecimal("3.5"), line(new int[] {VERAC_HELM, VERAC_BRASSARD, VERAC_SKIRT, VERAC_FLAIL, DAMNED},
			"set.barrows.verac.damned").getPoints().setScale(1));
		// Dharok's damned recoil is defensive: shown, not scored.
		assertEquals(ExperimentalE1.Status.EXCLUDED, line(new int[] {DHAROK_HELM, DHAROK_BODY, DHAROK_LEGS, DHAROK_AXE, DAMNED},
			"set.barrows.dharok.damned").getStatus());
	}

	@Test
	public void fullyDegradedBarrowsPieceBreaksTheSet()
	{
		assertNotNull(line(new int[] {VERAC_HELM, VERAC_BRASSARD, VERAC_SKIRT, VERAC_FLAIL}, "set.barrows.verac"));
		assertNull(line(new int[] {VERAC_HELM_BROKEN, VERAC_BRASSARD, VERAC_SKIRT, VERAC_FLAIL}, "set.barrows.verac"));
	}

	@Test
	public void lockedDefaultNeverIncludesBroughtForwardPoints()
	{
		int[][] kits = {
			{CRYSTAL_HELM, CRYSTAL_BODY, CRYSTAL_LEGS, BOFA},
			{KARIL_COIF, KARIL_TOP, KARIL_SKIRT, KARIL_XBOW, BOLT_RACK, DAMNED},
			{DHAROK_HELM, DHAROK_BODY, DHAROK_LEGS, DHAROK_AXE},
			{VOID_MAGE_HELM, ELITE_TOP, ELITE_ROBE, VOID_GLOVES, TRIDENT}};
		for (int[] kit : kits)
		{
			GearScoreBreakdown v1 = locked.score(items.kit(kit), items);
			assertEquals(0, v1.getSetBonus().signum());
			assertEquals(0, v1.getWeaponPassive().signum());
			assertFalse("detected effects stay unrated (?) in locked v1", v1.isComplete());
		}
	}

	/** The exact numbers given to the owner for the in-game E1 check (HANDOFF.md). */
	@Test
	public void inGameCheckValues()
	{
		expect("msb alone", new int[] {MSB}, "6.9", "28.2");
		expect("msb + rune arrows", new int[] {MSB, RUNE_ARROW}, "55.9", "93.6");
		expect("msb + dragon arrows", new int[] {MSB, DRAGON_ARROW}, "66.9", "28.2");
		expect("whip + rune arrows", new int[] {WHIP, RUNE_ARROW}, "139.2", "90.2");
		expect("rune 2h alone", new int[] {RUNE_2H}, "81.0", "23.6");
		List<WornItem> whip = items.kit(WHIP);
		assertEquals("Gear Score: 49.0</br><col=00c846>↑ +49.0</col>",
			GearScoreText.tooltip(locked.compare(RUNE_ARROW, whip, items), false));
		assertEquals("Gear Score: 49.0</br><col=b4b4b4>E1 </col><col=b4b4b4>→ 0.0</col>",
			GearScoreText.tooltip(e1.compare(RUNE_ARROW, whip, items), false));
	}
}
