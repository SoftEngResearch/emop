# Use the script or manually run commands to see eMOP in action

1. Run this as the first/last thing of your demo:
mvn clean

2. Run this to make a change to B:
sed -i.bak 's/i = a(l, " ");/i = a(Collections.synchronizedList(l), " ");/g' src/main/java/demo/B.java && rm src/main/java/demo/B.java.bak

3. Run this to restore the change made to B:
sed -i.bak 's/i = a(Collections.synchronizedList(l), " ");/i = a(l, " ");/g' src/main/java/demo/B.java && rm src/main/java/demo/B.java.bak


