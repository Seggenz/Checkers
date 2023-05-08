public enum PieceType {
    RED(1),WHITE(-1), RED_QUEEN(1), WHITE_QUEEN(-1);

    final int moveDir;

    PieceType(int moveDir) {
        this.moveDir = moveDir;
    }
}
