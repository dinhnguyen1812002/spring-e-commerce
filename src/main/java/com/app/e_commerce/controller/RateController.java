package com.app.e_commerce.controller;

import com.app.e_commerce.entity.Product;
import com.app.e_commerce.entity.Rate;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.exception.ProductNotFoundException;
import com.app.e_commerce.services.ProductService;
import com.app.e_commerce.services.RateService;
import com.app.e_commerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/ratings")
public class RateController {

    @Autowired
    private RateService rateService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public String addRating(@RequestParam("productId") Long productId,
                            @RequestParam("comment") String comment,
                            @RequestParam("star") Integer star,
                            Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        Product product = productService
                .getProductById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

        Rate rate = new Rate();
        rate.setProduct(product);
        rate.setComment(comment);
        rate.setStar(star);
        rate.setUser(getCurrentUser(principal));

        rateService.saveRate(rate);

        return "redirect:/products/" + productId;
    }

    private User getCurrentUser(Principal principal) {
        String username = principal.getName();
        return userService.findByUsername(username);
    }
}