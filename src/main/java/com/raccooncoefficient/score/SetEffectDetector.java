package com.raccooncoefficient.score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Pure-Java Wave 2 set-presence detector.
 *
 * Deliberately ignores charge and durability bookkeeping by owner direction.
 * Detecting a named equipment combination is NOT a claim about target-specific
 * damage, a universally active combat modifier or an approved Gear Score value.
 * Unknown item identities are not silently treated as absence.
 */
public final class SetEffectDetector
{
	public enum State { ACTIVE, INACTIVE, UNKNOWN }

	/** Resolves a RuneLite item ID to the item definition's display name. */
	public interface ItemNameSource
	{
		String nameOf(int itemId);
	}

	public static final class Effect
	{
		private final String key;
		private final String sourceUrl;
		private final String mechanics;
		private final State state;

		private Effect(String key, String sourceUrl, String mechanics, State state)
		{
			this.key = key;
			this.sourceUrl = sourceUrl;
			this.mechanics = mechanics;
			this.state = state;
		}

		public String getKey() { return key; }
		public String getSourceUrl() { return sourceUrl; }
		public String getMechanics() { return mechanics; }
		public State getState() { return state; }
		/** Set-effect-to-score conversion is not owner approved. */
		public boolean hasApprovedScore() { return false; }
	}

	private static final class Required
	{
		final int slot;
		final Set<String> permitted;

