# Design Document

## RPS

12 Variants of RPS

### "What" to instrument

Different notions of affected.

Solution: Utilize the functionality provided by `impacted-both-ways` branch in `STARTS`. Change the option `trackUsages` in `STARTS` `ImpactedMojo` to get different notions of affected classes.

However, `STARTS` need some works.

- [ ] $ps_1, \text{affected}_1(\Delta) = \Delta \circ (E^{-1})^* \circ E^*$
- [ ] $ps_2, \text{affected}_2(\Delta) = \Delta \circ ((E^{-1})^* \cup E^*)$
- [x] $ps_3, \text{affected}_3(\Delta) = \Delta \circ (E^{-1})^*$: What we currently have by defaulting `trackUsages` to `OPTION1` (need to be renamed)

Notes:

* $^*$: Reflexive and transitive closure
* $\circ$: Relational image
* $^{-1}$: Inverse relation

Paper section to refer to: "Techniques" paper p.5-6

Code section to refer to: `STARTS` `impacted-both-ways` branch, `getTransitiveClosurePerClass()` in `edu.illinois.starts.helpers.Loadables`

### "Where" to instrument

Whether to include libraries and non-affected classes:

- [ ] $ps_i$
- [ ] $ps_i^{c}$
- [x] $ps_i^{l}$: Currently implemented by `emop`, excluding libraries.
- [ ] $ps_i^{cl}$: Can be done through modifying the list of classes that `emop` can exclude during execution.

The implementation of $ps_i^{cl}$ can reference [this](https://github.com/thenewpyjiang/emop/commit/5ffd29ee744c8b728f315f113bbe0fe5126606c7), already in experimentation.

$ps_i$ and $ps_i^c$ needs to modify `STARTS` to include third-party libraries, at least that's my best idea.

## VMS

Don't have very good ideas yet.

Some primitive thoughts:

- [ ] Store messages as maps containing key information like where the violation was found
  - [ ] Problems: Don't have a reliable way of expressing the violation location. (Can't be line numbers because they are prone to change) Eventually might work with`ajc`.

## RPP

Ideas:

- [ ] Store from previous run statistics what generated most number of monitors. If it is configurable to specify to instrument which specs first, then instrument them according to that order.