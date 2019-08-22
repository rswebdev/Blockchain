package blockchain;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatterBuilder;
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

        Block workedOnBlock;
        //int askedTimes = 0;
        Blockchain.debugOutput(String.format("Miner # %d asks for block...", minerId), Blockchain.LOG_TYPE.DEBUG, Blockchain.LOG_SENDER.MINER);
        do {
            workedOnBlock = new Block(blockchain.getBlockToMine());
            //askedTimes++;
        } while (workedOnBlock.isValidated());// && askedTimes < 10);

        //Blockchain.debugOutput(String.format("Miner # %d received block %s", minerId, workedOnBlock), Blockchain.LOG_TYPE.DEBUG, Blockchain.LOG_SENDER.MINER);

        //if (!workedOnBlock.isValidated()) {
            Blockchain.debugOutput(String.format("Miner # %d received block %d...", minerId, workedOnBlock.hashCode()), Blockchain.LOG_TYPE.DEBUG, Blockchain.LOG_SENDER.MINER);

            workedOnBlock.setMiner(String.valueOf(minerId));
            workedOnBlock.addPayload(String.format("Miner # %d: %s finished hashing", minerId, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())));

            Blockchain.debugOutput(String.format("Miner # %d started hashing of block #%d...", minerId, workedOnBlock.id), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.MINER);
            String magicNumber;
            do {
                magicNumber = String.valueOf(workedOnBlock.random.nextInt() & Integer.MAX_VALUE);
                workedOnBlock.add("magicnumber", magicNumber);
                workedOnBlock.blockHash = workedOnBlock.sha256();
            } while (!workedOnBlock.blockHash.startsWith("0".repeat(workedOnBlock.zeroes)) && !Thread.currentThread().isInterrupted());

            if (!workedOnBlock.blockHash.startsWith("0".repeat(workedOnBlock.zeroes)) && Thread.currentThread().isInterrupted()) {
                Blockchain.debugOutput("Received \"Give up\" from application...", Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.MINER);
            } else {
                long creationEnd = new Date().getTime();
                workedOnBlock.creationDuration = creationEnd - workedOnBlock.creationStart;
                Blockchain.debugOutput(String.format("Miner # %d finished hashing of block #%d in %f seconds...", minerId, workedOnBlock.id, (float) workedOnBlock.creationDuration / 1000), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.MINER);
                Blockchain.debugOutput(String.format("Miner # %d adds block #%d to chain...", minerId, workedOnBlock.id), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.MINER);
                blockchain.receiveHash(workedOnBlock);
                Blockchain.debugOutput(String.format("Miner # %d is done with block #%d...", minerId, workedOnBlock.id), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.MINER);
            }
        //}
    }
}
