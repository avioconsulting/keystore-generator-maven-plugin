#!/bin/sh
set -e

echo "Pushing to general AVIO Nexus"
./gradlew clean uploadArchives

echo "Now updating customer code"
git checkout customer_branch_name
git rebase master
git push --force customer_remote_name customer_branch_name:master
