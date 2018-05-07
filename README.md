[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
NODEFLOW4J
============================
> A light-weight,JSON-defined and compenent-orented process engine for JAVA.

## Features
  
* **JSON-Defined**

  Different to most other process engine defined with XML, `nodeflow` defines with JSON representation.
  
* **Compenent-Orented**

  Including built-in compenents, you can custom your own compenents, and reuse its in different bussiness flow.
  This is a flexible and scalable way to build your bussiness flow.
    
* **Groovy Expression**

  We use the groovy to run the expression script, it is more powerful and faster.
  
## JSON-defined sample

```JSON
{
 "indexs":[{"idx":"foo","type":"INT"},{"idx":"bar","mode":2,"type":"INT"}],
 "nodes":[
   {"id":"n1","component":"ruleset","ruleset":["bar=foo*1000","bar+=24"],"next":"n2"},
   {"id":"n2","next":[{"check":"bar==1024","next":"n3"},{"check":"bar==2024","next":"n4"},{"next":"n5"}]},
   {"id":"n3","component":"testCustomComponent"},
   {"id":"n4"},
   {"id":"n5"}
 ]
}
```

* **indexs** - Input and output parameters

  *ind* - parameter name
  
  *type* - INT,LONG,DOUBLE or NUMERIC as parameter type is Integer, Long, Double or BigDecimal, and case insensitive
  
  *mode* - 1, 2, or 3 as parameter mode is input, output or both input and output.
  
* **nodes** - The nodeflow activities

  *id* - The unique key on the flow
  
  *component* - The component id
  
  *next* - The next step of the current node. The value is a id string of the node, or if object, it will check the **check** property.  

## Built-in components

We applies the following built-in components, more will be built in the future.
But you can custom the component by yourself.

**ruleset** - Execute a array of the groovy expressions.

**delay** - Delay before next step


## Getting started

**Running the sample**

The following is the code snippet of *com.github.zzzhan.nodeflow4j.NodeflowEngineTest*, which locates in *src/test/java*.
You can run and see the sample result.

```java
	nfJson = IOUtils.toString(NodeflowEngineTest.class.getResourceAsStream("/NodeflowEngineTest.nf"));
	Map<String, Object> params = new HashMap<String,Object>();
	params.put("foo", 1);
	NodeflowEngine nf = new NodeflowEngine(nfJson);
	nf.execute(params);
	assertEquals("n3", nf.getCurNode());
	Map<String, Object> output = nf.getOutput();
	assertEquals(2048, output.get("bar"));

	params.put("foo", 2);
	nf = new NodeflowEngine(nfJson);
	nf.execute(params);
	assertEquals("n4", nf.getCurNode());
	output = nf.getOutput();
	assertEquals(2024, output.get("bar"));

	params.put("foo", 3);
	nf = new NodeflowEngine(nfJson);
	nf.execute(params);
	assertEquals("n5", nf.getCurNode());
```

**Spring integration**

Add the following component to your spring project

```java
@Component
public class NfComponentDiscover implements ApplicationRunner, ApplicationContextAware {

	private ApplicationContext ctx;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Map<String, NfComponent> beansMap = ctx.getBeansOfType(NfComponent.class);
		Collection<NfComponent> list = beansMap.values();
		for (NfComponent component : list) {
			NodeflowEngine.register(component);
		}		
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.ctx = ctx;
	}

}
```

**Maven**

```xml
<dependency>
	<groupId>com.github.zzzhan</groupId>
	<artifactId>nodeflow4j</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```