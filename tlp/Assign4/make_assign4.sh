./clean_assign4.sh
cp -r ../Code/src ./src
cp -r ../Code/res ./res

jar -fcM assign4.zip ./build.sh ./run.sh ./src ./res ./writeup.pdf ./writeup.zip
