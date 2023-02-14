PROJECT=skiff-api-plane-23
COMMIT="$(git rev-parse HEAD)"
SB=$CI_MERGE_REQUEST_SOURCE_BRANCH_NAME
TB=$CI_MERGE_REQUEST_TARGET_BRANCH_NAME
git checkout $SB
git checkout $TB
echo Branches are: $SB/$TB >&2
SC=$(git log -n 1 --pretty=format:"%H" $SB --)
TC=$(git log -n 1 --pretty=format:"%H" $TB --)
echo Commits are $SC/$TC >&2
MERGE_BASE=$(git merge-base $SC $TC)
echo MergeBase: $MERGE_BASE >&2
CMTS="$(git log $TC...$SC --format="%H %s")"
INVALID_COMMITS="$(echo "$CMTS" | grep -vE '^[0-9a-f]+ ((Requirement|Task|Bug|Ticket|Feedback|Feature|Subtask)-[0-9]+: |Merge (remote-tracking )?(branch|merge)|(M|m)erge: merge)')"
echo MR labels are: "$CI_MERGE_REQUEST_LABELS"
if [[ ",$CI_MERGE_REQUEST_LABELS," =~ [.*,Misc,.*] ]]; then
  INVALID_COMMITS="$(echo "$INVALID_COMMITS" | grep -vE '^[0-9a-f]+ Misc: ')"
fi
if [[ $INVALID_COMMITS != "" ]]; then
  echo "Invalid commits:"
  echo "$INVALID_COMMITS"
  exit 1
fi
exit 0

wget -O yq https://github.com/mikefarah/yq/releases/download/v4.30.6/yq_linux_amd64
chmod +x yq

echo Sonar link: "https://sonar-hy.netease.com/project/issues?id=$PROJECT&pullRequest=$COMMIT"
TIMES=15
for i in `seq 1 $TIMES`; do
  RESP="$(curl -u 4c70c4b1e65ba13757239d6fea4cd5e548ad3e22: "https://sonar-hy.netease.com/api/measures/component?pullRequest=$COMMIT&component=$PROJECT&metricKeys=new_blocker_violations,new_critical_violations,new_major_violations")"
  STATS="$(echo "$RESP" | ./yq -P '.component.measures[]|{.metric: .period.value}')"
  echo "$RESP"
  echo "$STATS"

  BLOCKER=$(echo "$STATS" | ./yq '.new_blocker_violations')
  CRITICAL=$(echo "$STATS" | ./yq '.new_critical_violations')

  if [[ $BLOCKER != 0 || $CRITICAL != 0 ]]; then
    if echo "$RESP" | ./yq -P '.errors[0].msg' | grep -q "not found"; then
      echo sonar result not found, retrying...
      sleep 60
    else
      echo ERROR: There is new issue.
      exit 1
    fi
  else
    exit 0
  fi
done

echo sonar result not found, after $TIMES tries...
exit 1
