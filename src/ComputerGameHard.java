//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//public class ComputerGameHard extends Game {
//
//    public void makeBestMove() {
//        PieceType opponent = currentPlayer == PieceType.RED ? PieceType.WHITE : PieceType.RED;
//
//        // Check for all pieces if a capture move is possible
//        for (int y = 0; y < HEIGHT; y++) {
//            for (int x = 0; x < WIDTH; x++) {
//                Tile tile = board[x][y];
//                if (tile.hasPiece() && tile.getPiece().getType() == currentPlayer) {
//                    MoveResult result = tryCaptureMove(tile.getPiece());
//                    if (result.getType() == MoveType.KILL) {
//                        // If a capture move is possible, execute it
//                        executeMove(result);
//                        return;
//                    }
//                }
//            }
//        }
//
//        // If no capture move is possible, move the farthest piece forward
//        int startY = currentPlayer == PieceType.RED ? 0 : HEIGHT - 1;
//        int direction = currentPlayer == PieceType.RED ? 1 : -1;
//        for (int y = startY; y >= 0 && y < HEIGHT; y += direction) {
//            for (int x = 0; x < WIDTH; x++) {
//                Tile tile = board[x][y];
//                if (tile.hasPiece() && tile.getPiece().getType() == currentPlayer) {
//                    MoveResult result = tryNormalMove(tile.getPiece());
//                    if (result.getType() == MoveType.NORMAL) {
//                        // If a normal move is possible, execute it
//                        executeMove(result);
//                        return;
//                    }
//                }
//            }
//        }
//    }
//
//}
