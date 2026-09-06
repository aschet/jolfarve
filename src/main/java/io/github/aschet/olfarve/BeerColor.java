// SPDX-FileCopyrightText: 2026 Thomas Ascher <thomas.ascher@gmx.at>
//
// SPDX-License-Identifier: MIT

package io.github.aschet.olfarve;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * sRGB rendering of SRM and EBC beer color values.
 *
 * <p>The spectral model is A. J. de Lange, "Color," in <i>Brewing Materials and Processes</i>,
 * Elsevier, 2016, pp. 199-249: beer's transmittance across the visible range is approximated from
 * its absorption at 430 nm. Integrating that against the CIE 1931 color matching functions under
 * illuminant D65 gives XYZ tristimulus values, which are then transformed to sRGB.
 *
 * <p>The sRGB primaries, white point and gamma encoding follow <a
 * href="https://www.w3.org/Graphics/Color/srgb">w3.org</a>. The colorimetric data is documented in
 * {@link Cie}.
 */
public final class BeerColor {

  /**
   * The version of this library, read from a resource filtered at build time so {@code pom.xml}
   * stays the single source of truth.
   */
  public static final String VERSION = loadVersion();

  /**
   * Default optical path length in cm, set to the typical sample glass width specified by the <a
   * href="https://www.bjcp.org/education-training/education-resources/color-guide">BJCP color
   * guide</a>.
   */
  public static final double DEFAULT_PATH_LENGTH_CM = 5.0;

  // Both scales are defined as a multiple of the absorbance at 430 nm measured over a 1 cm path:
  // SRM = 12.7 * A430 and EBC = 25.0 * A430.
  private static final double SRM_PER_ABSORBANCE = 12.7;
  private static final double EBC_PER_ABSORBANCE = 25.0;

  // The de Lange approximation sums two exponentials decaying away from 430 nm, giving absorption
  // at any wavelength relative to the absorption there.
  private static final double REFERENCE_WAVELENGTH_NM = 430.0;
  private static final double SHORT_DECAY_WEIGHT = 0.02465;
  private static final double SHORT_DECAY_NM = 17.591;
  private static final double LONG_DECAY_WEIGHT = 0.97535;
  private static final double LONG_DECAY_NM = 82.122;

  // Piecewise sRGB gamma encoding: linear below the threshold, a power law above it.
  private static final double GAMMA_THRESHOLD = 0.0031308;
  private static final double GAMMA_SLOPE = 12.92;
  private static final double GAMMA_SCALE = 1.055;
  private static final double GAMMA_OFFSET = 0.055;
  private static final double GAMMA_EXPONENT = 1.0 / 2.4;

  private static final double K = calculateK();
  private static final SpectrumEntry[] SPECTRUM = buildSpectrum();

  private BeerColor() {}

  /**
   * Precomputed wavelength dependent terms of the integration.
   *
   * <p>Only the absorbance varies between conversions. The absorption ratios and the colorimetric
   * weights depend solely on wavelength, so they are evaluated once at class initialization rather
   * than on every call.
   */
  private static final class SpectrumEntry {
    final double absorptionRatio;
    final double sD65;
    final double xBar;
    final double yBar;
    final double zBar;

    SpectrumEntry(double absorptionRatio, double sD65, double xBar, double yBar, double zBar) {
      this.absorptionRatio = absorptionRatio;
      this.sD65 = sD65;
      this.xBar = xBar;
      this.yBar = yBar;
      this.zBar = zBar;
    }
  }

  /**
   * Reads {@code version.properties}, a resource filtered at build time with the {@code pom.xml}
   * version, so the two never drift apart.
   */
  private static String loadVersion() {
    Properties properties = new Properties();
    try (InputStream in = BeerColor.class.getResourceAsStream("version.properties")) {
      if (in != null) {
        properties.load(in);
      }
    } catch (IOException e) {
      // Fall through: getProperty below returns the "unknown" default.
    }
    return properties.getProperty("version", "unknown");
  }

  /**
   * Returns the normalizing constant for illuminant D65.
   *
   * <p>CIE defines {@code k = 100 / sum(S(lambda) * yBar(lambda))}, putting the luminance of a
   * perfectly transmitting sample at 100. Dropping the factor of 100 puts it at 1.0 instead, which
   * is the range sRGB expects.
   */
  private static double calculateK() {
    double luminance = 0.0;
    for (CieSample sample : Cie.SAMPLES) {
      luminance += sample.sD65 * sample.yBar;
    }
    return 1.0 / luminance;
  }

  /** Returns absorption at {@code wavelengthNm} relative to that at 430 nm. */
  private static double absorptionRatio(double wavelengthNm) {
    double offsetNm = wavelengthNm - REFERENCE_WAVELENGTH_NM;
    return SHORT_DECAY_WEIGHT * Math.exp(-offsetNm / SHORT_DECAY_NM)
        + LONG_DECAY_WEIGHT * Math.exp(-offsetNm / LONG_DECAY_NM);
  }

  private static SpectrumEntry[] buildSpectrum() {
    SpectrumEntry[] spectrum = new SpectrumEntry[Cie.SAMPLES.length];
    double wavelengthNm = Cie.FIRST_WAVELENGTH_NM;
    for (int i = 0; i < Cie.SAMPLES.length; i++) {
      CieSample sample = Cie.SAMPLES[i];
      spectrum[i] =
          new SpectrumEntry(
              absorptionRatio(wavelengthNm), sample.sD65, sample.xBar, sample.yBar, sample.zBar);
      wavelengthNm += Cie.WAVELENGTH_STEP_NM;
    }
    return spectrum;
  }

