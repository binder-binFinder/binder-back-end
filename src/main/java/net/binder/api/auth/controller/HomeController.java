package net.binder.api.auth.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
public class HomeController {

    // AWS ELB health 응답용 API
    @GetMapping("/")
    public void home() {
    }
}
