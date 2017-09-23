# checking_account_service

FIXME

## Usage

Follow Leiningen's website instructions to install leiningen in your computer (https://leiningen.org/#install).
Clone checking_account_service to your computer:
`git clone https://github.com/joaobertacchi/checking_account_service.git`

### Run the application locally (open default browser)

`lein ring server`

### Run the application locally (do not open browser)

`lein ring server-headless`

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

Copyleft © GNU GPL
