/**
 * @(#)IndexController.java 2021/09/07.
 * <p>
 * Copyright(C) 2021 by PHOENIX TEAM.
 * <p>
 * Last_Update 2021/09/22.
 * Version 1.00.
 */
package com.example.bookstore.controller.user;

import com.example.bookstore.dao.MessageDao;
import com.example.bookstore.dao.ProductDao;
import com.example.bookstore.dao.TrainBookDao;
import com.example.bookstore.dao.UserDao;
import com.example.bookstore.entity.*;
import com.example.bookstore.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.bookstore.common.Constants;
import com.example.bookstore.service.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class de hien thi trang chu nguoi dung
 *
 * @author khoa-ph
 * @version 1.00
 */
@Controller
public class IndexController {
    @Autowired
    UserRoleService userRoleService;

    @Autowired
    ProductService productService;

    @Autowired
    ManufacturerService manufacturerService;

    @Autowired
    CommentService commentService;

    @Autowired
    BlogService blogService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ParamService paramService;

    @Autowired
    OrderService orderService;

    @Autowired
    FavoriteService favoriteService;

    @Autowired
    TrainBookDao trainBookDao;

    @Autowired
    ProductDao productDao;

    @Autowired
    MessageDao messageDao;

    @Autowired
    UserDao userDao;

    /**
     * Hien thi trang chu cua giao dien nguoi dung
     *
     * @return trang index.html
     */
    @GetMapping({"/", "/index"})
    public String index(Model model) {
        List<Product> products = productDao.getListProduct();
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "http://localhost:5000/add_intent";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        for (Product product : products) {
            String intentName = "BOOK" + product.getId();
            if (trainBookDao.existsByIntentName(intentName)) {
                continue;
            }

            List<String> trainingPhrases = Arrays.asList(
                    "Sách nào có " + product.getName() + "?",
                    "Shop có sách nào thuộc loại " + product.getCategory().getName() + "?",
                    "Shop có bán sách tên là " + product.getName() + "?"
            );
            String link = "http://localhost:8080/san-pham/" + product.getNamesearch();
            List<String> responses = Collections.singletonList(
                    "Bạn có thể xem sách ở <a href='" + link + "' target='_blank'>" + link + "</a>."
            );


            String jsonBody = String.format("{\"intent_name\": \"%s\", \"training_phrases\": [%s], \"responses\": [%s]}",
                    intentName,
                    String.join(", ", trainingPhrases.stream().map(p -> "\"" + p.trim() + "\"").collect(Collectors.toList())),
                    String.join(", ", responses.stream().map(r -> "\"" + r.trim() + "\"").collect(Collectors.toList()))
            );

            try {
                HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
                restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            } catch (Exception e) {

            }

            TrainBook trainBook = new TrainBook();
            trainBook.setId(intentName);
            trainBook.setName(product.getName());
            trainBook.setIntentName(intentName);
            trainBook.setTrainingPhrases(String.join("\n", trainingPhrases));
            trainBook.setResponses(String.join("\n", responses));
            trainBook.setDescription(product.getDescription());
            trainBook.setLinkProduct("http://localhost:8080/san-pham/" + product.getNamesearch());
            trainBook.setProductId(product.getId());
            trainBookDao.save(trainBook);
        }

        return Constants.USER_DISPLAY_INDEX;
    }

    @ModelAttribute("manufacturer")
    public List<Manufacturer> manufacture(Model model) {
        List<Manufacturer> list = manufacturerService.findAll();
        return list;
    }

    @ModelAttribute("latestProduct")
    public List<List<ShowProduct>> getLatestProduct(Model model) {
        List<Product> list = productService.getListLatestProduct();

        List<ShowProduct> temp = new ArrayList<>();

        List<List<ShowProduct>> result = new ArrayList<List<ShowProduct>>();

        for (int i = 0; i < list.size(); i++) {
            int totalStar = commentService.getAllStarCommentByProductNameSearch(list.get(i).getNamesearch());

            ShowProduct showProduct = new ShowProduct();
            showProduct.setProduct(list.get(i));
            showProduct.setTotalStar(totalStar);
            temp.add(showProduct);
            if (i % 2 != 0) {
                result.add(temp);
                temp = new ArrayList<>();
            }
            if (i == (list.size() - 1)) {
                if (i % 2 == 0) {
                    result.add(temp);
                    temp = new ArrayList<>();
                }
            }
        }

        return result;
    }

    @ModelAttribute("viewsProduct")
    public List<ShowProduct> getViewsProduct(Model model) {
        List<Product> list = productService.getListViewsProduct();
        List<ShowProduct> listProduct = new ArrayList<ShowProduct>();

        for (Product product : list) {
            ShowProduct showProduct = new ShowProduct();
            int totalStar = commentService.getAllStarCommentByProductNameSearch(product.getNamesearch());
            showProduct.setProduct(product);
            showProduct.setTotalStar(totalStar);
            listProduct.add(showProduct);
        }

        return listProduct;
    }

