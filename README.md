[![License: LGPL v3 or later](https://img.shields.io/badge/License-LGPL%20v3%2B-blue.svg)](https://github.com/Hand-Lock/flatter-signs/blob/HEAD/LICENSE)

# Flatter Signs: A Mode 13h Addon

*A Fabric mod for Minecraft 1.20.1 that adds **billboarding** to signs when paired with the **Mode 13h** shaderpack.*

> **Designed as an addon for [Mode 13h: MS-DOSify!](https://modrinth.com/project/mode-13h).**
> Without the shaderpack you *can* still use the mod, but signs will render as a **cross/hatch** model (like vanilla grass/flowers), because Mode 13h is what actually converts those models into billboards.

---

![An image of a flat billboarded sign glowing in the dark](https://cdn.modrinth.com/data/cached_images/56ed0ede7d0bc5eeac4616cccc75be612ad11756.png)

## 🧾 Why This Exists

Mode 13h already billboards several objects for that crunchy DOS look — but **signs are special**: the moment you billboard them, the *vanilla text rendering* becomes useless (no “real” front face to draw on).

So **Flatter Signs** solves the readability problem by:

* making signs **one-sided** (like pre-1.20 behavior, fitting the “no backside” billboard logic),
* and printing the sign’s content in chat with a vanilla-style prefix:
  `<Sign> Hello world!`

---

![Reading a sign prints the contents to chat](https://cdn.modrinth.com/data/cached_images/a7fcc943779139dacda09d7a1b9ff4e0c1b2c1d6.png)

## ✨ Features

### 📌 Billboard-ready sign models

* Standing signs are turned into a **cross/hatch** model so Mode 13h can billboard them.
* **All wood variants** supported.
* Works with **signs and hanging signs**.

### 💬 Chat reading (right click)

* **Right click** a sign to print its text in chat with a `<Sign>` prefix.
* **Shift + right click** to **edit** the sign (since normal right click is now “read”).

### 🎨 Vanilla mechanics preserved (and made visible)

These interactions affect both the sign itself *and* the chat output:

* **Dye** a sign → the text printed in chat matches the dyed color.
* **Glow ink sac** → sign **glows in the dark**.
* **Honeycomb (wax)** → sign becomes **non-editable** (even with Shift+RMB) but still readable/printable.

---

## 🪵 Resourcepack-friendly by design

* Signs use the **item sign texture**, so they stay compatible with **any resource pack** automatically.
* Wall signs are **procedurally generated** from the item texture:
  if your pack keeps the *general vanilla sign silhouette*, it should “just work” with no extra assets (otherwise, check **Configuration** below).

---

## ⚠️ Known compromise

### Wall Hanging Signs

**Wall hanging signs are flat, not billboarded.**
Their shape makes true billboarding impractical, so this is the best balance between:

* readability,
* visual consistency,
* and not breaking the model into something uncanny.

---

## ⚙️ Configuration

A simple **`.json` config** lets you enable/disable each feature independently (billboard model, chat printing, one-sided behavior, shift-to-edit logic, dye/glow/wax behavior, hanging sign support, etc.).

* Location: `config/flattersigns.json` (generated after first launch)
* Use case: keep only the bits you want if you’re *not* using Mode 13h.

* Resource packs with non-vanilla sign silhouettes may need:
    * `wall_sign_texture_crop_height` (1..16) — visible pixel height taken from the item texture.
    * `wall_sign_texture_crop_offset` (0..16, clamped) — vertical offset into the item texture before cropping.

---

## 🧩 Compatibility

| Platform                        |  Minecraft | Works? | Notes                                                     |
| ------------------------------- | ---------: | :----: | --------------------------------------------------------- |
| **Fabric**                      | **1.20.1** |    ✅   | Native target                                             |
| **Forge**                       | **1.20.1** |    ✅   | Via **Sinytra Connector** + **Forgified Fabric API**      |
| **Without Mode 13h shaderpack** |     1.20.1 |   ✅*   | *No billboard effect; signs appear as cross/hatch models* |

---

## ✅ Quick usage cheat sheet

* **Read sign:** Right click
* **Edit sign:** Shift + right click
* **Dye / glow / wax:** Same items, same behavior — now reflected in chat readability too

---

## 🖇️ Credits

* **Mod & idea:** *HandLock_*
* **Made for:** [Mode 13h: MS-DOSify!](https://modrinth.com/project/mode-13h) shaderpack

> *“No backside, eyes up here!”*
