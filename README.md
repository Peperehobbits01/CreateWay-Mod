# CreateWay-Mod
The CreateWay mod is helping with seamless integration between mods inside the Creatway modpack and adds some usefull extra features for SMP's using the modpack to play.

On multiplayer servers, players can open the vote screen from the Eye of Ender button beside the recipe book in the survival inventory. Each ballot can include up to five dates between seven days and two months from today. Single-player worlds do not use the vote system. A date needs votes from at least four distinct players and at least 75% of players who logged in during the previous seven days before the vote ends. An operator can then set the opening time with `/setendhour HH:mm`. Players joining after the hour is set see the opening date and time in a title. Operators can use `/reopenvotes` to restart a completed poll; this clears the previous result and all votes while keeping player login history.

The server config is stored in `config/createwaymod-server.toml`. `disableAdvancementAnnouncements` defaults to `true`, disabling advancement chat announcements by default in new worlds; existing worlds retain their saved gamerule setting. `enableVoteSystem` defaults to `true` and can be set to `false` to disable the vote screen, voting, and vote commands. Restart the server after changing either option.

When More Babies is installed, an optional compatibility mixin uses the structure-generation random source for its Elder Guardian baby roll, avoiding cross-thread random access during C2ME chunk generation. More Babies is not required by this mod.
