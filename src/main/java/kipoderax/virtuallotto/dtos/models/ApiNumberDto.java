package kipoderax.virtuallotto.dtos.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class ApiNumberDto {

    private int number1;
    private int number2;
    private int number3;
    private int number4;
    private int number5;
    private int number6;
}
