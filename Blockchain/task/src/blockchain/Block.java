package blockchain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class Block implements Serializable {

    public static final long serialVersionUID = 0L;

    final int id;

    final String prevHash;

    HashMap<String, String> information = new HashMap<>();

    String blockHash = "";

    Random random;

    long creationStart;
    long creationDuration;

    final int zeroes;

    String miner = "";

    Block(int id, String prevHash, int zeroes) {

        this.creationStart = new Date().getTime();

        this.id = id;

        this.prevHash = prevHash;
        this.zeroes = zeroes;

        random = new Random();

        long timestamp = new Date().getTime();

        this.add("timestamp", String.valueOf(timestamp));
    }

    void add(String key, String information) {
        this.information.put(key, information);
    }

    String sha256() {
        return StringUtil.applySha256(this.prevHash + this.id + this.information.toString());
    }

    void setMiner(String miner) {
        this.miner = miner;
    }

    int getNumberOfZeroes() {
        return zeroes;
    }

    @Override
    public String toString() {

        return "Block:\n" +
                String.format("Created by miner # %s\n", this.miner) +
                String.format("Id: %d\n", this.id) +
                String.format("Timestamp: %s\n", this.information.get("timestamp")) +
                String.format("Magic number: %s\n", this.information.get("magicnumber")) +
                "Hash of the previous block:\n" +
                this.prevHash + "\n" +
                "Hash of the block:\n" +
                this.blockHash + "\n" +
                String.format("Block was generating for %.0f seconds", (float) creationDuration / 1000);
    }
}
