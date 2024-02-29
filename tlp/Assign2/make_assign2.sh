./clean_assign2.sh
cp -r ../Code/src ./src
cp ../Code/roadmap.txt ./

jar -fcM assign2.zip ./build.sh ./run.sh ./roadmap.txt ./src ./writeup.pdf ./writeup.zip
