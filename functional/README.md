# Casterlabs Commons/Functional

This package contains a bunch of functional code helpers.

## Examples

Example usage of Either:

```java
Either<String, Integer> stringOrInt = Either.newA("Hello World!");

stringOrInt
  .ifA((str) -> {
    System.out.println(str);
  })
  .ifB((i) -> {
    System.out.println(i);
  });
// Prints: Hello World!

stringOrInt.b(); // IllegalStateException: Unable to get the value as type B because the value is of type A.
```

Tuples:

```java
Pair<String, Integer> stringAndInt = new Pair("Hello World!", 42);

stringAndInt.a(); // "Hello World!"
stringAndInt.b(); // 42

// They're also ordered, so you can:
stringAndInt.get(0); // "Hello World!"


// There's also Triplet, Quadruple, and VariableTuple (which takes an arbitrary amount of arguments, very dirty).
```

## Adding to your project

Replace `_VERSION` with the latest version or commit in this repo and make sure to add the [Repository](https://github.com/Casterlabs/Commons#Repository) to your build system.

<details>
  <summary>Maven</summary>
  
  ```xml
    <dependency>
        <groupId>co.casterlabs.Commons</groupId>
        <artifactId>Functional</artifactId>
        <version>_VERSION</version>
    </dependency>
  ```
</details>

<details>
  <summary>Gradle</summary>
  
  ```gradle
	dependencies {
        implementation 'co.casterlabs:Commons.Functional:_VERSION'
	}
  ```
</details>

<details>
  <summary>SBT</summary>
  
  ```
libraryDependencies += "co.casterlabs.Commons" % "Functional" % "_VERSION"
  ```
</details>

<details>
  <summary>Leiningen</summary>
  
  ```
:dependencies [[co.casterlabs.Commons/Functional "_VERSION"]]	
  ```
</details>
