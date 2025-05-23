package kh.deli.domain.owner.controller;


import ch.qos.logback.core.net.SyslogOutputStream;
import com.google.gson.Gson;
import kh.deli.domain.owner.service.OwnerMenuService;
import kh.deli.global.entity.MenuDTO;
import kh.deli.global.entity.MenuOptionDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
@RequestMapping("/owner/menu/add")
public class MenuAddController {

    private final HttpSession session;
    private  final OwnerMenuService ownerMenuService;

    @RequestMapping("")

    public String toMenuAdd(Model model, Integer store_seq){

        model.addAttribute("store_seq", store_seq);
        return "/owner/menuAdd";
    }

    @RequestMapping("/menuAdd")
    public String menuAdd(MenuDTO menuDTO, MultipartFile file, int menu_seq) throws IOException {
        System.out.println("메뉴옵션없다");
        System.out.println(menu_seq+"메뉴 seqseq");
        System.out.println(menuDTO.getMenu_name());
        System.out.println(menuDTO.getMenu_price());



//        System.out.println(menu.getStore_seq());
//        System.out.println(menu.getMenu_sold_out());
//        System.out.println(menu.getMenu_num());
//        System.out.println(menu.getMenu_img());

//        System.out.println(menuDTO.getMenu_group());
//        System.out.println(menuDTO.getStore_seq());
//        System.out.println(menuDTO.getMenu_sold_out());
//        System.out.println(menuDTO.getMenu_num());
//        System.out.println(menuDTO.getMenu_img());


      ownerMenuService.insertMenu(menuDTO, file, menu_seq);


        return "redirect:/";
    }


    @RequestMapping("/menuAddAjax")
    @ResponseBody
    public String menuAdd(MenuOptionDTO menuOptionDTO) throws Exception {

        System.out.println("메뉴옵션없이 도달");
        System.out.println(new Gson().toJson(menuOptionDTO));

        System.out.println("이시발");
//        System.out.println(menuOptionDTO.getOption_price());
//        System.out.println(menuOptionDTO.getOption_name());
//


            ownerMenuService.insertMenuOption(menuOptionDTO);
            System.out.println("메뉴옵션추가");



        return "redirect:/";
    }

    @RequestMapping("getMenuSeq")
    @ResponseBody
    public int getMenuSeq() {
        int seq = ownerMenuService.getNextMenuSeq();

        System.out.println("AJAX");
        System.out.println(seq);
        return seq;
    }



}
