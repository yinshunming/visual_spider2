# Visual Selection Threshold Spec

> **Note**: This specs file documents the visual selection threshold configuration.
> As of current change, thresholds are hardcoded in `PlaywrightService.inspectSelectableElements()`.
> Future work may parameterize these values.

## ADDED Requirements

### Requirement: Visual selection element threshold defaults

The system SHALL use the following default thresholds when filtering selectable elements:

| Threshold | Default Value | Description |
|-----------|---------------|-------------|
| `widthPercent >` | 0.2 | Minimum width as percentage of document |
| `heightPercent >` | 0.08 | Minimum height as percentage of document |
| `area <=` | 400000 | Maximum element area in pixels |
| `div text.length <=` | 200 | Maximum text length for div elements |
| `div childCount <=` | 12 | Maximum child count for div elements |

#### Scenario: Element width threshold
- **WHEN** an element has `widthPercent <= 0.2`
- **THEN** the element SHALL be excluded from selectable elements

#### Scenario: Element height threshold
- **WHEN** an element has `heightPercent <= 0.08`
- **THEN** the element SHALL be excluded from selectable elements

#### Scenario: Element area threshold
- **WHEN** an element has `area > 400000`
- **THEN** the element SHALL be excluded from selectable elements

#### Scenario: Div element text length
- **WHEN** a div element has `text.length > 200`
- **THEN** the element SHALL be excluded from selectable elements

#### Scenario: Div element child count
- **WHEN** a div element has `childCount > 12`
- **THEN** the element SHALL be excluded from selectable elements

## MODIFIED Requirements

### Requirement: Element tag whitelist
**FROM**: `['h1','h2','h3','h4','p','span','a','time','div','article','section','li']`
**TO**: (unchanged - current implementation is sufficient)

## NOTES

- These thresholds are currently hardcoded in `PlaywrightService.java`
- Future enhancement: Move thresholds to configuration file (`application.yml`)
- Future enhancement: Per-site threshold profiles for different website structures
