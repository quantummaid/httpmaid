set -eu
export DeveloperModeEnabled=true
export MAVEN_OPTS="-Xms256m -Xmx256m"
mvn clean package -DskipTests=true
