

// Import any package as required
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class HuffmanSubmit implements Huffman {
    // Feel free to add more methods and variables as required.

    PriorityQueue<Node> pq; // priority queue for selecting two minimum frequency
    Map<Character, String> enOutput = new HashMap<>(); // to store character and its correspond code

    public void encode(String inputFile, String outputFile, String freqFile) {
        BinaryIn in = new BinaryIn(inputFile);
        Map<Character, Integer> map = new HashMap(); // to store character and its correspond frequency

        char[] list = new char[256]; // to store all characters(there are maximum 256 different characters)
        int index = 0;
        while(!in.isEmpty()){
            Character temp = in.readChar();
            if(map.containsKey(temp)){
                map.replace(temp, map.get(temp) + 1); // if the character already exists, add 1 to its frequency
            }else{
                map.put(temp, 1); // if not, add this character and give 1 to its frequency
                list[index] = temp;
                index ++;
            }
        }

        printFreqFile(map, list, index, freqFile); // print out the frequency file
        getPriorityQueue(map, index, list); // create a priority queue to help find two smallest frequency
        Node HuffTree = huffmanTree(); // use priority queue to build huffman tree
        GUI g = new GUI(HuffTree, index+1);
        enTraverse(HuffTree, ""); // set code to the huffman tree
        printOutPutFile(inputFile, outputFile); // print out encoded file

    }

    public void decode(String inputFile, String outputFile, String freqFile){
        // TODO: Your code here
        Scanner scan = null; // first read frequency file
        try{
            scan = new Scanner(new File(freqFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int index = 0;
        char[] list = new char[256]; // to store all characters
        Map<Character, Integer> map2 = new HashMap(); // to store all characters and their correspond frequency
        while(scan.hasNextLine()){
            String line = scan.nextLine();
            String[] s = line.split(" : ");
            map2.put((char)Integer.parseInt(s[0],2), Integer.parseInt(s[1])); // convert binary to character
            list[index] = (char)Integer.parseInt(s[0],2);
            index++;
        }

        getPriorityQueue(map2, index, list); // create a priority queue to help find two smallest frequency
        Node DeHuffTree = huffmanTree(); // use priority queue to build huffman tree

        BinaryIn in = new BinaryIn(inputFile); // read encoded file
        BinaryOut out = new BinaryOut(outputFile);

        Node tempNode = DeHuffTree; // set a temporary node to the root
        out.flush();

        while(!in.isEmpty()) {
            if(tempNode.isLeaf()){ // if reaches bottom, write decoded character
                out.write(tempNode.key);
                tempNode = DeHuffTree;
            }

            if(in.readBoolean() == true) { // if code if true, go to right
                tempNode = tempNode.right;
            }else{
                tempNode = tempNode.left; // if code if false, go to left
            }
        }
        out.close();

    }

    public void printFreqFile(Map<Character,Integer> map, char[] list, int index, String freqFile){
        try{
        	BufferedWriter pr = new BufferedWriter(new FileWriter(freqFile));
            pr.flush();
            for(int i = 0; i < index; i++){

                String s = Integer.toBinaryString(list[i]);

                while(s.length() < 8){
                    s = "0" + s;
                }
                pr.write(s +" : "+map.get(list[i]));
                pr.newLine();
            }
            pr.close();
        }catch(IOException e) {
        	e.printStackTrace();
        }

    }

    public void printOutPutFile(String inputFile, String outputFile){
        BinaryIn in = new BinaryIn(inputFile);
        BinaryOut out = new BinaryOut(outputFile);
        while(!in.isEmpty()){
            char temp = in.readChar();
            String s = enOutput.get(temp);
            String[] split = s.split("");
            for(String sTemp: split) {
                if (sTemp.equals("1")) {
                    out.write(true);
                }
                if(sTemp.equals("0")){
                    out.write(false);
                }
            }
        }
        out.flush();
        out.close();

    }

    public void enTraverse(Node tree, String code){
        if(tree.isLeaf()){
            enOutput.put(tree.key, code);
        }else {
            enTraverse(tree.left, code + "0");
            enTraverse(tree.right, code + "1");
        }

    }

    public Node huffmanTree(){
        while(pq.size() > 1) {
            Node min1 = pq.poll();
            Node min2 = pq.poll();

            Node parent = new Node(null, min1.freq + min2.freq, min1, min2);
            pq.add(parent);
        }
        return pq.poll();

    }

    public void getPriorityQueue(Map<Character,Integer> map, int index, char[] list){
        pq = new PriorityQueue<> (new freqComparator());

        for(int i = 0; i < index; i++){
            Node node = new Node(list[i], map.get(list[i]), null,null);
            pq.add(node);
        }

    }

    static class freqComparator implements Comparator<Node>{
        @Override
        public int compare(Node o1, Node o2) {
            return (o1.freq.compareTo(o2.freq));
        }

    }

    public class Node {
        Integer freq;
        Character key;
        Node left;
        Node right;

        Node(Character key, Integer freq, Node left, Node right) {
            this.key = key;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        public boolean isLeaf() {
            return (left == null) && (right == null);
        }

    }

    public class GUI extends JFrame {
        Node tree;
        int size;

        public GUI(Node tree, int size) {
            this.tree = tree;
            this.size = size;
            Canvas canvas = new Canvas();

            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setTitle("Huffman Tree");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());
            setVisible(true);
            add(canvas);
        }

        public class Canvas extends JPanel {

            int heightBetween = 1000*33/(10*size);


            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                int mid = 350;
                int top = 15;
                int counter = 0;
                paintHelper(tree, g, mid, top, counter);
            }

            //create paintHelper for recursion
            public void paintHelper(Node tree, Graphics g, int mid, int top, int counter){
                int diameter = 5;
                int recLength = 50;
                int recWid = 17;

                if(tree.isLeaf()){
                    g.drawRect(mid - recLength/2, top, recLength, recWid);
                    g.drawString("'"+tree.key+"': "+tree.freq, mid-23, top + 11);
                }else {
                    g.fillOval(mid - diameter / 2, top, diameter, diameter);
                    top = top + diameter;
                    if (tree.left != null) {
                        counter++;
                        g.drawLine(mid, top, mid - 200 / counter, top + heightBetween);
                        mid = mid - 200 / counter;
                        top = top + heightBetween;
                        paintHelper(tree.left, g, mid, top, counter);
                        mid = mid + 200 / counter;
                        top = top - heightBetween;
                    }
                    if (tree.right != null) {
                        g.drawLine(mid, top, mid + 520 / counter, top + heightBetween);
                        mid = mid + 520 / counter;
                        top = top + heightBetween;
                        counter++;
                        paintHelper(tree.right, g, mid, top, counter);
                    }
                }

            }
        }
    }


    public static void main(String[] args) throws IOException {
        Huffman  huffman = new HuffmanSubmit();

        huffman.encode("ur.jpg", "ur.enc", "freq2.txt");
        huffman.encode("alice30.txt", "alice30enc.enc", "freq.txt");

        huffman.decode("ur.enc", "ur_dec.jpg", "freq2.txt");
        huffman.decode("alice30enc.enc", "alice30_dec.txt", "freq.txt");

        // After decoding, both ur.jpg and ur_dec.jpg should be the same.
        // On linux and mac, you can use `diff' command to check if they are the same.

    }

}