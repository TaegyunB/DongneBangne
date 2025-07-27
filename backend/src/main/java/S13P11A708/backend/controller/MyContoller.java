package S13P11A708.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class MyContoller {

    @GetMapping("/my")
    public String myPage(){
        return "my";
    }
}
