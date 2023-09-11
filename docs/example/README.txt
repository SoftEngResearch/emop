# Use emop_demo.sh or manually run commands in emop_demo.sh to see eMOP in action

1. Run the following as the first/last command in the demo:
mvn clean

2. Run the following command to make a change to B:
sed -i.bak 's/i = a(l, " ");/i = a(Collections.synchronizedList(l), " ");/g' src/main/java/demo/B.java && rm src/main/java/demo/B.java.bak

3. Run the following command to restore the change made to B:
sed -i.bak 's/i = a(Collections.synchronizedList(l), " ");/i = a(l, " ");/g' src/main/java/demo/B.java && rm src/main/java/demo/B.java.bak
