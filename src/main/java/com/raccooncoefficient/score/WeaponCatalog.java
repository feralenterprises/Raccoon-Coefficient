package com.raccooncoefficient.score;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Data-driven weapon and ammunition families for the EXPERIMENTAL E1 model.
 * <p>
 * Items are classified by reusable name families (bow wood/metal tiers,
 * crossbow metal tiers, thrown weapons, staves) rather than one rule per item.
 * Verified exceptions that the game actually requires (powered-staff built-in
 * spells, weapons that ignore the ammo slot) are listed as data.
 * <p>
 * GAME FACTS used here (OSRS Wiki, see docs/RC-E1-EXPERIMENTAL-MODEL.md):
 * arrow/bow tiers (w/Arrows), bolt/crossbow metal tiers (w/Bolts), powered
 * staff built-in max hits at a given Magic level (individual item pages and
 * the Wiki DPS calculator's implementation), and that darts/knives/
 * chinchompas supply their own Ranged Strength from the weapon slot
 * (w/Ranged_Strength). This class contains no scoring coefficients.
 */
public final class WeaponCatalog
{
	public enum Kind
	{
		/** Nothing in the weapon slot (unarmed, crush, 4 ticks). */
		NONE,
		MELEE,
		/** Fires projectiles from the ammo slot (bows, crossbows, ballistae). */
		LAUNCHER,
		/** Ranged weapon that never fires the ammo slot (thrown, crystal bows, blowpipe). */
		SELF_AMMO_RANGED,
		/** Casts a built-in spell; damage comes from the weapon itself. */
		POWERED_STAFF,
		/** Autocasts player-chosen spells; the weapon supplies no base damage. */
		SPELL_STAFF,
		/** Ranged-looking weapon whose family is not catalogued. */
		UNKNOWN_RANGED
	}

	public enum AmmoClass
	{
		ARROW, OGRE_ARROW, BOLT, JAVELIN, BOLT_RACK, ATLATL_DART, OTHER, NONE
	}

	public enum Applicability
	{
		/** Ammo has no ranged attack or ranged strength, so firing is irrelevant. */
		IRRELEVANT,
		FIRED,
		NOT_FIRED,
		UNKNOWN
	}

	/** Classified weapon. Immutable. */
	public static final class Profile
	{
		private final Kind kind;
		private final AmmoClass firesAmmo;
		private final int ammoTier;
		private final String poweredStaffKey;
		private final boolean usesMeleeStrengthForRanged;
		private final boolean hiddenAmmo;

		Profile(Kind kind, AmmoClass firesAmmo, int ammoTier, String poweredStaffKey,
			boolean usesMeleeStrengthForRanged, boolean hiddenAmmo)
		{
			this.kind = kind;
			this.firesAmmo = firesAmmo;
			this.ammoTier = ammoTier;
			this.poweredStaffKey = poweredStaffKey;
			this.usesMeleeStrengthForRanged = usesMeleeStrengthForRanged;
			this.hiddenAmmo = hiddenAmmo;
		}

		public Kind getKind() { return kind; }
		public AmmoClass getFiresAmmo() { return firesAmmo; }
		/** Highest ammo tier fired, or 0 when unknown/not applicable. */
		public int getAmmoTier() { return ammoTier; }
		/** Key into {@link #poweredStaffMaxHitAt99}, or null. */
		public String getPoweredStaffKey() { return poweredStaffKey; }
		/** Eclipse atlatl: ranged damage scales with melee strength. */
		public boolean usesMeleeStrengthForRanged() { return usesMeleeStrengthForRanged; }
		/** Damage depends on ammunition stored inside the weapon (blowpipe darts). */
		public boolean hasHiddenAmmo() { return hiddenAmmo; }
		public boolean isRanged()
		{
			return kind == Kind.LAUNCHER || kind == Kind.SELF_AMMO_RANGED || kind == Kind.UNKNOWN_RANGED;
		}
	}

	/** Parsed ammunition. */
	public static final class Ammo
	{
		private final AmmoClass ammoClass;
		private final int tier;

		Ammo(AmmoClass ammoClass, int tier)
		{
			this.ammoClass = ammoClass;
			this.tier = tier;
		}

		public AmmoClass getAmmoClass() { return ammoClass; }
		/** 0 = unknown tier. */
		public int getTier() { return tier; }
	}

	// ---- Powered staves: base max hit of the built-in spell at 99 Magic ----
	// Game facts. Trident of the seas: floor(Magic/3) - 5; Trident of the
	// swamp: floor(Magic/3) - 2; Sanguinesti: floor(Magic/3); Tumeken's shadow:
	// floor(Magic/3) + 1; Eye of Ayak and Accursed sceptre: floor(Magic/3) - 6;
	// Thammaron's sceptre: floor(Magic/3) - 8; Warped sceptre:
	// floor((8*Magic + 96) / 37). Charged, equippable variants only.
	private static final Map<String, Integer> POWERED_AT_99;
	private static final Map<String, String> POWERED_NAMES;
	static
	{
		Map<String, Integer> max = new HashMap<String, Integer>();
		max.put("trident.seas", 99 / 3 - 5);
		max.put("trident.swamp", 99 / 3 - 2);
		max.put("sanguinesti", 99 / 3);
		max.put("shadow", 99 / 3 + 1);
		max.put("ayak", 99 / 3 - 6);
		max.put("accursed", 99 / 3 - 6);
		max.put("thammaron", 99 / 3 - 8);
		max.put("warped", (8 * 99 + 96) / 37);
		POWERED_AT_99 = Collections.unmodifiableMap(max);

		Map<String, String> n = new HashMap<String, String>();
		n.put("trident of the seas", "trident.seas");
		n.put("trident of the seas (e)", "trident.seas");
		n.put("trident of the seas (full)", "trident.seas");
		n.put("trident of the swamp", "trident.swamp");
		n.put("trident of the swamp (e)", "trident.swamp");
		n.put("sanguinesti staff", "sanguinesti");
		n.put("holy sanguinesti staff", "sanguinesti");
		n.put("tumeken's shadow", "shadow");
		n.put("eye of ayak", "ayak");
		n.put("accursed sceptre", "accursed");
		n.put("thammaron's sceptre", "thammaron");
		n.put("warped sceptre", "warped");
		POWERED_NAMES = Collections.unmodifiableMap(n);
	}

	private WeaponCatalog()
	{
	}

	/** Built-in spell base max hit at 99 Magic, or null if not a powered staff. */
	public static Integer poweredStaffMaxHitAt99(String key)
	{
		return key == null ? null : POWERED_AT_99.get(key);
	}

	public static String normalize(String name)
	{
		if (name == null)
		{
			return null;
		}
		String n = name.trim().toLowerCase(Locale.ROOT);
		if (n.isEmpty() || "null".equals(n))
		{
			return null;
		}
		return n;
	}

	/**
	 * Classify a weapon. {@code name} null means unidentified: callers must
	 * treat that as unknown, so this returns null rather than guessing.
	 */
	public static Profile classify(String rawName, EquipmentBonus stats)
	{
		String n = normalize(rawName);
		if (n == null)
		{
			return null;
		}
		String powered = POWERED_NAMES.get(n);
		if (powered != null)
		{
			return new Profile(Kind.POWERED_STAFF, AmmoClass.NONE, 0, powered, false, false);
		}

		// Ranged weapons that ignore the ammo slot.
		if (n.startsWith("crystal bow") || n.startsWith("bow of faerdhinen")
			|| n.startsWith("craw's bow") || n.startsWith("webweaver bow"))
		{
			return self(false);
		}
		if (n.contains("blowpipe"))
		{
			return self(true);
		}
		if (n.endsWith("knife") || n.contains("knife(") || n.contains("knife (")
			|| (n.contains(" dart") && !n.contains("atlatl")) || n.endsWith("thrownaxe")
			|| n.contains("chinchompa") || n.startsWith("toktz-xil-ul"))
		{
			return self(false);
		}
		if (n.startsWith("eclipse atlatl"))
		{
			return new Profile(Kind.LAUNCHER, AmmoClass.ATLATL_DART, 1, null, true, false);
		}

		// Launchers that fire the ammo slot.
		if (n.contains("karil's crossbow"))
		{
			return launcher(AmmoClass.BOLT_RACK, 1);
		}
		if (n.contains("crossbow"))
		{
			return launcher(AmmoClass.BOLT, crossbowTier(n));
		}
		if (n.contains("ballista"))
		{
			return launcher(AmmoClass.JAVELIN, 1);
		}
		if (n.contains("ogre bow"))
		{
			return launcher(AmmoClass.OGRE_ARROW, 1);
		}
		int bowTier = bowTier(n);
		if (bowTier != Integer.MIN_VALUE)
		{
			return launcher(AmmoClass.ARROW, bowTier);
		}

		// Magic weapons. Name families, because many staves carry melee stats
		// (e.g. Ahrim's staff, Twinflame staff, Staff of the dead).
		if (n.contains("staff") || n.contains("wand") || n.contains("sceptre"))
		{
			return new Profile(Kind.SPELL_STAFF, AmmoClass.NONE, 0, null, false, false);
		}

		// Uncatalogued: fall back to the item's own offensive stats.
		if (stats != null)
		{
			int melee = Math.max(stats.getAttackStab(), Math.max(stats.getAttackSlash(), stats.getAttackCrush()));
			if (stats.getAttackRanged() > melee && stats.getAttackRanged() > stats.getAttackMagic())
			{
				return new Profile(Kind.UNKNOWN_RANGED, AmmoClass.OTHER, 0, null, false, false);
			}
		}
		return new Profile(Kind.MELEE, AmmoClass.NONE, 0, null, false, false);
	}

	public static Profile unarmed()
	{
		return new Profile(Kind.NONE, AmmoClass.NONE, 0, null, false, false);
	}

	private static Profile self(boolean hiddenAmmo)
	{
		return new Profile(Kind.SELF_AMMO_RANGED, AmmoClass.NONE, 0, null, false, hiddenAmmo);
	}

	private static Profile launcher(AmmoClass ammo, int tier)
	{
		return new Profile(Kind.LAUNCHER, ammo, tier, null, false, false);
	}

	/** Bow tier by wood, or MIN_VALUE if not a bow; 0 = bow of unknown tier. */
	private static int bowTier(String n)
	{
		if (n.startsWith("dark bow") || n.startsWith("twisted bow") || n.startsWith("3rd age bow")
			|| n.startsWith("venator bow") || n.startsWith("scorching bow"))
		{
			return 7; // dragon arrows
		}
		boolean bow = n.contains("shortbow") || n.contains("longbow") || n.contains("comp bow")
			|| n.startsWith("seercull") || n.equals("signed oak bow") || n.startsWith("cursed goblin bow")
			|| n.equals("rain bow");
		if (!bow)
		{
			return n.endsWith(" bow") ? 0 : Integer.MIN_VALUE;
		}
		if (n.startsWith("magic ") || n.startsWith("seercull") || n.startsWith("bone shortbow"))
		{
			return 6; // amethyst / broad
		}
		if (n.startsWith("yew ")) return 5;
		if (n.startsWith("maple ")) return 4;
		if (n.startsWith("willow ")) return 3;
		if (n.startsWith("oak ") || n.equals("signed oak bow")) return 2;
		if (n.startsWith("shortbow") || n.startsWith("longbow")
			|| n.startsWith("cursed goblin bow") || n.equals("rain bow"))
		{
			return 1;
		}
		return 0;
	}

	private static int crossbowTier(String n)
	{
		if (n.startsWith("dragon") || n.startsWith("armadyl") || n.startsWith("zaryte"))
		{
			return 8;
		}
		if (n.startsWith("rune") || n.startsWith("runite")) return 7;
		if (n.startsWith("adamant")) return 6;
		if (n.startsWith("mithril")) return 5;
		if (n.startsWith("steel")) return 4;
		if (n.startsWith("iron")) return 3;
		if (n.startsWith("blurite")) return 2;
		if (n.startsWith("bronze") || n.equals("crossbow") || n.startsWith("phoenix crossbow")) return 1;
		return 0; // hunters', dorgeshuun and other special crossbows: not catalogued
	}

	public static Ammo parseAmmo(String rawName)
	{
		String n = normalize(rawName);
		if (n == null)
		{
			return null;
		}
		if (n.contains("bolt rack")) return new Ammo(AmmoClass.BOLT_RACK, 1);
		if (n.contains("javelin")) return new Ammo(AmmoClass.JAVELIN, 1);
		if (n.contains("atlatl dart")) return new Ammo(AmmoClass.ATLATL_DART, 1);
		if (n.contains("ogre arrow") || n.contains("brutal arrow")) return new Ammo(AmmoClass.OGRE_ARROW, 1);
		if (n.contains("arrow"))
		{
			return new Ammo(AmmoClass.ARROW, arrowTier(n));
		}
		if (n.contains("bolt"))
		{
			return new Ammo(AmmoClass.BOLT, boltTier(n));
		}
		return new Ammo(AmmoClass.OTHER, 0);
	}

	private static int arrowTier(String n)
	{
		if (n.startsWith("dragon")) return 7;
		if (n.startsWith("amethyst") || n.startsWith("broad")) return 6;
		if (n.startsWith("rune") || n.startsWith("ice arrow")) return 5;
		if (n.startsWith("adamant")) return 4;
		if (n.startsWith("mithril")) return 3;
		if (n.startsWith("steel")) return 2;
		if (n.startsWith("bronze") || n.startsWith("iron")) return 1;
		return 0;
	}

	private static int boltTier(String n)
	{
		if (n.contains("dragon bolt")) return 8; // includes gem-tipped dragon bolts
		if (n.startsWith("dragonstone") || n.startsWith("onyx") || n.startsWith("runite")
			|| n.contains("broad bolt")) return 7;
		if (n.startsWith("ruby") || n.startsWith("diamond") || n.startsWith("adamant")) return 6;
		if (n.startsWith("sapphire") || n.startsWith("emerald") || n.startsWith("mithril")) return 5;
		if (n.startsWith("topaz") || n.startsWith("steel")) return 4;
		if (n.startsWith("pearl") || n.startsWith("iron") || n.startsWith("silver")) return 3;
		if (n.startsWith("jade") || n.startsWith("blurite")) return 2;
		if (n.startsWith("opal") || n.startsWith("bronze")) return 1;
		return 0;
	}

	/**
	 * Whether the weapon actually fires this ammunition. A weapon that is not
	 * a launcher never fires the ammo slot; a launcher fires matching ammo up
	 * to its tier. Uncatalogued tiers stay UNKNOWN rather than guessed.
	 */
	public static Applicability applicability(Profile weapon, Ammo ammo, EquipmentBonus ammoStats)
	{
		if (ammoStats != null && ammoStats.getAttackRanged() == 0 && ammoStats.getRangedStrength() == 0)
		{
			return Applicability.IRRELEVANT;
		}
		if (weapon == null || ammo == null || ammoStats == null)
		{
			return Applicability.UNKNOWN;
		}
		switch (weapon.getKind())
		{
			case NONE:
			case MELEE:
			case SPELL_STAFF:
			case POWERED_STAFF:
			case SELF_AMMO_RANGED:
				return Applicability.NOT_FIRED;
			case UNKNOWN_RANGED:
				return Applicability.UNKNOWN;
			case LAUNCHER:
			default:
				break;
		}
		if (ammo.getAmmoClass() == AmmoClass.OTHER)
		{
			return Applicability.UNKNOWN;
		}
		if (ammo.getAmmoClass() != weapon.getFiresAmmo())
		{
			return Applicability.NOT_FIRED;
		}
		if (weapon.getAmmoTier() == 0 || ammo.getTier() == 0)
		{
			return Applicability.UNKNOWN;
		}
		return ammo.getTier() <= weapon.getAmmoTier() ? Applicability.FIRED : Applicability.NOT_FIRED;
	}
}
