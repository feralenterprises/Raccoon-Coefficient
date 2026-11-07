package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EXPERIMENTAL scoring layer "E1". NOT owner approved; NOT the default.
 * <p>
 * The locked v1 base formula is untouched. E1 adds signed adjustment lines on
 * top of it. Every adjustment is expressed as EQUIVALENT EQUIPMENT BONUS: the
 * extra attack/strength bonus that would produce the same change to the
 * accuracy roll or max hit, then priced with the locked v1 weights
 * (0.1 per attack point, 1.0 per strength / magic damage point).
 * <p>
 * Why that is exact for the common effects (game fact): accuracy roll =
 * effective level x (bonus + 64) and melee/ranged max hit is proportional to
 * effective strength x (strength bonus + 64). A multiplier m on the effective
 * level (Void, Barrows-style effects) or on the roll / max hit (Crystal,
 * Obsidian, Inquisitor) therefore equals adding (m - 1) x (bonus + 64) bonus,
 * apart from integer flooring. Magic max hit = base x (1 + magic damage %),
 * so a damage multiplier m equals (m - 1) x (100 + magic damage %) points.
 * <p>
 * SCORING JUDGEMENTS (all need owner approval; see docs):
 * reference attack speed 4 ticks; ranged speed uses Rapid (listed - 1);
 * powered-staff damage priced as the melee strength bonus giving the same max
 * hit for a reference player (99 Strength, aggressive, no prayer/potion:
 * effective strength 110); target-dependent accuracy passives priced at a
 * reference target whose defence roll equals the attack roll ("parity");
 * utility, defensive, HP-state, multi-target and target-specific effects
 * are shown but not scored.
 */
public final class ExperimentalE1
{
	public static final String LABEL = "E1";

	public enum Status
	{
		/** Contributes the stated points. */
		SCORED,
		/** Recognised, deliberately not scored by stated policy (0 points). */
		EXCLUDED,
		/** Inputs needed to score this are not observable: total becomes unknown. */
		UNKNOWN
	}

	/** One explained adjustment. */
	public static final class Line
	{
		private final String key;
		private final Status status;
		private final BigDecimal points;
		private final String reason;

		Line(String key, Status status, BigDecimal points, String reason)
		{
			this.key = key;
			this.status = status;
			this.points = points;
			this.reason = reason;
		}

		public String getKey() { return key; }
		public Status getStatus() { return status; }
		/** Signed points (zero unless SCORED). */
		public BigDecimal getPoints() { return points; }
		public String getReason() { return reason; }

		@Override
		public String toString()
		{
			return key + " " + status + " " + points.setScale(2, RoundingMode.HALF_UP).toPlainString() + " (" + reason + ")";
		}
	}

	/** Evaluation of one worn layout. */
	public static final class Result
	{
		private final List<Line> lines;
		private final boolean known;

		Result(List<Line> lines, boolean known)
		{
			this.lines = Collections.unmodifiableList(lines);
			this.known = known;
		}

		public List<Line> getLines() { return lines; }
		/** False when any needed input is missing; then sums must be treated as unknown. */
		public boolean isKnown() { return known; }

		public BigDecimal sum(String prefix)
		{
			BigDecimal total = BigDecimal.ZERO;
			for (Line line : lines)
			{
				if (line.getKey().startsWith(prefix))
				{
					total = total.add(line.getPoints());
				}
			}
			return total;
		}
	}

	static final BigDecimal REFERENCE_SPEED = BigDecimal.valueOf(4);
	/** 99 Strength + 8 + 3 (aggressive), no prayer or potion. */
	static final BigDecimal REFERENCE_EFFECTIVE_STRENGTH = BigDecimal.valueOf(110);
	private static final BigDecimal SIXTY_FOUR = BigDecimal.valueOf(64);
	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
	private static final int SCALE = 10;

