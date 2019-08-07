package kipoderax.virtuallotto.auth.controllers;

import kipoderax.virtuallotto.commons.displays.MainPageDisplay;
import kipoderax.virtuallotto.commons.forms.RegisterForm;
import kipoderax.virtuallotto.auth.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

    private UserService userService;
    private MainPageDisplay mainPageDisplay;

    public RegisterController(UserService userService,
                              MainPageDisplay mainPageDisplay) {

        this.userService = userService;
        this.mainPageDisplay = mainPageDisplay;
    }

    @GetMapping("/register")
    public String register(Model model) {

        model.addAttribute("registerForm", new RegisterForm());
        mainPageDisplay.displayGameStatus(model);

        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterForm registerForm,
                           Model model) {

        mainPageDisplay.displayGameStatus(model);

        if (!userService.register(registerForm, model)) {

            return "auth/register";
        }

        return "redirect:/login";
    }
}