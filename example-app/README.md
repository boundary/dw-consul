# example-app

A quick example of a service that registers itself in consul and has access to a consul client.

Exposes a small subset of consul kv store over a simplified HTTP api. Could be used as a starting point for building more significant business logic on top of consul.

# usage

First, make sure you have a local consul instance available.

```
  $ mvn package
  $ java -jar example-app/target/
  $ # verify service registration
  $ curl 127.0.0.1:8500/v1/health/service/example-app
  $ # create a key
  $ curl -X PUT -d "the-value" localhost:10500/v1/kv/foo
  $ # verify key
  $ curl 127.0.0.1:10501/v1/kv/foo

```