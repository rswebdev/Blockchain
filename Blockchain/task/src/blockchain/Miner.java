package blockchain;

import java.util.Date;

public class Miner implements Runnable {

    private final Blockchain blockchain;
    private int minerId;

    Miner(Blockchain blockchain, int minerId) {
        this.blockchain = blockchain;
        this.minerId = minerId;
    }

    @Override
    public void run() {

        Blockchain.debugOutput(String.format("Miner # %d generates a block...", minerId), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.MINER);

        Block block;

        do {
            block = blockchain.getBlockToMine();
        } while (block.isValidated());

        block.setMiner(String.valueOf(minerId));
        block.addPayload(String.format("Miner # %d: here comes the block, da da da daaa", minerId));

        Blockchain.debugOutput(String.format("Miner # %d started hashing of block #%d...", minerId, block.id), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.MINER);
        String magicNumber;
        do {
            magicNumber = String.valueOf(block.random.nextInt() & Integer.MAX_VALUE);
            block.add("magicnumber", magicNumber);
            block.blockHash = block.sha256();
        } while (!block.blockHash.startsWith("0".repeat(block.zeroes)) && !Thread.currentThread().isInterrupted());

        if (!block.blockHash.startsWith("0".repeat(block.zeroes)) && Thread.currentThread().isInterrupted()) {
            Blockchain.debugOutput("Received \"Give up\" from application...", Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.MINER);
        } else {
            long creationEnd = new Date().getTime();
            block.creationDuration = creationEnd - block.creationStart;
            Blockchain.debugOutput(String.format("Hashing of Block (%s-%d) done in %f", minerId, block.id, (float) block.creationDuration / 1000), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.MINER);
            Blockchain.debugOutput(String.format("Miner # %d finished hashing of block #%d...", minerId, block.id), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.MINER);

            Blockchain.debugOutput(String.format("Miner # %d adds block #%d to chain...", minerId, block.id), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.MINER);
            blockchain.receiveHash(block);
            Blockchain.debugOutput(String.format("Miner # %d is done with block #%d...", minerId, block.id), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.MINER);
        }
    }
}
