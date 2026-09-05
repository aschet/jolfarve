// SPDX-FileCopyrightText: 2026 Thomas Ascher <thomas.ascher@gmx.at>
//
// SPDX-License-Identifier: MIT

package io.github.aschet.olfarve;

import java.util.Objects;

/**
 * An sRGB color, gamma encoded, with components nominally in {@code [0, 1]}.
 *
 * <p>Components outside that range are accepted (e.g. from a hand built instance) and are clamped
 * on demand by {@link #toRgb8()} and {@link #toHex()}.
 */
public final class SrgbColor {

  private final double r;
  private final double g;
  private final double b;

  /**
   * Creates a color from its gamma encoded components.
   *
   * @param r the red component
   * @param g the green component
   * @param b the blue component
   */
  public SrgbColor(double r, double g, double b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  /**
   * Returns the red component.
   *
   * @return the red component
   */
  public double getR() {
    return r;
  }

  /**
   * Returns the green component.
   *
   * @return the green component
   */
  public double getG() {
    return g;
  }

  /**
   * Returns the blue component.
   *
   * @return the blue component
   */
  public double getB() {
    return b;
  }

  /**
   * Returns the color quantized to 8 bits per channel, in {@code {r, g, b}} order.
   *
   * <p>Components are clamped into gamut first, so the result is a valid 8 bit triplet even for an
   * instance built by hand out of range.
   *
   * <pre>{@code
   * new SrgbColor(1.0, 0.5, 0.0).toRgb8(); // {255, 128, 0}
   * new SrgbColor(2.0, -1.0, 0.0).toRgb8(); // {255, 0, 0}
   * }</pre>
   *
   * @return the color as {@code {r, g, b}}, each component in {@code [0, 255]}
   */
  public int[] toRgb8() {
    return new int[] {to8Bit(r), to8Bit(g), to8Bit(b)};
  }

  /**
   * Returns the color as a {@code #rrggbb} string.
   *
   * <pre>{@code
   * new SrgbColor(1.0, 0.5, 0.0).toHex(); // "#ff8000"
   * new SrgbColor(2.0, -1.0, 0.0).toHex(); // "#ff0000"
   * }</pre>
   *
   * @return the color as a lowercase {@code #rrggbb} hex string
   */
  public String toHex() {
    int[] rgb8 = toRgb8();
    return String.format("#%02x%02x%02x", rgb8[0], rgb8[1], rgb8[2]);
  }

  private static int to8Bit(double component) {
    return (int) Math.min(255, Math.max(0, Math.round(component * 255.0)));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SrgbColor)) {
      return false;
    }
    SrgbColor other = (SrgbColor) obj;
    return Double.compare(r, other.r) == 0
        && Double.compare(g, other.g) == 0
        && Double.compare(b, other.b) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(r, g, b);
  }

  @Override
  public String toString() {
    return "SrgbColor{r=" + r + ", g=" + g + ", b=" + b + "}";
  }
}
