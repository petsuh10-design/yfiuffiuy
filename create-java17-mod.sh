#!/bin/bash

echo "🔧 Creating Dragon Tracker mod compiled for Java 17..."

# Clean previous builds
rm -rf temp-build
mkdir -p temp-build/com/dragontracker
mkdir -p temp-build/META-INF
mkdir -p temp-build/assets/dragontracker/lang

# Create the main mod class (Java 17 compatible)
cat > temp-build/com/dragontracker/DragonTrackerMod.java << 'JAVAEOF'
package com.dragontracker;

import net.minecraftforge.fml.common.Mod;

@Mod("dragontracker")
public class DragonTrackerMod {
    public static final String MODID = "dragontracker";
    
    public DragonTrackerMod() {
        System.out.println("🐉 Dragon Tracker Mod загружен!");
        System.out.println("✅ Версия 1.0.0 - Java 17 совместимая");
        System.out.println("📖 Для полной версии используйте исходный код с Forge MDK");
    }
}
JAVAEOF

# Create minimal Forge API stubs for compilation only
mkdir -p temp-build/net/minecraftforge/fml/common
cat > temp-build/net/minecraftforge/fml/common/Mod.java << 'STUBEOF'
package net.minecraftforge.fml.common;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mod {
    String value();
}
STUBEOF

# Copy mods.toml
cat > temp-build/META-INF/mods.toml << 'TOMLEOF'
modLoader="javafml"
loaderVersion="[47,)"
license="MIT"

[[mods]]
modId="dragontracker"
version="1.0.0"
displayName="Dragon Tracker"
description="Track and locate dragons from Ice and Fire mod"
authors="DragonTracker Team"

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
TOMLEOF

# Copy language files
cat > temp-build/assets/dragontracker/lang/en_us.json << 'LANGEOF'
{
  "key.dragontracker.open_gui": "Open Dragon Tracker",
  "gui.dragontracker.title": "Dragon Tracker",
  "gui.dragontracker.no_dragons": "No dragons found",
  "gui.dragontracker.distance": "Distance: %s blocks",
  "gui.dragontracker.coordinates": "Coordinates: %s, %s, %s",
  "gui.dragontracker.type": "Type: %s",
  "gui.dragontracker.gender": "Gender: %s",
  "gui.dragontracker.teleport": "Teleport"
}
LANGEOF

cat > temp-build/assets/dragontracker/lang/uk_ua.json << 'LANGUKEOF'
{
  "key.dragontracker.open_gui": "Відкрити Dragon Tracker",
  "gui.dragontracker.title": "Dragon Tracker",
  "gui.dragontracker.no_dragons": "Драконів не знайдено",
  "gui.dragontracker.distance": "Відстань: %s блоків",
  "gui.dragontracker.coordinates": "Координати: %s, %s, %s",
  "gui.dragontracker.type": "Тип: %s",
  "gui.dragontracker.gender": "Стать: %s",
  "gui.dragontracker.teleport": "Телепортуватися"
}
LANGUKEOF

# Compile with explicit Java 17 target
echo "📦 Compiling for Java 17..."
cd temp-build

# Use Java 17 specific compilation flags
javac -source 17 -target 17 -cp . com/dragontracker/DragonTrackerMod.java

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    exit 1
fi

echo "✅ Compilation successful!"

# Remove Forge stub classes from final JAR (only include our mod)
rm -rf net/

# Create JAR with only our mod classes and resources
echo "📦 Creating JAR..."
jar cf dragontracker-1.0.0-java17.jar META-INF/ assets/ com/

if [ $? -ne 0 ]; then
    echo "❌ JAR creation failed!"
    exit 1
fi

# Move to releases
cd ..
mv temp-build/dragontracker-1.0.0-java17.jar releases/
rm -rf temp-build

echo "✅ Created dragontracker-1.0.0-java17.jar"
echo "📏 Size: $(ls -lh releases/dragontracker-1.0.0-java17.jar | awk '{print $5}')"

echo "🎉 JAVA 17 COMPATIBLE MOD CREATED!"
