package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sums worn equipment into one score.
 * <p>
 * Wave 1 total = base equipment + set bonus + weapon passive. The Wave 1
 * calculator is constructed with no set or weapon contributors, so those
 * terms are zero. Each worn item is scored once. Empty slots are omitted by
 * the caller and contribute nothing. An item id with no verified bonus is
 * skipped and marks the total incomplete.
 */
public final class GearScoreCalculator
{
	private final GearScoreCoefficients coefficients;
	private final List<SetBonus> setBonuses;
	private final List<WeaponPassive> weaponPassives;

	public GearScoreCalculator(GearScoreCoefficients coefficients)
	{
		this(coefficients, Collections.<SetBonus>emptyList(), Collections.<WeaponPassive>emptyList());
	}

	public GearScoreCalculator(
		GearScoreCoefficients coefficients,
		List<SetBonus> setBonuses,
		List<WeaponPassive> weaponPassives)
	{
		if (coefficients == null)
		{
			throw new IllegalArgumentException("coefficients are required");
		}
		this.coefficients = coefficients;
		this.setBonuses = copy(setBonuses);
		this.weaponPassives = copy(weaponPassives);
	}

	public static GearScoreCalculator wave1()
	{
		return new GearScoreCalculator(GearScoreCoefficients.wave1());
	}

	/**
	 * Wave 2 retains the locked base formula. An equipped unrated set effect
	 * deliberately marks the total incomplete instead of fabricating points.
	 */
	public static GearScoreCalculator wave2(SetEffectDetector.ItemNameSource identities)
	{
		return new GearScoreCalculator(
			GearScoreCoefficients.wave1(),
			Collections.<SetBonus>singletonList(
				new PendingSetEffectBonus(new SetEffectDetector(), identities)),
			Collections.<WeaponPassive>emptyList());
	}

	/**
	 * Wave 3 includes Wave 2 armour detection and weapon-passive identification.
	 * Neither extension supplies an invented numerical coefficient: active or
	 * contextual effects mark the aggregate as an incomplete base subtotal.
	 */
	public static GearScoreCalculator wave3(SetEffectDetector.ItemNameSource identities)
	{
		return new GearScoreCalculator(
			GearScoreCoefficients.wave1(),
			Collections.<SetBonus>singletonList(
				new PendingSetEffectBonus(new SetEffectDetector(), identities)),
			Collections.<WeaponPassive>singletonList(
				new PendingWeaponPassiveBonus(new WeaponPassiveDetector(), identities)));
	}

	public GearScoreCoefficients getCoefficients()
	{
		return coefficients;
	}

	public GearScoreBreakdown score(List<WornItem> worn, ItemStatSource source)
	{
		List<WornItem> items = worn == null ? Collections.<WornItem>emptyList() : worn;
		BigDecimal base = BigDecimal.ZERO;
		int unknownItems = 0;

		for (int i = 0; i < items.size(); i++)
		{
			WornItem item = items.get(i);
			if (item == null || item.getItemId() < 0)
			{
				continue;
			}
			EquipmentBonus bonus = source == null ? null : source.getVerifiedBonus(item.getItemId());
			if (bonus == null)
			{
				unknownItems++;
				continue;
			}
			base = base.add(scoreBonus(bonus));
		}

		BigDecimal set = BigDecimal.ZERO;
		BigDecimal passive = BigDecimal.ZERO;
		boolean extensionsKnown = true;
		for (int i = 0; i < setBonuses.size(); i++)
		{
			BigDecimal part = setBonuses.get(i).gearScore(worn == null ? null : items, source);
			if (part == null)
			{
				extensionsKnown = false;
				continue;
			}
			set = set.add(part);
		}
		for (int i = 0; i < weaponPassives.size(); i++)
		{
			BigDecimal part = weaponPassives.get(i).gearScore(worn == null ? null : items, source);
			if (part == null)
			{
				extensionsKnown = false;
				continue;
			}
			passive = passive.add(part);
		}

		return new GearScoreBreakdown(base, set, passive, unknownItems == 0, extensionsKnown, unknownItems);
	}

	public BigDecimal scoreBonus(EquipmentBonus bonus)
	{
		BigDecimal attack = BigDecimal.valueOf(bonus.getAttackStab()
			+ bonus.getAttackSlash()
			+ bonus.getAttackCrush()
			+ bonus.getAttackMagic()
			+ bonus.getAttackRanged()).multiply(coefficients.getAttackPerPoint());
		BigDecimal defence = BigDecimal.valueOf(bonus.getDefenceStab()
			+ bonus.getDefenceSlash()
			+ bonus.getDefenceCrush()
			+ bonus.getDefenceMagic()
			+ bonus.getDefenceRanged()).multiply(coefficients.getDefencePerPoint());
		BigDecimal melee = BigDecimal.valueOf(bonus.getMeleeStrength()).multiply(coefficients.getMeleeStrengthPerPoint());
		BigDecimal ranged = BigDecimal.valueOf(bonus.getRangedStrength()).multiply(coefficients.getRangedStrengthPerPoint());
		BigDecimal magic = bonus.getMagicDamagePercent().multiply(coefficients.getMagicDamagePerPoint());
		BigDecimal prayer = BigDecimal.valueOf(bonus.getPrayer()).multiply(coefficients.getPrayerPerPoint());
		return attack.add(defence).add(melee).add(ranged).add(magic).add(prayer);
	}