	/**
	 * Equivalent accuracy-roll multiplier minus one at the parity reference.
	 * Fang (outside ToA) at attack roll = defence roll: two attack rolls vs one
	 * defence roll gives hit chance ~2/3 (OSRS Wiki DPS calculator
	 * getFangAccuracyRoll); a normal roll needs 1.5x attack roll for 2/3.
	 */
	static final BigDecimal FANG_PARITY_EXTRA = new BigDecimal("0.5");
	/** Verac's: 25% ignore defence -> 0.25 + 0.75 x 0.5 = 0.625 = normal hit chance at 4/3 roll. */
	static final BigDecimal VERAC_PARITY_EXTRA = BigDecimal.ONE.divide(BigDecimal.valueOf(3), SCALE, RoundingMode.HALF_UP);

	private enum Style { MELEE, RANGED, MAGIC }

	/** Pricing for one detector effect key. Pure data. */
	private static final class Pricing
	{
		final Style style;
		final BigDecimal accuracy;
		final BigDecimal damage;
		final BigDecimal flatMagicDamage;
		final BigDecimal flatPrayer;
		final boolean crushOnly;
		final Status status;
		final String note;

		Pricing(Style style, String accuracy, String damage, String flatMagicDamage, boolean crushOnly,
			Status status, String note)
		{
			this(style, accuracy, damage, flatMagicDamage, "0", crushOnly, status, note);
		}

		Pricing(Style style, String accuracy, String damage, String flatMagicDamage, String flatPrayer,
			boolean crushOnly, Status status, String note)
		{
			this.flatPrayer = new BigDecimal(flatPrayer);
			this.style = style;
			this.accuracy = new BigDecimal(accuracy);
			this.damage = new BigDecimal(damage);
			this.flatMagicDamage = new BigDecimal(flatMagicDamage);
			this.crushOnly = crushOnly;
			this.status = status;
			this.note = note;
		}
	}

	private static Pricing scored(Style s, String acc, String dmg, String note)
	{
		return new Pricing(s, acc, dmg, "0", false, Status.SCORED, note);
	}

	private static Pricing excluded(String note)
	{
		return new Pricing(Style.MELEE, "0", "0", "0", false, Status.EXCLUDED, note);
	}

	private static final Map<String, Pricing> SET_PRICING;
	static
	{
		Map<String, Pricing> p = new HashMap<String, Pricing>();
		p.put("void.melee", scored(Style.MELEE, "0.10", "0.10", "+10% melee accuracy and damage"));
		p.put("void.ranged", scored(Style.RANGED, "0.10", "0.10", "+10% ranged accuracy and damage"));
		p.put("void.elite.ranged", scored(Style.RANGED, "0", "0.025", "elite: +2.5% more ranged damage"));
		p.put("void.magic", scored(Style.MAGIC, "0.45", "0", "+45% magic accuracy"));
		p.put("void.elite.magic", new Pricing(Style.MAGIC, "0", "0", "5", false, Status.SCORED,
			"elite: +5% magic damage (visible additive bonus)"));
		p.put("obsidian", scored(Style.MELEE, "0.10", "0.10", "+10% melee accuracy and damage with a TzHaar weapon"));
		p.put("inquisitor.helm", new Pricing(Style.MELEE, "0.005", "0.005", "0", true, Status.SCORED, "+0.5% crush"));
		p.put("inquisitor.body", new Pricing(Style.MELEE, "0.01", "0.01", "0", true, Status.SCORED, "+1% crush"));
		p.put("inquisitor.legs", new Pricing(Style.MELEE, "0.01", "0.01", "0", true, Status.SCORED, "+1% crush"));
		p.put("crystal.helm", scored(Style.RANGED, "0.05", "0.025", "+5% accuracy, +2.5% damage with crystal bow"));
		p.put("crystal.body", scored(Style.RANGED, "0.15", "0.075", "+15% accuracy, +7.5% damage with crystal bow"));
		p.put("crystal.legs", scored(Style.RANGED, "0.10", "0.05", "+10% accuracy, +5% damage with crystal bow"));
		p.put("barrows.verac", new Pricing(Style.MELEE, VERAC_PARITY_EXTRA.toPlainString(), "0", "0", false,
			Status.SCORED, "25% ignore defence; priced at parity reference target"));
		p.put("barrows.ahrim", excluded("utility: 25% chance to drain target Strength"));
		p.put("barrows.guthan", excluded("sustain: 25% chance to heal; no damage change"));
		p.put("barrows.karil", excluded("utility: 25% chance to drain target Agility"));
		p.put("barrows.torag", excluded("utility: drains target run energy"));
		// Carried forward from RACCOON-CRYSTAL-WAVE's fixed reference: 99 max HP,
		// 50 current HP -> damage x (1 + 49 x 99 / 10000) = x1.4851. Zero at full HP.
		p.put("barrows.dharok", scored(Style.MELEE, "0", "0.4851",
			"damage rises as HP falls; priced at a fixed 50/99 HP reference"));
		p.put("barrows.dharok.damned", excluded("defensive: 25% chance to recoil 15% of damage taken"));
		p.put("barrows.karil.damned", scored(Style.RANGED, "0", "0.125",
			"amulet: 25% chance of an extra hit for half damage = +12.5% expected damage"));
		p.put("barrows.verac.damned", new Pricing(Style.MELEE, "0", "0", "0", "7", false, Status.SCORED,
			"amulet: +7 prayer bonus"));
		p.put("barrows.ahrim.damned", scored(Style.MAGIC, "0", "0.075",
			"amulet: 25% chance of +30% autocast damage = +7.5% expected damage"));
		p.put("justiciar", excluded("defensive damage reduction; not converted to points (owner decision)"));
		SET_PRICING = Collections.unmodifiableMap(p);
	}

