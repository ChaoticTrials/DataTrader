# Data Trader

[![Curseforge](http://cf.way2muchnoise.eu/versions/For%20MC_650573_all.svg)](https://www.curseforge.com/minecraft/mc-mods/data-trader)
[![CurseForge](http://cf.way2muchnoise.eu/full_650573_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/data-trader)

[![Modrinth](https://img.shields.io/modrinth/game-versions/No1xZeN1?color=00AF5C&label=modrinth&logo=modrinth)](https://modrinth.com/mod/data-trader)
[![Modrinth](https://img.shields.io/modrinth/dt/No1xZeN1?color=00AF5C&logo=modrinth)](https://modrinth.com/mod/data-trader)

A mod which adds a new trader to the game.

## Defining the trades
### File structure
The trader can receive an ID for a trader offers "recipe". This is a list of single `trader offer`s. An example
file could look like this:

```json
{
  "Offers": [
    {
      "buy": {
        "tag": "forge:gems/diamond",
        "count": 3
      },
      "buyB": {
        "item": "minecraft:wooden_pickaxe"
      },
      "sell": {
        "item": "minecraft:diamond_pickaxe",
        "nbt": "{Damage:0,Enchantments:[{id:\"minecraft:efficiency\",lvl:2},{id:\"minecraft:unbreaking\", lvl:10}]}"
      },
      "rewardExp": false,
      "xp": 0
    }
  ]
}
```

This example includes only one merchant offer. The trader will sell the item in `sell`.

The player needs to provide the item in `buy` and `buyB` to receive the item.
`maxUses` is the amount of times the player can use this trade before the trader needs to restock.

If `rewardExp` is true, the player will receive xp points for each trade, amount defined in `xp`.

<br>
The following values are required:

- `buy`
- `sell`

The default values are:

|        Name         |  Default value  |
|:-------------------:|:---------------:|
|       `buyB`        |       Air       |
|     `rewardExp`     |      false      |
|        `xp`         |        0        |

### Where to put it in?
You use a data pack to provide these files. These are located at `<modid>/trader_offers/`. An example can be found
[here](src/main/resources/data/datatrader/trader_offers/).

### How to use?
You spawn the trader using the `/summon` command, or by using the spawn egg. After this, you use the command
`/datatrader setOffer @e <modid>:<path>` to set the recipe. This can also be done by datapacks.
For a normal trader, I recommend setting `NoAI` to `true`.

You can also summon a trader with a specific trading table by using the
command `/datatrader summon ~ ~ ~ <offer_id> <NoAI>`

### Setting custom skin overlay
Just put a texture to `<modid>:textures/entity/trader/<offer_id path>.png`. This texture should be something like the
profession overlay texture, e.g. `minecraft:textures/entity/villager/profession/cleric.png`.