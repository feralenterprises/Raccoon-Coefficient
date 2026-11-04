package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Evidence ledger for Wave 10 Gear Score calibration.
 *
 * The percentages below describe real combat mechanics. They are NOT Gear
 * Score points: converting accuracy, damage, protection, extra hit rolls or
 * proc chance into universal score points requires a user-approved combat
 * model, a target/attack style and often further combat context.
 *
 * This ledger does not change the locked Wave 1 scoring formula, inspect
 * combat events, or keep track of charges/durability.
 */
public final class EffectCalibrationLedger
{
	public enum Context
	{
		MELEE_STYLE,
		RANGED_STYLE,
		MAGIC_STYLE,
		MULTITARGET,
		TARGET_DEFENCE,
		DEFENSIVE_STYLE,
		SPELL_SELECTION,
		PROC_OR_HITPOINTS,
		DAMAGE_DISTRIBUTION
	}

	public enum Evidence
	{
		ABSENT,
		DETECTED_UNRATED,
		UNKNOWN
	}

	public enum Family
	{
		ARMOUR_SET,
		WEAPON_PASSIVE
	}

	/**
	 * A percentage is a *mechanical percentage*, never a scored point.
	 * Null means no universal fixed percentage has been claimed by this
	 * ledger. Follow-up hit percentage is NOT a damage multiplier.
	 */
	public static final class Mechanic
	{
		private final String key;
		private final Context context;
		private final BigDecimal accuracyPercent;
		private final BigDecimal damagePercent;
		private final BigDecimal followupHitPercent;
		private final String note;

		private Mechanic(
			String key,
			Context context,
			BigDecimal accuracyPercent,
			BigDecimal damagePercent,
			BigDecimal followupHitPercent,
			String note)
		{
			this.key = key;
			this.context = context;
			this.accuracyPercent = accuracyPercent;
			this.damagePercent = damagePercent;
			this.followupHitPercent = followupHitPercent;
			this.note = note;
		}

		public String getKey() { return key; }
		public Context getContext() { return context; }
		public BigDecimal getAccuracyPercent() { return accuracyPercent; }
		public BigDecimal getDamagePercent() { return damagePercent; }
		public BigDecimal getFollowupHitPercent() { return followupHitPercent; }
		public String getNote() { return note; }

		/** No conversion of mechanic percentages into Gear Score is approved. */
		public boolean hasApprovedGearScoreWeight() { return false; }
	}

	public static final class Observation
	{
		private final Family family;
		private final Mechanic mechanic;
		private final Evidence evidence;
		private final String sourceUrl;

		private Observation(Family family, Mechanic mechanic, Evidence evidence, String sourceUrl)
		{
			this.family = family;
			this.mechanic = mechanic;
			this.evidence = evidence;
			this.sourceUrl = sourceUrl;
		}

		public Family getFamily() { return family; }
		public Mechanic getMechanic() { return mechanic; }
		public Evidence getEvidence() { return evidence; }
		public String getSourceUrl() { return sourceUrl; }
	}

	private static final Map<String, Mechanic> CATALOG;

