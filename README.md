# checking_account_service

Checking account service is a REST API for bank checking account manipulation. It was developed using clojure
programing language and [compojure-api](https://github.com/metosin/compojure-api) library.

The service has four (4) endpoints:
1. **/accounts/:id/operations** for storing banking operations
1. **/accounts/:id/balance** for fetching checking account balance
1. **/accounts/:id/statement** for creating account statetement
1. **/accounts/:id/debts** for calculating debts periods and balance (usefull for interest calculation).

Current version stores all operations in memory. If you restart the service all stored operations will be lost.

This microservice was developed as a solution for an exercise. It helped me to learn Clojure language and how to
use it for web development.
A broader description of the exercise can be found
[here](https://github.com/joaobertacchi/checking_account_service/blob/master/exercise.txt)

## Usage

To use this service, first you must get Leiningen installed in your computer. Follow Leiningen's website
[install instructions](https://leiningen.org/#install).

### Get checking_account_service repo ###

You also need to clone checking_account_service repo into your computer:
```bash
git clone https://github.com/joaobertacchi/checking_account_service.git
```

### Run the application locally (do not open browser)

Once you got Leiningen installed and checking account service repo downloaded, go to repo's dir and start the
service:

```bash
cd checking_account_service
lein ring server-headless
```

After that, check REST API documentation (see Documentation section bellow) to learn how to interact with
checking account service.

## Documentation

### REST API
The checking account service generates pretty Swagger documentation for its REST API.
In order to access REST API docs you have to:
1. run the application locally, as explained above
1. access http://localhost:3000/doc/v1/index.html using your browser

There you can use the browser to test the endpoints by yourself. If you want, you can use curl:
```bash
curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
   "description": "Purchase on Amazon",
   "amount": -100.2,
   "date": "2017-09-25"
 }' 'http://localhost:3000/api/v1/accounts/1/operations'
```

### Codox for functions
You can also generate programming documentation. To do so, run:

```bash
lein codox
```

This will generate API documentation in the "target/doc" subdirectory.

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
