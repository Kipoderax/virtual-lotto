package kipoderax.virtuallotto.game.service;

import kipoderax.virtuallotto.auth.forms.ResultForm;
import kipoderax.virtuallotto.auth.repositories.UserRepository;
import kipoderax.virtuallotto.auth.service.UserSession;
import kipoderax.virtuallotto.dtos.mapper.ApiNumberMapper;
import kipoderax.virtuallotto.dtos.mapper.UserNumbersMapper;
import kipoderax.virtuallotto.dtos.models.ApiNumberDto;
import kipoderax.virtuallotto.dtos.models.UserNumbersDto;
import kipoderax.virtuallotto.game.model.GameModel;
import kipoderax.virtuallotto.game.repository.ApiNumberRepository;
import kipoderax.virtuallotto.game.repository.GameRepository;
import kipoderax.virtuallotto.game.repository.UserBetsRepository;
import kipoderax.virtuallotto.game.repository.UserExperienceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserNumbersService {

    private UserNumbersMapper userNumbersMapper;
    private ApiNumberMapper apiNumberMapper;

    private UserBetsRepository userBetsRepository;
    private GameRepository gameRepository;
    private UserExperienceRepository userExperienceRepository;
    private UserRepository userRepository;
    private ApiNumberRepository apiNumberRepository;

    private UserSession userSession;

    public UserNumbersService(UserNumbersMapper userNumbersMapper,
                              ApiNumberMapper apiNumberMapper,

                              UserBetsRepository userBetsRepository,
                              GameRepository gameRepository,
                              UserExperienceRepository userExperienceRepository,
                              UserRepository userRepository,
                              ApiNumberRepository apiNumberRepository,

                              UserSession userSession) {

        this.userNumbersMapper = userNumbersMapper;
        this.apiNumberMapper = apiNumberMapper;

        this.userBetsRepository = userBetsRepository;
        this.gameRepository = gameRepository;
        this.userExperienceRepository = userExperienceRepository;
        this.userRepository = userRepository;
        this.apiNumberRepository = apiNumberRepository;

        this.userSession = userSession;
    }


    public List<UserNumbersDto> userNumbersDtos(List<UserNumbersDto> userNumbersDtos, int userId) {

        userBetsRepository.findAllById(userId).stream()
                .map(n -> userNumbersDtos.add(userNumbersMapper.map(n)))
                .collect(Collectors.toList());

        return userNumbersDtos;
    }

    public List<UserNumbersDto> getAllUserNumbersById(int userId) {
        List<UserNumbersDto> userNumbersDtos = new ArrayList<>();

        userNumbersDtos(userNumbersDtos, userId);

        return userNumbersDtos;
    }

    public List<ApiNumberDto> userApiNumbers(List<ApiNumberDto> apiNumberDtos, int userId) {

        apiNumberRepository.findAllByUserId(userId).stream()
                .map(n -> apiNumberDtos.add(apiNumberMapper.map(n)))
                .collect(Collectors.toList());

        return apiNumberDtos;
    }

    public List<Integer> getUserApiNumber(int userId) {
        List<ApiNumberDto> apiNumberDtos = new ArrayList<>();

        userApiNumbers(apiNumberDtos, userId);

        List<Integer> intApiDtos = new ArrayList<>();
        intApiDtos.add(apiNumberDtos.get(0).getNumber1());
        intApiDtos.add(apiNumberDtos.get(0).getNumber2());
        intApiDtos.add(apiNumberDtos.get(0).getNumber3());
        intApiDtos.add(apiNumberDtos.get(0).getNumber4());
        intApiDtos.add(apiNumberDtos.get(0).getNumber5());
        intApiDtos.add(apiNumberDtos.get(0).getNumber6());

        return intApiDtos;
    }

    public ResultForm checkUserNumbers (int userId, GameModel gameModel) {
        ResultForm resultForm = new ResultForm();
        List<UserNumbersDto> userNumbersDtos = new ArrayList<>();
        userNumbersDtos(userNumbersDtos, userId);
        Integer maxBetsId = userBetsRepository.AmountBetsByUserId(userId);
        int currentUserNumberGame = gameRepository.findNumberGameByLogin(userSession.getUser().getLogin());

        theNumberOfTheGame(maxBetsId);
        int newUserNumberGame = currentUserNumberGame + maxBetsId;
        gameRepository.updateNumberGame(newUserNumberGame, userId);

        int[] goalNumbers = {resultForm.getFailGoal(), resultForm.getGoalOneNumber(), resultForm.getGoal2Numbers(),
                        resultForm.getGoal3Numbers(), resultForm.getGoal4Numbers(), resultForm.getGoal5Numbers(),
                        resultForm.getGoal6Numbers()};

        for (int i = 0; i < maxBetsId; i++) {
            if (maxBetsId == 0) {
                break;
            }
            else {
                int success = 0;
                List<Integer> currentNumbers = new ArrayList<>();
                for (int value : gameModel.getLastNumbers()) {
                    currentNumbers.add(userNumbersDtos.get(i).getNumber1());
                    currentNumbers.add(userNumbersDtos.get(i).getNumber2());
                    currentNumbers.add(userNumbersDtos.get(i).getNumber3());
                    currentNumbers.add(userNumbersDtos.get(i).getNumber4());
                    currentNumbers.add(userNumbersDtos.get(i).getNumber5());
                    currentNumbers.add(userNumbersDtos.get(i).getNumber6());

                    for (int j = 0; j <= 5; j++) {
                        if (value == currentNumbers.get(j)) {
                            gameModel.getAddGoalNumbers().add(gameModel.getLastNumbers().get(j));
                            success++;
                        }
                    }

                }
                upgradeAmountFrom3To6(success, goalNumbers, resultForm);
            }
        }

        addUserExperience(gameModel, goalNumbers, resultForm);
        costBets(maxBetsId, gameModel, resultForm);
        earnFromGoalNumbers(goalNumbers, gameModel, resultForm);
        resultEarn(maxBetsId * gameModel.getRewardsMoney()[0], resultForm.getTotalEarn(), resultForm);

        return resultForm;
    }

    public void upgradeAmountFrom3To6(int success, int[] goalNumbers, ResultForm resultForm) {

        int currentAmountOfThree = gameRepository.findCountOfThreeByLogin(userSession.getUser().getLogin());
        int currentAmountOfFour = gameRepository.findCountOfFourByLogin(userSession.getUser().getLogin());
        int currentAmountOfFive = gameRepository.findCountOfFiveByLogin(userSession.getUser().getLogin());
        int currentAmountOfSix = gameRepository.findCountOfSixByLogin(userSession.getUser().getLogin());

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

        int newAmountOfThree = currentAmountOfThree + resultForm.getGoal3Numbers();
        int newAmountOfFour = currentAmountOfFour + resultForm.getGoal4Numbers();
        int newAmountOfFive = currentAmountOfFive + resultForm.getGoal5Numbers();
        int newAmountOfSix = currentAmountOfSix + resultForm.getGoal6Numbers();

        gameRepository.updateAmountOfThree(newAmountOfThree, userSession.getUser().getId());
        gameRepository.updateAmountOfFour(newAmountOfFour, userSession.getUser().getId());
        gameRepository.updateAmountOfFive(newAmountOfFive, userSession.getUser().getId());
        gameRepository.updateAmountOfSix(newAmountOfSix, userSession.getUser().getId());
    }

    public int addUserExperience(GameModel gameModel, int[] goalNumbers, ResultForm resultForm) {

        Experience experience = new Experience();
        int currentUserExperience = userExperienceRepository.findExpByLogin(userSession.getUser().getLogin());
        int sumExperience = 0;

        for (int i = 1; i <= 6; i++) {
            sumExperience += goalNumbers[i] * gameModel.getRewardsExperience()[i-1];

        }
        resultForm.setTotalExp(sumExperience);

        int newUserExperience = currentUserExperience + resultForm.getTotalExp();
        userExperienceRepository.updateExperienceById(userSession.getUser().getId(), newUserExperience);
        userExperienceRepository.updateLevelById(userSession.getUser().getId(), experience.reachNextLevel(newUserExperience));
        return resultForm.getTotalExp();
    }

    public int theNumberOfTheGame(int number) {

        return number;
    }

    public int costBets(int number, GameModel gameModel, ResultForm resultForm) {

        resultForm.setTotalCostBets(number * gameModel.getRewardsMoney()[0]);

        return resultForm.getTotalCostBets();
    }

    public int earnFromGoalNumbers (int[] goalNumbers, GameModel gameModel, ResultForm resultForm) {

        int sumEarnMoney = 0;
        for (int i = 3; i <= 6; i++) {
            sumEarnMoney += goalNumbers[i] * gameModel.getRewardsMoney()[i-2];
        }

        resultForm.setTotalEarn(sumEarnMoney);

        return resultForm.getTotalEarn();
    }

    public int resultEarn(int totalCost, int winPrice, ResultForm resultForm) {

        int currentUserSaldo = userRepository.findSaldoByLogin(userSession.getUser().getLogin());

        resultForm.setFinishResult(winPrice + totalCost);

        int newUserSaldo = currentUserSaldo + resultForm.getFinishResult();
        userRepository.updateUserSaldoByLogin(newUserSaldo, userSession.getUser().getLogin());

        return resultForm.getFinishResult();
    }

    public boolean isNewNumberApi(List<Integer> lastApiNumberList, List<Integer> apiNumberList) {

        int success = 0;

        for (int value : lastApiNumberList) {

            for (int i = 0; i < lastApiNumberList.size(); i++) {

                //jesli ktoras wartosc liczb losowo wygenerowanych znajduje sie w tablicy
                if (value == apiNumberList.get(i)) {

                    success++;
                }
            }
        }

        return success == 6;

    }

}