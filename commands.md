This line to add the mods locally
```powershell
.\gradlew.bat runClient
```

These have edit on them to make kotlin work

build.gradle
>plugins

>repositories

> kotlin { jvmToolchain(21) }

> dependencies { implementation 'thedarkcolour:kotlinforforge-neoforge:5.12.0' localRuntime fileTree(dir: 'libs', include: ['*.jar']) }

> 

gradle.properties

> org.gradle.java.home=C:/Program Files/Eclipse Adoptium/jdk-21.0.12.8-hotspot

neoforge.mods.toml

    This here to add dependencies for the mod to work

> [[dependencies.${mod_id}]]
modId="kotlinforforge"
type="required"
versionRange="[5.12.0,)"
ordering="NONE"
side="BOTH"