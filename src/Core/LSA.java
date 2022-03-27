package Core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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

    public HashMap<String,Node> Nodes = new HashMap<>();
    public String source;

    public HashMap<String, Integer> Distances = new HashMap<>();
    public PriorityQueue<Dist> Q = new PriorityQueue<>();
    public HashSet<String> visited = new HashSet<>();
    public HashMap<String, String> Predecessor = new HashMap<>();

    public ArrayList<String> text = new ArrayList<>();


    public String[] sample = {
        "A: B:5 C:3 D:5 ",
        "B: A:5 C:4 E:3 F:2",
        "C: A:3 B:4 D:1 E:6",
        "D: A:5 C:1 E:3 ",
        "E: B:3 C:6 D:3 F:5 ",
        "F: B:2 E:5"
    };

    public void Reset(){
        Distances.clear();
        Predecessor.clear();
        Q.clear();
        visited.clear();
        text.clear();
        source = null;
    }

    public void Initialize(String path) throws Exception {

        //Read the file
        Scanner sc = new Scanner(Files.newBufferedReader(Paths.get(path)));
        while(sc.hasNextLine()){
            text.add(sc.nextLine());
        }

        for(String s : text){
            //Split with the first ':', ignoring following colons
            String[] parts = s.split(":", 2);
            String name = parts[0];
            String[] neighbors = parts[1].split(" ");

            for (String n : neighbors) {
                //Check if the same edges from different direction are consistent, throw an exception otherwise
                if (n.isEmpty()) continue;
                if(Nodes.containsKey(n)){
                    String[] nParts = n.split(":");
                    if(!nParts[0].equals(name))
                        throw new RuntimeException("Inconsistent edges");
                }
                String[] nParts = n.split(":");
                String neighbor = nParts[0];
                int distance = Integer.parseInt(nParts[1]);
                AddNode(name);
                AddNode(neighbor);
                AddEdge(name, neighbor, distance);
            }
        }

    }

    public int setSource(String source){
        Reset();
        this.source = source;
        for (String s : Nodes.keySet()) {
            Distances.put(s, Integer.MAX_VALUE);
            Predecessor.put(s, null);
        }
        //Set the distance of the source to 0
        Distances.put(source, 0);
        //Add neighbors of the source to the queue
        for(String s : Nodes.get(source).Neighbors.keySet()){
            Q.add(new Dist(s, Nodes.get(source).Neighbors.get(s)));
        }
        return 0;
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

    public int AddNode(String name){
        assert source == null; //There should not be selected or in-progress algorithm before editing the graph
        //Return -1 if the node already exists
        if(Nodes.containsKey(name))
            return -1;
        Nodes.put(name, new Node(name));
        return 0;
    }

    public int RemoveNode(String name){
        assert source == null; //There should not be selected or in-progress algorithm before editing the graph
        //Return -1 if the node does not exist
        if(!Nodes.containsKey(name))
            return -1;
        Nodes.remove(name);
        //remove all edges connected to this node
        for(String s : Nodes.keySet()){
            Nodes.get(s).Neighbors.remove(name);
        }
        return 0;
    }
    
    public int AddEdge(String from, String to, int distance){
        assert source == null; //There should not be selected or in-progress algorithm before editing the graph
        //Return -1 if the node does not exist
        if(!Nodes.containsKey(from) || !Nodes.containsKey(to))
            return -1;
        //Return -2 if the edge already exists
        if(Nodes.get(from).Neighbors.containsKey(to))
            return -2;
        Nodes.get(from).Neighbors.put(to, distance);
        Nodes.get(to).Neighbors.put(from, distance);
        return 0;
    }

    public int RemoveEdge(String from, String to){
        assert source == null; //There should not be selected or in-progress algorithm before editing the graph
        //Return -1 if the node does not exist
        if(!Nodes.containsKey(from) || !Nodes.containsKey(to))
            return -1;
        Nodes.get(from).Neighbors.remove(to);
        Nodes.get(to).Neighbors.remove(from);
        return 0;
    }








}
