package net.snakegame.game;

public class Snake {
    private int bodyLength;

    public Snake(){
        this.bodyLength = 1;
    }

    public Snake(int length){
        if (length > 0) {
            this.bodyLength = length;
        } else {
            this.bodyLength = 1;
        }
    }

    public void change_snake_size(int amount) {
        bodyLength += amount;
    }

    public int get_snake_length(){
        return bodyLength;
    }
    
}
