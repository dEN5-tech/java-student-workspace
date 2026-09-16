# ☕ Готовый воркспейс для изучения Java (Консоль, Интерактивность и Русификация)

Этот воркспейс полностью настроен для русскоязычных студентов: запуск в 1 клик, полная поддержка русского языка и кодировки UTF-8 (без `????` и кракозябр), автосохранение и интерактивный ввод (`Scanner`).

---

## 🇷🇺 Что русифицировано:
1. **Языковой пакет VS Code / Codium**: Добавлен `Russian Language Pack` (`ms-ceintl.vscode-language-pack-ru`).
2. **Кодировка UTF-8**: Во всех сценариях запуска (F5, `Ctrl+Shift+B`, Code Runner, Терминал) вшиты флаги `-Dfile.encoding=UTF-8` и локаль `ru_RU.UTF-8`.
3. **Корректный ввод и вывод кириллицы**: `System.out.println("Привет")` и `Scanner.nextLine()` отображаются без искажений.

---

## ⚡ Как запускать код (3 способа):

| Способ | Горячая клавиша | Описание |
| :--- | :--- | :--- |
| **Быстрый запуск** | **`Ctrl + Shift + B`** | Мгновенно запускает активный файл в терминале с поддержкой русского текста. |
| **Отладка / Запуск** | **`F5`** | Запускает код через Java Debugger с поддержкой точек останова (Breakpoints). |
| **Кнопка Play** | Нажать **▶** вверху справа | Запускает файл через Code Runner сразу в интерактивном терминале. |

---

## 📂 Структура файлов:

- [Main.java](file:///home/den5/Projects/java-student-workspace/Main.java) — Стартовый файл с интерактивным вводом имени, возраста и любимой оценки.
- [src/GuessGame.java](file:///home/den5/Projects/java-student-workspace/src/GuessGame.java) — Пример мини-игры «Угадай число» на русском языке.
- [.vscode/launch.json](file:///home/den5/Projects/java-student-workspace/.vscode/launch.json) — Запуск по F5 с русским UTF-8.
- [.vscode/tasks.json](file:///home/den5/Projects/java-student-workspace/.vscode/tasks.json) — Запуск по Ctrl+Shift+B с `-Dfile.encoding=UTF-8`.
- [.vscode/settings.json](file:///home/den5/Projects/java-student-workspace/.vscode/settings.json) — Настройки UTF-8, терминала и языка.
- [.vscode/extensions.json](file:///home/den5/Projects/java-student-workspace/.vscode/extensions.json) — Русский языковой пакет и Java расширения.

---

## 💡 Полезные советы для студентов:
1. **Как создавать новые задачи?**  
   Создайте любой `.java` файл, напишите класс и нажмите **`Ctrl+Shift+B`**.
2. **Scanner(System.in)**:  
   Вводите текст и числа прямо в нижнем окне «Терминал» и нажимайте `Enter`.
3. **Автосохранение**:  
   Включено автоматически через 1 секунду после окончания набора текста.
