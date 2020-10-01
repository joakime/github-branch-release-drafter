FROM adoptopenjdk/openjdk11:alpine
LABEL "repository"="https://github.com/joakime/github-branch-release-drafter"
LABEL "homepage"="https://github.com/joakime/github-branch-release-drafter"
LABEL "maintainer"="Joakim Erdfelt"
LABEL "com.github.actions.name"="Github Branch Release Drafter"
LABEL "com.github.actions.description"="Drafts your next release notes from pull requests as they are merged into relevant branches."
LABEL "com.github.actions.icon"="droplet"
LABEL "com.github.actions.color"="salmon"
WORKDIR /app
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]