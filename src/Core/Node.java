package Core;

import java.util.HashMap;

public class Node {
    public String name;
    public HashMap<String,Integer> Neighbors;
    public Node(String name) {
        this.name = name;
        Neighbors = new HashMap<String,Integer>();
    }
    public void addEdge(String name, int id){
        Neighbors.put(name,id);
    }
    public void removeEdge(String name){
        Neighbors.remove(name);
    }
    public int getEdge(String name){
        return Neighbors.get(name);
    }
    public boolean hasEdge(String name){
        return Neighbors.containsKey(name);
    }
    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", Neighbors=" + Neighbors +
                '}';
    }
}
