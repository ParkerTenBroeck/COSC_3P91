./clean_assign2.sh
cp -r ../Code/src ./src
cp ../Code/newmap.txt ./

jar -fcM assign2.zip ./build.sh ./run.sh ./newmap.txt ./src ./writeup.pdf ./writeup.zip
