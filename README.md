# Cincuentazo

Proyecto JavaFX para el juego **Cincuentazo**, desarrollado con arquitectura MVC, FXML, eventos de mouse y teclado, hilos para los turnos de la maquina, excepciones propias, Javadoc y pruebas unitarias con JUnit 5.

## Requisitos

- Java SE 17 o superior
- Maven 3.9 o superior
- IntelliJ IDEA recomendado
- Scene Builder opcional para editar `src/main/resources/edu/univalle/cincuentazo/view/game-view.fxml`

## Ejecutar

```bash
mvn clean javafx:run
```

En IntelliJ IDEA, ejecuta la clase:

```text
edu.univalle.cincuentazo.Launcher
```

No ejecutes directamente `CincuentazoApplication` si IntelliJ no configuro JavaFX, porque puede mostrar el error `JavaFX runtime components are missing`.

## Probar

```bash
mvn test
```

## Generar Javadoc

```bash
mvn javadoc:javadoc
```

El HTML queda en `target/site/apidocs/index.html`.

## Controles

- Seleccionar 1, 2 o 3 maquinas y presionar **Iniciar juego**.
- Click sobre una carta propia para jugarla.
- Si juegas un As y 1 o 10 son validos, la aplicacion pide elegir el valor.
- Teclas `1`, `2`, `3`, `4` para jugar una carta de la mano.
- Click en **Tomar carta** o tecla `Espacio` para tomar del mazo despues de jugar.
- Tecla `R` para reiniciar.

## Criterios cubiertos

- GUI en JavaFX + FXML con layouts `BorderPane`, `VBox`, `HBox`, `GridPane` y `StackPane`.
- MVC: modelo en `model`, controlador en `controller`, vista FXML/CSS en `resources`.
- Eventos: mouse para cartas, botones y mazo; teclado para atajos de juego.
- Interfaces: `Player`, `GameEventListener`, `CardSelectionListener`.
- Clases internas: manejadores dentro de `GameController`.
- Adaptadores: `GameEventAdapter` y `KeyboardShortcutAdapter`.
- Hilos: temporizador de jugada de maquina y temporizador de toma de carta.
- Excepciones propias marcadas y no marcadas.
- Tres clases de pruebas unitarias con JUnit 5.
