Cannons Revamped
=======

Fork by Intybyte / Vaan1310 </br>
Original by derPavlov

[What is new?](./FEATURES.md)

Issues:
---------------
If you have issues it is better that you report in this repository as the original one looks unmaintained for the time being

Help:
---------------
You can join the original cannons discord for assistance as I am there
https://discord.gg/fdt6sgsAgN

How to build / Get the jar
---------------
To compile and get the plugin jar you need to first [download maven](https://maven.apache.org/download.cgi) and then [install it](https://maven.apache.org/install.html).
There are some slight differences between Windows and Linux, you can find tutorials online in case you are stuck.

Now you need to go download the zip of the source code from GitHub and extract it somewhere.

Open the terminal to said folder and run `mvn clean install`. This will start building and compiling the plugin and give
you a jar to put into the plugin folder of your server.

If you plan on modifying the plugin and stay around the plugin scene for a bit longer
I suggest you download an IDE like IntellijIDEA and get you more experienced with dependencies and building with maven and gradle.

Dependency:
---------------

Use [jitpack.io](https://jitpack.io/#Intybyte/Cannons/) for dependency

```xml
<repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.Intybyte</groupId>
    <artifactId>Cannons</artifactId>
    <version>Tag</version>
</dependency>
```

Notes:
---------------
Never reload the plugin, even if you are using stuff like Plugman
