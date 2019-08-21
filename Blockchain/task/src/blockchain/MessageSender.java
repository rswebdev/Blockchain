package blockchain;

import java.util.Random;

public class MessageSender implements Runnable {

    private String name;
    private Random random;
    private Blockchain blockchain;

    private final static String[] questions = {
            "Do you recycle? Why/Why not?\n" +
            "Is your town/city polluted? In what way?",
            "Why should people use more public transport?",
            "Do you think the environment is more of a concern now than in the past?",
            "What is your opinion about climate change?",
            "Why is renewable energy better for the environment?",
            "What is the most important thing individuals can do to help the environment?",
            "Do you think the government of your country does enough to help the environment?",
            "What are the biggest signs of pollution in your country?",
            "Should we teach children about the environment in school?",
            "How do you feel about people who deny climate change?",
            "What should be done about people who drop litter in the streets?",
            "What might happen to the environment in the future?",
            "Is it more important to protect the environment or to have a strong economy?",
            "What advice would you give to future generations about the environment?",
            "Do you think people should be more concerned about environmental issues?",
            "Do you worry about the environment? Why/Why not?",
            "What is your opinion of organised groups who protest the environment?",
            "Do you think catastrophes like hurricanes are a result of climate change?",
            "Who is more responsible for the environment: individuals, government or companies?",
    };

    private final static String[] answers = {
        "Yes", "No", "I don't know"
    };

    MessageSender(String name, Blockchain blockchain) {
        this.name = name;
        this.random = new Random();
        this.blockchain = blockchain;
    }

    @Override
    public void run() {
        while(Thread.currentThread().isAlive()) {
            String message = this.name + ": ";
            if (random.nextBoolean()) {
                int index = this.random.nextInt(answers.length);
                message += answers[index];
            } else {
                int index = this.random.nextInt(questions.length);
                message += questions[index];
            }
            blockchain.addData(message);
        }
    }
}
