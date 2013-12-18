export VERSION=1.3
hdiutil convert -format UDZO -imagekey zlib-level=9 HarmonEye_uncompressed.dmg -o HarmonEye-$VERSION.dmg
