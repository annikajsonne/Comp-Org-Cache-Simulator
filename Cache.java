
/**
 * Class implementing a basic cache with a configurable size and associativity.
 * 
 * The cache is backed-up by a "Memory" object which actually stores stores the
 * values -- on a cache miss, the Memory should be accessed.
 * 
 */
public class Cache implements ReadOnlyCache {
	private Memory m_memory; //memory
	private int m_numBlocks; //number of blocks in the cache
	private int m_bytesPerBlock; //number of bytes per block 
	private int m_associativity; // what type of cache
	private byte[][] data; //cache data
	private boolean[] valid; //valid bit
	private int[] tags; // tags

	/**
	 * Constructor
	 * 
	 * @param memory        - An object implementing the Memory functionality. This
	 *                      should be accessed on a cache miss
	 * @param blockCount    - The number of blocks in the cache.
	 * @param bytesPerBlock - The number of bytes per block in the cache.
	 * @param associativity - The associativity of the cache. 1 means direct mapped,
	 *                      and a value of "blockCount" means fully-associative.
	 */
	public Cache(Memory memory, int blockCount, int bytesPerBlock, int associativity) {
		//setting the data members
		m_memory = memory;
		m_numBlocks = blockCount;
		m_bytesPerBlock = bytesPerBlock;
		m_associativity = associativity;
		data = new byte[m_numBlocks][m_bytesPerBlock];
		valid = new boolean[m_numBlocks];
		tags = new int[m_numBlocks];
	}

	/**
	 * Method to retrieve the value of the specified memory location.
	 * 
	 * @param address - The address of the byte to be retrieved.
	 * @return The value at the specified address.
	 */
	public byte load(int address) {
		//address subdivision:
		int tag = tag(address);
		int index = index(address);
		int offset = offset(address);
		
		//calculating block address
		int blockaddress = address - offset;
		
		//doing the loading depending on the associativity
		if (m_associativity == 1) {
			//direct mapped cache
			if (valid[index] && tags[index] == tag) {
				//if the data being asked for exists in the cache - there was a hit
				return data[index][offset];
			} else {
				//there was a miss
				//read the data from memory, set the valid bit to true and the tag at index to the tag, then return the data
				byte[] block = m_memory.read(blockaddress, m_bytesPerBlock);
				data[index] = block;
				valid[index] = true;
				tags[index] = tag;
				return data[index][offset];
			}

		} else if (m_associativity == m_numBlocks) {
			//fully associative cache
			for (int i = 0; i < data.length; i++) {
				if (valid[i] && tags[i] == tag) {
					//if data exists in the cache
					return data[i][offset];
				}
			}
			//if data does not exist in the cache, put the data in the first open spot in the cache and return it.
			for (int i = 0; i < data.length; i++) {
				if (valid[i] == false) {
					byte[] block = m_memory.read(blockaddress, m_bytesPerBlock);
					data[i] = block;
					valid[i] = true;
					tags[i] = tag;
					return data[i][offset];
				}
			}
			
			//if the cache is full, replace the oldest with the new data and return it.
			data[m_numBlocks - 1] = m_memory.read(blockaddress, m_bytesPerBlock);
			valid[m_numBlocks - 1] = true;
			tags[m_numBlocks - 1] = tag;
			return data[m_numBlocks - 1][offset];

		} else {
			//set associative cache
			if (valid[index] && tags[index] == tag) {
				//if the data being asked for exists in the cache - there was a hit
				return data[index][offset];
			} else {
				//there was a miss
				//read the data from memory, set the valid bit to true and the tag at index to the tag, then return the data
				byte[] block = m_memory.read(blockaddress, m_bytesPerBlock);
				data[index] = block;
				valid[index] = true;
				tags[index] = tag;
				return data[index][offset];
			}


		}
	}

	public int offset(int address) {
		//calculate the byte offset and return it
		return address % m_bytesPerBlock;
	}

	public int index(int address) {
		//calculate the index, depends on what kind of cache
		int index = 0;
		if (m_associativity == 1) {
			index = (address / m_bytesPerBlock) % m_numBlocks;
		} else if (m_associativity == m_numBlocks) {
			index = 0;
		} else {
			index = (address / m_bytesPerBlock) % m_numBlocks;
		}
		return index;
	}

	public int tag(int address) {
		//calculate the tag, depends on type of cache
		int tag = 0;
		if (m_associativity == 1) {
			tag = (address / (m_bytesPerBlock * m_numBlocks));
		} else if (m_associativity == m_numBlocks) {
			tag = address / m_bytesPerBlock;
		} else {
			tag = (address / (m_bytesPerBlock * m_numBlocks));
		}
		return tag;
	}
}
