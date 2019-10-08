package hello;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hello.services.AddressService;

@Controller
public class GreetingController {
    ApplicationContext ctx;
    
    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        
        // Test Autowired
        if (ctx == null) {
            ctx = new ClassPathXmlApplicationContext("beans_01.xml");
        }
        
        AddressService addressService = ctx.getBean("addressService", AddressService.class);
        addressService.show();
        
        return "greeting";
    }

}
