#!/bin/bash

set -ex

docker run --rm -p 3000:3000 joaobertacchi/checking_account_service
