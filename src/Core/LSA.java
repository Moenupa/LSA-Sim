package Core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class LSA {

    static class Dist implements Comparable<Dist>{
        String name;
        int distance;

        @Override
        public int compareTo(Dist o) {
            return this.distance - o.distance;
        }

        public Dist(String name, int distance){
            this.name = name;
            this.distance = distance;
        }
    }

    public HashMap<String,Node> Nodes;
    public String source;

    public HashMap<String, Integer> Distances ;
    public PriorityQueue<Dist> Q;
    public HashSet<String> visited;
    public HashMap<String, String> Predecessor;

    public void Reset(){
        Distances.clear();
        Predecessor.clear();
        Q.clear();
        visited.clear();
        source = null;
    }

    public void Initialize(String path){


        //Check if the same edges from different direction are consistent, throw an exception otherwise
    }

    public String SingleStep(){
        if(source == null)
            throw new RuntimeException("Source not set");
        //Single step of Dijkstra's algorithm
        Dist d = Q.poll();
        assert d != null;
        visited.add(d.name);
        for(String s : Nodes.get(d.name).Neighbors.keySet()){
            if(!visited.contains(s)){
                int newDist = Distances.get(d.name) + Nodes.get(d.name).Neighbors.get(s);
                if(!Distances.containsKey(s) || newDist < Distances.get(s)){
                    Distances.put(s, newDist);
                    Predecessor.put(s, d.name);
                    Q.add(new Dist(s, newDist));
                }
            }
        }
        return d.name;
    }

    public void Run(){
        while(!Q.isEmpty())
            SingleStep();
    }

    public void AddNode(String name){
        assert source == null; //There should be selected or in-progress algorithm before editing the graph

        Nodes.put(name, new Node(name));
    }

    public void RemoveNode(String name){
        assert source == null; //There should be selected or in-progress algorithm before editing the graph

        Nodes.remove(name);
        //remove all edges connected to this node
        for(String s : Nodes.keySet()){
            Nodes.get(s).Neighbors.remove(name);
        }
    }
    
    public void AddEdge(String from, String to, int distance){
        assert source == null; //There should be selected or in-progress algorithm before editing the graph

        Nodes.get(from).Neighbors.put(to, distance);
        Nodes.get(to).Neighbors.put(from, distance);
    }

    public void RemoveEdge(String from, String to){
        assert source == null; //There should be selected or in-progress algorithm before editing the graph

        Nodes.get(from).Neighbors.remove(to);
        Nodes.get(to).Neighbors.remove(from);
    }








}
