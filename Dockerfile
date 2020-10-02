FROM adoptopenjdk/openjdk11:alpine
LABEL "repository"="https://github.com/joakime/github-branch-release-drafter"
LABEL "homepage"="https://github.com/joakime/github-branch-release-drafter"
LABEL "maintainer"="Joakim Erdfelt"
LABEL "com.github.actions.name"="Github Branch Release Drafter"
LABEL "com.github.actions.description"="Drafts your next release notes from pull requests as they are merged into relevant branches."
LABEL "com.github.actions.icon"="droplet"
LABEL "com.github.actions.color"="salmon"
WORKDIR /app
# Copy build script and version of github-branch-release-drafter that docker should look for / use
COPY version.txt build.sh mvnw pom.xml header-template-java.txt /app/build/
# Copy Build Tool Wrapper
COPY .mvn/wrapper/* /app/build/.mvn/wrapper/
# Copy Source
COPY src /app/build/src/
RUN /app/build/build.sh
COPY entrypoint.sh /entrypoint.sh
# ENTRYPOINT ["/entrypoint.sh"]
