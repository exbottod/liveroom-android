# CI

`android-ci.yml` is the GitHub Actions workflow for this repo. It builds the debug APK on
every push to `main` and uploads it as an artifact.

It sits here because the GitHub connection used by the agent has the `repo`, `read:user`
and `read:org` scopes but not `workflow`, and GitHub rejects any write under
`.github/workflows/` without it. Two ways to activate it:

1. In GitHub web: open `ci/android-ci.yml`, copy the content, then Add file -> Create new
   file, name it `.github/workflows/android-ci.yml`, paste, commit.
2. Reconnect GitHub with the `workflow` scope, then the agent can move it into place.
