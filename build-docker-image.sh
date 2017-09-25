#!/bin/bash

set -ex

lein do clean, ring uberjar
cp target/checking_account_service-0.1.0-standalone.jar .
docker build -t joaobertacchi/checking_account_service .