		Required(int slot, String... permitted)
		{
			this.slot = slot;
			this.permitted = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(permitted)));
		}
	}

	private static final class Rule
	{
		final String id;
		final String url;
		final String mechanics;
		final Required[] required;

		Rule(String id, String url, String mechanics, Required... required)
		{
			this.id = id;
			this.url = url;
			this.mechanics = mechanics;
			this.required = required;
		}
	}

	private static final int HEAD = EquipmentSlot.HEAD;
	private static final int BODY = EquipmentSlot.BODY;
	private static final int LEGS = 7;
	private static final int GLOVES = 9;
	private static final int WEAPON = EquipmentSlot.WEAPON;

	private static Required r(int slot, String... names) { return new Required(slot, names); }
	private static Rule rule(String key, String url, String description, Required... required)
	{
		return new Rule(key, url, description, required);
	}

	private static final String VOID = "https://oldschool.runescape.wiki/w/Void_Knight_equipment";
	private static final String OBSIDIAN = "https://oldschool.runescape.wiki/w/Obsidian_armour";
	private static final String BARROWS = "https://oldschool.runescape.wiki/w/Barrows_armour";
	private static final String JUSTICIAR = "https://oldschool.runescape.wiki/w/Justiciar_armour";
	private static final String INQUISITOR = "https://oldschool.runescape.wiki/w/Inquisitor%27s_armour";
	private static final String CRYSTAL = "https://oldschool.runescape.wiki/w/Crystal_armour";
	private static final String MOONS = "https://oldschool.runescape.wiki/w/Passive_effect";

	private static Rule barrows(String brother, String head, String body, String legs, String weapon)
	{
		return rule("barrows." + brother, BARROWS, "Four-piece Barrows combat passive",
			r(HEAD, head), r(BODY, body), r(LEGS, legs), r(WEAPON, weapon));
	}

	private static Rule moon(String moon, String head, String body, String legs, String weapon)
	{
		return rule("moons." + moon, MOONS, "Four-piece conditional Moon combat passive",
			r(HEAD, head), r(BODY, body), r(LEGS, legs), r(WEAPON, weapon));
	}

	private static final List<Rule> RULES;
	static
	{
		List<Rule> rules = new ArrayList<Rule>();

		Required top = r(BODY, "void knight top", "elite void top");
		Required robe = r(LEGS, "void knight robe", "elite void robe");
		Required gloves = r(GLOVES, "void knight gloves");
		rules.add(rule("void.melee", VOID, "+10% melee damage/accuracy",
			r(HEAD, "void melee helm"), top, robe, gloves));
		rules.add(rule("void.ranged", VOID, "+10% ranged damage/accuracy",
			r(HEAD, "void ranger helm"), top, robe, gloves));
		rules.add(rule("void.magic", VOID, "+45% magic accuracy",
			r(HEAD, "void mage helm"), top, robe, gloves));
		rules.add(rule("void.elite.ranged", VOID, "Additional +2.5% ranged damage",
			r(HEAD, "void ranger helm"), r(BODY, "elite void top"), r(LEGS, "elite void robe"), gloves));
		rules.add(rule("void.elite.magic", VOID, "Additional +5% magic damage",
			r(HEAD, "void mage helm"), r(BODY, "elite void top"), r(LEGS, "elite void robe"), gloves));

		rules.add(rule("obsidian", OBSIDIAN, "+10% melee accuracy/damage with obsidian weapons",
			r(HEAD, "obsidian helmet"), r(BODY, "obsidian platebody"), r(LEGS, "obsidian platelegs"),
			r(WEAPON, "toktz-xil-ak", "toktz-xil-ek", "tzhaar-ket-em", "tzhaar-ket-om")));

		rules.add(barrows("ahrim", "ahrim's hood", "ahrim's robetop", "ahrim's robeskirt", "ahrim's staff"));
		rules.add(barrows("dharok", "dharok's helm", "dharok's platebody", "dharok's platelegs", "dharok's greataxe"));
		rules.add(barrows("guthan", "guthan's helm", "guthan's platebody", "guthan's chainskirt", "guthan's warspear"));
		rules.add(barrows("karil", "karil's coif", "karil's leathertop", "karil's leatherskirt", "karil's crossbow"));
		rules.add(barrows("torag", "torag's helm", "torag's platebody", "torag's platelegs", "torag's hammers"));
		rules.add(barrows("verac", "verac's helm", "verac's brassard", "verac's plateskirt", "verac's flail"));

		rules.add(rule("justiciar", JUSTICIAR, "Incoming damage reduction uses the relevant defence bonus / 3000 outside PvP and typeless damage",
			r(HEAD, "justiciar faceguard"), r(BODY, "justiciar chestguard"), r(LEGS, "justiciar legguards")));

		// 22 July 2026 changed Inquisitor from a full-set effect to piecewise.
		rules.add(rule("inquisitor.helm", INQUISITOR, "+0.5% crush-style accuracy and damage (independent piece effect)",
			r(HEAD, "inquisitor's great helm")));
		rules.add(rule("inquisitor.body", INQUISITOR, "+1% crush-style accuracy and damage (22 July 2026)",
			r(BODY, "inquisitor's hauberk")));
		rules.add(rule("inquisitor.legs", INQUISITOR, "+1% crush-style accuracy and damage (22 July 2026)",
			r(LEGS, "inquisitor's plateskirt")));

		// Crystal pieces contribute separately with an eligible bow.
		Required bow = r(WEAPON, "crystal bow", "bow of faerdhinen", "bow of faerdhinen (c)");
		rules.add(rule("crystal.helm", CRYSTAL, "+5% ranged accuracy, +2.5% damage with crystal bow",
			r(HEAD, "crystal helm"), bow));
		rules.add(rule("crystal.body", CRYSTAL, "+15% ranged accuracy, +7.5% damage with crystal bow",
			r(BODY, "crystal body"), bow));
		rules.add(rule("crystal.legs", CRYSTAL, "+10% ranged accuracy, +5% damage with crystal bow",
			r(LEGS, "crystal legs"), bow));

		rules.add(moon("blood", "blood moon helm", "blood moon chestplate", "blood moon tassets", "dual macuahuitl"));
		rules.add(moon("blue", "blue moon helm", "blue moon chestplate", "blue moon tassets", "blue moon spear"));
		rules.add(moon("eclipse", "eclipse moon helm", "eclipse moon chestplate", "eclipse moon tassets", "eclipse atlatl"));

		RULES = Collections.unmodifiableList(rules);
	}

	/** Returns one result per catalogued effect. */
	public List<Effect> evaluate(List<WornItem> worn, ItemNameSource names)
	{
		if (worn == null || names == null || WornLayout.hasDuplicateOccupiedSlot(worn))
		{
			List<Effect> unknown = new ArrayList<Effect>();
			for (Rule rule : RULES)
			{
				unknown.add(new Effect(rule.id, rule.url, rule.mechanics, State.UNKNOWN));
			}
			return Collections.unmodifiableList(unknown);
		}

		Map<Integer, String> bySlot = new HashMap<Integer, String>();
		for (WornItem item : worn)
		{
			if (item != null && item.getItemId() >= 0)
			{
				String name = names.nameOf(item.getItemId());
				bySlot.put(item.getSlotIndex(), canonical(name));
			}
		}

		List<Effect> result = new ArrayList<Effect>(RULES.size());
		for (Rule rule : RULES)
		{
			boolean absent = false;
			boolean uncertain = false;
			for (Required requirement : rule.required)
			{
				if (!bySlot.containsKey(requirement.slot))
				{
					absent = true;
					break;
				}
				String identity = bySlot.get(requirement.slot);
				if (identity == null)
				{
					uncertain = true;
				}
				else if (!requirement.permitted.contains(identity))
				{
					absent = true;
					break;
				}
			}
			State state = absent ? State.INACTIVE : uncertain ? State.UNKNOWN : State.ACTIVE;
			result.add(new Effect(rule.id, rule.url, rule.mechanics, state));
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Name normalization is limited to cosmetic/equipment-family identity.
	 * A trailing Barrows numeric stage or ornament suffix is ignored; no charge,
	 * durability, or broken-state monitoring is performed.
	 */
	private static String canonical(String name)
	{
		if (name == null || name.trim().isEmpty() || "null".equalsIgnoreCase(name.trim()))
		{
			return null;
		}
		String canonical = name.toLowerCase(Locale.ROOT).trim();
		if (canonical.endsWith(" (or)"))
		{
			canonical = canonical.substring(0, canonical.length() - 5);
		}
		if (canonical.endsWith(" 100") || canonical.endsWith(" 75")
			|| canonical.endsWith(" 50") || canonical.endsWith(" 25")
			|| canonical.endsWith(" 0"))
		{
			canonical = canonical.substring(0, canonical.lastIndexOf(' '));
		}
		return canonical;
	}
}
