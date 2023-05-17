public class MoveResult {

    private MoveType type;
    private Piece piece;
    private boolean additionalCapture;
    private int capturedPieces;

    public MoveType getType() {
        return type;
    }

    public Piece getPiece() {
        return piece;
    }
    public int getCapturedPieces() {
        return capturedPieces;
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
    public MoveResult(MoveType type, Piece piece, int capturedPieces) {
        this.type = type;
        this.piece = piece;
        this.capturedPieces = capturedPieces;
    }
    public MoveResult(MoveType type, Piece piece, boolean additionalCapture, int capturedPieces) {
        this.type = type;
        this.piece = piece;
        this.additionalCapture = additionalCapture;
        this.capturedPieces = capturedPieces;
    }



}
