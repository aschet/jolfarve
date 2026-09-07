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

The library has no runtime dependencies and targets Java 8 bytecode, so it
works as a plain dependency in desktop, server, and Android projects alike
(Android's Gradle build resolves `mavenCentral()` by default; no separate AAR
is needed).

## Usage

```java
import io.github.aschet.olfarve.BeerColor;
import io.github.aschet.olfarve.SrgbColor;

BeerColor.srmToSrgb(10).toHex();
BeerColor.ebcToSrgb(20).toHex();

// The default path length is 5 cm, the width of a typical sample glass
BeerColor.srmToSrgb(10, 1.0).toHex();

// Results are SrgbColor values with gamma encoded components in [0, 1]
SrgbColor color = BeerColor.srmToSrgb(10);
color.getR();
color.getG();
color.getB();
color.toRgb8();

// Or start from an absorbance measured at 430 nm
BeerColor.absorptionToSrgb(0.7874);
```

## Development

```bash
mvn test
mvn spotless:check   # mvn spotless:apply to fix formatting
mvn package
```
