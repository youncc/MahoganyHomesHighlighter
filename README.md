# Mahogany Homes Highlighter

A [RuneLite](https://runelite.net/) Plugin Hub plugin that highlights Mahogany Homes contract objects with customizable colors for **remove**, **build**, and **repair** tasks.

This plugin focuses on visual overlays only. It does not track supplies, show teleports, or manage contracts — for that, see the separate [Mahogany Homes](https://runelite.net/plugin-hub/show/mahogany-homes) hub plugin.

## Features

### Furniture highlighting

- Highlights contract hotspots and furniture inside your active Mahogany Homes house
- Color-coded by task type:
  - **Red** — remove
  - **Green** — build
  - **Orange** — repair
- Optional **Remove**, **Build**, or **Repair** labels on furniture

### Door highlighting

- Highlights doors inside your current contract house
- Separate colors for **open** and **closed** doors
- Optional **Open** / **Closed** status text

### Stair and ladder highlighting

- Highlights stairs and ladders that lead toward remaining contract tasks on other floors
- Shows task counts on relevant stairs (for example, `2 tasks upstairs`)
- When all tasks are done, points you toward the homeowner with a **Speak to** hint on the correct floor

### Homeowner highlighting

- Highlights the contract homeowner when all tasks are complete so you can turn in the contract

### Contract detection

- Detects your current contract from Amy/contract NPC dialog
- Remembers your active home per RuneScape profile
- Can import the current home from the [Mahogany Homes](https://runelite.net/plugin-hub/show/mahogany-homes) plugin if you already use it
- Auto-detects your contract when you enter a house with active work

### Render options

- Hull, 3D model outline, clickbox, and tile highlighting
- Adjustable border width, outline feather, and fill opacity
- Customizable clickbox border colors
- Configurable position for status text (above, on, or below objects)

## Configuration

### Furniture highlighting

| Setting | Description |
|---------|-------------|
| Highlight furniture | Toggle furniture and hotspot highlighting |
| Remove color | Highlight color for furniture that needs to be removed |
| Build color | Highlight color for hotspots and furniture that needs to be built |
| Repair color | Highlight color for furniture that needs to be repaired |
| Show action | Show Remove, Build, or Repair text on contract furniture |

### Door highlighting

| Setting | Description |
|---------|-------------|
| Highlight doors | Toggle door highlighting in your contract house |
| Open door color | Highlight color for open doors |
| Closed door color | Highlight color for closed doors |
| Show door status | Show Open or Closed text on doors |

### Stair highlighting

| Setting | Description |
|---------|-------------|
| Highlight stairs | Toggle stair and ladder highlighting |
| Stair color | Highlight color for stairs and ladders |
| Show stair status | Show task counts or turn-in hints on relevant stairs |

### Homeowner highlighting

| Setting | Description |
|---------|-------------|
| Highlight homeowner | Toggle homeowner highlighting when the contract is complete |
| Homeowner color | Highlight color for the contract homeowner |

### Render options

| Setting | Description |
|---------|-------------|
| Highlight hull | Draw a filled hull outline on objects |
| Highlight outline | Draw a 3D model outline |
| Highlight clickbox | Draw a hoverable clickbox overlay |
| Highlight tile | Highlight the tile under the object |
| Border width | Width of highlighted borders |
| Outline feather | How much of the model outline is faded (0–4) |
| Fill opacity | Opacity of hull and NPC highlight fills |
| Clickbox border color | Border color for highlighted clickboxes |
| Clickbox hover border color | Border color when a clickbox is hovered |
| Status text position | Where status text is drawn (above, on, or below objects) |

## Development

Requires Java 11+.

```bash
./gradlew run
```

This launches RuneLite in developer mode with the plugin loaded. See the [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) wiki page if you need to log in with a Jagex account.

## License

BSD 2-Clause License. See [LICENSE](LICENSE).
