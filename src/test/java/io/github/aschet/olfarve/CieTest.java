// SPDX-FileCopyrightText: 2026 Thomas Ascher <thomas.ascher@gmx.at>
//
// SPDX-License-Identifier: MIT

package io.github.aschet.olfarve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Sanity checks for {@link Cie}, mirroring pyolfarve's {@code test_cie_table_shape}. */
class CieTest {

  @Test
  void tableHasOneSamplePerFiveNanometerStepFrom380To780() {
    assertEquals(81, Cie.SAMPLES.length);
  }

  @Test
  void everySampleHasNonNegativeComponents() {
    for (CieSample sample : Cie.SAMPLES) {
      assertTrue(sample.xBar >= 0.0);
      assertTrue(sample.yBar >= 0.0);
      assertTrue(sample.zBar >= 0.0);
      assertTrue(sample.sD65 >= 0.0);
    }
  }
}
