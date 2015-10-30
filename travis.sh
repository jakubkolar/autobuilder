#!/bin/sh

# Get the GPG key
curl --location --silent --show-error https://github.com/jakubkolar/gpg-secret/blob/master/private.key.enc?raw=true \
	| openssl aes-256-cbc -K $encrypted_12c8071d2874_key -iv $encrypted_12c8071d2874_iv -d \
	| gpg --import

if [ "${TRAVIS_PULL_REQUEST}" = "false" ] ; then
    mvn deploy -B -P ci-coverage,ossrh-deploy --settings settings.xml
else
    mvn verify -B -P ci-coverage
fi
