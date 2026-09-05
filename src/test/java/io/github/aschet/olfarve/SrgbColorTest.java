// SPDX-FileCopyrightText: 2026 Thomas Ascher <thomas.ascher@gmx.at>
//
// SPDX-License-Identifier: MIT

package io.github.aschet.olfarve;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link SrgbColor}, mirroring pyolfarve's {@code TestSRGBColor}/{@code TestHexOutput}.
 */
class SrgbColorTest {

  @Test
  void toHexEndpoints() {
    assertEquals("#ffffff", new SrgbColor(1.0, 1.0, 1.0).toHex());
    assertEquals("#000000", new SrgbColor(0.0, 0.0, 0.0).toHex());
    assertEquals("#ff0000", new SrgbColor(1.0, 0.0, 0.0).toHex());
    assertEquals("#ff8000", new SrgbColor(1.0, 0.5, 0.0).toHex());
  }

  @Test
  void toRgb8() {
    assertArrayEquals(new int[] {255, 128, 0}, new SrgbColor(1.0, 0.5, 0.0).toRgb8());
  }

  @Test
  void hexOutputIsLowercaseAndPadded() {
    String text = new SrgbColor(0.04, 0.04, 0.04).toHex();
    assertEquals(text.toLowerCase(java.util.Locale.ROOT), text);
    assertEquals(7, text.length());
  }

  @Test
  void hexAgreesWithRgb8() {
    for (int srm = 0; srm <= 60; srm++) {
      SrgbColor color = Olfarve.srmToSrgb(srm);
      int[] rgb8 = color.toRgb8();
      String expected = String.format("#%02x%02x%02x", rgb8[0], rgb8[1], rgb8[2]);
      assertEquals(expected, color.toHex());
    }
  }

  @ParameterizedTest
  @CsvSource({"2.0,0.0,0.0,#ff0000", "-1.0,-1.0,-1.0,#000000", "1.5,0.0,0.0,#ff0000"})
  void outOfGamutComponentsAreClamped(double r, double g, double b, String expected) {
    SrgbColor color = new SrgbColor(r, g, b);
    assertEquals(7, color.toHex().length());
    for (int component : color.toRgb8()) {
      assertTrue(component >= 0 && component <= 255);
    }
    assertEquals(expected, color.toHex());
  }

  @Test
  void equalsAndHashCodeFollowComponentEquality() {
    SrgbColor a = new SrgbColor(0.1, 0.2, 0.3);
    SrgbColor b = new SrgbColor(0.1, 0.2, 0.3);
    SrgbColor different = new SrgbColor(0.1, 0.2, 0.4);

    assertEquals(a, a);
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, different);
    assertFalse(a.equals(null));
    assertFalse(a.equals("not a color"));
  }

  @Test
  void toStringContainsComponents() {
    String text = new SrgbColor(0.1, 0.2, 0.3).toString();
    assertTrue(text.contains("0.1"));
    assertTrue(text.contains("0.2"));
    assertTrue(text.contains("0.3"));
  }
}
