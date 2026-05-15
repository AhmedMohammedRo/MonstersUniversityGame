<h1 align="center">🎓 Monsters University: The Board Game</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Language-Java_11%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/Framework-JavaFX-FF0000?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX" />
  <img src="https://img.shields.io/badge/Pattern-Clean_Architecture-blue?style=for-the-badge" alt="Architecture" />
  <img src="https://img.shields.io/badge/Status-Completed-success?style=for-the-badge" alt="Status" />
</p>

<br>

## 📖 About The Project

**Monsters University** is an interactive, turn-based desktop board game developed from scratch using **Java** and **JavaFX**. Designed with a strict adherence to Object-Oriented Programming (OOP) principles, the project features a robust and clean architecture that completely separates the core game engine logic from the graphical user interface.

Players select their favorite monsters and navigate through a dynamic, grid-based board filled with interactive doors, conveyor belts, and traps. Collect energy, use strategic powerups, and outsmart your opponent to be the first to reach the finish line!

---

## ✨ Key Features

- 🎮 **Dual Game Modes:**
  - 🤖 **1 Player (VS Computer):** Play against an automated AI opponent that handles its own dice rolls and movements perfectly.
  - 👥 **2 Players (Local Multiplayer):** Challenge a friend on the same screen with dynamic turn-switching and UI updates.
- 🎨 **Advanced GUI & Animations:** Smooth dice-rolling animations, dynamic status badges (Frozen, Shielded, Confused), and cinematic asynchronous overlays for in-game alerts.
- ⚙️ **Smart Board Mechanics:** Automated path detection for Transport Cells (Conveyor Belts & Contamination Socks) with distinct visual and audio cues.
- ⚡ **Unique Powerups:** Strategic character abilities requiring precise energy management.
- 🔊 **Immersive Audio & UX:** Interactive action logs, hover effects, and a responsive mute toggle for sound effects.

---

## 🏗️ Project Architecture

The project follows a strict separation of concerns to ensure maintainability, scalability, and testability:

- 🧠 **game.engine (Core Logic):** Contains the board generation, dice mechanics, exception handling, and state management. Fully decoupled from the visual representation.
- 🖥️ **game.gui (Visuals):** Handles all JavaFX visual components, transitions, event listeners, and user interactions.

---

## 🚀 How to Run

1. Clone the repository to your local machine using the following link:
   https://github.com/AhmedMohammedRo/MonstersUniversityGame.git

2. Open the project in your preferred IDE (e.g., Eclipse, IntelliJ IDEA).
3. Ensure the JavaFX SDK is properly added to your project's build path and VM options.
4. Run the Main.java class located in the game.gui package to launch the game.

---

## 👨‍💻 Core Team & Contributors

This project was brought to life by an incredible team of developers. A huge thanks to all contributors for their hard work and dedication:

| Name | GitHub |
|---|---|
| Ahmed Roshdy | [@AhmedMohammedRo](https://github.com/AhmedMohammedRo) |
| Omar Shaker | [@Omar-Shaker-Elbana](https://github.com/Omar-Shaker-Elbana) |
| Mark Fahim | [@mark1234720](https://github.com/mark1234720) |
| Karl Hany | [@karlhany222-spec](https://github.com/karlhany222-spec) |


<br>
<p align="center"><i>Developed as a milestone project for academic coursework - May 2026</i></p>
