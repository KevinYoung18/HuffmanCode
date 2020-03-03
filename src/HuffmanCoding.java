import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffmanCoding
{
	Node tree;
	HashMap<Integer, String> encodingMap;
	HuffmanCoding()
	{
		this.encodingMap = new HashMap<Integer, String>(256);
	}
	public static void main(String[] args) throws Exception
	{
		HuffmanCoding c = new HuffmanCoding();
		long time = System.currentTimeMillis();
		c.generateTree("constitution.txt");
		System.out.println("\nExecution time(ms): " +(System.currentTimeMillis()- time) );
		
		time = System.currentTimeMillis();
		c.encodeFile("constitution.txt", "codedconstitution.txt");
		System.out.println("\nExecution time(ms): " +(System.currentTimeMillis()- time) );
	}
	public void generateTree(String fileName) throws IOException
	{
		HashMap<Integer, Integer> freqTable = getFrequencyTable(new File(fileName));
		freqToHuff(freqTable);
		
		
	}
	public void encodeFile(String inputFile, String outputFile) throws Exception
	{
		InputStream input = new FileInputStream(inputFile);
		FileOutputStream output = new FileOutputStream(outputFile);
		
		int thisByte;
		String currentString = "";
		byte[] outputBytes;
		while((thisByte = input.read()) != -1)
		{
			
			currentString += encodingMap.get(thisByte);
			if((currentString.length() % 4) == 0) {
				outputBytes = new byte[currentString.length()/4];
				outputBytes = parseString(currentString);
				currentString = "";
				output.write(outputBytes);
			}
		}
		
		output.write(Byte.parseByte("-1"));
		output.close();
		input.close();
	}
	
	
	private byte[] parseString(String currentString) throws Exception {
		int numOfBytes = currentString.length() / 4;
		byte[] byteArray =new byte[numOfBytes];
		
		for(int i = 0; i < numOfBytes; i++)
		{
			String byteStr ="";
			for(int j = 0; j < 4; j++)
			{
				byteStr += currentString.charAt(i*4+j);
			}
			byteArray[i] = stringToByte(byteStr);
		}
		
		return byteArray;
	}
	//returns a hashmap of how often(value) a byte of data(key) occurs in a given file
	private HashMap<Integer, Integer> getFrequencyTable (File inputFile) throws IOException
	{
		InputStream input = new FileInputStream(inputFile);
		HashMap<Integer, Integer> frequencyTable = new HashMap<Integer, Integer>(256);
		int thisByte;
		while((thisByte = input.read()) != -1)
		{
			Integer frequency = frequencyTable.get(thisByte);
			if(frequency == null) {
				frequency = 0;
			}
			else {
				frequency++;
			}
			frequencyTable.put(thisByte, frequency);
		}
		
		input.close();
		return frequencyTable;
		
	}
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
	//converts a binary string to byte e.g.: "0010" to a byte representing the integer 2
	private byte stringToByte(String binaryString) throws Exception
	{
		final int MAX_SIZE = 4;
		if(binaryString.length() != MAX_SIZE)
			throw  new Exception("Invalid binary string size");
		Integer value = 0;
		for(int i = MAX_SIZE - 1; i >= 0; i--)
		{
			if(binaryString.charAt(i) =='1')
			{
				value += 2^(MAX_SIZE-i);
			}
			else if(binaryString.charAt(i) != '0')
			{
				
				throw  new Exception("Invalid char in binary string: " + binaryString );
			}
		}
		return value.byteValue();
		
	}
	
}
class Node implements Comparable<Node>
{
	private int key;
	private int value;
	
	Node left;
	Node right;
	
	Node(int key, int value)
	{
		this.key = key;
		this.value = value;
	}
	Node(Node first, Node second, int value)
	{
		this.value = value;
		this.left = first;
		this.right = second;
	}
	
	@Override
	public int compareTo(Node arg) {
		if(value == arg.getValue())
		{
			if(key > arg.getKey())
				return 1;
			else 
				return -1;
		}
		else if(value > arg.getValue())
			return 1;
		else
			return -1;
	}
	public int getKey() {
		return key;
	}

	public int getValue() {
		return value;
	}
	
}
