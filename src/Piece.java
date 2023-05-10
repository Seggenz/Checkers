import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;


public class Piece extends StackPane {

    private PieceType type;
//    private PieceType currentPlayer;
    private ImageView crown;
    private double mouseX, mouseY;
    private double oldX, oldY;

    public void setType(PieceType newType) {
        this.type = newType;

        // Update crown visibility based on new type
        crown.setVisible(type == PieceType.RED_QUEEN || type == PieceType.WHITE_QUEEN);
    }

    public PieceType getType() {
        return type;
    }





    public double getOldX() {
        return oldX;
    }

    public double getOldY() {
        return oldY;
    }

    public Piece(PieceType type, int x, int y) {
        this.type = type;
//        this.currentPlayer = currentPlayer;

        crown = new ImageView(new Image("resources/images/crown.png"));
        crown.setFitHeight(Game.TILE_SIZE * 0.6);
        crown.setFitWidth(Game.TILE_SIZE * 0.6);
        crown.setVisible(false);
        crown.setTranslateX((Game.TILE_SIZE - crown.getFitWidth()) / 2);
        crown.setTranslateY((Game.TILE_SIZE - crown.getFitHeight()) / 2);

        move(x , y );

        Ellipse bg = new Ellipse(Game.TILE_SIZE * 0.3125, Game.TILE_SIZE * 0.26 );
        bg.setFill(Color.BLACK);
        bg.setStroke(Color.BLACK);
        bg.setStrokeWidth(Game.TILE_SIZE * 0.03);

        bg.setTranslateX((Game.TILE_SIZE - Game.TILE_SIZE * 0.3125 * 2) / 2) ;
        bg.setTranslateY((Game.TILE_SIZE - Game.TILE_SIZE * 0.26 * 2) / 2 +  Game.TILE_SIZE * 0.07) ;

        Ellipse ellipse = new Ellipse(Game.TILE_SIZE * 0.3125, Game.TILE_SIZE * 0.26 );
        ellipse.setFill(type == PieceType.RED ? Color.valueOf("#c40003") : Color.valueOf("#fff9f4"));
        ellipse.setStroke(Color.BLACK);
        ellipse.setStrokeWidth(Game.TILE_SIZE * 0.03);

        ellipse.setTranslateX((Game.TILE_SIZE - Game.TILE_SIZE * 0.3125 * 2) / 2) ;
        ellipse.setTranslateY((Game.TILE_SIZE - Game.TILE_SIZE * 0.26 * 2) / 2) ;
        
        getChildren().addAll(bg, ellipse);
        getChildren().add(crown);

        setOnMousePressed(e -> {
            if(type == Game.currentPlayer) {
                mouseX = e.getSceneX();
                mouseY = e.getSceneY();
            }

        });
        setOnMouseDragged(e -> {
            if(type == Game.currentPlayer) {
                relocate(e.getSceneX() - mouseX + oldX, e.getSceneY() - mouseY + oldY);
            }
        });

    }

    public void move(int x, int y) {
        oldX = x * Game.TILE_SIZE;
        oldY = y * Game.TILE_SIZE;
        relocate(oldX,oldY);

        if (type == PieceType.RED && y == Game.HEIGHT - 1) {
            setType(PieceType.RED_QUEEN);
        } else if (type == PieceType.WHITE && y == 0) {
            setType(PieceType.WHITE_QUEEN);
        }
    }

    public void abortMove() {
        relocate(oldX, oldY);
    }

}
