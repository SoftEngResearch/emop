# Major Stages

- [x] Needs to call `STARTS` to find affected cases.
  - [x] Needs to output to file just like `STARTS`.
  - [x] In addition, store that information in memory, as a mapping.
- [ ] Using information for affected classes, find affected specs to instrument for each affected class.
  - [ ] A mapping from type to spec should be maintained.
- [ ] Given affected classes, turn off unaffected specs for these with JavaMOP Agent.
- [ ] Monitor affected ones only.

## Stage 1

- [x] How to use STARTS API
- [x] `emop:impacted` (our impacted acts like the STARTS) (internally invoke STARTS)
- [x] Use STARTS as a Dependency
- [x] Extend STARTS
- [x] Store impacted classes in memory and in file

## Stage 2

- [ ] Get JavaMOP agent to print mapping from affected types to specs

## Side Tasks

- [ ] Set up continuous integration, eventually using it in upstream
- [ ] Finish writing integration test
- [x] Add checkstyle (use STARTS config)
