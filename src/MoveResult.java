public class MoveResult { //klasa sluzaca do okreslania typow ruchu

    private final MoveType type;
    private final Piece piece;
    private boolean additionalCapture;
    private int capturedPieces;

    public MoveType getType() { //typ ruchu
        return type;
    }

    public Piece getPiece() {
        return piece;
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
    public MoveResult(MoveType type, Piece piece, boolean additionalCapture, int capturedPieces) {
        this.type = type;
        this.piece = piece;
        this.additionalCapture = additionalCapture;
        this.capturedPieces = capturedPieces;
    }



}
