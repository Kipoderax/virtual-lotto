package kipoderax.virtuallotto.auth.service;

import kipoderax.virtuallotto.auth.entity.User;
import kipoderax.virtuallotto.commons.forms.LoginForm;
import kipoderax.virtuallotto.commons.forms.RegisterForm;
import kipoderax.virtuallotto.auth.repositories.UserRepository;
import kipoderax.virtuallotto.game.entity.ApiNumber;
import kipoderax.virtuallotto.game.entity.Game;
import kipoderax.virtuallotto.game.entity.UserExperience;
import kipoderax.virtuallotto.game.model.GameModel;
import kipoderax.virtuallotto.game.repository.ApiNumberRepository;
import kipoderax.virtuallotto.game.repository.GameRepository;
import kipoderax.virtuallotto.game.repository.UserExperienceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    public enum LoginResponse {
        SUCCESS, FAILED
    }

    private final UserSession userSession;

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserExperienceRepository userExperienceRepository;
    private final ApiNumberRepository apiNumberRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       UserSession userSession,
                       GameRepository gameRepository,
                       UserExperienceRepository userExperienceRepository,
                       ApiNumberRepository apiNumberRepository) {

        this.userSession = userSession;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.userExperienceRepository = userExperienceRepository;
        this.apiNumberRepository = apiNumberRepository;
    }

    public boolean register(RegisterForm registerForm) {
        GameModel gameModel = new GameModel();

        User user = new User();
        Game game = new Game();
        UserExperience userExperience = new UserExperience();
        ApiNumber apiNumber = new ApiNumber();

        if (isLoginFree(registerForm.getLogin())){

            return false;
        }

        user.setUsername(registerForm.getUsername());
        user.setLogin(registerForm.getLogin());
        user.setPassword(bCryptPasswordEncoder().encode(registerForm.getPassword()));
        user.setEmail(registerForm.getEmail());
        user.setSaldo(30);
        user.setDateOfCreatedAccount(new Date());

        game.setMaxBetsToSend(10);
        game.setNumberGame(0);
        game.setCountOfThree(0);
        game.setCountOfFour(0);
        game.setCountOfFive(0);
        game.setCountOfSix(0);

        userExperience.setLevel(1);
        userExperience.setExperience(0);

        apiNumber.setNumber1(gameModel.getConvertToJson().getLastLottoNumbers().get(0));
        apiNumber.setNumber2(gameModel.getConvertToJson().getLastLottoNumbers().get(1));
        apiNumber.setNumber3(gameModel.getConvertToJson().getLastLottoNumbers().get(2));
        apiNumber.setNumber4(gameModel.getConvertToJson().getLastLottoNumbers().get(3));
        apiNumber.setNumber5(gameModel.getConvertToJson().getLastLottoNumbers().get(4));
        apiNumber.setNumber6(gameModel.getConvertToJson().getLastLottoNumbers().get(5));

        game.setUser(user);
        user.setGame(game);
        userExperience.setUser(user);
        apiNumber.setUser(user);

        gameRepository.save(game);
        userRepository.save(user);
        userExperienceRepository.save(userExperience);
        apiNumberRepository.save(apiNumber);

        return true;
    }

    public boolean isLoginFree(String login) {

        return userRepository.existsByLogin(login);
    }

    public LoginResponse login(LoginForm loginForm) {
        Optional<User> userOptional =
                userRepository.findByLogin(loginForm.getLogin());

        if (!userOptional.isPresent()) {

            return LoginResponse.FAILED;
        }
        if (!bCryptPasswordEncoder().matches(
                loginForm.getPassword(), userOptional.get().getPassword()
        )) {

            return LoginResponse.FAILED;
        }

        userSession.setUserLogin(true);
        userSession.setUser(userOptional.get());

        return LoginResponse.SUCCESS;
    }

    public void logout() {

        userSession.setUserLogin(false);
        userSession.setUser(null);

    }

    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

}