### CollectorContext

There could be use cases where we want collect the information while we are validating the data. A simple example could be fetching some value from a database or from a microservice based on the data (which could be a text or a JSON object. It should be noted that this should be a simple operation or validation might take more time to complete.) in a given JSON node and the schema keyword we are using.

The fetched data can be stored somewhere so that it can be used later after the validation is done. Since the current validation logic already parses the data and schema, both validation and collecting the required information can be done in one go.

The `CollectorContext` and `Collector` classes are designed to work with this use case.

#### How to use CollectorContext

The `CollectorContext` is stored as a variable on the `ExecutionContext` that is used during the validation. This allows users to add objects to context at many points in the framework like Formats and Validators where the `ExecutionContext` is available as a parameter.

By default the `CollectorContext` created by the library contains maps backed by `HashMap`. If the `CollectorContext` needs to be shared by multiple threads then a `ConcurrentHashMap` needs to be used.

```java
CollectorContext collectorContext = new CollectorContext(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
```

Collectors are added to `CollectorContext`. Collectors allow to collect the objects. A `Collector` is added to `CollectorContext` with a name and corresponding `Collector` instance.

```java
CollectorContext collectorContext = executionContext.getCollectorContext();
collectorContext.add(SAMPLE_COLLECTOR_NAME, new Collector<List<String>>() {
    @Override
    public List<String> collect() {
        List<String> references = new ArrayList<String>();
        references.add(getDatasourceMap().get(node.textValue()));
        return references;
    }
});
```

However there might be use cases where we want to add a simple Object like String, Integer, etc, into the Context. This can be done the same way a collector is added to the context.

```java
CollectorContext collectorContext = executionContext.getCollectorContext();
collectorContext.add(SAMPLE_COLLECTOR, "sample-string");
```

Implementations that need to modify values the `CollectorContext` should do so in a thread-safe manner.

```java
CollectorContext collectorContext = executionContext.getCollectorContext();
AtomicInteger count = (AtomicInteger) collectorContext.getCollectorMap().computeIfAbsent(SAMPLE_COLLECTOR,
        (key) -> new AtomicInteger(0));
count.incrementAndGet();
```

To use the `CollectorContext` while validating, the `CollectorContext` should be instantiated outside and set for every validation execution.

At the end of all the runs the `CollectorContext.loadCollectors()` method can be called if needed for the `Collector` implementations to aggregate values.

```java
// This creates a CollectorContext that can be used by multiple threads although this is not neccessary in this example
CollectorContext collectorContext = new CollectorContext(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
// This adds a custom collect keyword that sets values in the CollectorContext whenever it gets processed
JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012()).keyword(new CollectKeyword()).build();
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema));
JsonSchema schema = factory.getSchema("{\n"
        + "  \"collect\": true\n"
        + "}");
for (int i = 0; i < 50; i++) {
    // The shared CollectorContext is set on the ExecutionContext for every run to aggregate data from all the runs
    schema.validate("1", InputFormat.JSON, executionContext -> {
        executionContext.setCollectorContext(collectorContext);
    });
}
// This is called for Collector implementations to aggregate data
collectorContext.loadCollectors();
AtomicInteger result = (AtomicInteger) collectorContext.get("collect");
assertEquals(50, result.get());
```

There might be use cases where a collector needs to collect the data at multiple touch points. For example one use case might be collecting data in a validator and a formatter. If you are using a `Collector` rather than a `Object`, the combine method of the `Collector` allows to define how we want to combine the data into existing `Collector`. `CollectorContext` `combineWithCollector` method calls the combine method on the `Collector`. User just needs to call the `CollectorContext` `combineWithCollector` method every time some data needs to merged into existing `Collector`. The `collect` method on the `Collector` is called by explicitly calling `CollectorContext.loadCollectors()` at the end of processing.

```java
class CustomCollector implements Collector<List<String>> {

    List<String> returnList = new ArrayList<>();

    private Map<String, String> referenceMap = null;

    public CustomCollector() {
        referenceMap = getDatasourceMap();
    }

    @Override
    public List<String> collect() {
        return returnList;
    }

    @Override
    public void combine(Object object) {
        synchronized(returnList) {
            returnList.add(referenceMap.get((String) object));
        }
    }
}
```

```java
private class CustomValidator extends AbstractJsonValidator {
    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        CollectorContext collectorContext = executionContext.getCollectorContext();
        CustomCollector customCollector = (CustomCollector) collectorContext.getCollectorMap().computeIfAbsent(SAMPLE_COLLECTOR,
                key -> new CustomCollector());
        customCollector.combine(node.textValue());
        return Collections.emptySet();
    }
}
```

One important thing to note when using Collectors is if we call get method on `CollectorContext` before the validation is complete, we would get back a `Collector` instance that was added to `CollectorContext`.

```java
// Returns Collector before validation is done.
Collector<List<String>> collector = collectorContext.get(SAMPLE_COLLECTOR);

// Returns data collected by Collector after the validation is done.
List<String> data = collectorContext.get(SAMPLE_COLLECTOR);

```

If you are using simple objects and if the data needs to be collected from multiple touch points, logic is straightforward as shown.

```java
List<String> returnList = (List<String>) collectorContext.getCollectorMap()
        .computeIfAbsent(SAMPLE_COLLECTOR, key -> new ArrayList<String>());
synchronized(returnList) {
    returnList.add(node.textValue());
}
```
