public class Cache{

	private class Block{
		int blockFrameNumber = -1; //block id
		int referenceBit = 1;
		int dirtyBit = 0;
		byte[] data;
	}

	Block[] blocks;
	private int blocksz;

	public Cache(int blockSize, int numBlocks){
		this.blocks = new Block[numBlocks];
		this.blocksz = blockSize;
		for(int i =0; i < numBlocks; i++){
			this.blocks[i] = new Block();		
			this.blocks[i].data = new byte[blockSize];;
		}
	}

	//cache read
	synchronized boolean read(int blockId, byte buffer[]){
		if(blockId < 0 || blockId >= 1000)return false;
		//check if block is cached		
		for(int i = 0; i < blocks.length; i++){
			if(blocks[i].blockFrameNumber == blockId){
				System.arraycopy(blocks[i].data, 0, buffer, 0, blocks[i].data.length);
				blocks[i].referenceBit = 1;
				return true;
			}
		}

		//data not cached, look for a free block
		for(int i = 0; i < blocks.length; i++)
			if(blocks[i].blockFrameNumber == -1){
				blocks[i].blockFrameNumber = blockId;
				SysLib.rawread(blocks[i].blockFrameNumber, blocks[i].data);
				System.arraycopy(blocks[i].data, 0, buffer, 0, blocks[i].data.length);
				return true;
			}
		

		int bIndex = 0;
		while(blocks[(bIndex % blocks.length)].referenceBit != 0){			
				blocks[bIndex].referenceBit = 0;
				bIndex++;
		}	
		bIndex %= blocks.length;
		//check dirty bit
		//up to date, no need to write to disk
		if(blocks[bIndex ].dirtyBit == 0){
			blocks[bIndex].blockFrameNumber = blockId;
			blocks[bIndex].referenceBit = 1;

			SysLib.rawread(blocks[bIndex].blockFrameNumber, blocks[bIndex].data);
			System.arraycopy(blocks[bIndex].data, 0, buffer, 0, blocks[bIndex].data.length);
			return true;
		}
		else if(blocks[bIndex].dirtyBit == 1){
			SysLib.rawwrite(blocks[bIndex].blockFrameNumber, blocks[bIndex].data);
			blocks[bIndex].blockFrameNumber = blockId;
			blocks[bIndex].referenceBit = 1;
			blocks[bIndex].dirtyBit = 0;

			byte[] temp = new byte[blocks[bIndex].data.length];
			SysLib.rawread(blocks[bIndex].blockFrameNumber, temp);
			System.arraycopy(temp, 0, blocks[bIndex].data, 0, temp.length);

			System.arraycopy(blocks[bIndex].data, 0, buffer, 0, blocks[bIndex].data.length);
			return true;
		}
		
		
		return false;
	}

	//set dirty bit and write
	synchronized boolean write(int blockId, byte buffer[]){
		if(blockId < 0 || blockId >= 1000)return false;
		//check if block is cached		
		for(int i = 0; i < blocks.length; i++){
			if(blocks[i].blockFrameNumber == blockId){
				blocks[i].dirtyBit = 1;
				System.arraycopy(buffer, 0, blocks[i].data, 0, buffer.length);
				blocks[i].referenceBit = 1;
				return true;
			}
		}
		//data not cached
		//find free block
		for(int i = 0; i < blocks.length; i++)
			if(blocks[i].blockFrameNumber == -1){
				blocks[i].dirtyBit = 1;
				blocks[i].blockFrameNumber = blockId;
				System.arraycopy(buffer, 0, blocks[i].data, 0, buffer.length);
				return true;
			}

		int bIndex = 0;
		while(blocks[bIndex % blocks.length].referenceBit != 0){			
			blocks[bIndex % blocks.length].referenceBit = 0;
			bIndex++;

		}
		bIndex %= blocks.length;
		//check dirty bit
		//up to date, no need to write to disk
		if(blocks[bIndex].dirtyBit == 0){
			SysLib.rawwrite(blocks[bIndex].blockFrameNumber, blocks[bIndex].data);

			blocks[bIndex].blockFrameNumber = blockId;
			blocks[bIndex].referenceBit = 1;
				
			blocks[bIndex].dirtyBit = 1;
			System.arraycopy(buffer, 0, blocks[bIndex].data, 0, buffer.length);
			return true;
		}
		else if(blocks[bIndex].dirtyBit == 1){
			SysLib.rawwrite(blocks[bIndex].blockFrameNumber, blocks[bIndex].data);

			blocks[bIndex].blockFrameNumber = blockId;
			blocks[bIndex].referenceBit = 1;
			blocks[bIndex].dirtyBit = 1;

			System.arraycopy(buffer, 0, blocks[bIndex].data, 0, buffer.length);
			return true;
		}
		
		
		return false;
	}

	//write blocks with dirty bit set to disk, reset dirty bits
	void sync(){
		for(int i = 0; i < blocks.length; i++){
			if(blocks[i].dirtyBit == 1){
				blocks[i].dirtyBit = 0;
				SysLib.rawwrite(blocks[i].blockFrameNumber, blocks[i].data);
			}
		}
	}

	//Clear all cache
	void flush(){
		for(int i = 0; i < blocks.length; i++){
			this.blocks[i] = new Block();	
			this.blocks[i].data = new byte[blocksz];
		}
	}

}
