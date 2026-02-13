# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Server Structure

The following sequence diagram represents the design of my chess server. The chess server exposes seven endpoints. An endpoint is a URL that maps to a method that handles HTTP network requests. My chess client calls the endpoints in order to play a game of chess. Each of these endpoints convert the HTTP network request into service object method calls, that in turn read and write data from data access objects. The data access objects persistently store data in a database. The service object method uses the information from the request and the data access objects to create a response that is sent back to the chess client through the HTTP server.

[![Server Sequence Diagram](Chess-Server-Design.svg)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGcnQMqQE4DcvQCLIgDmAdgFDkCGAxsAPaZzgiSnDkAOVmoNI3duiy5MXHnwFUhACWkATKGO68Q-QcGE41kcavXTN+KsCoBBGjWRJy8k1QBGVJDHkPKbYJgCeSbv1JiaAAGADoATkpiTHoAV04AYgAWAGYADgAmcJpoBJZieGAHcFiYACVIYhAkLxMQegpYFk9oAFoAPi1RAC5oAG0ABQB5NAAVAF1oAHpYl0wAHVIAbwAiWaxSKgBbSBXulegVgBpD7hQAd0Z5PYPjw8gtqhBwG8OAX3IMHDwO6DlSRRYXqrdaYTY7V53FZnJCXTDXfaHE4rB5PF6IlYff6Api-L7YHS9TCVarALAACgqVRqWAqAEdSjUAJSfEQ6NqdYymCxWFC9YiQYAAVTm5NB4MgLK55ks1g50Dc3QAYiAAdARVhpdAHN5oOLtroHMSqABrdVzaDnMDwPVzCXQKjgY3yXWQAAepJs0p5ctaeLZVl6Gsw0tZ2isbX930wvTMTsgVBdo1NbAAom6rJxQA0w6JI50mqx2L0ksEUotVjsUFQBXtDqnMDEY7aNgaHfHE7rTCa2JjyGx5ORvbKUPmuoToKRYuBwLn2b9h7ykN0aMaycGxXNpVL7D7R79FWZ5PJzZr7HOIwvdyPl6uE2SzLFgPByVQn-Bt0Pr0v5Yfj9BH2fUN8XnX5sSUXoqVJWlkGnDhwJ+KMemgEE7QNSFkTfZ9RnoHtSEhD58UQgtmmLaAMmCYIKzWNCIQxTD3xwvCCP7AEohieIEkYaQBVyJVYFTfBYDMaAABl6CqRpSM0JCgX6YYxkmKYXBQepSArfUdhOGE4XkQiRB+ToEObVDWzo25kR0q4WOM+UQMDaBwAk1VyXEyT6UZYAWXsmAr25G9+UFDdNMlL9-J-A8HF6ABFUofFPENzyNBMzViTZ30YEAAC9IEHRdfSQicpxnC9fNk5shXS59Mpy+R00zbMKCI3FfkLTwS2CABGaiqyQGtdl6FYGybXo0qw+AatyvsBzCmUIsKhzg2AgNfM5b9rF6O8TEgQCXxCndwt9TpFQA-9duW8MyqMhQILE5zSAqJA4M3MzIBOcamLYFlbL9TpmuBGjXoww4Ptw3sMX06MxzasiKKo5ZAbBdD6JBxiwfwiHWMHShyGiOJEmiSA2FyfJCkJ4m3LiDgYZkv6DObPp8FTUTU1GVMlJUpA1MWUG8NzQy-huuTVl58HbixIXcTpy7eic4gqdfNG8O8lb5XyvloAFYBdsV7D0YOuajoVKLoFirBdXOpLjVSqqJswbLctmvdUF+8cHOK2cfLHf71Vtya6ozSAszU-mWpIotgA67qEd6-q6yGxtGFGv37dq6a2PVl2FsgWN3wuglLzWw6NfkSAoAfd9dfgT7SAN53fxNiotnoXAALz88vdd4zemwegQEHH7yt6OGUMhvNXZp4fKNHrH2PxhJiRPBJOFVM1RNJaAAHEDRsGm7Pp3o+k31mlOIA0eaVthQ-lbuUJWUWMfF8gfulguc8c0lt6rKua5Vy61fWhrLWOsH51xvA3GKcULbt1MOQZKpo9QpwdnlQBWdX5FWnJ7VWrsfaVXGv7eqQdGrXwntJKOPVrBx0GsNJOiD8Gpymh8GamdvYrVzkBDu2Ci6Gw1iwGoX9kDkjARFY6JszbxTjOALeBp86gWugCW669+E70es9M+VZvqS1YdGYESxDjqOQHWPoqwDEAEl8B7E6hkFISRkTnHgGASAwYJQ3GOIsQ4xRaAmmccjFYUIDEADkDT7ExNAcYY9iLMAjlPeGeiVgGKQEYkxBpzGWOsbYw49jHE+PMm40gHjwBeJyQNPxyJAnBL8W8MJTC2K4w4okeQAB2cIwRIDBFyKmFIsABIADY4BrhgAI8ge8h7yRGBMaYBiL56zwhWcpOwIlS0Fgo4W99L6PyRPog0QTzLvGflo7Om0BkCJ-ujE48zQqd24c7QK2tK6gKduAyKkDzZtw4bA+BNt6HIMefNdB7tMGlW0chPBGUGEBwaiHZq0MyHQFLNHSslDazUMTs2MaYLkHpxQcXNBbt36W1gVcggqCjn3kgCci5wijYnSPCeARsjLxgUlr0WAxyDSqPAMAckZj8CaJWWHLoqyeV1isTYxZMLonkUotRYVvRRVJBqdjPGnFvBlycucEmJBCiqpnPQDVAApPu+ShkjNfnJQYQoJlTCmbbGuFZOCFNVZgWA9AnKYHOSk-A4qmX8t6OmWodBoAMDwtAAAZNAAAVka6AqoABm9B9n8v3jLSNRqTkPxODQV1jAPU7HMX-N+ACcW3JAesql+5REvIkTAqgcDrZ0IxbVX5BV-nvw9kCnBB9fbfNqoQ4OOZoWkMlfCih1ZkX1lRcnHtjDZ4sM7SmgltaiWZ1uRSz15aXaVtNlA6ROxQxzsObuyADLVrEuLXqTgdgyRrrzfgE4IVM3ZswBuiB6pL3bSPSesct9e790TTiYFckR5LG9a1WFwHFVz04tEKguo8hauADB3UTQExMCoJwB1ag6g5lNYKhmTMWZsyUm4Eh8icQssdFIhgzBUP-qUMmt+m0oA8CER265AVoA0GY5gRdrG51bqbi3GAu0r1Lq4Wenhy5OPcfpfYPjqDX2CdbrJwl4mV3SdQ0tOTL7nnQCUzALTqn-5d2ZdAX9A8DmjIgyQsDkrrMDiAA)
## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
