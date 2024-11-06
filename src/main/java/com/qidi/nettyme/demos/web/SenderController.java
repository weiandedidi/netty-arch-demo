package com.qidi.nettyme.demos.web;

import com.qidi.nettyme.demos.tcp.dto.Body;
import com.qidi.nettyme.demos.tcp.dto.CommonDto;
import com.qidi.nettyme.demos.tcp.dto.Header;
import com.qidi.nettyme.demos.tcp.dto.LoginBody;
import com.qidi.nettyme.demos.tcp.sender.SenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-05 16:47
 */
@RestController
@RequestMapping("/sender")
public class SenderController {
    @Autowired
    SenderService senderService;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseVO<String> send(@RequestBody Header header) {
        LoginBody loginBody = new LoginBody("9999", "哈哈哈");
        CommonDto<Body> commonDto = new CommonDto<>(header, loginBody);
        senderService.sendMessage(header.getClientId(), commonDto);
        return ResponseVO.successEmptyResponse();
    }
}
