#!/usr/bin/env bash

CURRENT_DIR=$PWD

function prepare_jar(){
  BASE_NAME=$1
  CLASS_NAME=$2
  BASE=shadowee/$BASE_NAME/
  if [ ! -e "$BASE$CLASS_NAME".jar ]; then
    mkdir -p "$BASE"
    cd "$BASE"
    touch "$CLASS_NAME".java
    echo "public interface $CLASS_NAME {}" >> "$CLASS_NAME".java
    javac "$CLASS_NAME".java
    jar cf "$CLASS_NAME".jar "$CLASS_NAME".class
    rm "$CLASS_NAME".java "$CLASS_NAME".class
    cd "$CURRENT_DIR"
  fi
}

prepare_jar lib UnmanagedBase
prepare_jar jars UnmanagedJars
prepare_jar jars UnmanagedClasspath
prepare_jar jars ManagedClasspath
prepare_jar jars InternalDependencyClasspath
prepare_jar jars ExternalDependencyClasspath
prepare_jar jars DependencyClasspath
