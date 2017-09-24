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
instructions to install it (https://leiningen.org/#install).

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

To open checking account service documentation access http://localhost:3000/doc/v1/index.html

## Tests
All the tests were created using clojure.test lib.

### Run the tests
To run the tests, just run the following:
```bash
lein test
```

### Run the tests during development for TDD (Mac OS)
During development it's useful to keep tests always running. Doing so will help you to figure out if
some change you are working on affects previously developed code. It's also very helpful if you intend
to follow TDD (test driven development) guidelines. In the methodology you first need to create a test
the code you intend to work on. As the code is still not working, this test will fail. Then your goal
is to develop just enogh code to make the test PASS.

#### Use Mac OS's notification subsystem for reporting tests status
Create /usr/local/bin/notify
```bash
#!/bin/bash
/usr/bin/osascript -e "display notification \"$*\" with title \"Tests\""
```

Add exec permission:
`chmod +x /usr/bin/notify`

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
````

#### Run autotest
`lein test-refresh`

## Packaging

### Packaging and running as standalone jar

```
lein do clean, ring uberjar
java -jar target/server.jar
```

### Packaging as war

`lein ring uberwar`

## License

Copyright © 2017 João Eduardo F. Bertacchi
