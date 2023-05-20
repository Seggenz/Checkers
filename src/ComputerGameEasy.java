import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class ComputerGameEasy extends Game {

    private StopWatch gameStopWatch = new StopWatch();
    public MainView mainView;

    public ComputerGameEasy() {
        this.gameStopWatch = new StopWatch();
    }

    public ComputerGameEasy(MainView mainView) {
        super(mainView);
        this.mainView = mainView;
    }

    public StopWatch getGameStopWatch() {
        return gameStopWatch;
    }
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
