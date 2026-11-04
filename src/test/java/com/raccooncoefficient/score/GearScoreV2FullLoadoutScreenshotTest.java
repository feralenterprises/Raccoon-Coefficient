package com.raccooncoefficient.score;

import java.math.BigDecimal;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Four complete summary panels supplied by the owner on 2026-11-04.
 *
 * Each fixture represents AGGREGATE displayed equipment bonuses as a
 * synthetic EquipmentBonus; it is NOT an invented per-item bank snapshot.
 * The displayed DPS numbers are reference context only, not a scoring
 * ingredient: screenshots may use different targets and combat conditions.
 *
 * Pure research only. Nothing here changes live UI or Wave 1 weights.
 */
public class GearScoreV2FullLoadoutScreenshotTest
{
    private static EquipmentBonus total(
        int stab, int slash, int crush, int magic, int ranged,
        int defenceStab, int defenceSlash, int defenceCrush,
        int defenceMagic, int defenceRanged,
        int meleeStrength, int rangedStrength, String magicDamagePercent,
        int prayer)
    {
        return new EquipmentBonus(EquipmentSlot.WEAPON, false,
            stab, slash, crush, magic, ranged,
            defenceStab, defenceSlash, defenceCrush,
            defenceMagic, defenceRanged,
            meleeStrength, rangedStrength, new BigDecimal(magicDamagePercent), prayer);
    }

    private static final EquipmentBonus RANGED_A = total(
        30, 27, 27, -4, 186,
        123, 112, 127, 96, 122,
        17, 106, "0", 10);

    private static final EquipmentBonus MAGIC = total(
        14, 14, 14, 119, -4,
        202, 188, 211, 134, 57,
        20, 0, "6", 5);

    private static final EquipmentBonus RANGED_B = total(
        30, 27, 27, -4, 100,
        126, 115, 130, 96, 122,
        17, 28, "0", 13);

    private static final EquipmentBonus MELEE = total(
        26, 107, 24, -5, -2,
        139, 139, 147, 93, 96,
        114, 0, "0", 8);

    private static void verify(String name, EquipmentBonus total,
        String expectedV1, String expectedV2)
    {
        assertEquals(name + " Wave 1", expectedV1,
            GearScoreCalculator.wave1().scoreBonus(total).toPlainString());
        assertEquals(name + " proposed v2", expectedV2,
            GearScoreV2Preview.itemDisplay(total).toPlainString());
    }

    @Test
    public void ownerFourCompleteEquipmentPanelsCalculateExactly()
    {
        verify("Ranged A (186 ranged attack)", RANGED_A, "212.6", "187.9");
        verify("Magic (119 magic attack)", MAGIC, "123.4", "135.3");
        verify("Ranged B (100 ranged attack)", RANGED_B, "128.4", "103.5");
        verify("Melee (107 slash attack)", MELEE, "194.4", "100.8");
    }

    @Test
    public void rangedStrengthIsNotNerfedLikeMeleeStrength()
    {
        EquipmentBonus onlyRangedStrength = total(
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 106, "0", 0);
        assertEquals("106.0",
            GearScoreV2Preview.itemDisplay(onlyRangedStrength).toPlainString());

        EquipmentBonus onlyMeleeStrength = total(
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            106, 0, "0", 0);
        assertEquals("31.8",
            GearScoreV2Preview.itemDisplay(onlyMeleeStrength).toPlainString());
    }

    @Test
    public void rawGearScoreIsNotDpsRankingAndMustNotPretendToBe()
    {
        // Owner screenshot: Ranged A 3.94704 DPS, Ranged B 8.56704 DPS.
        // v2 ranks A higher anyway. This is a demonstrated mismatch between
        // a generic stat-accumulation index and screenshot-specific DPS,
        // NOT evidence of a broken OSRS DPS calculator or comparable targets.
        assertTrue(GearScoreV2Preview.itemDisplay(RANGED_A)
            .compareTo(GearScoreV2Preview.itemDisplay(RANGED_B)) > 0);
        assertTrue(new BigDecimal("3.94704")
            .compareTo(new BigDecimal("8.56704")) < 0);
        assertTrue(new BigDecimal("1.51169")
            .compareTo(new BigDecimal("8.61173")) < 0);
    }
}
