#!/bin/bash

echo "Building Dragon Tracker Mod..."

# Create build directories
mkdir -p build/classes
mkdir -p build/libs

# Create a simple JAR manifest
cat > build/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Implementation-Title: Dragon Tracker
Implementation-Version: 1.0.0
Implementation-Vendor: Dragon Tracker Team
EOF

# Copy resources
cp -r src/main/resources/* build/classes/

# Create the JAR file
cd build/classes
jar cfm ../libs/dragontracker-1.0.0.jar ../MANIFEST.MF .

echo "JAR file created: build/libs/dragontracker-1.0.0.jar"