	private final GearScoreCoefficients coefficients;
	private final SetEffectDetector setDetector = new SetEffectDetector();

	public ExperimentalE1(GearScoreCoefficients coefficients)
	{
		if (coefficients == null)
		{
			throw new IllegalArgumentException("coefficients are required");
		}
		this.coefficients = coefficients;
	}

	/** Totals of verified item stats over the layout. */
	private static final class Totals
	{
		int stab, slash, crush, magic, ranged, str, rstr;
		BigDecimal mdmg = BigDecimal.ZERO;

		void add(EquipmentBonus b)
		{
			stab += b.getAttackStab();
			slash += b.getAttackSlash();
			crush += b.getAttackCrush();
			magic += b.getAttackMagic();
			ranged += b.getAttackRanged();
			str += b.getMeleeStrength();
			rstr += b.getRangedStrength();
			mdmg = mdmg.add(b.getMagicDamagePercent());
		}
	}

	public Result evaluate(List<WornItem> worn, ItemStatSource stats, SetEffectDetector.ItemNameSource names)
	{
		List<Line> lines = new ArrayList<Line>();
		if (worn == null || stats == null || names == null || WornLayout.hasDuplicateOccupiedSlot(worn))
		{
			lines.add(new Line("e1.input", Status.UNKNOWN, BigDecimal.ZERO, "equipment unavailable"));
			return new Result(lines, false);
		}

		Totals t = new Totals();
		WornItem weaponItem = null;
		WornItem ammoItem = null;
		for (WornItem item : worn)
		{
			if (item == null || item.getItemId() < 0)
			{
				continue;
			}
			EquipmentBonus bonus = stats.getVerifiedBonus(item.getItemId());
			if (bonus == null || WeaponCatalog.normalize(names.nameOf(item.getItemId())) == null)
			{
				lines.add(new Line("e1.input", Status.UNKNOWN, BigDecimal.ZERO,
					"item " + item.getItemId() + " has no verified stats or name"));
				return new Result(lines, false);
			}
			t.add(bonus);
			if (item.getSlotIndex() == EquipmentSlot.WEAPON)
			{
				weaponItem = item;
			}
			else if (item.getSlotIndex() == EquipmentSlot.AMMO)
			{
				ammoItem = item;
			}
		}

		EquipmentBonus weaponStats = weaponItem == null ? null : stats.getVerifiedBonus(weaponItem.getItemId());
		String weaponName = weaponItem == null ? null : names.nameOf(weaponItem.getItemId());
		WeaponCatalog.Profile weapon = weaponItem == null
			? WeaponCatalog.unarmed() : WeaponCatalog.classify(weaponName, weaponStats);
		boolean known = true;

		// ---- 1. Ammunition relevance -------------------------------------
		if (ammoItem != null)
		{
			EquipmentBonus ammoStats = stats.getVerifiedBonus(ammoItem.getItemId());
			WeaponCatalog.Applicability fired = WeaponCatalog.applicability(weapon,
				WeaponCatalog.parseAmmo(names.nameOf(ammoItem.getItemId())), ammoStats);
			if (fired == WeaponCatalog.Applicability.NOT_FIRED)
			{
				BigDecimal removed = BigDecimal.valueOf(ammoStats.getAttackRanged()).multiply(coefficients.getAttackPerPoint())
					.add(BigDecimal.valueOf(ammoStats.getRangedStrength()).multiply(coefficients.getRangedStrengthPerPoint()));
				t.ranged -= ammoStats.getAttackRanged();
				t.rstr -= ammoStats.getRangedStrength();
				lines.add(new Line("weapon.ammo.not_fired", Status.SCORED, removed.negate(),
					"weapon does not fire this ammunition; its ranged bonuses do nothing"));
			}
			else if (fired == WeaponCatalog.Applicability.UNKNOWN)
			{
				lines.add(new Line("weapon.ammo.unknown", Status.UNKNOWN, BigDecimal.ZERO,
					"cannot tell whether this weapon fires this ammunition"));
				known = false;
			}
		}

		// ---- 2. Weapon's own damage source and attack speed ---------------
		int meleeType = bestMeleeType(weaponStats);
		BigDecimal poweredEquivalent = null;
		if (weapon.getKind() == WeaponCatalog.Kind.POWERED_STAFF)
		{
			int maxHit = WeaponCatalog.poweredStaffMaxHitAt99(weapon.getPoweredStaffKey());
			// s such that floor(0.5 + 110 (s + 64) / 640) reaches this max hit (continuous form).
			poweredEquivalent = BigDecimal.valueOf(640L * maxHit)
				.divide(REFERENCE_EFFECTIVE_STRENGTH, SCALE, RoundingMode.HALF_UP).subtract(SIXTY_FOUR);
			lines.add(new Line("weapon.powered_staff", Status.SCORED,
				poweredEquivalent.multiply(coefficients.getMeleeStrengthPerPoint()),
				"built-in spell max hit " + maxHit + " at 99 Magic, priced as equal-max-hit melee strength"));
		}
		if (weapon.hasHiddenAmmo())
		{
			lines.add(new Line("weapon.hidden_ammo", Status.UNKNOWN, BigDecimal.ZERO,
				"damage depends on darts stored inside the weapon, which are not observable"));
			known = false;
		}

		BigDecimal damageTerm = null; // (strength-equivalent + 64) that attack speed scales
		BigDecimal strengthCoefficient = coefficients.getMeleeStrengthPerPoint();
		int speed = weaponStats == null ? 4 : weaponStats.getAttackSpeed();
		switch (weapon.getKind())
		{
			case NONE:
				speed = 4;
				damageTerm = BigDecimal.valueOf(t.str).add(SIXTY_FOUR);
				break;
			case MELEE:
				damageTerm = BigDecimal.valueOf(t.str).add(SIXTY_FOUR);
				break;
			case LAUNCHER:
			case SELF_AMMO_RANGED:
			case UNKNOWN_RANGED:
				speed = speed <= 0 ? 0 : Math.max(1, speed - 1); // Rapid
				if (weapon.usesMeleeStrengthForRanged())
				{
					damageTerm = BigDecimal.valueOf(t.str).add(SIXTY_FOUR);
				}
				else
				{
					damageTerm = BigDecimal.valueOf(t.rstr).add(SIXTY_FOUR);
					strengthCoefficient = coefficients.getRangedStrengthPerPoint();
				}
				break;
			case POWERED_STAFF:
				damageTerm = poweredEquivalent.add(SIXTY_FOUR);
				break;
			case SPELL_STAFF:
			default:
				lines.add(new Line("weapon.speed", Status.EXCLUDED, BigDecimal.ZERO,
					"spell damage is chosen by the player, so cast speed is not scored"));
				break;
		}
		if (damageTerm != null)
		{
			if (speed <= 0)
			{
				lines.add(new Line("weapon.speed", Status.UNKNOWN, BigDecimal.ZERO, "attack speed unknown"));
				known = false;
			}
			else if (speed != 4)
			{
				BigDecimal factor = REFERENCE_SPEED.divide(BigDecimal.valueOf(speed), SCALE, RoundingMode.HALF_UP)
					.subtract(BigDecimal.ONE);
				lines.add(new Line("weapon.speed", Status.SCORED,
					damageTerm.multiply(factor).multiply(strengthCoefficient),
					speed + "-tick attacks vs the 4-tick reference"));
			}
		}

		// ---- 3. Weapon passives -------------------------------------------
		String wn = WeaponCatalog.normalize(weaponName);
		if (wn != null)
		{
			if (wn.startsWith("osmumten's fang"))
			{
				if (meleeType == 0)
				{
					lines.add(new Line("passive.fang.accuracy", Status.SCORED,
						accuracyPoints(FANG_PARITY_EXTRA, t.stab),
						"stab: two attack rolls vs one defence roll; priced at parity reference target"));
				}
				lines.add(new Line("passive.fang.damage_band", Status.EXCLUDED, BigDecimal.ZERO,
					"15-85% damage band does not change expected damage"));
			}
			else if (wn.equals("twinflame staff"))
			{
				lines.add(new Line("passive.twinflame.standard", Status.SCORED,
					accuracyPoints(new BigDecimal("0.10"), t.magic).add(magicDamagePoints(new BigDecimal("0.10"), t.mdmg)),
					"+10% accuracy and damage with standard spellbook spells"));
				lines.add(new Line("passive.twinflame.second_hit", Status.EXCLUDED, BigDecimal.ZERO,
					"40% second hit depends on spell choice (not Strike or Surge)"));
			}
			else if (wn.equals("tumeken's shadow"))
			{
				BigDecimal tripled = t.mdmg.multiply(BigDecimal.valueOf(3)).min(HUNDRED);
				BigDecimal points = BigDecimal.valueOf(2L * t.magic).multiply(coefficients.getAttackPerPoint())
					.add(tripled.subtract(t.mdmg).multiply(coefficients.getMagicDamagePerPoint()));
				lines.add(new Line("passive.shadow.multiplier", Status.SCORED, points,
					"x3 worn magic attack and magic damage for its built-in spell (100% cap)"));
			}
			else if (wn.startsWith("venator bow"))
			{
				lines.add(new Line("passive.venator.bounce", Status.EXCLUDED, BigDecimal.ZERO,
					"extra targets in multicombat only"));
			}
			else if (wn.contains("scythe of vitur"))
			{
				lines.add(new Line("passive.scythe.multi_hit", Status.EXCLUDED, BigDecimal.ZERO,
					"extra hits depend on target size"));
			}
			else if (wn.equals("twisted bow"))
			{
				lines.add(new Line("passive.twisted_bow.scaling", Status.EXCLUDED, BigDecimal.ZERO,
					"scales with the target's Magic level (target-specific)"));
			}
			else if (wn.equals("soulreaper axe"))
			{
				lines.add(new Line("passive.soulreaper.stacks", Status.EXCLUDED, BigDecimal.ZERO,
					"strength stacks build during combat and cost HP on special"));
			}
		}

		// ---- 4. Set effects -------------------------------------------------
		for (SetEffectDetector.Effect effect : setDetector.evaluate(worn, names))
		{
			if (effect.getState() == SetEffectDetector.State.INACTIVE)
			{
				continue;
			}
			String key = "set." + effect.getKey();
			if (effect.getState() == SetEffectDetector.State.UNKNOWN)
			{
				lines.add(new Line(key, Status.UNKNOWN, BigDecimal.ZERO, "set pieces not identifiable"));
				known = false;
				continue;
			}
			Pricing pricing = SET_PRICING.get(effect.getKey());
			if (pricing == null)
			{
				lines.add(new Line(key, Status.UNKNOWN, BigDecimal.ZERO, "active set effect not modelled in E1"));
				known = false;
				continue;
			}
			if (pricing.status == Status.EXCLUDED)
			{
				lines.add(new Line(key, Status.EXCLUDED, BigDecimal.ZERO, pricing.note));
				continue;
			}
			if (pricing.crushOnly && meleeType != 2)
			{
				lines.add(new Line(key, Status.EXCLUDED, BigDecimal.ZERO, "crush-only effect; weapon's best style is not crush"));
				continue;
			}
			lines.add(new Line(key, Status.SCORED, price(pricing, t, meleeType, weapon)
				.add(pricing.flatPrayer.multiply(coefficients.getPrayerPerPoint())), pricing.note));
		}

		return new Result(lines, known);
	}

