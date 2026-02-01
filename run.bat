javac -cp "lib/gson.jar;." -d bin src\main\*.java src\reseaux\*.java src\model\*.java src\data\*.java

java -cp "lib/gson.jar;bin" main.ServeurVote
