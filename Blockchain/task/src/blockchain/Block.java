package blockchain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class Block implements Serializable {

    public static final long serialVersionUID = 0L;

    final int id;

    final String prevHash;

    HashMap<String, String> information = new HashMap<>();
    private ArrayList<String> payload = new ArrayList<>();

    String blockHash = null;

    long creationStart;
    long creationDuration;

    final int zeroes;

    String miner = null;

    Block(int id, String prevHash, int zeroes) {

        this.creationStart = new Date().getTime();

        this.id = id;

        this.prevHash = prevHash;
        this.zeroes = zeroes;

        long timestamp = new Date().getTime();

        this.add("timestamp", String.valueOf(timestamp));
    }

    Block (Block block) {
        id = block.id;
        miner = block.miner;
        blockHash = block.blockHash;
        creationDuration = block.creationDuration;
        creationStart = block.creationStart;
        information = block.information;
        payload = block.payload;
        prevHash = block.prevHash;
        zeroes = block.zeroes;

        long timestamp = new Date().getTime();
        this.add("timestamp", String.valueOf(timestamp));
    }

    void add(String key, String information) {
        this.information.put(key, information);
    }

    void addPayload(String payload) {
        this.payload.add(payload);
    }

    String sha256() {
        return StringUtil.applySha256(this.prevHash + this.id + this.information.toString() + this.payload.toString());
    }

    void setMiner(String miner) {
        this.miner = miner;
    }

    int getNumberOfZeroes() {
        return zeroes;
    }

    boolean isValidated() {
        return this.miner != null && this.blockHash != null;
    }

    @Override
    public String toString() {

        StringBuilder data = new StringBuilder();
        for (String line : this.payload) {
            data.append(line).append("\n");
        }
        return "Block:\n" +
                String.format("Created by miner # %s\n", this.miner) +
                String.format("Id: %d\n", this.id) +
                String.format("Timestamp: %s\n", this.information.get("timestamp")) +
                String.format("Magic number: %s\n", this.information.get("magicnumber")) +
                "Hash of the previous block:\n" +
                this.prevHash + "\n" +
                "Hash of the block:\n" +
                this.blockHash + "\n" +
                "Block data:\n" +
                data.toString() +
                String.format("Block was generating for %.0f seconds", (float) creationDuration / 1000);
    }

    @Override
    public Block clone() throws CloneNotSupportedException {
        super.clone();
        return new Block(this);
    }
}
