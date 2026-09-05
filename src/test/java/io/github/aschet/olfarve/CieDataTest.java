// SPDX-FileCopyrightText: 2026 Thomas Ascher <thomas.ascher@gmx.at>
//
// SPDX-License-Identifier: MIT

package io.github.aschet.olfarve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Sanity checks for {@link CieData}, mirroring pyolfarve's {@code test_cie_table_shape}. */
class CieDataTest {

  @Test
  void tableHasOneRowPerFiveNanometerStepFrom380To780() {
    assertEquals(81, CieData.SAMPLES.length);
  }

  @Test
  void everyRowHasFourNonNegativeComponents() {
    for (double[] row : CieData.SAMPLES) {
      assertEquals(4, row.length);
      for (double component : row) {
        assertTrue(component >= 0.0);
      }
    }
  }
}