    @ModelAttribute("listBlog")
    public List<Blog> getListBlog(Model model) {
        List<Blog> listBlog = blogService.getSixBlog();
        for (Blog blog : listBlog) {
            String uploadDay = paramService.convertDate(blog.getUploadday());
            blog.setUploadday(uploadDay);
        }
        return listBlog;
    }

    @ModelAttribute("listSale")
    public List<ShowProduct> getListProductSale(Model model) {
        List<Product> list = productService.getListProductSales();

        List<ShowProduct> listProduct = new ArrayList<ShowProduct>();

        for (Product product : list) {
            ShowProduct showProduct = new ShowProduct();
            int totalStar = commentService.getAllStarCommentByProductNameSearch(product.getNamesearch());
            showProduct.setProduct(product);
            showProduct.setTotalStar(totalStar);
            listProduct.add(showProduct);
        }

        return listProduct;
    }

    @ModelAttribute("listBestSeller")
    public List<ShowProduct> getListBestSeller(Model model) {
        Pageable topFour = PageRequest.of(0, 4);

        List<BestSellerModel> list = orderService.getListBestSellerProduct(topFour);

        List<ShowProduct> listProduct = new ArrayList<ShowProduct>();

        for (BestSellerModel bestSeller : list) {
            ShowProduct showProduct = new ShowProduct();
            int totalStar = commentService.getAllStarCommentByProductNameSearch(bestSeller.getProduct().getNamesearch());
            showProduct.setProduct(bestSeller.getProduct());
            showProduct.setTotalStar(totalStar);
            listProduct.add(showProduct);
        }
        return listProduct;
    }

    @ModelAttribute("listFavorite")
    public List<ShowProduct> demo(Model model) {
        Pageable topFour = PageRequest.of(0, 4);
        List<BestSellerModel> list = favoriteService.getListBestSellerProduct(topFour);

        List<ShowProduct> listProduct = new ArrayList<ShowProduct>();

        for (BestSellerModel bestSeller : list) {
            ShowProduct showProduct = new ShowProduct();
            int totalStar = commentService.getAllStarCommentByProductNameSearch(bestSeller.getProduct().getNamesearch());
            showProduct.setProduct(bestSeller.getProduct());
            showProduct.setTotalStar(totalStar);
            listProduct.add(showProduct);
        }
        return listProduct;
    }


    private String flaskApiUrl = "http://127.0.0.1:5000";
    private final RestTemplate restTemplate;

    public IndexController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/chat")
    public String chat(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findUserByEmail(email);
        System.out.println("EMAIL: " + email);
        List<MessageDto> messageDtos = getAllByIds(user.getId());
        model.addAttribute("email", email);
        model.addAttribute("chats", messageDtos);

        return "user/chat";
    }


    @PostMapping("/chat/send")
    public ResponseEntity<Object> sendChatMessage(@RequestBody ChatRequest chatRequest) {
        String url = flaskApiUrl + "/generate_response";
        processMessage(chatRequest, true);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ChatRequest> entity = new HttpEntity<>(chatRequest, headers);

            ResponseEntity<ChatResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, ChatResponse.class
            );

            ChatResponse chatResponse = response.getBody();

            ChatRequest chatbotReq = new ChatRequest();
            chatbotReq.setMessage(chatResponse.getMessage());
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userDao.findUserByEmail(email);
            chatbotReq.setUserId(user.getId());
            processMessage(chatbotReq, false);

            return ResponseEntity.ok(chatResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi server");
        }
    }

    MessageDto processMessage(ChatRequest chatRequest, Boolean out) {
        Message message = new Message();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findUserByEmail(email);
        message.setCreatedAt(LocalDateTime.now());


        message.setMessage(chatRequest.getMessage());
        message.setOutgoingFromUser(out);
        message.setUser(user);
        message = messageDao.save(message);

        MessageDto chatDto = modelMapper.map(message, MessageDto.class);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdDate = message.getCreatedAt().format(formatter);
        chatDto.setCreatedDate(createdDate);
        chatDto.setUserId(message.getUser().getId());

        return chatDto;
    }

    public List<MessageDto> getAllByIds(Integer userId) {
        List<Message> messageEntities = messageDao.findAllByUserId(userId);
        List<MessageDto> dtos = messageEntities.stream().map(chatEntity -> {
            MessageDto messageDto = modelMapper.map(chatEntity, MessageDto.class);
            messageDto.setUserId(userId);
            messageDto.setMessage(messageDto.getMessage());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String createdDate = chatEntity.getCreatedAt().format(formatter);
            messageDto.setCreatedDate(createdDate);
            return messageDto;
        }).toList();
        return dtos;
    }


}
