# Casterlabs Commons/WebSocket

An experimental WebSocket implementation. Not recommended for production use as this currently doesn't strictly adhere to the spec.

## Examples

```java
WebSocketClient client = new WebSocketClient(URI.create("wss://echo.casterlabs.co"));
client.setListener(new WebSocketListener() {
    @Override
    public void onOpen(WebSocketClient client, Map<String, String> headers, @Nullable String acceptedProtocol) {
        System.out.println("Connected. Headers: " + headers);
    }

    @Override
    public void onText(WebSocketClient client, String string) {
        System.out.println("Got text message: '" + string + "'");
    }

    @Override
    public void onBinary(WebSocketClient client, byte[] bytes) {
        System.out.println("Got binary message: len=" + bytes.length);
    }

    @Override
    public void onClosed(WebSocketClient client) {
        System.out.println("Closed");
    }
});
client.connect(10_000, 5_000);

Scanner in = new Scanner(System.in);
while (true) {
    client.send(in.nextLine());
}

// Type into the console to send messages to the server. It will echo them back :^)
```

## Adding to your project

Replace `VERSION_OR_HASH` with the latest version or commit in this repo and make sure to add the [Repository](https://github.com/Casterlabs/Commons#Repository) to your build system.

<details>
  <summary>Maven</summary>
  
  ```xml
    <dependency>
        <groupId>co.casterlabs.commons</groupId>
        <artifactId>websocket</artifactId>
        <version>VERSION_OR_HASH</version>
    </dependency>
  ```
</details>

<details>
  <summary>Gradle</summary>
  
  ```gradle
	dependencies {
        implementation 'co.casterlabs.commons:websocket:VERSION_OR_HASH'
	}
  ```
</details>

<details>
  <summary>SBT</summary>
  
  ```
libraryDependencies += "co.casterlabs.commons" % "websocket" % "VERSION_OR_HASH"
  ```
</details>

<details>
  <summary>Leiningen</summary>
  
  ```
:dependencies [[co.casterlabs.commons/websocket "VERSION_OR_HASH"]]	
  ```
</details>
