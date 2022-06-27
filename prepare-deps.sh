# Download sdk source
rm -r cardboard
curl -sLS https://github.com/googlevr/cardboard/archive/v1.17.0.zip > download.zip
unzip download.zip
rm download.zip
mv ./cardboard-1.17.0 ./cardboard

# Build sdk
cd ./cardboard
./gradlew sdk:assembleRelease -Parm64-v8a

# Prepare files
mv ./sdk/build/outputs/aar/sdk-release.aar ./cardboard-sdk.aar
cp ./sdk/include/cardboard.h ./cardboard.h