  /**
   * Gamma encodes one linear component, clamping it to {@code [0, 1]} first.
   *
   * <p>This is the inverse of the sRGB EOTF: it maps a linear tristimulus component to the
   * non-linear signal a display decodes.
   */
  private static double encodeGamma(double linear) {
    double clamped = Math.max(0.0, Math.min(1.0, linear));
    if (clamped <= GAMMA_THRESHOLD) {
      return clamped * GAMMA_SLOPE;
    }
    return GAMMA_SCALE * Math.pow(clamped, GAMMA_EXPONENT) - GAMMA_OFFSET;
  }

  /**
   * Converts a beer's absorption at 430 nm into an sRGB color, using {@link
   * #DEFAULT_PATH_LENGTH_CM} as the path length.
   *
   * @param absorption430 linear decadic absorption coefficient at 430 nm, in cm^-1
   * @return the gamma encoded color, with components in {@code [0, 1]}
   * @throws IllegalArgumentException if {@code absorption430} is negative
   */
  public static SrgbColor absorptionToSrgb(double absorption430) {
    return absorptionToSrgb(absorption430, DEFAULT_PATH_LENGTH_CM);
  }

  /**
   * Converts a beer's absorption at 430 nm into an sRGB color.
   *
   * <p>Prefer {@link #srmToSrgb} or {@link #ebcToSrgb} when you have a color value, which is what
   * brewing software reports. This method is for a photometer reading taken directly, where the
   * absorbance is the measurement and the SRM or EBC value is derived from it.
   *
   * @param absorption430 linear decadic absorption coefficient at 430 nm, in cm^-1. Numerically
   *     this is the ASBC/EBC absorbance A430, which is defined for a 1 cm path length.
   * @param pathLengthCm optical path length in cm, e.g. the glass width
   * @return the gamma encoded color, with components in {@code [0, 1]}
   * @throws IllegalArgumentException if either argument is negative
   */
  public static SrgbColor absorptionToSrgb(double absorption430, double pathLengthCm) {
    if (absorption430 < 0.0) {
      throw new IllegalArgumentException(
          "absorption430 must not be negative, got " + absorption430);
    }
    if (pathLengthCm < 0.0) {
      throw new IllegalArgumentException("pathLengthCm must not be negative, got " + pathLengthCm);
    }

    // Beer-Lambert law: absorbance A = a * l, and transmittance T = 10 ** -A.
    double absorbance430 = absorption430 * pathLengthCm;

    double tristimulusX = 0.0;
    double tristimulusY = 0.0;
    double tristimulusZ = 0.0;
    for (SpectrumEntry entry : SPECTRUM) {
      double transmittedPower = entry.sD65 * Math.pow(10.0, -absorbance430 * entry.absorptionRatio);
      tristimulusX += transmittedPower * entry.xBar;
      tristimulusY += transmittedPower * entry.yBar;
      tristimulusZ += transmittedPower * entry.zBar;
    }

    tristimulusX *= K;
    tristimulusY *= K;
    tristimulusZ *= K;

    // XYZ to linear sRGB, D65 white point.
    return new SrgbColor(
        encodeGamma(
            tristimulusX * 3.2406255 + tristimulusY * -1.537208 + tristimulusZ * -0.4986286),
        encodeGamma(
            tristimulusX * -0.9689307 + tristimulusY * 1.8757561 + tristimulusZ * 0.0415175),
        encodeGamma(
            tristimulusX * 0.0557101 + tristimulusY * -0.2040211 + tristimulusZ * 1.0569959));
  }

  /**
   * Converts a Standard Reference Method color value into an sRGB color, using {@link
   * #DEFAULT_PATH_LENGTH_CM} as the path length.
   *
   * @param srm the SRM color value
   * @return the gamma encoded color, with components in {@code [0, 1]}
   * @throws IllegalArgumentException if {@code srm} is negative
   */
  public static SrgbColor srmToSrgb(double srm) {
    return srmToSrgb(srm, DEFAULT_PATH_LENGTH_CM);
  }

  /**
   * Converts a Standard Reference Method color value into an sRGB color.
   *
   * @param srm the SRM color value
   * @param pathLengthCm optical path length in cm, e.g. the glass width
   * @return the gamma encoded color, with components in {@code [0, 1]}
   * @throws IllegalArgumentException if either argument is negative
   */
  public static SrgbColor srmToSrgb(double srm, double pathLengthCm) {
    return absorptionToSrgb(srm / SRM_PER_ABSORBANCE, pathLengthCm);
  }

  /**
   * Converts a European Brewery Convention color value into an sRGB color, using {@link
   * #DEFAULT_PATH_LENGTH_CM} as the path length.
   *
   * @param ebc the EBC color value
   * @return the gamma encoded color, with components in {@code [0, 1]}
   * @throws IllegalArgumentException if {@code ebc} is negative
   */
  public static SrgbColor ebcToSrgb(double ebc) {
    return ebcToSrgb(ebc, DEFAULT_PATH_LENGTH_CM);
  }

  /**
   * Converts a European Brewery Convention color value into an sRGB color.
   *
   * @param ebc the EBC color value
   * @param pathLengthCm optical path length in cm, e.g. the glass width
   * @return the gamma encoded color, with components in {@code [0, 1]}
   * @throws IllegalArgumentException if either argument is negative
   */
  public static SrgbColor ebcToSrgb(double ebc, double pathLengthCm) {
    return absorptionToSrgb(ebc / EBC_PER_ABSORBANCE, pathLengthCm);
  }
}
