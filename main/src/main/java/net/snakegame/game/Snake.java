package net.snakegame.game;

/** Klasse mit den Daten für die Schlange auf dem Spielfeld
 * @author Lennard Rütten
 * 
 */

public class Snake {
    private int bodyLength;
    private SnakeElement head;
    int starting_pos_x, starting_pos_y;

    /**Speichert die Richtung, in die sich die Schlange bewegen soll (standardmäßig 0, also rechts)
     * r = rechts
     * u = unten
     * l = links
     * o = oben
     */
    private char direction = 'r';

    public Snake(int starting_x, int starting_y){
        this.bodyLength = 1;
        head = new SnakeElement(bodyLength - 1, starting_x, starting_y);
        starting_pos_x = starting_x;
        starting_pos_y = starting_y;
    }

    /**Funktion zum Verlängern oder Kürzen der Schlange*/
    public void add_element() {
        bodyLength += 1;
        SnakeElement temp = head;
        while (temp.next != null){
            temp = temp.next;
        }
        temp.next = new SnakeElement(bodyLength - 1, temp.x_coord, temp.y_coord);
    }
    
    /**Gibt die länge der Schlange zurück
     * @return
     */
    public int get_snake_length(){
        return bodyLength;
    }

    public char get_direction(){
        return direction;
    }

    public int[] get_snake_head_coords() {
        return new int[] {head.x_coord, head.y_coord};
    }  

    /**Bewege die Schlange einen Schritt weiter in die Entsprechende Richtung
     * 
     */
    public void move_snake() {
        // bewege das erste Element der Schlange in die entsprechende Richtung
        switch (direction) {
            case 'r':
                head.set_prev_coords();
                head.x_coord++;
                break;
            case 'u':
                head.set_prev_coords();
                head.y_coord++;
                break;
            case 'l':
                head.set_prev_coords();
                head.x_coord--;
                break;
            case 'o':
                head.set_prev_coords();
                head.y_coord--;
                break;
            default:
                // nichts tun
                break;
        }

        // Rotiere durch die Schlange und bewege jedes Folgeelement an die Position des
        // vorderen Elements
        SnakeElement temp = head.next;
        SnakeElement prev = head;

        while (temp != null) {
            temp.set_prev_coords(); // Merke die vorige Position des Elements
            temp.x_coord = prev.prev_x; // Setze das element auf die Position des vorigen Elements
            temp.y_coord = prev.prev_y;

            prev = temp;
            temp = temp.next;
        }
    }

    public boolean is_snake_at_position(int x, int y){
        SnakeElement temp = head;
        while (temp != null) {
            if (temp.x_coord == x && temp.y_coord == y) {
                return true;
            }
            temp = temp.next;
        }
        return false;
    }

    private class SnakeElement {
        int element_number;
        int x_coord;
        int y_coord;
        int prev_x;
        int prev_y;
        SnakeElement next;

        public SnakeElement(int number, int x, int y){
            this.element_number = number;
            this.x_coord = x;
            this.y_coord = y;
            this.next = null;
        }

        public void set_coords(int x, int y) {
            this.x_coord = x;
            this.y_coord = y;
        }

        public void set_prev_coords(){
            prev_x = x_coord;
            prev_y = y_coord;
        }

        public int[] get_coords() {
            return new int[] {x_coord, y_coord};
        }


    }

    
}
