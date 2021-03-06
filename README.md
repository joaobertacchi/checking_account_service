# checking_account_service

Checking account service is a micro-service that provides REST API for viewing and manipulating a
checking account from a supposed bank. It was developed using clojure programing language and
[compojure-api](https://github.com/metosin/compojure-api) library.

The service has four (4) endpoints:
1. **/accounts/:id/operations** for registering banking operations
1. **/accounts/:id/balance** for fetching checking account balance
1. **/accounts/:id/statement** for generating a checking account statetement
1. **/accounts/:id/debts** for calculating debts periods and balance (usefull for interest calculation).

Current version has an in memory storage for operations managed by atoms using Clojure STM. That means
if you restart the service all stored operations will be lost.

This microservice was developed as a solution for an exercise. It helped me to learn Clojure language and
how to use it for web development. A broader description of the exercise can be found
[here](https://github.com/joaobertacchi/checking_account_service/blob/master/exercise.txt)

## Usage

To use this service, first you must get Leiningen installed in your computer. Follow Leiningen's website
[install instructions](https://leiningen.org/#install).

### Get checking_account_service repo ###

You also need to clone checking_account_service repo into your computer:
```bash
git clone https://github.com/joaobertacchi/checking_account_service.git
```

### Run the application locally (using Leiningen)

Once you got Leiningen installed and checking account service repo downloaded, go to repo's dir and start the
service:

```bash
cd checking_account_service
lein ring server-headless
```

After that, check REST API documentation (see Documentation section bellow) to learn how to interact with
checking account service.

### Create a container an run the application from it (using Docker)

```bash
./build-docker-image.sh
./run-docker-container.sh
```

## Documentation

### For REST API users
The checking account service generates pretty Swagger documentation for its REST API.
In order to access REST API docs you have to:
1. run the application (using leiningen or docker), as explained above
1. access http://localhost:3000/doc/v1/index.html using your browser

There you can use the browser to test the endpoints by yourself. If you want, you can use curl:
```bash
curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
   "description": "Purchase on Amazon",
   "amount": -100.2,
   "date": "2017-09-25"
 }' 'http://localhost:3000/api/v1/accounts/1/operations'
```

### For checking account service maintainers (Codox)
Checking account service is not intended to be used as a library. Still its project is configured
to generate documentation using codox just as an example.

If you need to refer to the service documentation, please check _For REST API users_ section.

In order to generate codox documentation, run the comman bellow:

```bash
lein codox
```

Documentation is generated in the "target/doc" subdirectory as static html.

## Tests
All the tests were created using clojure.test lib.

### Run the tests
To run the tests, just do the following:
```bash
lein test
```

### Run the tests during development for TDD (Mac OS)
While coding it's useful to keep tests always running. Doing so will help you to figure out if
some change you are working on affects previously developed code. It's also very helpful if you intend
to follow TDD (test driven development) guidelines. In this methodology, you first create a test and later
the related code. At first, your test will fail until you get a working code. Your goal
is to develop just enogh code to make the test PASS.

#### Use Mac OS's notification subsystem for reporting tests status
Create /usr/local/bin/notify
```bash
#!/bin/bash
/usr/bin/osascript -e "display notification \"$*\" with title \"Tests\""
```

Add exec permission
```bash
chmod +x /usr/bin/notify
```

Add some TDD configurations to ~/.lein/profiles.clj:
```clojure
{:user {:dependencies [[pjstadig/humane-test-output "0.8.0"]]
        :injections [(require 'pjstadig.humane-test-output)
                     (pjstadig.humane-test-output/activate!)]
        :plugins [[com.jakemccrary/lein-test-refresh "0.16.0"]]
        :test-refresh {:notify-command ["notify"]
                       :quiet true
                       :changes-only true}}}
; Ref.: https://apple.stackexchange.com/questions/57412/how-can-i-trigger-a-notification-center-notification-from-an-applescript-or-shel
```

#### Run autotest
Run the following in the terminal to start autotest environment

```bash
lein test-refresh
```

As soon as a file is changed in the dir, tests are run automatically. The PASS/FAIL result will be shown in
as a Mac OS notification. To further investigate an eventual problem, check your terminal!

### Fuzzy test

#### Install PyJFAPI
Get PyJFuzz and PyJFAPI repos:
```bash
git clone https://github.com/mseclab/PyJFuzz.git
git clone https://github.com/dzonerzy/PyJFAPI.git
```

Install PyJFuzz:
```bash
cd PyJFuzz && sudo python setup.py install
```

#### Run fuzzer
Start the application as explained above and run the fuzzer passing application's hostname,
port and a fuzzy template operation.

```bash
cd PyJFAPI
python pjfapi.py -H localhost -P 3000 --p 10 --s -T template_operation.json
```

Fuzzer will run until you kills it using Ctrl^C.

### Continuous Integration

[Travis CI](https://travis-ci.org/) is configured to run all the tests whenever a new commit arrives to the
[this project GitHub repo](https://github.com/joaobertacchi/checking_account_service). Check last commits build
status at (https://travis-ci.org/joaobertacchi/checking_account_service).

Travis can also be configured to continuous deployment, but at this moment I see no gain for this exercise
in doing so.

## Packaging

### Packaging and running as standalone jar

```bash
lein do clean, ring uberjar
java -jar target/checking_account_service-0.1.0-standalone.jar
```

### Packaging as war
```bash
lein ring uberwar
```

## License

Copyright © 2017 João Eduardo F. Bertacchi
