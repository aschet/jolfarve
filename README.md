# jolfarve

[![Maven Central](https://img.shields.io/maven-central/v/io.github.aschet/olfarve)](https://central.sonatype.com/artifact/io.github.aschet/olfarve)

*Øl farve* ("beer color") renders SRM and EBC beer color values as sRGB
colors, following the spectral model described by A. J. de Lange, "Color," in
*Brewing Materials and Processes*, Elsevier, 2016, pp. 199-249.

Given a color value and an optical path length (the width of the glass the
beer is viewed through), the sample's spectral transmittance is derived from
its absorption coefficient at 430 nm via the Beer-Lambert law, integrated
against the CIE 1931 color matching functions of the 2 degree standard
colorimetric observer under illuminant D65, and the resulting XYZ tristimulus
values are transformed to sRGB.

This is a Java port of [pyolfarve](https://github.com/aschet/pyolfarve),
kept numerically and structurally aligned with it. It has no runtime
dependencies and targets Java 8 bytecode, so it works as a plain dependency
in desktop, server, and **Android** projects alike (Android's Gradle build
resolves `mavenCentral()` by default; no separate AAR is needed).

## Installation

Maven:

```xml
<dependency>
  <groupId>io.github.aschet</groupId>
  <artifactId>olfarve</artifactId>
  <version>1.0.0</version>
</dependency>
```

Gradle (including Android modules):

```kotlin
implementation("io.github.aschet:olfarve:1.0.0")
```

## Usage

```java
import io.github.aschet.olfarve.Olfarve;
import io.github.aschet.olfarve.SrgbColor;

Olfarve.srmToSrgb(10).toHex();
Olfarve.ebcToSrgb(20).toHex();

// The default path length is 5 cm, the width of a typical sample glass
Olfarve.srmToSrgb(10, 1.0).toHex();

// Results are SrgbColor values with gamma encoded components in [0, 1]
SrgbColor color = Olfarve.srmToSrgb(10);
color.getR();
color.getG();
color.getB();
color.toRgb8();

// Or start from an absorbance measured at 430 nm
Olfarve.absorptionToSrgb(0.7874);
```

`SrgbColor` deliberately has no conversion to `java.awt.Color` or any other
platform color type — `toRgb8()` already makes that a one-liner, and it keeps
this library free of a `java.desktop` dependency that Android doesn't have.

## Command line

The jar is directly executable and takes one or more color values, printing
them as CSV:

```bash
java -jar olfarve-1.0.0.jar 1 2 10
```

```
1,#fae8b6
2,#f4d180
10,#ba5b00
```

Pick a scale and a path length:

```bash
java -jar olfarve-1.0.0.jar --scale ebc --path-length 1.0 8 20 40
```

Run `java -jar olfarve-1.0.0.jar --help` for the full list of options.

## Development

This project builds with [Maven](https://maven.apache.org/).

```bash
mvn test
mvn spotless:check   # mvn spotless:apply to fix formatting
mvn package
```

## Publishing

Releases are published to Maven Central from a GitHub Release via
`.github/workflows/publish.yml`, using Sonatype's Central Portal. To cut a
release: bump the version in `pom.xml` and `Olfarve.VERSION` together, tag it
(`vX.Y.Z`), and publish a GitHub Release from that tag.
