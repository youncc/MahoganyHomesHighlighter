# Mahogany Homes Highlighter

A [RuneLite](https://runelite.net/) Plugin Hub plugin that highlights Mahogany Homes contract objects with customizable colors for **remove**, **build**, and **repair** tasks.

This plugin focuses on visual overlays only. It does not track supplies, show teleports, or manage contracts — for that, see the separate [Mahogany Homes](https://runelite.net/plugin-hub/show/mahogany-homes) hub plugin.

## Features

- Highlights contract hotspots and furniture inside your active Mahogany Homes house
- Color-coded by task type:
  - **Red** — remove
  - **Green** — build
  - **Orange** — repair
- Detects your current contract from Amy/contract NPC dialog
- Configurable hull and clickbox rendering
- Adjustable highlight colors with alpha support

## Configuration

| Setting | Description |
|---------|-------------|
| Remove color | Highlight color for furniture that needs to be removed |
| Build color | Highlight color for hotspots and furniture that needs to be built |
| Repair color | Highlight color for furniture that needs to be repaired |
| Highlight hull | Draw a filled hull outline on objects |
| Highlight clickbox | Draw a hoverable clickbox overlay |

## Development

Requires Java 11+.

```bash
./gradlew run
```

This launches RuneLite in developer mode with the plugin loaded. See the [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) wiki page if you need to log in with a Jagex account.

## License

BSD 2-Clause License. See [LICENSE](LICENSE).
