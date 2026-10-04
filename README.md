# Шахматы с жизнями

Офлайн-приложение для двух игроков на одном Android-устройстве. Приложение
поддерживает несколько королей, обязательное взятие после превращения в короля,
вертикальную рокировку, редактор позиции и заявления о ничьей. Бой короля не
ограничивает обычные ходы. Минимальная версия Android — 4.4 (API 19).

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
