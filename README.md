# Casterlabs Commons

Casterlabs' Commons library, for that sweet, sweet code reuse.

## Packages

We sort out the different utils into distinct packages, allowing you to pull in only what you need. This avoids bloating your builds with a bunch of unused code, like a lot of other commons libraries do.

Select a subproject to get started.

[Platform](/platform/) &bull; OS & CPU arch detection.  
[Async](/async/) &bull; Threading & async helpers.  
[Events](/events/) &bull; Event helpers.  
[Functional](/functional/) &bull; Functional code helpers.  
[IPC](/ipc/) &bull; An in-progress IPC framework.  
[IO](/io/) &bull; Utilties for handling information.  
[WebSocket](/websocket/) &bull; An in-progress WebSocket client, not recommended for production use.  

## Repository

We use GitHub packages + our own resolver for our deployment and hosting.

<details>
  <summary>Maven</summary>
  
  ```xml
  <repositories>
    <repository>
      <id>casterlabs-maven</id>
      <url>https://repo.casterlabs.co/maven</url>
    </repository>
  </repositories>
  ```
</details>

<details>
  <summary>Gradle</summary>
  
  ```gradle
allprojects {
	repositories {
		maven { url 'https://repo.casterlabs.co/maven' }
	}
}
  ```
</details>

<details>
  <summary>SBT</summary>
  
  ```
resolvers += "casterlabs-maven" at "https://repo.casterlabs.co/maven"
  ```
</details>

<details>
  <summary>Leiningen</summary>
  
  ```
:repositories [["casterlabs-maven" "https://repo.casterlabs.co/maven"]]
  ```
</details>

## Used by

- Us :^)

_Want your project included here? Open an issue and we'll add you ❤._

## Development

This project utilizes Lombok for code generation (e.g Getters, Setters, Constructors), in order for your IDE to properly detect this, you'll need to install the Lombok extension. Instructions can be found [here](https://projectlombok.org/setup/) under "IDEs".

### Java Version

We've chosen to be compatible with the 3rd last LTS, which currently is Java 11. This allows us to support a wide variety of projects while not pinning ourselves to an ancient version.

When Java 25 releases in 2025, this project will be upgraded to Java 17. We'll then make a java-11 tag and freeze it for those who may still be stuck on 11.

#### Current Legacy Tags
- [java-8](https://github.com/Casterlabs/Commons/tree/java-8)
