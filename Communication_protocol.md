# The Connect 4 protocol 

Pour le travail de laboratoire, nous devons développer une application

# Section 1 - Overview

# Section 2 - Transport protocol

# Section 3 - Messages

## Saying ready

The client notifies a ready state when he wants to start the game
(A player slot is set to occupied as soon as he joins ->
other players cannot join as soon as 2 player slots are occupied)
The ready state only serves as waiting time before the
game start.

### Request

``` text
READY
```

### Response

- `WAITING` : If the player is the first, he waits for
another player to join and be ready.
- `GAME START` : The game starts because two players are ready.

## Place a token in the grid

The client sends a column to the server indicating the column number
where he want to place his token.

### Request

``` text
PLACE <colomn number>
```

### Response

- `INVALID`      : The desired column is non-existent in the gameboard
- `COLUMN FULL`  : The desired column is already filled with tokens.
- `WIN`          : The placed token results in a Win of the player.
- `LOSE`         : The placed token results in a Loss of the player.
- `DRAW`         : The placed token fills the gameboard completely resulting in a draw.
- `TOKEN PLACED` : The placed token is placed propreply in the column,
the game continues.

## Forfeit

The client want to FF.

### Request

``` text
FF
```

### Response

- `LOSE` : A player declared forfeit and results in a Loss of the player.
- `WIN`  : A player declared forfeit and results in a Win of the player.

## Quit the server

### Request

``` text
QUIT
```

### Response

None.

## Invalid message

If the server receives an unknown message, it must send an error message to the client.

### Response

- `ERROR <code>` : an error occurred while sending the message. The error code is an integer between -1 and -1 inclusive. 
The error code is as follow:
  - -1: invalid message

# Section 4 - Examples