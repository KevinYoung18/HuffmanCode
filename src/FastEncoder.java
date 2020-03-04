import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FastEncoder
{
	Node tree;
	HashMap<Integer, String> encodingMap;
	FastEncoder()
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
		HashMap<Integer, Integer> freqTable = getFrequencyTable(new File(fileName));
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
	
}
class Encoder extends RecursiveTask<ArrayList<byte[]>>
{
	
	private static final long serialVersionUID = 6090393976505943773L;
	HashMap<Integer, String> encodingTable;
	int maxSize = 1000;
	File inputFile;
	long start;
	long end;
	
	Encoder(HashMap<Integer, String> encodingTable, File inputFile, long start, long end)
	{
		this.encodingTable = encodingTable;
		this.start = start;
		this.end = end;
		this.inputFile = inputFile;
	}
	@Override
	protected ArrayList<byte[]> compute() {
		if((end-start) < maxSize)
		{
			ArrayList<byte[]> byteList = new ArrayList<byte[]>();
			try
			{
				FileInputStream input = new FileInputStream(inputFile);
				
				input.skip(start);
				Integer thisByte= 0;
				String thisString = "";
				for(long i = start; i < end; i++)
				{
					thisByte = input.read();
					thisString += encodingTable.get(thisByte);
					if((thisString.length() % 4) == 0) 
					{
						byteList.add(parseString(thisString));
						thisString = "";
					}
				}
				input.close();
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			return byteList;
		}
		else 
		{
			long mid = ((end - start)/2) + start;
			Encoder t1 = new Encoder(encodingTable, inputFile, start, mid);
			Encoder t2 = new Encoder(encodingTable, inputFile, mid, end);
			t1.fork();
			t2.fork();
			ArrayList<byte[]> byteList = new ArrayList<byte[]>();
			byteList.addAll(t1.join());
			byteList.addAll(t2.join());
			return byteList;

		}
		
	}
	
	//parses a string of 1's and 0's  and returns a byte array corresponding to  the string
	// string must be divisible by 4
	private byte[] parseString(String currentString) throws Exception {
		int numOfBytes = currentString.length() / 4;
		byte[] byteArray = new byte[numOfBytes];
		
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