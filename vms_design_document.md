# Violation Message Suppression (VMS) Implementation

## Usage Scenario (for straight-line pass)
- We will deal with two git-committed versions of the project, specified via SHAs (there are other usage scenarios that one can think of, especially when considering the usage of eMOP in an IDE setting, but we will go with this usage scenario for our straight-line pass).

## Notes from Brainstorming Session
- We only need to checkout/see the previous version of files that had violations.
- Want to use Git API to do all of the operations wrt finding previous version... (might also be able to get the diffs via the Git API?)
