# Casterlabs Commons

Casterlabs' Commons library, for that sweet, sweet code reuse.

## Packages

We sort out the different utils into distinct packages, allowing you to pull in only what you need. This avoids bloating your builds with a bunch of unused code, like a lot of other commons libraries do.

Select a subproject to get started.

[Platform](/Platform/) &bull; OS & CPU arch detection.

## Repository

We use Jitpack for our deployment and hosting.

<details>
  <summary>Maven</summary>
  
  ```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
  ```
</details>

<details>
  <summary>Gradle</summary>
  
  ```gradle
    allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}
  ```
</details>

<details>
  <summary>SBT</summary>
  
  ```
resolvers += "jitpack" at "https://jitpack.io"
  ```
</details>

<details>
  <summary>Leiningen</summary>
  
  ```
:repositories [["jitpack" "https://jitpack.io"]]
  ```
</details>

## Used by

-   Us :^)

_Want your project included here? Open an issue and we'll add you ❤._

## Development

This project utilizes Lombok for code generation (e.g Getters, Setters, Constructors), in order for your IDE to properly detect this, you'll need to install the Lombok extension. Instructions can be found [here](https://projectlombok.org/setup/) under "IDEs".

### Java Version

We've chosen to be compatible with the 3rd last LTS, which currently is Java 1.8. This allows us to support a wide variety of projects while not pinning ourselves to an ancient version.

When Java 21 releases in 2023, this project will be upgraded to Java 11. We'll then make a 1.8 branch and freeze it for those who may still be stuck on 1.8.
