SBT Dockerfile
==============

> Dockerfile with SBT as entrypoint

To compile app within the docker container, run from the app root folder

`docker run -it --rm -v $PWD:/root --name sbt sbt assembly`

First time will take a long time to download all deps, consecutive builds will be faster.