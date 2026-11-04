package com.raccooncoefficient.score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Wave 3: source-grounded weapon passive identification, NOT a DPS model.
 *
 * Only the occupied weapon slot is examined. Charged state, monster context,
 * combat style, spells, and durability are deliberately not observed. A
 * contextual effect is therefore never advertised as proven active.
 *
 * Every numeric passive weight is pending explicit owner approval.
 */
public final class WeaponPassiveDetector
{
	public enum State
	{
		ABSENT,
		PRESENT_UNRATED,
		PRESENT_CONDITIONAL,
		UNKNOWN
	}

	public static final class Effect
	{
		private final String key;
		private final String description;
		private final String requirement;
		private final String sourceUrl;
		private final State state;

		private Effect(
			String key,
			String description,
			String requirement,
			String sourceUrl,
			State state)
		{
			this.key = key;
			this.description = description;
			this.requirement = requirement;
			this.sourceUrl = sourceUrl;
			this.state = state;
		}

		public String getKey() { return key; }
		public String getDescription() { return description; }
		public String getRequirement() { return requirement; }
		public String getSourceUrl() { return sourceUrl; }
		public State getState() { return state; }

		/** Wave 3 cannot supply invented Gear Score points. */
		public boolean hasApprovedScore() { return false; }
	}

	private static final String FANG_URL =
		"https://oldschool.runescape.wiki/w/Osmumten%27s_fang";
	private static final String VENATOR_URL =
		"https://oldschool.runescape.wiki/w/Venator_bow";
	private static final String TWINFLAME_URL =
		"https://oldschool.runescape.wiki/w/Twinflame_staff";

	private static final Set<String> FANG = names(
		"osmumten's fang", "osmumten's fang (or)");
	private static final Set<String> VENATOR = names(
		"venator bow", "venator bow (uncharged)");
	private static final Set<String> TWINFLAME = names("twinflame staff");

	private static Set<String> names(String... values)
	{
		return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(values)));
	}

	/**
	 * All registered passives are listed, even if absent, so a scorer can
	 * distinguish verified absence from unknown item identity.
	 */
	public List<Effect> evaluate(
		List<WornItem> equipment,
		SetEffectDetector.ItemNameSource identities)
	{
		if (equipment == null || identities == null)
		{
			return all(State.UNKNOWN, State.UNKNOWN, State.UNKNOWN);
		}

		int weaponId = -1;
		for (WornItem worn : equipment)
		{
			if (worn != null && worn.getSlotIndex() == EquipmentSlot.WEAPON
				&& worn.getItemId() >= 0)
			{
				// A malformed duplicate weapon slot is not verified state.
				if (weaponId >= 0)
				{
					return all(State.UNKNOWN, State.UNKNOWN, State.UNKNOWN);
				}
				weaponId = worn.getItemId();
			}
		}
		if (weaponId < 0)
		{
			return all(State.ABSENT, State.ABSENT, State.ABSENT);
		}

		String itemName = identities.nameOf(weaponId);
		if (itemName == null || itemName.trim().isEmpty())
		{
			return all(State.UNKNOWN, State.UNKNOWN, State.UNKNOWN);
		}

		String normalized = itemName.trim().toLowerCase(Locale.ROOT);
		boolean fang = FANG.contains(normalized);
		boolean venator = VENATOR.contains(normalized);
		boolean twinflame = TWINFLAME.contains(normalized);

		List<Effect> effects = new ArrayList<Effect>(5);
		effects.add(new Effect(
			"fang.stab_accuracy",
			"Fang rerolls its attack accuracy when attacking with a stab style",
			"Stab/Lunge/Block style; outside ToA its accuracy-roll rule differs from inside ToA",
			FANG_URL,
			fang ? State.PRESENT_CONDITIONAL : State.ABSENT));
		effects.add(new Effect(
			"fang.damage_band",
			"Fang normal hits use the 15%-85% damage-roll band rather than the usual range",
			"Successful ordinary attack; excludes weapon special attacks",
			FANG_URL,
			fang ? State.PRESENT_UNRATED : State.ABSENT));
		effects.add(new Effect(
			"venator.ricochet",
			"Venator bow can bounce an arrow to up to two further targets",
			"Ancient essence charges and eligible nearby targets in multicombat; no charge monitoring",
			VENATOR_URL,
			venator ? State.PRESENT_CONDITIONAL : State.ABSENT));
		effects.add(new Effect(
			"twinflame.standard_spell_bonus",
			"Twinflame staff improves standard spellbook spell accuracy and damage by 10%",
			"Qualifying standard spellbook cast; not a universal static equipment bonus",
			TWINFLAME_URL,
			twinflame ? State.PRESENT_CONDITIONAL : State.ABSENT));
		effects.add(new Effect(
			"twinflame.second_hit",
			"Twinflame staff adds a second hit worth 40% of the first hit's damage, rounded down",
			"Elemental spells other than Strike or Surge; not every spell or attack",
			TWINFLAME_URL,
			twinflame ? State.PRESENT_CONDITIONAL : State.ABSENT));
		return Collections.unmodifiableList(effects);
	}

	private static List<Effect> all(State fang, State venator, State twinflame)
	{
		List<Effect> effects = new ArrayList<Effect>(5);
		effects.add(new Effect("fang.stab_accuracy",
			"Fang rerolls stab-style accuracy", "Stab attack style", FANG_URL, fang));
		effects.add(new Effect("fang.damage_band",
			"Fang narrows normal-hit damage rolls", "Successful ordinary attack", FANG_URL, fang));
		effects.add(new Effect("venator.ricochet",
			"Venator arrows may bounce", "Multicombat, suitable targets and charges",
			VENATOR_URL, venator));
		effects.add(new Effect("twinflame.standard_spell_bonus",
			"Twinflame standard spellbook bonus", "Qualifying standard spell", TWINFLAME_URL, twinflame));
		effects.add(new Effect("twinflame.second_hit",
			"Twinflame elemental spell extra hit", "Elemental spells excluding Strike/Surge",
			TWINFLAME_URL, twinflame));
		return Collections.unmodifiableList(effects);
	}
}
