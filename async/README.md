# Casterlabs Commons/Async

This package gives you simple facilties for threading and asynchronous work.

## Examples

Simple asynchronous task (which is cancellable):

```java
// You can also use createNonDaemon() if you need a normal thread.
AsyncTask.create(() -> {
  try {
    Thread.sleep(10000);
    System.out.println("Look ma, async!");
  } catch (InterruptedException e) {
    e.printStackTrace();
  }
});

// You can call #cancel() if you want.
```

Running sync-critical code:

```java
SyncExecutionQueue queue = new SyncExecutionQueue();

try {
  queue.execute(() -> {
    // Run your non thread-safe code here.
  });
} catch (InterruptedException e) {
  e.printStackTrace();
}
```

Running/Queuing thread-critical code:

```java
ThreadExecutionQueue queue = new ThreadExecutionQueue();

queue.submitTask(() -> {
  // All tasks will be executed in order of submission.
});

// You can also use #submitTaskAndWait() or #submitTaskWithPromise().
```

Using a ThreadExecutionQueue with a SWT Display:

```java
Display display = ...

ThreadExecutionQueue queue = new ThreadExecutionQueue(new ThreadExecutionQueue.Impl() {
  @Override
  public void getThread() {
    return display.getThread();
  }

  @Override
  public void submitTask(Runnable run) {
    display.asyncExec(run);
  }
});
```

Promise:

```java
// This resembles Javascript's promise.
new Promise<String>(() -> {
  return "world";
})
  .then((location) -> {
    System.out.printf("Hello %s!\n", location);
  })
  .except((t) -> {
    t.printStackTrace();
  });
```

## Adding to your project

Replace `VERSION_OR_HASH` with the latest version or commit in this repo and make sure to add the [Repository](https://github.com/Casterlabs/Commons#Repository) to your build system.

<details>
  <summary>Maven</summary>
  
  ```xml
    <dependency>
        <groupId>co.casterlabs.commons</groupId>
        <artifactId>Async</artifactId>
        <version>VERSION_OR_HASH</version>
    </dependency>
  ```
</details>

<details>
  <summary>Gradle</summary>
  
  ```gradle
	dependencies {
        implementation 'co.casterlabs.commons:Async:VERSION_OR_HASH'
	}
  ```
</details>

<details>
  <summary>SBT</summary>
  
  ```
libraryDependencies += "co.casterlabs.commons" % "Async" % "VERSION_OR_HASH"
  ```
</details>

<details>
  <summary>Leiningen</summary>
  
  ```
:dependencies [[co.casterlabs.commons/Async "VERSION_OR_HASH"]]	
  ```
</details>
