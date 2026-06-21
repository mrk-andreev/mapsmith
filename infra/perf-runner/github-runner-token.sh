#!/usr/bin/env bash
set -euo pipefail

# The external provider supplies the JSON query on stdin.
repository="$(jq -r '.repository' < /dev/stdin)"

if [[ -z "$repository" || "$repository" == "null" ]]; then
  echo "repository is required" >&2
  exit 1
fi

# gh must already be authenticated to this repository with Actions administration access.
token="$(gh api --method POST "repos/${repository}/actions/runners/registration-token" --jq '.token')"
printf '{"token":"%s"}\n' "$token"
