package Core;

import java.io.File;
import java.util.*;

public class LSA {

    static class Dist implements Comparable<Dist> {
        String name;
        int distance;
        String prev;

        public Dist(String name, int distance, String prev) {
            this.name = name;
            this.distance = distance;
            this.prev = prev;
        }

        @Override
        public int compareTo(Dist o) {
            return Integer.compare(this.distance, o.distance);
        }
    }


    public HashMap<String,Node> Nodes = new HashMap<>();
    public String source;

    public HashMap<String, Integer> Distances = new HashMap<>();
    public PriorityQueue<Dist> Q = new PriorityQueue<>();
    public HashSet<String> visited = new HashSet<>();
    public HashMap<String, String> Predecessor = new HashMap<>();

    public ArrayList<String> text = new ArrayList<>();

    public static String[] sample = {
        "A: B:5 C:3 D:5 ",
        "B: A:5 C:4 E:3 F:2",
        "C: A:3 B:4 D:1 E:6",
        "D: A:5 C:1 E:3 ",
        "E: B:3 C:6 D:3 F:5 ",
        "F: B:2 E:5"
    };

    // reset source but not nodes
    public void Reset() {
        Distances.clear();
        Predecessor.clear();
        Q.clear();
        visited.clear();
        source = null;
    }

    // reset all
    public void safeReset() {
        Reset();
        text.clear();
        Nodes.clear();
    }

    public void loadFromFile(File file) throws Exception {
        safeReset();
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            text.add(sc.nextLine());
        }
        sc.close();
    }

    public void loadFromStr(String text) {
        safeReset();
        this.text = new ArrayList<>(Arrays.asList(text.split("\n")));
    }

    /**
     * Parse `this.text` and store to `this.Nodes`.
     *
     * @exception RuntimeException if invalid format detected
     */
    public void parse() throws IllegalArgumentException {
        // clearing cache
        Nodes.clear();

        for (String s : text){
            parseLine(s);
        }
    }

    public void parseLine(String s) throws IllegalArgumentException {
        // Split with the first ':', ignoring following colons
        String[] parts = s.split(":", 2);

        // does not contain ':', err
        if (parts.length < 2)
            throw new IllegalArgumentException("Invalid Format: Missing ':', '<node>:<len>'.");

        String name = parts[0];
        String[] neighbors = parts[1].split(" ");

        for (String n : neighbors) {
            if (n.isEmpty()) continue;
            // Check if the same edges from different direction are consistent
            String[] nParts = n.split(":");
            if (nParts.length != 2)
                throw new IllegalArgumentException("Invalid Format: Missing ':', '<node>:<len>'.");
            if (Nodes.containsKey(n)){
                // n is in format like 'A:3'
                if (!nParts[0].equals(name))
                    throw new IllegalArgumentException("Invalid Format: Inconsistent edges.");
            }
            String neighbor = nParts[0];
            int distance;
            try {
                distance = Integer.parseInt(nParts[1]);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid Format: Invalid length, '<node>:<len>'.");
            }
            AddNode(name);
            AddNode(neighbor);
            AddEdge(name, neighbor, distance);
        }
    }

    public int setSource(String source) {
        Reset();
        this.source = source;
        for (String s : Nodes.keySet()) {
            Distances.put(s, Integer.MAX_VALUE);
            Predecessor.put(s, null);
        }
        //Set the distance of the source to 0
        Distances.put(source, 0);
        visited.add(source);

        //Add neighbors of the source to the queue
        Node src = Nodes.get(source);
        if (src == null)
            return 1;
        for(String s : src.Neighbors.keySet()){
            Q.add(new Dist(s, Nodes.get(source).Neighbors.get(s), source));
            Predecessor.put(s, source);
        }
        return 0;
    }

    public String SingleStep() {
        if(source == null)
            throw new RuntimeException("Source not set");
        if (Q.isEmpty())
            return "";
        // Single step of Dijkstra's algorithm
        Dist d = Q.poll();
        assert d != null;
        if (visited.contains(d.name)) return  SingleStep();
        visited.add(d.name);
        Distances.put(d.name, d.distance);
        Predecessor.put(d.name, d.prev);
        for(String s : Nodes.get(d.name).Neighbors.keySet()){
            if(!visited.contains(s)){
                int newDist = Distances.get(d.name) + Nodes.get(d.name).Neighbors.get(s);
                    Q.add(new Dist(s, newDist, d.name));
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

    @Override
    public String toString() {
        return "LSA{" +
                "Distances=" + Distances +
                ", \nvisited=" + visited +
                ", \nPredecessor=" + Predecessor +
                '}';
    }
}