	static
	{
		Map<String, Mechanic> catalog = new LinkedHashMap<String, Mechanic>();

		put(catalog, "void.melee", Context.MELEE_STYLE, "10", "10", null,
			"Melee-only Void accuracy and damage; bonus does not apply to unrelated styles.");
		put(catalog, "void.ranged", Context.RANGED_STYLE, "10", "10", null,
			"Ranged-only Void accuracy and damage; special weapon interactions vary.");
		put(catalog, "void.magic", Context.MAGIC_STYLE, "45", null, null,
			"Magic accuracy only; not a 45% magic damage boost.");
		put(catalog, "void.elite.ranged", Context.RANGED_STYLE, null, "2.5", null,
			"Additional ranged damage, additive to the ordinary 10% Void ranged damage.");
		put(catalog, "void.elite.magic", Context.MAGIC_STYLE, null, "5", null,
			"Additional magic damage when the full elite magic configuration is equipped.");
		put(catalog, "obsidian", Context.MELEE_STYLE, "10", "10", null,
			"Only eligible obsidian melee weapons with the full armour set.");
		for (String brother : new String[]{"ahrim", "dharok", "guthan", "karil", "torag", "verac"})
		{
			put(catalog, "barrows." + brother, Context.PROC_OR_HITPOINTS, null, null, null,
				"Brother-specific combat passive; not a universal constant DPS bonus.");
		}
		put(catalog, "justiciar", Context.DEFENSIVE_STYLE, null, null, null,
			"Incoming damage reduction depends on matching defence bonus divided by 3000.");
		put(catalog, "inquisitor.helm", Context.MELEE_STYLE, "0.5", "0.5", null,
			"Crush-style accuracy and damage per helm; unchanged by the former set/mace multipliers.");
		put(catalog, "inquisitor.body", Context.MELEE_STYLE, "1", "1", null,
			"Crush-style accuracy and damage from hauberk; 22 July 2026 rules.");
		put(catalog, "inquisitor.legs", Context.MELEE_STYLE, "1", "1", null,
			"Crush-style accuracy and damage from plateskirt; 22 July 2026 rules.");
		put(catalog, "crystal.helm", Context.RANGED_STYLE, "5", "2.5", null,
			"Crystal bow or Bow of Faerdhinen only.");
		put(catalog, "crystal.body", Context.RANGED_STYLE, "15", "7.5", null,
			"Crystal bow or Bow of Faerdhinen only.");
		put(catalog, "crystal.legs", Context.RANGED_STYLE, "10", "5", null,
			"Crystal bow or Bow of Faerdhinen only.");
		for (String moon : new String[]{"blood", "blue", "eclipse"})
		{
			put(catalog, "moons." + moon, Context.PROC_OR_HITPOINTS, null, null, null,
				"Conditional Moon weapon/armour combat passive.");
		}
		put(catalog, "fang.stab_accuracy", Context.TARGET_DEFENCE, null, null, null,
			"Stab-style dual accuracy roll; effectiveness depends on the target and raid context.");
		put(catalog, "fang.damage_band", Context.DAMAGE_DISTRIBUTION, null, null, null,
			"Ordinary-hit 15%-85% damage band lowers variance, not expected DPS.");
		put(catalog, "venator.ricochet", Context.MULTITARGET, null, null, null,
			"Secondary hits require eligible nearby targets in multicombat.");
		put(catalog, "twinflame.standard_spell_bonus", Context.MAGIC_STYLE, "10", "10", null,
			"Qualifying standard spellbook casts only; not every attack.");
		put(catalog, "twinflame.second_hit", Context.SPELL_SELECTION, null, null, "40",
			"Second elemental spell hit is 40% of first hit, rounded down; Strike/Surge excluded.");

		CATALOG = Collections.unmodifiableMap(catalog);
	}

	private static BigDecimal decimal(String value)
	{
		return value == null ? null : new BigDecimal(value);
	}

	private static void put(
		Map<String, Mechanic> target,
		String key,
		Context context,
		String accuracy,
		String damage,
		String followup,
		String note)
	{
		if (target.put(key, new Mechanic(
			key, context, decimal(accuracy), decimal(damage), decimal(followup), note)) != null)
		{
			throw new IllegalStateException("Duplicate calibration fact: " + key);
		}
	}

	public Map<String, Mechanic> catalog()
	{
		return CATALOG;
	}

	/**
	 * One result for each rule already supported by the two independent
	 * detectors, using their source URLs and observed equipped state.
	 * Fail loudly in engineering if a detector rule has no calibration fact.
	 */
	public List<Observation> observe(
		List<WornItem> worn,
		SetEffectDetector.ItemNameSource identities)
	{
		List<Observation> observations = new ArrayList<Observation>(CATALOG.size());
		for (SetEffectDetector.Effect effect : new SetEffectDetector().evaluate(worn, identities))
		{
			Evidence evidence = effect.getState() == SetEffectDetector.State.ACTIVE
				? Evidence.DETECTED_UNRATED
				: effect.getState() == SetEffectDetector.State.INACTIVE
					? Evidence.ABSENT : Evidence.UNKNOWN;
			observations.add(new Observation(Family.ARMOUR_SET,
				require(effect.getKey()), evidence, effect.getSourceUrl()));
		}
		for (WeaponPassiveDetector.Effect effect : new WeaponPassiveDetector().evaluate(worn, identities))
		{
			Evidence evidence = effect.getState() == WeaponPassiveDetector.State.ABSENT
				? Evidence.ABSENT
				: effect.getState() == WeaponPassiveDetector.State.UNKNOWN
					? Evidence.UNKNOWN : Evidence.DETECTED_UNRATED;
			observations.add(new Observation(Family.WEAPON_PASSIVE,
				require(effect.getKey()), evidence, effect.getSourceUrl()));
		}
		if (observations.size() != CATALOG.size())
		{
			throw new IllegalStateException("Calibration coverage differs from registered detector rules");
		}
		return Collections.unmodifiableList(observations);
	}

	private static Mechanic require(String key)
	{
		Mechanic mechanic = CATALOG.get(key);
		if (mechanic == null)
		{
			throw new IllegalStateException("Unreviewed combat effect: " + key);
		}
		return mechanic;
	}
}
