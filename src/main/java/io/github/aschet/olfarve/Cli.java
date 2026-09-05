// SPDX-FileCopyrightText: 2026 Thomas Ascher <thomas.ascher@gmx.at>
//
// SPDX-License-Identifier: MIT

package io.github.aschet.olfarve;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Command line interface for {@link Olfarve}. */
public final class Cli {

  private static final String PROG = "olfarve";
  private static final int EXIT_OK = 0;
  private static final int EXIT_USAGE_ERROR = 2;

  private Cli() {}

  /** Runs the command line interface and exits the process with its status. */
  public static void main(String[] args) {
    System.exit(run(args, System.out, System.err));
  }

  /**
   * Runs the command line interface.
   *
   * @param args the arguments to parse
   * @param out the stream to print results to
   * @param err the stream to print usage errors to
   * @return the process exit status
   */
  public static int run(String[] args, PrintStream out, PrintStream err) {
    String scale = "srm";
    double pathLengthCm = Olfarve.DEFAULT_PATH_LENGTH_CM;
    List<Double> colorValues = new ArrayList<>();

    int i = 0;
    while (i < args.length) {
      String arg = args[i];
      switch (arg) {
        case "-h":
        case "--help":
          printHelp(out);
          return EXIT_OK;
        case "-V":
        case "--version":
          out.println(PROG + " " + Olfarve.VERSION);
          return EXIT_OK;
        case "-s":
        case "--scale":
          if (i + 1 >= args.length) {
            return usageError(err, "argument " + arg + ": expected one argument");
          }
          scale = args[++i];
          if (!scale.equals("srm") && !scale.equals("ebc")) {
            return usageError(
                err, "argument -s/--scale: invalid choice: '" + scale + "' (choose from srm, ebc)");
          }
          break;
        case "-p":
        case "--path-length":
          if (i + 1 >= args.length) {
            return usageError(err, "argument " + arg + ": expected one argument");
          }
          Double pathLength = parseNonNegativeDouble(args[++i]);
          if (pathLength == null) {
            return usageError(err, "argument -p/--path-length: invalid value: '" + args[i] + "'");
          }
          pathLengthCm = pathLength;
          break;
        default:
          Double colorValue = parseNonNegativeDouble(arg);
          if (colorValue == null) {
            return usageError(err, "unrecognized argument: '" + arg + "'");
          }
          colorValues.add(colorValue);
          break;
      }
      i++;
    }

    if (colorValues.isEmpty()) {
      return usageError(err, "the following arguments are required: VALUE");
    }

    for (double colorValue : colorValues) {
      SrgbColor color =
          scale.equals("ebc")
              ? Olfarve.ebcToSrgb(colorValue, pathLengthCm)
              : Olfarve.srmToSrgb(colorValue, pathLengthCm);
      out.println(formatValue(colorValue) + "," + color.toHex());
    }
    return EXIT_OK;
  }

  /**
   * Parses {@code text} as a non-negative, finite double.
   *
   * <p>A leading {@code -} does not automatically mean an option: like argparse's negative-number
   * heuristic, a token that parses as a number is treated as a value even if it starts with a dash,
   * so it can be rejected below as negative rather than as an unknown option.
   */
  private static Double parseNonNegativeDouble(String text) {
    double value;
    try {
      value = Double.parseDouble(text);
    } catch (NumberFormatException e) {
      return null;
    }
    if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0) {
      return null;
    }
    return value;
  }

  /** Formats a color value compactly, without a trailing {@code .0} for whole numbers. */
  private static String formatValue(double value) {
    if (value == Math.rint(value) && !Double.isInfinite(value)) {
      return String.format(Locale.ROOT, "%.0f", value);
    }
    String text = Double.toString(value);
    return text;
  }

  private static int usageError(PrintStream err, String message) {
    err.println("usage: " + PROG + " [-h] [-s {srm,ebc}] [-p CM] [-V] VALUE [VALUE ...]");
    err.println(PROG + ": error: " + message);
    return EXIT_USAGE_ERROR;
  }

  private static void printHelp(PrintStream out) {
    out.println("usage: " + PROG + " [-h] [-s {srm,ebc}] [-p CM] [-V] VALUE [VALUE ...]");
    out.println();
    out.println("Render SRM/EBC beer color values as sRGB colors.");
    out.println();
    out.println("positional arguments:");
    out.println("  VALUE                 one or more color values to convert");
    out.println();
    out.println("options:");
    out.println("  -h, --help            show this help message and exit");
    out.println("  -s, --scale {srm,ebc}");
    out.println("                        color scale of the values (default: srm)");
    out.println(
        "  -p, --path-length CM  optical path length in cm (default: "
            + formatValue(Olfarve.DEFAULT_PATH_LENGTH_CM)
            + ")");
    out.println("  -V, --version         show program's version number and exit");
  }
}
