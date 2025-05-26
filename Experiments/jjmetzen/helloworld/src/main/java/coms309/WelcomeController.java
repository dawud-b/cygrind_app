package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "Your name is Joseph Metzen";
    }

    @GetMapping("/one/{name}")
    public String welcome(@PathVariable String name) {
        return "This is your journey to ProgressiveOverload: " + name;
    }

    @GetMapping("/two/{Manager}")
    public String Manager(@PathVariable String Manager) {return "Welcome to da club " + Manager ;}
}
