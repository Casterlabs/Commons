# Casterlabs Commons

Casterlabs' Commons library, for that sweet, sweet code reuse.

## Packages

We sort out the different utils into distinct packages, allowing you to pull in only what you need. Unlike other common libraries, this avoids bloating your builds with a bunch of unused code.

Select a subproject to see it's details and dependency declaration.

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
