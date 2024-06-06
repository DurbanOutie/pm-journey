#! /bin/bash
echo "Cleaning build directory..."
rm -rf ./build
mkdir ./build
mkdir ./build/classes
#mkdir ./build/resources

echo "pulling in new dependancies from external resources..."
#can be commented out when working with a final version of sira
cp -r ../sira4j/dist/sira.jar ./lib/jar/sira.jar

echo "copying dependancies..."
cp -r ./lib ./build/classes/lib

echo "compiling game sources..."
srcs=`find ./src/main/java -not -type d -not -name Main.* -not -path *platform*`
echo $srcs
javac -cp ./build/classes/lib/jar/sira.jar -d ./build/classes $srcs

echo "packaging game..."
bins=`find ./build/classes -not -type d`
jar cf ./build/pmjourney.jar -C ./build/classes pmjourney/game/Game.class $bins

echo "building platform..."
javac -cp ./build/pmjourney.jar:./build/classes/lib/jar/sira.jar -d ./build/classes ./src/main/java/pmjourney/platform/App.java

echo "packaging platform..."
bins=`find ./build/classes -not -type d`
jar cf ./build/pmjourney.jar -C ./build/classes pmjourney/game/Game.class $bins

echo "building entrypoint..."
javac -cp ./build/pmjourney.jar  -d ./build/classes ./src/main/java/Main.java
