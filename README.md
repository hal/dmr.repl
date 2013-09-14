dmr-repl
========

A REPL shell for managing Wildfly server instances. Build on sbt and the scala REPL at it's core.


```scala

scala> val client = connect()
client: ControllerClient = ControllerClient@3aeece0e


scala> storage.contents
root


scala> val op = storage.load("root").get
op: org.jboss.dmr.ModelNode =
{
    "address" => [],
    "operation" => "read-resource"
}

scala> client.execute(op)
res4: Option[org.jboss.dmr.ModelNode] =
Some({
    "outcome" => "success",
    "result" => {
        "management-major-version" => 1,
        "management-micro-version" => 0,
        "management-minor-version" => 4,
        "name" => "radio-86rk",
        "namespaces" => [],
        "product-name" => "EAP",
        "product-version" => "6.2.0.Alpha1",
        "profile-name" => undefined,
        "release-codename" => "Janus",
        "release-version" => "7.3.0-redhat-SNAPSHOT",
        "schema-locations" => [],
        "core-service" => {
            "management" => undefined,
            "service-container" => undefined,
            "module-loading" => undefined,
            "server-environment" => undefined,
            "platform-mbean" => undefined,
            "patching" => undefi...
scala>

```
