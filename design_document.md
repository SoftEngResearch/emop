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

- [ ] Store violations from $P_1$ and $P_2$
  - Violations should store the specification violated along with the file and line number where it occurred
- [ ] For every violation in $P_2$, look at the same violations in $P_1$
  - [ ] Use `diff` (via JGit, found [here](https://www.eclipse.org/jgit/download/)) to determine if these violations
        occured in the same location in the code
    - You can interpret this as being the same line number in code, after taking account all edits in a file
    - Make sure to use the option allowing for the analysis of renames
    - JGit was chosen for its robust [documentation](https://download.eclipse.org/jgit/site/6.3.0.202209071007-r/apidocs/org/eclipse/jgit/diff/package-summary.html) 
      and its continued maintenance
    - There were many options for analyzing differences such as JEdit (JDiff) and diff-map, but these alternatives
      lacked documentation and have not been maintained in several years
    - Might be able to optimize runtime by doing this on a per-file instead of a per-violation basis
    - Other relevant sites for how to do this ([here](https://www.codeaffine.com/2016/06/16/jgit-diff/))
  - [ ] If the previous condition is true, stop the violation from being reported in $P_2$
    - Otherwise, report the violation

Possible issues and things to think about
* If there is a version of `violation-counts` present, keep it in `violation-counts-old` and refer back to it for $P_1$
* MMMP have multiple `violation-counts`, VMS at the moment does not account for this
* How to deal with libraries in general, their lack of version history and if we want their violations or not
* Class + Spec + Line = the equation to determine if violations are the same or not
* Suggestions: implement this as its own independent variant (in its own module running mop) to begin with, integrate later
* We need options for showing different violations (show me what I saw before)
* First pass: evaluate only on common-filesupload
* If necessary, clone JGit and modify it

## RPP

Ideas:

- [ ] Store from previous run statistics what generated most number of monitors. If it is configurable to specify to instrument which specs first, then instrument them according to that order.