# Cat Food Energy System

Cat Food is ObeseCat's player-facing energy unit. Internally it uses NeoForge's
standard Forge Energy capability so compatible mods can transfer the energy.

## Conversion

- 1 Cat Food point (CF) = 100 FE
- Plutonium Cat Food = 25 CF = 2,500 FE
- Lithium Deuteride Cat Food = 100 CF = 10,000 FE

`CatFoodEnergy` is the single source of truth for these values.

## Fat Man

Fat Man stores up to 500 CF and accepts FE through the NeoForge entity energy
capability. The capability is input-only: external systems can charge Fat Man,
but cannot drain him.

Players can also feed the two nuclear cat food items directly to Fat Man. At
300 CF he begins the existing ten-second nuclear countdown. A Cat Food-powered
detonation uses the full lithium-scale crater, creates Trinitite, and triggers
the Atomic Fire Sphere.

Legacy saves containing the old reactive-meal counter are migrated at 100 CF
per old meal, preserving the old three-meal detonation threshold.

## Atomic Can Opener

The Atomic Can Opener is an early-access Cat Food generator whose real progression gate is obtaining nuclear Cat Food:

- Capacity: 1,000 CF / 100,000 FE
- Output: up to 1,000 FE per tick in total
- Processing time: 40 ticks per Cat Food item
- Input: one Cat Food-only inventory slot
- FE capability: output-only

Right-click the machine to open its cyan generator interface. The screen shows
the Cat Food input, processing progress, CF/FE buffer, and six side controls.
The GUI has independent `ITEM` and `POWER` configuration layers. Every
world direction can enable or disable Cat Food input and FE output separately,
so one face can support either function, both functions, or neither. The top
defaults to item input and all six faces default to power output. Item pipes can
insert only through enabled item faces; machines and cables can draw energy only
from enabled power faces. Total export is capped at 1,000 FE per tick across
every face.

The intentionally accessible recipe uses iron ingots, redstone, and a
stonecutter. Progression is fuel-gated instead: Lithium Deuteride Cat Food is
found in End City treasure, rewarding players who successfully rush the End.

The `obesecat:can_opener` registry id remains unchanged for world compatibility;
its player-facing name is Atomic Can Opener. Its flavor text is
`Harness The Power`.


## Food Bin

The Food Bin is the standard Cat Food/FE battery and compatibility test block.
It stores 1,000,000 FE and transfers up to 10,000 FE per operation. Every face
exposes the standard NeoForge energy capability for both input and output, so it
can sit between the Atomic Can Opener, Pipez energy pipes, RFTools cells, and
future ObeseCat cables or machines without side configuration.

Its shaped recipe uses exactly four Trinitite and four iron ingots.

## Cat Charger

The Cat Charger is an orange cat-themed FE charging machine. It accepts up to
10,000 FE per operation from every face, buffers 500,000 FE, and transfers 200
FE per tick into the compatible FE item in its single GUI slot. Its recipe uses
one redstone block, four Trinitite, and two iron blocks.

## Portable Cat Charger

The Portable Cat Charger is a 500,000 FE inventory battery. Right-clicking it
toggles charging and its enchantment glint indicates the active state. While
active anywhere in the player's inventory, it charges compatible main-hand and
off-hand FE items at up to 1,000 FE per tick. Its recipe uses one redstone dust,
four Trinitite, and two iron ingots.

## Creative Energy Tools

The Creative Food Bin and Creative Portable Cat Charger are dev-only creative
tab tools with no recipes. Both expose non-depleting FE sources and report
`Integer.MAX_VALUE` capacity. The Creative Food Bin accepts and supplies any
requested FE amount from every face. The Creative Portable Cat Charger uses the
normal activation workflow but offers effectively instant held-item charging.
## Extension rule

Future magical items should use the same conversion helpers and FE capability,
while choosing input/output permissions per object. This keeps Cat Food
interoperable without making every powered item an unrestricted battery.
## Chargeable cast items

Final Fantasy-inspired skill and summon items expose receive-only FE buffers. They can be charged by the Cat Charger or Portable Cat Charger, but cannot discharge into machines.

- Holy Sword skills: 2,500 FE per cast, 25,000 FE capacity (10 casts)
- Mighty Sword skills: 3,500 FE per cast, 35,000 FE capacity (10 casts)
- Dark Sword skills: 5,000 FE per cast, 50,000 FE capacity (10 casts)
- Magicite summons: 10,000 FE per cast, 100,000 FE capacity (10 casts)

Energy is consumed only after a sword skill successfully begins. Summons require a valid block target before energy is consumed.
