// SPDX-FileCopyrightText: 2026 Thomas Ascher <thomas.ascher@gmx.at>
//
// SPDX-License-Identifier: MIT

package io.github.aschet.olfarve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link BeerColor}, mirroring pyolfarve's {@code tests/test_color.py}. */
class BeerColorTest {

  private static final Map<Integer, String> SRM_REFERENCE = new TreeMap<>();

  static {
    SRM_REFERENCE.put(1, "#fae8b6");
    SRM_REFERENCE.put(2, "#f4d180");
    SRM_REFERENCE.put(4, "#e7aa31");
    SRM_REFERENCE.put(10, "#ba5b00");
    SRM_REFERENCE.put(20, "#7d1900");
    SRM_REFERENCE.put(30, "#540000");
    SRM_REFERENCE.put(40, "#390000");
    SRM_REFERENCE.put(50, "#270000");
  }

  private static java.util.stream.Stream<Map.Entry<Integer, String>> srmReference() {
    return SRM_REFERENCE.entrySet().stream();
  }

  @Test
  void absorptionToSrgbReferenceValue() {
    assertEquals("#ba5b00", BeerColor.absorptionToSrgb(10.0 / 12.7).toHex());
  }

  @Test
  void normalizationFactorScalesWhiteToOne() {
    SrgbColor color = BeerColor.absorptionToSrgb(0.0);
    assertEquals(1.0, color.getR(), 1e-4);
    assertEquals(1.0, color.getG(), 1e-4);
    assertEquals(1.0, color.getB(), 1e-4);
    assertEquals("#ffffff", color.toHex());
  }

  @ParameterizedTest
  @MethodSource("srmReference")
  void srmReferenceColors(Map.Entry<Integer, String> entry) {
    assertEquals(entry.getValue(), BeerColor.srmToSrgb(entry.getKey()).toHex());
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 5, 10, 25, 40})
  void ebcMatchesEquivalentSrm(int srm) {
    double ebc = srm * 25.0 / 12.7;
    SrgbColor fromEbc = BeerColor.ebcToSrgb(ebc);
    SrgbColor fromSrm = BeerColor.srmToSrgb(srm);
    assertEquals(fromSrm.getR(), fromEbc.getR(), 1e-9);
    assertEquals(fromSrm.getG(), fromEbc.getG(), 1e-9);
    assertEquals(fromSrm.getB(), fromEbc.getB(), 1e-9);
  }

  @Test
  void componentsAreWithinUnitRange() {
    for (int srm = 0; srm <= 60; srm++) {
      SrgbColor color = BeerColor.srmToSrgb(srm);
      for (double component : new double[] {color.getR(), color.getG(), color.getB()}) {
        assertTrue(component >= 0.0 && component <= 1.0);
      }
    }
  }

  @Test
  void colorDarkensMonotonicallyWithColorValue() {
    double previous = Double.POSITIVE_INFINITY;
    for (int srm = 0; srm <= 40; srm++) {
      SrgbColor color = BeerColor.srmToSrgb(srm);
      double luminance = color.getR() + color.getG() + color.getB();
      assertTrue(luminance < previous);
      previous = luminance;
    }
  }

  @Test
  void longerPathLengthDarkensColor() {
    SrgbColor shortPath = BeerColor.srmToSrgb(10, 1.0);
    SrgbColor longPath = BeerColor.srmToSrgb(10, 10.0);
    double shortLuminance = shortPath.getR() + shortPath.getG() + shortPath.getB();
    double longLuminance = longPath.getR() + longPath.getG() + longPath.getB();
    assertTrue(longLuminance < shortLuminance);
  }

  @Test
  void zeroPathLengthIsWhite() {
    assertEquals("#ffffff", BeerColor.srmToSrgb(20, 0.0).toHex());
  }

  @Test
  void defaultPathLengthMatchesBjcpGlassWidth() {
    assertEquals(5.0, BeerColor.DEFAULT_PATH_LENGTH_CM);
    assertEquals(
        BeerColor.srmToSrgb(10), BeerColor.srmToSrgb(10, BeerColor.DEFAULT_PATH_LENGTH_CM));
  }

  @Test
  void negativeAbsorptionRaises() {
    assertThrows(IllegalArgumentException.class, () -> BeerColor.absorptionToSrgb(-0.1));
  }

  @Test
  void negativePathLengthRaises() {
    assertThrows(IllegalArgumentException.class, () -> BeerColor.absorptionToSrgb(1.0, -1.0));
  }

  @Test
  void negativeSrmRaises() {
    assertThrows(IllegalArgumentException.class, () -> BeerColor.srmToSrgb(-1));
  }

  @Test
  void negativeSrmPathLengthRaises() {
    assertThrows(IllegalArgumentException.class, () -> BeerColor.srmToSrgb(1, -1.0));
  }

  @Test
  void negativeEbcRaises() {
    assertThrows(IllegalArgumentException.class, () -> BeerColor.ebcToSrgb(-1));
  }

  @Test
  void negativeEbcPathLengthRaises() {
    assertThrows(IllegalArgumentException.class, () -> BeerColor.ebcToSrgb(1, -1.0));
  }
}
