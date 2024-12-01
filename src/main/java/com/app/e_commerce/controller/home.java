package com.app.e_commerce.controller;
import com.app.e_commerce.entity.Product;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.repository.UserRepo;
import com.app.e_commerce.services.ProductService;
import com.app.e_commerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping
public class home {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    ProductService productService;
    @GetMapping
    public String home(Model model)
    {
        List<Product> listProduct = productService.getAllProducts();
        model.addAttribute("products", listProduct);
        return "Layout";
    }
    @GetMapping("/user-list")
    public String getAllUser(Model model){
        List<User> listUser = userService.getAllUser();
        model.addAttribute("users", listUser);
        return "users/list-user";
    }
    @GetMapping("/search")
    public String searchProducts(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("products", productService.searchProducts(keyword));
        return "fragments/header";
    }



}
