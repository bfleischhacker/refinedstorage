# JEI missing-ingredient requests for Refined Storage 1.12.2

Based on `forresthopkinsa/refinedstorage` release `v1.6.16.h20a`, commit
`d17b9f278c5c3e11005edd1106712f4045f28bca`. Development branch:
`feature/jei-missing-ingredient-autocrafting`.

## Use

Open a crafting recipe from an RS crafting grid. Hover JEI's plus button to see
the existing red missing-input and blue craftable-input highlights. Use the
same request modifiers as the RS grid: **Ctrl+Shift+left-click the plus**.

The review list groups missing inputs by the available RS pattern output.
Quantities cover **one recipe's matrix fill**, subtracting network, crafting
matrix and player-inventory stock. Shift is part of the request gesture; it
does not request a stack of recipes. Select each ingredient to adjust its
quantity, inspect RS's normal resource preview, then press Start. Even with
Shift held, this flow always shows the resource preview. Cancel submits
nothing. Confirmed entries disappear from the list; Done returns to the grid.
After crafting finishes, transfer the recipe again normally.

Red inputs still need manual supply. Blue inputs can be requested even when
some red inputs remain. Pattern-output selection respects JEI alternatives,
NBT and the existing server's metadata/durability comparison. Overlapping
substitutes are allocated without counting one stored item twice. If multiple
pattern outputs are valid substitutes, any matching output may be selected;
the item's name and requested quantity are visible before confirmation.

The client estimate cannot know whether the network can clear the existing
matrix, nor reserve stock against other users or pending crafting tasks.
The unchanged server revalidates resources and AUTOCRAFTING permission when
each request is submitted. Entries are removed after submission, not a server
success acknowledgement. Reopen the recipe review if resources changed and a
submission was rejected. Deliberately repeating a new confirmed request can
create another job, just as requesting from the normal grid can.

## Why an input hook is needed

JEI 4.14.3.242 disables its plus button for USER_FACING transfer errors. Its
later COSMETIC error type did not exist yet. The bridge retains the error and
uses a Forge mouse-input hook only while that recipe's plus tooltip is drawn,
only within the plus button's actual bounds, and only with RS request modifiers
and left-button-down. It consumes that event once, verifies the same open RS
container and crafting permission, and recomputes the missing-input plan.
Moving to another screen or drawing another frame clears the prior hover.

Upstream hardcodes Shift+Ctrl in `GuiGrid`; `RSKeyBindings` does not define a
request-crafting binding. `CraftingRequestGesture.isActive()` now centralizes
that same predicate for both grid and JEI. It does not introduce a new binding.
The reflection calls (`getParentScreen`, `getRecipeTransferButton`) were checked
against the installed JEI jar. No new JEI implementation class is linked.

No recipes, costs, machinery, network packets, storage operations, crafting
engine or server permission rules are replaced. The only general preview
change prevents multiple submissions from one confirmation screen.

## Build and verification

Use a Java 8 **JDK**, independently of the game's chosen Java runtime. For this
checkout the isolated JDK is `.toolchain/jdk8u504-b01`. Gradle 4.2.1 and
ForgeGradle 2.3 are retained; obsolete repository URLs now use HTTPS.

```powershell
$env:JAVA_HOME = "$PWD/.toolchain/jdk8u504-b01"
$env:GRADLE_USER_HOME = "$PWD/.toolchain/gradle-home"
./gradlew.bat setupDecompWorkspace --no-daemon -x getAssets
./gradlew.bat test build --no-daemon -x getAssets
```

The distributable is `build/libs/refinedstorage-1.6.16.h20a-jei.1.jar`.
Do not install the `api`, `deobf` or `sources` jars. The build targets Java 8
bytecode and performs Forge reobfuscation. The upstream SemVer and legacy
ForgeGradle warnings are expected.

14 regression tests pass: stock deficits and repeated inputs, network/matrix/
inventory capacities, alternative ingredients, mixed red/blue inputs, zero-stock
craftable displays, overlapping substitutions, and 500 generated inventories
checked against exhaustive allocation. Minecraft-backed tests exercise actual
RS NBT, metadata, wildcard and durability comparisons. Test reports are in
`build/reports/tests/test/index.html`. Server permission/resource enforcement
is preserved by routing through the existing request and preview packets.

These checks do not establish in-game startup or interaction success. Before
general use, verify in the isolated Ultimate Alchemy test instance:

1. Save and fully exit; back up saves and the current h20a jar.
2. Replace only the RS jar in the **test** instance, retaining addons and JEI.
3. Check startup, ordinary transfer and pattern-grid behavior.
4. Test one and multiple missing patterned inputs, repeated inputs with partial
   stock, substitutions and NBT variants; verify quantities in each preview.
5. Test cancel, held modifiers, rapid repeated clicks and Enter, red uncraftable
   inputs, insufficient crafting resources, and a player without autocrafting
   permission. Confirm tasks appear only after Start.
6. Confirm red/blue highlights remain and the same modifiers request crafts in
   the normal grid. Requesting from a filtered search must use all network stock.

Never change the original Ultimate Alchemy instance or copy test saves back.
Rollback: exit the test instance, remove the `h20a-jei.1` jar and restore the
saved `refinedstorage-1.6.16.h20a.jar`. No world migration is performed.
