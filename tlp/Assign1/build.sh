mkdir compiled
javac -verbose -d compiled src/*.java src/traffic_sim/*.java src/traffic_sim/io/*.java src/traffic_sim/map/*.java src/traffic_sim/map/intersection/*.java src/traffic_sim/vehicle/*.java src/traffic_sim/vehicle/controller/*.java src/traffic_sim/exceptions/*.java

cd compiled
jar -cvfe ../traffic_sim.jar Main ./*
