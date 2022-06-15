# Major Stages

- [ ] Needs to call `STARTS` to find affected cases.
  - [ ] Needs to output to file just like `STARTS`.
  - [ ] In addition, store that information in memory, as a mapping.
- [ ] Using information for affected classes, find affected specs to instrument for each affected class.
  - [ ] A mapping from type to spec should be maintained.
- [ ] Given affected classes, turn off unaffected specs for these with JavaMOP Agent.
- [ ] Monitor affected ones only.

## Stage 1

- [ ] How to use STARTS API
- [ ] `emop:impacted` (our impacted acts like the STARTS) (internally invoke STARTS)
- [ ] Use STARTS as a Dependency
- [ ] Extend STARTS
- [ ] Store impacted classes in memory and in file

### Specific Functionalities

- [ ] Something with project source code (types and tests) as **input**, and checksum for them as **output**.
- [ ] Something with project source code (types and tests) and previous checksum as **input**, changed classes and their associated impacted classes (types and tests) as **output**.

## Side Tasks

- [ ] Set up continuous integration, eventually using it in upstream
