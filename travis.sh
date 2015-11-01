#!/bin/sh

##
# The MIT License (MIT)
#
# Copyright (c) 2015 Jakub Kolar
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
##

BUILD_ONLY="mvn verify -B -P ci-coverage"
BUILD_AND_DEPLOY="mvn deploy -B -P ci-coverage,ossrh-deploy --settings settings.xml"

fetch_key()
{
    # Get and decrypt the GPG key
    curl --location --silent --show-error https://github.com/jakubkolar/gpg-secret/blob/master/private.key.enc?raw=true \
        | openssl aes-256-cbc -K $encrypted_12c8071d2874_key -iv $encrypted_12c8071d2874_iv -d \
        | gpg --import
}

# Not a pull request -> potentially going to be deployed
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] ; then

    # This is kind-of-crazy...
    PROJECT_VERSION=`mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive exec:exec`

    case "${PROJECT_VERSION}" in
      *-SNAPSHOT)
        # Snapshots of master branch are deployed
        if [ "${TRAVIS_BRANCH}" = "master" ] ; then
            fetch_key
            ${BUILD_AND_DEPLOY}
        else
            ${BUILD_ONLY}
        fi
        ;;

      *)
        # Non-snapshot versions are deployed only if tagged
        if [ -n "${TRAVIS_TAG}" ] ; then
            fetch_key
            ${BUILD_AND_DEPLOY}
        else
            ${BUILD_ONLY}
        fi
        ;;
    esac

else
    # Pull request? Just build and test + coverage, no deployment, javadoc, source artifacts and signing
    ${BUILD_ONLY}
fi
