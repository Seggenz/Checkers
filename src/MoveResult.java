public class MoveResult {

    private MoveType type;
    private Piece piece;
    private boolean additionalCapture;

    public MoveType getType() {
        return type;
    }

    public Piece getPiece() {
        return piece;
    }

    public boolean isAdditionalCapture() {
        return additionalCapture;
    }
    public MoveResult(MoveType type) {
        this(type, null,false);
    }
    public MoveResult(MoveType type, Piece piece) {
        this(type,piece,false);
    }
    public MoveResult(MoveType type, Piece piece, boolean additionalCapture) {
        this.type = type;
        this.piece = piece;
        this.additionalCapture = additionalCapture;
    }



}