	private BigDecimal price(Pricing p, Totals t, int meleeType, WeaponCatalog.Profile weapon)
	{
		switch (p.style)
		{
			case MELEE:
			{
				int attack = meleeType == 0 ? t.stab : meleeType == 1 ? t.slash : t.crush;
				return accuracyPoints(p.accuracy, attack).add(strengthPoints(p.damage, t.str,
					coefficients.getMeleeStrengthPerPoint()));
			}
			case RANGED:
			{
				int strength = weapon.usesMeleeStrengthForRanged() ? t.str : t.rstr;
				// Ogre bows' max hit ignores all other gear and bonuses (ported
				// from RACCOON-CRYSTAL-WAVE; DPS calculator PlayerVsNPCCalc).
				BigDecimal damage = weapon.getFiresAmmo() == WeaponCatalog.AmmoClass.OGRE_ARROW ? BigDecimal.ZERO : p.damage;
				return accuracyPoints(p.accuracy, t.ranged).add(strengthPoints(damage, strength,
					coefficients.getRangedStrengthPerPoint()));
			}
			case MAGIC:
			default:
				return accuracyPoints(p.accuracy, t.magic)
					.add(magicDamagePoints(p.damage, t.mdmg))
					.add(p.flatMagicDamage.multiply(coefficients.getMagicDamagePerPoint()));
		}
	}

	private BigDecimal accuracyPoints(BigDecimal extra, int attackBonus)
	{
		return extra.multiply(BigDecimal.valueOf(attackBonus).add(SIXTY_FOUR)).multiply(coefficients.getAttackPerPoint());
	}

	private static BigDecimal strengthPoints(BigDecimal extra, int strengthBonus, BigDecimal weight)
	{
		return extra.multiply(BigDecimal.valueOf(strengthBonus).add(SIXTY_FOUR)).multiply(weight);
	}

	private BigDecimal magicDamagePoints(BigDecimal extra, BigDecimal magicDamage)
	{
		return extra.multiply(magicDamage.add(HUNDRED)).multiply(coefficients.getMagicDamagePerPoint());
	}

	/** 0 stab, 1 slash, 2 crush: the weapon's highest melee attack bonus (unarmed = crush). */
	static int bestMeleeType(EquipmentBonus weapon)
	{
		if (weapon == null)
		{
			return 2;
		}
		int best = 0;
		int value = weapon.getAttackStab();
		if (weapon.getAttackSlash() > value)
		{
			best = 1;
			value = weapon.getAttackSlash();
		}
		if (weapon.getAttackCrush() > value)
		{
			best = 2;
		}
		return best;
	}
}
