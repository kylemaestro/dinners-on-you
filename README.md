## About the game

This project is over 7 years old, and dates back to when I first started learning to program. The code is messy and unoptimized
at best, and hacky and cryptic at worst, but represents one of my first collaborative efforts to complete a full, playable game.

Two players use the same keyboard (WASD/numpad) to face off over a dinner bill dispute. Players must destroy the opponents base
while defending their own. Command a heavy-hitting Destroyer ship while laying mines to defend your territory. Loser gets stuck
with the bill, so fight with everything you've got!

The game runs in a Java applet, and with proper permissions modifications can still be ran today. My end goal for this project
is for a full rewrite in Java or perhaps a sequel of sorts in Unity. Nevertheless, I expect to commit a new branch with some
basic bugfixes and performance improvements, and maybe even a workaround for the applet issues the project faces today.

Credit to Cameron F. for major contributions to the game's vision and code.

-kda

---

## Play it in the browser

The original applet runs in a modern browser via [CheerpJ](https://cheerpj.com/) (a Java VM
compiled to WebAssembly).
### Run locally

You need a JDK (to build the jar) and Python (to serve the files).

```sh
# 1. Build the jar
javac --release 8 -encoding windows-1252 -d build *.java
jar cf Asteroids.jar -C build .

# 2. Serve the folder (CheerpJ won't run from file://)
python -m http.server 5500

# 3. Open http://127.0.0.1:5500/index.html
```

`index.html` loads CheerpJ and runs the game from `Asteroids.jar`.

### Deploy

Hosted from Github Pages

1. Make sure `index.html` and `Asteroids.jar` are committed to the branch you publish.
2. Repo **Settings → Pages**: Source = "Deploy from a branch", branch = `master`, folder = `/ (root)`.
3. In the same page, set **Custom domain** to `yourdomain.dev` and save.
4. At your DNS provider for `yourdomain.dev`, add a CNAME record:
   - **Name:** `dinners-on-you`
   - **Value:** `<your-github-username>.github.io`
5. Wait for DNS to propagate, then check **Enforce HTTPS**.
