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

## Contributing

We appreciate all types of contributions. See the [contributing guide](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md) for detailed instructions on how to get started.

## Licensing

This is a Moderne proprietary module available only for use by Moderne customers under the terms of a commercial contract.

For more information about licensing, please contact [Moderne](https://www.moderne.io/).
