# GCR Docker Image Poller Plugin for GoCD

This is a plugin which provides docker images in GCR as package material in GoCD. It is inspired by [this Docker image poller by Magnus Lyckå](https://github.com/magnus-lycka/gocd-docker-poller)

## Building the code base

To build the code base just execute:

- `mvn clean package`

## Getting started

### Requirements

- GoCD Server 19.0+

### Plugin Installation

Build the plugin as mentioned above.

Install one of our releases into `${GO_SERVER_DIR}/plugins/external` and restart the server.

### Plugin Configuration

To configure the package repository, you will only need to provide the following 3 items:

1. `GCP Service Account Key`: Service account secret in JSON that has access to the desired GCR repository.
2. `GCP project id`: Google cloud project id
3. `GCR url`: GCR repository url, e.g. gcr.io, asia.gcr.io, us.gcr.io

To configure the material pulling packages from this repository, you will need to provide:

1. `Package Name`: Name of package (on GoCD)
2. `Docker Image Name`: Name of the docker image in the repository
3. `Docker Tag Filter Regular Expression`: Filter for desired image tags in REGEX

### Referencing the package in a pipeline

When this material triggers the pipeline, it will set the following environment variables, which you can use in your pipeline to access the latest docker image.

1. `GO_REPO_<docker registry name>_<package name>_DOCKER_REGISTRY_NAME`: docker imame registry name
2. `GO_PACKAGE_<docker registry name>_<package name>_DOCKER_IMAGE`: docker image name
3. `GO_PACKAGE_<docker registry name>_<package name>_LABEL`: docker image tag
