<p align="center">
  <a href="https://docs.openrewrite.org">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-dark.svg">
      <source media="(prefers-color-scheme: light)" srcset="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-light.svg">
      <img alt="OpenRewrite Logo" src="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-light.svg" width='600px'>
    </picture>
  </a>
</p>

<div align="center">
  <h1>rewrite-cobol</h1>
</div>

<div align="center">

<!-- Keep the gap above this line, otherwise they won't render correctly! -->
[![ci](https://github.com/moderneinc/rewrite-cobol/actions/workflows/ci.yml/badge.svg)](https://github.com/moderneinc/rewrite-cobol/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.openrewrite/rewrite-cobol.svg)](https://mvnrepository.com/artifact/org.openrewrite/rewrite-cobol)
</div>

## What is this?

This project implements a [Rewrite module](https://github.com/openrewrite/rewrite) that provides parsers, visitors, and recipes for COBOL and related mainframe technologies. It supports parsing and transforming COBOL source code, JCL (Job Control Language), and Control-M job definitions.

### Language Support

- **COBOL** — Full parsing of COBOL-85 (IBM ANSI 85 and HP Tandem dialects), including preprocessor directives (COPY, REPLACE) and copybook resolution
- **JCL** — Job Control Language parsing (`.jcl`, `.prc` files)
- **Control-M** — Control-M job scheduling definition parsing

### Recipes

- **Search** — Find copybooks, words, indicators, references, and relationships across COBOL programs
- **Cleanup** — Remove debugging mode from SOURCE-COMPUTER paragraphs
- **Format** — Remove words and shift sequence areas

## Tested against

Unit tests cover the constructs one at a time, and the NIST COBOL-85 conformance suite in
`src/test/resources/gov/nist` covers the standard. Neither contains CICS, IMS, DB2 or JCL, so the
parser is also measured against real mainframe applications — these three, which are public:

| Application | Repository | Programs | Jobs | CICS | Embedded SQL | BMS maps | DL/I |
|---|---|---:|---:|---:|---:|---:|---:|
| CardDemo | [aws-samples/aws-mainframe-modernization-carddemo](https://github.com/aws-samples/aws-mainframe-modernization-carddemo) | 44 | 55 | 25 | 4 | 7 | 7 |
| GenApp | [cicsdev/cics-genapp](https://github.com/cicsdev/cics-genapp) | 31 | 29 | 31 | 8 | 5 | — |
| Bank of Z | [IBM/Bank-of-Z](https://github.com/IBM/Bank-of-Z) | 43 | 1 | 31 | 15 | 9 | 11 |

Counted by file: 118 COBOL programs and 85 jobs in all, with the columns saying how many programs
use each technology. Between them they cover online and batch, both database managers, screen
handling, and the copybook and `EXEC SQL INCLUDE` idioms that make a program unreadable without its
library.

Clone them side by side into one directory and point the tests at it. The corpus tests skip
themselves when the variables are unset, so a normal `./gradlew test` does not need them:

```bash
COBOL_CORPUS=/path/to/corpus JCL_CORPUS=/path/to/corpus ./gradlew test
```

`CorpusCoverageTest` reports how much of the corpus parses rather than requiring all of it to: what
the parser cannot yet read is the point of the measurement. It also asserts that every program it did
parse prints back byte-identical to the input, which is the property recipes depend on.

## Contributing

We appreciate all types of contributions. See the [contributing guide](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md) for detailed instructions on how to get started.

## Licensing

This is a Moderne proprietary module available only for use by Moderne customers under the terms of a commercial contract.

For more information about licensing, please contact [Moderne](https://www.moderne.io/).
