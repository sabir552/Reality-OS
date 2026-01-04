#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched.
if $cygwin ; then
    [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Attempt to find java
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum number of open files
if ! $cygwin && ! $msys ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            # Use the system hard limit
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if $darwin; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin ; then
    APP_HOME=`cygpath --path --windows "$APP_HOME"`
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

# Split up the JVM options only if the JAVA_OPTS variable is not defined.
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS=($DEFAULT_JVM_OPTS)
fi

# Split up the Gradle options only if the GRADLE_OPTS variable is not defined.
if [ -z "$GRADLE_OPTS" ]; then
    GRADLE_OPTS=()
fi

# Collect all arguments for the java command, following the shell quoting and substitution rules
#
# (The nested-quoting technique is based on https://stackoverflow.com/a/13253245/227503)
#
# The purpose of this step is to handle the case where one of the arguments is a Gradle property definition
# that contains a space, e.g. "-Dmy.prop=a value".  If we don't do this, the shell will split
# the argument into two parts, which is not what we want.
#
# This is not 100% bulletproof, as it assumes that the user is not defining a property value that contains
# a quote character.  That's a reasonable assumption, though.
#
eval set -- "$(
    printf '%q ' "$JAVACMD" "${JAVA_OPTS[@]}" -Dorg.gradle.appname="$APP_BASE_NAME"
    printf '-Dorg.gradle.internal.wrapper.script_path=%q ' "$0"
    printf '%q ' "${GRADLE_OPTS[@]}" "$@"
)"

# This is the magic bit that will be reported in the ProcessEnvironment.
# It makes it possible for the daemon to know how it was launched.
#
# This is passed as a system property, rather than an environment variable,
# because the command line of a running process is available to all users on a
# multi-user system.  We don't want to expose potentially sensitive
# information included in the GRADLE_OPTS environment variable.
#
# In order to avoid any shell quoting issues, we resort to the following trick:
#   - create a temporary file
#   - write the name of the file to a well-known environment variable
#   - write the original command line to the file
#   - delete the file in an exit trap
#
# See also: https://github.com/gradle/gradle/issues/15082
#
if [ -z "$GRADLE_ORIGINAL_COMMAND_STRING" ]; then
    # Using a file under /tmp is not ideal, but it is the most cross-platform solution.
    # We could use mktemp, but that is not available on all systems.
    # We could use a file in the user's home directory, but that is not always available.
    #
    # We use a file name that is unlikely to be used by any other process.
    # We use a trap to delete the file on exit.
    # We do not use a random file name, because we want to be able to find the file
    # in the case where the trap is not executed for some reason.
    #
    GRADLE_ENV_FILE="/tmp/gradle-original-command-$$.env"

    # The command string is a single line, so we can use printf.
    # The file has to be written in the platform's default encoding.
    #
    printf "%s" "$*" > "$GRADLE_ENV_FILE"
    export GRADLE_ENV_FILE

    # Ensure that the file is deleted on exit.
    #
    # This is not 100% bulletproof, as it assumes that the shell will execute the trap.
    #
    trap 'rm -f "$GRADLE_ENV_FILE" "$GRADLE_ENV_FILE.lock"' EXIT
fi

# Start the Gradle wrapper
#
exec "$@"

