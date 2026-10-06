# Шахматы с жизнями — 0.2 beta

Офлайн-приложение для двух игроков на одном Android-устройстве. Приложение
поддерживает несколько королей, обязательное взятие после превращения в короля,
вертикальную рокировку, редактор позиции и заявления о ничьей, в том числе по
двум несоседним голым королям. В режиме против компьютера настраиваются сторона,
бюджет поиска (1 000 / 2 000 / 5 000 / 10 000 / 25 000 / 50 000 узлов) и
отображение оценки. Обычный уровень использует те же 10 000 узлов на решение,
что и плейтесты. Есть русская и английская локализации и экран «О программе» с
атрибуцией движка. Бой короля не ограничивает обычные ходы. Минимальная версия
Android — 4.4 (API 19).

## Сборка

Нужны JDK 17 и Android SDK с платформой API 35. В Windows задайте `JAVA_HOME` и
`ANDROID_HOME` (или `ANDROID_SDK_ROOT`), затем из этой папки запустите:

```bat
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
gradlew.bat copyLifeChessApk
```

Unit-тесты движка расположены в `app/src/test`. Debug APK создаётся в
`app/build/outputs/apk/debug/app-debug.apk`.

Release-сборка без локальных ключей создаётся неподписанной. Чтобы собрать
подписанный APK, сначала создайте собственный ключ (не используйте ключ,
опубликованный в прежней версии этого репозитория):

```bat
mkdir signing
keytool -genkeypair -v -keystore signing\lifeschess-release.jks -alias lifeschess -keyalg RSA -keysize 2048 -validity 10000
```

Создайте `signing/signing.properties` со своими секретами:

```properties
storeFile=lifeschess-release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=lifeschess
keyPassword=YOUR_KEY_PASSWORD
```

Папка `signing` игнорируется Git. Храните новый ключ и пароли отдельно в
надёжном месте: для выпуска обновлений понадобится тот же ключ. Вместо файла
`signing.properties` можно передать секреты через переменные окружения текущей
сессии `cmd.exe`:

```bat
set LIFECHESS_RELEASE_KEYSTORE=signing\lifeschess-release.jks
set LIFECHESS_RELEASE_KEY_ALIAS=lifeschess
set LIFECHESS_RELEASE_STORE_PASSWORD=YOUR_STORE_PASSWORD
set LIFECHESS_RELEASE_KEY_PASSWORD=YOUR_KEY_PASSWORD
```

Затем выполните:

```bat
gradlew.bat copyLifeChessApk
```

Итоговый файл появится в корне проекта как `LifeChess.apk`. Не публикуйте этот
APK, keystore или файл `signing.properties` в Git.

Ключ подписи, который ранее находился в публичной истории репозитория, следует
считать скомпрометированным. Он удалён из истории ветки `main`; используйте
новый ключ для будущих сборок. GitHub может временно сохранять кеши старых
объектов.

## Установка

Подключите Android-устройство с включённой отладкой по USB, затем установите
debug-сборку командой:

```bat
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Для финальной установки можно перенести `LifeChess.apk` на устройство и открыть
его файловым менеджером. Разрешение на установку из неизвестного источника
может потребоваться включить в настройках Android.

## Консольный вариант Fairy-Stockfish

Инструкции по сборке и компьютерной проверке варианта, JSON-контракту v2 и
ограничениям интеграции находятся в [docs/lifechess-engine.md](docs/lifechess-engine.md).
Полный тестовый снимок начальной позиции находится в
`tools/fixtures/lifechess-initial.jsonl`; JSON Lines-адаптер запускается через
`tools/lifechess_console.py` с путём к собранному `stockfish.exe`.

## English

**Chess with Life** is an offline Android chess variant for two people sharing
one device, with an optional built-in Fairy-Stockfish opponent. It keeps
ordinary piece movement while changing the role of kings: kings may be
captured, their safety never restricts a move, and victory comes from taking
every opponent king. The app includes a position editor, draw claims, three
castling types, configurable engine search budgets, optional position
evaluation, and a Russian/English interface. It requires Android 4.4 (API 19)
or newer and needs no account, network connection, or permissions.

### Features

- Local two-player games and games against the adapted Fairy-Stockfish engine.
- Search budgets of 1,000, 2,000, 5,000, 10,000, 25,000, or 50,000 nodes per
  engine decision. These are not Elo ratings.
- Optional evaluation from White's perspective, disabled by default.
- Russian and English interface; change language from the main menu.
- Position editor, promotion choices, mandatory king-capture debt, and draw
  claims for repetition, the 50-move rule, agreement, and non-adjacent kings.
- An About screen credits Fairy-Stockfish and links to the adapted fork and its
  GPL-3.0 license.
- Offline operation; game state stays on the device.

### Build and test

Use JDK 17 and Android SDK Platform 35. On Windows, set `JAVA_HOME` and
`ANDROID_HOME` (or `ANDROID_SDK_ROOT`), then run:

```bat
gradlew.bat :app:testDebugUnitTest
gradlew.bat :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
Engine integration and console-protocol checks are documented in
[`docs/lifechess-engine.md`](docs/lifechess-engine.md). The authoritative
Russian rules and implementation profile are in
[`docs/lifechess-rules.md`](docs/lifechess-rules.md).

### Rules in brief

The full rules are included in the app and in the
[0.2 beta release notes](docs/release-0.2-beta.md). In short: kings can be
captured, and check, checkmate, or stalemate does not restrict play. A player
wins immediately by taking the opponent's last king. A king may capture a
friendly piece, including another king; only kings can capture friendly
pieces. Promoting to a king may create a debt requiring the next player to
capture any king of that side. Standard and vertical castling do not check
attacked squares. Repetition, 50 moves, and the bare-kings condition are claims,
not automatic draws; an agreed draw requires an offer and acceptance.
