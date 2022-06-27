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

- [x] Get JavaMOP agent to print mapping from affected types to specs
- [ ] We want a mapping from **affected specs** to **affected classes**
- [ ] First step is to parse the string to mapping, and then use API to replace it

## Side Tasks

- [x] Set up continuous integration, eventually using it in upstream
- [ ] Finish writing integration test
- [x] Add checkstyle (use STARTS config)
- [ ] Figure out how to get verbose log with Surefire > 2.16

### Impacted Classes file in `.starts/`

- [ ] Replace . with /
- [ ] Remove $num
- [ ] Append .java suffix
- [ ] ajc append classpath  for both two & all

### Notes

Instrument specs in all classes to specs in a subset (affected) for both two and all
What is Java Agent? It's something attached to the JVM
Start with bash script, then convert to Java

## TODO

- [ ] Read AspectJ compile-time weaving
- [ ] Given a bunch of .aj files, try weave it with two & all
- [ ] Change commons-fileupload 
- [ ] See affected classes
- [ ] Run ajc with two & all for affected files
