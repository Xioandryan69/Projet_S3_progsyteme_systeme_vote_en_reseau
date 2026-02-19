
javac -cp "lib/*;." -d bin ^
src\main\*.java ^
src\reseaux\*.java ^
src\model\*.java ^
src\data\*.java ^
src\client\*.java

java -cp "lib/*;bin;." main.ServeurVote
java -cp "lib/*;bin;." client.VoteClientGUI
