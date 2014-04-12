export VERSION=1.4
hdiutil convert -format UDZO -imagekey zlib-level=9 HarmonEye_uncompressed.dmg -o HarmonEye-$VERSION.dmg
