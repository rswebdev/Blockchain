package blockchain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.NoSuchElementException;

class Blockchain implements Serializable {

    public static final long serialVersionUID = 0L;

    LinkedList<Block> blocks = new LinkedList<>();
    private int nextId = 1;
    private int numberOfZeroes;
    private static boolean logDebugOutput = false;
    private static boolean printOutput = false;
    private Block newBlock;

    enum LOG_TYPE {
        INFO, IMPORTANT, DEBUG
    }

    enum LOG_SENDER {
        CHAIN("[B]-[B]-[B]"),
        MINER("{M} --> [B]"),
        APP  ("APPLICATION");

        String shortName;
        LOG_SENDER(String shortName) {
            this.shortName = shortName;
        }
        public String getShortName() {
            return shortName;
        }
    }

    Blockchain(int numberOfZeroes) {
        this.numberOfZeroes = numberOfZeroes;
         newBlock = new Block(nextId(), lastHash(), getZeroes());
    }

    void setNumbersOfZeroes(int numberOfZeroes) {
        this.numberOfZeroes = numberOfZeroes;
    }

    private int nextId() {
        return nextId;
    }

    private String lastHash() {
        String lastHash = "0";
        if (blocks.size() > 0) {
            lastHash = blocks.getLast().blockHash;
        }
        return lastHash;
    }

    private int getZeroes() {
        return numberOfZeroes;
    }

    private void thinkAboutNumbersOfZeroes(float creationTime) {
        if (creationTime > 120.0) {
            numberOfZeroes--;
            numberOfZeroes--;
            debugOutput("Creation took too long, decreasing N to " + numberOfZeroes, LOG_TYPE.IMPORTANT, LOG_SENDER.CHAIN);
        } else if (creationTime > 60.0) {
            numberOfZeroes--;
            debugOutput("Creation took too long, decreasing N to " + numberOfZeroes, LOG_TYPE.IMPORTANT, LOG_SENDER.CHAIN);
        } else if (creationTime < 10.0) {
            numberOfZeroes++;
            debugOutput("Creation was too fast, increasing N to " + numberOfZeroes, LOG_TYPE.IMPORTANT, LOG_SENDER.CHAIN);
        }
    }

    Block getBlockToMine() {
        if (blocks.size() > 0) {
            return blocks.getLast();
        } else {
            return newBlock;
        }
    }

    void receiveHash(Block block) {
        debugOutput(String.format("Receiving block #%d from miner # %s...", block.id, block.miner), LOG_TYPE.INFO, LOG_SENDER.CHAIN);
        if (block.id == nextId) {
            if (validateBlock(block, this)) {
                nextId++;
                blocks.removeLast();
                blocks.add(block);
                newBlock = new Block(nextId(), lastHash(), getZeroes());
                thinkAboutNumbersOfZeroes(block.creationDuration / 1000);
                debugOutput(String.format("Accepting block #%d from miner # %s. You're the fastest.", block.id, block.miner), LOG_TYPE.IMPORTANT, LOG_SENDER.CHAIN);
            }
        } else {
            debugOutput(String.format("Rejecting block #%d from miner # %s. Someone else was faster.", block.id, block.miner), LOG_TYPE.INFO, LOG_SENDER.CHAIN);
        }
    }

    void addData(String data) {
        newBlock.addPayload(data);
    }

    void print() {
        print(0, blocks.size());
    }

    void print(int blocksFromStartOrEnd) {
        if (blocksFromStartOrEnd == 0) {
            try {
                Block b = blocks.get(0);
                System.out.println(b);
                System.out.println();
            } catch (IndexOutOfBoundsException e) {
                //
            }
        } else {

            int from;
            int to;

            if (blocksFromStartOrEnd < 0) {
                from = blocks.size() + blocksFromStartOrEnd;
                to = blocks.size();
            } else {
                from = 0;
                to = blocksFromStartOrEnd;
            }

            print(from, to);
        }
    }

    void print(int from, int to) throws IndexOutOfBoundsException {
        if (from > blocks.size()-1) {
            throw new IndexOutOfBoundsException(String.format("List of Blocks is smaller than %d, %d", from, to));
        } else if (from > to) {
            throw new IndexOutOfBoundsException(String.format("From (%d) needs to be larger than To (%d)", from, to));
        }
        to = Math.min(to, blocks.size());

        int lastNumberOfZeroes = 0;

        for (int i = from; i < to; i++) {
            try {
                Block b = blocks.get(i);
                System.out.println(b);
                if (b.getNumberOfZeroes() > lastNumberOfZeroes) {
                    System.out.println("N was increased to " + b.getNumberOfZeroes());
                } else if (b.getNumberOfZeroes() < lastNumberOfZeroes) {
                    System.out.println("N was decreased by " + (lastNumberOfZeroes - b.getNumberOfZeroes()));
                } else {
                    System.out.println("N stays the same");
                }
                lastNumberOfZeroes = b.getNumberOfZeroes();
                System.out.println();
            } catch (IndexOutOfBoundsException e) {
                //
            }
        }
    }

    static boolean isValid(LinkedList<Block> chain) {
        String lastPrevHash = "";
        String lastBlockHash = "";
        boolean valid = true;
        for (Block b : chain) {
            if ("".equals(lastPrevHash)) {
                lastPrevHash = "0";
                lastBlockHash = b.sha256();
            } else {
                valid = valid && lastBlockHash.equals(b.prevHash);
                debugOutput(lastBlockHash, LOG_TYPE.DEBUG, LOG_SENDER.CHAIN);
                debugOutput(b.prevHash, LOG_TYPE.DEBUG, LOG_SENDER.CHAIN);
                lastBlockHash = b.sha256();
                lastPrevHash = b.prevHash;
            }
        }
        return valid;
    }

    private static boolean validateBlock(Block block, Blockchain blockchain) {
        LinkedList<Block> validationChain = new LinkedList<>(blockchain.blocks);
        validationChain.add(block);
        return isValid(validationChain);
    }

    void validate() {
        if (isValid(blocks)) {
            debugOutput("Chain is valid!", LOG_TYPE.IMPORTANT, LOG_SENDER.CHAIN);
        } else {
            debugOutput("Chain is NOT valid!", LOG_TYPE.IMPORTANT, LOG_SENDER.CHAIN);
        }
    }

    static void setOutputOptions(boolean printOutput, boolean logDebugOutput) {
        Blockchain.printOutput = printOutput;
        Blockchain.logDebugOutput = logDebugOutput;
    }

    static void debugOutput(String s, LOG_TYPE type, LOG_SENDER sender) {
        if (printOutput) {
            if (type.equals(LOG_TYPE.IMPORTANT)) {
                s = "(!) " + s.toUpperCase();
            } else {
                s = "    (i) " + s;
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date now = new Date();
            String formattedDateTime = simpleDateFormat.format(now);

            if (!type.equals(LOG_TYPE.DEBUG) || logDebugOutput) {
                System.out.println(String.format("%s - (%s) : %s", formattedDateTime, sender.getShortName(), s));
            }
        }
    }
}
