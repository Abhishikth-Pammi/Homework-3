## Police-Thief Game
- **Logic:** The game starts with two nodes which are farthest away in the graph. Those two nodes are assigned as Police and Thief.
- Both move one step ahead at a time to their children nodes and as the game contiue, if theif get to the node having the valuabe data, the thief wins the game.
- If the thief node and police node meets, the police wins the game.
- if either the thief or police has no children nodes, the opposite person wins.
- **Youtube Link:**
- blababla

Before you begin, ensure you have met the following requirements:

- **Java:** The project typically requires Java. You can check if it's installed using:
  java -version

mark

- **SBT (Scala Build Tool):** This is essential to build and package Scala projects. If it's not already installed, follow the installation guidelines [here](https://www.scala-sbt.org/download.html).

## Building the Project using `sbt assembly`

1. **Clone the repository:**

git clone https://github.com/Abhishikth-Pammi/HomeWork-2.git
cd HomeWork-2

2. **Clean the project (Optional):**

It's generally a good practice to clean your project before assembling it.

sbt clean

3. **Compile the project:**

Ensure there are no compilation errors.

sbt compile

4**Run the assembly command:**

sbt assembly

Upon successful execution, this command creates a single assembly JAR file under the `target/scala-x.x.x/` directory. This JAR will contain your project's compiled class files as well as its dependencies.

5**Running the assembled JAR (Optional):**

If you want to run the application after assembling:

java -jar target/scala-2.13/HW2_Code-assembly-0.1.0-SNAPSHOT.jar

Run this Spark command
spark-submit --master local --class Main --jars HW3_Code-assembly-0.1.0-SNAPSHOT.jar --driver-class-path HW3_Code-assembly-0.1.0-SNAPSHOT.jar HW3_Code-assembly-0.1.0-SNAPSHOT.jar "classes/"