	public GearScoreComparison compare(int candidateItemId, List<WornItem> worn, ItemStatSource source)
	{
		if (source == null)
		{
			return GearScoreComparison.unknownItem();
		}
		EquipmentBonus candidate = source.getVerifiedBonus(candidateItemId);
		if (candidate == null)
		{
			return GearScoreComparison.unknownItem();
		}

		if (worn == null)
		{
			return GearScoreComparison.knownItemUnknownDelta(round1(scoreBonus(candidate)));
		}

		List<WornItem> current = worn;
		Map<Integer, WornItem> bySlot = indexBySlot(current);
		List<Integer> affected = affectedSlots(candidate, bySlot, source);
		if (affected == null)
		{
			return GearScoreComparison.knownItemUnknownDelta(round1(scoreBonus(candidate)));
		}

		// The known base-stat change can be computed from ONLY displaced slots.
		// Missing unrelated gear must not be silently treated as zero, and an
		// unresolved effect must not suppress an otherwise verified base delta.
		BigDecimal baseDelta = scoreBonus(candidate);
		for (Integer slot : affected)
		{
			WornItem removed = bySlot.get(slot);
			if (removed != null)
			{
				EquipmentBonus removedBonus = source.getVerifiedBonus(removed.getItemId());
				if (removedBonus == null)
				{
					return GearScoreComparison.knownItemUnknownDelta(round1(scoreBonus(candidate)));
				}
				baseDelta = baseDelta.subtract(scoreBonus(removedBonus));
			}
		}

		List<WornItem> after = new ArrayList<WornItem>(current.size() + 1);
		for (int i = 0; i < current.size(); i++)
		{
			WornItem item = current.get(i);
			if (item == null || item.getItemId() < 0)
			{
				continue;
			}
			if (!affected.contains(Integer.valueOf(item.getSlotIndex())))
			{
				after.add(item);
			}
		}
		after.add(new WornItem(candidate.getSlot(), candidateItemId));

		GearScoreBreakdown before = score(current, source);
		GearScoreBreakdown next = score(after, source);
		BigDecimal itemScore = round1(scoreBonus(candidate));

		if (!before.isComplete() || !next.isComplete())
		{
			return GearScoreComparison.knownItemBaseOnlyDelta(itemScore, round1(baseDelta));
		}

		// Round the precise change ONCE; subtracting two rounded displayed
		// totals can introduce a spurious 0.1 difference at HALF_UP boundaries.
		BigDecimal delta = round1(next.totalUnrounded().subtract(before.totalUnrounded()));
		return GearScoreComparison.knownItem(itemScore, delta);
	}

	/**
	 * Slots whose current items are removed if the candidate is equipped.
	 * Returns null when a removed slot, or a weapon that might be two-handed,
	 * has no verified data.
	 */
	private List<Integer> affectedSlots(EquipmentBonus candidate, Map<Integer, WornItem> bySlot, ItemStatSource source)
	{
		List<Integer> affected = new ArrayList<Integer>(2);
		affected.add(Integer.valueOf(candidate.getSlot()));

		if (candidate.isTwoHanded() && candidate.getSlot() == EquipmentSlot.WEAPON)
		{
			affected.add(Integer.valueOf(EquipmentSlot.SHIELD));
		}

		if (candidate.getSlot() == EquipmentSlot.SHIELD)
		{
			WornItem weapon = bySlot.get(Integer.valueOf(EquipmentSlot.WEAPON));
			if (weapon != null)
			{
				EquipmentBonus weaponBonus = source.getVerifiedBonus(weapon.getItemId());
				if (weaponBonus == null)
				{
					return null;
				}
				if (weaponBonus.isTwoHanded())
				{
					affected.add(Integer.valueOf(EquipmentSlot.WEAPON));
				}
			}
		}

		for (int i = 0; i < affected.size(); i++)
		{
			WornItem existing = bySlot.get(affected.get(i));
			if (existing != null && source.getVerifiedBonus(existing.getItemId()) == null)
			{
				return null;
			}
		}
		return affected;
	}

	private static Map<Integer, WornItem> indexBySlot(List<WornItem> worn)
	{
		Map<Integer, WornItem> bySlot = new HashMap<Integer, WornItem>();
		for (int i = 0; i < worn.size(); i++)
		{
			WornItem item = worn.get(i);
			if (item == null || item.getItemId() < 0)
			{
				continue;
			}
			bySlot.put(Integer.valueOf(item.getSlotIndex()), item);
		}
		return bySlot;
	}

	private static BigDecimal round1(BigDecimal value)
	{
		return value.setScale(1, RoundingMode.HALF_UP);
	}

	private static <T> List<T> copy(List<T> values)
	{
		if (values == null || values.isEmpty())
		{
			return Collections.emptyList();
		}
		List<T> copy = new ArrayList<T>(values.size());
		for (int i = 0; i < values.size(); i++)
		{
			T value = values.get(i);
			if (value == null)
			{
				throw new IllegalArgumentException("contributor is required");
			}
			copy.add(value);
		}
		return Collections.unmodifiableList(copy);
	}
}
