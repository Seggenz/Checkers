public enum PieceType {
    RED(1),WHITE(-1), RED_QUEEN(1), WHITE_QUEEN(-1);

    final int moveDir;

    PieceType(int moveDir) {
        this.moveDir = moveDir;
    }

    public int getMoveDir() {
        return moveDir;
    }

    public PieceType getQueenType() {
        if (this == RED) {
            return RED_QUEEN;
        } else if (this == WHITE) {
            return WHITE_QUEEN;
        } else {
            return this;
        }
    }
}
