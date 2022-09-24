# Casterlabs Commons/Platform

This package allows you to detect the host's Operating System and CPU Architecture, perfect if you need to implement native code conditionally.

## Examples

Is the host system a Linux flavor?

```java
if (Platform.osDistribution == OSDistribution.LINUX) {
    // Yes.
}
```

Are we running on Unix (e.g macOS or Linux) or Windows?

```java
switch (Platform.osFamily) {
    case UNIX: {
        break;
    }

    case WINDOWS: {
        break;
    }

    default: {
        break;
    }
}
```

What CPU Arch does the host use?

```java
System.out.println(Platform.arch); // amd64, aarch64, etc.
```

Get a filename for a library:

```java
System.out.println(Platform.formatLibrary("Webview")); // libwebview.so, Webview.dll, etc.
```

## Why are osFamily and osDistribution two different fields?

Well, the history of operating systems is complicated.

For example, when looking at [the history of the Unix family](https://upload.wikimedia.org/wikipedia/commons/6/64/Revised_Unix_OS_Chart.png), we see a lot of familiar faces such as Linux, BSD, and even macOS; And since these distributions are decendants of the same parent project, they have a lot in common. This _potentially_ allows you to reuse native code between Linux and macOS.

## Adding to your project

Replace `_VERSION` with the latest version or commit in this repo and make sure to add the [Repository](https://github.com/Casterlabs/Commons#Repository) to your build system.

<details>
  <summary>Maven</summary>
  
  ```xml
    <dependency>
        <groupId>co.casterlabs.Commons</groupId>
        <artifactId>Platform</artifactId>
        <version>_VERSION</version>
    </dependency>
  ```
</details>

<details>
  <summary>Gradle</summary>
  
  ```gradle
	dependencies {
        implementation 'co.casterlabs:Commons.Platform:_VERSION'
	}
  ```
</details>

<details>
  <summary>SBT</summary>
  
  ```
libraryDependencies += "co.casterlabs.Commons" % "Platform" % "_VERSION"
  ```
</details>

<details>
  <summary>Leiningen</summary>
  
  ```
:dependencies [[co.casterlabs.Commons/Platform "_VERSION"]]	
  ```
</details>
