package kipoderax.virtuallotto.commons.dtos.mapper;

import kipoderax.virtuallotto.auth.entity.HistoryGame;
import kipoderax.virtuallotto.commons.dtos.models.HistoryGameDto;
import kipoderax.virtuallotto.commons.dtos.models.HistoryGameDtoBuilder;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class HistoryMapper implements Mapper<HistoryGame, HistoryGameDto> {

    @Override
    public HistoryGameDto map(HistoryGame from) {

        return new HistoryGameDtoBuilder()
                .username(from.getUser().getUsername())
                .dateGame(from.getDateGame())
                .amountBets(from.getAmountBets())
                .amountGoalThrees(from.getAmountGoalThrees())
                .amountGoalFours(from.getAmountGoalFours())
                .amountGoalFives(from.getAmountGoalFives())
                .amountGoalSixes(from.getAmountGoalSixes())
                .experience(from.getExperience())
                .result(from.getResult())
                .build();
    }
}
