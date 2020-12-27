# DiscordPlays

'Twitch Plays' inspired application that allows discord servers to play together in a fun and quirky way.

- Note: Application was designed with Nintendo GBA/NDS systems in mind, more specifically using the Desmume emulator.

## WARNING: Spamming reacts too fast will likely get you ratelimited!

## How to:

### Prerequisites

- [Java 8+](https://jdk.java.net/8/)
- [Discord Server](https://discord.com/) (Channel Id)
- [Discord Bot Account](https://discord.com/developers/applications) (Token)

### Getting Started

It is highly recommended you set up the bot in a text channel on its own for everything to work as expected.

#### Setup
1) Join bot account to the server of your choice.
2) Download `DiscordPlays.jar` from this repository.
3) Obtain `bot_token` & discord text `channel_id`.
4) Open terminal, type: `java -jar DiscordPlays.jar bot_token channel_id`
5) In chosen channel (`channel_id`) type `?setupController` to set up controls.
6) Open and focus (click on) on program/emulator of choice.

#### Note
[DeSmuME](http://desmume.org/) was used in development and testing, so is guaranteed to work with the following controller layout.

![](https://i.imgur.com/yKijjQ1.png)
