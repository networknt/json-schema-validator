### CollectorContext


There could be usecases where we want collect the information while we are validating the data. A simple example could be fetching some value from a database or from a microservice based on the data (which could be a text or a JSON object) in a given JSON node and the schema keyword we are using. 

The fetched data can be stored some where so that it can be used later after the validation is done. Since the current validation logic already parses the data and schema, both validation and collecting the required information can be done in one go.

CollectorContext and Collector classes are designed to satisfy this usage.

#### How to use CollectorContext

Objects of CollectorContext live on ThreadLocal which is unique for every thread. This allows users to add objects to context at many points in the framework like Formats,Keywords,Validators etc.

CollectorContext instance can be obtained by calling the getInstance static method on CollectorContext.This method gives an instance from the ThreadLocal for the current thread.

Collectors are added to CollectorContext. Collectors allows to collect the objects. A Collector is added to CollectorContext with a name and corresponding Collector instance.

```
CollectorContext collectorContext = CollectorContext.getInstance();
			collectorContext.add(SAMPLE_COLLECTOR_TYPE, new Collector<List<String>>() {
				@Override
				public List<String> collect() {
					List<String> references = new ArrayList<String>();
					references.add(getDatasourceMap().get(node.textValue()));
					return references;
				}
			});
```

To validate the schema with the ability to use CollectorContext, validateAndCollect method has to be invoked on the JsonSchema class. This class returns a ValidationResult that contains the errors encountered during validation and a CollectorContext instance. Objects constructed by Collectors can be retrieved from CollectorContext by using the name they were added with.


```
 ValidationResult validationResult = jsonSchema.validateAndCollect(jsonNode);
 CollectorContext context = validationResult.getCollectorContext();
 List<String> contextValue = (List<String>)context.get(SAMPLE_COLLECTOR_TYPE);
 
```

Note that CollectorContext will be removed from ThreadLocal once validateAndCollect method returns. Also the data is loaded into CollectorContext only after all the validations are done.

