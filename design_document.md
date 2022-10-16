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

Input: (optional) list of critical specs, (optional) list of background specs. If neither are provided, then we have to either (1) autocompute a list of critical specs (if we have a `violation-counts` file to refer to), or (2) run all specs as either critical/background.
Output: standard out/err of test execution for critical phase, violation-counts for critical phase, file containing test execution for background phase, violation-counts for background phase. Bonus if we can provide a file listing the violated specs, so we can reuse it for the next run of RPP.

### Autocomputing the list of critical specs
A first pass implementation can assume that a list of critical specs and/or a list of background specs is provided. This part is more simple, and we can definitely put it in later.
- [ ] Can we get `violation-counts` automatically stored in the `.starts` directory?
- [ ] Need to parse a `violation-counts` file to retrieve the list of specs that were previously violated
  - [ ] Any spec that does not belong in this set is brought to the background phase.

### Executing the critical and background phases
Note that for the first pass implementation, we will run the critical and background phases sequentially. Later, we can work out running the two phases in parallel.
- [ ] Create two versions of the JavaMOP agent. Replace the `aop-ajc.xml` file for both JavaMOP agents accordingly (one containing the critical specs, one containing the background specs).
- [ ] Get a handle on maven surefire (follow how to do this in STARTS/DSI)
- [ ] The standard out and standard error of the background phase needs to be redirected to a file.
- [ ] Need to relocate `violation-counts`.
- [ ] Nice to have: if either critical or background has 0 specs, then we want to skip it.
