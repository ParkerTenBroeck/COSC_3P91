mkdir compiled
javac -verbose -d compiled src/*.java src/traffic_sim/*.java src/traffic_sim/io/*.java src/traffic_sim/map/*.java src/traffic_sim/map/intersection/*.java src/traffic_sim/vehicle/*.java src/traffic_sim/vehicle/controller/*.java src/traffic_sim/excpetions/*.java src/traffic_sim/gamble/*.java src/traffic_sim/excpetions/*.java src/traffic_sim/map/xml/*.java src/traffic_sim/excpetions/*.java src/traffic_sim/networking/*.java 

cd compiled
jar -cvfe ../traffic_sim.jar Main ./* ../res/*

