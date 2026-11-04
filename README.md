# Raccoon Coefficient

Number go up, brrr.

Raccoon Coefficient is a RuneLite plugin that turns the bonuses on your worn equipment into one Gear Score. Open the equipment stats screen and the score sits in the empty left side of the title bar. Hover an equippable item to see that item's score and how it compares with the piece it would replace.

## Wave 1 score

Ten attack or defence bonus points equal one Gear Score point. The other weights are:

| Stat | Gear Score per point |
| --- | --- |
| Attack bonuses, every style | 0.1 |
| Defence bonuses, every style | 0.1 |
| Melee strength | 1.0 |
| Ranged strength | 1.0 |
| Magic damage (%) | 1.0 |
| Prayer | 0.5 |

Negative bonuses stay negative. The total is shown to one decimal place. An item with no verified equipment data is left unknown instead of being counted as zero.

Wave 1 total = base equipment score + 0 set bonus + 0 weapon passive. Set effects and weapon passives are extension points only. They are not calculated yet.

## Run

```
./gradlew test
./gradlew run
```

`test` runs the scoring checks. `run` launches RuneLite with this plugin loaded in developer mode. The test suite does not log into the game.
