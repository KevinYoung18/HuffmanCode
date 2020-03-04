import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ForkJoinPool;

public class FastHuffmanCoding
{
	Node tree;
	HashMap<Integer, String> encodingMap;
	FastHuffmanCoding()
	{
		this.encodingMap = new HashMap<Integer, String>(256);
	}
	public static void main(String[] args) throws Exception
	{
		HuffmanCoding c = new HuffmanCoding();
		long time = System.currentTimeMillis();
		
		c.generateTree("constitution.txt");
		
		System.out.println("\nTime to generate tree(ms): " + (System.currentTimeMillis()- time) );
		long encodeTime = System.currentTimeMillis();
		
		c.encodeFile("constitution.txt", "codedconstitution.txt");
		
		System.out.println("\nEncoding time(ms): " + (System.currentTimeMillis()- encodeTime) );
		System.out.println("\nTotal execution time(ms): " + (System.currentTimeMillis()- time) );
	}
	public void generateTree(String fileName) throws IOException
	{

		ForkJoinPool pool = new ForkJoinPool();
		HashMap<Integer, Integer> freqTable = new HashMap<Integer, Integer>();
		File inputFile = new File(fileName);
		FrequencyTable t0 = new FrequencyTable(freqTable, inputFile, 0, inputFile.length());
		freqTable = pool.invoke(t0);
		freqToHuff(freqTable);
	}
	//encodes the indicated file with the huffman tree belonging to this object
	public void encodeFile(String inputFile, String outputFile) throws Exception
	{
		File input = new File(inputFile);
		FileOutputStream output = new FileOutputStream(outputFile);
		
		ForkJoinPool pool = new ForkJoinPool();
		Encoder task = new Encoder(encodingMap, input, 0, input.length());
		ArrayList<byte[]> byteList = pool.invoke(task);
		
		for(byte[] byteArray : byteList) 
			output.write(byteArray);
		
		output.write(Byte.parseByte("-1"));
		output.close();
	}
	
	
	
	//returns a hashmap of how often(value) a byte of data(key) occurs in a given file
	
	//takes a frequency Table, converts it to a huffman tree and sets the root to Node tree
	private void freqToHuff(HashMap<Integer, Integer> freqTable)
	{
		Node root = null;
		ArrayList<Integer> keys = new ArrayList<Integer>();
		PriorityQueue<Node> q = new PriorityQueue<Node>();
		keys.addAll(freqTable.keySet());
		for(Integer key : keys)
		{
			q.add(new Node(key, freqTable.get(key)));
		}

		
		while(q.size() >= 2)
		{
			Node first = q.poll();
			Node second = q.poll();
			
			int sum = first.getValue() + second.getValue();
			
			Node parent = new Node(first, second, sum);
			root = parent;
			q.add(parent);
		}
		
		this.tree = root;
		buildMap(root, "");
		
	}
	private void buildMap(Node n, String s)
	{
		
		if(n.left == null && n.right == null)
		{
			this.encodingMap.put(n.getKey(), s);
		}
		else
		{
			buildMap(n.left, s + "0");
			buildMap(n.right, s + "1");
		}
	}
	
	
}
