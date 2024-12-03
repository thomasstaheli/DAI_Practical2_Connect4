# DAI_Practical2_Connect4
## Description

The Command-Line Connect Four project is a console-based implementation of the classic Connect Four game. It provides an interactive and straightforward interface where two players can compete in turns, placing their tokens on a grid to form a winning sequence. The project emphasizes game logic, board rendering, and user interaction through textual input and output.

## Previsualisation

![client interface]()

## Dependencies
### PicoCLI
The only dependency we are using in this project is PicoClI to implement easily the differents commands.

## Cloning the Repository
To get started, clone the repository using the following command:

```shell
git clone https://github.com/thomasstaheli/DAI_Practical2_Connect4.git
cd DAI_Practical2_Connect4
```
## Build the Project (JAR)
To build the JAR, run the following command from the root of the repository:

./mvnw dependency:go-offline clean compile package

## Compile 

## Docker

### Build

```shell
docker build -t connect4 .
```

### Publish

#### Connexion à la Github Container Registry

```shell
docker login ghcr.io -u <username>
```

#### Tag l'image

```shell
docker tag connect4 ghcr.io/<username>/connect4:latest
```

#### Publish the image

```shell
docker push ghcr.io/<username>/connect4
```

#### Pull the image

```shell
docker pull ghcr.io/<username>/connect4
```

### Run as Server

### With Docker

<exposed port> = port exposé sur ma machine
<port container> = port qui utilise le protocol dans le container
```shell
docker run -p <exposed port>:<port container> connect4 server
```
Example :
```shell
docker run -p 6433:6433 connect4 server
```

### Whithout Docler
```shell
java -jar target/DAI_Practical2_Connect4-1.0-SNAPSHOT.jar server -p <port> -h <board height> -w <board width> -wl <win lenght condition>
```
Example :
You can run with the default value :
```shell
java -jar target/DAI_Practical2_Connect4-1.0-SNAPSHOT.jar server 
```
Or width other values :
```shell
java -jar target/DAI_Practical2_Connect4-1.0-SNAPSHOT.jar server  -p 6464 -h 10 -w 10 -wl 6
```

## Run as Client
### With Docker

Le client se connecte sur le port <exposed port>
```shell
docker run -it connect4 client -p <port> --ip <ip>
```

### Whithout Docler

```shell
java -jar target/DAI_Practical2_Connect4-1.0-SNAPSHOT.jar client -p <port> --ip <ip>
```
Example :
You can run with the default value :
```shell
java -jar target/DAI_Practical2_Connect4-1.0-SNAPSHOT.jar client
```
Or width other values :
```shell
java -jar target/DAI_Practical2_Connect4-1.0-SNAPSHOT.jar client -p 6464 --ip 172.16.32.24
```

## Contributing
If you have any ideas to improve our project, just create a fork repository and open a pull-request !

Authors
- Thomas Stäheli
- Thirusan Rajadurai
