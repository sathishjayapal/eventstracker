#!/bin/bash

echo "=========================================="
echo "MapStruct Implementation Generator"
echo "=========================================="

cd /Users/sathishjayapal/IdeaProjects/eventstracker

echo ""
echo "Step 1: Cleaning project..."
mvn clean

echo ""
echo "Step 2: Compiling with MapStruct..."
mvn compile -DskipTests

echo ""
echo "Step 3: Checking generated files..."
if [ -d "target/generated-sources/annotations" ]; then
    echo "✅ Generated sources directory exists"

    echo ""
    echo "Generated MapStruct implementations:"
    find target/generated-sources/annotations -name "*MapperImpl.java" -type f

    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ MapStruct implementations generated successfully!"

        echo ""
        echo "Checking compiled classes..."
        find target/classes -name "*MapperImpl.class" -type f

        echo ""
        echo "=========================================="
        echo "SUCCESS! You can now run: mvn spring-boot:run"
        echo "=========================================="
    else
        echo ""
        echo "❌ No MapperImpl.java files found"
        echo "Check Maven output above for errors"
    fi
else
    echo "❌ Generated sources directory not created"
    echo "MapStruct annotation processor may not have run"
fi

echo ""
echo "To run the application:"
echo "  mvn spring-boot:run"

