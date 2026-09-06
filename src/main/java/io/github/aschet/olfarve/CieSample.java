// SPDX-FileCopyrightText: 2026 Thomas Ascher <thomas.ascher@gmx.at>
//
// SPDX-License-Identifier: MIT

package io.github.aschet.olfarve;

/**
 * One wavelength sample of the CIE 1931 observer and the D65 illuminant.
 *
 * <p>Field names follow CIE notation: {@code xBar}, {@code yBar} and {@code zBar} are the color
 * matching functions x(lambda), y(lambda) and z(lambda); {@code sD65} is the relative spectral
 * power distribution S(lambda) of illuminant D65.
 */
final class CieSample {

  final double xBar;
  final double yBar;
  final double zBar;
  final double sD65;

  CieSample(double xBar, double yBar, double zBar, double sD65) {
    this.xBar = xBar;
    this.yBar = yBar;
    this.zBar = zBar;
    this.sD65 = sD65;
  }
}
