# The Connect 4 protocol 

Pour le travail de laboratoire, nous devons développer une application

# Section 1 - Overview
The "Connect 4" protocol is a communication protocol that allows two clients to play against each 
other on a board generated and managed by the server.
# Section 2 - Transport protocol
The "Connect 4" protocol is a text transport protocol. It uses the TCP transport protocol to ensure the reliability of data transmission. The port it uses is the port number 6433. We selected this port for no particular reason other that it isn't used by default and it is the one used for an exercice.

Every message must be encoded in UTF-8 and delimited by a newline character (\n). The messages are treated as text messages.

The initial connection must be established by the clients.

Once one connection is established, the server gets into a waiting state.

When two players send a ready state, the game can start.

Each client can then take turns to send a column where he wants to place a token.

The server's role is to verify if the received column number is valid (is it even a number?) and in between given bounds.

If these conditions are met, the server will add the token to it's gameboard and process the result before sending the board infos to both player clients.

Otherwise, the server sends an error message to the client.

The error must specify which condition has not been met.

On an unknown message, the server must send an error to both clients.

Once the game ends, the server send a GameOver message to both player clients and wait for X seconds. When the wait is done, the server disconnects both players, the server returns to the waiting state with both players already connected but no ready for the next game. (Maybe disconnect both players so other players can join more easily)

# Section 3 - Messages

## Client connect to the server

The server notifies the client the rulesets of the game, when he just connected.
After that, when two players are connected, the server say wich one start playing and wich one start waiting.

### Request

None

### Response

- `INIT <height board> <width board> <win lenght> ` : The server send to the client the ruletset of the game, like the width and the height of the board and the number of tokens that should be place to win the game.
- `PLAY` : Indicating that the game start and the player place first.
- `WAIT` : Indicating that the game start and the player is not playing first.

## Place a token in the grid

The client sends a column to the server indicating the column number
where he want to place his token.

### Request

``` text
PLACE <colomn number>
```

### Reponse to the player who placed the token

- `TOKEN_PLACED` : The placed token is placed propreply in the column, the game continues.
- `ERROR <code>` : an error occurred while the PLACE command. The error code is an integer between 1 and 3 inclusive. 
  - 1 : The index is incorrect, the number is not between 0 and gameboard.width - 1.
  - 2 : The index is not a number.
  - 3 : The column number is already full.

### Reponse to the player who didn't placed the token

The server send two response in the row, the first one is to indicate the game status and the second one is to indicate where the token has been placed.

#### First response

- `GAME_CONTINUE` : The placed token by the other player did not succes to a win or a draw, so the game continues.
- `LOSE`   : The placed token by the other player results in a loss of the game.
- `DRAW`   : The placed token by the other player results in a draw of the game.

#### Second response

- `INSERTED <column number>` : The placed token by the other player is indicated to the waiting player.

## Forfeit

The client want to FF.

### Request

``` text
FF15
```

### Response

- `LOSE` : A player declared forfeit and results in a Loss of the player.
- `WIN`  : A player declared forfeit and results in a Win of the player.

## Invalid message

If the server receives an unknown message, it must send an error message to the client.

### Response

- `ERROR <code>` : an error occurred while sending the message. The error code is an integer. 
The error code is as follow:
  - -1 : invalid command

# Section 4 - Examples

## Functionnal example

![functionnal_example](./img/protocol_functionnal.png)

## Invalid command or input example

![invalid_example](./img/protocol_invalid_examples.png)
