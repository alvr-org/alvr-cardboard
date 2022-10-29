SDK_VRS="1.17.0"

# Download ALVR source
rm -r "ALVR"
curl -sLS "https://github.com/alvr-org/ALVR/archive/refs/heads/master.zip" > download.zip
unzip download.zip
rm download.zip
mv ALVR-master ALVR

rm -r cardboard

# Download sdk source
rm -r "cardboard-${SDK_VRS}"
curl -sLS "https://github.com/googlevr/cardboard/archive/v${SDK_VRS}.zip" > download.zip
unzip download.zip
rm download.zip

# Build sdk
pushd "cardboard-${SDK_VRS}"
./gradlew sdk:assembleRelease -Parm64-v8a
popd

# Prepare files
mkdir cardboard
mv "cardboard-${SDK_VRS}/sdk/build/outputs/aar/sdk-release.aar" cardboard/cardboard-sdk.aar
cp "cardboard-${SDK_VRS}/sdk/include/cardboard.h" cardboard/cardboard.h
rm -r "cardboard-${SDK_VRS}"