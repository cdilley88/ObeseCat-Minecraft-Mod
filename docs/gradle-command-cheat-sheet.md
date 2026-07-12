# Gradle Command Cheat Sheet

Quick terminal commands for day-to-day ObeseCat mod work.

## From the repo root

Use PowerShell in this folder:

```powershell
cd "G:\ObeseCat Minecraft Mod"
```

## Build and run

Build mod JAR:

```powershell
.\gradlew.bat build
```

Run Minecraft client with mod in dev environment:

```powershell
.\gradlew.bat runClient
```

Run dedicated server in dev environment:

```powershell
.\gradlew.bat runServer
```

Run game tests:

```powershell
.\gradlew.bat runGameTestServer
```

## Useful maintenance

Refresh dependencies:

```powershell
.\gradlew.bat --refresh-dependencies
```

Clean build outputs:

```powershell
.\gradlew.bat clean
```

## Where outputs go

Built JARs are written to:

- `build/libs/`

## Java note

If Java is not detected, set JAVA_HOME to the bundled JDK for the current shell session:

```powershell
$env:JAVA_HOME="G:\ObeseCat Minecraft Mod\jdk21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```
