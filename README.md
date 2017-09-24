# checking_account_service

Checking account service is a REST API for bank checking account manipulation. It was developed using clojure
programing language and [compojure-api](https://github.com/metosin/compojure-api) library.

The service supports storing banking operations, fetching checking account balance, creating account statetement
for a given period and calculating periods of negative balance (for interest calculation).

Current version stores all operations in memory. If you restart the service all stored operations will be lost.

This microservice was developed as an exercise for learning Clojure language and its usage for we development.
A broader description of the exercise can be found
[here](https://github.com/joaobertacchi/checking_account_service/blob/master/exercise.txt)

## Usage

To use this service, first you must get Leiningen installed in your computer. Follow Leiningen's website
instructions to install it (https://leiningen.org/#install).

### Get checking_account_service repo ###

You also need to clone checking_account_service repo into your computer:
`git clone https://github.com/joaobertacchi/checking_account_service.git`

### Run the application locally (do not open browser)

Once you got Leiningen installed and checking account service repo downloaded, go to repo's dir and start the
service:

`cd checking_account_service`
`lein ring server-headless`

To open checking account service documentation access http://localhost:3000/doc/v1/index.html

### Run the tests

`lein test`

### Run the tests during development for TDD (Mac OS)
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

Run autotest:
`lein test-refresh`

### Packaging and running as standalone jar

```
lein do clean, ring uberjar
java -jar target/server.jar
```

### Packaging as war

`lein ring uberwar`

## License

Copyright © 2017 João Eduardo F. Bertacchi
