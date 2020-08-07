#!/usr/bin/env bash

eval $(aws sts assume-role \
  --role-arn arn:aws:iam::712767472906:role/CodeBuildServiceRole-httpmaid \
  --role-session-name "$USER" |
    jq -r 'to_entries[0] | "
      export AWS_ACCESS_KEY_ID=\(.value.AccessKeyId)
      export AWS_SECRET_ACCESS_KEY=\(.value.SecretAccessKey)
      export AWS_SESSION_TOKEN=\(.value.SessionToken)"')

cat <<EOF
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
AWS_SESSION_TOKEN=$AWS_SESSION_TOKEN
EOF

