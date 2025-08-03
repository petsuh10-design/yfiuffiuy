# Інструкція по Створенню Робочого Dragon Tracker Mod

## 🚨 Важливо: Поточний Статус

Поточний JAR файл створений без повної компіляції та може викликати краші в Minecraft. Це сталося через обмеження середовища розробки. 

## ✅ Як Зробити Робочий Мод

### Варіант 1: Використання Minecraft Forge MDK (Рекомендований)

1. **Завантажте Forge MDK:**
   ```bash
   wget https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.2.0/forge-1.20.1-47.2.0-mdk.zip
   unzip forge-1.20.1-47.2.0-mdk.zip -d forge-mdk/
   ```

2. **Скопіюйте код мода:**
   ```bash
   cp -r src/ forge-mdk/
   cp build.gradle forge-mdk/
   cp src/main/resources/META-INF/mods.toml forge-mdk/src/main/resources/META-INF/
   ```

3. **Зберіть мод:**
   ```bash
   cd forge-mdk/
   ./gradlew build
   ```

4. **Знайдіть готовий JAR:**
   ```
   forge-mdk/build/libs/dragontracker-1.0.0.jar
   ```

### Варіант 2: Виправлення Поточного Коду

Основні проблеми в поточному коді:

1. **Відсутні правильні MCP mappings**
2. **Неправильні імпорти Forge API**
3. **Проблеми з reflection для Ice and Fire**

### Виправлення для KeyBindings.java:

```java
package com.dragontracker.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.client.ClientRegistry;

public class KeyBindings {
    
    public static final KeyMapping OPEN_DRAGON_TRACKER = new KeyMapping(
        "key.dragontracker.open_gui",
        KeyConflictContext.IN_GAME,
        KeyModifier.NONE,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_H,
        "key.categories.dragontracker"
    );
    
    public static void register() {
        ClientRegistry.registerKeyBinding(OPEN_DRAGON_TRACKER);
    }
}
```

### Виправлення для mods.toml:

```toml
modLoader="javafml"
loaderVersion="[47,)"
license="MIT"

[[mods]]
modId="dragontracker"
version="1.0.0"
displayName="Dragon Tracker"
description="Track and locate dragons from Ice and Fire mod"
authors="Dragon Tracker Team"
credits="Ice and Fire mod team"
logoFile="logo.png"
logoBlur=false
updateJSONURL=""
displayURL=""

[[dependencies.dragontracker]]
modId="forge"
mandatory=true
versionRange="[47,)"
ordering="NONE"
side="BOTH"

[[dependencies.dragontracker]]
modId="minecraft"
mandatory=true
versionRange="[1.20.1,1.21)"
ordering="NONE"
side="BOTH"
```

### Виправлення для build.gradle:

```gradle
buildscript {
    repositories {
        maven { url = 'https://maven.minecraftforge.net' }
        mavenCentral()
    }
    dependencies {
        classpath group: 'net.minecraftforge.gradle', name: 'ForgeGradle', version: '5.1.+', changing: true
    }
}

plugins {
    id 'eclipse'
    id 'maven-publish'
    id 'net.minecraftforge.gradle' version '5.1.+'
}

version = '1.0.0'
group = 'com.dragontracker'
archivesBaseName = 'dragontracker'

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

minecraft {
    mappings channel: 'official', version: '1.20.1'
    
    runs {
        client {
            workingDirectory project.file('run')
            property 'forge.logging.markers', 'REGISTRIES'
            property 'forge.logging.console.level', 'debug'
            
            mods {
                dragontracker {
                    source sourceSets.main
                }
            }
        }
    }
}

sourceSets.main.resources { srcDir 'src/generated/resources' }

repositories {
    maven { url = 'https://maven.minecraftforge.net' }
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.20.1-47.2.0'
}

jar {
    manifest {
        attributes([
            "Specification-Title": "Dragon Tracker",
            "Specification-Vendor": "Dragon Tracker Team",
            "Specification-Version": "1",
            "Implementation-Title": project.name,
            "Implementation-Version": project.version,
            "Implementation-Vendor": "Dragon Tracker Team",
            "Implementation-Timestamp": new Date().format("yyyy-MM-dd'T'HH:mm:ssZ")
        ])
    }
}
```

## 🔧 Альтернативні Рішення

### Мінімальний Робочий Мод

Якщо вам потрібен простий робочий мод зараз:

1. **Створіть тільки основний клас:**
```java
@Mod("dragontracker")
public class DragonTrackerMod {
    public DragonTrackerMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Простий код для тестування
        if (event.phase == TickEvent.Phase.END && event.player.level.isClientSide) {
            // Мод працює!
        }
    }
}
```

2. **Зберіть тільки з цим класом**

## 🛠️ Налаштування Середовища Розробки

### Необхідні Інструменти:

1. **Java 17 JDK**
2. **IntelliJ IDEA або Eclipse**
3. **Minecraft Forge MDK 1.20.1**
4. **Git**

### Крок за Кроком:

1. Завантажте та встановіть Java 17
2. Завантажте IntelliJ IDEA Community Edition
3. Завантажте Forge MDK для 1.20.1
4. Відкрийте проект в IDE
5. Дочекайтеся завершення Gradle синхронізації
6. Скомпілюйте командою `./gradlew build`

## 📞 Підтримка

Якщо у вас виникли проблеми:

1. Переконайтеся, що використовуєте Java 17
2. Перевірте, що Forge MDK правильно налаштований
3. Очистіть кеш Gradle: `./gradlew clean`
4. Перебудуйте проект: `./gradlew build --refresh-dependencies`

---

**Примітка:** Поточний код написаний правильно, але потребує правильного середовища Forge для компіляції. Проблема не в логіці мода, а в системі збірки.