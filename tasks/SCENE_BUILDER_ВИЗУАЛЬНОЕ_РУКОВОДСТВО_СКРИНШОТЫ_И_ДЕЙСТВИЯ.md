# 🎨 Пошаговое визуальное руководство: Работа в Scene Builder без написания FXML-кода

Данное руководство создано для того, чтобы создавать интерфейсы в **Scene Builder** исключительно визуально (мышкой), без необходимости вручную писать или редактировать XML-код.

---

## 🖼 Графические карточки-инструкции (Скриншоты)

Все сгенерированные визуальные карточки с подсветкой действий находятся в папке:  
📂 [`C:/Users/dEN5/screenshots/`](file:///C:/Users/dEN5/screenshots/)

1. **[Карточка 1: Общий обзор рабочих панелей](file:///C:/Users/dEN5/screenshots/01_scene_builder_overview.png)**  
   - Расположение панелей: *Library (слева)*, *Document/Hierarchy (слева внизу)*, *Canvas (по центру)*, *Inspector (справа)*.
2. **[Карточка 2: Настройка вкладки CODE в Inspector](file:///C:/Users/dEN5/screenshots/02_inspector_code_binding.png)**  
   - Как привязать `Controller class`, назначить переменные `fx:id` и методы нажатия `On Action` (`#btnClick`).
3. **[Карточка 3: Создание сетки 3х3 для Крестиков-Ноликов](file:///C:/Users/dEN5/screenshots/03_tictactoe_gridpane_setup.png)**  
   - Добавление `GridPane`, вставка 9 кнопок, установка шрифтов и единого обработчика.
4. **[Карточка 4: Секретная автогенерация кода Java](file:///C:/Users/dEN5/screenshots/04_controller_skeleton_export.png)**  
   - Экспорт готового Java-контроллера через меню `View -> Show Sample Controller Skeleton`.

---

## 🚀 Как открыть Scene Builder для любого проекта в 1 клик

В каждом из проектов лежит скрипт `tools/open-scenebuilder.bat`.  
Вы также можете выполнить команду в PowerShell:

```powershell
# Для проекта #3 (Крестики-Нолики):
& "$env:LOCALAPPDATA\SceneBuilder\SceneBuilder.exe" "C:\Users\dEN5\Разработка игры на Java - #3 Создание игры Крестики Нолики\src\com\example\MainView.fxml"

# Для проекта #4-7 (2D Игра):
& "$env:LOCALAPPDATA\SceneBuilder\SceneBuilder.exe" "C:\Users\dEN5\Разработка игры на Java - #7 Отслеживание соприкосновений (Коллизии)\src\com\example\MainView.fxml"
```

---

## 🧭 Пошаговый атлас: куда кликать в Scene Builder

### Шаг 1. Создание холста (Canvas)
1. В левой панели **Library** разверните секцию **Containers**.
2. Зажмите мышкой **`AnchorPane`** и перетащите на пустой центральный экран.
3. В правой панели **Inspector** откройте вкладку **Layout** и задайте размеры (например, `Pref Width: 712`, `Pref Height: 400`).

---

### Шаг 2. Привязка Java-класса контроллера (Критически важно!)
1. В левом нижнем углу найдите аккордеон **`Controller`** (под деревом *Hierarchy*).
2. Разверните его и в строке **`Controller class`** введите:
   ```
   com.example.MainController
   ```
3. *Теперь Scene Builder знает, в какой Java-класс отправлять события!*

---

### Шаг 3. Добавление визуальных элементов (Controls)
1. В панели **Library** разверните **Controls**:
   - **`Button`** — кнопки.
   - **`Label`** — текстовые надписи (счет, пауза, проигрыш).
   - **`ImageView`** — спрайты и фоновые картинки.
2. Перетащите элемент на холст в нужную позицию.
3. Для **`ImageView`**:
   - Выделите его на холсте.
   - В панели **Inspector $\rightarrow$ Properties $\rightarrow$ Image** нажмите на `...` и выберите картинку из папки `src/com/example/images/` (например, `bg.png` или `player.png`).

---

### Шаг 4. Настройка связи с Java-кодом (Inspector $\rightarrow$ CODE)
Это главная вкладка, которая заменяет написание кода:

| Что хотите сделать | Куда нажать в Scene Builder | Что ввести |
|---|---|---|
| **Дать имя элементу** (чтобы управлять им из Java) | Inspector $\rightarrow$ вкладка **Code** $\rightarrow$ поле **`fx:id`** | `player` или `enemy` или `labelPause` |
| **Связать клик по кнопке с методом** | Inspector $\rightarrow$ вкладка **Code** $\rightarrow$ поле **`On Action`** | `#btnClick` или `#onHelloButtonClick` |

> 💡 **Правило:** Имя метода в поле `On Action` всегда пишется со знаком решётки `#` в начале (например, `#btnClick`).

---

### Шаг 5. Автоматическое получение готового Java-кода
Вам даже не нужно вручную писать аннотации `@FXML` в Java:
1. В верхнем меню нажмите: **`View` $\rightarrow$ `Show Sample Controller Skeleton`**.
2. В появившемся окне поставьте галочку **`[X] Full`**.
3. Нажмите **`Copy`** в правом нижнем углу.
4. Откройте файл `MainController.java` в вашей среде разработки и вставьте скопированный код!
