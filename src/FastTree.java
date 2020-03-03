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

public class FastTree
{
	Node tree;
	HashMap<Integer, String> encodingMap;
	FastTree()
	{
		this.encodingMap = new HashMap<Integer, String>(256);
	}
	public static void main(String[] args) throws Exception
	{
		HuffmanCoding c = new HuffmanCoding();
		long time = System.currentTimeMillis();
		c.generateTree("constitution.txt");
		
		System.out.println("\nExecution time(ms): " +(System.currentTimeMillis()- time) );
		
		c.encodeFile("constitution.txt", "codedconstitution.txt");
		System.out.println("\nTotal Execution time(ms): " +(System.currentTimeMillis()- time) );
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

//returns a hashmap of how often(value) a byte of data(key) occurs in a given file
class FrequencyTable extends RecursiveTask<HashMap<Integer, Integer>>
{
	private static final long serialVersionUID = 1134248248939109408L;
	HashMap<Integer, Integer> table;
	int maxSize = 1000;
	File inputFile;
	long start;
	long end;
	
	FrequencyTable(HashMap<Integer, Integer> table, File inputFile, long start, long end)
	{
		this.table = table;
		this.start = start;
		this.inputFile = inputFile;
	}
	@Override
	protected HashMap<Integer, Integer> compute() {
		if((end-start) < maxSize)
		{
			try
			{
				FileInputStream input = new FileInputStream(inputFile);
				input.skip(start);
				for(long i = start; i < end; i++)
				{
					
					Integer thisByte;
					thisByte = input.read();
					Integer frequency = table.get(thisByte);
					if(frequency == null) {
						frequency = 0;
					}
					else {
						frequency++;
					}
					table.put(thisByte, frequency);
					
				}
				input.close();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			return table;
		}
		else 
		{
			long mid = ((end - start)/2) + start;
			FrequencyTable t1 = new FrequencyTable(table, inputFile, start, mid);
			FrequencyTable t2 = new FrequencyTable(table, inputFile, mid, end);
			t1.fork();
			t2.fork();
			
			return sum(t1.join(), t2.join());
		}
		
	}
	private HashMap<Integer, Integer> sum(HashMap<Integer, Integer> table1, HashMap<Integer, Integer> table2)
	{
		ArrayList<Integer> keyList = new ArrayList<Integer>();
		keyList.addAll(table1.keySet());
		
		for(Integer key: keyList)
		{
			if(table2.containsKey(key))
			{
				table2.put(key, table1.get(key) + table2.get(key));
			}
			else
			{
				table2.put(key, table1.get(key));
			}
		}
		return table2;
	}
	
}