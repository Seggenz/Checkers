import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class ComputerGameEasy extends Game {

    @Override
    public void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (currentPlayer == PieceType.WHITE && !gameOver ) {
                makeRandomMove();
            }
            startTimer();
        }));
        timer.play();
    }

}
