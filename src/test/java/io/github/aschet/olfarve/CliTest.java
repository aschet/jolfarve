// SPDX-FileCopyrightText: 2026 Thomas Ascher <thomas.ascher@gmx.at>
//
// SPDX-License-Identifier: MIT

package io.github.aschet.olfarve;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Tests for {@link Cli}, mirroring pyolfarve's {@code tests/test_cli.py}. */
class CliTest {

  private static final class Result {
    final int status;
    final String[] outLines;
    final String[] errLines;

    Result(int status, String out, String err) {
      this.status = status;
      this.outLines = out.isEmpty() ? new String[0] : out.split("\\R");
      this.errLines = err.isEmpty() ? new String[0] : err.split("\\R");
    }
  }

  private static Result run(String... args) {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
    int status =
        Cli.run(
            args,
            new PrintStream(outBytes, true, StandardCharsets.UTF_8),
            new PrintStream(errBytes, true, StandardCharsets.UTF_8));
    return new Result(
        status,
        outBytes.toString(StandardCharsets.UTF_8),
        errBytes.toString(StandardCharsets.UTF_8));
  }

  @Test
  void noColorValuesIsAUsageError() {
    Result result = run();
    assertEquals(2, result.status);
    assertTrue(result.errLines.length > 0);
  }

  @Test
  void explicitColorValues() {
    Result result = run("4", "10");
    assertArrayEquals(new String[] {"4,#e7aa31", "10,#ba5b00"}, result.outLines);
  }

  @Test
  void ebcScale() {
    Result result = run("--scale", "ebc", "20");
    assertArrayEquals(new String[] {"20," + Olfarve.ebcToSrgb(20).toHex()}, result.outLines);
  }

  @Test
  void pathLengthOption() {
    Result result = run("--path-length", "1.0", "10");
    assertArrayEquals(new String[] {"10," + Olfarve.srmToSrgb(10, 1.0).toHex()}, result.outLines);
  }

  @Test
  void fractionalValuesAreFormattedCompactly() {
    Result result = run("3.5");
    assertTrue(result.outLines[0].startsWith("3.5,#"));
  }

  @Test
  void shortOptions() {
    Result withShort = run("-s", "ebc", "-p", "1", "20");
    Result withLong = run("--scale", "ebc", "--path-length", "1", "20");
    assertArrayEquals(withLong.outLines, withShort.outLines);
  }

  @Test
  void invalidInputExitsWithError() {
    assertEquals(2, run("-1").status);
    assertEquals(2, run("--path-length", "-1", "10").status);
    assertEquals(2, run("abc").status);
    assertEquals(2, run().status);
  }

  @Test
  void version() {
    Result result = run("--version");
    assertEquals(0, result.status);
    assertTrue(result.outLines[0].contains(Olfarve.VERSION));
  }
}
