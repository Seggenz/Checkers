import javafx.scene.paint.Color;

public enum PieceType {
    RED(1),WHITE(-1), RED_QUEEN(1), WHITE_QUEEN(-1);

    final int moveDir;

    PieceType(int moveDir) {
        this.moveDir = moveDir;
    }

    public PieceType getQueen() {
        switch (this) {
            case RED:
                return RED_QUEEN;
            case WHITE:
                return WHITE_QUEEN;
            case RED_QUEEN:
                return RED;
            case WHITE_QUEEN:
                return WHITE;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
    public Color getColor() {
        switch (this) {
            case RED:
            case RED_QUEEN:
                return Color.RED;
            case WHITE:
            case WHITE_QUEEN:
                return Color.WHITE;
            default:
                throw new IllegalStateException("Invalid piece type");
        }
    }

}
