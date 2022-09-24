# Casterlabs Commons/Async

This package gives you simple facilties for threading and asynchronous work.

## Examples

Simple asynchronous task (which is cancellable):

```java
new AsyncTask(() -> {
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
SyncQueue queue = new SyncQueue();

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
ThreadQueue queue = new ThreadQueue();

queue.submitTask(() -> {
  // All tasks will be executed in order of submission.
});

// You can also use #submitTaskAndWait() or #submitTaskWithPromise().
```

Using a ThreadQueue with a SWT Display:

```java
Display display = ...

ThreadQueue queue = new ThreadQueue(new ThreadQueue.Impl() {
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

Replace `_VERSION` with the latest version or commit in this repo and make sure to add the [Repository](https://github.com/Casterlabs/Commons#Repository) to your build system.

<details>
  <summary>Maven</summary>
  
  ```xml
    <dependency>
        <groupId>co.casterlabs.Commons</groupId>
        <artifactId>Async</artifactId>
        <version>_VERSION</version>
    </dependency>
  ```
</details>

<details>
  <summary>Gradle</summary>
  
  ```gradle
	dependencies {
        implementation 'co.casterlabs:Commons.Async:_VERSION'
	}
  ```
</details>

<details>
  <summary>SBT</summary>
  
  ```
libraryDependencies += "co.casterlabs.Commons" % "Async" % "_VERSION"
  ```
</details>

<details>
  <summary>Leiningen</summary>
  
  ```
:dependencies [[co.casterlabs.Commons/Async "_VERSION"]]	
  ```
</details>
