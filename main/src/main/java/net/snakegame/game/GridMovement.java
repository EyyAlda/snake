package net.snakegame.game;

import com.almasb.fxgl.entity.component.Component;

public class GridMovement extends Component {

    private int[][] grid;

    public GridMovement(int[][] grid){
        this.grid = grid;
    }

}
