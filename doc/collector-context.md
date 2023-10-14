### CollectorContext


There could be usecases where we want collect the information while we are validating the data. A simple example could be fetching some value from a database or from a microservice based on the data (which could be a text or a JSON object. It should be noted that this should be a simple operation or validation might take more time to complete.) in a given JSON node and the schema keyword we are using. 

The fetched data can be stored somewhere so that it can be used later after the validation is done. Since the current validation logic already parses the data and schema, both validation and collecting the required information can be done in one go.

CollectorContext and Collector classes are designed to work with this use case.

#### How to use CollectorContext

Objects of CollectorContext live on ThreadLocal which is unique for every thread. This allows users to add objects to context at many points in the framework like Formats, Validators (Effectively CollectorContext can be used at any touch point in the validateAndCollect method thread call).

CollectorContext instance can be obtained by calling the getInstance static method on CollectorContext. This method gives an instance from the ThreadLocal for the current thread.

Collectors are added to CollectorContext. Collectors allow to collect the objects. A Collector is added to CollectorContext with a name and corresponding Collector instance.

```java
CollectorContext collectorContext = CollectorContext.getInstance();
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
CollectorContext collectorContext = CollectorContext.getInstance();
collectorContext.add(SAMPLE_COLLECTOR,"sample-string")

```

To retrieve the `CollectorContext` after validation or walking, automatic reseting of collector contexts has to be disabled:

```java
SchemaValidatorsConfig configuration = new SchemaValidatorsConfig();
configuration.setResetCollectorContext(false);
JsonSchema jsonSchema = factory.getSchema(uri, configuration);

```

To use the `CollectorContext` while validating, the `validateAndCollect` method has to be invoked on the `JsonSchema` class.
This method returns a `ValidationResult` that contains the errors encountered during validation and a `CollectorContext` instance.
Objects constructed by collectors or directly added to `CollectorContext` can be retrieved from `CollectorContext` by using the name they were added with.


```java
List<String> contextValue;
try {
    ValidationResult validationResult = jsonSchema.validateAndCollect(jsonNode);
    CollectorContext context = validationResult.getCollectorContext();
    contextValue = (List<String>)context.get(SAMPLE_COLLECTOR);
} finally {
   // Remove all data from collector context.
   // Otherwise, data is kept between validation runs which is usually unintended
   // and a potential memory leak.
   if (CollectorContext.getInstance() != null) CollectorContext.getInstance().reset();
}
// do something with contextValue
```

There might be usecases where a collector needs to collect the data at multiple touch points. For example one usecase might be collecting data in a validator and a formatter. If you are using a Collector rather than a Object, the combine method of the Collector allows to define how we want to combine the data into existing Collector. CollectorContext combineWithCollector method calls the combine method on the Collector. User just needs to call the CollectorContext combineWithCollector method every time some data needs to merged into existing Collector. The collect method on the Collector is called by the framework at the end of validation to return the data that was collected.

```java
 class CustomCollector implements Collector<List<String>> {

	List<String> returnList = new ArrayList<String>();

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
		returnList.add(referenceMap.get((String) object));
	}
}

CollectorContext collectorContext = CollectorContext.getInstance();
if (collectorContext.get(SAMPLE_COLLECTOR) == null) {
	collectorContext.add(SAMPLE_COLLECTOR, new CustomCollector());
}
collectorContext.combineWithCollector(SAMPLE_COLLECTOR, node.textValue());

```

One important thing to note when using Collectors is if we call get method on CollectorContext before the validation is complete, we would get back a Collector instance that was added to CollectorContext.

```java
// Returns Collector before validation is done.
Collector<List<String>> collector = collectorContext.get(SAMPLE_COLLECTOR);

// Returns data collected by Collector after the validation is done.
List<String> data = collectorContext.get(SAMPLE_COLLECTOR);

```

If you are using simple objects and if the data needs to be collected from multiple touch points, logic is straightforward as shown.

```java

CollectorContext collectorContext = CollectorContext.getInstance();
// If collector name is not added to context add one.
if (collectorContext.get(SAMPLE_COLLECTOR) == null) {
	collectorContext.add(SAMPLE_COLLECTOR, new ArrayList<String>());
}
// In this case we are adding a list to CollectorContext.
List<String> returnList = (List<String>) collectorContext.get(SAMPLE_COLLECTOR);

```

