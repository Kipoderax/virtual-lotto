package kipoderax.virtuallotto.game.model;

import kipoderax.virtuallotto.game.service.ConvertToJson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class GameModel {

    private ConvertToJson convertToJson = new ConvertToJson();

    private int number; //komponent numberSet
    private int experience;
    private int level;
    private int[] rewardsMoney = {-3, 24, 120, 4_800, 2_000_000};
    private int[] rewardsExperience = {1, 3, 21, 186, 1_985, 15_134};
    private int saldo;
    private int winPerOneGame; //przedstawia zysk dla aktualnej gry

//    private List<Integer> wins = convertToJson.getLastWins(convertToJson.getLastLottoNumbers());
    private List<Integer> lastNumbers = convertToJson.getLastNumbers(convertToJson.getLastLottoNumbers());
    private Integer[] targetEasyVersion = {2, 5, 8, 10, 12, 18}; //losowanie z 25

    private Set<Integer> numberSet = new TreeSet<>(); //zbior 6 wylosowanych liczb
    private List<Integer> addGoalNumbers = new ArrayList<>(); //zbiór trafionych liczb


}