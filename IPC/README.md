# Casterlabs Commons/IPC

In progress :^)

## Protocol

All actual messaging is done over JSON, this is for interoperability and ease-of-debugging (with the exception being `Throwable`s, which use Java's built-in \[de\]serialization). Passing non-ipc objects is accomplished by serializing them using Rson, where you will need to add either `@JsonField` or `@JsonClass(exposeAll = true)` to your fields or classes respectively.

## Examples

Full example:

```java
public class Example {
    private static IpcConnection client;
    private static IpcConnection host;

    public static void main(String[] args) throws InterruptedException {
        client = new IpcConnection() {
            @Override
            protected void handleMessage(Object message) {
                TestObject test = (TestObject) message;

                System.out.printf("Answer to life, the universe, and everything: %d\n", test.answer(21));
            }

            @Override
            protected void send(IpcPacket packet) {
                System.out.printf("[Client] Send: %s\n", Rson.DEFAULT.toJsonString(packet));
                AsyncTask.createNonDaemon(() -> host.receive(packet));
            }
        };

        host = new IpcConnection() {
            @Override
            protected void handleMessage(Object message) {
                // Unused
            }

            @Override
            protected void send(IpcPacket packet) {
                System.out.printf("[Host  ] Send: %s\n", Rson.DEFAULT.toJsonString(packet));
                AsyncTask.createNonDaemon(() -> client.receive(packet));
            }
        };

        TestObject localInstance = new TestObject();

        host.sendMessage(localInstance);

        Thread.sleep(100000); // Sleep indefinitely. Prevents GC.
    }

    public static class TestObject extends IpcObject {

        public int answer(int base) {
            return base * 2;
        }

    }

}
```

## Adding to your project

Replace `_VERSION` with the latest version or commit in this repo and make sure to add the [Repository](https://github.com/Casterlabs/Commons#Repository) to your build system.

<details>
  <summary>Maven</summary>
  
  ```xml
    <dependency>
        <groupId>co.casterlabs.Commons</groupId>
        <artifactId>IPC</artifactId>
        <version>_VERSION</version>
    </dependency>
  ```
</details>

<details>
  <summary>Gradle</summary>
  
  ```gradle
	dependencies {
        implementation 'co.casterlabs:Commons.IPC:_VERSION'
	}
  ```
</details>

<details>
  <summary>SBT</summary>
  
  ```
libraryDependencies += "co.casterlabs.Commons" % "IPC" % "_VERSION"
  ```
</details>

<details>
  <summary>Leiningen</summary>
  
  ```
:dependencies [[co.casterlabs.Commons/IPC "_VERSION"]]	
  ```
</details>
