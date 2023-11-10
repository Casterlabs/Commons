# Casterlabs Commons/Events

This package has helpers for creating event-based systems.

## Examples

SimpleEventProvider, which doesn't have an event type:

```java
SingleEventProvider<String> ep = new SingleEventProvider<>();

int taskId = ep.on((location) -> {
  System.out.printf("Hello %s!\n", location);
});

ep.fireEvent("world");
ep.off(taskId);
```

EventProvider, which does have an event type:

```java
public class CoffeeMachineMonitor extends EventProvider<CoffeeEventType, CoffeeEvent> {

  public static enum CoffeeEventType {
    // ...
  }

  // CoffeeEvent, CoffeeBrewErrorEvent, etc...

}
```

```java
CoffeeMachineMonitor mon = new CoffeeMachineMonitor();

ep.on(CoffeeEventType.BREW_FINISHED, () -> {
  System.out.println("Brew finished!");
});

ep.on(CoffeeEventType.BREW_ERROR, (e) -> {
  CoffeeBrewErrorEvent ev = (CoffeeBrewErrorEvent) e;
  System.out.printf("Error whilst brewing: %s\n", e.getMessage());
});
```

## Adding to your project

Replace `_VERSION` with the latest version or commit in this repo and make sure to add the [Repository](https://github.com/Casterlabs/Commons#Repository) to your build system.

<details>
  <summary>Maven</summary>
  
  ```xml
    <dependency>
        <groupId>co.casterlabs.Commons</groupId>
        <artifactId>Events</artifactId>
        <version>_VERSION</version>
    </dependency>
  ```
</details>

<details>
  <summary>Gradle</summary>
  
  ```gradle
	dependencies {
        implementation 'co.casterlabs:Commons.Events:_VERSION'
	}
  ```
</details>

<details>
  <summary>SBT</summary>
  
  ```
libraryDependencies += "co.casterlabs.Commons" % "Events" % "_VERSION"
  ```
</details>

<details>
  <summary>Leiningen</summary>
  
  ```
:dependencies [[co.casterlabs.Commons/Events "_VERSION"]]	
  ```
</details>
