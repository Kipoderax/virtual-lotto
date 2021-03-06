package kipoderax.virtuallotto.game.service.user_numbers;

import kipoderax.virtuallotto.auth.entity.HistoryGame;
import kipoderax.virtuallotto.auth.entity.User;
import kipoderax.virtuallotto.commons.dtos.mapper.ApiNumbersMapper;
import kipoderax.virtuallotto.commons.forms.HistoryGameForm;
import kipoderax.virtuallotto.commons.forms.ResultForm;
import kipoderax.virtuallotto.auth.repositories.HistoryGameRepository;
import kipoderax.virtuallotto.auth.repositories.UserRepository;
import kipoderax.virtuallotto.auth.service.UserSession;
import kipoderax.virtuallotto.commons.dtos.mapper.BetNumbersMapper;
import kipoderax.virtuallotto.commons.dtos.models.LottoNumbersDto;
import kipoderax.virtuallotto.commons.validation.InputNumberValidation;
import kipoderax.virtuallotto.game.model.GameModel;
import kipoderax.virtuallotto.game.repository.ApiNumberRepository;
import kipoderax.virtuallotto.game.repository.GameRepository;
import kipoderax.virtuallotto.game.repository.UserBetsRepository;
import kipoderax.virtuallotto.game.repository.UserExperienceRepository;
import kipoderax.virtuallotto.game.service.ConvertToJson;
import kipoderax.virtuallotto.game.service.Experience;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class UserNumbersService {

    private BetNumbersMapper betNumbersMapper;
    private ApiNumbersMapper apiNumbersMapper;

    private UserBetsRepository userBetsRepository;
    private GameRepository gameRepository;
    private UserExperienceRepository userExperienceRepository;
    private UserRepository userRepository;
    private ApiNumberRepository apiNumberRepository;
    private HistoryGameRepository historyGameRepository;

    private WinnerBetsServiceImpl winnerBetsService;

    private UserSession userSession;

    public UserNumbersService(BetNumbersMapper betNumbersMapper,
                              ApiNumbersMapper apiNumbersMapper,

                              UserBetsRepository userBetsRepository,
                              GameRepository gameRepository,
                              UserExperienceRepository userExperienceRepository,
                              UserRepository userRepository,
                              ApiNumberRepository apiNumberRepository,
                              HistoryGameRepository historyGameRepository,

                              WinnerBetsServiceImpl winnerBetsService,

                              UserSession userSession) {

        this.betNumbersMapper = betNumbersMapper;
        this.apiNumbersMapper = apiNumbersMapper;

        this.userBetsRepository = userBetsRepository;
        this.gameRepository = gameRepository;
        this.userExperienceRepository = userExperienceRepository;
        this.userRepository = userRepository;
        this.apiNumberRepository = apiNumberRepository;
        this.historyGameRepository = historyGameRepository;

        this.winnerBetsService = winnerBetsService;

        this.userSession = userSession;
    }


    public void userNumbersDtos(List<LottoNumbersDto> lottoNumbersDtos, int userId) {

        userBetsRepository.findAllById(userId).stream()
                .map(n -> lottoNumbersDtos.add(betNumbersMapper.map(n)))
                .collect(Collectors.toList());

    }

    public List<LottoNumbersDto> getAllUserNumbersById(int userId) {
        List<LottoNumbersDto> lottoNumbersDtos = new ArrayList<>();

        userNumbersDtos(lottoNumbersDtos, userId);

        return lottoNumbersDtos;
    }


    private void userApiNumbers(List<LottoNumbersDto> lottoNumbersDtos, int userId) {

        apiNumberRepository.findAllByUserId(userId).stream()
                .map(n -> lottoNumbersDtos.add(apiNumbersMapper.map(n)))
                .collect(Collectors.toList());

    }

    public List<Integer> getUserApiNumber(int userId) {
        List<LottoNumbersDto> numbersForms = new ArrayList<>();

        userApiNumbers(numbersForms, userId);

        List<Integer> intApiDtos = new ArrayList<>();
        intApiDtos.add(numbersForms.get(0).getNumber1());
        intApiDtos.add(numbersForms.get(0).getNumber2());
        intApiDtos.add(numbersForms.get(0).getNumber3());
        intApiDtos.add(numbersForms.get(0).getNumber4());
        intApiDtos.add(numbersForms.get(0).getNumber5());
        intApiDtos.add(numbersForms.get(0).getNumber6());

        return intApiDtos;
    }

    public ResultForm checkUserNumbers(GameModel gameModel, int userId, String username) {
        winnerBetsService.getWinnerBetsWith3Numbers().clear();
        winnerBetsService.getWinnerBetsWith4Numbers().clear();
        winnerBetsService.getWinnerBetsWith5Numbers().clear();
        winnerBetsService.getWinnerBetsWith6Numbers().clear();

        ResultForm resultForm = new ResultForm();
        HistoryGame historyGame = new HistoryGame();
        User user = userSession.getUser();

        List<LottoNumbersDto> lottoNumbersDtos = new ArrayList<>();
        userNumbersDtos(lottoNumbersDtos, userId);
        Integer maxBetsId = userBetsRepository.AmountBetsByUserId(userId);

        int currentUserNumberGame;
        int currentProfit = gameRepository.findProfit(username);

        if (historyGameRepository.amountBets(username) != null) {
            currentUserNumberGame = historyGameRepository.amountBets(username);
        } else { currentUserNumberGame = 0; }

        int newUserNumberGame = currentUserNumberGame + maxBetsId;
        gameRepository.updateNumberGame(newUserNumberGame, userId);

        int[] goalNumbers = {resultForm.getFailGoal(), resultForm.getGoalOneNumber(), resultForm.getGoal2Numbers(),
                resultForm.getGoal3Numbers(), resultForm.getGoal4Numbers(), resultForm.getGoal5Numbers(),
                resultForm.getGoal6Numbers()};

        for (int i = 0; i < maxBetsId; i++) {
            if (maxBetsId == 0) {
                break;
            } else {
                int success = 0;
                List<Integer> currentNumbers = new ArrayList<>();

                for (int value : gameModel.getLastNumbers().subList(0, 6)) {
                    currentNumbers.add(lottoNumbersDtos.get(i).getNumber1());
                    currentNumbers.add(lottoNumbersDtos.get(i).getNumber2());
                    currentNumbers.add(lottoNumbersDtos.get(i).getNumber3());
                    currentNumbers.add(lottoNumbersDtos.get(i).getNumber4());
                    currentNumbers.add(lottoNumbersDtos.get(i).getNumber5());
                    currentNumbers.add(lottoNumbersDtos.get(i).getNumber6());

                    for (int j = 0; j <= 5; j++) {
                        if (value == currentNumbers.get(j)) {
                            gameModel.getAddGoalNumbers().add(gameModel.getLastNumbers().get(j));
                            success++;
                        }
                    }

                }
                goalBetsWithSuccess(success, currentNumbers);
                upgradeAmountFrom3To6(success, goalNumbers, resultForm);
            }
        }

        saveAmountGoalAfterViewResult(resultForm);
        addUserExperience(gameModel, goalNumbers, resultForm);
        costBets(maxBetsId, gameModel, resultForm);
        earnFromGoalNumbers(goalNumbers, resultForm);
        resultEarn(maxBetsId * gameModel.getRewardsMoney()[0], resultForm.getTotalEarn(), resultForm);
        renewUserSaldo(username, userId, resultForm.getTotalEarn(), maxBetsId);
        saveDateToHistoryUser(gameModel);
        maxBetsForSend(userId, username);

        historyGame.setDateGame(saveDateToHistoryUser(gameModel).substring(0, 10));
        historyGame.setAmountBets(maxBetsId);
        historyGame.setAmountGoalThrees(goalNumbers[3]);
        historyGame.setAmountGoalFours(goalNumbers[4]);
        historyGame.setAmountGoalFives(goalNumbers[5]);
        historyGame.setAmountGoalSixes(goalNumbers[6]);
        historyGame.setExperience(resultForm.getTotalExp());
        historyGame.setResult(resultForm.getFinishResult());
        historyGame.setUser(user);

        historyGameRepository.save(historyGame);
        gameRepository.updateProfit(currentProfit + resultForm.getTotalEarn(), userId);

        return resultForm;
    }

    public String saveDateToHistoryUser(GameModel gameModel) {
        HistoryGameForm historyGameForm = new HistoryGameForm();
        historyGameForm.setDateGame(gameModel.getDateGame().get(0));

        return historyGameForm.getDateGame();
    }

    public int renewUserSaldo(String username, int userId, int totalEarn, int betsSended) {
        int renewSaldo;
        int currentUserSaldo = userRepository.findSaldoByUserId(userId);
        int maxSaldoForUser = userExperienceRepository.findLevelByLogin(username) * 4;

        if (betsSended >= 10) {

            renewSaldo = currentUserSaldo + maxSaldoForUser + totalEarn;
        } else if (betsSended == 0) {

            renewSaldo = currentUserSaldo;
        } else {

            renewSaldo = currentUserSaldo + (betsSended * 3)
                    + (userExperienceRepository.findLevelByLogin(username) * 4) + totalEarn;
        }

        userRepository.updateUserSaldoByLogin(renewSaldo, userId);

        return renewSaldo;
    }

    public void saveAmountGoalAfterViewResult(ResultForm resultForm) {
        int currentAmountOfThree = gameRepository.findCountOfThreeByUsername(userSession.getUser().getUsername());
        int currentAmountOfFour = gameRepository.findCountOfFourByUsername(userSession.getUser().getUsername());
        int currentAmountOfFive = gameRepository.findCountOfFiveByUsername(userSession.getUser().getUsername());
        int currentAmountOfSix = gameRepository.findCountOfSixByUsername(userSession.getUser().getUsername());

        int newAmountOfThree = currentAmountOfThree + resultForm.getGoal3Numbers();
        int newAmountOfFour = currentAmountOfFour + resultForm.getGoal4Numbers();
        int newAmountOfFive = currentAmountOfFive + resultForm.getGoal5Numbers();
        int newAmountOfSix = currentAmountOfSix + resultForm.getGoal6Numbers();

        gameRepository.updateAmountOfThree(newAmountOfThree, userSession.getUser().getId());
        gameRepository.updateAmountOfFour(newAmountOfFour, userSession.getUser().getId());
        gameRepository.updateAmountOfFive(newAmountOfFive, userSession.getUser().getId());
        gameRepository.updateAmountOfSix(newAmountOfSix, userSession.getUser().getId());
    }

    public void upgradeAmountFrom3To6(int success, int[] goalNumbers, ResultForm resultForm) {

        for (int i = 0; i <= 6; i++) {
            if (success == i) {
                goalNumbers[i]++;
            }
        }

        resultForm.setFailGoal(goalNumbers[0]);
        resultForm.setGoalOneNumber(goalNumbers[1]);
        resultForm.setGoal2Numbers(goalNumbers[2]);
        resultForm.setGoal3Numbers(goalNumbers[3]);
        resultForm.setGoal4Numbers(goalNumbers[4]);
        resultForm.setGoal5Numbers(goalNumbers[5]);
        resultForm.setGoal6Numbers(goalNumbers[6]);

    }

    public void addUserExperience(GameModel gameModel, int[] goalNumbers, ResultForm resultForm) {

        Experience experience = new Experience();
        int currentUserExperience = userExperienceRepository.findExpByLogin(userSession.getUser().getUsername());
        int sumExperience = 0;

        for (int i = 1; i <= 6; i++) {
            sumExperience += goalNumbers[i] * gameModel.getRewardsExperience()[i - 1];

        }
        resultForm.setTotalExp(sumExperience);

        int newUserExperience = currentUserExperience + resultForm.getTotalExp();
        userExperienceRepository.updateExperienceById(userSession.getUser().getId(), newUserExperience);
        userExperienceRepository.updateLevelById(userSession.getUser().getId(), experience.currentLevel(newUserExperience));
    }

    public void costBets(int number, GameModel gameModel, ResultForm resultForm) {

        resultForm.setTotalCostBets(number * gameModel.getRewardsMoney()[0]);

    }

    public void earnFromGoalNumbers(int[] goalNumbers, ResultForm resultForm) {

        ConvertToJson convertToJson = new ConvertToJson();

        int sumEarnMoney = 0;
        for (int i = 3; i <= 6; i++) {
            convertToJson.getMoneyRew()[0] = 24;
            if (convertToJson.getMoneyRew()[3] == 0) {
                convertToJson.getMoneyRew()[3] = 2_000_000;
            }
            sumEarnMoney += goalNumbers[i] * convertToJson.getLastWins(convertToJson.getMoneyRew()[i - 3]);
        }

        resultForm.setTotalEarn(sumEarnMoney);

    }

    public void resultEarn(int totalCost, int winPrice, ResultForm resultForm) {
        resultForm.setFinishResult(winPrice + totalCost);
    }

    public boolean isNewNumberApi(List<Integer> lastApiNumberList, List<Integer> apiNumberList) {

        int success = 0;

        for (int value : lastApiNumberList) {

            for (int i = 0; i < lastApiNumberList.size(); i++) {

                if (value == apiNumberList.get(i)) {

                    success++;
                }
            }
        }

        return success == 6;
    }

    public int maxBetsForSend(int userId, String username) {
        int level = userExperienceRepository.findLevelByLogin(username);
        int userSaldo = userRepository.findSaldoByUserId(userId);
        int leftBets;

        if (level > 5) {
            leftBets = Math.min(userSaldo / 3, level * 2);
        } else {
            leftBets = 10;
        }

        gameRepository.updateMaxBetsToSend(leftBets, userId);

        return leftBets;
    }

    public int leftBetsToSend(int userId) {

        return gameRepository.findMaxBetsToSend(userId) - userBetsRepository.AmountBetsByUserId(userId);
    }

    public boolean saveUserInputNumbers(int numbers[], int id) {
        GameModel gameModel = new GameModel();
        InputNumberValidation inputNumberValidation = new InputNumberValidation();
        inputNumberValidation.sort(numbers);

        int currentSaldo = userRepository.findSaldoByUserId(id);
        userBetsRepository.saveInputNumbersByIdUser(id, numbers[0], numbers[1], numbers[2], numbers[3],
                numbers[4], numbers[5]);

        int newSaldo = currentSaldo + gameModel.getRewardsMoney()[0];
        userRepository.updateUserSaldoByLogin(newSaldo, id);

        return true;
    }

    public void goalBetsWithSuccess(int success, List<Integer> listUserBets) {

        LottoNumbersDto lottoNumbersDto = new LottoNumbersDto(listUserBets.get(0), listUserBets.get(1),
                listUserBets.get(2), listUserBets.get(3), listUserBets.get(4), listUserBets.get(5));

        switch (success) {
            case 3:

                winnerBetsService.addWinnerBetsWith3Numbers(lottoNumbersDto);
                break;
            case 4:

                winnerBetsService.addWinnerBetsWith4Numbers(lottoNumbersDto);
                break;
            case 5:

                winnerBetsService.addWinnerBetsWith5Numbers(lottoNumbersDto);
                break;
            case 6:

                winnerBetsService.addWinnerBetsWith6Numbers(lottoNumbersDto);
                break;

            default:
                break;
        }
    }

    public void generateNumber(LottoNumbersDto lottoNumbersDto) {
        SecureRandom randomNumber = new SecureRandom();
        Set<Integer> orderNumberSet = new TreeSet<>();
        int number;

        while (orderNumberSet.size() != 6) {

            number = randomNumber.nextInt(49) + 1;
            orderNumberSet.add(number);
        }

        List<Integer> numberSet = new ArrayList<>(orderNumberSet);

        lottoNumbersDto.setNumber1(numberSet.get(0));
        lottoNumbersDto.setNumber2(numberSet.get(1));
        lottoNumbersDto.setNumber3(numberSet.get(2));
        lottoNumbersDto.setNumber4(numberSet.get(3));
        lottoNumbersDto.setNumber5(numberSet.get(4));
        lottoNumbersDto.setNumber6(numberSet.get(5));

    }
}