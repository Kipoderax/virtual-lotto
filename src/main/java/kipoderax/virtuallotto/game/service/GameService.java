package kipoderax.virtuallotto.game.service;

import kipoderax.virtuallotto.auth.forms.LoginForm;
import kipoderax.virtuallotto.auth.repositories.UserRepository;
import kipoderax.virtuallotto.game.entity.Game;
import kipoderax.virtuallotto.game.model.GameModel;
import kipoderax.virtuallotto.game.model.GameNoEntity;
import kipoderax.virtuallotto.game.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class GameService {
    private int currentSaldo;
    private int count;
    private int myWin;

    private SecureRandom randomNumber;

    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    private GameModel gameModel = new GameModel();
    private Game game;
    private GameNoEntity games;

    private LoginForm loginForm;

    public GameService(GameRepository gameRepository, UserRepository userRepository,
                       GameNoEntity games) {
        this.randomNumber = new SecureRandom();

        this.gameRepository = gameRepository;
        this.userRepository = userRepository;

        this.game = new Game();
        this.games = games;

//        this.gameModel = new GameModel();
    }

    //SHOW TARGET
    public List<Integer> showTarget() {

        return new ArrayList<>(Arrays.asList(gameModel.getTargetRealVersion()));
    }

    //GENERATE NUMBER
    public Set<Integer> generateNumber(GameModel gameModel) {

        while (gameModel.getNumberSet().size() != 6) {

            gameModel.setNumber(randomNumber.nextInt(49) + 1);
            gameModel.getNumberSet().add(gameModel.getNumber());
        }

        return gameModel.getNumberSet();
    }

    //GOAL NUMBER
    public List<Integer> addGoalNumber(GameModel gameModel) {
//        gameModel.getAddGoalNumbers().clear();
//        Game game = new Game();

        currentSaldo = games.getSaldo();
        count = 0;
        for (int value : gameModel.getNumberSet()) {

            for (int i = 0; i < gameModel.getNumberSet().size(); i++) {

                if (value == gameModel.getTargetRealVersion()[i]) {

                    gameModel.getAddGoalNumbers().add(gameModel.getTargetRealVersion()[i]);
                    count++;
                }
            }
        }

        upgradeCurrentSaldo();

        return gameModel.getAddGoalNumbers();
    }

    //UPGRADE SALDO
    public void upgradeCurrentSaldo() {
        myWin = 0;

        for (int i = 3; i <= games.getRewards().length; i++) {

            if (count == i) {

                currentSaldo += games.getRewards()[i - 2] + games.getRewards()[0];
                games.setSaldo(currentSaldo);
                myWin = games.getRewards()[i-2];
            }
        }

        if (count < 3) {

            currentSaldo += games.getRewards()[0];
            games.setSaldo(currentSaldo);
        }

//        System.out.println(gameRepository.findAllBySaldo(10000));

    }

    //GET SALDO
    public int getSaldo() {

        return currentSaldo;
    }

    //GET ACTUAL WIN
    public int getMyWin() {

        return myWin;
    }

    //INPUT NUMBERS
    public void inputNumbers(int number) {

        Set<Integer> numbersSet = new TreeSet<>();
        gameModel.setNumber(number);
        numbersSet.add(gameModel.getNumber());

    }
}