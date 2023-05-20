package com.me.hash.demo.controllers.rest

import com.me.hash.demo.services.UserInputProcessingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

@Controller
@RequestMapping("/ui")
class UiController {

    @Autowired
    UserInputProcessingService service

    @GetMapping
    public String ui() {
        'ui'
    }

    @GetMapping("/debug")
    public String debug(Model model) {
        model.addAttribute('debug', service.debug(''))
        'debug'
    }

    @PostMapping("/boot")
    public RedirectView boot(@RequestParam(name="boot_argument", required=true, defaultValue="root") String bootArgument) {
        service.boot(bootArgument)
        new RedirectView('/ui')
    }

    @PostMapping("/search")
    public RedirectView download(@RequestParam(name="find_rule", required=true) String findRule,
                           @RequestParam(name="store_path", required=true) String storePath,
                           @RequestParam(name="file_name", required=true) String fileName) {
        service.download(findRule, storePath, fileName)
        new RedirectView('/ui')
    }

    @PostMapping("/publish")
    public RedirectView publish(@RequestParam(name="find_rule", required=true) String findRule,
                                @RequestParam(name="store_path", required=true) String storePath,
                                @RequestParam(name="file_name", required=true) String fileName) {
        //println 'publish'
        service.publish(findRule, storePath, fileName)
        new RedirectView('/ui')
    }
}
