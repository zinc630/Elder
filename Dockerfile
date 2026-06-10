FROM ubuntu:latest
LABEL authors="zinc"

ENTRYPOINT ["top", "-b"]