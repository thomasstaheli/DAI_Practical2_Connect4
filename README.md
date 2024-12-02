# DAI_Practical2_Connect4
This is a small project, where you can play Connect4 in a 1v1 match

# DAI_Practical2_Connect4
This is a small project, where you can play Connect4 in a 1v1 match

# Docker

## Build

docker build -t connect4 .

## Publish

### Connexion à la Github Container Registry

docker login ghcr.io -u <username>

### Tag l'image

docker tag connect4 ghcr.io/<username>/connect4:latest

### Publish the image

docker push ghcr.io/<username>/connect4

### Pull the image

docker pull ghcr.io/<username>/connect4

## Run as Server

<exposed port> = port exposé sur ma machine
<port container> = port qui utilise le protocol dans le container
command : docker run -p <exposed port>:<port container> connect4 server
Example : docker run -p 6433:6433 connect4 server

## Run as Client

Le client se connecte sur le port <exposed port>
command : docker run -it connect4 client --ip <ip>
