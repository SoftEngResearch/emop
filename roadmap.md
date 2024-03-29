# Major Stages

- [x] Needs to call `STARTS` to find affected cases.
  - [x] Needs to output to file just like `STARTS`.
  - [x] In addition, store that information in memory, as a mapping.
- [x] Using information for affected classes, find affected specs to instrument for each affected class.
  - [x] A mapping from type to spec should be maintained.
- [x] Given affected classes, turn off unaffected specs for these with JavaMOP Agent.
- [x] Monitor affected ones only.

## Stage 1

- [x] How to use STARTS API
- [x] `emop:impacted` (our impacted acts like the STARTS) (internally invoke STARTS)
- [x] Use STARTS as a Dependency
- [x] Extend STARTS
- [x] Store impacted classes in memory and in file

## Stage 2

- [x] Get AspectJ to print mapping from affected types to specs
- [x] We want a mapping from **affected specs** to **affected classes**
- [x] First step is to parse the string to mapping
- [ ] Use AspectJ API to obtain the mapping directly

## Push for the paper

- [ ] Need these Mojos
  - [ ] RPS
  - [ ] VMS
  - [ ] RPP
- [ ] Need these data (50 SHAs each project for 50 projects)
  - [ ] RPS
  - [ ] VMS
  - [ ] RPP
  - [ ] Power set of the three
  - [ ] Combine RTS with three techniques
- [ ] Inspect these for data
  - [ ] Are they correct?
  - [ ] Do they look good?
- [ ] Things not available in 2019 (novelty)
  - [ ] Maybe MMMP?
  - [ ] New strategies for RPP
  - [ ] Different methods of doing diff for VMS
  
## Side Tasks

- [x] Set up continuous integration, eventually using it in upstream
- [ ] Finish writing integration test
- [x] Add checkstyle (use STARTS config)
- [ ] Figure out how to get verbose log with Surefire > 2.16

### Notes

Instrument specs in all classes to specs in a subset (affected) for both two and all
What is Java Agent? It's something attached to the JVM
Start with bash script, then convert to Java

## TODO

- [x] Read AspectJ compile-time weaving
- [x] Given a bunch of .aj files, try weave it with two & all
- [x] Change commons-fileupload 
- [x] See affected classes
- [x] Run ajc with two & all for affected files
