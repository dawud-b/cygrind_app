package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple Hello World Controller to display the string returned
 *
 * @author Joseph Metzen
 */

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "To get a list of all users, run GET \"/users\"\n" +
                "To create a new user, run POST \"/users\"\n";
    }
}

